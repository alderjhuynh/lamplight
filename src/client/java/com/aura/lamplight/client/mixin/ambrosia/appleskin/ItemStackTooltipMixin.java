package com.aura.lamplight.client.mixin.ambrosia.appleskin;

import com.aura.lamplight.ambrosia.client.appleskin.TooltipOverlayHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ItemStack.class)
public abstract class ItemStackTooltipMixin {

    @Inject(at = @At("RETURN"), method = "getTooltipLines")
    private void ambrosia$appendFoodOverlay(Item.TooltipContext context, Player player, TooltipFlag type,
                                           CallbackInfoReturnable<List<Component>> cir) {
        TooltipOverlayHandler.INSTANCE.onItemTooltip((ItemStack) (Object) this, context, type, cir.getReturnValue());
    }
}
