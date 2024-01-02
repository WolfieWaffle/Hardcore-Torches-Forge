package com.github.wolfiewaffle.hardcore_torches.recipe;

import com.github.wolfiewaffle.hardcore_torches.config.Config;
import com.github.wolfiewaffle.hardcore_torches.init.ItemInit;
import com.github.wolfiewaffle.hardcore_torches.item.OilCanItem;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import org.jetbrains.annotations.Nullable;

public class OilCanRecipe extends ShapelessRecipe {
    final int fuelAmount;
    static final ItemStack result = new ItemStack(ItemInit.OIL_CAN.get());

    public OilCanRecipe(String group, ItemStack result, NonNullList<Ingredient> recipeItems, int fuelAmount) {
        super(group, CraftingBookCategory.EQUIPMENT, result, recipeItems);
        this.fuelAmount = fuelAmount;
    }

    @Override
    public ItemStack assemble(CraftingContainer grid, RegistryAccess registryAccess) {
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

    public static class Serializer implements RecipeSerializer<OilCanRecipe> {

        private static final Codec<OilCanRecipe> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance.group(ExtraCodecs.strictOptionalField(Codec.STRING, "group", "").forGetter((builder) -> {
                return builder.getGroup();
            }), CraftingRecipeCodecs.ITEMSTACK_OBJECT_CODEC.fieldOf("result").forGetter((recipe) -> {
                return recipe.getResultItem(null);
            }), Ingredient.CODEC_NONEMPTY.listOf().fieldOf("ingredients").flatXmap((recipe) -> {
                Ingredient[] aingredient = recipe.stream().filter((ings) -> {
                    return !ings.isEmpty();
                }).toArray((count) -> {
                    return new Ingredient[count];
                });
                if (aingredient.length == 0) {
                    return DataResult.error(() -> {
                        return "No ingredients for shapeless recipe";
                    });
                } else {
                    return aingredient.length > 3 * 3 ? DataResult.error(() -> {
                        return "Too many ingredients for shapeless recipe";
                    }) : DataResult.success(NonNullList.of(Ingredient.EMPTY, aingredient));
                }
            }, DataResult::success).forGetter((recipe) -> {
                return recipe.getIngredients();
            }), Codec.INT.fieldOf("fuel").forGetter((recipe) -> {
                return recipe.fuelAmount;
            })).apply(instance, OilCanRecipe::new);
        });

        public Codec<OilCanRecipe> codec() {
            return CODEC;
        }

        @Override
        public @Nullable OilCanRecipe fromNetwork(FriendlyByteBuf friendlyByteBuf) {
            ShapelessRecipe rec = ShapelessRecipe.Serializer.SHAPELESS_RECIPE.fromNetwork(friendlyByteBuf);
            int fuel = friendlyByteBuf.readVarInt();

            return new OilCanRecipe(rec.getGroup(), rec.getResultItem(null), rec.getIngredients(), fuel);
        }

        public void toNetwork(FriendlyByteBuf friendlyByteBuf, OilCanRecipe oilCanRecipe) {
            ShapelessRecipe rec = new ShapelessRecipe(oilCanRecipe.getGroup(), CraftingBookCategory.EQUIPMENT, result, oilCanRecipe.getIngredients());

            ShapelessRecipe.Serializer.SHAPELESS_RECIPE.toNetwork(friendlyByteBuf, rec);
            friendlyByteBuf.writeVarInt(oilCanRecipe.fuelAmount);
        }
    }
}
