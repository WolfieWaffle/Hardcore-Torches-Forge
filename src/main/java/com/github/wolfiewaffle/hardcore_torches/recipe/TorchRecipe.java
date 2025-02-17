package com.github.wolfiewaffle.hardcore_torches.recipe;

import com.github.wolfiewaffle.hardcore_torches.config.Config;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;

import java.util.Optional;

public class TorchRecipe extends ShapedRecipe {
    ShapedRecipePattern pattern;

    public TorchRecipe(String group, ShapedRecipePattern pattern, ItemStack result) {
        super(group, CraftingBookCategory.EQUIPMENT, pattern, result.copyWithCount(Config.torchCraftAmount.get()));
        this.pattern = pattern;
    }

    @Override
    public ItemStack assemble(CraftingContainer grid, RegistryAccess registryAccess) {
        ItemStack resultStack = this.getResultItem(registryAccess).copy();
        resultStack.setCount(Config.torchCraftAmount.get());

        return resultStack;
    }

    public static class Serializer implements RecipeSerializer<TorchRecipe> {
        private static final ResourceLocation NAME = new ResourceLocation("hardcore_torches", "torch");

        public static final Codec<TorchRecipe> CODEC = RecordCodecBuilder.create((builder) -> builder.group(
                ExtraCodecs.strictOptionalField(Codec.STRING, "group", "").forGetter((rec) -> rec.getGroup()),
                ShapedRecipePattern.MAP_CODEC.forGetter((rec) -> rec.pattern),
                ItemStack.ITEM_WITH_COUNT_CODEC.fieldOf("result").forGetter((rec) -> rec.getResultItem(null).copyWithCount(Config.torchCraftAmount.get()))
        ).apply(builder, TorchRecipe::new));

        public Serializer() {
        }

        public Codec<TorchRecipe> codec() {
            return CODEC;
        }

        public TorchRecipe fromNetwork(FriendlyByteBuf friendlyByteBuf) {
            ShapedRecipe recipe = ShapedRecipe.Serializer.SHAPED_RECIPE.fromNetwork(friendlyByteBuf);
            return (new TorchRecipe(recipe.getGroup(), new ShapedRecipePattern(recipe.getRecipeWidth(), recipe.getRecipeHeight(), recipe.getIngredients(), Optional.empty()), recipe.getResultItem(null)));
        }

        public void toNetwork(FriendlyByteBuf friendlyByteBuf, TorchRecipe torchRecipe) {
            ShapedRecipe rec = new ShapedRecipe(torchRecipe.getGroup(), CraftingBookCategory.EQUIPMENT, torchRecipe.pattern, torchRecipe.getResultItem(null));
            ShapedRecipe.Serializer.SHAPED_RECIPE.toNetwork(friendlyByteBuf, rec);
        }
    }
}
