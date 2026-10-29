package com.aura.lamplight.mixin.tinkerer;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ServerPlayer.class)
public interface ServerPlayerInitMenuInvoker {
	@Invoker("initMenu")
	void lamplight$invokeInitMenu(AbstractContainerMenu menu);
}
