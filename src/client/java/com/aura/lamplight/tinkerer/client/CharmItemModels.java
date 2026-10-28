package com.aura.lamplight.tinkerer.client;

import com.aura.lamplight.tinkerer.Tinkerer;
import com.aura.lamplight.tinkerer.charm.CharmComponent;
import com.aura.lamplight.tinkerer.charm.ModDataComponents;
import net.fabricmc.fabric.api.client.model.loading.v1.ExtraModelKey;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelModifier;
import net.fabricmc.fabric.api.client.model.loading.v1.UnbakedExtraModel;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.CompositeModel;
import net.minecraft.client.renderer.item.CuboidItemModelWrapper;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class CharmItemModels implements ModelLoadingPlugin {

	private static final Map<Identifier, List<String>> SHAPES_BY_ITEM = Map.of(
		Tinkerer.id("charm_ring"), List.of("band", "signet"),
		Tinkerer.id("charm_necklace"), List.of("locket")
	);

	private static final List<String> MATERIALS = List.of(
		"amethyst", "coal", "copper", "diamond", "emerald", "gold", "iron",
		"lapis", "netherite", "quartz", "redstone", "resin", "rose_gold",
		"ruby", "sapphire"
	);

	private static final ExtraModelKey<Boolean> LAYER_MODELS =
		ExtraModelKey.create(() -> "lamplight charm layer models");

	@Override
	public void initialize(final ModelLoadingPlugin.Context context) {
		context.addModel(LAYER_MODELS, new UnbakedExtraModel<>() {
			@Override
			public void resolveDependencies(final ResolvableModel.Resolver resolver) {
				for (String shape : SHAPES_BY_ITEM.values().stream().flatMap(List::stream).distinct().toList()) {
					for (String material : MATERIALS) {
						resolver.markDependency(settingLayerId(shape, material));
						resolver.markDependency(insetLayerId(shape, material));
					}
				}
			}

			@Override
			public Boolean bake(final ModelBaker baker) {
				return Boolean.TRUE;
			}
		});
		context.modifyItemModelAfterBake().register(CharmItemModels::replaceModel);
	}

	private static ItemModel replaceModel(final ItemModel model, final ModelModifier.AfterBakeItem.Context context) {
		List<String> shapes = SHAPES_BY_ITEM.get(context.itemId());
		if (shapes == null) {
			return model;
		}
		Map<CompositeKey, ItemModel> pairs = new HashMap<>();
		for (String shape : shapes) {
			Map<String, ItemModel> settings = new HashMap<>();
			Map<String, ItemModel> insetsMap = new HashMap<>();
			for (String material : MATERIALS) {
				settings.put(material, bakingLayer(context, settingLayerId(shape, material)));
				insetsMap.put(material, bakingLayer(context, insetLayerId(shape, material)));
			}
			for (String setting : MATERIALS) {
				ItemModel settingModel = settings.get(setting);
				for (String inset : MATERIALS) {
					pairs.put(new CompositeKey(shape, setting, inset),
							new CompositeModel(List.of(settingModel, insetsMap.get(inset))));
				}
			}
		}
		return new CharmMaterialDisplay(model, Map.copyOf(pairs));
	}

	private static ItemModel bakingLayer(final ModelModifier.AfterBakeItem.Context context, final Identifier layer) {
		return new CuboidItemModelWrapper.Unbaked(layer, Optional.empty(), List.of())
				.bake(context.bakingContext(), context.transformation());
	}

	private static Identifier settingLayerId(final String shape, final String material) {
		return Tinkerer.id("item/charm/" + shape + "_" + material);
	}

	private static Identifier insetLayerId(final String shape, final String material) {
		return Tinkerer.id("item/charm/" + shape + "_inset_" + material);
	}

	private record CompositeKey(String shape, String setting, String inset) {}

	private record CharmMaterialDisplay(ItemModel fallback,
	                                    Map<CompositeKey, ItemModel> pairs) implements ItemModel {

		@Override
		public void update(final ItemStackRenderState state, final ItemStack stack, final ItemModelResolver resolver,
		                   final ItemDisplayContext displayContext, final ClientLevel level, final ItemOwner owner,
		                   final int seed) {
			select(stack).update(state, stack, resolver, displayContext, level, owner, seed);
		}

		private ItemModel select(final ItemStack stack) {
			CharmComponent charm = stack.get(ModDataComponents.CHARM);
			if (charm == null || charm.insets().size() != 1) {
				return this.fallback;
			}
			CompositeKey key = new CompositeKey(
					charm.templateId().getPath(),
					charm.settingItem().getPath(),
					charm.insets().get(0).item().getPath());
			return this.pairs.getOrDefault(key, this.fallback);
		}
	}
}
