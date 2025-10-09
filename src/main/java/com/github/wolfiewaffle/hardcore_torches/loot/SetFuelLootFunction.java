package com.github.wolfiewaffle.hardcore_torches.loot;

import com.github.wolfiewaffle.hardcore_torches.HardcoreTorches;
import com.github.wolfiewaffle.hardcore_torches.block.AbstractHardcoreTorchBlock;
import com.github.wolfiewaffle.hardcore_torches.block.AbstractLanternBlock;
import com.github.wolfiewaffle.hardcore_torches.blockentity.FuelBlockEntity;
import com.github.wolfiewaffle.hardcore_torches.blockentity.IFuelBlock;
import com.github.wolfiewaffle.hardcore_torches.component.DataTypes;
import com.github.wolfiewaffle.hardcore_torches.util.ETorchState;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.List;

public class SetFuelLootFunction extends LootItemConditionalFunction {

    public SetFuelLootFunction(List<LootItemCondition> lootConditions) {
        super(lootConditions);
    }

    public static final MapCodec<SetFuelLootFunction> CODEC = RecordCodecBuilder.mapCodec((instance) -> commonFields(instance).apply(instance, SetFuelLootFunction::new));

    @Override
    public LootItemFunctionType getType() {
        return HardcoreTorches.SET_FUEL_LOOT_FUNCTION;
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext context) {
        if (!(stack.getItem() instanceof BlockItem)) return stack; // No regular items
        if (!context.hasParam(LootContextParams.BLOCK_ENTITY)) return stack; // If no block entity, can't set fuel

        BlockEntity blockEntity = context.getParam(LootContextParams.BLOCK_ENTITY);
        Block block = ((BlockItem) stack.getItem()).getBlock();

        if (block instanceof AbstractHardcoreTorchBlock || block instanceof AbstractLanternBlock) {

            // Set fuel
            if (blockEntity != null && blockEntity instanceof FuelBlockEntity) {
                int remainingFuel = ((FuelBlockEntity) blockEntity).getFuel();

                if (remainingFuel != ((IFuelBlock) block).getMaxFuel()) {
                    stack.set(DataTypes.FUEL, remainingFuel);
                }
            }

            if (block instanceof AbstractHardcoreTorchBlock && ((AbstractHardcoreTorchBlock) ((BlockItem) stack.getItem()).getBlock()).burnState == ETorchState.BURNT) {
                stack.remove(DataTypes.FUEL);
            }
        }

        return stack;
    }
}
