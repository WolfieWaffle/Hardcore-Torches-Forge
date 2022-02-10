package com.github.wolfiewaffle.hardcore_torches.item;

import com.github.wolfiewaffle.hardcore_torches.blockentity.FuelBlockEntity;
import com.github.wolfiewaffle.hardcore_torches.blockentity.LanternBlockEntity;
import com.github.wolfiewaffle.hardcore_torches.blockentity.TorchBlockEntity;
import com.github.wolfiewaffle.hardcore_torches.config.Config;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;

import java.awt.*;

public class OilCanItem extends Item {

    public OilCanItem(Properties properties) {
        super(properties);
    }

    // region Fuel Bar
    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        return true;
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        double maxFuel = Config.maxCanFuel.get();
        double fuel = getFuel(stack);
        if (maxFuel != 0) {
            return 1.0 - (fuel / maxFuel);
        }

        return 0;
    }

    @Override
    public int getRGBDurabilityForDisplay(ItemStack stack) {
        return Color.HSBtoRGB(0.5f, 1.0f, 1.0f);
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        CompoundNBT oldNbt = null;
        CompoundNBT newNbt = null;

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

        CompoundNBT nbt = stack.getTag();

        if (nbt != null && nbt.contains("Fuel")) {
            return nbt.getInt("Fuel");
        }

        return 0;
    }

    public static ItemStack setFuel(ItemStack stack, int fuel) {
        if (stack.getItem() instanceof OilCanItem) {
            CompoundNBT nbt = stack.getTag();

            if (nbt == null) nbt = new CompoundNBT();

            nbt.putInt("Fuel", Math.max(0, Math.min(Config.maxCanFuel.get(), fuel)));
            stack.setTag(nbt);
        }

        return stack;
    }

    public static ItemStack addFuel(ItemStack stack, int amount) {

        if (stack.getItem() instanceof OilCanItem) {
            CompoundNBT nbt = stack.getTag();
            int fuel = 0;

            if (nbt != null) {
                fuel = nbt.getInt("Fuel");
            } else {
                nbt = new CompoundNBT();
            }

            fuel = Math.min(Config.maxCanFuel.get(), Math.max(0, fuel + amount));

            nbt.putInt("Fuel", fuel);
            stack.setTag(nbt);
        }

        return stack;
    }

    public static boolean fuelBlock(FuelBlockEntity be, World world, ItemStack stack) {
        if (!world.isClientSide) {
            int maxTaken = 0;

            // Lanterns
            if (be instanceof LanternBlockEntity) {
                maxTaken = Math.max(0, Config.defaultLanternFuel.get() - be.getFuel());
            }

            // Torches
            if (be instanceof TorchBlockEntity) {
                maxTaken = Math.max(0, Config.defaultTorchFuel.get() - be.getFuel());
            }

            int taken = Math.min(maxTaken, getFuel(stack));

            // Set the fuel values
            addFuel(stack, -taken);
            be.setFuel(be.getFuel() + taken);

            return taken > 0;
        }
        return false;
    }
    // endregion

    @Override
    public void fillItemCategory(ItemGroup tab, NonNullList<ItemStack> stacks) {
        super.fillItemCategory(tab, stacks);

        if (this.allowdedIn(tab)) {
            stacks.add(OilCanItem.setFuel(new ItemStack(this), Config.maxCanFuel.get()));
        }
    }
}
