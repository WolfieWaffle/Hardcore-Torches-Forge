package com.github.wolfiewaffle.hardcore_torches.recipe;

import com.github.wolfiewaffle.hardcore_torches.config.Config;
import com.github.wolfiewaffle.hardcore_torches.item.TorchItem;
import com.github.wolfiewaffle.hardcore_torches.util.ETorchState;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;

public class DamageLightRecipe extends ShapelessRecipe {
    final boolean damageItem;

    public DamageLightRecipe(String group, ItemStack result, NonNullList<Ingredient> recipeItems, boolean damageItem) {
        super(group, CraftingBookCategory.MISC, result, recipeItems);
        this.damageItem = damageItem;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        int fuel = 0;
        ItemStack resultStack = this.getResultItem(registries).copy();

        for(int i = 0; i < input.size(); ++i) {
            ItemStack itemstack = input.getItem(i);

            if (itemstack.getItem() instanceof TorchItem torch) {
                if (torch.burnState == ETorchState.UNLIT || torch.burnState == ETorchState.SMOLDERING) fuel = TorchItem.getFuel(itemstack);
            }
        }

        if (resultStack.getItem() instanceof TorchItem) {
            return TorchItem.setFuel(resultStack, fuel);
        }

        return ItemStack.EMPTY;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
        NonNullList<ItemStack> nonnulllist = NonNullList.withSize(input.size(), ItemStack.EMPTY);

        for(int i = 0; i < nonnulllist.size(); ++i) {
            ItemStack item = input.getItem(i);

            if (damageItem == false) {
                ItemStack result = item.copyWithCount(1);
                if (item.getItem() instanceof TorchItem torch) {
                    if (torch.burnState == ETorchState.LIT) nonnulllist.set(i, result);
                } else {
                    nonnulllist.set(i, result);
                }
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
        private static final ResourceLocation NAME = ResourceLocation.parse("hardcore_torches:damage_light");

        // Codec
        private static final MapCodec<DamageLightRecipe> CODEC = RecordCodecBuilder.mapCodec((builder) -> builder.group(
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

                Codec.BOOL.fieldOf("damage").forGetter((rec) -> rec.damageItem)

        ).apply(builder, DamageLightRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, DamageLightRecipe> STREAM_CODEC = StreamCodec.of(Serializer::toNetwork, Serializer::fromNetwork);

        public Serializer() {
        }

        @Override
        public MapCodec<DamageLightRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, DamageLightRecipe> streamCodec() {
            return STREAM_CODEC;
        }

        private static DamageLightRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
            String group = buffer.readUtf();
            int i = buffer.readVarInt();
            NonNullList<Ingredient> ingredients = NonNullList.withSize(i, Ingredient.EMPTY);
            ingredients.replaceAll((ingredient) -> Ingredient.CONTENTS_STREAM_CODEC.decode(buffer));
            ItemStack result = ItemStack.STREAM_CODEC.decode(buffer);
            boolean damageItem = buffer.readBoolean();

            return new DamageLightRecipe(group, result, ingredients, damageItem);
        }

        private static void toNetwork(RegistryFriendlyByteBuf buffer, DamageLightRecipe recipe) {
            buffer.writeUtf(recipe.getGroup());
            buffer.writeVarInt(recipe.getIngredients().size());
            for(Ingredient ingredient : recipe.getIngredients()) {
                Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, ingredient);
            }
            ItemStack.STREAM_CODEC.encode(buffer, recipe.getResultItem(null));
            buffer.writeBoolean(recipe.damageItem);
        }
    }
}
