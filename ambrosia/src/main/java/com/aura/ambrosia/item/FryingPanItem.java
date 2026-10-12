package com.aura.ambrosia.item;

import com.aura.ambrosia.food.PanFriedFoods;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class FryingPanItem extends Item {
    public static final int COOK_TIME_TICKS = 100;

    public FryingPanItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null || context.getHand() != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }
        Level level = context.getLevel();
        if (!isLitCampfire(level, context.getClickedPos())) {
            return InteractionResult.PASS;
        }
        ItemStack offhand = player.getOffhandItem();
        if (!PanFriedFoods.isUncookedFood(level, offhand)) {
            return InteractionResult.PASS;
        }
        player.startUsingItem(context.getHand());
        return InteractionResult.CONSUME;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity user) {
        return COOK_TIME_TICKS;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.BLOCK;
    }

    @Override
    public void onUseTick(Level level, LivingEntity user, ItemStack pan, int remaining) {
        if (!(user instanceof Player player)) {
            user.releaseUsingItem();
            return;
        }
        HitResult hit = ProjectileUtil.getHitResultOnViewVector(
                player, EntitySelector.CAN_BE_PICKED, player.blockInteractionRange());
        if (hit.getType() != HitResult.Type.BLOCK) {
            user.releaseUsingItem();
            return;
        }
        BlockPos pos = ((BlockHitResult) hit).getBlockPos();
        if (!isLitCampfire(level, pos)) {
            user.releaseUsingItem();
            return;
        }
        if (!PanFriedFoods.isUncookedFood(level, player.getOffhandItem())) {
            user.releaseUsingItem();
            return;
        }

        if (!level.isClientSide() && level instanceof ServerLevel server) {
            Vec3 at = ((BlockHitResult) hit).getLocation();
            int elapsed = getUseDuration(pan, user) - remaining;
            if (elapsed % 10 == 0) {
                server.sendParticles(ParticleTypes.FLAME,
                        at.x, at.y + 0.35, at.z, 2, 0.15, 0.1, 0.15, 0.01);
                server.sendParticles(ParticleTypes.SMOKE,
                        at.x, at.y + 0.6, at.z, 2, 0.15, 0.2, 0.15, 0.02);
                level.playSound(null, pos, SoundEvents.CAMPFIRE_CRACKLE, SoundSource.BLOCKS, 0.6F,
                        1.1F + level.getRandom().nextFloat() * 0.3F);
            }
        }
    }

    @Override
    public ItemStack finishUsingItem(ItemStack pan, Level level, LivingEntity user) {
        if (!(user instanceof Player player)) {
            return pan;
        }
        if (level.isClientSide()) {
            return pan;
        }
        ItemStack raw = player.getOffhandItem().copyWithCount(1);
        if (!PanFriedFoods.isUncookedFood(level, raw)) {
            return pan;
        }
        Optional<ItemStack> fried = PanFriedFoods.panFry(level, raw);
        if (fried.isEmpty()) {
            return pan;
        }
        if (!player.hasInfiniteMaterials()) {
            player.getOffhandItem().shrink(1);
        }
        ItemStack result = fried.get();
        BlockPos at = player.blockPosition();
        if (player.getOffhandItem().isEmpty()) {
            player.setItemInHand(InteractionHand.OFF_HAND, result.copy());
        } else if (!player.getInventory().add(result.copy())) {
            player.drop(result.copy(), false);
        }
        player.getCooldowns().addCooldown(pan, 10);
        level.playSound(null, player, SoundEvents.GENERIC_EXTINGUISH_FIRE, SoundSource.PLAYERS, 0.8F, 1.0F);
        level.playSound(null, at, SoundEvents.FURNACE_FIRE_CRACKLE, SoundSource.BLOCKS, 0.8F, 1.2F);
        if (level instanceof ServerLevel server) {
            Vec3 look = player.getLookAngle();
            server.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                    player.getX() + look.x, player.getY() + 1.2, player.getZ() + look.z,
                    6, 0.1, 0.1, 0.1, 0.03);
        }
        return pan;
    }

    public static boolean isLitCampfire(Level level, BlockPos pos) {
        if (level == null || pos == null) {
            return false;
        }
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof CampfireBlock)) {
            return false;
        }
        return CampfireBlock.isLitCampfire(state);
    }
}
