package com.example.lavaswimmer;

import com.example.lavaswimmer.enchantment.LavaSwimmerEnchantments;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.Vec3d;

public class LavaSwimHandler {
    private static final double BASE_SWIM_BOOST = 0.2;  // x5 boost pour le sprint

    public static void init() {
        ServerTickEvents.START_SERVER_TICK.register(server -> {
            RegistryEntryLookup<Enchantment> enchantmentLookup = server
                    .getRegistryManager()
                    .getOrThrow(RegistryKeys.ENCHANTMENT);
            
            RegistryEntry<Enchantment> lavaSwimmerEntry = enchantmentLookup
                    .getOptional(LavaSwimmerEnchantments.LAVA_SWIMMER)
                    .orElse(null);
            
            if (lavaSwimmerEntry == null) return;

            for (PlayerEntity player : server.getPlayerManager().getPlayerList()) {
                if (!player.isInLava()) continue;

                int level = EnchantmentHelper.getEquipmentLevel(lavaSwimmerEntry, player);
                if (level <= 0) continue;

                // Force agressive la posture de nage en lave
                player.setSwimming(true);
                player.setPose(EntityPose.SWIMMING);

                // Annule les dégâts de chute en lave si Fire Resistance active
                // (traite la lave comme de l'eau)
                if (player.hasStatusEffect(StatusEffects.FIRE_RESISTANCE)) {
                    player.fallDistance = 0;
                }

                // Ajoute un boost directionnel puissant pendant la nage sprint (x5)
                if (player.isSprinting()) {
                    Vec3d look = player.getRotationVector();
                    double swimBoost = BASE_SWIM_BOOST * level;
                    Vec3d velocity = player.getVelocity().add(look.multiply(swimBoost));
                    player.setVelocity(velocity);
                    player.velocityModified = true;
                }

                // === OXYGÈNE (identique à l'eau vanilla) ===
                int currentAir = player.getAir();
                if (currentAir > -20) {
                    // Consomme 1 tick d'air par tick, comme dans l'eau
                    player.setAir(currentAir - 1);
                    if (player.getAir() <= -20) {
                        // Reinitialise le compteur et inflige 2 dégâts de noyade
                        player.setAir(0);
                        player.damage(
                            ((net.minecraft.server.world.ServerWorld) player.getEntityWorld()),
                            player.getDamageSources().drown(),
                            2.0f
                        );
                    }
                }
            }
        });
    }
} 