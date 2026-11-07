package com.aura.lamplight.client.mixin.puppeteer.fpanims;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.aura.lamplight.puppeteer.fpanims.FpAnimsClient;
import com.aura.lamplight.puppeteer.fpanims.FpAnimsConfig;
import com.aura.lamplight.puppeteer.fpanims.BlockEmissionRenderer;
import com.aura.lamplight.puppeteer.fpanims.FpAnimsTags;
import com.aura.lamplight.puppeteer.fpanims.RenderHelper;
import com.aura.lamplight.puppeteer.fpanims.TorchDetect;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.model.animal.axolotl.AdultAxolotlModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BellRenderState;
import net.minecraft.client.renderer.blockentity.BellRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.AxolotlRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.LingeringPotionItem;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SplashPotionItem;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.ComparatorBlock;
import net.minecraft.world.level.block.RedstoneTorchBlock;
import net.minecraft.world.level.block.RepeaterBlock;
import net.minecraft.world.level.block.entity.BellBlockEntity;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.PistonType;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings ("all")
@Mixin(ItemInHandRenderer.class)
public abstract class HeldItemsMixin {
    @Unique
    private static boolean fpanims$isOffsetHeldItem(ItemStack stack, Identifier id) {
        if (stack.is(ItemTags.SPEARS)) return true;
        return id != null && id.getPath().contains("scythe");
        // DUDE I SWEAR TO GOD I MADE A SCYTHE ITEM TAG WHERE IS IT
    }

    @Unique
    private static final Identifier fpanims$CAVE_MAP = Identifier.fromNamespaceAndPath("alexscaves", "cave_map");

    private float pistonCount = 0.0F;
    private boolean repPower = false;
    private float prevAge = 0.0F;
    private float swingAngleY = 0.0F;
    private float swingAngleX = 0.0F;
    private float swingVelocityY = 0.0F;
    private float swingVelocityX = 0.0F;
    private float swingVelocityZ = 0.0F;
    private float vertAngleY = 0.0F;
    private float vertVelocityYSlime = 0.0F;
    private float vertAngleYSlime = 0.0F;
    private float riptideCounter = 0.0F;
    private float netherCounter = 0.0F;
    private float headFallCount = 0.0F;
    private static final float SOUND_TRIGGER_THRESHOLD = 13.5F;
    private static final float SOUND_COOLDOWN = 14.5F;
    private float soundCooldownTimer = 0.0F;
    private float globalAnimationCounter = 0.0F;
    private float fallCounter = 0.0F;
    private boolean useItemPrev = false;
    private float inWaterCounter = 0.0F;
    private float freezeCounter = 0.0F;
    private float clCount = 0.0F;
    private float crawlCount = 0.0F;
    private float directionalCrawlCount = 0.0F;
    private float climbCount = 0.0F;
    private boolean isAttacking = false;
    private boolean left = false;
    private float itemSwitchCountO = 0.0F;
    private boolean itemSwitchedEventO = false;
    private float itemSwitchCount = 0.0F;
    private boolean itemSwitchedEvent = false;
    private ItemStack prevItemM = ItemStack.EMPTY;
    private ItemStack prevItemO = ItemStack.EMPTY;
    private boolean wasSubmerged;
    private float inspectCounter;
    private float inspectSpinCounter;
    private boolean isNearRedstonePrev;
    private boolean isCrouchingPrev;
    private float shieldCounterO;
    private float shieldCounter;
    private boolean shieldEvent;
    private boolean shieldEventO;
    private static AdultAxolotlModel fpanims$axolotlModel;

    @Shadow
    private ItemStack offHandItem;

    @Shadow
    public abstract void renderItem(LivingEntity entity, ItemStack stack, ItemDisplayContext type, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords);

    @Shadow
    private void renderPlayerArm(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, float inverseArmHeight, float attackValue, HumanoidArm arm) {
    }

    @Shadow
    private void applyItemArmTransform(PoseStack poseStack, HumanoidArm arm, float inverseArmHeight) {
    }

    @Shadow
    private void applyItemArmAttackTransform(PoseStack poseStack, HumanoidArm arm, float attackValue) {
    }

    @Shadow
    protected void renderTwoHandedMap(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, float xRot, float inverseArmHeight, float attackValue) {
    }

    @Shadow
    protected void renderOneHandedMap(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, float inverseArmHeight, HumanoidArm arm, float attackValue, ItemStack map) {
    }

    private float easeInOutBack(float x) {
        float c1 = 1.70158F;
        float c2 = c1 * 1.525F;
        return x < 0.5F
            ? (float) (Math.pow(2.0F * x, 2.0F) * ((c2 + 1.0F) * 2.0F * x - c2) / 2.0F)
            : (float) ((Math.pow(2.0F * x - 2.0F, 2.0F) * ((c2 + 1.0F) * (x * 2.0F - 2.0F) + c2) + 2.0F) / 2.0F);
    }

    private float getAttackDamage(ItemStack stack) {
        float totalDamage = 0.0F;
        ItemAttributeModifiers modifiers = stack.get(DataComponents.ATTRIBUTE_MODIFIERS);
        if (modifiers == null) {
            return totalDamage;
        }
        for (ItemAttributeModifiers.Entry entry : modifiers.modifiers()) {
            if (entry.attribute().equals(Attributes.ATTACK_DAMAGE)) {
                totalDamage += (float) entry.modifier().amount();
            }
        }
        return totalDamage;
    }

    private boolean isNearRedstoneBlock(AbstractClientPlayer player) {
        BlockPos standingPos = BlockPos.containing(player.getX(), player.getY() - 0.1, player.getZ());
        BlockState standingBlock = player.level().getBlockState(standingPos);
        return standingBlock.is(Blocks.REDSTONE_BLOCK);
    }

    private void swingArm(float swingProgress, float equipProgress, PoseStack matrices, int armX, HumanoidArm arm) {
        float f = -0.4F * Mth.sin(Mth.sqrt(swingProgress) * (float) Math.PI);
        float g = 0.2F * Mth.sin(Mth.sqrt(swingProgress) * ((float) Math.PI * 2F));
        float h = -0.2F * Mth.sin(swingProgress * (float) Math.PI);
        matrices.translate((float) armX * f, g, h);
        this.applyItemArmTransform(matrices, arm, equipProgress);
        this.applyItemArmAttackTransform(matrices, arm, swingProgress);
    }

    private void altSwing(PoseStack matrices, HumanoidArm arm, float swingProgress) {
        int direction = arm == HumanoidArm.RIGHT ? 1 : -1;
        float swingSin = Mth.sin(swingProgress * (float) Math.PI);
        matrices.mulPose(Axis.YP.rotationDegrees((float) direction * (45.0F + swingSin * 0.0F)));
        matrices.mulPose(Axis.YP.rotationDegrees((float) direction * -45.0F));
    }

    private boolean isInAnyFluid(AbstractClientPlayer player) {
        return player.isInWater() || player.isInLava() || player.isInPowderSnow;
    }

    @Inject(
        method = "submitArmWithItem",
        at = @At("HEAD"),
        cancellable = true
    )
    private void fpanims$onSubmitArmWithItem(AbstractClientPlayer player, float partialTicks, float pitch, InteractionHand hand, float swingProgress, ItemStack stack, float equipProgress, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int light, CallbackInfo ci) {
        if (hand == InteractionHand.MAIN_HAND) {
            FpAnimsClient.updateFrameTime();
        }
        boolean isUsingSandpaper = player.getMainHandItem().getItem().toString().contains("sand_paper") && player.isUsingItem() && player.getUsedItemHand() == InteractionHand.MAIN_HAND
            || player.getOffhandItem().getItem().toString().contains("sand_paper") && player.isUsingItem() && player.getUsedItemHand() == InteractionHand.OFF_HAND;
        if (!isUsingSandpaper) {
            if (FpAnimsConfig.ENABLE_PUNCHING || !stack.isEmpty() || player.isSwimming() || player.isVisuallyCrawling() || player.onClimbable()) {
                Item item = stack.getItem();
                Identifier itemId = BuiltInRegistries.ITEM.getKey(item);
                Item mainHandItem = player.getMainHandItem().getItem();
                Identifier mainHandId = BuiltInRegistries.ITEM.getKey(mainHandItem);
                if (!fpanims$CAVE_MAP.equals(itemId) && !fpanims$CAVE_MAP.equals(mainHandId) && !fpanims$CAVE_MAP.equals(BuiltInRegistries.ITEM.getKey(player.getOffhandItem().getItem()))) {
                    List<String> blockedMods = FpAnimsConfig.MOD_IDS_TO_EXCLUDE;
                    List<String> blockedItems = FpAnimsConfig.ITEM_IDS_TO_EXCLUDE;
                    if (itemId != null) {
                        String namespace = itemId.getNamespace().toLowerCase();
                        String fullId = itemId.toString().toLowerCase();
                        if (blockedMods.contains(namespace) || blockedItems.contains(fullId)) {
                            return;
                        }
                    }

                    if (mainHandId != null) {
                        String namespace = mainHandId.getNamespace().toLowerCase();
                        String fullId = mainHandId.toString().toLowerCase();
                        if (blockedMods.contains(namespace) || blockedItems.contains(fullId)) {
                            return;
                        }
                    }

                    double tt = FpAnimsClient.deltaTime * FpAnimsConfig.ANIMATION_SPEED * (double) 30.0F;
                    boolean bl = hand == InteractionHand.MAIN_HAND;
                    HumanoidArm arm = bl ? player.getMainArm() : player.getMainArm().getOpposite();
                    float kj = bl ? 1.0F : -1.0F;
                    poseStack.pushPose();
                    poseStack.pushPose();
                    poseStack.translate(FpAnimsConfig.getXOffset() * kj, FpAnimsConfig.getYOffset(), FpAnimsConfig.getZOffset());
                    if (player.getMainHandItem().getItem() != this.prevItemM.getItem()) {
                        this.itemSwitchedEvent = true;
                        this.itemSwitchCount = 0.0F;
                        player.playSound((SoundEvent) SoundEvents.ARMOR_EQUIP_LEATHER.value(), 0.2F, 1.4F);
                    }

                    if (this.useItemPrev != player.isUsingItem() && !player.isUsingItem() && player.getMainHandItem().getUseAnimation() == ItemUseAnimation.BLOCK) {
                        this.shieldEvent = true;
                    }

                    if (this.useItemPrev != player.isUsingItem() && !player.isUsingItem() && player.getOffhandItem().getUseAnimation() == ItemUseAnimation.BLOCK) {
                        this.shieldEventO = true;
                    }

                    if (this.shieldEvent && this.shieldCounter == 0.0F) {
                        this.shieldCounter = 1.0F;
                    }

                    if (this.shieldCounter < 0.0F) {
                        this.shieldCounter = 0.0F;
                        this.shieldEvent = false;
                    }

                    if (this.shieldEvent) {
                        this.shieldCounter = (float) ((double) this.shieldCounter - 0.1 * tt);
                    }

                    if (this.shieldEventO && this.shieldCounterO == 0.0F) {
                        this.shieldCounterO = 1.0F;
                    }

                    if (this.shieldCounterO < 0.0F) {
                        this.shieldCounterO = 0.0F;
                        this.shieldEventO = false;
                    }

                    if (this.shieldEventO) {
                        this.shieldCounterO = (float) ((double) this.shieldCounterO - 0.1 * tt);
                    }

                    if (this.useItemPrev != player.isUsingItem() && !player.isUsingItem() && (stack.getUseAnimation() == ItemUseAnimation.TRIDENT || stack.getUseAnimation() == ItemUseAnimation.BOW) && bl) {
                        this.itemSwitchedEvent = true;
                        this.itemSwitchCount = 0.0F;
                    }

                    if (this.useItemPrev != player.isUsingItem() && !player.isUsingItem() && (player.getOffhandItem().getUseAnimation() == ItemUseAnimation.TRIDENT || player.getOffhandItem().getUseAnimation() == ItemUseAnimation.BOW) && !bl) {
                        this.itemSwitchedEventO = true;
                        this.itemSwitchCountO = 0.0F;
                    }

                    if (this.itemSwitchedEvent) {
                        this.itemSwitchCount = (float) ((double) this.itemSwitchCount + FpAnimsConfig.SWITCH_SPEED * tt);
                    }

                    if (this.itemSwitchCount > 1.0F) {
                        this.itemSwitchCount = 1.0F;
                        this.itemSwitchedEvent = false;
                    }

                    if (this.itemSwitchCount < 1.0F || bl && (item instanceof AxeItem || item instanceof ShovelItem || item instanceof HoeItem || stack.is(Items.MACE))) {
                        equipProgress = 0.0F;
                    }

                    if (player.getOffhandItem().getItem() != this.prevItemO.getItem()) {
                        this.itemSwitchedEventO = true;
                        this.itemSwitchCountO = 0.0F;
                    }

                    if (this.itemSwitchedEventO) {
                        this.itemSwitchCountO = (float) ((double) this.itemSwitchCountO + FpAnimsConfig.SWITCH_SPEED * tt);
                    }

                    if (this.itemSwitchCountO > 1.0F) {
                        this.itemSwitchCountO = 1.0F;
                        this.itemSwitchedEventO = false;
                    }

                    if (!player.isScoping()) {
                        float yaw = player.getYRot();
                        double radians = Math.toRadians((double) yaw);
                        double forwardX = -Math.sin(radians);
                        double forwardZ = Math.cos(radians);
                        Vec3 horizontalVelocity = player.getDeltaMovement();
                        double dotProduct = horizontalVelocity.x * forwardX + horizontalVelocity.z * forwardZ;
                        double crossProduct = player.getDeltaMovement().x * forwardZ - horizontalVelocity.z * forwardX;
                        float al;
                        if (player.getXRot() != 0.0F) {
                            al = 90.0F / player.getXRot() / 10.0F;
                        } else {
                            al = 1.0F;
                        }

                        if (al > 1.0F) {
                            al = 1.0F;
                        }

                        if (al < 0.0F) {
                            al = 1.0F;
                        }

                        boolean inspectHeld = FpAnimsClient.CUSTOM_KEY != null && FpAnimsClient.CUSTOM_KEY.isDown();
                        if (!player.isUsingItem() && bl && inspectHeld && (!stack.isEmpty() && (this.getAttackDamage(stack) > 0.0F || stack.is(FpAnimsTags.TOOLS)) || stack.getItem() instanceof HoeItem)) {
                            this.inspectCounter = (float) ((double) this.inspectCounter + 0.1 * tt);
                            if (this.inspectCounter > 1.0F) {
                                this.inspectCounter = 1.0F;
                            }
                        } else if (bl && !inspectHeld) {
                            this.inspectCounter = (float) ((double) this.inspectCounter - 0.15 * tt);
                            if (this.inspectCounter < 0.0F) {
                                this.inspectCounter = 0.0F;
                            }
                        }

                        if (this.inspectCounter == 1.0F) {
                            this.inspectSpinCounter = (float) ((double) this.inspectSpinCounter + 0.022 * tt);
                        } else {
                            this.inspectSpinCounter = 0.0F;
                        }

                        if (this.inspectSpinCounter > 1.0F) {
                            this.inspectSpinCounter = 1.0F;
                        }

                        this.easeInOutBack(this.inspectSpinCounter);
                        if (bl) {
                            if (player.getMainArm() == HumanoidArm.LEFT && inspectHeld && this.inspectCounter > 0.0F) {
                                bl = !bl;
                            }

                            float switchItems = bl ? 1.0F : -1.0F;
                            poseStack.translate(0.7 * (double) this.easeInOutBack(this.inspectCounter) * (double) switchItems, (double) 0.0F, (double) 0.0F);
                            poseStack.mulPose(Axis.XP.rotationDegrees(-7.0F * Mth.sin(this.easeInOutBack(this.inspectSpinCounter) * 6.28F) * switchItems));
                            poseStack.mulPose(Axis.YP.rotationDegrees(50.0F * this.easeInOutBack(this.inspectCounter) * switchItems));
                        }

                        this.globalAnimationCounter = (float) ((double) this.globalAnimationCounter + 0.1 * tt);
                        if (player.getDeltaMovement().y() < -0.54) {
                            this.headFallCount = (float) ((double) this.headFallCount + 0.1 * tt);
                        } else {
                            this.headFallCount = 0.0F;
                        }

                        float switchItems = Mth.sin(Mth.clamp(this.itemSwitchCount, 0.0F, 0.5F) * 3.14F);
                        float switch_fast = Mth.sin(Mth.clamp(this.itemSwitchCount, 0.0F, 0.125F) * 12.56F);
                        float switchItemsO = Mth.sin(Mth.clamp(this.itemSwitchCountO, 0.0F, 0.5F) * 3.14F);
                        float switch_fastO = Mth.sin(Mth.clamp(this.itemSwitchCountO, 0.0F, 0.125F) * 12.56F);
                        if (bl) {
                            switchItems = this.easeInOutBack(switchItems);
                            poseStack.translate((double) (0.0F * switch_fast), (double) -0.75F * (double) switch_fast, 0.15 * (double) switch_fast);
                            poseStack.mulPose(Axis.ZP.rotationDegrees(63.0F * switch_fast));
                            poseStack.mulPose(Axis.XP.rotationDegrees(30.0F * switch_fast));
                            poseStack.mulPose(Axis.XP.rotationDegrees(-30.0F * switchItems));
                            poseStack.mulPose(Axis.ZP.rotationDegrees(-63.0F * switchItems));
                            poseStack.translate((double) (0.0F * switchItems), (double) 0.75F * (double) switchItems, -0.2 * (double) switchItems);
                        } else {
                            switchItemsO = this.easeInOutBack(switchItemsO);
                            poseStack.translate((double) (0.0F * switch_fastO), (double) -0.75F * (double) switch_fastO, 0.15 * (double) switch_fastO);
                            poseStack.mulPose(Axis.ZP.rotationDegrees(-63.0F * switch_fastO));
                            poseStack.mulPose(Axis.XP.rotationDegrees(30.0F * switch_fastO));
                            poseStack.mulPose(Axis.XP.rotationDegrees(-30.0F * switchItemsO));
                            poseStack.mulPose(Axis.ZP.rotationDegrees(63.0F * switchItemsO));
                            poseStack.translate((double) (0.0F * switchItemsO), (double) 0.75F * (double) switchItemsO, -0.2 * (double) switchItemsO);
                        }

                        float swing_global = Mth.sin(Mth.clamp(swingProgress, 0.4F, 1.0F) * 3.14F * 5.0F);
                        float swing_rot = (double) swingProgress < 0.6 ? Mth.sin(Mth.clamp(swingProgress, 0.0F, 0.12506F) * 12.56F) : Mth.sin(Mth.clamp(swingProgress, 0.62532F, 0.75038F) * 12.56F);
                        float swing = Mth.sin(swingProgress * 3.14F);
                        swing = this.easeInOutBack(swing);
                        if ((stack.getUseAnimation() == ItemUseAnimation.TRIDENT && player.isUsingItem() || stack.is(Items.EXPERIENCE_BOTTLE) || stack.is(Items.WIND_CHARGE) || stack.is(Items.EGG) || stack.is(Items.ENDER_EYE) || stack.is(Items.SNOWBALL) || stack.is(Items.ENDER_PEARL) || stack.getItem() instanceof SplashPotionItem || stack.getItem() instanceof LingeringPotionItem) && player.getOffhandItem().isEmpty() && !stack.is(Items.FIRE_CHARGE) && !player.isSwimming() && !player.isVisuallyCrawling() && !player.onClimbable()) {
                            if (player.getMainArm() == HumanoidArm.LEFT) {
                                bl = !bl;
                            }

                            float ll = bl ? 1.0F : -1.0F;
                            poseStack.pushPose();
                            poseStack.translate(-0.15 * (double) ll, 0.2, (double) -0.25F);
                            poseStack.mulPose(Axis.YP.rotationDegrees(-25.0F * ll));
                            poseStack.mulPose(Axis.XP.rotationDegrees(-25.0F));
                            poseStack.mulPose(Axis.YP.rotationDegrees(25.0F * ll * swing));
                            poseStack.mulPose(Axis.XP.rotationDegrees(30.0F * swing));
                            poseStack.translate(-0.15 * (double) ll, 0.1, 0.1);
                            poseStack.translate((double) 0.0F, -0.55 * (double) swing, 0.4 * (double) swing * (double) 3.14F);
                            this.renderPlayerArm(poseStack, submitNodeCollector, light, equipProgress, 0.0F, arm.getOpposite());
                            poseStack.popPose();
                        }

                        if (Minecraft.getInstance().options.keyAttack.isDown() && !this.isAttacking && swingProgress < 0.001F) {
                            this.left = !this.left;
                        }

                        if (!stack.isEmpty()) {
                            if (player.getMainArm() == HumanoidArm.LEFT) {
                                bl = !bl;
                            }

                            float ll = bl ? 1.0F : -1.0F;
                            if ((this.left || stack.getItem() instanceof AxeItem || stack.getUseAnimation() == ItemUseAnimation.TRIDENT || stack.getUseAnimation() == ItemUseAnimation.BLOCK) && !(stack.getItem() instanceof ShovelItem)) {
                                if (!stack.is(ItemTags.SWORDS) && !(stack.getItem() instanceof AxeItem)) {
                                    if (stack.getUseAnimation() == ItemUseAnimation.TRIDENT) {
                                        poseStack.translate((double) 0.0F, (double) 0.0F, 0.45 * (double) swing_rot);
                                        poseStack.translate((double) -0.25F * (double) kj * (double) swing, -0.35 * (double) swing_rot, -0.6 * (double) swing);
                                        poseStack.translate((double) 0.0F, 0.1 * (double) swing, (double) 0.0F);
                                        poseStack.mulPose(Axis.YP.rotationDegrees(15.0F * swing_rot * ll));
                                        poseStack.mulPose(Axis.ZP.rotationDegrees(30.0F * swing_rot * ll));
                                    } else if (stack.is(FpAnimsTags.TOOLS) && stack.getUseAnimation() != ItemUseAnimation.BLOCK && !(stack.getItem() instanceof ShovelItem)) {
                                        poseStack.translate(0.1 * (double) ll * (double) swing_rot, 0.1 * (double) swing_rot, (double) -0.5F * (double) swing);
                                        poseStack.mulPose(Axis.XN.rotationDegrees(-30.0F * swing_rot));
                                        poseStack.mulPose(Axis.ZP.rotationDegrees(-20.0F * swing_rot * ll));
                                        poseStack.mulPose(Axis.XN.rotationDegrees(40.0F * swing));
                                    } else if (stack.getUseAnimation() != ItemUseAnimation.BLOCK) {
                                        poseStack.translate(0.1 * (double) ll * (double) swing_rot, 0.1 * (double) swing_rot, -0.1 * (double) swing);
                                        poseStack.mulPose(Axis.XN.rotationDegrees(-30.0F * swing_rot));
                                        poseStack.mulPose(Axis.ZP.rotationDegrees(-10.0F * swing_rot * ll));
                                        poseStack.mulPose(Axis.XN.rotationDegrees(40.0F * swing));
                                        poseStack.mulPose(Axis.YP.rotationDegrees(10.0F * swing * ll));
                                    } else {
                                        poseStack.translate(0.1 * (double) ll * (double) swing_rot, 0.1 * (double) swing_rot, -0.2 * (double) swing);
                                        poseStack.mulPose(Axis.XN.rotationDegrees(-10.0F * swing_rot));
                                        poseStack.mulPose(Axis.ZP.rotationDegrees(-10.0F * swing_rot * ll));
                                        poseStack.mulPose(Axis.XN.rotationDegrees(20.0F * swing));
                                    }
                                } else {
                                    poseStack.translate(0.8 * (double) ll * (double) swing_rot, 0.3 * (double) swing_rot, (double) -0.5F * (double) swing);
                                    poseStack.mulPose(Axis.YP.rotationDegrees(15.0F * swing_rot * ll));
                                    poseStack.mulPose(Axis.XN.rotationDegrees(-20.0F * swing_rot));
                                    poseStack.mulPose(Axis.ZP.rotationDegrees(-70.0F * swing_rot * ll));
                                    if (stack.is(ItemTags.SWORDS)) {
                                        poseStack.mulPose(Axis.XN.rotationDegrees(40.0F * swing));
                                    } else {
                                        poseStack.mulPose(Axis.XN.rotationDegrees(30.0F * swing));
                                    }
                                }
                            } else if (!(stack.getItem() instanceof ShovelItem)) {
                                if (stack.is(ItemTags.SWORDS)) {
                                    poseStack.translate(-0.55 * (double) ll * (double) swing_rot, -0.8 * (double) swing_rot, -0.77 * (double) swing);
                                    poseStack.mulPose(Axis.YP.rotationDegrees(5.0F * swing_rot * ll));
                                    poseStack.mulPose(Axis.XN.rotationDegrees(-30.0F * swing_rot));
                                    poseStack.mulPose(Axis.ZP.rotationDegrees(70.0F * swing_rot * ll));
                                    poseStack.mulPose(Axis.XN.rotationDegrees(50.0F * swing));
                                } else if (stack.is(FpAnimsTags.TOOLS) && !(stack.getItem() instanceof ShovelItem)) {
                                    poseStack.translate(0.1 * (double) ll * (double) swing_rot, 0.1 * (double) swing_rot, (double) -0.5F * (double) swing);
                                    poseStack.mulPose(Axis.XN.rotationDegrees(-30.0F * swing_rot));
                                    poseStack.mulPose(Axis.ZP.rotationDegrees(-20.0F * swing_rot * ll));
                                    poseStack.mulPose(Axis.XN.rotationDegrees(40.0F * swing));
                                } else {
                                    poseStack.translate(0.1 * (double) ll * (double) swing_rot, 0.1 * (double) swing_rot, -0.1 * (double) swing);
                                    poseStack.mulPose(Axis.XN.rotationDegrees(-30.0F * swing_rot));
                                    poseStack.mulPose(Axis.ZP.rotationDegrees(-10.0F * swing_rot * ll));
                                    poseStack.mulPose(Axis.XN.rotationDegrees(40.0F * swing));
                                    poseStack.mulPose(Axis.YP.rotationDegrees(10.0F * swing * ll));
                                }
                            } else if (stack.getItem() instanceof ShovelItem) {
                                poseStack.translate((double) 0.0F, 0.15 * (double) swing_rot, (double) -0.25F * (double) swing_rot);
                                poseStack.translate((double) 0.0F, (double) 0.0F, -0.2 * (double) swing);
                                poseStack.mulPose(Axis.YP.rotationDegrees(15.0F * swing_rot));
                                poseStack.mulPose(Axis.XP.rotationDegrees(-35.0F * swing_rot));
                                poseStack.mulPose(Axis.XP.rotationDegrees(30.0F * swing));
                            }
                        } else if (Block.byItem(stack.getItem()) != Blocks.AIR && (!stack.is(FpAnimsTags.TOOLS) || stack.is(ItemTags.TRIMMABLE_ARMOR) || stack.is(ItemTags.BOOKSHELF_BOOKS) || stack.getUseAnimation() == ItemUseAnimation.EAT || !stack.isEnchantable()) && stack.getUseAnimation() != ItemUseAnimation.BOW && stack.getUseAnimation() != ItemUseAnimation.SPYGLASS && this.getAttackDamage(stack) == 0.0F && stack.getUseAnimation() != ItemUseAnimation.BLOCK && !stack.is(Items.WARPED_FUNGUS_ON_A_STICK) && !stack.is(Items.CARROT_ON_A_STICK) && !(stack.getItem() instanceof FishingRodItem) && !stack.is(Items.SHEARS)) {
                            swingProgress = (float) ((double) swingProgress * 1.2);
                            if (swingProgress > 1.0F) {
                                swingProgress = 1.0F;
                            }
                        } else if (!(stack.getItem() instanceof ShovelItem)) {
                            swingProgress = (float) ((double) swingProgress * (double) 1.5F);
                            if (swingProgress > 1.0F) {
                                swingProgress = 1.0F;
                            }
                        }

                        if (player.getDeltaMovement().length() >= 0.08) {
                            this.crawlCount = (float) ((double) this.crawlCount + 0.1 * player.getDeltaMovement().length() * (double) 2.0F * tt);
                            this.directionalCrawlCount = (float) ((double) this.directionalCrawlCount + 0.1 * dotProduct * (double) 4.0F * tt);
                            this.directionalCrawlCount = (float) ((double) this.directionalCrawlCount + (dotProduct > (double) 0.0F ? 0.1 * Math.abs(crossProduct) * (double) 4.0F * tt : 0.1 * Math.abs(crossProduct) * (double) -1.0F * (double) 4.0F * tt));
                        }

                        if (player.getDeltaMovement().y() > (double) 0.0F) {
                            this.climbCount = (float) ((double) this.climbCount + 0.1 * tt);
                        }

                        if (player.getDeltaMovement().y() < (double) 0.0F) {
                            this.climbCount = (float) ((double) this.climbCount - 0.1 * tt);
                        }

                        if ((player.isVisuallyCrawling() && FpAnimsConfig.ENABLE_CLIMB_AND_CRAWL || player.onClimbable() && !player.isCrouching() && !player.onGround() && Math.abs(player.getDeltaMovement().y()) > (double) 0.0F && FpAnimsConfig.ENABLE_CLIMB_AND_CRAWL) && !player.isUsingItem() && swingProgress == 0.0F) {
                            this.clCount = (float) ((double) this.clCount + 0.1 * tt);
                            if (this.clCount > 1.0F) {
                                this.clCount = 1.0F;
                            }

                            if (!stack.is(Items.LANTERN) && !stack.is(Items.SOUL_LANTERN)) {
                                poseStack.mulPose(Axis.XP.rotationDegrees(-20.0F * this.clCount));
                            }
                        } else {
                            this.clCount = (float) ((double) this.clCount * Math.pow((double) 0.88F, tt));
                        }

                        if (swingProgress == 0.0F) {
                            poseStack.translate(bl ? player.getXRot() / 650.0F * this.clCount * -1.0F : player.getXRot() / 650.0F * this.clCount, 0.0F, 0.0F);
                            poseStack.mulPose(Axis.XP.rotationDegrees(player.getXRot() * this.clCount));
                        }

                        if (!stack.is(Items.LANTERN) && !stack.is(Items.SOUL_LANTERN)) {
                            poseStack.translate(0.0F, 0.0F, player.getXRot() / 120.0F * this.clCount);
                        } else if (swingProgress == 0.0F) {
                            poseStack.translate(0.0F, 0.0F, player.getXRot() / 80.0F * this.clCount);
                        }

                        if (player.onClimbable() && !player.isCrouching() && FpAnimsConfig.ENABLE_CLIMB_AND_CRAWL && !player.onGround() && !stack.is(Items.LANTERN) && !stack.is(Items.SOUL_LANTERN) && !player.isUsingItem()) {
                            poseStack.translate((double) 0.0F, 0.1, -0.2);
                        }

                        if (this.isInAnyFluid(player) && !player.isSwimming() && !player.isUnderWater()) {
                            this.inWaterCounter = (float) ((double) this.inWaterCounter + 0.1 * tt);
                            if (this.inWaterCounter >= 1.0F) {
                                this.inWaterCounter = 1.0F;
                            }
                        } else {
                            this.inWaterCounter = (float) ((double) this.inWaterCounter * Math.pow((double) 0.88F, tt));
                        }

                        boolean holdingMap = player.getMainHandItem().getItem() instanceof MapItem || player.getOffhandItem().getItem() instanceof MapItem;
                        double mapRenderProgress = holdingMap ? (double) 1.0F : (double) 0.0F;
                        if (player.isInPowderSnow && mapRenderProgress > 0.1) {
                            this.freezeCounter = (float) ((double) this.freezeCounter + 0.1 * tt);
                        } else {
                            this.freezeCounter = (float) ((double) this.freezeCounter * Math.pow((double) 0.88F, tt));
                        }

                        poseStack.translate((double) 0.0F, 0.02 * (double) this.inWaterCounter, (double) 0.0F);
                        poseStack.mulPose(Axis.ZP.rotationDegrees(8.0F * kj * this.inWaterCounter));
                        poseStack.mulPose(Axis.XP.rotationDegrees(0.3F * Mth.sin(this.freezeCounter * 5.0F)));
                        if (player.getDeltaMovement().y() < -0.85 && stack.is(Items.MACE) && player.getMainHandItem() == stack) {
                            this.fallCounter = (float) ((double) this.fallCounter + 0.1 * tt);
                            if (this.fallCounter >= 1.0F) {
                                this.fallCounter = 1.0F;
                            }
                        } else {
                            this.fallCounter = (float) ((double) this.fallCounter * Math.pow((double) 0.88F, tt));
                        }

                        if (bl) {
                            poseStack.mulPose(Axis.XP.rotationDegrees(45.0F * this.fallCounter));
                            poseStack.translate((double) 0.0F, -0.2 * (double) this.fallCounter, (double) 0.0F);
                        }

                        this.vertAngleY = (float) ((double) this.vertAngleY + player.getDeltaMovement().y() * (double) 0.015F * tt);
                        this.vertAngleY = (float) ((double) this.vertAngleY - (double) (0.1F * this.vertAngleY) * tt);
                        this.vertAngleY = (float) ((double) this.vertAngleY * Math.pow((double) 0.88F, tt));
                        this.vertVelocityYSlime = (float) ((double) this.vertVelocityYSlime + player.getDeltaMovement().y() * (double) 0.015F * tt);
                        this.vertVelocityYSlime = (float) ((double) this.vertVelocityYSlime - (double) (0.1F * this.vertAngleYSlime) * tt);
                        this.vertVelocityYSlime = (float) ((double) this.vertVelocityYSlime * Math.pow((double) 0.88F, tt));
                        this.vertAngleYSlime = (float) ((double) this.vertAngleYSlime + (double) this.vertVelocityYSlime * tt);
                        this.vertAngleYSlime = (float) Mth.clamp((double) this.vertAngleYSlime, -0.3, (double) 255.0F);
                        poseStack.translate(0.0F, this.vertAngleY * -1.0F, 0.0F);
                        poseStack.translate((double) 0.0F, Math.sin((double) player.tickCount * 0.1) * 0.007 * (double) kj, (double) 0.0F);
                        poseStack.mulPose(Axis.YP.rotationDegrees(0.15F * Mth.sin((float) player.tickCount * 0.15F) * kj));
                        if (!stack.isEmpty() || player.isVisuallyCrawling() || player.onClimbable() && !player.isCrouching() && !player.onGround() || player.isSwimming()) {
                            if (player.getMainArm() == HumanoidArm.LEFT) {
                                bl = !bl;
                            }

                            if (stack.getUseAnimation() == ItemUseAnimation.BLOCK) {
                                poseStack.translate(0.0F, 0.0F, 0.0F);
                            } else {
                                poseStack.translate((double) 0.0F, -0.1, 0.1);
                            }
                        }

                        if (stack.is(Items.LANTERN) || stack.is(Items.SOUL_LANTERN) || stack.is(ItemTags.HANGING_SIGNS)) {
                            poseStack.translate((double) 0.0F, 0.1, (double) 0.0F);
                            if (player.isSwimming()) {
                                poseStack.translate((double) 0.0F, -0.1, 0.1);
                            }
                        }

                        if (player.isSwimming() && swingProgress == 0.0F && FpAnimsConfig.ENABLE_SWIMMING_ANIM) {
                            double s = (double) this.crawlCount;
                            double swingAmplitude = (double) 1.5F;
                            double frequency = (double) 2.0F;
                            s *= frequency;
                            double handRotation = Math.sin(s) * swingAmplitude;
                            double smoothRotation = handRotation * 0.8;
                            poseStack.mulPose(Axis.YP.rotationDegrees((float) (bl ? smoothRotation : -smoothRotation)));
                            poseStack.translate((double) 0.0F, (double) 0.0F, smoothRotation * (double) 0.2F);
                            double currentSpeed = (double) (this.crawlCount * 2.0F);
                            double a = Math.cos(currentSpeed);
                            double b = a;
                            if (a <= (double) 0.0F) {
                                b = a * (double) 0.5F;
                            }

                            poseStack.mulPose(Axis.YN.rotationDegrees((float) (bl ? b * (double) 30.0F : b * (double) 30.0F * (double) -1.0F)));
                            poseStack.translate((double) 0.0F, (double) 0.0F, a * (double) 0.2F);
                            if (stack.isEmpty() && !bl && !player.isInvisible()) {
                                float j1 = bl ? 1.0F : -1.0F;
                                poseStack.translate((double) j1, (double) 0.0F - (double) equipProgress * 0.3, 0.3);
                                poseStack.mulPose(Axis.YP.rotationDegrees(45.0F * j1));
                                poseStack.mulPose(Axis.ZP.rotationDegrees(-40.0F * j1));
                                poseStack.mulPose(Axis.XP.rotationDegrees(30.0F));
                                this.altSwing(poseStack, arm, swingProgress);
                                float h5 = Mth.sin(equipProgress * 3.14F);
                                poseStack.scale(0.9F, 0.9F, 0.9F);
                                this.renderPlayerArm(poseStack, submitNodeCollector, light, 0.0F, 0.0F, arm);
                                ci.cancel();
                            }
                        }

                        if ((player.onClimbable() && !player.isCrouching() && !player.onGround() || player.isVisuallyCrawling() && swingProgress == 0.0F) && !player.isUsingItem()) {
                            double s = (double) this.climbCount;
                            float g = (float) player.getDeltaMovement().y();
                            float h = Mth.cos((float) s * 2.0F);
                            float j = bl ? 1.0F : -1.0F;
                            if (player.onClimbable()) {
                                if (!stack.is(Items.LANTERN) && !stack.is(Items.SOUL_LANTERN)) {
                                    poseStack.mulPose(Axis.XP.rotationDegrees(20.0F * h * j));
                                } else {
                                    poseStack.mulPose(Axis.XP.rotationDegrees(1.0F * h * j));
                                }
                            }

                            if (player.isVisuallyCrawling() && !player.isUsingItem() && swingProgress == 0.0F) {
                                float l = Mth.sin(this.directionalCrawlCount * 4.0F * 1.0F);
                                float dt = Mth.cos(this.directionalCrawlCount * 4.0F * 1.0F);
                                if (stack.is(Items.LANTERN) || stack.is(Items.SOUL_LANTERN)) {
                                    l *= 0.14F;
                                    dt *= 0.14F;
                                }

                                poseStack.translate(0.2 * (double) l, 0.3 * (double) l * (double) j, -0.2 * (double) l * (double) j * (double) al);
                                poseStack.mulPose(Axis.YP.rotationDegrees(25.0F * l));
                                poseStack.mulPose(Axis.XP.rotationDegrees(Mth.clamp(20.0F * dt * j, 0.0F, 20.0F)));
                            }

                            if (stack.isEmpty() && !bl && !player.isInvisible() && (!player.onGround() && !player.isCrouching() && player.onClimbable() || player.isVisuallyCrawling())) {
                                float l = bl ? 1.0F : -1.0F;
                                poseStack.translate((double) l, (double) 0.0F - (double) equipProgress * 0.3, 0.3);
                                poseStack.mulPose(Axis.YP.rotationDegrees(45.0F * l));
                                poseStack.mulPose(Axis.ZP.rotationDegrees(-40.0F * l));
                                poseStack.mulPose(Axis.XP.rotationDegrees(30.0F));
                                this.altSwing(poseStack, arm, swingProgress);
                                poseStack.scale(0.9F, 0.9F, 0.9F);
                                this.renderPlayerArm(poseStack, submitNodeCollector, light, 0.0F, 0.0F, arm);
                                ci.cancel();
                            }
                        }

                        if (stack.isEmpty()) {
                            if (bl && !player.isInvisible()) {
                                if ((player.onGround() || !player.onClimbable()) && !player.isSwimming() && !player.isVisuallyCrawling()) {
                                    if (player.getMainArm() == HumanoidArm.LEFT) {
                                        bl = !bl;
                                    }

                                    float ll = bl ? 1.0F : -1.0F;
                                    poseStack.translate((double) 0.0F, 0.2 * (double) swing_rot, 0.15 * (double) swing_rot);
                                    poseStack.translate(0.1 * (double) ll * (double) swing, 0.15 * (double) swing, -0.45 * (double) swing);
                                    poseStack.mulPose(Axis.YP.rotationDegrees(35.0F * swing * ll));
                                    poseStack.mulPose(Axis.XP.rotationDegrees(-30.0F * swing));
                                    poseStack.mulPose(Axis.YP.rotationDegrees(-10.0F * swing_rot * ll));
                                    poseStack.mulPose(Axis.XP.rotationDegrees(10.0F * swing_rot));
                                    this.renderPlayerArm(poseStack, submitNodeCollector, light, 0.0F, 0.0F, arm);
                                    ci.cancel();
                                } else {
                                    float ll = bl ? 1.0F : -1.0F;
                                    poseStack.translate((double) ll, (double) 0.0F - (double) equipProgress * 0.3, 0.3);
                                    poseStack.mulPose(Axis.YP.rotationDegrees(45.0F * ll));
                                    poseStack.mulPose(Axis.ZP.rotationDegrees(-40.0F * ll));
                                    poseStack.mulPose(Axis.XP.rotationDegrees(30.0F));
                                    this.altSwing(poseStack, arm, swingProgress);
                                    float f = Mth.sin(equipProgress * 3.14F);
                                    poseStack.scale(0.9F, 0.9F, 0.9F);
                                    this.renderPlayerArm(poseStack, submitNodeCollector, light, 0.0F, 0.0F, arm);
                                    ci.cancel();
                                }
                            }
                        } else if (stack.has(DataComponents.MAP_ID)) {
                            if (bl && this.offHandItem.isEmpty()) {
                                poseStack.translate((double) 0.0F, 0.1, (double) 0.0F);
                                this.renderTwoHandedMap(poseStack, submitNodeCollector, light, pitch, equipProgress, swingProgress);
                            } else {
                                poseStack.translate(bl ? -0.1 : 0.1, 0.1, (double) 0.0F);
                                this.renderOneHandedMap(poseStack, submitNodeCollector, light, equipProgress, arm, swingProgress, stack);
                            }
                        } else if (stack.getUseAnimation() == ItemUseAnimation.CROSSBOW) {
                            poseStack.pushPose();
                            if (player.getMainArm() == HumanoidArm.LEFT) {
                                bl = !bl;
                            }

                            boolean bl2 = CrossbowItem.isCharged(stack);
                            boolean bl3 = arm == HumanoidArm.RIGHT;
                            int i = bl3 ? 1 : -1;
                            if (player.isUsingItem() && player.getUseItemRemainingTicks() > 0 && player.getUsedItemHand() == hand) {
                                float f = (float) stack.getUseDuration(player) - ((float) player.getUseItemRemainingTicks() - partialTicks + 1.0F);
                                float g = f / (float) CrossbowItem.getChargeDuration(stack, player);
                                if (g > 1.0F) {
                                    g = 1.0F;
                                }

                                if (g > 0.1F) {
                                    float h = Mth.sin((f - 0.1F) * 1.3F);
                                    float j = g - 0.1F;
                                    float jn = h * j;
                                    poseStack.translate(jn * 0.0F, jn * 0.004F, jn * 0.0F);
                                }

                                this.applyItemArmTransform(poseStack, arm, equipProgress);
                                float jn = this.easeInOutBack(Mth.clamp(g * 3.0F, 0.0F, 1.0F));
                                poseStack.translate((float) i * -0.4785682F * jn, -0.24387F * jn, 0.05731531F * jn);
                                poseStack.mulPose(Axis.XP.rotationDegrees(-11.935F * jn));
                                poseStack.mulPose(Axis.YP.rotationDegrees((float) i * 65.3F * jn));
                                poseStack.mulPose(Axis.ZP.rotationDegrees((float) i * 9.785F * jn));
                                poseStack.translate(g * 0.0F, g * 0.0F, g * 0.04F);
                                poseStack.scale(1.0F, 1.0F, 1.0F);
                                poseStack.mulPose(Axis.YN.rotationDegrees((float) i * 45.0F));
                            } else {
                                this.swingArm(swingProgress, equipProgress, poseStack, i, arm);
                                if (bl2 && swingProgress < 0.001F && bl) {
                                    poseStack.translate((float) i * -0.341864F, 0.0F, 0.0F);
                                    poseStack.mulPose(Axis.YP.rotationDegrees((float) i * 10.0F));
                                }
                            }

                            float jn = bl ? 1.0F : -1.0F;
                            poseStack.translate(0.0F, 0.0F, -1.0F);
                            poseStack.translate(-0.45 * (double) i, 0.45, 1.7);
                            poseStack.translate((double) (1.0F * jn), (double) 0.0F - (double) equipProgress * 0.3, 0.3);
                            poseStack.mulPose(Axis.YP.rotationDegrees(45.0F * jn));
                            poseStack.mulPose(Axis.ZP.rotationDegrees(-40.0F * jn));
                            poseStack.mulPose(Axis.XP.rotationDegrees(30.0F));
                            this.altSwing(poseStack, arm, swingProgress);
                            float breathe = Mth.sin(equipProgress * 3.14F);
                            poseStack.scale(0.9F, 0.9F, 0.9F);
                            this.renderPlayerArm(poseStack, submitNodeCollector, light, 0.0F, 0.0F, arm);
                            poseStack.translate((double) -0.25F * (double) i, (double) 1.25F, 0.05);
                            poseStack.mulPose(Axis.YP.rotationDegrees((float) (-90 * i)));
                            poseStack.mulPose(Axis.XP.rotationDegrees(77.0F));
                            poseStack.mulPose(Axis.ZP.rotationDegrees((float) (85 * i)));
                            poseStack.scale(1.2F, 1.2F, 1.2F);
                            poseStack.mulPose(Axis.XP.rotationDegrees(-10.0F));
                            poseStack.translate((double) 0.0F, -0.15, 0.15);
                            this.renderItem(player, stack, bl3 ? ItemDisplayContext.FIRST_PERSON_RIGHT_HAND : ItemDisplayContext.FIRST_PERSON_LEFT_HAND, poseStack, submitNodeCollector, light);
                            poseStack.popPose();
                            ci.cancel();
                            if (player.isUsingItem() && player.getUseItemRemainingTicks() > 0 && player.getUsedItemHand() == hand) {
                                float f = (float) stack.getUseDuration(player) - ((float) player.getUseItemRemainingTicks() - partialTicks + 1.0F);
                                float g = f / (float) CrossbowItem.getChargeDuration(stack, player);
                                if (g > 1.0F) {
                                    g = 1.0F;
                                }

                                if (g > 0.1F) {
                                    float h = Mth.sin((f - 0.1F) * 1.3F);
                                    float j = g - 0.1F;
                                    float yawDelta = h * j;
                                    poseStack.translate(yawDelta * 0.0F, yawDelta * 0.004F, yawDelta * 0.0F);
                                }

                                poseStack.mulPose(Axis.YN.rotationDegrees((double) g <= 0.2 ? 75.0F * g * 5.0F * (float) i : (float) (75 * i)));
                                poseStack.mulPose(Axis.XN.rotationDegrees(10.0F * g * 1.5F));
                                poseStack.translate(-0.37 * (double) i, 0.05, 0.6);
                                poseStack.translate(0.15 * (double) g * (double) i, (double) 0.0F, (double) 0.0F);
                                this.renderPlayerArm(poseStack, submitNodeCollector, light, equipProgress, swingProgress, arm.getOpposite());
                                ci.cancel();
                            }
                        } else {
                            hmiBlockRest: {
                                boolean bl2 = arm == HumanoidArm.RIGHT;
                                int l = bl2 ? 1 : -1;
                                if (player.isUsingItem() && player.getUseItemRemainingTicks() > 0 && player.getUsedItemHand() == hand) {
                                    switch (stack.getUseAnimation()) {
                                        case NONE:
                                            this.applyItemArmTransform(poseStack, arm, equipProgress);
                                            break;
                                        case EAT:
                                        case DRINK:
                                            float jn = (float) stack.getUseDuration(player) - ((float) player.getUseItemRemainingTicks() - partialTicks + 1.0F);
                                            float breathe = jn / 5.0F;
                                            if (breathe > 1.0F) {
                                                breathe = 1.0F;
                                            }

                                            float yawDelta = Mth.sin(jn / 2.0F * 3.14F);
                                            yawDelta /= 10.0F;
                                            poseStack.translate((double) l, 0.1, 0.3);
                                            poseStack.translate((double) ((float) (0 * l) * breathe), -0.7 * (double) breathe, -0.2 * (double) breathe);
                                            poseStack.translate((double) 0.0F, -0.2 * (double) yawDelta, -0.2 * (double) yawDelta);
                                            poseStack.translate((double) 0.0F, 0.1 * (double) this.easeInOutBack(Mth.sin(breathe * 3.14F)), (double) 0.0F);
                                            poseStack.mulPose(Axis.YP.rotationDegrees((float) (45 * l)));
                                            poseStack.mulPose(Axis.ZP.rotationDegrees((float) (-40 * l)));
                                            poseStack.mulPose(Axis.XP.rotationDegrees(30.0F));
                                            this.altSwing(poseStack, arm, swingProgress);
                                            float pitchDelta = Mth.sin(equipProgress * 3.14F);
                                            poseStack.scale(0.9F, 0.9F, 0.9F);
                                            poseStack.mulPose(Axis.YP.rotationDegrees(45.0F * breathe * (float) l));
                                            this.renderPlayerArm(poseStack, submitNodeCollector, light, 0.0F, swingProgress, arm);
                                            break;
                                        case BLOCK:
                                            float angularSpeed = (float) stack.getUseDuration(player) - ((float) player.getUseItemRemainingTicks() - partialTicks + 1.0F);
                                            double s = (double) (angularSpeed / 4.0F);
                                            float s2 = angularSpeed / 6.0F;
                                            if (s > (double) 1.0F) {
                                                s = (double) 1.0F;
                                            }

                                            if (s2 > 1.0F) {
                                                s2 = 1.0F;
                                            }

                                            poseStack.translate((double) 0.0F, -0.2, (double) 0.0F);
                                            poseStack.translate((double) l, (double) 0.0F, 0.3);
                                            poseStack.translate(0.37 * s * (double) l, (double) 0.0F, -1.1 * s);
                                            poseStack.translate(-0.2 * (double) l * (double) s2, (double) 0.0F, (double) 0.0F);
                                            poseStack.mulPose(Axis.XP.rotationDegrees((float) ((double) 10.0F * Math.sin((double) s2 * 3.14))));
                                            poseStack.mulPose(Axis.YP.rotationDegrees((float) ((double) 70.0F * s * (double) ((float) l))));
                                            poseStack.mulPose(Axis.YP.rotationDegrees((float) (45 * l)));
                                            poseStack.mulPose(Axis.ZP.rotationDegrees((float) (-40 * l)));
                                            poseStack.mulPose(Axis.XP.rotationDegrees(30.0F));
                                            poseStack.mulPose(Axis.YP.rotationDegrees((float) ((double) (5 * l) * s)));
                                            poseStack.mulPose(Axis.XP.rotationDegrees((float) ((double) -10.0F * s)));
                                            poseStack.translate((double) 0.0F, (double) 0.0F, -0.2 * s);
                                            this.altSwing(poseStack, arm, swingProgress);
                                            poseStack.scale(0.9F, 0.9F, 0.9F);
                                            this.renderPlayerArm(poseStack, submitNodeCollector, light, 0.0F, swingProgress, arm);
                                            poseStack.translate(0.35 * (double) l, -0.13, -0.12);
                                            poseStack.mulPose(Axis.ZP.rotationDegrees(10.0F * (float) l));
                                            poseStack.mulPose(Axis.YP.rotationDegrees(10.0F * (float) l));
                                            poseStack.mulPose(Axis.XP.rotationDegrees(0.0F));
                                            poseStack.translate(-0.2 * (double) l, -0.04, 0.15);
                                            poseStack.scale(1.0F, 1.0F, 1.0F);
                                            break;
                                        case BOW:
                                            if (player.getMainArm() == HumanoidArm.LEFT) {
                                                bl = !bl;
                                            }

                                            l = bl ? 1 : -1;
                                            float m1 = (float) stack.getUseDuration(player) - ((float) player.getUseItemRemainingTicks() - partialTicks + 1.0F);
                                            float f1 = m1 / 15.0F;
                                            float f = (f1 * f1 + f1 * 2.0F) / 3.0F;
                                            if (f1 > 1.0F) {
                                                f1 = 1.0F;
                                            }

                                            poseStack.mulPose(Axis.XP.rotationDegrees(-30.0F));
                                            poseStack.mulPose(Axis.YP.rotationDegrees(-15.0F));
                                            poseStack.mulPose(Axis.YP.rotationDegrees(15.0F * this.easeInOutBack(Mth.clamp(f1 * 2.0F, 0.0F, 1.0F))));
                                            poseStack.mulPose(Axis.XP.rotationDegrees(30.0F * this.easeInOutBack(Mth.clamp(f1 * 2.0F, 0.0F, 1.0F))));
                                            poseStack.pushPose();
                                            if (f1 > 0.1F) {
                                                float g1 = Mth.sin((m1 - 0.1F) * 1.3F);
                                                float j1 = g1 * f1;
                                                poseStack.translate(j1 * 0.0F, j1 * 0.004F, j1 * 0.0F);
                                            }

                                            poseStack.translate(bl ? -0.1 : 0.1, (double) 0.0F, (double) f1 * 0.15);
                                            this.renderPlayerArm(poseStack, submitNodeCollector, light, equipProgress, swingProgress, arm);
                                            poseStack.popPose();
                                            poseStack.translate(-1.03 * (double) l, (double) 0.0F, -0.97);
                                            poseStack.mulPose(Axis.YP.rotationDegrees((float) (-65 * l)));
                                            poseStack.mulPose(Axis.YP.rotationDegrees((float) (-45 * l)));
                                            poseStack.mulPose(Axis.ZP.rotationDegrees((float) (40 * l)));
                                            poseStack.mulPose(Axis.XP.rotationDegrees(30.0F));
                                            this.altSwing(poseStack, arm, swingProgress);
                                            poseStack.scale(0.9F, 0.9F, 0.9F);
                                            this.renderPlayerArm(poseStack, submitNodeCollector, light, equipProgress, swingProgress, arm.getOpposite());
                                            poseStack.mulPose(Axis.YN.rotation(2.5F * (float) l));
                                            poseStack.translate(bl ? -0.65 : 0.65, -0.35, 0.27);
                                            if (f1 > 1.0F) {
                                                f1 = 1.0F;
                                            }

                                            poseStack.popPose();
                                            poseStack.mulPose(Axis.XP.rotationDegrees(-30.0F));
                                            poseStack.mulPose(Axis.YP.rotationDegrees(-15.0F));
                                            poseStack.mulPose(Axis.YP.rotationDegrees(15.0F * this.easeInOutBack(Mth.clamp(f1 * 2.0F, 0.0F, 1.0F))));
                                            poseStack.mulPose(Axis.XP.rotationDegrees(30.0F * this.easeInOutBack(Mth.clamp(f1 * 2.0F, 0.0F, 1.0F))));
                                            if (FpAnimsConfig.MB3D_COMPAT && BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace().equals("minecraft") && !stack.is(FpAnimsTags.BUCKETS)) {
                                                poseStack.mulPose(Axis.YP.rotationDegrees((float) (10 * l)));
                                            }

                                            poseStack.mulPose(Axis.YP.rotationDegrees((float) (10 * l)));
                                            poseStack.mulPose(Axis.XN.rotationDegrees(75.0F));
                                            poseStack.mulPose(Axis.ZN.rotationDegrees((float) (-5 * l)));
                                            poseStack.translate(0.8 * (double) l, (double) (0.0F - equipProgress * 0.3F), -0.1);
                                            if (f > 0.1F) {
                                                float g1 = Mth.sin((m1 - 0.1F) * 1.3F);
                                                float h1 = f1 - 0.1F;
                                                float j1 = g1 * h1;
                                                poseStack.translate(j1 * 0.0F, j1 * 0.004F, j1 * 0.0F);
                                            }

                                            poseStack.pushPose();
                                            break;
                                        case TRIDENT:
                                            float dt = (float) stack.getUseDuration(player) - ((float) player.getUseItemRemainingTicks() - partialTicks + 1.0F);
                                            float f2 = dt / 10.0F;
                                            if (f2 > 1.0F) {
                                                f2 = 1.0F;
                                            }

                                            if (f2 > 0.1F) {
                                                float g = Mth.sin((dt - 0.1F) * 1.3F);
                                                float h = f2 - 0.1F;
                                                float j = g * h;
                                                poseStack.translate(j * 0.0F, j * 0.004F, j * 0.0F);
                                            }

                                            float g1 = this.easeInOutBack(Mth.clamp(f2 * 1.35F, 0.0F, 1.0F));
                                            poseStack.mulPose(Axis.XP.rotationDegrees(45.0F * g1));
                                            poseStack.mulPose(Axis.YP.rotationDegrees((float) (25 * l) * g1));
                                            poseStack.translate(0.2 * (double) g1 * (double) l, (double) 0.0F, 0.8 * (double) g1);
                                            this.renderPlayerArm(poseStack, submitNodeCollector, light, equipProgress, swingProgress, arm);
                                            poseStack.mulPose(Axis.XP.rotationDegrees(135.0F));
                                            poseStack.mulPose(Axis.ZP.rotationDegrees((float) (-65 * l)));
                                            poseStack.translate((double) (0.65F * (float) l), (double) -1.0F, -0.6);
                                            break;
                                        case BRUSH:
                                            float h1 = (float) (player.getUseItemRemainingTicks() % 10);
                                            float j1 = h1 - partialTicks + 1.0F;
                                            float h5 = 1.0F - j1 / 10.0F;
                                            float z = (float) stack.getUseDuration(player) - ((float) player.getUseItemRemainingTicks() - partialTicks + 1.0F);
                                            float x = z / 6.0F;
                                            if (x > 1.0F) {
                                                x = 1.0F;
                                            }

                                            x = this.easeInOutBack(x);
                                            float sway = Mth.sin(z / 2.5F);
                                            poseStack.translate(-0.1 * (double) sway * (double) l, (double) 0.0F, (double) 0.0F);
                                            poseStack.translate(0.06 * (double) x * (double) l, 0.1 * (double) x, (double) 0.0F);
                                            poseStack.mulPose(Axis.YP.rotationDegrees(20.0F * x * (float) l));
                                            poseStack.translate(-0.1 * (double) sway * (double) l, -0.2 * (double) sway, (double) 0.0F);
                                            poseStack.mulPose(Axis.ZP.rotationDegrees(20.0F * sway * (float) l));
                                            poseStack.translate((double) l, (double) 0.0F - (double) equipProgress * 0.3, 0.3);
                                            poseStack.mulPose(Axis.YP.rotationDegrees((float) (45 * l)));
                                            poseStack.mulPose(Axis.ZP.rotationDegrees((float) (-40 * l)));
                                            poseStack.mulPose(Axis.XP.rotationDegrees(30.0F));
                                            this.altSwing(poseStack, arm, swingProgress);
                                            poseStack.scale(0.9F, 0.9F, 0.9F);
                                            this.renderPlayerArm(poseStack, submitNodeCollector, light, equipProgress, swingProgress, arm);
                                    }
                                } else if (player.isAutoSpinAttack() && stack.getUseAnimation() == ItemUseAnimation.TRIDENT) {
                                    this.riptideCounter = (float) ((double) this.riptideCounter + 0.15 * tt);
                                    float dt = (float) stack.getUseDuration(player) - ((float) player.getUseItemRemainingTicks() - partialTicks + 1.0F);
                                    float f = dt / 10.0F;
                                    if (f > 1.0F) {
                                        f = 1.0F;
                                    }

                                    if (f > 0.1F) {
                                        float g = Mth.sin((dt - 0.1F) * 1.3F);
                                        float h = f - 0.1F;
                                        float j = g * h;
                                        poseStack.translate(j * 0.0F, j * 0.004F, j * 0.0F);
                                    }

                                    poseStack.mulPose(Axis.XP.rotationDegrees(45.0F - this.riptideCounter * 2.0F));
                                    poseStack.mulPose(Axis.YP.rotationDegrees((float) (25 * l)));
                                    poseStack.translate(0.2 * (double) l, (double) 0.0F, (double) 0.75F);
                                    poseStack.translate((double) 0.0F, (double) 0.0F, 0.01 * (double) Mth.sin(this.riptideCounter * 6.28F));
                                    this.renderPlayerArm(poseStack, submitNodeCollector, light, equipProgress, swingProgress, arm);
                                    poseStack.mulPose(Axis.XP.rotationDegrees(135.0F));
                                    poseStack.mulPose(Axis.ZP.rotationDegrees((float) (-65 * l)));
                                    poseStack.translate((double) (0.65F * (float) l), (double) -1.0F, -0.6);
                                } else {
                                    this.riptideCounter = 0.0F;
                                    float dt;
                                    if (bl) {
                                        dt = this.itemSwitchCount < 0.75038F ? Mth.sin(Mth.clamp(this.itemSwitchCount, 0.0F, 0.25F) * 3.14F * 2.0F) : Mth.sin(Mth.clamp(this.itemSwitchCount, 0.75038F, 1.0F) * 3.14F * 4.0F) * 1.2F;
                                    } else {
                                        dt = this.itemSwitchCountO < 0.75038F ? Mth.sin(Mth.clamp(this.itemSwitchCountO, 0.0F, 0.25F) * 3.14F * 2.0F) : Mth.sin(Mth.clamp(this.itemSwitchCountO, 0.75038F, 1.0F) * 3.14F * 4.0F) * 1.2F;
                                    }

                                    if (!stack.is(Items.LANTERN) && !stack.is(Items.SOUL_LANTERN) && !stack.is(ItemTags.HANGING_SIGNS) && !Block.byItem(stack.getItem()).defaultBlockState().is(FpAnimsTags.CHAINS) && !stack.is(Items.BELL)) {
                                        if (stack.getUseAnimation() == ItemUseAnimation.BLOCK) {
                                            poseStack.translate((double) 0.0F, -0.2, (double) 0.0F);
                                        }
                                    } else {
                                        poseStack.translate(0.1 * (double) l, (double) 0.0F, -0.1);
                                        poseStack.mulPose(Axis.XP.rotationDegrees(10.0F));
                                    }

                                    if (item.getDescriptionId().toLowerCase().contains("music_disc")) {
                                        poseStack.mulPose(Axis.XP.rotationDegrees(-5.0F * dt));
                                    }

                                    float jn = this.shieldCounter;
                                    float breathe = this.shieldCounter;
                                    float yawDelta = this.shieldCounterO;
                                    float pitchDelta = this.shieldCounterO;
                                    poseStack.translate((double) l, (double) 0.0F - (double) equipProgress * 0.3, 0.3);
                                    if (stack.getUseAnimation() == ItemUseAnimation.BLOCK && bl) {
                                        poseStack.translate(0.37 * (double) jn * (double) l, (double) 0.0F, -1.1 * (double) jn);
                                        poseStack.translate(-0.2 * (double) l * (double) breathe, (double) 0.0F, (double) 0.0F);
                                        poseStack.mulPose(Axis.XP.rotationDegrees((float) ((double) 10.0F * Math.sin((double) breathe * 3.14))));
                                        poseStack.mulPose(Axis.YP.rotationDegrees(70.0F * jn * (float) l));
                                    } else if (stack.getUseAnimation() == ItemUseAnimation.BLOCK && !bl) {
                                        poseStack.translate(0.37 * (double) yawDelta * (double) l, (double) 0.0F, -1.1 * (double) yawDelta);
                                        poseStack.translate(-0.2 * (double) l * (double) pitchDelta, (double) 0.0F, (double) 0.0F);
                                        poseStack.mulPose(Axis.XP.rotationDegrees((float) ((double) 10.0F * Math.sin((double) pitchDelta * 3.14))));
                                        poseStack.mulPose(Axis.YP.rotationDegrees(70.0F * yawDelta * (float) l));
                                    }

                                    poseStack.mulPose(Axis.YP.rotationDegrees((float) (45 * l)));
                                    poseStack.mulPose(Axis.ZP.rotationDegrees((float) (-40 * l)));
                                    poseStack.mulPose(Axis.XP.rotationDegrees(30.0F));
                                    if (stack.getUseAnimation() == ItemUseAnimation.BLOCK && bl) {
                                        poseStack.mulPose(Axis.YP.rotationDegrees((float) (5 * l) * jn));
                                        poseStack.mulPose(Axis.XP.rotationDegrees(-10.0F * jn));
                                        poseStack.translate((double) 0.0F, (double) 0.0F, -0.2 * (double) jn);
                                    } else if (stack.getUseAnimation() == ItemUseAnimation.BLOCK && !bl) {
                                        poseStack.mulPose(Axis.YP.rotationDegrees((float) (5 * l) * yawDelta));
                                        poseStack.mulPose(Axis.XP.rotationDegrees(-10.0F * yawDelta));
                                        poseStack.translate((double) 0.0F, (double) 0.0F, -0.2 * (double) yawDelta);
                                    }

                                    this.altSwing(poseStack, arm, swingProgress);
                                    poseStack.scale(0.9F, 0.9F, 0.9F);
                                    this.renderPlayerArm(poseStack, submitNodeCollector, light, 0.0F, 0.0F, arm);
                                }

                                poseStack.translate(-0.3 * (double) l, 0.65, -0.1);

                                if (fpanims$isOffsetHeldItem(stack, itemId)) {
                                    poseStack.translate(0.14 * (double) l, 0.0, 0.0); // tune this
                                }

                                poseStack.mulPose(Axis.YP.rotationDegrees((float) (-65 * l)));
                                poseStack.mulPose(Axis.XP.rotationDegrees(10.0F));
                                if (stack.is(ItemTags.WOOL_CARPETS)) {
                                    poseStack.translate(0.2 * (double) l, -0.1, (double) 0.0F);
                                }

                                if (Block.byItem(stack.getItem()) != Blocks.AIR && stack.getUseAnimation() != ItemUseAnimation.EAT && !stack.is(FpAnimsTags.BUCKETS)) {
                                    if (TorchDetect.isTorchLike(stack)) {
                                        if (!stack.is(Items.TORCHFLOWER)) {
                                            poseStack.scale(1.5F, 1.5F, 1.5F);
                                        }

                                        poseStack.mulPose(Axis.YN.rotationDegrees((float) (25 * l)));
                                        poseStack.mulPose(Axis.XP.rotationDegrees(5.0F));
                                        poseStack.mulPose(Axis.ZP.rotationDegrees((float) (75 * l)));
                                        poseStack.translate(0.16 * (double) l, 0.17, 0.05);
                                    } else if ((stack.is(Items.STRING) || stack.is(Items.REDSTONE) || stack.is(Items.LEVER) || stack.is(Items.TRIPWIRE_HOOK) || Block.byItem(stack.getItem()).defaultBlockState().is(FpAnimsTags.GLASS_PANES) || Block.byItem(stack.getItem()).defaultBlockState().is(BlockTags.RAILS) || Block.byItem(stack.getItem()).defaultBlockState().is(BlockTags.CLIMBABLE)) && !Block.byItem(stack.getItem()).defaultBlockState().is(BlockTags.LEAVES) && !Block.byItem(stack.getItem()).defaultBlockState().is(BlockTags.COMBINATION_STEP_SOUND_BLOCKS) && !Block.byItem(stack.getItem()).defaultBlockState().is(BlockTags.BANNERS)) {
                                        poseStack.translate((double) 0.0F, (double) 0.0F, -0.1);
                                        poseStack.mulPose(Axis.YN.rotationDegrees((float) (5 * l)));
                                        poseStack.mulPose(Axis.XP.rotationDegrees(15.0F));
                                        poseStack.mulPose(Axis.ZP.rotationDegrees((float) (75 * l)));
                                    } else if (!stack.is(Items.LANTERN) && !stack.is(Items.SOUL_LANTERN) && !stack.is(ItemTags.HANGING_SIGNS) && !stack.is(Items.NETHER_STAR)) {
                                        poseStack.mulPose(Axis.YN.rotationDegrees((float) (25 * l)));
                                        poseStack.mulPose(Axis.XP.rotationDegrees(5.0F));
                                        poseStack.mulPose(Axis.ZP.rotationDegrees((float) (75 * l)));
                                        poseStack.translate(0.2 * (double) l, 0.2, 0.05);
                                        if (Block.byItem(stack.getItem()).defaultBlockState().is(BlockTags.BANNERS)) {
                                            poseStack.translate(-0.2 * (double) l, (double) 0.0F, (double) 0.0F);
                                            poseStack.scale(1.1F, 1.1F, 1.1F);
                                        }
                                    } else {
                                        float dt = (float) (FpAnimsClient.deltaTime * FpAnimsConfig.ANIMATION_SPEED * (double) 30.0F);
                                        float jn = player.yRotO - player.getYRot();
                                        float breathe = player.xRotO - player.getXRot();
                                        this.swingVelocityY += jn * 0.015F * dt;
                                        this.swingVelocityY += swingProgress * 2.0F * dt;
                                        this.swingVelocityX += breathe * 0.015F * dt;
                                        this.swingVelocityY -= 0.1F * this.swingAngleY * dt;
                                        this.swingVelocityX -= 0.1F * this.swingAngleX * dt;
                                        this.swingVelocityY = (float) ((double) this.swingVelocityY * Math.pow((double) 0.88F, (double) dt));
                                        this.swingVelocityX = (float) ((double) this.swingVelocityX * Math.pow((double) 0.88F, (double) dt));
                                        this.swingAngleY += this.swingVelocityY * dt;
                                        this.swingAngleX += this.swingVelocityX * dt;
                                        double currentSpeed = player.getDeltaMovement().length();
                                        this.swingVelocityZ = (float) ((double) this.swingVelocityZ + (bl ? (currentSpeed * (double) -1.0F * (double) 15.0F - (double) this.swingVelocityZ) * (double) 0.1F * (double) dt : (currentSpeed * (double) 15.0F - (double) this.swingVelocityZ) * (double) 0.1F * (double) dt));
                                        if ((currentSpeed > 0.09 && player.onGround() || player.isSwimming() || player.onClimbable() && !player.isCrouching() && !player.onGround()) && (Boolean) Minecraft.getInstance().options.bobView().get()) {
                                            this.swingVelocityY += (ThreadLocalRandom.current().nextBoolean() ? -5.5F : 5.5F) * currentSpeed * dt;
                                        }

                                        if (!stack.is(Items.NETHER_STAR)) {
                                            poseStack.translate((double) 0.0F, (double) 0.0F, -0.1);
                                            float sway = !stack.is(Items.LANTERN) && !stack.is(Items.SOUL_LANTERN) ? 1.0F : 0.0F;
                                            poseStack.mulPose(Axis.YN.rotationDegrees((float) (35 * l) + this.swingAngleY * sway));
                                            poseStack.mulPose(Axis.XP.rotationDegrees(15.0F + this.swingAngleX * sway));
                                            poseStack.mulPose(Axis.ZP.rotationDegrees((float) (75 * l) + this.swingVelocityZ * sway));
                                            if (stack.is(ItemTags.HANGING_SIGNS) && !FpAnimsConfig.MB3D_COMPAT) {
                                                poseStack.translate((double) -0.25F * (double) l, -0.4, (double) 0.25F);
                                                poseStack.mulPose(Axis.YP.rotationDegrees((float) (40 * l)));
                                                poseStack.mulPose(Axis.XP.rotationDegrees(30.0F));
                                            } else if (stack.is(ItemTags.HANGING_SIGNS) && FpAnimsConfig.MB3D_COMPAT) {
                                                poseStack.translate(-0.1 * (double) l, (double) 0.0F, (double) 0.0F);
                                            }

                                            poseStack.translate(0.3 * (double) l, -0.35, (double) 0.0F);
                                            poseStack.translate((double) 0.0F, (double) 0.0F, 0.1);
                                            poseStack.scale(1.5F, 1.5F, 1.5F);
                                        }
                                    }
                                } else {
                                    if ((!stack.is(FpAnimsTags.TOOLS) || stack.is(ItemTags.TRIMMABLE_ARMOR) || stack.is(ItemTags.BOOKSHELF_BOOKS) || stack.getUseAnimation() == ItemUseAnimation.EAT || !stack.isEnchantable()) && stack.getUseAnimation() != ItemUseAnimation.BOW && stack.getUseAnimation() != ItemUseAnimation.SPYGLASS && this.getAttackDamage(stack) == 0.0F && stack.getUseAnimation() != ItemUseAnimation.BLOCK && !stack.is(Items.WARPED_FUNGUS_ON_A_STICK) && !stack.is(Items.CARROT_ON_A_STICK) && !(stack.getItem() instanceof FishingRodItem) && !stack.is(Items.SHEARS) && !(stack.getItem() instanceof HoeItem) && (!FpAnimsConfig.MB3D_COMPAT || stack.is(FpAnimsTags.BUCKETS))) {
                                        if (stack.getUseAnimation() == ItemUseAnimation.BRUSH) {
                                            poseStack.mulPose(Axis.XN.rotationDegrees(25.0F));
                                            poseStack.translate(bl ? (double) 0.0F : 0.35, bl ? (double) 0.0F : (double) 0.25F, bl ? (double) 0.0F : 0.37);
                                            if (!bl) {
                                                poseStack.scale(0.75F, 0.75F, 0.75F);
                                            }

                                            poseStack.mulPose(Axis.ZN.rotationDegrees((float) (-75 * l)));
                                            poseStack.mulPose(Axis.XN.rotationDegrees(35.0F));
                                            poseStack.translate(bl ? -0.05 : 0.85, bl ? (double) 0.0F : 0.05, bl ? 0.08 : -0.2);
                                        } else {
                                            poseStack.mulPose(Axis.YN.rotationDegrees((float) (5 * l)));
                                            poseStack.mulPose(Axis.XP.rotationDegrees(15.0F));
                                            poseStack.mulPose(Axis.ZP.rotationDegrees((float) (75 * l)));
                                            poseStack.translate((double) 0.0F, -0.05, -0.1);
                                            poseStack.scale(0.7F, 0.7F, 0.7F);
                                        }

                                        if (stack.is(Items.FEATHER) || stack.is(Items.SLIME_BALL) || stack.is(Items.PUFFERFISH)) {
                                            this.vertVelocityYSlime = (float) ((double) this.vertVelocityYSlime + (double) swingProgress * 0.03 * FpAnimsClient.deltaTime * FpAnimsConfig.ANIMATION_SPEED * (double) 30.0F);
                                            if ((player.getDeltaMovement().length() > 0.09 && player.onGround() || player.isSwimming() || player.isVisuallyCrawling() || player.onClimbable() && !player.isCrouching() && !player.onGround()) && (Boolean) Minecraft.getInstance().options.bobView().get()) {
                                                this.vertVelocityYSlime += (float) (-0.05 * player.getDeltaMovement().length() * FpAnimsClient.deltaTime * FpAnimsConfig.ANIMATION_SPEED * (double) 30.0F);
                                            }

                                            poseStack.scale(1.0F, 1.0F + this.vertAngleYSlime * -2.0F, 1.0F);
                                        }
                                    } else if (stack.getUseAnimation() == ItemUseAnimation.BLOCK && stack.getUseAnimation() != ItemUseAnimation.TRIDENT) {
                                        poseStack.mulPose(Axis.ZP.rotationDegrees((float) (160 * l)));
                                        poseStack.mulPose(Axis.YP.rotationDegrees((float) (-60 * l)));
                                        poseStack.mulPose(Axis.XP.rotationDegrees(-70.0F));
                                        poseStack.scale(0.75F, 0.75F, 0.75F);
                                        poseStack.translate(0.15 * (double) l, bl ? 0.35 : 0.45, bl ? -0.15 : -0.1);
                                        poseStack.translate(0.17 * (double) l, (double) 0.0F, 0.3);
                                        poseStack.mulPose(Axis.YP.rotationDegrees((float) (-90 * l)));
                                    } else if (stack.getUseAnimation() == ItemUseAnimation.TRIDENT) {
                                        poseStack.mulPose(Axis.YN.rotationDegrees((float) (75 * l)));
                                        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
                                        poseStack.mulPose(Axis.ZP.rotationDegrees((float) (45 * l)));
                                        poseStack.translate(-0.3F * (float) l, 0.0F, 0.0F);
                                    } else if (stack.getUseAnimation() != ItemUseAnimation.TRIDENT) {
                                        poseStack.mulPose(Axis.YN.rotationDegrees((float) (75 * l)));
                                        poseStack.mulPose(Axis.XP.rotationDegrees(70.0F));
                                        poseStack.mulPose(Axis.ZP.rotationDegrees((float) (45 * l)));
                                    }

                                    if (stack.getUseAnimation() != ItemUseAnimation.BLOCK) {
                                        poseStack.scale(1.2F, 1.2F, 1.2F);
                                    }

                                    if (stack.getUseAnimation() == ItemUseAnimation.BOW && !player.isUsingItem()) {
                                        poseStack.translate(-0.1 * (double) l, -0.2, (double) 0.0F);
                                    }

                                    if (stack.is(Items.MACE)) {
                                        if (FpAnimsConfig.MB3D_COMPAT) {
                                            poseStack.translate(-0.08, 0.17, (double) 0.0F);
                                            poseStack.mulPose(Axis.XP.rotationDegrees(40.0F));
                                        }

                                        poseStack.translate(0.1 * (double) l, (double) 0.0F, (double) 0.0F);
                                        poseStack.scale(0.9F, 0.9F, 0.9F);
                                    }
                                }

                                if (RenderHelper.shouldRenderCustom(stack) && item instanceof BlockItem) {
                                    BlockItem blockItem = (BlockItem) item;
                                    Block block = blockItem.getBlock();
                                    if ((!stack.is(FpAnimsTags.BUCKETS) && stack.getUseAnimation() != ItemUseAnimation.EAT && !stack.is(ItemTags.BANNERS) && !stack.is(Items.STRING) && !stack.is(Items.REDSTONE) && !stack.is(Items.LEVER) && !stack.is(Items.TRIPWIRE_HOOK) && !Block.byItem(stack.getItem()).defaultBlockState().is(FpAnimsTags.GLASS_PANES) && !Block.byItem(stack.getItem()).defaultBlockState().is(BlockTags.RAILS) && !Block.byItem(stack.getItem()).defaultBlockState().is(BlockTags.CLIMBABLE) || Block.byItem(stack.getItem()).defaultBlockState().is(BlockTags.LEAVES)) && !Block.byItem(stack.getItem()).defaultBlockState().is(BlockTags.COMBINATION_STEP_SOUND_BLOCKS)) {
                                        poseStack.pushPose();
                                        if (!bl2) {
                                            poseStack.translate(-0.4F, 0.0F, 0.0F);
                                        }

                                        poseStack.scale(0.4F, 0.4F, 0.4F);
                                        poseStack.translate(-0.9 * (double) l, -0.45, (double) -0.5F);
                                        if (stack.is(Items.LANTERN) || stack.is(Items.SOUL_LANTERN)) {
                                            poseStack.translate((double) 0.5F, (double) 0.5625F, (double) 0.5F);
                                            poseStack.mulPose(Axis.XP.rotationDegrees(Mth.clamp(this.swingAngleY * 1.5F, -40.0F, 40.0F)));
                                            poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.clamp((this.swingAngleX + this.swingVelocityZ) * 1.5F, -35.0F, 35.0F)));
                                            poseStack.translate((double) -0.5F, (double) -0.5625F, (double) -0.5F);
                                        }

                                        if (Block.byItem(stack.getItem()).defaultBlockState().is(BlockTags.BUTTONS)) {
                                            poseStack.translate(0.2 * (double) l, -0.15, -0.2);
                                        }

                                        if (Block.byItem(stack.getItem()).defaultBlockState().is(BlockTags.PRESSURE_PLATES)) {
                                            poseStack.translate((double) 0.0F, 0.1, (double) 0.0F);
                                        }

                                        BlockState blockState = blockItem.getBlock().defaultBlockState();
                                        if (blockState.is(BlockTags.FLOWERS) || blockState.is(FpAnimsTags.SAPLINGS) || blockState.is(Blocks.TALL_GRASS) || blockState.is(BlockTags.REPLACEABLE) || blockState.is(FpAnimsTags.REPLACEABLE_BY_MUSHROOMS)) {
                                            poseStack.pushPose();
                                            BlockEmissionRenderer.renderSingleBlockEmission(Blocks.FLOWER_POT.defaultBlockState(), poseStack, submitNodeCollector, light);
                                            poseStack.popPose();
                                            poseStack.mulPose(Axis.XN.rotationDegrees(this.swingAngleY / 3.0F));
                                        }

                                        if (stack.is(Items.SLIME_BLOCK) || stack.is(Items.HONEY_BLOCK) || Block.byItem(stack.getItem()).defaultBlockState().is(BlockTags.FLOWERS) || Block.byItem(stack.getItem()).defaultBlockState().is(BlockTags.LEAVES) || Block.byItem(stack.getItem()).defaultBlockState().is(FpAnimsTags.SAPLINGS) || Block.byItem(stack.getItem()).defaultBlockState().is(BlockTags.SWORD_EFFICIENT) || blockState.is(FpAnimsTags.REPLACEABLE_BY_MUSHROOMS) || blockState.is(BlockTags.REPLACEABLE)) {
                                            this.vertVelocityYSlime = (float) ((double) this.vertVelocityYSlime + (double) swingProgress * 0.03 * FpAnimsClient.deltaTime * FpAnimsConfig.ANIMATION_SPEED * (double) 30.0F);
                                            if ((player.getDeltaMovement().length() > 0.09 && player.onGround() || player.isSwimming() || player.isVisuallyCrawling() || player.onClimbable() && !player.isCrouching() && !player.onGround()) && (Boolean) Minecraft.getInstance().options.bobView().get()) {
                                                this.vertVelocityYSlime += (float) (-0.05 * player.getDeltaMovement().length() * FpAnimsClient.deltaTime * FpAnimsConfig.ANIMATION_SPEED * (double) 30.0F);
                                            }

                                            poseStack.scale(1.0F - this.vertAngleYSlime * -2.0F, 1.0F + this.vertAngleYSlime * -2.0F, 1.0F);
                                        }

                                        if (blockState.is(BlockTags.FLOWERS) || blockState.is(FpAnimsTags.SAPLINGS) || blockState.is(Blocks.TALL_GRASS) || blockState.is(BlockTags.REPLACEABLE) || blockState.is(FpAnimsTags.REPLACEABLE_BY_MUSHROOMS) || blockState.is(FpAnimsTags.CHAINS) || stack.is(Items.BELL)) {
                                            float yawDelta = (float) (FpAnimsClient.deltaTime * FpAnimsConfig.ANIMATION_SPEED * (double) 30.0F);
                                            float pitchDelta = player.yRotO - player.getYRot();
                                            float angularSpeed = player.xRotO - player.getXRot();
                                            this.swingVelocityY += pitchDelta * 0.015F * yawDelta;
                                            this.swingVelocityY += swingProgress * 2.0F * yawDelta;
                                            this.swingVelocityX += angularSpeed * 0.015F * yawDelta;
                                            this.swingVelocityY -= 0.1F * this.swingAngleY * yawDelta;
                                            this.swingVelocityX -= 0.1F * this.swingAngleX * yawDelta;
                                            this.swingVelocityY = (float) ((double) this.swingVelocityY * Math.pow((double) 0.88F, (double) yawDelta));
                                            this.swingVelocityX = (float) ((double) this.swingVelocityX * Math.pow((double) 0.88F, (double) yawDelta));
                                            this.swingAngleY += this.swingVelocityY * yawDelta;
                                            this.swingAngleX += this.swingVelocityX * yawDelta;
                                            double currentSpeed = player.getDeltaMovement().length();
                                            this.swingVelocityZ = (float) ((double) this.swingVelocityZ + (bl ? (currentSpeed * (double) -1.0F * (double) 15.0F - (double) this.swingVelocityZ) * (double) 0.1F * (double) yawDelta : (currentSpeed * (double) 15.0F - (double) this.swingVelocityZ) * (double) 0.1F * (double) yawDelta));
                                            if ((currentSpeed > 0.09 && player.onGround() || player.isSwimming() || player.onClimbable() && !player.isCrouching() && !player.onGround()) && (Boolean) Minecraft.getInstance().options.bobView().get()) {
                                                this.swingVelocityY += (ThreadLocalRandom.current().nextBoolean() ? -5.5F : 5.5F) * currentSpeed * yawDelta;
                                            }
                                        }

                                        if ((float) player.tickCount - this.prevAge >= 100.0F) {
                                            this.repPower = !this.repPower;
                                            this.prevAge = (float) player.tickCount;
                                        }

                                        if (blockItem.getBlock() == Blocks.NOTE_BLOCK) {
                                            if (this.isNearRedstonePrev != this.isNearRedstoneBlock(player) && this.isNearRedstoneBlock(player)) {
                                                player.playSound((SoundEvent) SoundEvents.NOTE_BLOCK_HARP.value(), 0.7F, 1.0F);
                                            }

                                            if (this.isNearRedstoneBlock(player) && this.isCrouchingPrev != player.isCrouching() && this.isCrouchingPrev != player.isCrouching() && player.isCrouching()) {
                                                player.playSound((SoundEvent) SoundEvents.NOTE_BLOCK_HARP.value(), 0.7F, ThreadLocalRandom.current().nextFloat());
                                            }
                                        }

                                        if (blockItem.getBlock() == Blocks.REPEATER && this.repPower) {
                                            blockState = (BlockState) blockState.setValue(RepeaterBlock.POWERED, true);
                                        }

                                        if (blockItem.getBlock() == Blocks.COMPARATOR && this.repPower) {
                                            blockState = (BlockState) blockState.setValue(ComparatorBlock.POWERED, true);
                                        }

                                        if (blockItem.getBlock() == Blocks.REDSTONE_TORCH && player.isUnderWater() || this.isNearRedstoneBlock(player) && blockItem.getBlock() == Blocks.REDSTONE_TORCH) {
                                            if (player.isUnderWater() != this.wasSubmerged) {
                                                player.playSound(SoundEvents.FIRE_EXTINGUISH, 0.5F, 1.0F);
                                            }

                                            blockState = (BlockState) blockState.setValue(RedstoneTorchBlock.LIT, false);
                                        }

                                        if ((blockItem.getBlock() == Blocks.CAMPFIRE || blockItem.getBlock() == Blocks.SOUL_CAMPFIRE) && player.isUnderWater()) {
                                            if (player.isUnderWater() != this.wasSubmerged) {
                                                player.playSound(SoundEvents.FIRE_EXTINGUISH, 0.5F, 1.0F);
                                            }

                                            blockState = (BlockState) blockState.setValue(CampfireBlock.LIT, false);
                                        }

                                        if (!stack.is(Items.PISTON) && !stack.is(Items.STICKY_PISTON)) {
                                            if (blockState.getLightEmission() > 0) {
                                                if (blockState.is(BlockTags.FLOWERS) || blockState.is(FpAnimsTags.SAPLINGS) || blockState.is(Blocks.TALL_GRASS) || blockState.is(BlockTags.REPLACEABLE) || blockState.is(FpAnimsTags.REPLACEABLE_BY_MUSHROOMS)) {
                                                    poseStack.translate((double) 0.0F, 0.3, (double) 0.0F);
                                                    if (blockState.is(BlockTags.FLOWERS)) {
                                                        poseStack.mulPose(Axis.XN.rotationDegrees(this.swingAngleY / 3.0F));
                                                    }
                                                }

                                                BlockEmissionRenderer.renderSingleBlockEmission(blockState, poseStack, submitNodeCollector, light);
                                            } else if (stack.is(Items.BELL)) {
                                                poseStack.translate((double) 0.0F, -1.45, (double) 0.0F);
                                                poseStack.pushPose();
                                                float yawDelta = player.yRotO - player.getYRot();
                                                float pitchDelta = player.xRotO - player.getXRot();
                                                float angularSpeed = (float) Math.sqrt((double) (yawDelta * yawDelta + pitchDelta * pitchDelta));
                                                this.soundCooldownTimer = (float) Math.max((double) 0.0F, (double) this.soundCooldownTimer - tt);
                                                if (Mth.abs(this.swingAngleY) > 13.5F && this.soundCooldownTimer <= 0.0F) {
                                                    player.playSound(SoundEvents.BELL_BLOCK, 1.0F, 1.0F);
                                                    this.soundCooldownTimer = 14.5F;
                                                }

                                                poseStack.mulPose(Axis.XP.rotationDegrees(this.swingAngleY));
                                                poseStack.mulPose(Axis.YP.rotationDegrees(this.swingAngleX / 3.0F));
                                                BlockEntityRenderDispatcher blockEntityDispatcher = Minecraft.getInstance().getBlockEntityRenderDispatcher();
                                                BellBlockEntity bellBlockEntity = new BellBlockEntity(BlockPos.ZERO, Blocks.BELL.defaultBlockState());
                                                BlockEntityRenderer<?, ?> bellRendererRaw = blockEntityDispatcher.getRenderer(bellBlockEntity);
                                                if (bellRendererRaw instanceof BellRenderer bellRendererInstance) {
                                                    BellRenderState bellState = bellRendererInstance.createRenderState();
                                                    bellRendererInstance.extractRenderState(bellBlockEntity, bellState, 0.0F, Minecraft.getInstance().gameRenderer.mainCamera().position(), null);
                                                    bellState.lightCoords = light;
                                                    bellRendererInstance.submit(bellState, poseStack, submitNodeCollector, FpAnimsClient.cameraState());
                                                }
                                                poseStack.popPose();
                                                EnumProperty<AttachFace> attachFaceProp = null;

                                                for (Property<?> prop : blockState.getProperties()) {
                                                    if (prop.getName().equals("face") && prop instanceof EnumProperty) {
                                                        attachFaceProp = (EnumProperty) prop;
                                                        break;
                                                    }
                                                }

                                                if (attachFaceProp != null) {
                                                    BlockEmissionRenderer.renderSingleBlockEmission((BlockState) blockState.setValue(attachFaceProp, AttachFace.CEILING), poseStack, submitNodeCollector, light);
                                                }
                                            } else {
                                                if (blockState.is(BlockTags.FLOWERS) || blockState.is(FpAnimsTags.SAPLINGS) || blockState.is(Blocks.TALL_GRASS) || blockState.is(BlockTags.REPLACEABLE) || blockState.is(FpAnimsTags.REPLACEABLE_BY_MUSHROOMS)) {
                                                    poseStack.translate((double) 0.0F, 0.3, (double) 0.0F);
                                                    if (blockState.is(BlockTags.FLOWERS)) {
                                                        poseStack.mulPose(Axis.XN.rotationDegrees(this.swingAngleY / 3.0F));
                                                    }
                                                }

                                                if (blockState.is(BlockTags.DOORS)) {
                                                    poseStack.translate(-0.4 * (double) l, -0.2, -0.3);
                                                    poseStack.scale(0.7F, 0.7F, 0.7F);
                                                    poseStack.mulPose(Axis.YP.rotationDegrees((float) (20 * l)));
                                                    poseStack.translate(0.0F, 0.0F, 0.5F);
                                                }

                                                if (blockState.is(FpAnimsTags.CHAINS)) {
                                                    poseStack.translate(-0.1 * (double) l, (double) -1.0F, (double) 0.0F);
                                                    poseStack.mulPose(Axis.XN.rotationDegrees(this.swingAngleY / 3.0F));
                                                    poseStack.translate(0.0F, 0.0F, 0.5F);
                                                }

                                                BlockEmissionRenderer.renderSingleBlockEmission(blockState, poseStack, submitNodeCollector, light);
                                                if (blockState.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)) {
                                                    poseStack.translate(0.0F, 1.0F, 0.0F);
                                                    if (blockState.is(BlockTags.FLOWERS) || blockState.is(Blocks.TALL_GRASS) || blockState.is(BlockTags.REPLACEABLE) || blockState.is(FpAnimsTags.REPLACEABLE_BY_MUSHROOMS)) {
                                                        poseStack.mulPose(Axis.XN.rotationDegrees(this.swingAngleY));
                                                    }

                                                    BlockEmissionRenderer.renderSingleBlockEmission((BlockState) blockState.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER), poseStack, submitNodeCollector, light);
                                                }

                                                if (blockState.is(FpAnimsTags.CHAINS)) {
                                                    poseStack.translate(0.0F, -1.0F, 0.0F);
                                                    poseStack.mulPose(Axis.XN.rotationDegrees(this.swingAngleY));
                                                    poseStack.translate(0.0F, 0.0F, 0.5F);
                                                    BlockEmissionRenderer.renderSingleBlockEmission(blockState, poseStack, submitNodeCollector, light);
                                                }
                                            }
                                        } else {
                                            if (this.isNearRedstonePrev != this.isNearRedstoneBlock(player)) {
                                                if (this.isNearRedstoneBlock(player)) {
                                                    player.playSound(SoundEvents.PISTON_EXTEND, 0.7F, 1.0F);
                                                } else {
                                                    player.playSound(SoundEvents.PISTON_CONTRACT, 0.7F, 1.0F);
                                                }
                                            }

                                            if (this.isNearRedstoneBlock(player)) {
                                                this.pistonCount = (float) ((double) this.pistonCount + 0.2 * tt);
                                            } else {
                                                this.pistonCount = (float) ((double) this.pistonCount - 0.2 * tt);
                                            }

                                            if (this.pistonCount > 1.0F) {
                                                this.pistonCount = 1.0F;
                                            } else if (this.pistonCount < 0.0F) {
                                                this.pistonCount = 0.0F;
                                            }

                                            poseStack.translate(0.08 * (double) l, (double) 0.0F, (double) 0.0F);
                                            BlockState pistonState = (BlockState) ((BlockState) Blocks.PISTON.defaultBlockState().setValue(PistonBaseBlock.EXTENDED, true)).setValue(PistonBaseBlock.FACING, Direction.UP);
                                            BlockState pistonHeadState = Blocks.AIR.defaultBlockState();
                                            if (stack.is(Items.PISTON)) {
                                                pistonHeadState = (BlockState) ((BlockState) Blocks.PISTON_HEAD.defaultBlockState().setValue(BlockStateProperties.FACING, Direction.UP)).setValue(BlockStateProperties.SHORT, false);
                                            }

                                            if (stack.is(Items.STICKY_PISTON)) {
                                                pistonHeadState = (BlockState) ((BlockState) ((BlockState) Blocks.PISTON_HEAD.defaultBlockState().setValue(BlockStateProperties.FACING, Direction.UP)).setValue(BlockStateProperties.SHORT, false)).setValue(BlockStateProperties.PISTON_TYPE, PistonType.STICKY);
                                            }

                                            BlockEmissionRenderer.renderSingleBlockEmission(pistonState, poseStack, submitNodeCollector, light);
                                            poseStack.translate((double) 0.0F, -0.0312, (double) 0.0F);
                                            poseStack.translate((double) 0.0F, 0.4 * (double) Mth.clamp(this.vertAngleYSlime * -1.0F, 0.0F, 255.0F), (double) 0.0F);
                                            poseStack.translate((double) 0.0F, 0.01 * (double) Mth.sin(this.headFallCount * 4.14F), (double) 0.0F);
                                            poseStack.translate((double) 0.0F, Mth.clamp((double) this.vertAngleY * (double) -6.5F + (double) this.pistonCount, 0.0312, 0.7), (double) 0.0F);
                                            BlockEmissionRenderer.renderSingleBlockEmission(pistonHeadState, poseStack, submitNodeCollector, light);
                                        }

                                        poseStack.popPose();
                                        break hmiBlockRest;
                                    }
                                }

                                if (BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace().equals("sophisticatedstorage") && (item.getDescriptionId().toLowerCase().contains("chest") || item.getDescriptionId().toLowerCase().contains("barrel") || item.getDescriptionId().toLowerCase().contains("shulker"))) {
                                    if (!bl) {
                                        poseStack.mulPose(Axis.YP.rotationDegrees(45.0F));
                                        poseStack.translate(0.13, (double) 0.0F, 0.1);
                                    } else {
                                        poseStack.mulPose(Axis.YP.rotationDegrees(-45.0F));
                                        poseStack.translate(-0.13, (double) 0.0F, 0.1);
                                    }
                                }

                                if (itemId.getNamespace().equals("create")) {
                                    if (itemId.getPath().equals("wrench") && !FpAnimsConfig.MB3D_COMPAT) {
                                        poseStack.translate((double) 0.0F, 0.2, 0.1);
                                        poseStack.mulPose(Axis.YP.rotationDegrees(-80.0F * (float) l));
                                        poseStack.mulPose(Axis.ZP.rotationDegrees(25.0F * (float) l));
                                    }

                                    if (!itemId.getPath().equals("belt_connector") && !itemId.getPath().equals("clipboard")) {
                                        if (itemId.getPath().equals("shaft") || itemId.getPath().equals("cogwheel") || itemId.getPath().equals("large_cogwheel")) {
                                            poseStack.translate(-0.15 * (double) l, (double) 0.0F, (double) 0.0F);
                                        }
                                    } else {
                                        poseStack.translate(-0.15 * (double) l, -0.15, -0.1);
                                        poseStack.mulPose(Axis.XP.rotationDegrees(10.0F));
                                        poseStack.scale(0.7F, 0.7F, 0.7F);
                                    }
                                }

                                float jn = Mth.sin(Mth.clamp(swingProgress, 0.0F, 0.9F) * 3.48F);
                                jn = this.easeInOutBack(jn);
                                if (stack.is(FpAnimsTags.TOOLS) && !stack.is(ItemTags.TRIMMABLE_ARMOR) && !stack.is(ItemTags.BOOKSHELF_BOOKS) && stack.getUseAnimation() != ItemUseAnimation.EAT && stack.isEnchantable() || stack.getUseAnimation() == ItemUseAnimation.BOW || stack.getUseAnimation() == ItemUseAnimation.SPYGLASS || this.getAttackDamage(stack) != 0.0F || stack.getUseAnimation() == ItemUseAnimation.BLOCK || stack.is(Items.WARPED_FUNGUS_ON_A_STICK) || stack.is(Items.CARROT_ON_A_STICK) || stack.getItem() instanceof FishingRodItem || stack.is(Items.SHEARS)) {
                                    if (stack.is(ItemTags.SWORDS)) {
                                        poseStack.mulPose(Axis.XP.rotationDegrees(-60.0F * jn));
                                        poseStack.translate((double) 0.0F, 0.1 * (double) jn, -0.1 * (double) jn);
                                    }

                                    if (stack.getItem() instanceof ShovelItem) {
                                        poseStack.mulPose(Axis.XP.rotationDegrees(-80.0F * swing_rot));
                                        poseStack.mulPose(Axis.XP.rotationDegrees(30.0F * swing));
                                    } else if (stack.getUseAnimation() == ItemUseAnimation.TRIDENT) {
                                        poseStack.mulPose(Axis.XP.rotationDegrees(-40.0F * swing_rot));
                                        poseStack.translate((double) 0.0F, 0.1 * (double) swing_rot, -0.1 * (double) swing_rot);
                                    } else if (stack.getUseAnimation() != ItemUseAnimation.BLOCK) {
                                        poseStack.mulPose(Axis.XP.rotationDegrees(-35.0F * jn));
                                        poseStack.translate((double) 0.0F, 0.05 * (double) jn, -0.05 * (double) jn);
                                    }
                                }

                                if (stack.is(Items.NETHER_STAR) || stack.is(Items.END_CRYSTAL) && FpAnimsConfig.MB3D_COMPAT && !stack.is(FpAnimsTags.BUCKETS)) {
                                    this.netherCounter = (float) ((double) this.netherCounter + 0.9 * tt);
                                    poseStack.translate((double) 0.0F, (double) 0.25F + 0.02 * (double) Mth.sin(this.netherCounter * 0.1F), (double) 0.0F);
                                    poseStack.mulPose(Axis.XP.rotationDegrees(3.0F * Mth.sin(this.netherCounter * 0.2F)));
                                    poseStack.scale(1.0F + 0.01F * Mth.sin(this.netherCounter), 1.0F + 0.01F * Mth.sin(this.netherCounter), 1.0F + 0.01F * Mth.sin(this.netherCounter));
                                    poseStack.mulPose(Axis.XP.rotationDegrees(-10.0F * this.easeInOutBack(Mth.sin(this.itemSwitchCount * 3.14F))));
                                    poseStack.mulPose(Axis.YP.rotationDegrees(360.0F * this.easeInOutBack(this.itemSwitchCount)));
                                    poseStack.translate(0.25F * (float) l, 0.0F, 0.0F);
                                } else {
                                    this.netherCounter = 0.0F;
                                }

                                if (FpAnimsConfig.MB3D_COMPAT && BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace().equals("minecraft") && !stack.is(FpAnimsTags.BUCKETS)) {
                                    if (stack.is(ItemTags.SWORDS)) {
                                        poseStack.translate((double) 0.0F, 0.2, (double) 0.0F);
                                    }

                                    if (stack.is(Items.FEATHER) || stack.is(Items.SLIME_BALL)) {
                                        this.vertVelocityYSlime = (float) ((double) this.vertVelocityYSlime + (double) swingProgress * 0.03 * FpAnimsClient.deltaTime * FpAnimsConfig.ANIMATION_SPEED * (double) 30.0F);
                                        if ((player.getDeltaMovement().length() > 0.09 && player.onGround() || player.isSwimming() || player.isVisuallyCrawling() || player.onClimbable() && !player.isCrouching() && !player.onGround()) && (Boolean) Minecraft.getInstance().options.bobView().get()) {
                                            this.vertVelocityYSlime += (float) (-0.05 * player.getDeltaMovement().length() * FpAnimsClient.deltaTime * FpAnimsConfig.ANIMATION_SPEED * (double) 30.0F);
                                        }

                                        poseStack.scale(1.0F, 1.0F + this.vertAngleYSlime * -2.0F, 1.0F);
                                    }
                                }

                                if (stack.getItem() instanceof ShovelItem) {
                                    poseStack.translate(0.07 * (double) l, (double) 0.0F, 0.05);
                                    poseStack.mulPose(Axis.YP.rotationDegrees((float) (90 * l)));
                                    poseStack.mulPose(Axis.XP.rotationDegrees(-15.0F));
                                }

                                if (item.getDescriptionId().toLowerCase().contains("music_disc")) {
                                    float breathe = this.itemSwitchCount < 0.667F ? 0.0F : Mth.sin(Mth.clamp(this.itemSwitchCount, 0.667F, 1.0F) * 3.14F * 4.0F) * 1.2F;
                                    if (bl) {
                                        poseStack.translate((double) 0.0F, 0.1 * (double) breathe, (double) 0.0F);
                                        float yawDelta = this.itemSwitchCount < 0.72783F ? Mth.sin(Mth.clamp(this.itemSwitchCount, 0.0F, 0.1F) * 3.14F * 4.0F) : Mth.sin(Mth.clamp(this.itemSwitchCount, 0.72783F, 1.0F) * 3.14F * 2.0F - 3.0F);
                                        yawDelta = this.easeInOutBack(yawDelta);
                                        poseStack.translate(0.15 * (double) yawDelta * (double) l, 0.06 * (double) yawDelta, (double) 0.0F);
                                        poseStack.mulPose(Axis.ZP.rotationDegrees((float) (90 * l) * yawDelta));
                                        poseStack.mulPose(Axis.YP.rotationDegrees((float) (17 * l) * yawDelta));
                                        poseStack.mulPose(Axis.XP.rotationDegrees(360.0F * this.easeInOutBack(this.itemSwitchCount)));
                                    } else {
                                        poseStack.translate((double) 0.0F, 0.1 * (double) breathe, (double) 0.0F);
                                        float yawDelta = this.itemSwitchCountO < 0.72783F ? Mth.sin(Mth.clamp(this.itemSwitchCountO, 0.0F, 0.1F) * 3.14F * 4.0F) : Mth.sin(Mth.clamp(this.itemSwitchCountO, 0.72783F, 1.0F) * 3.14F * 2.0F - 3.0F);
                                        yawDelta = this.easeInOutBack(yawDelta);
                                        poseStack.translate(0.15 * (double) yawDelta * (double) l, 0.06 * (double) yawDelta, (double) 0.0F);
                                        poseStack.mulPose(Axis.ZP.rotationDegrees((float) (90 * l) * yawDelta));
                                        poseStack.mulPose(Axis.YP.rotationDegrees((float) (17 * l) * yawDelta));
                                        poseStack.mulPose(Axis.XP.rotationDegrees(360.0F * this.easeInOutBack(this.itemSwitchCountO)));
                                    }
                                }

                                if (stack.is(FpAnimsTags.GEMS_TAG) || stack.isEnchanted()) {
                                    light = 15728880;
                                }

                                if (bl) {
                                    poseStack.scale(Mth.clamp(switchItems, 0.7F, 1.0F), Mth.clamp(switchItems, 0.7F, 1.0F), Mth.clamp(switchItems, 0.7F, 1.0F));
                                } else {
                                    poseStack.scale(Mth.clamp(switchItemsO, 0.7F, 1.0F), Mth.clamp(switchItemsO, 0.7F, 1.0F), Mth.clamp(switchItemsO, 0.7F, 1.0F));
                                }

                                boolean render3DBuckets = FpAnimsConfig.ENABLE_3D_BUCKETS || Minecraft.getInstance().getResourcePackRepository().getSelectedIds().contains(FpAnimsClient.BUCKET_PACK_ID);
                                if (render3DBuckets && stack.is(Items.AXOLOTL_BUCKET)) {
                                    if (fpanims$axolotlModel == null) {
                                        fpanims$axolotlModel = new AdultAxolotlModel(Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.AXOLOTL));
                                    }
                                    Entity fish = EntityTypes.AXOLOTL.create(Minecraft.getInstance().level, EntitySpawnReason.BUCKET);
                                    if (fish instanceof Axolotl axolotlEntity) {
                                        SubmitNodeCollector collector = submitNodeCollector;
                                        AdultAxolotlModel model = fpanims$axolotlModel;
                                        AxolotlRenderState renderState = new AxolotlRenderState();
                                        renderState.ageInTicks = (float) axolotlEntity.tickCount + partialTicks;
                                        renderState.variant = axolotlEntity.getVariant();
                                        renderState.playingDeadFactor = 0.0F;
                                        renderState.inWaterFactor = 1.0F;
                                        renderState.movingFactor = 0.0F;
                                        renderState.onGroundFactor = 0.0F;
                                        model.setupAnim(renderState);
                                        if (model instanceof AxolotlModelAccessor accessor) {
                                            ModelPart head = accessor.getHead();
                                            head.zRot = (float) Math.toRadians((double) (10.0F * Mth.sin(this.headFallCount * 2.7F)));
                                            head.xRot = (float) Math.toRadians((double) (70.0F + 130.0F * this.vertAngleYSlime + 5.0F * Mth.sin(this.globalAnimationCounter * 0.56F)));
                                            ModelPart part2 = accessor.getLeftLeg();
                                            ModelPart right = accessor.getRightLeg();
                                            ModelPart tail = accessor.getTail();
                                            ModelPart backLegL = accessor.getLeftLegB();
                                            ModelPart backLegR = accessor.getRightLegB();
                                            tail.visible = false;
                                            backLegL.visible = false;
                                            backLegR.visible = false;
                                            right.xRot = (float) Math.toRadians((double) (30.0F + 220.0F * this.vertAngleYSlime * 1.4F + 10.0F * Mth.sin(this.headFallCount * 2.7F) + 2.0F * Mth.sin(this.globalAnimationCounter)));
                                            part2.xRot = right.xRot;
                                            part2.yRot = (float) Math.toRadians((double) (-10.0F * this.vertAngleYSlime));
                                            right.yRot = part2.yRot;
                                            right.zRot = (float) Math.toRadians((double) (30.0F - 60.0F * this.vertAngleYSlime + 10.0F * Mth.sin(this.headFallCount * 2.7F)));
                                            part2.zRot = (float) Math.toRadians((double) (-30.0F - 60.0F * this.vertAngleYSlime + 10.0F * Mth.sin(this.headFallCount * 2.7F)));
                                            poseStack.pushPose();
                                            poseStack.translate(0.76 * (double) l, 0.73, -0.025);
                                            poseStack.mulPose(Axis.ZP.rotationDegrees((float) (120 * l)));
                                            poseStack.mulPose(Axis.YP.rotationDegrees((float) (-70 * l)));
                                            if (axolotlEntity.isBaby()) {
                                                poseStack.scale(1.0F, 1.0F, 1.0F);
                                            } else {
                                                poseStack.scale(0.7F, 0.7F, 0.7F);
                                            }

                                            Axolotl.Variant variant = axolotlEntity.getVariant();
                                            Identifier texture = Identifier.fromNamespaceAndPath("minecraft", "textures/entity/axolotl/axolotl_" + variant.name().toLowerCase() + ".png");
                                            collector.submitModel(model, renderState, poseStack, RenderTypes.entityCutout(texture), light, net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY, -1, null, 0, null);
                                            tail.visible = true;
                                            backLegL.visible = true;
                                            backLegR.visible = true;
                                            poseStack.popPose();
                                        }

                                        this.renderItem(player, Items.WATER_BUCKET.getDefaultInstance(), bl2 ? ItemDisplayContext.FIRST_PERSON_RIGHT_HAND : ItemDisplayContext.FIRST_PERSON_LEFT_HAND, poseStack, collector, light);
                                        ci.cancel();
                                    }
                                } else if (render3DBuckets && stack.is(Items.PUFFERFISH_BUCKET)) {
                                    poseStack.pushPose();
                                    float breathe = 0.01F * Mth.sin(this.globalAnimationCounter * 0.4F);
                                    poseStack.translate((double) 0.0F, 0.2 + (double) breathe, -0.01);
                                    poseStack.mulPose(Axis.XP.rotationDegrees(-10.0F));
                                    poseStack.mulPose(Axis.YP.rotationDegrees((float) (20 * l)));
                                    poseStack.mulPose(Axis.ZP.rotationDegrees((float) (10 * l)));
                                    this.vertVelocityYSlime = (float) ((double) this.vertVelocityYSlime + (double) swingProgress * 0.03 * FpAnimsClient.deltaTime * FpAnimsConfig.ANIMATION_SPEED * (double) 30.0F);
                                    if ((player.getDeltaMovement().length() > 0.09 && player.onGround() || player.isSwimming() || player.isVisuallyCrawling() || player.onClimbable() && !player.isCrouching() && !player.onGround()) && (Boolean) Minecraft.getInstance().options.bobView().get()) {
                                        this.vertVelocityYSlime += (float) (-0.05 * player.getDeltaMovement().length() * FpAnimsClient.deltaTime * FpAnimsConfig.ANIMATION_SPEED * (double) 30.0F);
                                    }

                                    poseStack.scale(0.83F + breathe, 0.83F + this.vertAngleYSlime * -2.0F + breathe, 0.83F + breathe);
                                    this.renderItem(player, Items.PUFFERFISH.getDefaultInstance(), bl2 ? ItemDisplayContext.FIRST_PERSON_RIGHT_HAND : ItemDisplayContext.FIRST_PERSON_LEFT_HAND, poseStack, submitNodeCollector, light);
                                    poseStack.popPose();
                                    this.renderItem(player, Items.WATER_BUCKET.getDefaultInstance(), bl2 ? ItemDisplayContext.FIRST_PERSON_RIGHT_HAND : ItemDisplayContext.FIRST_PERSON_LEFT_HAND, poseStack, submitNodeCollector, light);
                                    ci.cancel();
                                } else if (render3DBuckets && stack.is(Items.SALMON_BUCKET)) {
                                    poseStack.pushPose();
                                    if (!bl) {
                                        poseStack.translate(-0.05, (double) 0.0F, (double) 0.0F);
                                    }

                                    poseStack.translate(0.07 * (double) l, 0.24 - (double) this.vertAngleYSlime, -0.06);
                                    Entity fish = EntityTypes.SALMON.create(Minecraft.getInstance().level, EntitySpawnReason.BUCKET);
                                    poseStack.mulPose(Axis.YP.rotationDegrees(-100.0F));
                                    poseStack.mulPose(Axis.ZP.rotationDegrees(40.0F));
                                    poseStack.mulPose(Axis.XP.rotationDegrees(-80.0F));
                                    poseStack.scale(0.5F, 0.5F, 0.5F);
                                    if (fish != null) {
                                        fpanims$submitEntity(fish, poseStack, submitNodeCollector);
                                    }

                                    poseStack.popPose();
                                    this.renderItem(player, Items.WATER_BUCKET.getDefaultInstance(), bl2 ? ItemDisplayContext.FIRST_PERSON_RIGHT_HAND : ItemDisplayContext.FIRST_PERSON_LEFT_HAND, poseStack, submitNodeCollector, light);
                                    ci.cancel();
                                } else if (render3DBuckets && stack.is(Items.COD_BUCKET)) {
                                    poseStack.pushPose();
                                    poseStack.translate(0.07 * (double) l, 0.24 - (double) this.vertAngleYSlime, -0.06);
                                    Entity fish = EntityTypes.COD.create(Minecraft.getInstance().level, EntitySpawnReason.BUCKET);
                                    poseStack.mulPose(Axis.YP.rotationDegrees(-100.0F));
                                    poseStack.mulPose(Axis.ZP.rotationDegrees(40.0F));
                                    poseStack.mulPose(Axis.XP.rotationDegrees(-80.0F));
                                    poseStack.scale(0.5F, 0.5F, 0.5F);
                                    if (fish != null) {
                                        fpanims$submitEntity(fish, poseStack, submitNodeCollector);
                                    }

                                    poseStack.popPose();
                                    this.renderItem(player, Items.WATER_BUCKET.getDefaultInstance(), bl2 ? ItemDisplayContext.FIRST_PERSON_RIGHT_HAND : ItemDisplayContext.FIRST_PERSON_LEFT_HAND, poseStack, submitNodeCollector, light);
                                    ci.cancel();
                                } else if (render3DBuckets && stack.is(Items.TROPICAL_FISH_BUCKET)) {
                                    poseStack.pushPose();
                                    if (!bl) {
                                        poseStack.translate(-0.05, (double) 0.0F, (double) 0.0F);
                                    }

                                    poseStack.translate(0.07 * (double) l, 0.24 - (double) this.vertAngleYSlime, (double) 0.0F);
                                    Entity fish = EntityTypes.TROPICAL_FISH.create(Minecraft.getInstance().level, EntitySpawnReason.BUCKET);
                                    poseStack.mulPose(Axis.YP.rotationDegrees(-100.0F));
                                    poseStack.mulPose(Axis.ZP.rotationDegrees(40.0F));
                                    poseStack.mulPose(Axis.XP.rotationDegrees(-80.0F));
                                    poseStack.scale(0.7F, 0.7F, 0.7F);
                                    if (fish != null) {
                                        fpanims$submitEntity(fish, poseStack, submitNodeCollector);
                                    }

                                    poseStack.popPose();
                                    this.renderItem(player, Items.WATER_BUCKET.getDefaultInstance(), bl2 ? ItemDisplayContext.FIRST_PERSON_RIGHT_HAND : ItemDisplayContext.FIRST_PERSON_LEFT_HAND, poseStack, submitNodeCollector, light);
                                    ci.cancel();
                                } else if (render3DBuckets && stack.is(Items.TADPOLE_BUCKET)) {
                                    poseStack.pushPose();
                                    poseStack.translate(-0.1 * (double) l, 0.16 - (double) this.vertAngleYSlime, (double) 0.0F);
                                    Entity fish = EntityTypes.TADPOLE.create(Minecraft.getInstance().level, EntitySpawnReason.BUCKET);
                                    poseStack.mulPose(Axis.YP.rotationDegrees((float) (-100 * l)));
                                    poseStack.mulPose(Axis.ZP.rotationDegrees((float) (20 * l)));
                                    poseStack.mulPose(Axis.XP.rotationDegrees(-60.0F));
                                    if (fish != null) {
                                        fpanims$submitEntity(fish, poseStack, submitNodeCollector);
                                    }

                                    poseStack.popPose();
                                    this.renderItem(player, Items.WATER_BUCKET.getDefaultInstance(), bl2 ? ItemDisplayContext.FIRST_PERSON_RIGHT_HAND : ItemDisplayContext.FIRST_PERSON_LEFT_HAND, poseStack, submitNodeCollector, light);
                                    ci.cancel();
                                } else {
                                    if (arm == player.getMainArm()) {
                                        poseStack.translate(-0.2 * (double) Mth.sin(Mth.clamp(this.inspectSpinCounter * 3.14F, 0.0F, 0.5F)) * (double) l, (double) 0.0F, (double) 0.0F);
                                        poseStack.mulPose(Axis.XP.rotationDegrees(-360.0F * this.easeInOutBack(this.inspectSpinCounter) * (float) l));
                                    }

                                    this.renderItem(player, stack, bl2 ? ItemDisplayContext.FIRST_PERSON_RIGHT_HAND : ItemDisplayContext.FIRST_PERSON_LEFT_HAND, poseStack, submitNodeCollector, light);
                                    ci.cancel();
                                }
                            }
                        }

                        poseStack.popPose();
                        poseStack.popPose();
                        this.prevItemM = player.getMainHandItem();
                        this.prevItemO = player.getOffhandItem();
                        this.isAttacking = Minecraft.getInstance().options.keyAttack.isDown();
                        this.wasSubmerged = player.isUnderWater();
                        this.isNearRedstonePrev = this.isNearRedstoneBlock(player);
                        this.isCrouchingPrev = player.isCrouching();
                        this.useItemPrev = player.isUsingItem();
                        ci.cancel();
                    }

                }
            }
        }
    }

    private void fpanims$submitEntity(Entity fish, PoseStack poseStack, SubmitNodeCollector submitNodeCollector) {
        EntityRenderDispatcher entityDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        EntityRenderState fishState = entityDispatcher.extractEntity(fish, 0.0F);
        if (fishState != null) {
            entityDispatcher.submit(fishState, FpAnimsClient.cameraState(), 0.0, 0.0, 0.0, poseStack, submitNodeCollector);
        }
    }
}
