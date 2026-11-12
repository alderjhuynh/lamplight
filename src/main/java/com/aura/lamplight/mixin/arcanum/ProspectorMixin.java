package com.aura.lamplight.mixin.arcanum;

import com.aura.lamplight.arcanum.enchantment.ArcanumEnchantments;
import com.aura.lamplight.arcanum.server.RebuildHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Block.class)
public class ProspectorMixin {

    @Inject(method = "tryDropExperience", at = @At("HEAD"), cancellable = true)
    private void lamplight$prospector(ServerLevel level, BlockPos pos, ItemStack tool, IntProvider xp,
                                      CallbackInfo ci) {
        int lvl = RebuildHandler.lvl(tool, ArcanumEnchantments.PROSPECTOR);
        if (lvl <= 0) return;
        ci.cancel();
        int amount = EnchantmentHelper.processBlockExperience(level, tool, xp.sample(level.getRandom()));
        if (amount <= 0) return;
        if ((Boolean) level.getGameRules().get(GameRules.BLOCK_DROPS)) {
            ExperienceOrb.award(level, Vec3.atCenterOf(pos), amount + 2 * lvl);
        }
    }
}
