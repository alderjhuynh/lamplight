package com.aura.lamplight.picturesque.client.splash;

import com.aura.lamplight.picturesque.client.PicturesqueConfig;
import com.aura.lamplight.picturesque.client.PicturesqueState;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings ("deprecation")
public final class SplashManager {
    private static final SplashManager INSTANCE = new SplashManager();

    private static final float REFERENCE_WIDTH = 0.6F;
    private static final float MIN_SCALE = 0.3F;
    private static final float MAX_SCALE = 16.0F;

    private static final float FULL_IMPACT_SPEED = 1.2F;
    private static final float SCALE_AT_MIN_SPEED = 0.85F;
    private static final float SCALE_AT_FULL_SPEED = 1.3F;

    private static final int MAX_EFFECTS = 48;
    private static final double MAX_SPAWN_DISTANCE_SQR = 96.0 * 96.0;
    private static final double MAX_RENDER_DISTANCE_SQR = 128.0 * 128.0;
    private static final int MAX_SURFACE_CLIMB = 24;

    private static final float RIM_OUTSET = 0.03F;

    private final List<SplashEffect> effects = new ArrayList<>();
    private @Nullable ClientLevel level;

    private SplashManager() {}

    public static SplashManager get() {
        return INSTANCE;
    }

    public void onFluidEntry(Entity entity, boolean lava) {
        PicturesqueConfig config = PicturesqueState.get().config();
        if (config == null || !config.splashes) {
            return;
        }
        if (!(entity.level() instanceof ClientLevel clientLevel)) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (clientLevel != mc.level || mc.player == null) {
            return;
        }
        if (entity.isPassenger() || entity.isRemoved()) {
            return;
        }
        if (entity.distanceToSqr(mc.player) > MAX_SPAWN_DISTANCE_SQR) {
            return;
        }

        Vec3 motion = entity.getDeltaMovement();
        double down = Math.max(-motion.y, -entity.getKnownSpeed().y);
        if (down < config.splashMinSpeed) {
            return;
        }

        TagKey<Fluid> tag = lava ? FluidTags.LAVA : FluidTags.WATER;
        Surface surface = findSurface(clientLevel, entity, tag);
        if (surface == null) {
            return;
        }

        double impact = Mth.clamp((down - config.splashMinSpeed) / Math.max(1.0E-3, FULL_IMPACT_SPEED - config.splashMinSpeed), 0.0, 1.0);
        float hitboxScale = Mth.clamp(entity.getBbWidth() / REFERENCE_WIDTH, MIN_SCALE, MAX_SCALE);
        float scale = hitboxScale * Mth.lerp((float) impact, SCALE_AT_MIN_SPEED, SCALE_AT_FULL_SPEED);

        double x = entity.getBoundingBox().getCenter().x;
        double z = entity.getBoundingBox().getCenter().z;

        for (SplashEffect existing : this.effects) {
            if (existing.lava == lava && existing.age < 3) {
                double dx = existing.x - x;
                double dz = existing.z - z;
                if (dx * dx + dz * dz < 0.09 && Math.abs(existing.y - surface.y) < 1.0) {
                    return;
                }
            }
        }

        int tint = lava ? -1 : waterTint(clientLevel, surface);
        if (this.effects.size() >= MAX_EFFECTS) {
            this.effects.remove(0);
        }
        this.level = clientLevel;
        this.effects.add(new SplashEffect(x, surface.y, z, scale, lava, tint, surface.pos.above()));
    }

    private record Surface(BlockPos pos, BlockState state, FluidState fluid, double y) {}

    private static @Nullable Surface findSurface(Level level, Entity entity, TagKey<Fluid> tag) {
        var box = entity.getBoundingBox();
        double cx = box.getCenter().x;
        double cz = box.getCenter().z;
        int minY = Mth.floor(box.minY);
        int maxY = Mth.floor(box.maxY);

        int centreX = Mth.floor(cx);
        int centreZ = Mth.floor(cz);
        Surface found = surfaceInColumn(level, centreX, centreZ, minY, maxY, tag);
        if (found != null) {
            return found;
        }
        for (int bx = Mth.floor(box.minX); bx <= Mth.floor(box.maxX); bx++) {
            for (int bz = Mth.floor(box.minZ); bz <= Mth.floor(box.maxZ); bz++) {
                if (bx == centreX && bz == centreZ) {
                    continue;
                }
                found = surfaceInColumn(level, bx, bz, minY, maxY, tag);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private static @Nullable Surface surfaceInColumn(Level level, int x, int z, int minY, int maxY, TagKey<Fluid> tag) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int y = maxY; y >= minY; y--) {
            pos.set(x, y, z);
            if (!level.getFluidState(pos).is(tag)) {
                continue;
            }
            for (int i = 0; i < MAX_SURFACE_CLIMB; i++) {
                FluidState above = level.getFluidState(pos.above());
                if (!above.is(tag) || isFalling(above)) {
                    break;
                }
                pos.move(0, 1, 0);
            }
            BlockPos top = pos.immutable();
            FluidState fluid = level.getFluidState(top);
            return new Surface(top, level.getBlockState(top), fluid, top.getY() + fluid.getHeight(level, top));
        }
        return null;
    }

    private static boolean isFalling(FluidState state) {
        return state.hasProperty(FlowingFluid.FALLING) && state.getValue(FlowingFluid.FALLING);
    }

    private static int waterTint(ClientLevel level, Surface surface) {
        BlockTintSource source = null;
        try {
            source = Minecraft.getInstance().getModelManager().getFluidStateModelSet().get(surface.fluid()).tintSource();
        } catch (RuntimeException ignored) {
        }
        if (source == null) {
            return -1;
        }
        return 0xFF000000 | source.colorInWorld(surface.state(), level, surface.pos());
    }

    public void tick(Minecraft mc) {
        if (this.effects.isEmpty()) {
            return;
        }
        if (mc.level == null || mc.level != this.level) {
            this.effects.clear();
            this.level = null;
            return;
        }
        if (mc.isPaused()) {
            return;
        }
        for (int i = this.effects.size() - 1; i >= 0; i--) {
            SplashEffect effect = this.effects.get(i);
            effect.age++;
            if (effect.expired()) {
                this.effects.remove(i);
            }
        }
    }

    public void collect(LevelRenderContext ctx) {
        if (this.effects.isEmpty()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        ClientLevel clientLevel = mc.level;
        if (clientLevel == null || clientLevel != this.level) {
            return;
        }
        PoseStack pose = ctx.poseStack();
        SubmitNodeCollector collector = ctx.submitNodeCollector();
        if (pose == null || collector == null) {
            return;
        }
        CameraRenderState cam = ctx.levelState().cameraRenderState;
        if (cam == null || !cam.initialized || cam.pos == null) {
            return;
        }
        float partial = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);

        for (SplashEffect effect : this.effects) {
            double dx = effect.x - cam.pos.x;
            double dy = effect.y - cam.pos.y;
            double dz = effect.z - cam.pos.z;
            if (dx * dx + dy * dy + dz * dz > MAX_RENDER_DISTANCE_SQR) {
                continue;
            }
            float age = effect.age + partial;
            int light = clientLevel.hasChunkAt(effect.lightPos)
                    ? LightCoordsUtil.getLightCoords(clientLevel, effect.lightPos)
                    : LightCoordsUtil.FULL_SKY;

            pose.pushPose();
            try {
                pose.translate((float) dx, (float) dy, (float) dz);
                if (effect.lava) {
                    submitLava(collector, pose, effect, age);
                } else {
                    submitWater(collector, pose, effect, age, light);
                }
            } finally {
                pose.popPose();
            }
        }
    }

    private static void submitWater(SubmitNodeCollector collector, PoseStack pose, SplashEffect effect, float age, int light) {
        float scale = effect.scale;
        int tint = effect.tint;
        int white = 0xFFFFFFFF;

        int ripple = (int) (age / SplashEffect.RIPPLE_TICKS_PER_FRAME);
        if (ripple >= 0 && ripple < SplashTextures.RIPPLE_FRAMES) {
            submit(collector, pose, RenderTypes.entityTranslucent(SplashTextures.RIPPLE[ripple]),
                    (leaf, vc) -> SplashGeometry.ripple(vc, leaf, scale, tint, light));
        }

        int frame = (int) (age / SplashEffect.SPLASH_TICKS_PER_FRAME);
        if (frame < 0 || frame >= SplashTextures.SPLASH_FRAMES) {
            return;
        }
        if (frame < SplashTextures.SPLASH_BODY_FRAMES) {
            submit(collector, pose, RenderTypes.entityTranslucent(SplashTextures.SPLASH[frame]),
                    (leaf, vc) -> SplashGeometry.walls(vc, leaf, scale, 0.0F, tint, light));
        }
        float outset = RIM_OUTSET * scale;
        submit(collector, pose, RenderTypes.entityTranslucent(SplashTextures.RIM[frame]), (leaf, vc) -> {
            SplashGeometry.walls(vc, leaf, scale, outset, white, light);
            SplashGeometry.ring(vc, leaf, scale, white, light);
        });
    }

    private static void submitLava(SubmitNodeCollector collector, PoseStack pose, SplashEffect effect, float age) {
        int frame = (int) (age / SplashEffect.SPLASH_TICKS_PER_FRAME);
        if (frame < 0 || frame >= SplashTextures.SPLASH_FRAMES) {
            return;
        }
        float scale = effect.scale;
        int white = 0xFFFFFFFF;
        submit(collector, pose, RenderTypes.entityTranslucentEmissive(SplashTextures.LAVA[frame]), (leaf, vc) -> {
            SplashGeometry.walls(vc, leaf, scale, 0.0F, white, LightCoordsUtil.FULL_BRIGHT);
            SplashGeometry.ring(vc, leaf, scale, white, LightCoordsUtil.FULL_BRIGHT);
        });
    }

    private static void submit(SubmitNodeCollector collector, PoseStack pose,
                               RenderType type,
                               SubmitNodeCollector.CustomGeometryRenderer renderer) {
        collector.submitCustomGeometry(pose, type, renderer);
    }
}
