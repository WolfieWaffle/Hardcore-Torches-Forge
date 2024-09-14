package com.github.wolfiewaffle.hardcore_torches.recipe;

import com.github.wolfiewaffle.hardcore_torches.config.Config;
import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;

public class TorchRecipe extends ShapedRecipe {

    public TorchRecipe(ResourceLocation id, String group, int width, int height, ItemStack result, NonNullList<Ingredient> recipeItems) {
        super(id, group, CraftingBookCategory.EQUIPMENT, width, height, recipeItems, result);
    }

    @Override
    public ItemStack assemble(CraftingContainer grid, RegistryAccess registryAccess) {
        ItemStack resultStack = this.getResultItem(registryAccess).copy();
        resultStack.setCount(Config.torchCraftAmount.get());

        return resultStack;
    }

    public static class Serializer implements RecipeSerializer<TorchRecipe> {
        private static final ResourceLocation NAME = new ResourceLocation("hardcore_torches", "torch");

        public TorchRecipe fromJson(ResourceLocation resourceLocation, JsonObject json) {
            ShapedRecipe recipe = ShapedRecipe.Serializer.SHAPED_RECIPE.fromJson(resourceLocation, json);
            return new TorchRecipe(recipe.getId(), recipe.getGroup(), recipe.getRecipeWidth(), recipe.getRecipeHeight(), recipe.getResultItem(null), recipe.getIngredients());
        }

        public TorchRecipe fromNetwork(ResourceLocation resourceLocation, FriendlyByteBuf friendlyByteBuf) {
            ShapedRecipe recipe = ShapedRecipe.Serializer.SHAPED_RECIPE.fromNetwork(resourceLocation, friendlyByteBuf);
            return new TorchRecipe(recipe.getId(), recipe.getGroup(), recipe.getRecipeWidth(), recipe.getRecipeHeight(), recipe.getResultItem(null), recipe.getIngredients());
        }

        public void toNetwork(FriendlyByteBuf friendlyByteBuf, TorchRecipe torchRecipe) {
            ShapedRecipe rec = new ShapedRecipe(torchRecipe.getId(), torchRecipe.getGroup(), CraftingBookCategory.EQUIPMENT, 1, 2, torchRecipe.getIngredients(),torchRecipe.getResultItem(null));
            ShapedRecipe.Serializer.SHAPED_RECIPE.toNetwork(friendlyByteBuf, rec);
        }
    }
}
