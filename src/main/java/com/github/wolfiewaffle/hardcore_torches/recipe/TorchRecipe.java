package com.github.wolfiewaffle.hardcore_torches.recipe;

import com.github.wolfiewaffle.hardcore_torches.config.Config;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.neoforged.neoforge.common.util.FriendlyByteBufUtil;

import java.util.Optional;

public class TorchRecipe extends ShapedRecipe {
    ShapedRecipePattern pattern;

    public TorchRecipe(String group, ShapedRecipePattern pattern, ItemStack result) {
        super(group, CraftingBookCategory.EQUIPMENT, pattern, result.copyWithCount(Config.torchCraftAmount.get()));
        this.pattern = pattern;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        ItemStack resultStack = this.getResultItem(registries).copy();
        resultStack.setCount(Config.torchCraftAmount.get());

        return resultStack;
    }

    public static class Serializer implements RecipeSerializer<TorchRecipe> {
        private static final ResourceLocation NAME = ResourceLocation.parse("hardcore_torches:torch");

        public static final MapCodec<TorchRecipe> CODEC = RecordCodecBuilder.mapCodec((builder) -> builder.group(
                Codec.STRING.optionalFieldOf("group", "").forGetter((rec) -> rec.getGroup()),
                ShapedRecipePattern.MAP_CODEC.forGetter((rec) -> rec.pattern),
                ItemStack.STRICT_CODEC.fieldOf("result").forGetter((rec) -> rec.getResultItem(null).copyWithCount(Config.torchCraftAmount.get()))
        ).apply(builder, TorchRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, TorchRecipe> STREAM_CODEC = StreamCodec.of(Serializer::toNetwork, Serializer::fromNetwork);

        public Serializer() {
        }

        @Override
        public MapCodec<TorchRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, TorchRecipe> streamCodec() {
            return STREAM_CODEC;
        }

        private static TorchRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
            String group = buffer.readUtf();
            ShapedRecipePattern shapedRecipePattern = ShapedRecipePattern.STREAM_CODEC.decode(buffer);
            ItemStack resultStack = ItemStack.STREAM_CODEC.decode(buffer);

            return new TorchRecipe(group, shapedRecipePattern, resultStack);
        }

        private static void toNetwork(RegistryFriendlyByteBuf buffer, TorchRecipe torchRecipe) {
            buffer.writeUtf(torchRecipe.getGroup());
            ShapedRecipePattern.STREAM_CODEC.encode(buffer, torchRecipe.pattern);
            ItemStack.STREAM_CODEC.encode(buffer, torchRecipe.getResultItem(null));
        }
    }
}
