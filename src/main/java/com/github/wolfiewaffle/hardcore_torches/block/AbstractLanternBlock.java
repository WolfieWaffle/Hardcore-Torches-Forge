package com.github.wolfiewaffle.hardcore_torches.block;

import com.github.wolfiewaffle.hardcore_torches.HardcoreTorches;
import com.github.wolfiewaffle.hardcore_torches.blockentity.FuelBlockEntity;
import com.github.wolfiewaffle.hardcore_torches.blockentity.IFuelBlock;
import com.github.wolfiewaffle.hardcore_torches.blockentity.LanternBlockEntity;
import com.github.wolfiewaffle.hardcore_torches.component.DataTypes;
import com.github.wolfiewaffle.hardcore_torches.config.Config;
import com.github.wolfiewaffle.hardcore_torches.init.BlockEntityInit;
import com.github.wolfiewaffle.hardcore_torches.item.LanternItem;
import com.github.wolfiewaffle.hardcore_torches.util.LanternGroup;
import com.github.wolfiewaffle.hardcore_torches.util.LanternTools;
import com.github.wolfiewaffle.hardcore_torches.util.TorchTools;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.function.IntSupplier;

public abstract class AbstractLanternBlock extends BaseEntityBlock implements EntityBlock, IFuelBlock, SimpleWaterloggedBlock {
    public static final BooleanProperty HANGING;
    public static final BooleanProperty WATERLOGGED;
    protected static final VoxelShape AABB;
    protected static final VoxelShape HANGING_AABB;
    public static final int LANTERN_LIGHT_LEVEL = 15;
    public boolean isLit;
    public LanternGroup group;
    public IntSupplier maxFuel;

    static {
        HANGING = BlockStateProperties.HANGING;
        WATERLOGGED = BlockStateProperties.WATERLOGGED;
        AABB = Shapes.or(Block.box(5.0F, 0.0F, 5.0F, 11.0F, 7.0F, 11.0F), Block.box(6.0F, 7.0F, 6.0F, 10.0F, 9.0F, 10.0F));
        HANGING_AABB = Shapes.or(Block.box(5.0F, 1.0F, 5.0F, 11.0F, 8.0F, 11.0F), Block.box(6.0F, 8.0F, 6.0F, 10.0F, 10.0F, 10.0F));
    }

    protected AbstractLanternBlock(Properties prop, boolean isLit, IntSupplier maxFuel) {
        super(prop);
        this.isLit = isLit;
        this.maxFuel = maxFuel;
    }

    @Override
    public boolean isSoulVariant() {
        return group == HardcoreTorches.soulLanterns;
    }

    @Override
    public int getMaxFuel() {
        return maxFuel.getAsInt();
    }

    @Override
    public boolean canLight(Level world, BlockPos pos) {
        return ((LanternBlockEntity) world.getBlockEntity(pos)).getFuel() > 0 && !isLit;
    }

    @Override
    public void extinguish(Level world, BlockPos pos, BlockState state, boolean playSound) {
        if (!world.isClientSide) {
            if (playSound) world.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1f, 1f);
            TorchTools.displayParticle(ParticleTypes.LARGE_SMOKE, state, world, pos);
            TorchTools.displayParticle(ParticleTypes.LARGE_SMOKE, state, world, pos);
            TorchTools.displayParticle(ParticleTypes.SMOKE, state, world, pos);
            TorchTools.displayParticle(ParticleTypes.SMOKE, state, world, pos);
            setState(world, pos, false);
        }
    }

    @Override
    public ItemInteractionResult attemptLight(Level world, BlockPos pos, BlockState state, Player player, ItemStack stack, InteractionHand hand) {
        return LanternTools.basicAttemptLight(world, pos, player, stack, hand);
    }

    @Override
    public void light(Level world, BlockPos pos) {
        if (!world.isClientSide) {
            world.playSound(null, pos, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 1f, 1f);
            setState(world, pos, true);
        }
    }

    public void setState(Level world, BlockPos pos, boolean lit) {
        BlockState oldState = world.getBlockState(pos);
        BlockState newState = group.getLanternBlock(lit).defaultBlockState();
        newState = newState.setValue(HANGING, oldState.getValue(HANGING)).setValue(WATERLOGGED, oldState.getValue(WATERLOGGED));
        int newFuel = 0;

        if (world.getBlockEntity(pos) != null) newFuel = ((FuelBlockEntity) world.getBlockEntity(pos)).getFuel();
        world.setBlockAndUpdate(pos, newState);
        if (world.getBlockEntity(pos) != null) ((FuelBlockEntity) world.getBlockEntity(pos)).setFuel(newFuel);
    }

    @Override
    public ItemStack getStack(Level world, BlockPos pos) {
        ItemStack stack = new ItemStack(world.getBlockState(pos).getBlock().asItem());
        BlockEntity blockEntity = world.getBlockEntity(pos);
        int remainingFuel;

        // Set fuel
        if (blockEntity != null && blockEntity instanceof FuelBlockEntity) {
            remainingFuel = ((FuelBlockEntity) blockEntity).getFuel();
            stack.set(DataTypes.FUEL, remainingFuel);
        }

        return stack;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        return LanternTools.interactLantern(state, world, pos, player, hand);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hitResult) {
        return LanternTools.interactLanternEmpty(state, world, pos, player, InteractionHand.MAIN_HAND);
    }

    @Override
    public boolean isLit() {
        return isLit;
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.setPlacedBy(world, pos, state, placer, itemStack);

        BlockEntity be = world.getBlockEntity(pos);

        if (be != null && be instanceof FuelBlockEntity && itemStack.getItem() instanceof LanternItem) {
            int fuel = LanternItem.getFuel(itemStack);

            ((FuelBlockEntity) be).setFuel(fuel);
        }
    }

    public boolean isLightItem(ItemStack stack) {
        if (stack.is(HardcoreTorches.FREE_LANTERN_LIGHT_ITEMS)) return true;
        if (stack.is(HardcoreTorches.DAMAGE_LANTERN_LIGHT_ITEMS)) return true;
        if (stack.is(HardcoreTorches.CONSUME_LANTERN_LIGHT_ITEMS)) return true;
        return false;
    }

    // region IFuelBlock
    @Override
    public void outOfFuel(Level world, BlockPos pos, BlockState state) {
        ((AbstractLanternBlock) world.getBlockState(pos).getBlock()).extinguish(world, pos, state, true);
    }
    //endregion

    // region Overridden methods for LanternBlock since I can't extend 2 classes
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = Blocks.LANTERN.getStateForPlacement(context);
        BlockState newState = null;
        if (state != null) newState = defaultBlockState().setValue(HANGING, state.getValue(HANGING)).setValue(WATERLOGGED, state.getValue(WATERLOGGED));
        return newState;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> stateDefinition) {
        stateDefinition.add(HANGING, WATERLOGGED);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction direction = getConnectedDirection(state).getOpposite();
        return Block.canSupportCenter(level, pos.relative(direction), direction.getOpposite());
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState p_153494_) {
        return PushReaction.DESTROY;
    }

    protected static Direction getConnectedDirection(BlockState state) {
        return state.getValue(HANGING) ? Direction.DOWN : Direction.UP;
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }

        return getConnectedDirection(state).getOpposite() == direction && !state.canSurvive(level, pos) ? Blocks.AIR.defaultBlockState() : super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(HANGING) ? HANGING_AABB : AABB;
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
        return false;
    }
    // endregion

    // region BlockEntity code
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
        // The server-level deadline queue handles expiry; no client or server BE ticker.
        return null;
    }

    @Override
    public RenderShape getRenderShape(BlockState p_49232_) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        LanternBlockEntity be = new LanternBlockEntity(pos, state);
        be.setFuel(Config.startingLanternFuel.get());
        return be;
    }
    //endregion
}
