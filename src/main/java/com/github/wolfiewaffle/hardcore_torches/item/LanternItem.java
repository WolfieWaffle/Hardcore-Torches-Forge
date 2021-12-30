package com.github.wolfiewaffle.hardcore_torches.item;

import com.github.wolfiewaffle.hardcore_torches.block.AbstractLanternBlock;
import com.github.wolfiewaffle.hardcore_torches.config.Config;
import com.github.wolfiewaffle.hardcore_torches.init.BlockInit;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.awt.*;

public class LanternItem extends BlockItem {
    public boolean isLit;

    public LanternItem(Block block, Properties properties) {
        super(block, properties);
        this.isLit = ((AbstractLanternBlock) block).isLit;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        int fuel = getFuel(stack);

        if (fuel > 0 && fuel < Config.defaultLanternFuel.get()) {
            return true;
        }

        return false;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        int maxFuel = Config.defaultLanternFuel.get();
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
        Item item = stack.getItem();
        if (!(item instanceof LanternItem)) return 0;

        CompoundTag nbt = stack.getTag();
        int fuel;

        if (nbt != null && nbt.contains("Fuel")) {
            return nbt.getInt("Fuel");
        }

        return ((LanternItem) stack.getItem()).isLit ? Config.defaultLanternFuel.get(): 0;
    }

    public static ItemStack addFuel(ItemStack stack, Level world, int amount) {

        if (stack.getItem() instanceof  LanternItem && !world.isClientSide) {
            LanternItem item = (LanternItem) stack.getItem();

            CompoundTag nbt = stack.getTag();
            int fuel = item.isLit ? Config.defaultLanternFuel.get() : 0;

            if (nbt != null) {
                fuel = nbt.getInt("Fuel");
            } else {
                nbt = new CompoundTag();
            }

            fuel += amount;

            // If burn out
            if (fuel <= 0) {
                stack = stateStack(stack, false);
            } else {
                if (fuel > Config.defaultLanternFuel.get()) {
                    fuel = Config.defaultLanternFuel.get();
                }

                nbt.putInt("Fuel", fuel);
                stack.setTag(nbt);
            }
        }

        return stack;
    }

    public static ItemStack stateStack(ItemStack inputStack, boolean isLit) {
        ItemStack outputStack = ItemStack.EMPTY;

        if (inputStack.getItem() instanceof BlockItem && inputStack.getItem() instanceof LanternItem) {
            LanternItem newItem = (LanternItem) (isLit ? BlockInit.LIT_LANTERN.get().asItem() : BlockInit.UNLIT_LANTERN.get().asItem());

            outputStack = new ItemStack(newItem, inputStack.getCount());

            if (inputStack.getTag() != null) {
                outputStack.setTag(inputStack.getTag().copy());
            }
        }

        return outputStack;
    }
}
