package com.aura.lamplight.client.mixin.armory;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.ArmorSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ArmorSlot.class)
public interface ArmorSlotAccessor {
	@Accessor("slot")
	EquipmentSlot lamplight$getEquipmentSlot();
}
