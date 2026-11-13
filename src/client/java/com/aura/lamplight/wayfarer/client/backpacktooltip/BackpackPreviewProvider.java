package com.aura.lamplight.wayfarer.client.backpacktooltip;

import com.aura.lamplight.wayfarer.BackpackMenu;
import com.aura.lamplight.wayfarer.ModDataComponents;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;

import java.util.List;

public class BackpackPreviewProvider extends ContentsPreviewProvider {
	public BackpackPreviewProvider() {
		super(BackpackMenu.SLOTS, false, 3, 3);
	}

	@Override
	public boolean showTooltipHints(PreviewContext context) {
		return this.shouldDisplay(context);
	}

	@Override
	public List<ItemStack> getInventory(PreviewContext context) {
		var registries = context.registryLookup();
		var contents = context.stack().get(ModDataComponents.BACKPACK_CONTENTS);
		var invMaxSize = this.getInventoryMaxSize(context);
		var inv = NonNullList.withSize(invMaxSize, ItemStack.EMPTY);

		if (registries != null && contents != null)
			contents.copyInto(inv);

		return inv;
	}

	@Override
	public ColorKey getWindowColorKey(PreviewContext context) {
		return ColorKey.LEATHER;
	}

	@Override
	public int getWindowColor(PreviewContext context) {
		DyedItemColor dyed = context.stack().get(DataComponents.DYED_COLOR);

		if (dyed != null) {
			return 0xFF000000 | ColorKey.dimToMinBrightness(dyed.rgb());
		}
		return 0xFF000000 | ColorKey.LEATHER.rgb();
	}
}
