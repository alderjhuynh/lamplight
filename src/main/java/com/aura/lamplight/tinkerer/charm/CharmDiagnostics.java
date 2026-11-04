package com.aura.lamplight.tinkerer.charm;

import com.aura.lamplight.tinkerer.ModItems;
import com.aura.lamplight.tinkerer.Tinkerer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings ("all")
public final class CharmDiagnostics {
	private CharmDiagnostics() {}

	private static final Identifier TEST_SLOT_A = Tinkerer.id("charmstats_test_a");
	private static final Identifier TEST_SLOT_B = Tinkerer.id("charmstats_test_b");

	private static final String CHARM_MODIFIER_PREFIX = "charm/";

	private int failures;

	public static void initialize() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(Commands.literal("charmstats")
					.requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
					.executes(context -> audit(context.getSource(), context.getSource().getPlayerOrException()))
					.then(Commands.literal("test")
							.executes(context -> new CharmDiagnostics().test(
									context.getSource(), context.getSource().getPlayerOrException()))));
		});
	}

	private static int audit(final CommandSourceStack source, final Player player) {
		CharmAggregate aggregate = CharmAggregate.of(CharmAttachments.wornEntries(player));
		line(source, "== charmstats: " + player.getName().getString(), ChatFormatting.GOLD);
		for (CharmAggregate.Entry entry : CharmAttachments.wornEntries(player)) {
			ResolvedCharm charm = ResolvedCharm.of(entry.stack());
			if (charm == null) {
				line(source, "  " + entry.slotId() + " -> unresolved charm", ChatFormatting.RED);
				continue;
			}
			StringBuilder text = new StringBuilder("  " + entry.slotId() + " "
					+ idOfTemplate(charm.template()) + ", setting "
					+ idOfMaterial(charm.setting()) + ", potency per inset "
					+ trim(charm.setting().settingMultiplier() * charm.template().focus()));
			for (ResolvedCharm.InsetApplication inset : charm.insets()) {
				text.append(inset.material() == null ? " <unknown inset>" : " "
						+ idOfMaterial(inset.material()));
			}
			line(source, text.toString(), ChatFormatting.GRAY);
		}
		line(source, "- dodge: " + percent(aggregate.dodgeChance()) + " applied (raw "
				+ percent(aggregate.dodgeChanceUncapped()) + ", cap "
				+ percent(aggregate.dodgeCap()) + ")", ChatFormatting.BLUE);
		line(source, "- ignite: " + percent(aggregate.igniteChance()) + " chance for "
				+ trim(aggregate.igniteSeconds()) + "s (chance raw "
				+ percent(aggregate.igniteChanceUncapped()) + ")", ChatFormatting.BLUE);
		line(source, "- crit: " + percent(aggregate.critChance()) + " chance, "
				+ percent(aggregate.critBonus()) + " bonus damage (raw "
				+ percent(aggregate.critChanceUncapped()) + " / "
				+ percent(aggregate.critBonusUncapped()) + ")", ChatFormatting.BLUE);
		line(source, "- cannot dodge: " + aggregate.cannotDodge(), ChatFormatting.BLUE);
		for (Map.Entry<Holder<MobEffect>, Double> entry : aggregate.cleanseRates().object2DoubleEntrySet()) {
			entry.getKey().unwrapKey().ifPresent(key -> line(source, "- cleanse " + key
					+ " at " + percent(Math.min(1.0, entry.getValue())) + "/s", ChatFormatting.BLUE));
		}
		liveCharmModifiers(source, player);
		return 1;
	}

	private static @Nullable Identifier idOfTemplate(final CharmTemplate template) {
		return CharmTemplates.idOf(template);
	}

	private static @Nullable Identifier idOfMaterial(final CharmMaterial material) {
		return CharmMaterials.idOf(material);
	}

	private static void liveCharmModifiers(final CommandSourceStack source, final Player player) {
		for (var entry : BuiltInRegistries.ATTRIBUTE.entrySet()) {
			Holder<Attribute> holder = BuiltInRegistries.ATTRIBUTE.wrapAsHolder(entry.getValue());
			AttributeInstance instance = player.getAttribute(holder);
			if (instance == null) {
				continue;
			}
			List<AttributeModifier> charmModifiers = instance.getModifiers().stream()
					.filter(modifier -> modifier.id().getNamespace().equals(Tinkerer.MOD_ID)
							&& modifier.id().getPath().startsWith(CHARM_MODIFIER_PREFIX))
					.toList();
			if (charmModifiers.isEmpty()) {
				continue;
			}
			line(source, "- live " + entry.getKey().identifier() + ": value " + trim(instance.getValue()), ChatFormatting.AQUA);
			for (AttributeModifier modifier : charmModifiers) {
				line(source, "    " + modifier.id() + " = " + trim(modifier.amount()), ChatFormatting.AQUA);
			}
		}
	}

	private int test(final CommandSourceStack source, final Player player) {
		source.sendSystemMessage(Component.literal("== charmstats self-test (worn charms are restored"
				+ " afterwards)").withStyle(ChatFormatting.GOLD));
		this.failures = 0;
		Map<String, ItemStack> worn = new HashMap<>(CharmAttachments.wornCharms(player));
		try {
			CharmTemplate band = CharmTemplates.byId(Tinkerer.id("band"));
			if (band == null) {
				throw new IllegalStateException("the band charm template is not loaded");
			}
			testAttributes(source, player, band);
			testDodgeCap(source);
			testDodgeOrderIndependence(source);
			testCannotDodgeUnion(source);
			testResistClamp(source, player);
			testCleanseClamp(source, player);
			source.sendSystemMessage(Component.literal(failures == 0 ? "ALL PASSED"
					: failures + " CHECK(S) FAILED").withStyle(failures == 0
					? ChatFormatting.GREEN : ChatFormatting.RED));
		} catch (IllegalStateException error) {
			failures++;
			source.sendSystemMessage(Component.literal("Test aborted: " + error.getMessage())
					.withStyle(ChatFormatting.RED));
		} finally {
			restoreWorn(player, worn);
		}
		return failures == 0 ? 1 : 0;
	}

	private void testAttributes(final CommandSourceStack source, final Player player,
	                            final CharmTemplate band) {
		CharmMaterial iron = materialByPath("iron");
		if (iron == null) {
			throw new IllegalStateException("the iron charm material is not loaded");
		}
		try {
			equipTestCharm(player, TEST_SLOT_A, charmStack(band, iron));
			equipTestCharm(player, TEST_SLOT_B, charmStack(band, iron));
			reapply(player);
			Holder<Attribute> armor = attributeByPath("armor");
			if (armor == null) {
				throw new IllegalStateException("the vanilla armor attribute is missing");
			}
			AttributeInstance instance = player.getAttribute(armor);
			double live = instance == null ? 0.0 : instance.getModifiers().stream()
					.filter(modifier -> modifier.id().getNamespace().equals(Tinkerer.MOD_ID))
					.filter(modifier -> modifier.id().getPath().equals("charm/armor/add_value"))
					.mapToDouble(AttributeModifier::amount)
					.sum();
			check(source, "two worn iron charms composite +2.0 flat Armor (attribute charms are"
				+ " flat points; percent ops are separate)", 2.0, live);
		} catch (IllegalStateException error) {
			throw error;
		} finally {
			clearTestSlots(player);
		}
	}

	private void testDodgeCap(final CommandSourceStack source) {
		DodgeCharmEffect effect = new DodgeCharmEffect(0.34, 0.5, List.of());
		CharmAggregate aggregate = new CharmAggregate();
		for (int i = 0; i < 5; i++) {
			aggregate.mergeInset(effect, 1.0);
		}
		check(source, "raw dodge getter exposes the 170% sum", 1.7, aggregate.dodgeChanceUncapped());
		check(source, "composite dodge clamps to the 50% charm cap", 0.5, aggregate.dodgeChance());
	}

	private void testDodgeOrderIndependence(final CommandSourceStack source) {
		DodgeCharmEffect lowCap = new DodgeCharmEffect(0.3, 0.3, List.of());
		DodgeCharmEffect highCap = new DodgeCharmEffect(0.3, 0.9, List.of());
		CharmAggregate lowFirst = new CharmAggregate();
		lowFirst.mergeInset(lowCap, 1.0);
		lowFirst.mergeInset(highCap, 1.0);
		CharmAggregate highFirst = new CharmAggregate();
		highFirst.mergeInset(highCap, 1.0);
		highFirst.mergeInset(lowCap, 1.0);
		check(source, "merge order never pushes dodge past any charm's cap",
				lowFirst.dodgeChance(), highFirst.dodgeChance());
		check(source, "both orders clamp to the 30% cap", 0.3, lowFirst.dodgeChance());
	}

	private void testCannotDodgeUnion(final CommandSourceStack source) {
		TagKey<DamageType> fall = TagKey.create(Registries.DAMAGE_TYPE,
				DamageTypeTags.IS_FALL.location());
		TagKey<DamageType> drowning = TagKey.create(Registries.DAMAGE_TYPE,
				DamageTypeTags.IS_DROWNING.location());
		CharmAggregate aggregate = new CharmAggregate();
		aggregate.mergeInset(new DodgeCharmEffect(0.1, 0.5, List.of(fall)), 1.0);
		aggregate.mergeInset(new DodgeCharmEffect(0.1, 0.5, List.of(drowning)), 1.0);
		int held = (aggregate.cannotDodge().contains(fall) ? 1 : 0)
				+ (aggregate.cannotDodge().contains(drowning) ? 1 : 0);
		check(source, "cannot-dodge lists union across charms instead of overwriting",
				2, held);
	}

	private void testResistClamp(final CommandSourceStack source, final Player player) {
		TagKey<DamageType> fire = TagKey.create(Registries.DAMAGE_TYPE,
				DamageTypeTags.IS_FIRE.location());
		ResistCharmEffect effect = new ResistCharmEffect(List.of(), List.of(fire), 0.3, 0.5);
		Holder<DamageType> damageType = player.level().registryAccess()
				.lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(DamageTypes.IN_FIRE);
		CharmAggregate forward = new CharmAggregate();
		forward.mergeInset(effect, 1.0);
		forward.mergeInset(effect, 1.0);
		CharmAggregate backward = new CharmAggregate();
		backward.mergeInset(effect, 1.0);
		backward.mergeInset(effect, 1.0);
		check(source, "60% summed fire resist clamps to the 50% cap in both merge orders",
				0.5, forward.resist(damageType));
		check(source, "resist is order-independent", forward.resist(damageType),
				backward.resist(damageType));
	}

	private void testCleanseClamp(final CommandSourceStack source, final Player player) {
		Holder<MobEffect> poison = effectByPath(player, "poison");
		if (poison == null) {
			throw new IllegalStateException("the vanilla poison mob effect is missing");
		}
		CleanseCharmEffect effect = new CleanseCharmEffect(List.of(poison), 0.4, 0.4);
		CharmAggregate aggregate = new CharmAggregate();
		for (int i = 0; i < 3; i++) {
			aggregate.mergeInset(effect, 1.0);
		}
		check(source, "three 40%/s cleanse rates clamp to the 40% cap",
				0.4, aggregate.cleanseRates().getOrDefault(poison, 0.0));
	}

	private static @Nullable Holder<MobEffect> effectByPath(final Player player, final String path) {
		for (var entry : BuiltInRegistries.MOB_EFFECT.entrySet()) {
			Identifier effectId = entry.getKey().identifier();
			if (effectId.getNamespace().equals("minecraft") && effectId.getPath().equals(path)) {
				return BuiltInRegistries.MOB_EFFECT.wrapAsHolder(entry.getValue());
			}
		}
		return null;
	}

	private static @Nullable CharmMaterial materialByPath(final String path) {
		for (Map.Entry<Identifier, CharmMaterial> entry : CharmMaterials.all().entrySet()) {
			if (entry.getKey().getPath().equals(path)) {
				return entry.getValue();
			}
		}
		return null;
	}

	private static @Nullable Holder<Attribute> attributeByPath(final String path) {
		for (var entry : BuiltInRegistries.ATTRIBUTE.entrySet()) {
			Identifier attributeId = entry.getKey().identifier();
			if (attributeId.getNamespace().equals("minecraft") && attributeId.getPath().equals(path)) {
				return BuiltInRegistries.ATTRIBUTE.wrapAsHolder(entry.getValue());
			}
		}
		return null;
	}

	private ItemStack charmStack(final CharmTemplate template, final CharmMaterial setting,
	                             final CharmMaterial... insets) {
		Identifier templateId = CharmTemplates.idOf(template);
		Identifier settingId = CharmMaterials.idOf(setting);
		Item item = ModItems.CHARMS.get("ring");
		if (templateId == null || settingId == null || item == null) {
			throw new IllegalStateException("ring charm item, template id or setting id is missing");
		}
		List<CharmComponent.Inset> insetList = new ArrayList<>();
		for (CharmMaterial inset : insets) {
			Identifier insetId = CharmMaterials.idOf(inset);
			if (insetId == null) {
				throw new IllegalStateException("charm inset material lacks an id");
			}
			insetList.add(new CharmComponent.Inset(insetId, Math.max(1, inset.insetCost())));
		}
		ItemStack stack = new ItemStack(item);
		stack.set(ModDataComponents.CHARM, new CharmComponent(templateId, settingId,
				setting.settingCost(), List.copyOf(insetList)));
		return stack;
	}

	private static void equipTestCharm(final Player player, final Identifier slotId, final ItemStack stack) {
		CharmAttachments.setCharm(player, slotId, stack);
	}

	private static void clearTestSlots(final Player player) {
		CharmAttachments.setCharm(player, TEST_SLOT_A, null);
		CharmAttachments.setCharm(player, TEST_SLOT_B, null);
	}

	private static void reapply(final Player player) {
		CharmAggregate.stripAttributes(player);
		CharmAggregate.of(CharmAttachments.wornEntries(player)).applyAttributes(player);
	}

	private void restoreWorn(final Player player, final Map<String, ItemStack> worn) {
		clearTestSlots(player);
		for (Map.Entry<String, ItemStack> entry : worn.entrySet()) {
			Identifier slotId = Identifier.tryParse(entry.getKey());
			if (slotId != null) {
				CharmAttachments.setCharm(player, slotId, entry.getValue());
			}
		}
		reapply(player);
	}

	private void check(final CommandSourceStack source, final String label, final double expected,
	                   final double actual) {
		boolean pass = Math.abs(expected - actual) < 1e-6;
		failures += pass ? 0 : 1;
		source.sendSystemMessage(Component.literal((pass ? "[PASS] " : "[FAIL] ") + label
				+ (pass ? "" : " (expected " + trim(expected) + ", got " + trim(actual) + ")"))
				.withStyle(pass ? ChatFormatting.GREEN : ChatFormatting.RED));
	}

	private void check(final CommandSourceStack source, final String label, final Object expected,
	                   final Object actual) {
		boolean pass = expected.equals(actual);
		failures += pass ? 0 : 1;
		source.sendSystemMessage(Component.literal((pass ? "[PASS] " : "[FAIL] ") + label
				+ (pass ? "" : " (expected " + expected + ", got " + actual + ")"))
				.withStyle(pass ? ChatFormatting.GREEN : ChatFormatting.RED));
	}

	private static void line(final CommandSourceStack source, final String text, final ChatFormatting color) {
		source.sendSystemMessage(Component.literal(text).withStyle(color));
	}

	private static String percent(final double value) {
		return String.format("%.0f%%", value * 100.0);
	}

	private static String trim(final double value) {
		if (Math.abs(value - Math.rint(value)) < 1e-9) {
			return String.format("%.0f", value);
		}
		return String.format("%.2f", value);
	}
}
