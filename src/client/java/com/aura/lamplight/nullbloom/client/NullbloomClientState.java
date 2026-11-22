package com.aura.lamplight.nullbloom.client;

import com.aura.lamplight.nullbloom.NullbloomDimension;
import net.minecraft.client.Minecraft;

public final class NullbloomClientState {
	private NullbloomClientState() {
	}

	public static boolean inside() {
		Minecraft client = Minecraft.getInstance();
		return client != null && NullbloomDimension.is(client.level);
	}
}
