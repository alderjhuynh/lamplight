package com.aura.lamplight.mixin.hearthwright;

import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PoiTypes.class)
public interface PoiTypesAccessor {

    @Accessor("TYPE_BY_STATE")
    static Map<BlockState, Holder<PoiType>> statesByType() {
        throw new AssertionError("Mixin accessor was not applied");
    }
}
