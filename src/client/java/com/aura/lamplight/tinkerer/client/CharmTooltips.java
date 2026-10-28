package com.aura.lamplight.tinkerer.client;

import com.aura.lamplight.tinkerer.CharmItem;
import com.aura.lamplight.tinkerer.TemplateItem;
import com.aura.lamplight.tinkerer.charm.AlloyCharmEffect;
import com.aura.lamplight.tinkerer.charm.AttributeCharmEffect;
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
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
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
			return Component.literal(signedPercent(attr.amount() * potency) + " " + attributeName(attr));
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
					+ " " + damageScope(resist));
		}
		if (effect instanceof CleanseCharmEffect cleanse) {
			return Component.literal(plainPercent(cleanse.ratePerPotency() * potency) + "/s Debuff Cleanse");
		}
		return Component.translatable("tooltip.lamplight.charm_other");
	}

	private static String attributeName(final AttributeCharmEffect attr) {
		Identifier id = BuiltInRegistries.ATTRIBUTE.getKey(attr.attribute().value());
		return Component.translatableWithFallback("attribute.name." + id.getPath(),
				titleCase(id.getPath())).getString();
	}

	private static String damageScope(final ResistCharmEffect resist) {
		List<String> names = new ArrayList<>();
		for (TagKey<DamageType> tag : resist.tags()) {
			String path = tag.location().getPath();
			if (path.startsWith("is_")) {
				path = path.substring(3);
			}
			names.add(titleCase(path));
		}
		for (Identifier type : resist.types()) {
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

	private static String focusPercent(final double value) {
		return String.format("%.2f", value);
	}

	private static String seconds(final double value) {
		if (Math.abs(value - Math.rint(value)) < 1e-9) {
			return String.format("%.0fs", value);
		}
		return String.format("%.1fs", value);
	}

	private static Component materialName(final CharmMaterial material) {
		Identifier itemId = material.item();
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
