package com.example.lavaswimmer;

import com.example.lavaswimmer.enchantment.LavaSwimmerEnchantments;
import net.fabricmc.api.ModInitializer;

public class LavaSwimmerMod implements ModInitializer {
    @Override
    public void onInitialize() {
        LavaSwimmerEnchantments.initialize();
        LavaSwimHandler.init();
    }
}