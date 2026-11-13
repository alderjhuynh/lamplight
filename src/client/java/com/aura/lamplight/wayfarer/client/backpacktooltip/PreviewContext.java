package com.aura.lamplight.wayfarer.client.backpacktooltip;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import org.jspecify.annotations.Nullable;

public record PreviewContext(ItemStack stack, @Nullable Player owner, PreviewConfiguration config,
                             HolderLookup.@Nullable Provider registryLookup) {

	public static Builder builder(ItemStack stack) {
		return new Builder(stack);
	}

	public static class Builder {
		private final ItemStack stack;
		private Player owner;
		private HolderLookup.Provider registryLookup;

		private Builder(ItemStack stack) {
			this.stack = stack;
		}

		public Builder withOwner(@Nullable Player owner) {
			this.owner = owner;
			return this;
		}

		public Builder withRegistryLookup(HolderLookup.@Nullable Provider registryLookup) {
			this.registryLookup = registryLookup;
			return this;
		}

		public PreviewContext build() {
			if (this.registryLookup == null && this.owner != null) {
				this.registryLookup = this.owner.registryAccess();
			}
			return new PreviewContext(this.stack, this.owner, BackpackTooltip.INSTANCE.configuration(),
					this.registryLookup);
		}
	}
}
