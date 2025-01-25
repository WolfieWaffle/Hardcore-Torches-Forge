package com.github.wolfiewaffle.hardcore_torches.util;

import com.github.wolfiewaffle.hardcore_torches.MainMod;
import com.github.wolfiewaffle.hardcore_torches.block.AbstractLanternBlock;
import com.github.wolfiewaffle.hardcore_torches.blockentity.IFuelBlock;
import com.github.wolfiewaffle.hardcore_torches.blockentity.IFuelBlockEntity;
import com.github.wolfiewaffle.hardcore_torches.config.Config;
import com.github.wolfiewaffle.hardcore_torches.init.ItemInit;
import com.github.wolfiewaffle.hardcore_torches.item.OilCanItem;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class LanternTools {

    public static InteractionResult basicAttemptLight(Level world, BlockPos pos, Player player, ItemStack stack, InteractionHand hand) {

        // Soul lanterns
        boolean isSoul = false;
        if (world.getBlockState(pos).getBlock() instanceof IFuelBlock lantern) {
            isSoul = lantern.isSoulVariant();
        }

        if (world.getBlockState(pos).getBlock() instanceof IFuelBlock block) {
            if (world.getBlockEntity(pos) instanceof IFuelBlockEntity fuelBlockEntity) {
                // If not enough fuel to light
                if (!world.isClientSide) {
                    if (fuelBlockEntity.getFuel() < Config.minLanternIgnitionFuel.get()) {
                        world.playSound(null, pos, SoundEvents.LANTERN_HIT, SoundSource.BLOCKS, 1f, 1f);
                        if (!isSoul) player.displayClientMessage(Component.literal("Needs fuel from an Oil Can"), true);
                        else player.displayClientMessage(Component.literal("Needs XP from an Amethyst Shard"), true);
                    } else if (block.attemptUseItem(stack, player, hand, ETorchState.LIT)) {
                        block.light(world, pos);
                    }
                }
            }
        }

        player.swing(hand);
        return InteractionResult.SUCCESS;
    }

    public static InteractionResult interactLantern(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand) {

        // Get variables
        ItemStack stack = player.getItemInHand(hand);

        if (!(world.getBlockEntity(pos) instanceof IFuelBlockEntity)) return InteractionResult.FAIL;
        IFuelBlockEntity fuelBlockEntity = (IFuelBlockEntity) world.getBlockEntity(pos);

        if (!(state.getBlock() instanceof IFuelBlock)) return InteractionResult.FAIL;
        IFuelBlock block = (IFuelBlock) state.getBlock();

        // Soul lanterns
        boolean isSoul = false;
        if (state.getBlock() instanceof IFuelBlock lantern) {
            isSoul = lantern.isSoulVariant();
        }

        // Pick up lantern
        if (player.isCrouching() && Config.pickUpLanterns.get()) {
            if (!world.isClientSide) {
                player.addItem(block.getStack(world, pos));
                world.playSound(null, pos, SoundEvents.LANTERN_PLACE, SoundSource.BLOCKS, 1f, 1f);
            }

            world.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
            player.swing(hand);
            return InteractionResult.SUCCESS;
        }

        // Igniting
        if (!block.isLit() && block.itemValid(stack, ETorchState.LIT)) {
            return block.attemptLight(world, pos, state, player, stack, hand);
        }

        // Hand extinguish
        if (Config.handUnlightLantern.get() && block.isLit()) {
            if (!TorchTools.canLight(stack.getItem(), state.getBlock().defaultBlockState())) {
                block.extinguish(world, pos, state, true);
                return InteractionResult.SUCCESS;
            }
        }

        // Fuel message
        boolean showFuel = (stack.isEmpty() || stack.getItem() == ItemInit.OIL_CAN.get()) && Config.fuelMessage.get();
        if (hand == InteractionHand.MAIN_HAND && !world.isClientSide && showFuel) {
            player.displayClientMessage(Component.literal("Fuel: " + fuelBlockEntity.getFuel()), true);
        }

        // Soul lantern handling
        if (isSoul) {
            return SoulAttunement.soulAttune(world, pos, fuelBlockEntity, block, player, hand);
        }

        // Adding fuel with coal if enabled
        if (stack.is(ItemTags.COALS) && !Config.lanternsNeedCan.get()) {
            if (!world.isClientSide) {
                int oldFuel = fuelBlockEntity.getFuel();

                // Add fuel if more can be added
                if (fuelBlockEntity.canAddFuel()) {
                    if (oldFuel + Config.defLanternFuelItem.get() < block.getMaxFuel()) {
                        world.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1f, 1f);
                    } else {
                        world.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1f, 1f);
                    }
                    stack.grow(-1);
                    fuelBlockEntity.setFuel(Math.min(oldFuel + Config.defLanternFuelItem.get(), block.getMaxFuel()));
                }
            }
            player.swing(hand);
            return InteractionResult.SUCCESS;
        }

        // Adding fuel with can
        if (stack.getItem() instanceof OilCanItem && Config.lanternsNeedCan.get()) {
            if (!world.isClientSide) {
                if (OilCanItem.fuelBlock(fuelBlockEntity, world, stack)) {
                    world.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1f, 1f);
                }
            }
            player.swing(hand);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }
}
