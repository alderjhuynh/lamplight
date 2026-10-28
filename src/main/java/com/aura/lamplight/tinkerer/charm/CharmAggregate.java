package com.aura.lamplight.tinkerer.charm;

import com.aura.lamplight.Lamplight;
import it.unimi.dsi.fastutil.objects.Object2DoubleArrayMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import net.minecraft.core.registries.BuiltInRegistries;

public final class CharmAggregate {

	public record Entry(Identifier slotId, ItemStack stack) {}

	private static final class ResistBucket {
		private double fraction;
		private double cap = 1.0;
		private final List<ResistCharmEffect> effects = new ArrayList<>();

		private void add(ResistCharmEffect resist, double potency) {
			this.fraction += resist.fraction() * potency;
			this.cap = Math.min(this.cap, resist.cap());
			this.effects.add(resist);
		}

		private boolean matches(Holder<DamageType> type) {
			return this.effects.stream().anyMatch(effect -> effect.matches(type));
		}

		private double reduce(Holder<DamageType> type) {
			return matches(type) ? Math.min(this.cap, this.fraction) : 0.0;
		}
	}

	private final Object2ObjectMap<Holder<Attribute>, Object2DoubleMap<AttributeModifier.Operation>> attributes = new Object2ObjectArrayMap<>();
	private double dodgeChance;
	private List<TagKey<DamageType>> cannotDodge = List.of();
	private double igniteChance;
	private double igniteSeconds;
	private double critChance;
	private double critBonus;
	private final List<ResistBucket> resistBuckets = new ArrayList<>();
	private final Object2DoubleMap< Holder<MobEffect>> cleanseRates = new Object2DoubleArrayMap<>();

	public static CharmAggregate of(final List<Entry> worn) {
		CharmAggregate aggregate = new CharmAggregate();
		for (Entry entry : worn) {
			ResolvedCharm charm = ResolvedCharm.of(entry.stack());
			if (charm != null) {
				charm.mergeInto(aggregate);
			}
		}
		return aggregate;
	}

	public void mergeInset(final CharmEffect effect, final double potency) {
		if (effect instanceof AlloyCharmEffect) {
			return;
		}
		if (effect instanceof AttributeCharmEffect attr) {
			this.attributes.computeIfAbsent(attr.attribute(), k -> new Object2DoubleArrayMap<>())
					.merge(attr.operation(), attr.amount() * potency, Double::sum);
		} else if (effect instanceof DodgeCharmEffect dodge) {
			this.cannotDodge = dodge.cannotDodge();
			this.dodgeChance = Math.min(dodge.cap(), this.dodgeChance + dodge.chance() * potency);
		} else if (effect instanceof IgniteCharmEffect ignite) {
			this.igniteChance = Math.min(ignite.capChance(), this.igniteChance + ignite.chance() * potency);
			this.igniteSeconds = Math.min(ignite.capSeconds(), this.igniteSeconds + ignite.seconds() * potency);
		} else if (effect instanceof CritCharmEffect crit) {
			this.critChance = Math.min(crit.capChance(), this.critChance + crit.chance() * potency);
			this.critBonus = Math.min(crit.capBonus(), this.critBonus + crit.bonus() * potency);
		} else if (effect instanceof ResistCharmEffect resist) {
			mergeResistance(resist, potency);
		} else if (effect instanceof CleanseCharmEffect cleanse) {
			for (Holder<MobEffect> matched : cleanse.effects()) {
				this.cleanseRates.mergeDouble(matched, Math.min(cleanse.cap(), cleanse.ratePerPotency() * potency), Double::sum);
			}
		}
	}

	private void mergeResistance(final ResistCharmEffect resist, final double potency) {
		for (ResistBucket bucket : this.resistBuckets) {
			ResistCharmEffect first = bucket.effects.get(0);
			if (first.types().equals(resist.types()) && first.tags().equals(resist.tags())) {
				bucket.add(resist, potency);
				return;
			}
		}
		ResistBucket fresh = new ResistBucket();
		fresh.add(resist, potency);
		this.resistBuckets.add(fresh);
	}

	public double dodgeChance() {
		return this.dodgeChance;
	}

	public List<TagKey<DamageType>> cannotDodge() {
		return this.cannotDodge;
	}

	public double igniteChance() {
		return this.igniteChance;
	}

	public double igniteSeconds() {
		return this.igniteSeconds;
	}

	public double critChance() {
		return this.critChance;
	}

	public double critBonus() {
		return this.critBonus;
	}

	public Object2DoubleMap<Holder<MobEffect>> cleanseRates() {
		return this.cleanseRates;
	}

	public double resist(final Holder<DamageType> type) {
		double total = 0.0;
		for (ResistBucket bucket : this.resistBuckets) {
			total += bucket.reduce(type);
		}
		return Math.min(total, 1.0);
	}

	public static Identifier modifierId(final Holder<Attribute> attribute, final AttributeModifier.Operation operation) {
		String path = attribute.unwrapKey().map(key -> key.identifier().getPath()).orElse("unknown");
		return Lamplight.id("charm/" + path + "/" + operation.name().toLowerCase(Locale.ROOT));
	}

	public void applyAttributes(final LivingEntity entity) {
		if (this.attributes.isEmpty()) {
			return;
		}
		for (Map.Entry<Holder<Attribute>, Object2DoubleMap<AttributeModifier.Operation>> entry : this.attributes.entrySet()) {
			AttributeInstance instance = entity.getAttribute(entry.getKey());
			if (instance == null) {
				continue;
			}
			for (Object2DoubleMap.Entry<AttributeModifier.Operation> mod : entry.getValue().object2DoubleEntrySet()) {
				if (mod.getDoubleValue() == 0.0) {
					continue;
				}
				Identifier id = modifierId(entry.getKey(), mod.getKey());
				AttributeModifier modifier = new AttributeModifier(id, mod.getDoubleValue(), mod.getKey());
				boolean fresh = instance.getModifiers().stream()
						.noneMatch(existing -> existing.id().equals(id) && existing.amount() == modifier.amount());
				if (fresh) {
					instance.addOrUpdateTransientModifier(modifier);
				}
			}
		}
	}

	public static void stripAttributes(final LivingEntity entity) {
		for (var entry : BuiltInRegistries.ATTRIBUTE.entrySet()) {
			Holder<Attribute> holder = BuiltInRegistries.ATTRIBUTE.wrapAsHolder(entry.getValue());
			AttributeInstance instance = entity.getAttribute(holder);
			if (instance == null) {
				continue;
			}
			for (AttributeModifier modifier : Set.copyOf(instance.getModifiers())) {
				if (modifier.id().getNamespace().equals(Lamplight.MOD_ID) && modifier.id().getPath().startsWith("charm/")) {
					instance.removeModifier(modifier.id());
				}
			}
		}
	}

	public boolean isEmpty() {
		return this.attributes.isEmpty()
				&& this.dodgeChance == 0.0
				&& this.igniteChance == 0.0
				&& this.critChance == 0.0
				&& this.resistBuckets.isEmpty()
				&& this.cleanseRates.isEmpty();
	}
}
