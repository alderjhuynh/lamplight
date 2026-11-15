package com.aura.lamplight.armory.client;

import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;

public class ThrownChakramRenderState extends EntityRenderState {
	public final ItemStackRenderState item = new ItemStackRenderState();
	public boolean returning;
}