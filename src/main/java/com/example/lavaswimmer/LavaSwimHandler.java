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
    private static final double HORIZONTAL_SPEED_MULTIPLIER = 2.0;  // Double la vitesse horizontale vanilla
    private static final double DOWNWARD_GRAVITY_MULTIPLIER = 1.0 / 3.0; // Descend 3x plus lentement

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
            }
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
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

                // Récupère la vélocité actuelle
                Vec3d currentVelocity = player.getVelocity();

                // Double la vitesse horizontale par rapport à la vanilla
                double newY = currentVelocity.y;
                if (newY < 0) {
                    // Réduit uniquement la chute, sans bloquer la montée
                    newY = newY * DOWNWARD_GRAVITY_MULTIPLIER;
                }

                Vec3d velocity = new Vec3d(
                        currentVelocity.x * HORIZONTAL_SPEED_MULTIPLIER,
                        newY,
                        currentVelocity.z * HORIZONTAL_SPEED_MULTIPLIER
                );
                
                player.setVelocity(velocity);
                player.velocityModified = true;

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