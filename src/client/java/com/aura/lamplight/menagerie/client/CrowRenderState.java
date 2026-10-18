package com.aura.lamplight.menagerie.client;

import com.aura.lamplight.menagerie.crow.CrowEntity;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.item.ItemStack;

public class CrowRenderState extends LivingEntityRenderState {
    public float flyProgress;
    public float sitProgress;
    public float attackProgress;
    public ItemStack heldItem = ItemStack.EMPTY;
    public CrowEntity crow;
}
