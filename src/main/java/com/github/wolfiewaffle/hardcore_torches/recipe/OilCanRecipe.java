package com.github.wolfiewaffle.hardcore_torches.recipe;

import com.github.wolfiewaffle.hardcore_torches.config.Config;
import com.github.wolfiewaffle.hardcore_torches.item.OilCanItem;
import com.google.gson.JsonObject;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapelessRecipe;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;

public class OilCanRecipe extends ShapelessRecipe {
    final int fuelAmount;

    public OilCanRecipe(ResourceLocation id, String group, ItemStack result, NonNullList<Ingredient> recipeItems, int fuelAmount) {
        super(id, group, result, recipeItems);
        this.fuelAmount = fuelAmount;
    }

    @Override
    public ItemStack assemble(CraftingInventory grid) {
        int startFuel;

        for(int i = 0; i < grid.getContainerSize(); ++i) {
            ItemStack itemstack = grid.getItem(i);

            if (itemstack.getItem() instanceof OilCanItem) {
                OilCanItem can = (OilCanItem) itemstack.getItem();

                startFuel = can.getFuel(itemstack);

                return OilCanItem.setFuel(itemstack.copy(), (int) (startFuel + (fuelAmount * Config.oilRecipeMultiplier.get())));
            }
        }

        return ItemStack.EMPTY;
    }

    public static class Serializer extends net.minecraftforge.registries.ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<OilCanRecipe> {
        private static final ResourceLocation NAME = new ResourceLocation("hardcore_torches", "oil_can");

        public OilCanRecipe fromJson(ResourceLocation resourceLocation, JsonObject json) {
            ShapelessRecipe recipe = ShapelessRecipe.Serializer.SHAPELESS_RECIPE.fromJson(resourceLocation, json);
            int fuel = json.get("fuel").getAsInt();

            return new OilCanRecipe(recipe.getId(), recipe.getGroup(), recipe.getResultItem(), recipe.getIngredients(), fuel);
        }

        public OilCanRecipe fromNetwork(ResourceLocation resourceLocation, PacketBuffer friendlyByteBuf) {
            ShapelessRecipe recipe = ShapelessRecipe.Serializer.SHAPELESS_RECIPE.fromNetwork(resourceLocation, friendlyByteBuf);

            int fuelValue = friendlyByteBuf.readVarInt();
            return new OilCanRecipe(recipe.getId(), recipe.getGroup(), recipe.getResultItem(), recipe.getIngredients(), fuelValue);
        }

        public void toNetwork(PacketBuffer friendlyByteBuf, OilCanRecipe oilCanRecipe) {
            ShapelessRecipe rec = new ShapelessRecipe(oilCanRecipe.getId(), oilCanRecipe.getGroup(), oilCanRecipe.getResultItem(), oilCanRecipe.getIngredients());
            ShapelessRecipe.Serializer.SHAPELESS_RECIPE.toNetwork(friendlyByteBuf, rec);

            friendlyByteBuf.writeVarInt(oilCanRecipe.fuelAmount);
        }
    }
}
