package com.aura.lamplight.client.mixin.folio;

import com.aura.lamplight.folio.effect.Frozen;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(KeyMapping.class)
public abstract class FrozenKeyMappingMixin {

    @Shadow
    private int clickCount;

    @Inject(method = "isDown", at = @At("HEAD"), cancellable = true)
    private void folio$frozenNotHeld(final CallbackInfoReturnable<Boolean> cir) {
        if (folio$silenced()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "consumeClick", at = @At("HEAD"), cancellable = true)
    private void folio$frozenNotClicked(final CallbackInfoReturnable<Boolean> cir) {
        if (folio$silenced()) {
            this.clickCount = 0;
            cir.setReturnValue(false);
        }
    }

    private boolean folio$silenced() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.player == null || !Frozen.is(minecraft.player)) {
            return false;
        }
        KeyMapping.Category category = ((KeyMapping) (Object) this).getCategory();
        return category != KeyMapping.Category.MISC
            && category != KeyMapping.Category.MULTIPLAYER
            && category != KeyMapping.Category.DEBUG;
    }
}
