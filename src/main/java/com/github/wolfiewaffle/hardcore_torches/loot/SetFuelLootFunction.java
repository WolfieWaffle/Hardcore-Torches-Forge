package com.github.wolfiewaffle.hardcore_torches.loot;

import com.github.wolfiewaffle.hardcore_torches.MainMod;
import com.github.wolfiewaffle.hardcore_torches.block.AbstractHardcoreTorchBlock;
import com.github.wolfiewaffle.hardcore_torches.block.AbstractLanternBlock;
import com.github.wolfiewaffle.hardcore_torches.blockentity.FuelBlockEntity;
import com.github.wolfiewaffle.hardcore_torches.config.Config;
import com.github.wolfiewaffle.hardcore_torches.util.ETorchState;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootFunction;
import net.minecraft.loot.LootFunctionType;
import net.minecraft.loot.LootParameters;
import net.minecraft.loot.conditions.ILootCondition;

public class SetFuelLootFunction extends LootFunction {

    public SetFuelLootFunction(ILootCondition[] lootConditions) {
        super(lootConditions);
    }

    @Override
    public LootFunctionType getType() {
        return MainMod.SET_FUEL_LOOT_FUNCTION;
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext context) {
        if (!(stack.getItem() instanceof BlockItem)) return stack; // No regular items

        TileEntity blockEntity = context.getParamOrNull(LootParameters.BLOCK_ENTITY);
        Block block = ((BlockItem) stack.getItem()).getBlock();

        if (block instanceof AbstractHardcoreTorchBlock || block instanceof AbstractLanternBlock) {

            // Set fuel
            if (blockEntity != null && blockEntity instanceof FuelBlockEntity) {
                int remainingFuel = ((FuelBlockEntity) blockEntity).getFuel();

                if (remainingFuel != Config.defaultTorchFuel.get()) {
                    CompoundNBT nbt = new CompoundNBT();
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

    public static class Serializer extends LootFunction.Serializer<SetFuelLootFunction> {

        public SetFuelLootFunction deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, ILootCondition[] lootConditions) {
            return new SetFuelLootFunction(lootConditions);
        }
    }
}
