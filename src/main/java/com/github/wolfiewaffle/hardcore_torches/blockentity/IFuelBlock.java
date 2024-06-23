package com.github.wolfiewaffle.hardcore_torches.blockentity;

import com.github.wolfiewaffle.hardcore_torches.MainMod;
import com.github.wolfiewaffle.hardcore_torches.util.ETorchState;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface IFuelBlock {

    int getMaxFuel();

    void outOfFuel(Level world, BlockPos pos, BlockState state);

    default boolean itemValid(ItemStack stack, ETorchState attemptedState) {
        switch (attemptedState) {
            case LIT:
                return privateValid(stack, getFreeLightItems(), getDamageLightItems(), getConsumeLightItems());
            case SMOLDERING:
                return privateValid(stack, getFreeSmotherItems(), getDamageSmotherItems(), getConsumeSmotherItems());
            case UNLIT:
                return privateValid(stack, getFreeExtinguishItems(), getDamageExtinguishItems(), getConsumeExtinguishItems());
        }
        return false;
    }

    default boolean attemptUseItem(ItemStack stack, Player player, InteractionHand hand, ETorchState attemptedState) {
        switch (attemptedState) {
            case LIT:
                return privateAttemptUse(stack, player, hand, getFreeLightItems(), getDamageLightItems(), getConsumeLightItems());
            case SMOLDERING:
                return privateAttemptUse(stack, player, hand, getFreeSmotherItems(), getDamageSmotherItems(), getConsumeSmotherItems());
            case UNLIT:
                return privateAttemptUse(stack, player, hand, getFreeExtinguishItems(), getDamageExtinguishItems(), getConsumeExtinguishItems());
        }
        return false;
    }

    private boolean privateValid(ItemStack stack, TagKey free, TagKey damage, TagKey consume) {

        // Infinite items
        if (stack.is(free)) {
            return true;
        }

        // Durability items
        if (stack.is(damage)) {
            return true;
        }

        // Consume items
        if (stack.is(consume)) {
            return true;
        }

        return false;
    }

    private boolean privateAttemptUse(ItemStack stack, Player player, InteractionHand hand, TagKey free, TagKey damage, TagKey consume) {
        // Infinite items
        if (free != null && stack.is(free)) {
            return true;
        }

        // Durability items
        if (damage != null && stack.is(damage)) {
            if (stack.isDamageableItem() && player instanceof ServerPlayer) {
                stack.hurt(1, RandomSource.create(), (ServerPlayer) player);
            }
            return true;
        }

        // Consume items
        if (consume != null && stack.is(consume)) {
            if (!player.isCreative()) {
                stack.grow(-1);
            }
            return true;
        }

        return false;
    }

    boolean canLight(Level world, BlockPos pos);

    default TagKey getFreeLightItems() {
        return MainMod.FREE_TORCH_LIGHT_ITEMS;
    }

    default TagKey getDamageLightItems() {
        return MainMod.DAMAGE_TORCH_LIGHT_ITEMS;
    }

    default TagKey getConsumeLightItems() {
        return MainMod.CONSUME_TORCH_LIGHT_ITEMS;
    }

    default TagKey getFreeExtinguishItems() {
        return MainMod.FREE_TORCH_EXTINGUISH_ITEMS;
    }

    default TagKey getDamageExtinguishItems() {
        return MainMod.DAMAGE_TORCH_EXTINGUISH_ITEMS;
    }

    default TagKey getConsumeExtinguishItems() {
        return MainMod.CONSUME_TORCH_EXTINGUISH_ITEMS;
    }

    default TagKey getFreeSmotherItems() {
        return MainMod.FREE_TORCH_SMOTHER_ITEMS;
    }

    default TagKey getDamageSmotherItems() {
        return MainMod.DAMAGE_TORCH_SMOTHER_ITEMS;
    }

    default TagKey getConsumeSmotherItems() {
        return MainMod.CONSUME_TORCH_SMOTHER_ITEMS;
    }
}
