package com.github.wolfiewaffle.hardcore_torches.compat.farmersdelight;

import com.github.wolfiewaffle.hardcore_torches.config.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import vectorwing.farmersdelight.common.block.StoveBlock;

import javax.annotation.Nullable;

public class HardcoreStove extends StoveBlock {

    public HardcoreStove(Properties properties, boolean lit) {
        super(properties);
        registerDefaultState(this.defaultBlockState().setValue(LIT, lit));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite()).setValue(LIT, false);
    }

    @Override
    public ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        BlockEntity be = world.getBlockEntity(pos);
        Item heldItem = stack.getItem();

        if (be instanceof HardcoreStoveBlockEntity stove) {

            // Add fuel
            int burnTime = stack.getBurnTime(RecipeType.SMELTING);
            if (burnTime > 0) {
                if (stove.canAcceptFuel(burnTime)) {
                    stack.shrink(1);
                    stove.addFuel((int) (burnTime * Config.campfireFuelFactor.get()));
                    world.playSound(null, pos, SoundEvents.BONE_MEAL_USE, SoundSource.BLOCKS, 1f, 1f);
                }
                return ItemInteractionResult.SUCCESS;
            }

            // Fail to ignite
            if (stove.fuel <= 0) {
                if (heldItem == Items.FLINT_AND_STEEL || heldItem == Items.FIRE_CHARGE) {
                    if (!world.isClientSide) player.displayClientMessage(Component.literal("Right click with combustible items to add fuel!"), true);
                    return ItemInteractionResult.CONSUME;
                }
            }
        }

        return super.useItemOn(stack, state, world, pos, player, hand, hitResult);
    }

    @Override
    @Nullable
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return ((BlockEntityType)FarmersCommonCompat.STOVE_BLOCK_ENTITY.get()).create(pos, state);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return state.getValue(LIT) ? createTickerHelper(blockEntityType, FarmersCommonCompat.STOVE_BLOCK_ENTITY.get(), level.isClientSide ? HardcoreStoveBlockEntity::animationTick : HardcoreStoveBlockEntity::serverTick) : null;
    }
}
