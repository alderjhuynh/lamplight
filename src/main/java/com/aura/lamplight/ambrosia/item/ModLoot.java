package com.aura.lamplight.ambrosia.item;

import com.aura.lamplight.ambrosia.Ambrosia;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.advancements.predicates.ItemPredicate;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.animal.cow.AbstractCow;
import net.minecraft.world.entity.animal.cow.MushroomCow;
import net.minecraft.world.entity.animal.rabbit.Rabbit;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

public final class ModLoot {
    private ModLoot() {
    }

    private static Identifier vanilla(String path) {
        return Identifier.withDefaultNamespace(path);
    }

    private static LootPool.Builder chanceDrop(ItemLike item, float chance) {
        return LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1.0F))
                .add(LootItem.lootTableItem(item)
                        .when(LootItemRandomChanceCondition.randomChance(chance)));
    }

    private static LootPool.Builder knifeDrop(ItemLike item, float chance, HolderGetter<Item> items) {
        return LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1.0F))
                .add(LootItem.lootTableItem(item)
                        .when(MatchTool.toolMatches(ItemPredicate.Builder.item().of(items,
                                ModItems.WOODEN_KNIFE, ModItems.STONE_KNIFE, ModItems.COPPER_KNIFE,
                                ModItems.IRON_KNIFE, ModItems.GOLDEN_KNIFE, ModItems.DIAMOND_KNIFE,
                                ModItems.NETHERITE_KNIFE)))
                        .when(LootItemRandomChanceCondition.randomChance(chance)));
    }

    @SuppressWarnings("all")
    public static void register() {
        LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
            Identifier id = key.identifier();
            if (id.equals(vanilla("blocks/short_grass"))) {
                HolderGetter<Item> items = registries.lookupOrThrow(Registries.ITEM);
                tableBuilder.withPool(knifeDrop(ModItems.CORN_SEEDS, 0.08F, items));
                tableBuilder.withPool(knifeDrop(ModItems.RICE_SEEDS, 0.08F, items));
                tableBuilder.withPool(knifeDrop(ModItems.TOMATO_SEEDS, 0.06F, items));
                tableBuilder.withPool(knifeDrop(ModItems.LETTUCE_SEEDS, 0.08F, items));
                tableBuilder.withPool(knifeDrop(ModItems.GRAPE_SEEDS, 0.06F, items));
                tableBuilder.withPool(chanceDrop(ModItems.GINGER, 0.02F));
            } else if (id.equals(vanilla("chests/village/village_plains_house"))) {
                tableBuilder.withPool(chanceDrop(ModItems.LETTUCE, 0.35F));
                tableBuilder.withPool(chanceDrop(ModItems.TOMATO, 0.35F));
            } else if (id.equals(vanilla("chests/village/village_savanna_house"))) {
                tableBuilder.withPool(chanceDrop(ModItems.GINGER, 0.35F));
                tableBuilder.withPool(chanceDrop(ModItems.GRAPE_SEEDS, 0.25F));
            } else if (id.equals(vanilla("chests/village/village_desert_house"))) {
                tableBuilder.withPool(chanceDrop(ModItems.GINGER, 0.35F));
            } else if (id.equals(vanilla("chests/jungle_temple"))) {
                tableBuilder.withPool(chanceDrop(ModItems.CINNAMON, 0.5F));
                tableBuilder.withPool(chanceDrop(ModItems.COFFEE_CHERRIES, 0.5F));
            }
        });

        UseEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
            if (!(entity instanceof AbstractCow) || world.isClientSide()) {
                return InteractionResult.PASS;
            }
            ItemStack held = player.getItemInHand(hand);
            if (!held.is(Items.GLASS_BOTTLE)) {
                return InteractionResult.PASS;
            }
            if (!player.hasInfiniteMaterials()) {
                held.shrink(1);
            }
            ItemStack filled = new ItemStack(ModItems.MILK_BOTTLE);
            if (held.isEmpty()) {
                player.setItemInHand(hand, filled);
            } else if (!player.getInventory().add(filled)) {
                player.drop(filled, false);
            }
            player.playSound(SoundEvents.COW_MILK, 1.0F, 1.0F);
            Ambrosia.LOGGER.debug("{} filled a milk bottle", player.getName().getString());
            return InteractionResult.SUCCESS;
        });
        UseEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
            if (world.isClientSide() || hand != InteractionHand.MAIN_HAND) {
                return InteractionResult.PASS;
            }
            ItemStack held = player.getItemInHand(hand);
            if (!ModItems.isKnife(held) || !(entity instanceof LivingEntity living)) {
                return InteractionResult.PASS;
            }
            if (entity instanceof AgeableMob ageable && ageable.isBaby()) {
                return InteractionResult.PASS;
            }
            Item cutlet = cutletForEntity(entity);
            if (cutlet == null) {
                return InteractionResult.PASS;
            }
            living.hurt(world.damageSources().playerAttack(player), 1.0F);
            if (!player.hasInfiniteMaterials()) {
                held.hurtAndBreak(1, player, hand);
            }
            ItemStack drop = new ItemStack(cutlet);
            world.addFreshEntity(new ItemEntity(world, entity.getX(), entity.getY() + 0.5, entity.getZ(), drop));
            player.getCooldowns().addCooldown(held, 20);
            world.playSound(null, entity.blockPosition(), SoundEvents.MOOSHROOM_SHEAR, SoundSource.PLAYERS, 0.8F, 1.2F);
            return InteractionResult.SUCCESS;
        });
    }

    private static Item cutletForEntity(Entity entity) {
        if (entity instanceof AbstractCow || entity instanceof MushroomCow) {
            return ModItems.BEEF_CUTLETS;
        } else if (entity instanceof Sheep) {
            return ModItems.MUTTON_CUTLETS;
        } else if (entity instanceof Chicken) {
            return ModItems.CHICKEN_CUTLETS;
        } else if (entity instanceof Rabbit) {
            return ModItems.RABBIT_CUTLETS;
        }
        return null;
    }
}
