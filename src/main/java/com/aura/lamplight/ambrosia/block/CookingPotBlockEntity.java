package com.aura.lamplight.ambrosia.block;

import com.aura.lamplight.ambrosia.food.CookingPotRecipe;
import com.aura.lamplight.ambrosia.food.CookingPotRecipeInput;
import com.aura.lamplight.ambrosia.food.ModRecipes;
import com.aura.lamplight.ambrosia.sound.ModSounds;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class CookingPotBlockEntity extends BlockEntity implements Container {
    public static final int INPUT_SLOTS = 6;
    public static final int CONTAINER_SLOT = 6;
    public static final int OUTPUT_SLOT = 7;
    public static final int SLOT_COUNT = 8;

    private static final int DATA_COOK_TIME = 0;
    private static final int DATA_COOK_TOTAL = 1;
    private static final int DATA_HEATED = 2;

    private final NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
    private final ContainerData data = new SimpleContainerData(3);
    private int cookTime;

    public CookingPotBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.COOKING_POT, pos, state);
    }

    public ContainerData getContainerData() {
        return this.data;
    }

    public boolean isHeated() {
        return this.data.get(DATA_HEATED) != 0;
    }

    public float getCookProgress() {
        int total = this.data.get(DATA_COOK_TOTAL);
        return total <= 0 ? 0.0F : Math.min(1.0F, this.data.get(DATA_COOK_TIME) / (float) total);
    }

    @Override
    public int getContainerSize() {
        return SLOT_COUNT;
    }

    @Override
    public ItemStack getItem(int index) {
        return this.items.get(index);
    }

    @Override
    public ItemStack removeItem(int index, int count) {
        ItemStack result = ContainerHelper.removeItem(this.items, index, count);
        if (!result.isEmpty()) {
            this.setChanged();
        }
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int index) {
        return ContainerHelper.takeItem(this.items, index);
    }

    @Override
    public void setItem(int index, ItemStack stack) {
        this.items.set(index, stack);
        if (stack.getCount() > this.getMaxStackSize()) {
            stack.setCount(this.getMaxStackSize());
        }
        this.setChanged();
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            this.cookTime = Math.min(this.cookTime, currentRecipeTotal());
            this.data.set(DATA_COOK_TIME, this.cookTime);
        }
    }

    @Override
    public boolean stillValid(Player player) {
        if (this.level == null || this.level.getBlockEntity(this.worldPosition) != this) {
            return false;
        }
        return player.distanceToSqr(this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 0.5,
                this.worldPosition.getZ() + 0.5) <= 64.0;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : this.items) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void clearContent() {
        this.items.clear();
    }

    @Override
    protected void saveAdditional(net.minecraft.world.level.storage.ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, this.items);
        output.putInt("CookTime", this.cookTime);
    }

    @Override
    protected void loadAdditional(net.minecraft.world.level.storage.ValueInput input) {
        super.loadAdditional(input);
        ContainerHelper.loadAllItems(input, this.items);
        this.cookTime = input.getIntOr("CookTime", 0);
    }

    private CookingPotRecipeInput input() {
        return new CookingPotRecipeInput(List.of(
                this.items.get(0), this.items.get(1), this.items.get(2),
                this.items.get(3), this.items.get(4), this.items.get(5)));
    }

    private Optional<RecipeHolder<CookingPotRecipe>> findRecipe() {
        if (this.level == null) {
            return Optional.empty();
        }
        var server = this.level.getServer();
        if (server == null) {
            return Optional.empty();
        }
        Optional<RecipeHolder<CookingPotRecipe>> match = server.getRecipeManager()
                .getRecipeFor(ModRecipes.COOKING_POT_TYPE, this.input(), this.level);
        if (match.isEmpty()) {
            return match;
        }
        if (match.get().value().container().isPresent()
                && !match.get().value().container().get().test(this.items.get(CONTAINER_SLOT))) {
            return Optional.empty();
        }
        return match;
    }

    private int currentRecipeTotal() {
        return this.findRecipe().map(holder -> holder.value().cookingTime()).orElse(CookingPotRecipe.DEFAULT_COOKING_TIME);
    }

    private boolean canOutput(ItemStack result) {
        ItemStack output = this.items.get(OUTPUT_SLOT);
        if (output.isEmpty()) {
            return true;
        }
        if (!ItemStack.isSameItemSameComponents(output, result)) {
            return false;
        }
        return output.getCount() + result.getCount() <= output.getMaxStackSize();
    }

    private void consumeInputs(CookingPotRecipe recipe) {
        List<net.minecraft.world.item.crafting.Ingredient> remaining = new java.util.ArrayList<>(recipe.ingredients());
        for (int i = 0; i < INPUT_SLOTS; i++) {
            ItemStack stack = this.items.get(i);
            if (stack.isEmpty()) {
                continue;
            }
            for (int j = 0; j < remaining.size(); j++) {
                if (remaining.get(j).test(stack)) {
                    stack.shrink(1);
                    remaining.remove(j);
                    break;
                }
            }
        }
        recipe.container().ifPresent(container -> {
            if (container.test(this.items.get(CONTAINER_SLOT))) {
                this.items.get(CONTAINER_SLOT).shrink(1);
            }
        });
    }

    private void finishCooking(RecipeHolder<CookingPotRecipe> holder) {
        ItemStack result = holder.value().assemble(this.input());
        ItemStack output = this.items.get(OUTPUT_SLOT);
        if (output.isEmpty()) {
            this.items.set(OUTPUT_SLOT, result.copy());
        } else {
            output.grow(result.getCount());
        }
        this.consumeInputs(holder.value());
        this.cookTime = 0;
        this.data.set(DATA_COOK_TIME, 0);
        this.setChanged();
    }

    public static boolean isHeated(Level level, BlockPos pos) {
        BlockState below = level.getBlockState(pos.below());
        if (below.is(Blocks.LAVA) || below.is(Blocks.MAGMA_BLOCK)) {
            return true;
        }
        if (below.is(Blocks.FIRE) || below.is(Blocks.SOUL_FIRE)) {
            return true;
        }
        return below.getBlock() instanceof CampfireBlock && CampfireBlock.isLitCampfire(below);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, CookingPotBlockEntity pot) {
        boolean heated = isHeated(level, pos);
        pot.data.set(DATA_HEATED, heated ? 1 : 0);
        Optional<RecipeHolder<CookingPotRecipe>> match = pot.findRecipe();
        boolean canCook = heated && match.isPresent() && pot.canOutput(match.get().value().assemble(pot.input()));
        if (!canCook) {
            if (match.isEmpty()) {
                pot.cookTime = 0;
                pot.data.set(DATA_COOK_TIME, 0);
            }
            pot.data.set(DATA_COOK_TOTAL, pot.currentRecipeTotal());
            return;
        }
        int total = match.get().value().cookingTime();
        pot.data.set(DATA_COOK_TOTAL, total);
        pot.cookTime++;
        pot.data.set(DATA_COOK_TIME, pot.cookTime);
        if (!level.isClientSide() && pot.cookTime % 80 == 0 && level instanceof ServerLevel server) {
            var sound = server.getRandom().nextBoolean() ? ModSounds.POT_BOIL_SOUP : ModSounds.POT_BOIL_WATER;
            level.playSound(null, pos, sound, SoundSource.BLOCKS, 0.5F, 0.9F + server.getRandom().nextFloat() * 0.2F);
        }
        if (pot.cookTime >= total) {
            pot.finishCooking(match.get());
        }
        pot.setChanged();
    }

    public ItemStack previewResult() {
        Optional<RecipeHolder<CookingPotRecipe>> match = this.findRecipe();
        if (match.isEmpty()) {
            return ItemStack.EMPTY;
        }
        return match.get().value().assemble(this.input());
    }
}
