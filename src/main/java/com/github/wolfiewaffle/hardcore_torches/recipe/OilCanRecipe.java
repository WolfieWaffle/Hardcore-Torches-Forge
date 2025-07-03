package com.github.wolfiewaffle.hardcore_torches.recipe;

import com.github.wolfiewaffle.hardcore_torches.config.Config;
import com.github.wolfiewaffle.hardcore_torches.item.OilCanItem;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

public class OilCanRecipe extends ShapelessRecipe {
    final int fuelAmount;

    public OilCanRecipe(String group, ItemStack result, NonNullList<Ingredient> recipeItems, int fuelAmount) {
        super(group, CraftingBookCategory.EQUIPMENT, result, recipeItems);
        this.fuelAmount = fuelAmount;
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        StackedContents recipeMatcher = new StackedContents();
        Item fuelItem = null;
        int i = 0;

        for(int j = 0; j < input.size(); ++j) {
            ItemStack itemStack = input.getItem(j);
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
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        int startFuel = 0;
        int addFuel = 0;
        ItemStack resultStack = ItemStack.EMPTY;

        for(int i = 0; i < input.size(); ++i) {
            ItemStack itemstack = input.getItem(i);

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
        private static final ResourceLocation NAME = ResourceLocation.parse("hardcore_torches:oil_can");

        private static final MapCodec<OilCanRecipe> CODEC = RecordCodecBuilder.mapCodec((builder) -> builder.group(
                Codec.STRING.optionalFieldOf("group", "").forGetter((rec) -> rec.getGroup()),
                ItemStack.STRICT_CODEC.fieldOf("result").forGetter((rec) -> rec.getResultItem(null).copyWithCount(Config.torchCraftAmount.get())),
                Ingredient.CODEC_NONEMPTY.listOf().fieldOf("ingredients").flatXmap((rec) -> {
                        Ingredient[] aingredient = rec.toArray((i) -> new Ingredient[i]);

                        if (aingredient.length == 0) {
                            return DataResult.error(() -> "No ingredients for shapeless recipe");
                        } else {
                            return aingredient.length > ShapedRecipePattern.getMaxHeight() * ShapedRecipePattern.getMaxWidth() ?
                                    DataResult.error(() -> "Too many ingredients for shapeless recipe. The maximum is: %s".formatted(ShapedRecipePattern.getMaxHeight() * ShapedRecipePattern.getMaxWidth()))
                                    : DataResult.success(NonNullList.of(Ingredient.EMPTY, aingredient));
                        }
                    }, DataResult::success).forGetter((rec) -> rec.getIngredients()),

                Codec.INT.fieldOf("fuel").forGetter((rec) -> rec.fuelAmount)

        ).apply(builder, OilCanRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, OilCanRecipe> STREAM_CODEC = StreamCodec.of(Serializer::toNetwork, Serializer::fromNetwork);

        @Override
        public MapCodec<OilCanRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, OilCanRecipe> streamCodec() {
            return STREAM_CODEC;
        }

        public Serializer() {
        }

        private static OilCanRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
            String group = buffer.readUtf();
            int i = buffer.readVarInt();
            NonNullList<Ingredient> ingredients = NonNullList.withSize(i, Ingredient.EMPTY);
            ingredients.replaceAll((ingredient) -> Ingredient.CONTENTS_STREAM_CODEC.decode(buffer));
            ItemStack result = ItemStack.STREAM_CODEC.decode(buffer);
            int fuel = buffer.readVarInt();

            return new OilCanRecipe(group, result, ingredients, fuel);
        }

        private static void toNetwork(RegistryFriendlyByteBuf buffer, OilCanRecipe recipe) {
            buffer.writeUtf(recipe.getGroup());
            buffer.writeVarInt(recipe.getIngredients().size());
            for(Ingredient ingredient : recipe.getIngredients()) {
                Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, ingredient);
            }
            ItemStack.STREAM_CODEC.encode(buffer, recipe.getResultItem(null));
            buffer.writeInt(recipe.fuelAmount);
        }
    }
}
