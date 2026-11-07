package com.aura.lamplight.client.mixin.puppeteer.fpanims;

import net.minecraft.client.model.animal.axolotl.AdultAxolotlModel;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AdultAxolotlModel.class)
public interface AxolotlModelAccessor {
    @Accessor("head")
    ModelPart getHead();

    @Accessor("leftFrontLeg")
    ModelPart getLeftLeg();

    @Accessor("rightFrontLeg")
    ModelPart getRightLeg();

    @Accessor("tail")
    ModelPart getTail();

    @Accessor("leftHindLeg")
    ModelPart getLeftLegB();

    @Accessor("rightHindLeg")
    ModelPart getRightLegB();
}
