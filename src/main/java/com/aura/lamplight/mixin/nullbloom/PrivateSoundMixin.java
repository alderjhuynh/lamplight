package com.aura.lamplight.mixin.nullbloom;

import com.aura.lamplight.nullbloom.NullbloomDimension;
import net.minecraft.core.Holder;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public abstract class PrivateSoundMixin {
	@Inject(
		method = "playSeededSound(Lnet/minecraft/world/entity/Entity;DDDLnet/minecraft/core/Holder;Lnet/minecraft/sounds/SoundSource;FFJ)V",
		at = @At("HEAD"),
		cancellable = true
	)
	private void nullbloom$privatePositionSound(
		@Nullable Entity except, double x, double y, double z, Holder<SoundEvent> sound, SoundSource source,
		float volume, float pitch, long seed, CallbackInfo ci
	) {
		ServerLevel self = (ServerLevel) (Object) this;
		if (!NullbloomDimension.is(self)) {
			return;
		}
		if (except instanceof Player) {
			ci.cancel();
		} else if (except == null && source == SoundSource.PLAYERS) {
			ClientboundSoundPacket packet = new ClientboundSoundPacket(sound, source, x, y, z, volume, pitch, seed);
			for (ServerPlayer player : self.players()) {
				if (player.distanceToSqr(x, y, z) <= 4.0) {
					player.connection.send(packet);
				}
			}
			ci.cancel();
		}
	}

	@Inject(
		method = "playSeededSound(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/core/Holder;Lnet/minecraft/sounds/SoundSource;FFJ)V",
		at = @At("HEAD"),
		cancellable = true
	)
	private void nullbloom$privateEntitySound(
		@Nullable Entity except, Entity sourceEntity, Holder<SoundEvent> sound, SoundSource source,
		float volume, float pitch, long seed, CallbackInfo ci
	) {
		if (NullbloomDimension.is((ServerLevel) (Object) this) && (except instanceof Player || sourceEntity instanceof Player)) {
			ci.cancel();
		}
	}
}
