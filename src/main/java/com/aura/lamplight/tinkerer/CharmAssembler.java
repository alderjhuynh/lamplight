package com.aura.lamplight.tinkerer;

import com.aura.lamplight.tinkerer.charm.CharmComponent;
import com.aura.lamplight.tinkerer.charm.CharmMaterial;
import com.aura.lamplight.tinkerer.charm.CharmMaterials;
import com.aura.lamplight.tinkerer.charm.CharmTemplate;
import com.aura.lamplight.tinkerer.charm.CharmTemplates;
import com.aura.lamplight.tinkerer.charm.ModDataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class CharmAssembler {
	private CharmAssembler() {}

	public static ItemStack assemble(final JewelingSlots crafting, final @Nullable CharmTemplate template) {
		if (template == null) {
			return ItemStack.EMPTY;
		}
		int insetCells = Math.min(template.insetCount(), JewelingTableMenu.MAX_INSETS);
		ItemStack settingStack = crafting.stackIn(JewelingTableMenu.SETTING_INDEX);
		CharmMaterial setting = CharmMaterials.byItem(settingStack);
		if (setting == null || settingStack.getCount() < setting.settingCost()) {
			return ItemStack.EMPTY;
		}
		List<CharmComponent.Inset> insets = new ArrayList<>();
		for (int i = 0; i < insetCells; i++) {
			ItemStack stack = crafting.stackIn(JewelingTableMenu.INSET_START + i);
			CharmMaterial material = CharmMaterials.byItem(stack);
			Identifier materialId = material == null ? null : CharmMaterials.idOf(material);
			if (material == null || materialId == null || stack.getCount() < material.insetCost()) {
				return ItemStack.EMPTY;
			}
			insets.add(new CharmComponent.Inset(materialId, material.insetCost()));
		}
		ItemStack charmItem = charmItemFor(template.slotType());
		if (charmItem.isEmpty()) {
			return ItemStack.EMPTY;
		}
		ItemStack result = charmItem.copyWithCount(1);
		Identifier templateId = CharmTemplates.idOf(template);
		Identifier settingId = CharmMaterials.idOf(setting);
		if (templateId == null || settingId == null) {
			return ItemStack.EMPTY;
		}
		result.set(ModDataComponents.CHARM, new CharmComponent(templateId, settingId,
				setting.settingCost(), List.copyOf(insets)));
		return result;
	}

	public static void consume(final JewelingSlots crafting, final @Nullable CharmTemplate template) {
		if (template == null) {
			return;
		}
		ItemStack settingStack = crafting.stackIn(JewelingTableMenu.SETTING_INDEX);
		CharmMaterial setting = CharmMaterials.byItem(settingStack);
		if (setting != null) {
			settingStack.shrink(setting.settingCost());
			crafting.setStackIn(JewelingTableMenu.SETTING_INDEX, settingStack);
		}
		int insetCells = Math.min(template.insetCount(), JewelingTableMenu.MAX_INSETS);
		for (int i = 0; i < insetCells; i++) {
			ItemStack stack = crafting.stackIn(JewelingTableMenu.INSET_START + i);
			CharmMaterial material = CharmMaterials.byItem(stack);
			if (material == null) {
				continue;
			}
			stack.shrink(material.insetCost());
			crafting.setStackIn(JewelingTableMenu.INSET_START + i, stack);
		}
	}

	private static ItemStack charmItemFor(final String slotType) {
		var item = ModItems.CHARMS.get(slotType);
		return item == null ? ItemStack.EMPTY : new ItemStack(item);
	}
}
