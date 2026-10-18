package com.aura.lamplight.client.mixin.ambrosia.appleskin;

import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(FoodData.class)
public interface FoodDataAccessor {
    
    @Accessor("exhaustionLevel")
    float ambrosia$getExhaustionLevel();
}
