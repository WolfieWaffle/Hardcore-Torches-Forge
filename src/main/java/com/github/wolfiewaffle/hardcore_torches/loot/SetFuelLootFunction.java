package com.github.wolfiewaffle.hardcore_torches.loot;

import com.github.wolfiewaffle.hardcore_torches.MainMod;
import com.github.wolfiewaffle.hardcore_torches.block.AbstractHardcoreTorchBlock;
import com.github.wolfiewaffle.hardcore_torches.block.AbstractLanternBlock;
import com.github.wolfiewaffle.hardcore_torches.blockentity.FuelBlockEntity;
import com.github.wolfiewaffle.hardcore_torches.blockentity.IFuelBlock;
import com.github.wolfiewaffle.hardcore_torches.util.ETorchState;
import com.mojang.serialization.Codec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.jetbrains.annotations.NotNull;

public class SetFuelLootFunction implements LootItemFunction {

    @Override
    public @NotNull LootItemFunctionType getType() {
        return MainMod.SET_FUEL_LOOT_FUNCTION;
    }

    public static final Codec<SetFuelLootFunction> CODEC = Codec.unit(SetFuelLootFunction::new);

    @Override
    public ItemStack apply(ItemStack stack, LootContext context) {
        if (!(stack.getItem() instanceof BlockItem)) return stack; // No regular items

        BlockEntity blockEntity = context.getParam(LootContextParams.BLOCK_ENTITY);
        Block block = ((BlockItem) stack.getItem()).getBlock();

        if (block instanceof AbstractHardcoreTorchBlock || block instanceof AbstractLanternBlock) {

            // Set fuel
            if (blockEntity != null && blockEntity instanceof FuelBlockEntity) {
                int remainingFuel = ((FuelBlockEntity) blockEntity).getFuel();

                if (remainingFuel != ((IFuelBlock) block).getMaxFuel()) {
                    CompoundTag nbt = new CompoundTag();
                    nbt.putInt("Fuel", (remainingFuel));
                    stack.setTag(nbt);
                }
            }

            if (block instanceof AbstractHardcoreTorchBlock && ((AbstractHardcoreTorchBlock) ((BlockItem) stack.getItem()).getBlock()).burnState == ETorchState.BURNT) {
                stack.removeTagKey("Fuel");
            }
        }

        return stack;
    }
}
