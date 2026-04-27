package com.example.lavaswimmer.mixin;

import com.example.lavaswimmer.enchantment.LavaSwimmerEnchantments;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.fog.FogData;
import net.minecraft.client.render.fog.LavaFogModifier;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LavaFogModifier.class)
public abstract class LavaFogModifierMixin {

    private static final int LAVA_SWIMMER_TINT = 0xE38A2E;

    @Inject(method = "getFogColor", at = @At("HEAD"), cancellable = true)
    private void lavaswimmer$overrideLavaFogColor(ClientWorld world, Camera camera, int skyColor, float tickProgress,
                                                   CallbackInfoReturnable<Integer> cir) {
        Entity focused = camera.getFocusedEntity();
        int level = getLavaSwimmerLevel(focused, world);
        if (level <= 0) {
            return;
        }

        // Si le joueur est immergé, désactiver le fog complètement
        if (focused != null && focused.isInLava()) {
            // Retourner la couleur du ciel pour que le fog soit invisible
            cir.setReturnValue(skyColor);
        } else {
            cir.setReturnValue(LAVA_SWIMMER_TINT);
        }
    }

    @Inject(method = "applyStartEndModifier", at = @At("TAIL"))
    private void lavaswimmer$extendLavaFogDistance(FogData fogData, Entity entity, BlockPos blockPos, ClientWorld world,
                                                    float viewDistance, RenderTickCounter tickCounter, CallbackInfo ci) {
        int level = getLavaSwimmerLevel(entity, world);
        if (level <= 0) {
            return;
        }

        // Si le joueur est dans la lave, désactiver complètement le fog
        if (entity != null && entity.isInLava()) {
            // Désactiver le fog en mettant les distances à une valeur très élevée
            fogData.environmentalStart = Float.MAX_VALUE;
            fogData.renderDistanceStart = Float.MAX_VALUE;
            fogData.environmentalEnd = Float.MAX_VALUE;
            fogData.renderDistanceEnd = Float.MAX_VALUE;
            return;
        }

        // Sinon, augmenter la distance de visibilité pour les niveaux 2 et 3
        float multiplier;
        if (level == 1) {
            multiplier = 1.5f;  // +50%
        } else if (level == 2) {
            multiplier = 2.5f;  // +150% (vraiment augmenté)
        } else {
            multiplier = 3.5f;  // +250% (maximum)
        }

        float fogEnd = Math.max(32.0f, viewDistance * multiplier);
        fogData.environmentalStart = 0.0f;
        fogData.renderDistanceStart = 0.0f;
        fogData.environmentalEnd = fogEnd;
        fogData.renderDistanceEnd = fogEnd;
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
