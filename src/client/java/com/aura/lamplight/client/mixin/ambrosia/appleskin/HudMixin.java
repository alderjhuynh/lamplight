package com.aura.lamplight.client.mixin.ambrosia.appleskin;

import com.aura.lamplight.ambrosia.client.appleskin.AppleSkin;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Hud.class)
public class HudMixin {

    @Inject(method = "extractFood", at = @At("HEAD"))
    private void ambrosia$appleSkinExtractFoodPre(GuiGraphicsExtractor extractor, Player player, int top, int right, CallbackInfo ci) {
        AppleSkin.INSTANCE.onExtractFoodPre(extractor, player, top, right, ambrosia$getGuiTicks());
    }

    @Inject(method = "extractFood", at = @At("RETURN"))
    private void ambrosia$appleSkinExtractFoodPost(GuiGraphicsExtractor extractor, Player player, int top, int right, CallbackInfo ci) {
        AppleSkin.INSTANCE.onExtractFoodPost(extractor, player, top, right, ambrosia$getGuiTicks());
    }

    @Inject(method = "extractPlayerHealth", at = @At("RETURN"))
    private void ambrosia$appleSkinExtractPlayerHealth(GuiGraphicsExtractor extractor, CallbackInfo ci) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        int left = extractor.guiWidth() / 2 - 91;
        int top = extractor.guiHeight() - 39;

        AppleSkin.INSTANCE.onExtractHealth(extractor, player, left, top, ambrosia$getGuiTicks());
    }

    private int ambrosia$getGuiTicks() {
        return ((Hud) (Object) this).getGuiTicks();
    }
}
