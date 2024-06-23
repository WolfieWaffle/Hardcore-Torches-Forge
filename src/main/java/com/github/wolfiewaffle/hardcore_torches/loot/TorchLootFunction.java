package com.github.wolfiewaffle.hardcore_torches.loot;

import com.github.wolfiewaffle.hardcore_torches.MainMod;
import com.github.wolfiewaffle.hardcore_torches.block.AbstractHardcoreTorchBlock;
import com.github.wolfiewaffle.hardcore_torches.config.Config;
import com.github.wolfiewaffle.hardcore_torches.util.ETorchState;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.functions.SetStewEffectFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.List;

public class TorchLootFunction implements LootItemFunction {

    @Override
    public LootItemFunctionType getType() {
        return MainMod.HARDCORE_TORCH_LOOT_FUNCTION;
    }

    public static final Codec<TorchLootFunction> CODEC = Codec.unit(TorchLootFunction::new);

    @Override
    public ItemStack apply(ItemStack stack, LootContext context) {
        BlockEntity blockEntity = context.getParam(LootContextParams.BLOCK_ENTITY);
        BlockState state = context.getParam(LootContextParams.BLOCK_STATE);
        ItemStack itemStack = new ItemStack(state.getBlock().asItem());
        ETorchState torchState;
        ETorchState dropTorchState;

        // Non-fuel modifications
        if (state.getBlock() instanceof AbstractHardcoreTorchBlock) {
            torchState = ((AbstractHardcoreTorchBlock) state.getBlock()).burnState;
            dropTorchState = torchState;

            // If torches burn out when dropped
            if (Config.torchesBurnWhenDropped.get()) {
                if (dropTorchState != ETorchState.BURNT) {
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

}
