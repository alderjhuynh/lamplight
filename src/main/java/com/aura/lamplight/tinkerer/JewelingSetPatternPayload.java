package com.aura.lamplight.tinkerer;

import com.aura.lamplight.tinkerer.charm.CharmTemplate;
import com.aura.lamplight.tinkerer.charm.CharmTemplates;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

public record JewelingSetPatternPayload(int containerId, Optional<Identifier> templateId)
		implements CustomPacketPayload {

	public static final JewelingSetPatternPayload.Type<JewelingSetPatternPayload> TYPE =
			new Type<>(Tinkerer.id("set_jeweling_pattern"));

	public static final StreamCodec<RegistryFriendlyByteBuf, JewelingSetPatternPayload> CODEC =
			StreamCodec.composite(
					ByteBufCodecs.VAR_INT, JewelingSetPatternPayload::containerId,
					ByteBufCodecs.optional(Identifier.STREAM_CODEC), JewelingSetPatternPayload::templateId,
					JewelingSetPatternPayload::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public static void initialize() {
		PayloadTypeRegistry.serverboundPlay().register(JewelingSetPatternPayload.TYPE, CODEC);
		ServerPlayNetworking.registerGlobalReceiver(JewelingSetPatternPayload.TYPE, (payload, context) -> {
			context.server().execute(() -> {
				if (context.player().containerMenu instanceof JewelingTableMenu menu
						&& menu.containerId == payload.containerId()) {
					Identifier templateId = payload.templateId().orElse(null);
					if (templateId == null) {
						menu.setPattern(null);
					} else {
						CharmTemplate template = CharmTemplates.byId(templateId);
						if (template != null && menu.maySelect(template)) {
							menu.setPattern(templateId);
						}
					}
					ServerPlayer player = context.player();
					menu.sendAllDataToRemote();
					ServerPlayNetworking.send(player, new JewelingPatternAppliedPayload(
							menu.containerId, Optional.ofNullable(menu.patternId())));
				}
			});
		});
	}
}
