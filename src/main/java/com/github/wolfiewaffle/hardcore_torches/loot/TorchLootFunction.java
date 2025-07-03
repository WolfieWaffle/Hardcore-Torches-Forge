package com.github.wolfiewaffle.hardcore_torches.loot;

import com.github.wolfiewaffle.hardcore_torches.HardcoreTorches;
import com.github.wolfiewaffle.hardcore_torches.block.AbstractHardcoreTorchBlock;
import com.github.wolfiewaffle.hardcore_torches.config.Config;
import com.github.wolfiewaffle.hardcore_torches.util.ETorchState;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.List;

public class TorchLootFunction extends LootItemConditionalFunction {

    public TorchLootFunction(List<LootItemCondition> lootConditions) {
        super(lootConditions);
    }

    public static final MapCodec<TorchLootFunction> CODEC = RecordCodecBuilder.mapCodec((instance) -> commonFields(instance).apply(instance, TorchLootFunction::new));

    @Override
    public LootItemFunctionType getType() {
        return HardcoreTorches.HARDCORE_TORCH_LOOT_FUNCTION;
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext context) {
        BlockEntity blockEntity = context.getParam(LootContextParams.BLOCK_ENTITY);
        BlockState state = context.getParam(LootContextParams.BLOCK_STATE);
        ItemStack itemStack = new ItemStack(state.getBlock().asItem());
        ETorchState torchState;
        ETorchState dropTorchState;

        // Non-fuel modifications
        if (state.getBlock() instanceof AbstractHardcoreTorchBlock torch) {
            torchState = torch.burnState;
            dropTorchState = torchState;

            // Soul torches work different
            if (torch.group == HardcoreTorches.soulTorches) return getSoulTorch(torch);

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
            if (dropTorchState == ETorchState.BURNT) {
                if (Config.burntDrop.get() == 0) {
                    itemStack = getChangedStack(state, dropTorchState);
                } else if (Config.burntDrop.get() == 1) {
                    itemStack = new ItemStack(Items.STICK, 1);
                } else {
                    return ItemStack.EMPTY;
                }
            } else {
                itemStack = getChangedStack(state, dropTorchState);
            }
        }

        return itemStack;
    }

    private ItemStack getSoulTorch(AbstractHardcoreTorchBlock torch) {
        return new ItemStack(torch.asItem());
    }

    private ItemStack getChangedStack(BlockState state, ETorchState torchState) {
        return new ItemStack(((AbstractHardcoreTorchBlock) state.getBlock()).group.getStandingTorch(torchState).asItem());
    }
}
