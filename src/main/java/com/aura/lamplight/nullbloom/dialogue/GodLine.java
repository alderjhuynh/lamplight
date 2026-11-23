package com.aura.lamplight.nullbloom.dialogue;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record GodLine(String text, float speed, int pauseBeforeMs) {
	public static final StreamCodec<ByteBuf, GodLine> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.stringUtf8(512), GodLine::text,
		ByteBufCodecs.FLOAT, GodLine::speed,
		ByteBufCodecs.VAR_INT, GodLine::pauseBeforeMs,
		GodLine::new
	);
}
