package com.aura.lamplight.mixin.armory;

import com.aura.lamplight.armory.BackSlotAttachments;
import com.aura.lamplight.armory.DualWielded;
import com.aura.lamplight.armory.TwoHanded;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.EquipmentSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerMixin {
	@Shadow
	public ServerPlayer player;

	@Inject(method = "handlePlayerAction", at = @At("HEAD"), cancellable = true)
	private void armory$disarmBeforeOffhandSwap(ServerboundPlayerActionPacket packet, CallbackInfo ci) {
		if (packet.getAction() == ServerboundPlayerActionPacket.Action.SWAP_ITEM_WITH_OFFHAND) {
			boolean blocked = TwoHanded.isTwoHanded(this.player.getMainHandItem())
					|| DualWielded.isDualWielded(this.player.getMainHandItem())
					|| DualWielded.isGhost(this.player.getItemBySlot(EquipmentSlot.OFFHAND));
			BackSlotAttachments.setHolding(this.player, false);
			if (blocked) {
				ci.cancel();
			}
		}
	}
}
