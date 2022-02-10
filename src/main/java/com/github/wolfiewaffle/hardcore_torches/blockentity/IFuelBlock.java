package com.github.wolfiewaffle.hardcore_torches.blockentity;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.ITag;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Random;

public interface IFuelBlock {

    void outOfFuel(World world, BlockPos pos, BlockState state);

    default boolean itemValid(ItemStack stack, ITag free, ITag damage, ITag consume) {
        Item item = stack.getItem();

        // Infinite items
        if (free.contains(item)) {
            return true;
        }

        // Durability items
        if (damage.contains(item)) {
            return true;
        }

        // Consume items
        if (consume.contains(item)) {
            return true;
        }

        return false;
    }

    default boolean attemptUse(ItemStack stack, PlayerEntity player, Hand hand, ITag free, ITag damage, ITag consume) {
        Item item = stack.getItem();

        // Infinite items
        if (free.contains(item)) {
            return true;
        }

        // Durability items
        if (damage.contains(item)) {
            if (stack.isDamageableItem() && player instanceof ServerPlayerEntity) {
                stack.hurt(1, new Random(), (ServerPlayerEntity) player);
            }
            return true;
        }

        // Consume items
        if (consume.contains(item)) {
            if (!player.isCreative()) {
                stack.grow(-1);
            }
            return true;
        }

        return false;
    }
}
