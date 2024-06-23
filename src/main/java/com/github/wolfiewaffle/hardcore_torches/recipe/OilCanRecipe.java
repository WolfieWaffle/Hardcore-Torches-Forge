package com.github.wolfiewaffle.hardcore_torches.recipe;

import com.github.wolfiewaffle.hardcore_torches.config.Config;
import com.github.wolfiewaffle.hardcore_torches.init.ItemInit;
import com.github.wolfiewaffle.hardcore_torches.item.OilCanItem;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.RecipeMatcher;
import org.jetbrains.annotations.Nullable;

public class OilCanRecipe extends ShapelessRecipe {
    final int fuelAmount;
    static final ItemStack result = new ItemStack(ItemInit.OIL_CAN.get());

    public OilCanRecipe(String group, CraftingBookCategory category, ItemStack result, NonNullList<Ingredient> recipeItems, int fuelAmount) {
        super(group, category, result, recipeItems);
        this.fuelAmount = fuelAmount;
    }

    @Override
    public boolean matches(CraftingContainer grid, Level world) {
        StackedContents recipeMatcher = new StackedContents();
        Item fuelItem = null;
        int i = 0;

        for(int j = 0; j < grid.getContainerSize(); ++j) {
            ItemStack itemStack = grid.getItem(j);
            if (!itemStack.isEmpty()) {
                if (itemStack.getItem() instanceof OilCanItem) {
                    // Oil can
                    recipeMatcher.accountStack(itemStack, 1);
                    ++i;
                } else {
                    // Anything else
                    if (fuelItem == null) {
                        recipeMatcher.accountStack(itemStack, 1);
                        ++i;
                        fuelItem = itemStack.getItem();
                    } else if (fuelItem != itemStack.getItem()) {
                        recipeMatcher.accountStack(itemStack, 1);
                        ++i;
                    }
                }
            }
        }

        boolean match = recipeMatcher.canCraft(this, null);
        return i == this.getIngredients().size() && match;
    }

    @Override
    public ItemStack assemble(CraftingContainer grid, RegistryAccess registryAccess) {
        int startFuel = 0;
        int addFuel = 0;
        ItemStack resultStack = ItemStack.EMPTY;

        for(int i = 0; i < grid.getContainerSize(); ++i) {
            ItemStack itemstack = grid.getItem(i);

            if (!itemstack.isEmpty()) {
                if (itemstack.getItem() instanceof OilCanItem) {
                    OilCanItem can = (OilCanItem) itemstack.getItem();
                    startFuel = can.getFuel(itemstack);
                    resultStack = itemstack;
                } else {
                    addFuel += fuelAmount * Config.oilRecipeMultiplier.get();
                }
            }
        }

        if (resultStack.getItem() instanceof OilCanItem) {
            return OilCanItem.setFuel(resultStack.copy(), startFuel + addFuel);
        }

        return ItemStack.EMPTY;
    }

    public static class Serializer implements RecipeSerializer<OilCanRecipe> {
        private static final Codec<OilCanRecipe> CODEC = RecordCodecBuilder.create((recipe) -> {
            return recipe.group(ExtraCodecs.strictOptionalField(Codec.STRING, "group", "").forGetter((rec) -> {
                return rec.getGroup();
            }), CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter((rec) -> {
                return rec.category();
            }), ItemStack.ITEM_WITH_COUNT_CODEC.fieldOf("result").forGetter((red) -> {
                return red.getResultItem(null);
            }), Ingredient.CODEC_NONEMPTY.listOf().fieldOf("ingredients").flatXmap((ingredients) -> {
                Ingredient[] aingredient = ingredients.stream().filter((ingredient) -> {
                    return !ingredient.isEmpty();
                }).toArray((index) -> {
                    return new Ingredient[index];
                });
                if (aingredient.length == 0) {
                    return DataResult.error(() -> {
                        return "No ingredients for shapeless recipe";
                    });
                } else {
                    return (aingredient.length > 9) ? DataResult.error(() -> {
                        return "Too many ingredients for shapeless recipe";
                    }) : DataResult.success(NonNullList.of(Ingredient.EMPTY, aingredient));
                }
            }, DataResult::success).forGetter((rec) -> {
                return rec.getIngredients();
            }), Codec.INT.fieldOf("fuel").forGetter((rec) -> {
                return rec.fuelAmount;
            })).apply(recipe, OilCanRecipe::new);
        });

        public Codec<OilCanRecipe> codec() {
            return CODEC;
        }

        @Override
        public @Nullable OilCanRecipe fromNetwork(FriendlyByteBuf friendlyByteBuf) {
            ShapelessRecipe rec = ShapelessRecipe.Serializer.SHAPELESS_RECIPE.fromNetwork(friendlyByteBuf);
            int fuel = friendlyByteBuf.readVarInt();

            return new OilCanRecipe(rec.getGroup(), CraftingBookCategory.EQUIPMENT, rec.getResultItem(null), rec.getIngredients(), fuel);
        }

        public void toNetwork(FriendlyByteBuf friendlyByteBuf, OilCanRecipe oilCanRecipe) {
            ShapelessRecipe rec = new ShapelessRecipe(oilCanRecipe.getGroup(), CraftingBookCategory.EQUIPMENT, result, oilCanRecipe.getIngredients());

            ShapelessRecipe.Serializer.SHAPELESS_RECIPE.toNetwork(friendlyByteBuf, rec);
            friendlyByteBuf.writeVarInt(oilCanRecipe.fuelAmount);
        }
    }
}
