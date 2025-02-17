package com.github.wolfiewaffle.hardcore_torches.recipe;

import com.github.wolfiewaffle.hardcore_torches.item.TorchItem;
import com.github.wolfiewaffle.hardcore_torches.util.ETorchState;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;

public class DamageLightRecipe extends ShapelessRecipe {
    final boolean damageItem;

    public DamageLightRecipe(String group, ItemStack result, NonNullList<Ingredient> recipeItems, boolean damageItem) {
        super(group, CraftingBookCategory.MISC, result, recipeItems);
        this.damageItem = damageItem;
    }

    @Override
    public ItemStack assemble(CraftingContainer grid, RegistryAccess registryAccess) {
        int fuel = 0;
        ItemStack resultStack = this.getResultItem(registryAccess).copy();

        for(int i = 0; i < grid.getContainerSize(); ++i) {
            ItemStack itemstack = grid.getItem(i);

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
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer container) {
        NonNullList<ItemStack> nonnulllist = NonNullList.withSize(container.getContainerSize(), ItemStack.EMPTY);

        for(int i = 0; i < nonnulllist.size(); ++i) {
            ItemStack item = container.getItem(i);

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
        private static final ResourceLocation NAME = new ResourceLocation("hardcore_torches", "damage_light");

        // Codec
        private static final Codec<DamageLightRecipe> CODEC = RecordCodecBuilder.create((builder) -> builder.group(

                ExtraCodecs.strictOptionalField(Codec.STRING, "group", "").forGetter((p_301127_) -> p_301127_.getGroup()),

                ItemStack.ITEM_WITH_COUNT_CODEC.fieldOf("result").forGetter((p_301142_) -> p_301142_.getResultItem(null)),

                Ingredient.CODEC_NONEMPTY.listOf().fieldOf("ingredients").flatXmap((ingredients) -> {
                    Ingredient[] aingredient = ingredients.toArray((index) -> new Ingredient[index]);
                    if (aingredient.length == 0) {
                        return DataResult.error(() -> "No ingredients for shapeless recipe");
                    } else {
                        return aingredient.length > ShapedRecipePattern.getMaxHeight() * ShapedRecipePattern.getMaxWidth() ? DataResult.error(() -> "Too many ingredients for shapeless recipe. The maximum is: %s".formatted(ShapedRecipePattern.getMaxHeight() * ShapedRecipePattern.getMaxWidth())) : DataResult.success(NonNullList.of(Ingredient.EMPTY, aingredient));
                    }
                }, DataResult::success).forGetter((p_300975_) -> p_300975_.getIngredients()),

                Codec.BOOL.fieldOf("damage").forGetter((rec) -> rec.damageItem)

        ).apply(builder, DamageLightRecipe::new));

        public Serializer() {
        }

        public Codec<DamageLightRecipe> codec() {
            return CODEC;
        }

        public DamageLightRecipe fromNetwork(FriendlyByteBuf friendlyByteBuf) {
            ShapelessRecipe recipe = ShapelessRecipe.Serializer.SHAPELESS_RECIPE.fromNetwork(friendlyByteBuf);

            boolean damageItem = friendlyByteBuf.readBoolean();
            return new DamageLightRecipe(recipe.getGroup(), recipe.getResultItem(null), recipe.getIngredients(), damageItem);
        }

        public void toNetwork(FriendlyByteBuf friendlyByteBuf, DamageLightRecipe damageLightRecipe) {
            ShapelessRecipe rec = new ShapelessRecipe(damageLightRecipe.getGroup(), CraftingBookCategory.EQUIPMENT, damageLightRecipe.getResultItem(null), damageLightRecipe.getIngredients());
            ShapelessRecipe.Serializer.SHAPELESS_RECIPE.toNetwork(friendlyByteBuf, rec);

            friendlyByteBuf.writeBoolean(damageLightRecipe.damageItem);
        }
    }
}
