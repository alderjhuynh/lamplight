package com.aura.lamplight.client.mixin.menagerie;

import com.aura.lamplight.menagerie.client.CrowShoulderRenderState;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(AvatarRenderState.class)
public abstract class AvatarRenderStateMixin implements CrowShoulderRenderState {
    @Unique
    private boolean menagerie$crowOnLeftShoulder;

    @Unique
    private boolean menagerie$crowOnRightShoulder;

    @Override
    public boolean menagerie$hasCrowOnLeftShoulder() {
        return this.menagerie$crowOnLeftShoulder;
    }

    @Override
    public boolean menagerie$hasCrowOnRightShoulder() {
        return this.menagerie$crowOnRightShoulder;
    }

    @Override
    public void menagerie$setCrowOnLeftShoulder(boolean present) {
        this.menagerie$crowOnLeftShoulder = present;
    }

    @Override
    public void menagerie$setCrowOnRightShoulder(boolean present) {
        this.menagerie$crowOnRightShoulder = present;
    }
}
