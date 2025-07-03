package com.github.wolfiewaffle.hardcore_torches.item;

import com.github.wolfiewaffle.hardcore_torches.blockentity.*;
import com.github.wolfiewaffle.hardcore_torches.component.DataTypes;
import com.github.wolfiewaffle.hardcore_torches.config.Config;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

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
        ItemStack stack1 = oldStack.copy();
        ItemStack stack2 = newStack.copy();

        stack1.remove(DataTypes.FUEL);
        stack2.remove(DataTypes.FUEL);

        return super.shouldCauseReequipAnimation(stack1, stack2, slotChanged);
    }
    // endregion

    // region Fuel Methods
    public static int getFuel(ItemStack stack) {
        Item item = stack.getItem();
        if (!(item instanceof OilCanItem)) return 0;

        return stack.getOrDefault(DataTypes.FUEL, 0);
    }

    public static ItemStack setFuel(ItemStack stack, int fuel) {
        if (stack.getItem() instanceof OilCanItem) {
            stack.set(DataTypes.FUEL, Math.max(0, Math.min(Config.maxCanFuel.get(), fuel)));
        }

        return stack;
    }

    public static ItemStack addFuel(ItemStack stack, int amount) {

        if (stack.getItem() instanceof OilCanItem) {
            int fuel = stack.getOrDefault(DataTypes.FUEL, 0);

            fuel = Math.min(Config.maxCanFuel.get(), Math.max(0, fuel + amount));

            stack.set(DataTypes.FUEL, fuel);
        }

        return stack;
    }

    public static boolean fuelBlock(IFuelBlockEntity be, Level world, ItemStack stack) {
        if (!world.isClientSide) {
            int maxFromCan = 0;

            // Max that can be applied to the block
            int maxFuel = be.getMaxFuel();
            maxFromCan = Math.max(0, maxFuel - be.getFuel());

            int taken = Math.min(maxFromCan, getFuel(stack));

            // Set the fuel values
            addFuel(stack, -taken);
            be.setFuel(be.getFuel() + taken);

            return taken > 0;
        }
        return false;
    }
    // endregion

//    @Override
//    public void fillItemCategory(CreativeModeTab tab, NonNullList<ItemStack> stacks) {
//        super.fillItemCategory(tab, stacks);
//
//        if (this.allowedIn(tab)) {
//            stacks.add(OilCanItem.setFuel(new ItemStack(this), Config.maxCanFuel.get()));
//        }
//    }
}
