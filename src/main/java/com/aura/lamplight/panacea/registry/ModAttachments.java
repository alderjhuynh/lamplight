package com.aura.lamplight.panacea.registry;

import com.aura.lamplight.panacea.Panacea;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.world.entity.LivingEntity;

public class ModAttachments {
	public static final AttachmentType<Float> UNDYING_DAMAGE = AttachmentRegistry.<Float>builder()
			.persistent(Codec.FLOAT)
			.buildAndRegister(Panacea.id("undying_damage"));

	public static float cachedUndyingDamage(LivingEntity entity) {
		Float cached = entity.getAttached(UNDYING_DAMAGE);
		return cached == null ? 0.0F : cached;
	}

	public static void setCachedUndyingDamage(LivingEntity entity, float damage) {
		entity.setAttached(UNDYING_DAMAGE, damage);
	}

	public static void register() {
		Object ignored = UNDYING_DAMAGE;
	}
}
