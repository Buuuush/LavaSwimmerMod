package com.example.lavaswimmer.enchantment;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public final class LavaSwimmerEnchantments {

    public static final RegistryKey<Enchantment> LAVA_SWIMMER =
            RegistryKey.of(RegistryKeys.ENCHANTMENT,
                    Identifier.of("lavaswimmer", "lava_swimmer"));

    public static void initialize() {
        // Rien à enregistrer en code : l'enchantement est défini en JSON
    }
}