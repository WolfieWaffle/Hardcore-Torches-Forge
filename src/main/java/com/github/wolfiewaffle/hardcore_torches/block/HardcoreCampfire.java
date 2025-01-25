package com.github.wolfiewaffle.hardcore_torches.block;

import com.github.wolfiewaffle.hardcore_torches.blockentity.HardcoreCampfireBlockEntity;
import com.github.wolfiewaffle.hardcore_torches.blockentity.IFuelBlock;
import com.github.wolfiewaffle.hardcore_torches.config.Config;
import com.github.wolfiewaffle.hardcore_torches.init.BlockEntityInit;
import com.github.wolfiewaffle.hardcore_torches.util.ETorchState;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.FlintAndSteelItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.function.ToIntFunction;

public class HardcoreCampfire extends CampfireBlock implements IFuelBlock {

    public HardcoreCampfire(boolean p_51236_, int p_51237_, Properties p_51238_) {
        super(p_51236_, p_51237_, p_51238_);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext p_51240_) {
        LevelAccessor levelaccessor = p_51240_.getLevel();
        BlockPos blockpos = p_51240_.getClickedPos();
        boolean flag = levelaccessor.getFluidState(blockpos).getType() == Fluids.WATER;
        return this.defaultBlockState().setValue(WATERLOGGED, Boolean.valueOf(flag)).setValue(SIGNAL_FIRE, Boolean.valueOf(this.isSmokeSource(levelaccessor.getBlockState(blockpos.below())))).setValue(LIT, false).setValue(FACING, p_51240_.getHorizontalDirection());
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack stack = player.getItemInHand(hand);

        // Override flint and steel. This may have to change if more blocks become able to light campfires.
        if (stack.getItem() instanceof FlintAndSteelItem) {
            if (state.getValue(CampfireBlock.LIT)) return super.use(state, world, pos, player, hand, hit);

            if (!world.isClientSide) {
                //canLight should maybe be static so I don't have to do this.
                Block block = state.getBlock();

                if (block instanceof HardcoreCampfire campfire && !campfire.canLight(world, pos)) {
                    if (campfire.getFuel(world, pos) <= 0) return needsFuel(player);
                } else if (CampfireBlock.canLight(state) && world.getBlockEntity(pos) instanceof HardcoreCampfireBlockEntity campfire) {
                    if (attemptUseItem(stack, player, hand, ETorchState.LIT)) {
                        light(world, pos, state);
                        player.swing(hand);
                        return InteractionResult.SUCCESS;
                    }
                }
            }

            return InteractionResult.CONSUME;
        }

        if (isValidStack(stack, getFreeLightItems(), getDamageLightItems(), getConsumeLightItems())) {
            if (state.getValue(CampfireBlock.LIT)) return super.use(state, world, pos, player, hand, hit);

            if (!world.isClientSide) {
                if (getFuel(world, pos) <= 0) {
                    return needsFuel(player);
                } else if (world.getBlockEntity(pos) instanceof HardcoreCampfireBlockEntity campfire) {
                    if (attemptUseItem(stack, player, hand, ETorchState.LIT)) {
                        light(world, pos, state);
                        player.swing(hand);
                        return InteractionResult.SUCCESS;
                    }
                }
            }

            return InteractionResult.CONSUME;
        }

        return super.use(state, world, pos, player, hand, hit);
    }

    // Added here because its private in CampfireBlock
    private boolean isSmokeSource(BlockState p_51324_) {
        return p_51324_.is(Blocks.HAY_BLOCK);
    }

    // Same here
    public static ToIntFunction<BlockState> litBlockEmission(int p_50760_) {
        return (p_50763_) -> {
            return p_50763_.getValue(BlockStateProperties.LIT) ? p_50760_ : 0;
        };
    }

    @Override
    public int getMaxFuel() {
        return Config.campfireMaxFuel.get(); // Set high cuz idk
    }

    @Override
    public void outOfFuel(Level world, BlockPos pos, BlockState state) {
        BlockState newState = state.setValue(CampfireBlock.LIT, false);
        world.setBlockAndUpdate(pos, newState);
    }

    public void light(Level world, BlockPos pos, BlockState state) {
        BlockState newState = state.setValue(CampfireBlock.LIT, true);
        world.setBlockAndUpdate(pos, newState);
        world.playSound(null, pos, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 1f, 1f);
    }

    @Override
    public boolean canLight(Level world, BlockPos pos) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity != null && blockEntity instanceof HardcoreCampfireBlockEntity campfire) {
            if (campfire.getFuel() <= 0) return false;
        }
        return canLight(world.getBlockState(pos));
    }

    public int getFuel(Level world, BlockPos pos) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity != null && blockEntity instanceof HardcoreCampfireBlockEntity campfire) {
            return campfire.getFuel();
        }
        return 0;
    }

    public InteractionResult needsFuel(Player player) {
        player.displayClientMessage(Component.literal("Drop combustible items on top to add fuel!"), true);
        return InteractionResult.CONSUME;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new HardcoreCampfireBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
        if (world.isClientSide) {
            return state.getValue(LIT) ? createTickerHelper(type, BlockEntityInit.CAMPFIRE_BLOCK_ENTITY.get(), HardcoreCampfireBlockEntity::clientTick) : null;
        } else {
            return state.getValue(LIT) ? createTickerHelper(type, BlockEntityInit.CAMPFIRE_BLOCK_ENTITY.get(), HardcoreCampfireBlockEntity::cookTick) : createTickerHelper(type, BlockEntityInit.CAMPFIRE_BLOCK_ENTITY.get(), HardcoreCampfireBlockEntity::cooldownTick);
        }
    }

    // These methods are needed for IFuelBlock

    @Override
    public boolean isSoulVariant() {
        return false;
    }

    // NOT USED FOR NOW
    @Override
    public ItemStack getStack(Level world, BlockPos pos) {
        return new ItemStack(world.getBlockState(pos).getBlock());
    }

    // NOT USED FOR NOW
    @Override
    public void extinguish(Level world, BlockPos pos, BlockState state, boolean playSound) {
        outOfFuel(world, pos, state);
    }

    // NOT USED FOR NOW
    @Override
    public void light(Level world, BlockPos pos) {

    }

    // NOT USED FOR NOW
    @Override
    public InteractionResult attemptLight(Level world, BlockPos pos, BlockState state, Player player, ItemStack stack, InteractionHand hand) {
        return InteractionResult.FAIL;
    }

    // NOT USED FOR NOW
    @Override
    public boolean isLit() {
        return false;
    }
}
