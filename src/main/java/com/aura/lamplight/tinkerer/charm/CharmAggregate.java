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

	private static final class Clamped {
		private double sum;
		private double cap = 1.0;

		private void add(final double value, final double limit) {
			if (limit < this.cap) {
				this.cap = limit;
			}
			this.sum += value;
		}

		private double value() {
			return Math.min(this.cap, this.sum);
		}
	}

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

	private static final class CleanseRate {
		private final Clamped rate = new Clamped();
	}

	private final Object2ObjectMap<Holder<Attribute>, Object2DoubleMap<AttributeModifier.Operation>> attributes = new Object2ObjectArrayMap<>();
	private final Clamped dodgeChance = new Clamped();
	private final List<TagKey<DamageType>> cannotDodge = new ArrayList<>();
	private final Clamped igniteChance = new Clamped();
	private final Clamped igniteSeconds = new Clamped();
	private final Clamped critChance = new Clamped();
	private final Clamped critBonus = new Clamped();
	private final List<ResistBucket> resistBuckets = new ArrayList<>();
	private final Object2ObjectMap<Holder<MobEffect>, CleanseRate> cleanseRates = new Object2ObjectArrayMap<>();

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
			this.dodgeChance.add(dodge.chance() * potency, dodge.cap());
			for (TagKey<DamageType> tag : dodge.cannotDodge()) {
				if (!this.cannotDodge.contains(tag)) {
					this.cannotDodge.add(tag);
				}
			}
		} else if (effect instanceof IgniteCharmEffect ignite) {
			this.igniteChance.add(ignite.chance() * potency, ignite.capChance());
			this.igniteSeconds.add(ignite.seconds() * potency, ignite.capSeconds());
		} else if (effect instanceof CritCharmEffect crit) {
			this.critChance.add(crit.chance() * potency, crit.capChance());
			this.critBonus.add(crit.bonus() * potency, crit.capBonus());
		} else if (effect instanceof ResistCharmEffect resist) {
			mergeResistance(resist, potency);
		} else if (effect instanceof CleanseCharmEffect cleanse) {
			for (Holder<MobEffect> matched : cleanse.effects()) {
				CleanseRate bucket = this.cleanseRates.computeIfAbsent(matched, key -> new CleanseRate());
				bucket.rate.add(cleanse.ratePerPotency() * potency, cleanse.cap());
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
		return this.dodgeChance.value();
	}

	public double dodgeChanceUncapped() {
		return this.dodgeChance.sum;
	}

	public double dodgeCap() {
		return this.dodgeChance.cap;
	}

	public List<TagKey<DamageType>> cannotDodge() {
		return this.cannotDodge;
	}

	public double igniteChance() {
		return this.igniteChance.value();
	}

	public double igniteChanceUncapped() {
		return this.igniteChance.sum;
	}

	public double igniteSeconds() {
		return this.igniteSeconds.value();
	}

	public double critChance() {
		return this.critChance.value();
	}

	public double critChanceUncapped() {
		return this.critChance.sum;
	}

	public double critBonus() {
		return this.critBonus.value();
	}

	public double critBonusUncapped() {
		return this.critBonus.sum;
	}

	public Object2DoubleMap<Holder<MobEffect>> cleanseRates() {
		Object2DoubleMap<Holder<MobEffect>> clamped = new Object2DoubleArrayMap<>();
		for (Object2ObjectMap.Entry<Holder<MobEffect>, CleanseRate> entry : this.cleanseRates.object2ObjectEntrySet()) {
			clamped.put(entry.getKey(), entry.getValue().rate.value());
		}
		return clamped;
	}

	public double resist(final Holder<DamageType> type) {
		double total = 0.0;
		for (ResistBucket bucket : this.resistBuckets) {
			total += bucket.reduce(type);
		}
		return Math.min(total, 1.0);
	}

	public record ResistGroup(List<Identifier> types, List<TagKey<DamageType>> tags, double fraction) {}

	public record AttributeGroup(Holder<Attribute> attribute, AttributeModifier.Operation operation, double amount) {}

	public List<AttributeGroup> attributeGroups() {
		List<AttributeGroup> groups = new ArrayList<>();
		for (Map.Entry<Holder<Attribute>, Object2DoubleMap<AttributeModifier.Operation>> entry : this.attributes.entrySet()) {
			for (Object2DoubleMap.Entry<AttributeModifier.Operation> mod : entry.getValue().object2DoubleEntrySet()) {
				if (mod.getDoubleValue() != 0.0) {
					groups.add(new AttributeGroup(entry.getKey(), mod.getKey(), mod.getDoubleValue()));
				}
			}
		}
		return groups;
	}

	public List<ResistGroup> resistGroups() {
		List<ResistGroup> groups = new ArrayList<>();
		for (ResistBucket bucket : this.resistBuckets) {
			ResistCharmEffect first = bucket.effects.get(0);
			groups.add(new ResistGroup(first.types(), first.tags(), Math.min(bucket.cap, bucket.fraction)));
		}
		return groups;
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
				&& this.dodgeChance.sum == 0.0
				&& this.igniteChance.sum == 0.0
				&& this.igniteSeconds.sum == 0.0
				&& this.critChance.sum == 0.0
				&& this.resistBuckets.isEmpty()
				&& this.cleanseRates.isEmpty();
	}
}
