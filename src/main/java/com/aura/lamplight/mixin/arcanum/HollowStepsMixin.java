package com.aura.lamplight.mixin.arcanum;

import com.aura.lamplight.arcanum.enchantment.ArcanumEnchantments;
import com.aura.lamplight.arcanum.server.RebuildHandler;
import net.minecraft.core.Holder;
import net.minecraft.tags.GameEventTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.gameevent.GameEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class HollowStepsMixin {

    @Inject(method = "gameEvent(Lnet/minecraft/core/Holder;Lnet/minecraft/world/entity/Entity;)V", at = @At("HEAD"), cancellable = true)
    private void lamplight$hollowSteps(Holder<GameEvent> event, Entity context, CallbackInfo ci) {
        if (event.is(GameEventTags.VIBRATIONS) && (Object) this instanceof Player player) {
            int lvl = RebuildHandler.armorLvl(player, ArcanumEnchantments.HOLLOW_STEPS);
            if (lvl > 0) ci.cancel();
        }
    }
}
