package com.github.wolfiewaffle.hardcore_torches.recipe;

import com.github.wolfiewaffle.hardcore_torches.config.Config;
import com.github.wolfiewaffle.hardcore_torches.item.OilCanItem;
import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.Level;

public class OilCanRecipe extends ShapelessRecipe {
    final int fuelAmount;

    public OilCanRecipe(ResourceLocation id, String group, ItemStack result, NonNullList<Ingredient> recipeItems, int fuelAmount) {
        super(id, group, CraftingBookCategory.EQUIPMENT, result, recipeItems);
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
        private static final ResourceLocation NAME = new ResourceLocation("hardcore_torches", "oil_can");

        public OilCanRecipe fromJson(ResourceLocation resourceLocation, JsonObject json) {
            ShapelessRecipe recipe = ShapelessRecipe.Serializer.SHAPELESS_RECIPE.fromJson(resourceLocation, json);
            int fuel = json.get("fuel").getAsInt();

            return new OilCanRecipe(recipe.getId(), recipe.getGroup(), recipe.getResultItem(null), recipe.getIngredients(), fuel);
        }

        public OilCanRecipe fromNetwork(ResourceLocation resourceLocation, FriendlyByteBuf friendlyByteBuf) {
            ShapelessRecipe recipe = ShapelessRecipe.Serializer.SHAPELESS_RECIPE.fromNetwork(resourceLocation, friendlyByteBuf);

            int fuelValue = friendlyByteBuf.readVarInt();
            return new OilCanRecipe(recipe.getId(), recipe.getGroup(), recipe.getResultItem(null), recipe.getIngredients(), fuelValue);
        }

        public void toNetwork(FriendlyByteBuf friendlyByteBuf, OilCanRecipe oilCanRecipe) {
            ShapelessRecipe rec = new ShapelessRecipe(oilCanRecipe.getId(), oilCanRecipe.getGroup(), CraftingBookCategory.EQUIPMENT, oilCanRecipe.getResultItem(null), oilCanRecipe.getIngredients());
            ShapelessRecipe.Serializer.SHAPELESS_RECIPE.toNetwork(friendlyByteBuf, rec);

            friendlyByteBuf.writeVarInt(oilCanRecipe.fuelAmount);
        }
    }
}
