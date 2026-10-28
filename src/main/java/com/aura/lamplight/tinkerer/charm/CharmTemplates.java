package com.aura.lamplight.tinkerer.charm;

import com.aura.lamplight.tinkerer.JewelingTableMenu;
import com.aura.lamplight.tinkerer.Tinkerer;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

@SuppressWarnings ("all")
public class CharmTemplates extends SimpleJsonResourceReloadListener<CharmTemplate>
		implements IdentifiableResourceReloadListener {

	private static final AtomicReference<Map<Identifier, CharmTemplate>> LOADED = new AtomicReference<>(Map.of());

	public CharmTemplates() {
		super(CharmTemplate.CODEC, FileToIdConverter.json("charm_template"));
	}

	@Override
	protected void apply(final Map<Identifier, CharmTemplate> data, final ResourceManager manager,
	                     final ProfilerFiller profiler) {
		Map<Identifier, CharmTemplate> usable = new java.util.HashMap<>();
		for (Map.Entry<Identifier, CharmTemplate> entry : data.entrySet()) {
			if (entry.getValue().insetCount() > JewelingTableMenu.MAX_INSETS) {
				Tinkerer.LOGGER.error(
						"Charm template '{}' wants {} inset slots but the jeweling table layout fits at most {}, so it is skipped; widen the table layout or lower inset_count",
						entry.getKey(), entry.getValue().insetCount(), JewelingTableMenu.MAX_INSETS);
				continue;
			}
			usable.put(entry.getKey(), entry.getValue());
		}
		LOADED.set(Map.copyOf(usable));
		Tinkerer.LOGGER.info("Loaded {} charm template definitions", usable.size());
	}

	@Override
	public Identifier getFabricId() {
		return Tinkerer.id("charm_templates");
	}

	public static Map<Identifier, CharmTemplate> all() {
		return LOADED.get();
	}

	public static void applySnapshot(final Map<Identifier, CharmTemplate> data) {
		LOADED.set(Map.copyOf(data));
	}

	public static CharmTemplate byId(final Identifier id) {
		return LOADED.get().get(id);
	}

	public static CharmTemplate byItemStack(final ItemStack stack) {
		if (stack.isEmpty()) {
			return null;
		}
		Identifier itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
		for (CharmTemplate template : LOADED.get().values()) {
			if (Objects.equals(template.item(), itemId)) {
				return template;
			}
		}
		return null;
	}

	public static Identifier idForItem(final Identifier itemId) {
		for (Map.Entry<Identifier, CharmTemplate> entry : LOADED.get().entrySet()) {
			if (Objects.equals(entry.getValue().item(), itemId)) {
				return entry.getKey();
			}
		}
		return null;
	}

	public static @Nullable Identifier idOf(final CharmTemplate template) {
		for (Map.Entry<Identifier, CharmTemplate> entry : LOADED.get().entrySet()) {
			if (entry.getValue() == template) {
				return entry.getKey();
			}
		}
		return null;
	}

	public record TemplateEntry(Identifier id, CharmTemplate def) {}

	public static List<TemplateEntry> ordered() {
		List<TemplateEntry> entries = new ArrayList<>();
		for (Map.Entry<Identifier, CharmTemplate> entry : LOADED.get().entrySet()) {
			entries.add(new TemplateEntry(entry.getKey(), entry.getValue()));
		}
		entries.sort(Comparator
				.comparing((TemplateEntry e) -> e.def().slotType())
				.thenComparingInt(e -> e.def().insetCount())
				.thenComparing(e -> e.def().focus(), Comparator.reverseOrder())
				.thenComparing(e -> e.id().toString()));
		return List.copyOf(entries);
	}

	public static boolean isAvailable(final Player player, final CharmTemplate template) {
		if (template.unlockedByDefault()) {
			return true;
		}
		Item item = BuiltInRegistries.ITEM.getOptional(template.item()).orElse(null);
		return item != null && player.getInventory().countItem(item) > 0;
	}

	public static void initialize() {
		ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new CharmTemplates());
	}
}
