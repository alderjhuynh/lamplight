package com.aura.picturesque.client;

import com.aura.picturesque.client.color.PicturesquePalette;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Dusty golden light shafts on a budget: a whisper of slow golden motes
 * drifting through leaves and near windows.
 *
 * <p>Spawns at most a few {@link DustParticleOptions} motes per tick around
 * the camera when the sky is visible and light is warm (morning/golden/dusk
 * score highest, night leans on lanterns instead). No new particle type, no
 * shader: just vanilla dust tinted {@link PicturesquePalette#MOTE_GOLD}.
 * Respects the {@code lightShaftMotes} toggle and bails out silently if the
 * level or camera is unavailable.
 */
public final class MoteSpawner {
    private static final DustParticleOptions MOTE = new DustParticleOptions(PicturesquePalette.MOTE_GOLD, 0.55f);

    private MoteSpawner() {
    }

    public static void tick(Minecraft client) {
        PicturesqueState state = PicturesqueState.get();
        PicturesqueConfig config = state.config();
        if (config == null || !config.enabled || !config.lightShaftMotes) {
            return;
        }
        ClientLevel level = client.level;
        if (level == null || client.player == null) {
            return;
        }
        // Day edges read best; at deep night lanterns carry the mood instead.
        float nightness = DayPhase_nightFactor(level);
        if (nightness > 0.85f) {
            return;
        }
        if (level.getRainLevel(1.0f) > 0.6f) {
            return;
        }

        RandomSource random = level.getRandom();
        // 0-2 motes per tick keeps it a whisper, not weather.
        int count = random.nextInt(3);
        if (count == 0) {
            return;
        }
        BlockPos center = client.gameRenderer.mainCamera().blockPosition();
        for (int i = 0; i < count; i++) {
            int dx = random.nextInt(17) - 8;
            int dy = random.nextInt(7) - 2;
            int dz = random.nextInt(17) - 8;
            BlockPos pos = center.offset(dx, dy, dz);
            if (!level.canSeeSky(pos)) {
                continue;
            }
            int sky = level.getBrightness(LightLayer.SKY, pos);
            if (sky < 10) {
                continue;
            }
            BlockState blockState = level.getBlockState(pos);
            if (!blockState.isAir()) {
                continue;
            }
            double x = pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.8;
            double y = pos.getY() + 0.5 + (random.nextDouble() - 0.5) * 0.8;
            double z = pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.8;
            double vx = (random.nextDouble() - 0.5) * 0.008;
            double vy = -0.004 - random.nextDouble() * 0.008;
            double vz = (random.nextDouble() - 0.5) * 0.008;
            level.addParticle(MOTE, x, y, z, vx, vy, vz);
        }
    }

    private static float DayPhase_nightFactor(ClientLevel level) {
        return com.aura.picturesque.client.color.DayPhase.nightFactor(level.getDefaultClockTime());
    }
}
