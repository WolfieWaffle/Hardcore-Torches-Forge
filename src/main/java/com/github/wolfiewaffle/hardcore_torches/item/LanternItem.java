package com.github.wolfiewaffle.hardcore_torches.item;

import com.github.wolfiewaffle.hardcore_torches.block.AbstractLanternBlock;
import com.github.wolfiewaffle.hardcore_torches.config.Config;
import com.github.wolfiewaffle.hardcore_torches.init.BlockInit;
import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.List;

public class LanternItem extends BlockItem {
    public boolean isLit;

    public LanternItem(Block block, Properties properties) {
        super(block, properties);
        this.isLit = ((AbstractLanternBlock) block).isLit;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable World world, List<ITextComponent> list, ITooltipFlag flag) {
        if (Config.lanternsNeedCan.get()) list.add(new StringTextComponent("Requires an Oil Can").withStyle(TextFormatting.GRAY));
        list.add(new StringTextComponent("Light with Flint and Steel").withStyle(TextFormatting.GRAY));
        super.appendHoverText(stack, world, list, flag);
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        return true;
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        double maxFuel = Config.defaultLanternFuel.get();
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

    public static int getFuel(ItemStack stack) {
        Item item = stack.getItem();
        if (!(item instanceof LanternItem)) return 0;

        CompoundNBT nbt = stack.getTag();
        int fuel;

        if (nbt != null && nbt.contains("Fuel")) {
            return nbt.getInt("Fuel");
        }

        return ((LanternItem) stack.getItem()).isLit ? Config.defaultLanternFuel.get(): 0;
    }

    public static ItemStack addFuel(ItemStack stack, World world, int amount) {

        if (stack.getItem() instanceof  LanternItem && !world.isClientSide) {
            LanternItem item = (LanternItem) stack.getItem();

            CompoundNBT nbt = stack.getTag();
            int fuel = item.isLit ? Config.defaultLanternFuel.get() : 0;

            if (nbt != null) {
                fuel = nbt.getInt("Fuel");
            } else {
                nbt = new CompoundNBT();
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
