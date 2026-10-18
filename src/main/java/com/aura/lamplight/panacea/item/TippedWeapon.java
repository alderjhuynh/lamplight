package com.aura.lamplight.panacea.item;

import com.aura.lamplight.panacea.registry.ModComponents;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;

/**
 * Stored on a sword dipped in a potion cauldron. Holds the merged potion effects
 * and a pool of charges derived from their duration; each successful melee hit
 * spends one charge to apply the effects to the struck target.
 */
public record TippedWeapon(PotionContents potion, int charges) {
    public static final int MIN_CHARGES = 4;
    public static final int MAX_CHARGES = 32;
    private static final int TICKS_PER_CHARGE = 300;

    public static final Codec<TippedWeapon> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            PotionContents.CODEC.fieldOf("potion").forGetter(TippedWeapon::potion),
            Codec.intRange(0, MAX_CHARGES).fieldOf("charges").forGetter(TippedWeapon::charges)
    ).apply(instance, TippedWeapon::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, TippedWeapon> STREAM_CODEC = StreamCodec.composite(
            PotionContents.STREAM_CODEC, TippedWeapon::potion,
            ByteBufCodecs.VAR_INT, TippedWeapon::charges,
            TippedWeapon::new
    );

    public static TippedWeapon of(PotionContents contents) {
        int longestDuration = 0;
        for (MobEffectInstance effect : contents.getAllEffects()) {
            longestDuration = Math.max(longestDuration, effect.getDuration());
        }
        int charges = Mth.clamp(Math.round(longestDuration / (float) TICKS_PER_CHARGE), MIN_CHARGES, MAX_CHARGES);
        return new TippedWeapon(contents, charges);
    }

    /**
     * Applies the stored effects to the target and consumes one charge,
     * removing the component (and glint) entirely once the pool runs dry.
     */
    public void consumeOnHit(ItemStack stack, LivingEntity target) {
        this.potion.applyToLivingEntity(target, 1.0F);
        if (this.charges <= 1) {
            stack.remove(ModComponents.TIPPED_WEAPON);
            stack.remove(DataComponents.ENCHANTMENT_GLINT_OVERRIDE);
        } else {
            stack.set(ModComponents.TIPPED_WEAPON, new TippedWeapon(this.potion, this.charges - 1));
        }
    }
}
