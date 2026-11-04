package com.aura.lamplight.tinkerer.client;

import com.aura.lamplight.tinkerer.CharmItem;
import com.aura.lamplight.tinkerer.TemplateItem;
import com.aura.lamplight.tinkerer.charm.AlloyCharmEffect;
import com.aura.lamplight.tinkerer.charm.AttributeCharmEffect;
import com.aura.lamplight.tinkerer.charm.CharmAggregate;
import com.aura.lamplight.tinkerer.charm.CharmAttachments;
import com.aura.lamplight.tinkerer.charm.CharmEffect;
import com.aura.lamplight.tinkerer.charm.CharmMaterial;
import com.aura.lamplight.tinkerer.charm.CharmTemplate;
import com.aura.lamplight.tinkerer.charm.CharmTemplates;
import com.aura.lamplight.tinkerer.charm.CleanseCharmEffect;
import com.aura.lamplight.tinkerer.charm.CritCharmEffect;
import com.aura.lamplight.tinkerer.charm.DodgeCharmEffect;
import com.aura.lamplight.tinkerer.charm.IgniteCharmEffect;
import com.aura.lamplight.tinkerer.charm.ResistCharmEffect;
import com.aura.lamplight.tinkerer.charm.ResolvedCharm;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Environment(EnvType.CLIENT)
@SuppressWarnings ("all")
public final class CharmTooltips {
	private CharmTooltips() {}

	public static void initialize() {
		ItemTooltipCallback.EVENT.register(CharmTooltips::append);
	}

	public static List<Component> effectLines(final ItemStack stack) {
		List<Component> lines = new ArrayList<>();
		appendCharm(stack, TooltipFlag.NORMAL, lines);
		return lines;
	}

	private static void append(final ItemStack stack, final net.minecraft.world.item.Item.TooltipContext context,
	                           final TooltipFlag flag, final List<Component> lines) {
		if (stack.getItem() instanceof CharmItem) {
			appendCharm(stack, flag, lines);
		} else if (stack.getItem() instanceof TemplateItem) {
			appendTemplate(stack, lines);
		}
	}

	private static void appendTemplate(final ItemStack stack, final List<Component> lines) {
		CharmTemplate template = CharmTemplates.byItemStack(stack);
		if (template == null) {
			lines.add(Component.translatable("tooltip.lamplight.template_unloaded").withStyle(ChatFormatting.GRAY));
			return;
		}
		lines.add(Component.empty());
		lines.add(Component.translatable("tooltip.lamplight.template_slot",
				Component.translatable("charm_slot_type.lamplight." + template.slotType())
						.withStyle(ChatFormatting.BLUE))
				.withStyle(ChatFormatting.GRAY));
		lines.add(Component.translatable("tooltip.lamplight.template_insets", template.insetCount())
				.withStyle(ChatFormatting.GRAY));
		if (template.focus() != 1.0) {
			lines.add(Component.translatable("tooltip.lamplight.template_focus",
					focusPercent(template.focus())).withStyle(ChatFormatting.GRAY));
		}
	}

	private static void appendCharm(final ItemStack stack, final TooltipFlag flag, final List<Component> lines) {
		ResolvedCharm charm = ResolvedCharm.of(stack);
		if (charm == null) {
			lines.add(Component.translatable("tooltip.lamplight.charm_unloaded").withStyle(ChatFormatting.GRAY));
			return;
		}
		List<ResolvedCharm.InsetApplication> insets = charm.insets();
		lines.add(Component.empty());
		lines.add(Component.translatable("tooltip.lamplight.charm_setting", materialName(charm.setting()))
				.withStyle(ChatFormatting.GRAY));
		addInsetsRow(insets, lines);
		lines.add(Component.empty());
		lines.add(Component.translatable("tooltip.lamplight.charm_when_worn").withStyle(ChatFormatting.GRAY));
		boolean capped = false;
		for (int i = 0; i < insets.size(); i++) {
			ResolvedCharm.InsetApplication inset = insets.get(i);
			if (inset.material() == null) {
				lines.add(Component.translatable("tooltip.lamplight.charm_unknown", literalItem(nameAt(charm, i)))
						.withStyle(ChatFormatting.DARK_GRAY));
				continue;
			}
			for (CharmEffect effect : inset.effects()) {
				if (effect instanceof AlloyCharmEffect alloy) {
					lines.add(Component.translatable("tooltip.lamplight.charm_alloy",
							plainPercent(Math.min(alloy.cap(), alloy.bonusPerPotency() * inset.potency())))
							.withStyle(ChatFormatting.BLUE));
					continue;
				}
				capped |= effect instanceof DodgeCharmEffect || effect instanceof IgniteCharmEffect
						|| effect instanceof CritCharmEffect || effect instanceof CleanseCharmEffect;
				lines.add(effectLine(effect, inset.potency()).withStyle(ChatFormatting.BLUE));
			}
		}
	}

	private static void addInsetsRow(final List<ResolvedCharm.InsetApplication> insets,
	                                 final List<Component> lines) {
		MutableComponent joined = Component.empty();
		boolean first = true;
		for (ResolvedCharm.InsetApplication inset : insets) {
			if (inset.material() == null) {
				continue;
			}
			if (!first) {
				joined.append(", ");
			}
			joined.append(materialName(inset.material()));
			first = false;
		}
		if (!first) {
			lines.add(Component.translatable("tooltip.lamplight.charm_insets", joined)
					.withStyle(ChatFormatting.GRAY));
		}
	}

	private static MutableComponent effectLine(final CharmEffect effect, final double potency) {
		if (effect instanceof AttributeCharmEffect attr) {
			double value = attr.amount() * potency;
			// Attribute charms grant flat points under ADD_VALUE; only the multiplied
			// operations are true percentages of the attribute's base/total. Report the
			// units the engine actually uses instead of always writing "%".
			String amount = attr.operation() == AttributeModifier.Operation.ADD_VALUE
					? signedPoints(value)
					: signedPercent(value);
			return Component.literal(amount + " " + attributeName(attr));
		}
		if (effect instanceof DodgeCharmEffect dodge) {
			return Component.literal(plainPercent(dodge.chance() * potency) + " Dodge Chance");
		}
		if (effect instanceof IgniteCharmEffect ignite) {
			return Component.literal(plainPercent(ignite.chance() * potency)
					+ " Ignite (" + seconds(ignite.seconds() * potency) + ")");
		}
		if (effect instanceof CritCharmEffect crit) {
			return Component.literal(plainPercent(crit.chance() * potency)
					+ " Crit Chance, +" + plainPercent(crit.bonus() * potency) + " Damage");
		}
		if (effect instanceof ResistCharmEffect resist) {
			return Component.literal("-" + plainPercent(resist.fraction() * potency)
					+ " " + damageScope(resist.types(), resist.tags()));
		}
		if (effect instanceof CleanseCharmEffect cleanse) {
			return Component.literal(plainPercent(cleanse.ratePerPotency() * potency) + "/s Debuff Cleanse");
		}
		return Component.translatable("tooltip.lamplight.charm_other");
	}

	private static String attributeName(final AttributeCharmEffect attr) {
		Identifier id = BuiltInRegistries.ATTRIBUTE.getKey(attr.attribute().value());
		return attributeName(id);
	}

	private static String attributeName(final Holder<Attribute> attribute) {
		Identifier id = BuiltInRegistries.ATTRIBUTE.getKey(attribute.value());
		return attributeName(id);
	}

	private static String attributeName(final Identifier id) {
		return Component.translatableWithFallback("attribute.name." + id.getPath(),
				titleCase(id.getPath())).getString();
	}

	private static String damageScope(final List<Identifier> types, final List<TagKey<DamageType>> tags) {
		List<String> names = new ArrayList<>();
		for (TagKey<DamageType> tag : tags) {
			String path = tag.location().getPath();
			if (path.startsWith("is_")) {
				path = path.substring(3);
			}
			names.add(titleCase(path));
		}
		for (Identifier type : types) {
			names.add(titleCase(type.getPath()));
		}
		if (names.isEmpty()) {
			return "Damage Taken";
		}
		return String.join(" / ", names) + " Damage";
	}

	private static String titleCase(final String path) {
		String[] parts = path.split("_");
		StringBuilder text = new StringBuilder();
		for (int i = 0; i < parts.length; i++) {
			if (parts[i].isEmpty()) {
				continue;
			}
			if (i > 0 && text.length() > 0) {
				text.append(' ');
			}
			text.append(Character.toUpperCase(parts[i].charAt(0)))
					.append(parts[i].substring(1).toLowerCase());
		}
		return text.toString();
	}

	private static String plainPercent(final double value) {
		return String.format("%.0f%%", value * 100.0);
	}

	private static String signedPercent(final double value) {
		return String.format("%+.0f%%", value * 100.0);
	}

	private static String signedPoints(final double value) {
		if (Math.abs(value - Math.rint(value)) < 1e-9) {
			return String.format("%+.0f", value);
		}
		return String.format("%+.2f", value);
	}

	private static String focusPercent(final double value) {
		return String.format("%.2f", value);
	}

	private static String seconds(final double value) {
		if (Math.abs(value - Math.rint(value)) < 1e-9) {
			return String.format("%.0fs", value);
		}
		return String.format("%.1fs", value);
	}

	public static List<Component> wornTotals(final Player player) {
		CharmAggregate aggregate = CharmAggregate.of(CharmAttachments.wornEntries(player));
		if (aggregate.isEmpty()) {
			return List.of();
		}
		List<Component> lines = new ArrayList<>();
		lines.add(Component.translatable("tooltip.lamplight.charm_totals")
				.withStyle(ChatFormatting.WHITE));
		for (CharmAggregate.AttributeGroup group : aggregate.attributeGroups()) {
			double amount = group.amount();
			boolean percentage = group.operation() != AttributeModifier.Operation.ADD_VALUE;
			String styled = percentage ? signedPercent(amount) : signedPoints(amount);
			lines.add(Component.literal(styled + " " + attributeName(group.attribute()))
					.withStyle(ChatFormatting.BLUE));
		}
		double dodgeRaw = aggregate.dodgeChanceUncapped();
		if (dodgeRaw > 0.0) {
			double dodgeApplied = aggregate.dodgeChance();
			lines.add(Component.literal(plainPercent(dodgeApplied) + " Dodge Chance"
					+ capNote(dodgeRaw, dodgeApplied)).withStyle(ChatFormatting.BLUE));
		}
		double igniteRawChance = aggregate.igniteChanceUncapped();
		if (igniteRawChance > 0.0 || aggregate.igniteSeconds() > 0.0) {
			lines.add(Component.literal(plainPercent(aggregate.igniteChance()) + " Ignite Chance ("
					+ seconds(aggregate.igniteSeconds()) + ")"
					+ capNote(igniteRawChance, aggregate.igniteChance())).withStyle(ChatFormatting.BLUE));
		}
		double critRawChance = aggregate.critChanceUncapped();
		double critRawBonus = aggregate.critBonusUncapped();
		if (critRawChance > 0.0 || critRawBonus > 0.0) {
			lines.add(Component.literal(plainPercent(aggregate.critChance()) + " Crit Chance, +"
					+ plainPercent(aggregate.critBonus()) + " Damage"
					+ critCapNote(critRawChance, aggregate.critChance(), critRawBonus, aggregate.critBonus()))
					.withStyle(ChatFormatting.BLUE));
		}
		for (CharmAggregate.ResistGroup group : aggregate.resistGroups()) {
			lines.add(Component.literal("-" + plainPercent(group.fraction())
					+ " " + damageScope(group.types(), group.tags())).withStyle(ChatFormatting.BLUE));
		}
		for (Map.Entry<Holder<MobEffect>, Double> entry : aggregate.cleanseRates().object2DoubleEntrySet()) {
			entry.getKey().unwrapKey().ifPresent(key -> lines.add(Component.literal(
					plainPercent(Math.min(1.0, entry.getValue())) + "/s "
					+ titleCase(key.identifier().getPath()) + " Cleanse")
					.withStyle(ChatFormatting.BLUE)));
		}
		if (!aggregate.cannotDodge().isEmpty()) {
			List<String> scopes = new ArrayList<>();
			for (TagKey<DamageType> tag : aggregate.cannotDodge()) {
				scopes.add(titleCase(tag.location().getPath()).toLowerCase());
			}
		}
		return lines;
	}

	private static String capNote(final double raw, final double applied) {
		if (applied >= raw - 1e-9) {
			return "";
		}
		return " (" + plainPercent(raw) + " before cap)";
	}

	private static String critCapNote(final double rawChance, final double appliedChance,
	                                  final double rawBonus, final double appliedBonus) {
		StringBuilder note = new StringBuilder();
		if (appliedChance < rawChance - 1e-9) {
			note.append("capped at ").append(plainPercent(appliedChance)).append(" chance");
		}
		if (appliedBonus < rawBonus - 1e-9) {
			if (note.length() > 0) {
				note.append(", ");
			}
			note.append(plainPercent(appliedBonus)).append(" bonus");
		}
		if (note.length() == 0) {
			return "";
		}
		return " (" + note + ")";
	}

	private static Component materialName(final CharmMaterial material) {		Identifier itemId = material.item();
		Item item = BuiltInRegistries.ITEM.getOptional(itemId).orElse(null);
		if (item == null) {
			return literalItem(itemId);
		}
		return new ItemStack(item).getHoverName();
	}

	private static Component literalItem(final Identifier item) {
		return Component.literal(item.toString());
	}

	private static Identifier nameAt(final ResolvedCharm charm, final int index) {
		return charm.component().insets().get(index).item();
	}
}
