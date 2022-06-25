package com.github.wolfiewaffle.hardcore_torches.item;

import com.github.wolfiewaffle.hardcore_torches.blockentity.FuelBlockEntity;
import com.github.wolfiewaffle.hardcore_torches.blockentity.IFuelBlock;
import com.github.wolfiewaffle.hardcore_torches.blockentity.LanternBlockEntity;
import com.github.wolfiewaffle.hardcore_torches.blockentity.TorchBlockEntity;
import com.github.wolfiewaffle.hardcore_torches.config.Config;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.awt.*;

public class OilCanItem extends Item {

    public OilCanItem(Properties properties) {
        super(properties);
    }

    // region Fuel Bar
    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        int maxFuel = Config.maxCanFuel.get();
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
    // endregion

    // region Fuel Methods
    public static int getFuel(ItemStack stack) {
        Item item = stack.getItem();
        if (!(item instanceof OilCanItem)) return 0;

        CompoundTag nbt = stack.getTag();

        if (nbt != null && nbt.contains("Fuel")) {
            return nbt.getInt("Fuel");
        }

        return 0;
    }

    public static ItemStack setFuel(ItemStack stack, int fuel) {
        if (stack.getItem() instanceof OilCanItem) {
            CompoundTag nbt = stack.getTag();

            if (nbt == null) nbt = new CompoundTag();

            nbt.putInt("Fuel", Math.max(0, Math.min(Config.maxCanFuel.get(), fuel)));
            stack.setTag(nbt);
        }

        return stack;
    }

    public static ItemStack addFuel(ItemStack stack, int amount) {

        if (stack.getItem() instanceof OilCanItem) {
            CompoundTag nbt = stack.getTag();
            int fuel = 0;

            if (nbt != null) {
                fuel = nbt.getInt("Fuel");
            } else {
                nbt = new CompoundTag();
            }

            fuel = Math.min(Config.maxCanFuel.get(), Math.max(0, fuel + amount));

            nbt.putInt("Fuel", fuel);
            stack.setTag(nbt);
        }

        return stack;
    }

    public static boolean fuelBlock(FuelBlockEntity be, Level world, ItemStack stack) {
        if (!world.isClientSide) {
            int maxFromCan = 0;

            // Max that can be applied to the block
            Block block = be.getBlockState().getBlock();
            if (block instanceof IFuelBlock) {
                int maxFuel = ((IFuelBlock) block).getMaxFuel();
                maxFromCan = Math.max(0, maxFuel - be.getFuel());
            }

            int taken = Math.min(maxFromCan, getFuel(stack));

            // Set the fuel values
            addFuel(stack, -taken);
            be.setFuel(be.getFuel() + taken);

            return taken > 0;
        }
        return false;
    }
    // endregion

    @Override
    public void fillItemCategory(CreativeModeTab tab, NonNullList<ItemStack> stacks) {
        super.fillItemCategory(tab, stacks);

        if (this.allowedIn(tab)) {
            stacks.add(OilCanItem.setFuel(new ItemStack(this), Config.maxCanFuel.get()));
        }
    }
}
