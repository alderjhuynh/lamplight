package com.aura.lamplight.mixin.ambrosia;

import com.aura.lamplight.ambrosia.food.VanillaFoodTweaks;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Holder.Reference.class)
public abstract class VanillaFoodOverrideMixin {

    @Inject(method = "components", at = @At("RETURN"), cancellable = true)
    private void ambrosia$applyFoodTweak(CallbackInfoReturnable<DataComponentMap> cir) {
        Holder.Reference<?> self = (Holder.Reference<?>) (Object) this;
        final Object value;
        try {
            value = self.value();
        } catch (IllegalStateException ignored) {
            return;
        }
        if (!(value instanceof Item item)) {
            return;
        }
        DataComponentMap original = cir.getReturnValue();
        DataComponentMap patched = VanillaFoodTweaks.patch(item, original);
        if (patched != original) {
            cir.setReturnValue(patched);
        }
    }
}
