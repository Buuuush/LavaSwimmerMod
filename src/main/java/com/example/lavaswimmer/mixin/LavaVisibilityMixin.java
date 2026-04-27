package com.example.lavaswimmer.mixin;

import com.example.lavaswimmer.enchantment.LavaSwimmerEnchantments;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.state.OutlineRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class LavaVisibilityMixin {

    @Inject(method = "drawBlockOutline", at = @At("HEAD"), cancellable = true)
    private void lavaswimmer$hideLavaOutline(MatrixStack matrices, VertexConsumer vertexConsumer,
                                            double x, double y, double z,
                                            OutlineRenderState outlineState, int color, CallbackInfo ci) {
        // Vérifie si c'est de la lave en comparant la couleur de l'outline
        // La lave a une couleur orange/rouge caractéristique
        if (isLavaOutline(color) && hasLavaSwimmer()) {
            ci.cancel();
        }
    }

    private static boolean isLavaOutline(int color) {
        // La lave a une couleur orange/rouge (valeur approximative)
        // Format ARGB: alpha=255, red=255, green=100, blue=0
        int red = (color >> 16) & 0xFF;
        int green = (color >> 8) & 0xFF;
        int blue = color & 0xFF;

        // Vérifie si la couleur est dans la plage de la lave (orange/rouge)
        return red > 200 && green < 150 && blue < 100;
    }

    private static boolean hasLavaSwimmer() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.player == null) return false;

        RegistryEntryLookup<Enchantment> enchantmentLookup = client.world.getRegistryManager()
                .getOrThrow(RegistryKeys.ENCHANTMENT);

        RegistryEntry<Enchantment> lavaSwimmerEntry = enchantmentLookup
                .getOptional(LavaSwimmerEnchantments.LAVA_SWIMMER)
                .orElse(null);

        if (lavaSwimmerEntry == null) return false;

        return EnchantmentHelper.getEquipmentLevel(lavaSwimmerEntry, client.player) > 0;
    }
}
