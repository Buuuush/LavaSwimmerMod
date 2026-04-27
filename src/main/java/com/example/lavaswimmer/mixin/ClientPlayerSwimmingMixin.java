package com.example.lavaswimmer.mixin;

import com.example.lavaswimmer.enchantment.LavaSwimmerEnchantments;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.EntityPose;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerSwimmingMixin {

    @Inject(method = "tick", at = @At("TAIL"))
    private void lavaswimmer$forceSwimmingClient(CallbackInfo ci) {
        ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;
        ClientWorld world = MinecraftClient.getInstance().world;

        if (!player.isInLava()) return;

        int level = getLavaSwimmerLevel(player, world);
        if (level <= 0) return;

        // Forcer la pose swimming côté client pour que la caméra F5 montre la nage
        player.setSwimming(true);
        player.setPose(EntityPose.SWIMMING);
    }

    private static int getLavaSwimmerLevel(Entity entity, ClientWorld world) {
        if (!(entity instanceof LivingEntity living)) {
            return 0;
        }

        RegistryEntryLookup<Enchantment> enchantmentLookup = world
                .getRegistryManager()
                .getOrThrow(RegistryKeys.ENCHANTMENT);

        RegistryEntry<Enchantment> lavaSwimmerEntry = enchantmentLookup
                .getOptional(LavaSwimmerEnchantments.LAVA_SWIMMER)
                .orElse(null);

        if (lavaSwimmerEntry == null) {
            return 0;
        }

        return EnchantmentHelper.getEquipmentLevel(lavaSwimmerEntry, living);
    }
}
