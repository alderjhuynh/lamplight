package com.aura.lamplight.menagerie.client;

import com.aura.lamplight.menagerie.raccoon.RaccoonEntity;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.item.ItemStack;

public class RaccoonRenderState extends LivingEntityRenderState {
    public float standProgress;
    public float washProgress;
    public float eatProgress;
    public int attackTick;
    public ItemStack heldItem = ItemStack.EMPTY;
    public RaccoonEntity raccoon;
}
