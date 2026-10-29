package com.aura.lamplight.tinkerer.charm;

import com.aura.lamplight.tinkerer.Tinkerer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("all") 
public final class CharmEvents {
	private CharmEvents() {}

	private static final ThreadLocal<Boolean> CHARM_PROC = ThreadLocal.withInitial(() -> false);

	public static boolean isCharmProc() {
		return CHARM_PROC.get();
	}

	private static final Map<UUID, String> SIGNATURES = new HashMap<>();

	public static void initialize() {
		PayloadTypeRegistry.clientboundPlay().register(CharmSyncPayload.TYPE, CharmSyncPayload.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(CharmSyncRequestPayload.TYPE, CharmSyncRequestPayload.CODEC);

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			CharmSlotAttributes.ensureBases(handler.player);
			ServerPlayNetworking.send(handler.player, CharmSyncPayload.snapshot(handler.player));
			CharmDataSyncer.acknowledgeJoinSync();
		});
		ServerPlayNetworking.registerGlobalReceiver(CharmSyncRequestPayload.TYPE, (payload, context) -> {
			context.server().execute(() -> {
				ServerPlayNetworking.send(context.player(), CharmSyncPayload.snapshot(context.player()));
				context.player().inventoryMenu.broadcastFullState();
			});
		});
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
			SIGNATURES.remove(handler.player.getUUID());
			CharmDataSyncer.forget(handler.player.getUUID());
		});

		ServerLivingEntityEvents.AFTER_DAMAGE.register(CharmEvents::afterDamage);
		ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
			CharmSlotAttributes.ensureBases(newPlayer);
			CharmDataSyncer.forget(newPlayer.getUUID());
		});
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			for (ServerPlayer player : server.getPlayerList().getPlayers()) {
				refreshCharms(player);
				CharmDataSyncer.tickCharmSlots(player);
			}
		});
	}

	private static void refreshCharms(final ServerPlayer player) {
		String signature = CharmAttachments.wornEntries(player).stream()
				.map(entry -> entry.slotId() + "=" + BuiltInRegistries.ITEM.getKey(entry.stack().getItem())
						+ ":" + entry.stack().get(ModDataComponents.CHARM))
				.sorted()
				.toList()
				.toString();
		String previous = SIGNATURES.put(player.getUUID(), signature);
		if (signature.equals(previous)) {
			if (player.tickCount % 20 == 0) {
				tickCleanse(player);
			}
			return;
		}
		CharmAggregate.stripAttributes(player);
		CharmAggregate.of(CharmAttachments.wornEntries(player)).applyAttributes(player);
		if (player.tickCount % 20 == 0) {
			tickCleanse(player);
		}
	}

	private static void tickCleanse(final ServerPlayer player) {
		var rates = CharmAggregate.of(CharmAttachments.wornEntries(player)).cleanseRates();
		if (rates.isEmpty()) {
			return;
		}
		for (var entry : rates.object2DoubleEntrySet()) {
			Holder<MobEffect> effect = entry.getKey();
			MobEffectInstance instance = player.getEffect(effect);
			if (instance == null || instance.isInfiniteDuration()) {
				continue;
			}
			double rate = Math.min(1.0, entry.getDoubleValue());
			int remaining = instance.getDuration();
			int next = (int) Math.ceil(remaining * (1.0 - rate));
			player.removeEffect(effect);
			if (next > 0) {
				player.addEffect(new MobEffectInstance(effect, next, instance.getAmplifier(),
						instance.isAmbient(), instance.isVisible(), instance.showIcon()));
			}
		}
	}

	private static void afterDamage(final LivingEntity victim, final DamageSource source,
	                                final float baseDamage, final float damageTaken, final boolean blocked) {
		if (blocked || damageTaken <= 0.0F || CHARM_PROC.get()) {
			return;
		}
		if (!(source.getEntity() instanceof ServerPlayer attacker)) {
			return;
		}
		if (source.getDirectEntity() != attacker) {
			return;
		}
		CharmAggregate aggregate = CharmAggregate.of(CharmAttachments.wornEntries(attacker));
		ServerLevel level = (ServerLevel) victim.level();
		if (aggregate.igniteChance() > 0.0 && aggregate.igniteSeconds() > 0.0
				&& !victim.fireImmune()
				&& level.getRandom().nextFloat() < aggregate.igniteChance()) {
			victim.igniteForTicks((int) Math.round(aggregate.igniteSeconds() * 20.0));
		}
		if (aggregate.critChance() > 0.0 && aggregate.critBonus() > 0.0
				&& level.getRandom().nextFloat() < aggregate.critChance()) {
			float extra = damageTaken * (float) aggregate.critBonus();
			if (extra >= 0.5F && victim.isAlive()) {
				CHARM_PROC.set(true);
				try {
					victim.hurtServer(level, source, extra);
				} finally {
					CHARM_PROC.set(false);
				}
			}
		}
	}

	public static boolean tryUnlockSlot(final ServerPlayer player, final CharmSlotTypes.CharmSlotType slot) {
		if (player.containerMenu == null || player.level().isClientSide()) {
			return false;
		}
		if (CharmAttachments.isUnlocked(player, slot)) {
			return false;
		}
		if (!slot.def().canAfford(player.getInventory())) {
			return false;
		}
		slot.def().pay(player.getInventory());
		CharmAttachments.setUnlocked(player, slot.id(), true);
		CharmSlotAttributes.ensureUnlock(player, slot);
		player.containerMenu.broadcastChanges();
		player.level().playSound(null, player.blockPosition(),
				SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.7F, 1.1F);
		return true;
	}
}
