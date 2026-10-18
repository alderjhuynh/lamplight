package com.aura.lamplight.arcanum.client.ability;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

public class FlowGlideSound extends AbstractTickableSoundInstance {
    private final LocalPlayer player;

    public FlowGlideSound(LocalPlayer player, SoundEvent sound) {
        super(sound, SoundSource.PLAYERS, RandomSource.create());
        this.player = player;
        this.looping = true;
        this.delay = 0;
        this.volume = 1.0F;
        this.pitch = 1.0F;
        this.x = player.getX();
        this.y = player.getY();
        this.z = player.getZ();
    }

    @Override
    public void tick() {
        if (player == null || player.isRemoved() || !FlowGlide.isGliding) {
            this.stop();
            return;
        }
        Vec3 pos = player.position();
        this.x = pos.x;
        this.y = pos.y;
        this.z = pos.z;
        float speed = (float) player.getDeltaMovement().length();
        this.volume = Math.min(1.0F, 0.5F + speed);
    }
}
