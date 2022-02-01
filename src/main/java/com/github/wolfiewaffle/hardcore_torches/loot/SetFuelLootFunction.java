package com.github.wolfiewaffle.hardcore_torches.loot;

import com.github.wolfiewaffle.hardcore_torches.MainMod;
import com.github.wolfiewaffle.hardcore_torches.block.AbstractHardcoreTorchBlock;
import com.github.wolfiewaffle.hardcore_torches.block.AbstractLanternBlock;
import com.github.wolfiewaffle.hardcore_torches.blockentity.FuelBlockEntity;
import com.github.wolfiewaffle.hardcore_torches.init.ItemInit;
import com.github.wolfiewaffle.hardcore_torches.util.ETorchState;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class SetFuelLootFunction extends LootItemConditionalFunction {

    public SetFuelLootFunction(LootItemCondition[] lootConditions) {
        super(lootConditions);
    }

    @Override
    public LootItemFunctionType getType() {
        return MainMod.SET_FUEL_LOOT_FUNCTION;
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext context) {
        if (!(stack.getItem() instanceof BlockItem)) return stack; // No regular items

        BlockEntity blockEntity = context.getParam(LootContextParams.BLOCK_ENTITY);
        Block block = ((BlockItem) stack.getItem()).getBlock();

        if (block instanceof AbstractHardcoreTorchBlock || block instanceof AbstractLanternBlock) {

            // Set fuel
            if (blockEntity != null && blockEntity instanceof FuelBlockEntity) {
                int remainingFuel = ((FuelBlockEntity) blockEntity).getFuel();
                CompoundTag nbt = new CompoundTag();
                nbt.putInt("Fuel", (remainingFuel));
                stack.setTag(nbt);
            }

            if (block instanceof AbstractHardcoreTorchBlock && ((AbstractHardcoreTorchBlock) ((BlockItem) stack.getItem()).getBlock()).burnState == ETorchState.BURNT) {
                stack.removeTagKey("Fuel");
            }
        }

        return stack;
    }

    public static class Serializer extends LootItemConditionalFunction.Serializer<SetFuelLootFunction> {

        public SetFuelLootFunction deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootItemCondition[] lootConditions) {
            return new SetFuelLootFunction(lootConditions);
        }
    }
}
