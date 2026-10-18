package com.aura.lamplight.mixin.arcanum;

import com.aura.lamplight.arcanum.enchantment.RemovedEnchantments;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;

import java.util.stream.Stream;

@Mixin(CreativeModeTabs.class)
public class CreativeModeTabsMixin {

    @WrapOperation(method = "generateEnchantmentBookTypesOnlyMaxLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/HolderLookup;listElements()Ljava/util/stream/Stream;"))
    private static Stream<Holder<Enchantment>> arcanum$filterRemovedOnlyMax(HolderLookup<Enchantment> instance, Operation<Stream<Holder<Enchantment>>> original) {
        Stream<Holder<Enchantment>> stream = original.call(instance);
        return stream.filter(holder -> !RemovedEnchantments.isRemoved(holder));
    }

    @WrapOperation(method = "generateEnchantmentBookTypesAllLevels", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/HolderLookup;listElements()Ljava/util/stream/Stream;"))
    private static Stream<Holder<Enchantment>> arcanum$filterRemovedAllLevels(HolderLookup<Enchantment> instance, Operation<Stream<Holder<Enchantment>>> original) {
        Stream<Holder<Enchantment>> stream = original.call(instance);
        return stream.filter(holder -> !RemovedEnchantments.isRemoved(holder));
    }
}
