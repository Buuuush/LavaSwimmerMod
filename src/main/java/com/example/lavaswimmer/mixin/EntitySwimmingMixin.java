package com.example.lavaswimmer.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntitySwimmingMixin {
    
    @Inject(method = "isSwimming", at = @At("RETURN"), cancellable = true)
    private void lavaswimmer$isSwimmingInLava(CallbackInfoReturnable<Boolean> cir) {
        Entity entity = (Entity)(Object) this;
        
        // Ne s'applique que aux joueurs
        if (!(entity instanceof PlayerEntity player)) {
            return;
        }
        
        // Si déjà en nage (eau), ne pas modifier
        if (cir.getReturnValue()) {
            return;
        }
        
        // Vérifier si le joueur est en lave
        if (!player.isInLava()) {
            return;
        }
        
        // Vérifier si le joueur a l'enchantement Lava Swimmer
        if (hasLavaSwimmerEnchantment(player)) {
            cir.setReturnValue(true);
        }
    }
    
    private static boolean hasLavaSwimmerEnchantment(PlayerEntity player) {
        // Vérifier main hand
        if (hasEnchantmentOnStack(player.getMainHandStack())) {
            return true;
        }
        
        // Vérifier off hand
        if (hasEnchantmentOnStack(player.getOffHandStack())) {
            return true;
        }
        
        // Vérifier l'armure (slots 36-39 : bottes, leggings, chestplate, helmet)
        for (int i = 36; i <= 39; i++) {
            if (hasEnchantmentOnStack(player.getInventory().getStack(i))) {
                return true;
            }
        }
        
        return false;
    }
    
    private static boolean hasEnchantmentOnStack(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        
        try {
            var enchantmentComponent = stack.getEnchantments();
            
            // Itérer sur les enchantements
            for (var entry : enchantmentComponent.getEnchantmentEntries()) {
                RegistryEntry<Enchantment> enchantment = entry.getKey();
                
                // Vérifier si l'enchantement a l'ID "lavaswimmer:lava_swimmer"
                if (enchantment.getKey().isPresent()) {
                    Identifier id = enchantment.getKey().get().getValue();
                    if (id.getNamespace().equals("lavaswimmer") && id.getPath().equals("lava_swimmer")) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            // Si ça échoue, ignorer
        }
        
        return false;
    }
}
