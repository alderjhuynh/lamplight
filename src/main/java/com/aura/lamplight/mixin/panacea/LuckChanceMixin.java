package com.aura.lamplight.mixin.panacea;

import net.minecraft.util.Mth;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LootItemRandomChanceCondition.class)
public abstract class LuckChanceMixin {

    @Shadow
    @Final
    private NumberProvider chance;

    @Inject(method = "test(Lnet/minecraft/world/level/storage/loot/LootContext;)Z", at = @At("HEAD"), cancellable = true)
    private void panacea$luckShiftsRareDrops(LootContext context, CallbackInfoReturnable<Boolean> cir) {
        float probability = this.chance.getFloat(context);
        float adjusted = Mth.clamp(probability + context.getLuck() * 0.01F, 0.0F, 1.0F);
        cir.setReturnValue(context.getRandom().nextFloat() < adjusted);
    }
}
