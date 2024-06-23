package com.github.wolfiewaffle.hardcore_torches.item;

import com.github.wolfiewaffle.hardcore_torches.MainMod;
import com.github.wolfiewaffle.hardcore_torches.block.AbstractLanternBlock;
import com.github.wolfiewaffle.hardcore_torches.block.LanternBlock;
import com.github.wolfiewaffle.hardcore_torches.compat.curio.LanternCurioProvider;
import com.github.wolfiewaffle.hardcore_torches.config.Config;
import com.github.wolfiewaffle.hardcore_torches.init.BlockInit;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.ModList;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.type.capability.ICurio;

import java.awt.*;
import java.util.List;
import java.util.function.IntSupplier;

public class LanternItem extends BlockItem {
    public static Capability<ICurio> CURIO_CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});
    public boolean isLit;
    public IntSupplier maxFuel;
    private AbstractLanternBlock lanternBlock;

    public LanternItem(Block block, Properties properties) {
        super(block, properties);
        this.isLit = ((AbstractLanternBlock) block).isLit;
        this.maxFuel = ((AbstractLanternBlock) block).maxFuel;
        if (block instanceof AbstractLanternBlock) lanternBlock = (AbstractLanternBlock) block;
    }

    public int getMaxFuel() {
        return maxFuel.getAsInt();
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {

        if (ModList.get().isLoaded("curios")) {
            return new LanternCurioProvider(stack);
        }

        return null;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        int fuel = getFuel(stack);
        int max = getMaxFuel();

        if (max != 0) {
            return Math.round(13.0f - (max - fuel) * 13.0f / max);
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

        if (nbt != null && nbt.contains("Fuel")) {
            return nbt.getInt("Fuel");
        } else {
            LanternItem lanternItem = ((LanternItem) item);
            int startingFuel = Config.startingLanternFuel.get();
            if (lanternItem.lanternBlock.group == MainMod.soulLanterns) startingFuel = 0;

            return lanternItem.isLit ? lanternItem.getMaxFuel() : startingFuel;
        }
    }

    public static ItemStack addFuel(ItemStack stack, Level world, int amount) {
        int maxFuel;
        Item item = stack.getItem();
        if (item instanceof LanternItem) {
            maxFuel = ((LanternItem) item).getMaxFuel();
        } else {
            maxFuel = 0;
        }

        if (stack.getItem() instanceof  LanternItem && !world.isClientSide) {
            LanternItem lanternItem = (LanternItem) item;

            CompoundTag nbt = stack.getTag();
            int fuel = lanternItem.isLit ? maxFuel : 0;

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
                if (fuel > maxFuel) {
                    fuel = maxFuel;
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
            LanternItem newItem = (LanternItem) ((LanternItem) inputStack.getItem()).lanternBlock.group.getLanternBlock(isLit).asItem();

            outputStack = new ItemStack(newItem, inputStack.getCount());

            if (inputStack.getTag() != null) {
                outputStack.setTag(inputStack.getTag().copy());
            }
        }

        return outputStack;
    }
}
