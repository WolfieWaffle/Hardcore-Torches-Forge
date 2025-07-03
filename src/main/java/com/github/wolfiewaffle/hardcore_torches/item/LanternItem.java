package com.github.wolfiewaffle.hardcore_torches.item;

import com.github.wolfiewaffle.hardcore_torches.HardcoreTorches;
import com.github.wolfiewaffle.hardcore_torches.block.AbstractLanternBlock;
import com.github.wolfiewaffle.hardcore_torches.compat.amendments.AmendmentsCommonCompat;
import com.github.wolfiewaffle.hardcore_torches.component.DataTypes;
import com.github.wolfiewaffle.hardcore_torches.config.Config;
import net.mehvahdjukaar.amendments.common.block.WallLanternBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.fml.ModList;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.function.IntSupplier;

public class LanternItem extends BlockItem {
    public boolean isLit;
    public IntSupplier maxFuel;
    private AbstractLanternBlock lanternBlock;

    public LanternItem(Block block, Properties properties) {
        super(block, properties);
        this.isLit = ((AbstractLanternBlock)block).isLit;
        this.maxFuel = ((AbstractLanternBlock)block).maxFuel;
        if (block instanceof AbstractLanternBlock) {
            this.lanternBlock = (AbstractLanternBlock)block;
        }

    }

    public int getMaxFuel() {
        return this.maxFuel.getAsInt();
    }

    @Nullable
    protected BlockState getPlacementState(BlockPlaceContext context) {
        if (ModList.get().isLoaded("amendments")) {
            if (context.replacingClickedOnBlock()) {
                return super.getPlacementState(context);
            }

            if (context.getClickedFace() != Direction.DOWN && context.getClickedFace() != Direction.UP) {
                BlockState state;
                if (this.lanternBlock.group == HardcoreTorches.basicLanterns) {
                    if (this.lanternBlock.isLit) {
                        state = AmendmentsCommonCompat.LIT_WALL_LANTERN.get().defaultBlockState();
                    } else {
                        state = AmendmentsCommonCompat.UNLIT_WALL_LANTERN.get().defaultBlockState();
                    }
                } else if (this.lanternBlock.isLit) {
                    state = AmendmentsCommonCompat.LIT_WALL_SOUL_LANTERN.get().defaultBlockState();
                } else {
                    state = AmendmentsCommonCompat.UNLIT_WALL_SOUL_LANTERN.get().defaultBlockState();
                }

                state = state.setValue(WallLanternBlock.FACING, context.getClickedFace());
                BlockPos pos = context.getClickedPos().relative(context.getClickedFace().getOpposite());
                boolean canSurvive = context.getLevel().getBlockState(pos).isFaceSturdy(context.getLevel(), pos, context.getClickedFace());
                if (canSurvive) {
                    return state;
                }

                return null;
            }
        }

        return super.getPlacementState(context);
    }

    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    public int getBarWidth(ItemStack stack) {
        int fuel = getFuel(stack);
        int max = this.getMaxFuel();
        return max != 0 ? Math.round(13.0F - (float)(max - fuel) * 13.0F / (float)max) : 0;
    }

    public int getBarColor(ItemStack stack) {
        return Color.HSBtoRGB(0.5F, 1.0F, 1.0F);
    }

    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        ItemStack stack1 = oldStack.copy();
        ItemStack stack2 = newStack.copy();

        stack1.remove(DataTypes.FUEL);
        stack2.remove(DataTypes.FUEL);

        return super.shouldCauseReequipAnimation(stack1, stack2, slotChanged);
    }

    public static int getFuel(ItemStack stack) {
        Item item = stack.getItem();
        if (!(item instanceof LanternItem)) {
            return 0;
        } else {
            LanternItem lanternItem = (LanternItem)item;
            int startingFuel = Config.startingLanternFuel.get();
            if (lanternItem.lanternBlock.group == HardcoreTorches.soulLanterns) {
                startingFuel = 0;
            }

            int def = lanternItem.isLit ? lanternItem.getMaxFuel() : startingFuel;

            return stack.getOrDefault(DataTypes.FUEL, def);
        }
    }

    public static ItemStack addFuel(ItemStack stack, Level world, int amount) {
        Item item = stack.getItem();
        int maxFuel;
        if (item instanceof LanternItem) {
            maxFuel = ((LanternItem)item).getMaxFuel();
        } else {
            maxFuel = 0;
        }

        if (stack.getItem() instanceof LanternItem lanternItem && !world.isClientSide) {
            int startingFuel = Config.startingLanternFuel.get();
            if (lanternItem.lanternBlock.group == HardcoreTorches.soulLanterns) {
                startingFuel = 0;
            }

            int def = lanternItem.isLit ? lanternItem.getMaxFuel() : startingFuel;

            int fuel = stack.getOrDefault(DataTypes.FUEL, def);

            fuel += amount;
            if (fuel <= 0) {
                stack = stateStack(stack, false);
            } else {
                if (fuel > maxFuel) {
                    fuel = maxFuel;
                }

                stack.set(DataTypes.FUEL, fuel);
            }
        }

        return stack;
    }

    public static ItemStack stateStack(ItemStack inputStack, boolean isLit) {
        ItemStack outputStack = ItemStack.EMPTY;
        if (inputStack.getItem() instanceof BlockItem && inputStack.getItem() instanceof LanternItem) {
            LanternItem newItem = (LanternItem)((LanternItem)inputStack.getItem()).lanternBlock.group.getLanternBlock(isLit).asItem();
            outputStack = new ItemStack(newItem, inputStack.getCount());
            if (inputStack.getComponents() != null && !inputStack.getComponents().isEmpty()) {
                outputStack.applyComponents(inputStack.copy().getComponents());
            }
        }

        return outputStack;
    }
}
