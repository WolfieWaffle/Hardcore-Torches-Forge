package com.github.wolfiewaffle.hardcore_torches.item;

import com.github.wolfiewaffle.hardcore_torches.MainMod;
import com.github.wolfiewaffle.hardcore_torches.block.AbstractHardcoreTorchBlock;
import com.github.wolfiewaffle.hardcore_torches.config.Config;
import com.github.wolfiewaffle.hardcore_torches.util.ETorchState;
import com.github.wolfiewaffle.hardcore_torches.util.TorchGroup;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.awt.*;

public class TorchItem extends StandingAndWallBlockItem {
    public ETorchState burnState;
    TorchGroup torchGroup;

    public TorchItem(Block floorBlock, Block wallBlock, Properties properties) {
        super(floorBlock, wallBlock, properties);
        this.burnState = ((AbstractHardcoreTorchBlock) getBlock()).burnState;
        this.torchGroup = ((AbstractHardcoreTorchBlock) getBlock()).group;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        int fuel = getFuel(stack);

        if (fuel > 0 && fuel < Config.defaultTorchFuel.get()) {
            return true;
        }

        return false;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        int maxFuel = Config.defaultTorchFuel.get();
        int fuel = getFuel(stack);

        if (maxFuel != 0) {
            return Math.round(13.0f - (maxFuel - fuel) * 13.0f / maxFuel);
        }

        return 0;
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return Color.HSBtoRGB(0.5f, 1.0f, 1.0f);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        ItemStack cStack = context.getItemInHand();
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();

        // Make sure it's a torch and get its type
        if (cStack.getItem() instanceof TorchItem) {
            ETorchState torchState = ((TorchItem) cStack.getItem()).burnState;
            Block block = world.getBlockState(pos).getBlock();

            if (torchState == ETorchState.UNLIT || torchState == ETorchState.SMOLDERING) {

                // Unlit and Smoldering
                if (MainMod.FREE_TORCH_LIGHT_BLOCKS.contains(block)) {
                    Player player = context.getPlayer();
                    if (player != null && !world.isClientSide)
                        player.setItemInHand(context.getHand(), stateStack(cStack, ETorchState.LIT));
                    if (!world.isClientSide) world.playSound(null, pos, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 0.5f, 1.2f);
                    return InteractionResult.SUCCESS;
                }
            }
        }

        return super.useOn(context);
    }

    /** Gets a stack of a torch but with a different burn state **/
    public static ItemStack stateStack(ItemStack inputStack, ETorchState newState) {
        ItemStack outputStack = ItemStack.EMPTY;

        if (inputStack.getItem() instanceof BlockItem && inputStack.getItem() instanceof TorchItem) {
            AbstractHardcoreTorchBlock newBlock = (AbstractHardcoreTorchBlock) ((BlockItem)inputStack.getItem()).getBlock();
            TorchItem newItem = (TorchItem) newBlock.group.getStandingTorch(newState).asItem();

            outputStack = changedCopy(inputStack, newItem);
            if (newState == ETorchState.BURNT) outputStack.setTag(null);
        }

        return outputStack;
    }

    /** Copy of a stack with a new Item **/
    public static ItemStack changedCopy(ItemStack stack, Item replacementItem) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack itemStack = new ItemStack(replacementItem, stack.getCount());
        if (stack.getTag() != null) {
            itemStack.setTag(stack.getTag().copy());
        }
        return itemStack;
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        CompoundTag oldNbt = null;
        CompoundTag newNbt = null;

        if (oldStack.getTag() != null) {
            oldNbt = oldStack.getTag().copy();
            oldNbt.remove("Fuel");
        }

        if (newStack.getTag() != null) {
            newNbt = newStack.getTag().copy();
            newNbt.remove("Fuel");
        }

        if (oldNbt == null && newNbt != null) return true;
        if (oldNbt != null && newNbt == null) return true;
        if (oldNbt == null && newNbt == null) return false;

        return oldNbt.equals(null);
    }

    public static int getFuel(ItemStack stack) {
        CompoundTag nbt = stack.getTag();
        int fuel;

        if (nbt != null && nbt.contains("Fuel")) {
            return nbt.getInt("Fuel");
        }

        return Config.defaultTorchFuel.get();
    }

    public boolean sameTorchGroup(TorchItem item1, TorchItem item2) {
        if (item1.torchGroup == item2.torchGroup) {
            return true;
        }
        return false;
    }

    public static ItemStack addFuel(ItemStack stack, Level world, int amount) {

        if (stack.getItem() instanceof  TorchItem && !world.isClientSide) {
            CompoundTag nbt = stack.getTag();
            int fuel = Config.defaultTorchFuel.get();

            if (nbt != null) {
                fuel = nbt.getInt("Fuel");
            } else {
                nbt = new CompoundTag();
            }

            fuel += amount;

            // If burn out
            if (fuel <= 0) {
                if (Config.burntStick.get()) {
                    stack = new ItemStack(Items.STICK, stack.getCount());
                } else {
                    stack = stateStack(stack, ETorchState.BURNT);
                }
            } else {
                if (fuel > Config.defaultTorchFuel.get()) {
                    fuel = Config.defaultTorchFuel.get();
                }

                nbt.putInt("Fuel", fuel);
                stack.setTag(nbt);
            }
        }

        return stack;
    }



//    @Override
//    public boolean overrideStackedOnOther(ItemStack stack, Slot slot, ClickAction clickAction, Player player) {
//        ItemStack otherStack = slot.getItem();
//
//        // If you are clicking on it with a non HCTorch item or with empty, use vanilla behavior
//        if (!slot.allowModification(player) || !(otherStack.getItem() instanceof TorchItem) || otherStack.isEmpty()) {
//            return super.overrideStackedOnOther(stack, slot, clickAction, player);
//        }
//
//        // Return left click if either is full
//        if (stack.getCount() >= stack.getMaxStackSize() || otherStack.getCount() >= otherStack.getMaxStackSize()) {
//            return false;
//        }
//
//        // Ensure torches are in same group
//        if (!sameTorchGroup((TorchItem) stack.getItem(), (TorchItem) otherStack.getItem())) {
//            return false;
//        }
//
//        if (((TorchItem) stack.getItem()).burnState == ETorchState.LIT) {
//            // If clicked is lit, return if clicked with burnt
//            if (((TorchItem) otherStack.getItem()).burnState == ETorchState.BURNT) {
//                return false;
//            }
//        } else if (((TorchItem) stack.getItem()).burnState == ETorchState.UNLIT) {
//            // If clicked is unlit, return if clicked is not unlit
//            if (((TorchItem) otherStack.getItem()).burnState != ETorchState.UNLIT) {
//                return false;
//            }
//        }
//
//        if (!otherStack.isEmpty()) {
//            int max = stack.getMaxStackSize();
//            int usedCount = stack.getCount(); //clickType != ClickType.RIGHT ? otherStack.getCount() : 1;
//            int otherMax = otherStack.getMaxStackSize();
//
//            int remainder = Math.max(0, usedCount - (max - stack.getCount()));
//            int addedNew = usedCount - remainder;
//
//            // Average both stacks
//            int stack1Fuel = getFuel(stack) * stack.getCount();
//            int stack2Fuel = getFuel(otherStack) * addedNew;
//            int totalFuel = stack1Fuel + stack2Fuel;
//
//            // NBT
//            CompoundTag nbt = new CompoundTag();
//            nbt.putInt("Fuel", totalFuel / (stack.getCount() + addedNew));
//
//            if (addedNew > 0) {
//                stack.grow(-addedNew);
//                stack.setTag(nbt);
//                otherStack.setCount(otherStack.getCount() - addedNew);
//                return true;
//            }
//        }
//        return super.overrideStackedOnOther(stack, slot, clickAction, player);
//    }
}
