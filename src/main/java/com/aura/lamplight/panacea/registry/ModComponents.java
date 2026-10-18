package com.aura.lamplight.panacea.registry;

import com.aura.lamplight.panacea.item.TippedWeapon;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;

import static com.aura.lamplight.panacea.Panacea.id;

public class ModComponents {
    public static final DataComponentType<TippedWeapon> TIPPED_WEAPON = registerComponent(
            "tipped_weapon",
            DataComponentType.<TippedWeapon>builder()
                    .persistent(TippedWeapon.CODEC)
                    .networkSynchronized(TippedWeapon.STREAM_CODEC)
                    .build()
    );

    private static <T> DataComponentType<T> registerComponent(String path, DataComponentType<T> type) {
        return Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, id(path), type);
    }

    public static void register() {
    }
}
