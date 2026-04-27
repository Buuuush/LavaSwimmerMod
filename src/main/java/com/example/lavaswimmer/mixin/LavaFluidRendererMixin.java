package com.example.lavaswimmer.mixin;

import com.example.lavaswimmer.enchantment.LavaSwimmerEnchantments;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.block.FluidRenderer;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FluidRenderer.class)
public abstract class LavaFluidRendererMixin {

    private static final double VISIBILITY_RADIUS = 3.0; // Rayon de 3 blocs

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void lavaswimmer$hideLavaWhenSubmerged(BlockRenderView world, BlockPos pos, VertexConsumer vertexConsumer,
                                                   BlockState state, FluidState fluidState, CallbackInfo ci) {
        if (!fluidState.isIn(FluidTags.LAVA)) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        PlayerEntity player = client.player;
        ClientWorld clientWorld = client.world;
        if (player == null || clientWorld == null) {
            return;
        }

        if (!player.isInLava()) {
            return;
        }

        // Vérifier que le bloc est dans le rayon de visibilité
        double distance = Math.sqrt(pos.getSquaredDistance(player.getBlockPos()));
        if (distance > VISIBILITY_RADIUS) {
            return; // Ne pas cacher les blocs loin du joueur
        }

        RegistryEntryLookup<Enchantment> enchantmentLookup = clientWorld
                .getRegistryManager()
                .getOrThrow(RegistryKeys.ENCHANTMENT);

        RegistryEntry<Enchantment> lavaSwimmerEntry = enchantmentLookup
                .getOptional(LavaSwimmerEnchantments.LAVA_SWIMMER)
                .orElse(null);

        if (lavaSwimmerEntry == null) {
            return;
        }

        if (EnchantmentHelper.getEquipmentLevel(lavaSwimmerEntry, player) <= 0) {
            return;
        }

        // Cache le rendu des faces de lave quand le joueur est immergé et possède l'enchantement
        ci.cancel();
    }
}
