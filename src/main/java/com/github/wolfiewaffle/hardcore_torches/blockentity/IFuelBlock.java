package com.github.wolfiewaffle.hardcore_torches.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Random;

public interface IFuelBlock {

    int getMaxFuel();

    void outOfFuel(Level world, BlockPos pos, BlockState state);

    default boolean itemValid(ItemStack stack, TagKey free, TagKey damage, TagKey consume) {

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

    default boolean attemptUse(ItemStack stack, Player player, InteractionHand hand, TagKey free, TagKey damage, TagKey consume) {

        // Infinite items
        if (stack.is(free)) {
            return true;
        }

        // Durability items
        if (stack.is(damage)) {
            if (stack.isDamageableItem() && player instanceof ServerPlayer) {
                stack.hurt(1, RandomSource.create(), (ServerPlayer) player);
            }
            return true;
        }

        // Consume items
        if (stack.is(consume)) {
            if (!player.isCreative()) {
                stack.grow(-1);
            }
            return true;
        }

        return false;
    }

    boolean canLight(Level world, BlockPos pos);
}
