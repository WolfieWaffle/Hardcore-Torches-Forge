package com.github.wolfiewaffle.hardcore_torches.loot;

import com.github.wolfiewaffle.hardcore_torches.MainMod;
import com.github.wolfiewaffle.hardcore_torches.block.AbstractHardcoreTorchBlock;
import com.github.wolfiewaffle.hardcore_torches.config.Config;
import com.github.wolfiewaffle.hardcore_torches.util.ETorchState;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.block.BlockState;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootFunction;
import net.minecraft.loot.LootFunctionType;
import net.minecraft.loot.LootParameters;
import net.minecraft.loot.conditions.ILootCondition;

public class TorchLootFunction extends LootFunction {

    public TorchLootFunction(ILootCondition[] lootConditions) {
        super(lootConditions);
    }

    @Override
    public LootFunctionType getType() {
        return MainMod.HARDCORE_TORCH_LOOT_FUNCTION;
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext context) {
        TileEntity blockEntity = context.getParamOrNull(LootParameters.BLOCK_ENTITY);
        BlockState state = context.getParamOrNull(LootParameters.BLOCK_STATE);
        ItemStack itemStack = new ItemStack(state.getBlock().asItem());
        ETorchState torchState;
        ETorchState dropTorchState;

        // Non-fuel modifications
        if (state.getBlock() instanceof AbstractHardcoreTorchBlock) {
            torchState = ((AbstractHardcoreTorchBlock) state.getBlock()).burnState;
            dropTorchState = torchState;

            // If torches burn out when dropped
            if (Config.torchesBurnWhenDropped.get()) {
                if (dropTorchState != ETorchState.BURNT && dropTorchState != ETorchState.UNLIT) {
                    dropTorchState = ETorchState.BURNT;
                }
            } else {
                // If torches extinguish when dropped
                if (Config.torchesExtinguishWhenBroken.get()) {
                    if (dropTorchState != ETorchState.BURNT) {
                        dropTorchState = ETorchState.UNLIT;
                    }
                }
            }

            // If smoldering, drop unlit
            if (dropTorchState == ETorchState.SMOLDERING) {
                dropTorchState = ETorchState.UNLIT;
            }

            // Set item stack
            if (Config.burntStick.get() && dropTorchState == ETorchState.BURNT) {
                // If burnt torches drop sticks
                itemStack = new ItemStack(Items.STICK);
            } else {
                itemStack = getChangedStack(state, dropTorchState);
            }
        }

        return itemStack;
    }

    private ItemStack getChangedStack(BlockState state, ETorchState torchState) {
        return new ItemStack(((AbstractHardcoreTorchBlock) state.getBlock()).group.getStandingTorch(torchState).asItem());
    }

    public static class Serializer extends LootFunction.Serializer<TorchLootFunction> {

        public TorchLootFunction deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, ILootCondition[] lootConditions) {
            return new TorchLootFunction(lootConditions);
        }
    }
}
