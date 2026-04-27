package com.example.lavaswimmer.mixin;

import com.example.lavaswimmer.enchantment.LavaSwimmerEnchantments;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.EntityPose;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.world.World;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRenderSwimmingMixin {

    @Inject(method = "render", at = @At("HEAD"))
    private void lavaswimmer$forceSwimmingRender(LivingEntityRenderState renderState, MatrixStack matrixStack,
                                                 OrderedRenderCommandQueue renderQueue, CameraRenderState cameraState,
                                                 CallbackInfo ci) {
        PlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return;

        if (!player.isInLava()) return;

        ClientWorld world = MinecraftClient.getInstance().world;
        if (world == null) return;

        int level = getLavaSwimmerLevel(player, world);
        if (level <= 0) return;

        // Force la pose SWIMMING juste pendant le rendu
        player.setSwimming(true);
        player.setPose(EntityPose.SWIMMING);
    }

    private static int getLavaSwimmerLevel(PlayerEntity player, World world) {
        RegistryEntryLookup<Enchantment> enchantmentLookup = world
                .getRegistryManager()
                .getOrThrow(RegistryKeys.ENCHANTMENT);

        RegistryEntry<Enchantment> lavaSwimmerEntry = enchantmentLookup
                .getOptional(LavaSwimmerEnchantments.LAVA_SWIMMER)
                .orElse(null);

        if (lavaSwimmerEntry == null) {
            return 0;
        }

        return EnchantmentHelper.getEquipmentLevel(lavaSwimmerEntry, player);
    }
}
