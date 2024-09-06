package com.github.wolfiewaffle.hardcore_torches.recipe;

import com.github.wolfiewaffle.hardcore_torches.config.Config;
import com.github.wolfiewaffle.hardcore_torches.item.OilCanItem;
import com.github.wolfiewaffle.hardcore_torches.item.TorchItem;
import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;

public class DamageLightRecipe extends ShapelessRecipe {
    final boolean damageItem;

    public DamageLightRecipe(ResourceLocation id, String group, ItemStack result, NonNullList<Ingredient> recipeItems, int damageItem) {
        super(id, group, CraftingBookCategory.EQUIPMENT, result, recipeItems);
        this.damageItem = damageItem == 1 ? true : false;
    }

    @Override
    public ItemStack assemble(CraftingContainer grid, RegistryAccess registryAccess) {
        int fuel = 0;
        ItemStack resultStack = this.getResultItem(registryAccess).copy();

        for(int i = 0; i < grid.getContainerSize(); ++i) {
            ItemStack itemstack = grid.getItem(i);

            if (itemstack.getItem() instanceof TorchItem) {
                fuel = TorchItem.getFuel(itemstack);
            }
        }

        if (resultStack.getItem() instanceof TorchItem) {
            return TorchItem.setFuel(resultStack, fuel);
        }

        return ItemStack.EMPTY;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer container) {
        NonNullList<ItemStack> nonnulllist = NonNullList.withSize(container.getContainerSize(), ItemStack.EMPTY);

        for(int i = 0; i < nonnulllist.size(); ++i) {
            ItemStack item = container.getItem(i);

            if (damageItem == false) {
                ItemStack result = item.copy();
                if (!(item.getItem() instanceof TorchItem))
                nonnulllist.set(i, result);
                continue;
            }

            if (item.hasCraftingRemainingItem()) {
                nonnulllist.set(i, item.getCraftingRemainingItem());
            } else if (item.isDamageableItem() && damageItem == true) {
                if (item.getDamageValue() == item.getMaxDamage() - 1) continue;
                else {
                    ItemStack result = item.copy();
                    result.setDamageValue(item.getDamageValue() + 1);
                    nonnulllist.set(i, result);
                }
            }
        }

        return nonnulllist;
    }

    public static class Serializer implements RecipeSerializer<DamageLightRecipe> {
        private static final ResourceLocation NAME = new ResourceLocation("hardcore_torches", "damage_light");

        public DamageLightRecipe fromJson(ResourceLocation resourceLocation, JsonObject json) {
            ShapelessRecipe recipe = ShapelessRecipe.Serializer.SHAPELESS_RECIPE.fromJson(resourceLocation, json);
            boolean damageItem = json.get("damage").getAsBoolean();

            return new DamageLightRecipe(recipe.getId(), recipe.getGroup(), recipe.getResultItem(null), recipe.getIngredients(), damageItem == true ? 1 : 0);
        }

        public DamageLightRecipe fromNetwork(ResourceLocation resourceLocation, FriendlyByteBuf friendlyByteBuf) {
            ShapelessRecipe recipe = ShapelessRecipe.Serializer.SHAPELESS_RECIPE.fromNetwork(resourceLocation, friendlyByteBuf);

            int damageItem = friendlyByteBuf.readVarInt();
            return new DamageLightRecipe(recipe.getId(), recipe.getGroup(), recipe.getResultItem(null), recipe.getIngredients(), damageItem);
        }

        public void toNetwork(FriendlyByteBuf friendlyByteBuf, DamageLightRecipe damageLightRecipe) {
            ShapelessRecipe rec = new ShapelessRecipe(damageLightRecipe.getId(), damageLightRecipe.getGroup(), CraftingBookCategory.EQUIPMENT, damageLightRecipe.getResultItem(null), damageLightRecipe.getIngredients());
            ShapelessRecipe.Serializer.SHAPELESS_RECIPE.toNetwork(friendlyByteBuf, rec);

            friendlyByteBuf.writeInt(damageLightRecipe.damageItem == true ? 1 : 0);
        }
    }
}
