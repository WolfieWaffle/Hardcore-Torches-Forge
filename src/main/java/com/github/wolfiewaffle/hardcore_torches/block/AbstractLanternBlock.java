package com.github.wolfiewaffle.hardcore_torches.block;

import com.github.wolfiewaffle.hardcore_torches.MainMod;
import com.github.wolfiewaffle.hardcore_torches.blockentity.FuelBlockEntity;
import com.github.wolfiewaffle.hardcore_torches.blockentity.IFuelBlock;
import com.github.wolfiewaffle.hardcore_torches.blockentity.LanternBlockEntity;
import com.github.wolfiewaffle.hardcore_torches.config.Config;
import com.github.wolfiewaffle.hardcore_torches.init.BlockEntityInit;
import com.github.wolfiewaffle.hardcore_torches.init.BlockInit;
import com.github.wolfiewaffle.hardcore_torches.init.ItemInit;
import com.github.wolfiewaffle.hardcore_torches.item.LanternItem;
import com.github.wolfiewaffle.hardcore_torches.item.OilCanItem;
import com.github.wolfiewaffle.hardcore_torches.util.ETorchState;
import com.github.wolfiewaffle.hardcore_torches.util.TorchTools;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
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
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.function.IntSupplier;

public abstract class AbstractLanternBlock extends BaseEntityBlock implements EntityBlock, IFuelBlock, SimpleWaterloggedBlock {
    public static final BooleanProperty HANGING;
    public static final BooleanProperty WATERLOGGED;
    public static final int LANTERN_LIGHT_LEVEL = 15;
    public boolean isLit;
    public IntSupplier maxFuel;

    static {
        HANGING = BlockStateProperties.HANGING;
        WATERLOGGED = BlockStateProperties.WATERLOGGED;
    }

    protected AbstractLanternBlock(Properties prop, boolean isLit, IntSupplier maxFuel) {
        super(prop);
        this.isLit = isLit;
        this.maxFuel = maxFuel;
    }

    @Override
    public int getMaxFuel() {
        return maxFuel.getAsInt();
    }

    @Override
    public boolean canLight(Level world, BlockPos pos) {
        return ((LanternBlockEntity) world.getBlockEntity(pos)).getFuel() > 0 && !isLit;
    }

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

    public void light(Level world, BlockPos pos, BlockState state) {
        if (!world.isClientSide) {
            world.playSound(null, pos, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 1f, 1f);
            setState(world, pos, true);
        }
    }

    public void setState(Level world, BlockPos pos, boolean lit) {
        BlockState oldState = world.getBlockState(pos);
        BlockState newState = lit ? BlockInit.LIT_LANTERN.get().defaultBlockState() : BlockInit.UNLIT_LANTERN.get().defaultBlockState();
        newState = newState.setValue(HANGING, oldState.getValue(HANGING)).setValue(WATERLOGGED, oldState.getValue(WATERLOGGED));
        int newFuel = 0;

        if (world.getBlockEntity(pos) != null) newFuel = ((FuelBlockEntity) world.getBlockEntity(pos)).getFuel();
        world.setBlockAndUpdate(pos, newState);
        if (world.getBlockEntity(pos) != null) ((FuelBlockEntity) world.getBlockEntity(pos)).setFuel(newFuel);
    }

    protected ItemStack getStack(Level world, BlockPos pos) {
        ItemStack stack = new ItemStack(world.getBlockState(pos).getBlock().asItem());
        BlockEntity blockEntity = world.getBlockEntity(pos);
        int remainingFuel;

        // Set fuel
        if (blockEntity != null && blockEntity instanceof FuelBlockEntity) {
            remainingFuel = ((FuelBlockEntity) blockEntity).getFuel();
            CompoundTag nbt = new CompoundTag();
            nbt.putInt("Fuel", (remainingFuel));
            stack.setTag(nbt);
        }

        return stack;
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack stack = player.getItemInHand(hand);
        BlockEntity be = world.getBlockEntity(pos);

        // Pick up lantern
        if (player.isCrouching()) {
            if (!world.isClientSide) player.addItem(getStack(world, pos));
            world.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
            if (!world.isClientSide) world.playSound(null, pos, SoundEvents.LANTERN_PLACE, SoundSource.BLOCKS, 1f, 1f);
            player.swing(hand);
            return InteractionResult.SUCCESS;
        }

        // Igniting
        if (!this.isLit && itemValid(stack, MainMod.FREE_LANTERN_LIGHT_ITEMS, MainMod.DAMAGE_LANTERN_LIGHT_ITEMS, MainMod.CONSUME_LANTERN_LIGHT_ITEMS)) {

            // If not enough fuel to light
            if (!world.isClientSide) {
                if (((FuelBlockEntity) world.getBlockEntity(pos)).getFuel() < Config.minLanternIgnitionFuel.get()) {
                    world.playSound(null, pos, SoundEvents.LANTERN_HIT, SoundSource.BLOCKS, 1f, 1f);
                    player.displayClientMessage(new TextComponent("Not enough fuel to ignite!"), true);
                } else if (attemptUse(stack, player, hand, MainMod.FREE_LANTERN_LIGHT_ITEMS, MainMod.DAMAGE_LANTERN_LIGHT_ITEMS, MainMod.CONSUME_LANTERN_LIGHT_ITEMS)) {
                    light(world, pos, state);
                }
            }

            player.swing(hand);
            return InteractionResult.SUCCESS;
        }

        // Adding fuel
        if (stack.is(ItemTags.COALS) && !Config.lanternsNeedCan.get()) {
            if (be instanceof FuelBlockEntity && !world.isClientSide) {
                int oldFuel = ((FuelBlockEntity) be).getFuel();

                if (oldFuel < getMaxFuel()) {
                    if (oldFuel + Config.defLanternFuelItem.get() < getMaxFuel()) {
                        world.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1f, 1f);
                    } else {
                        world.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1f, 1f);
                    }

                    stack.grow(-1);
                    ((FuelBlockEntity) be).setFuel(Math.min(oldFuel + Config.defLanternFuelItem.get(), getMaxFuel()));
                }
            }
            player.swing(hand);
            return InteractionResult.SUCCESS;
        }

        // Adding fuel with can
        if (stack.getItem() instanceof OilCanItem && Config.lanternsNeedCan.get()) {
            if (be instanceof FuelBlockEntity && !world.isClientSide) {
                if (OilCanItem.fuelBlock((FuelBlockEntity) be, world, stack)) {
                    world.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1f, 1f);
                }
            }
            player.swing(hand);
            return InteractionResult.SUCCESS;
        }

        boolean showFuel = (stack.isEmpty() || stack.getItem() == ItemInit.OIL_CAN.get()) && Config.fuelMessage.get();

        // Fuel message
        if (be.getType() == BlockEntityInit.LANTERN_BLOCK_ENTITY.get() && hand == InteractionHand.MAIN_HAND && !world.isClientSide && showFuel) {
            player.displayClientMessage(new TextComponent("Fuel: " + ((FuelBlockEntity) be).getFuel()), true);
        }

        if (Config.lanternsNeedCan.get() && hand == InteractionHand.MAIN_HAND && !stack.isEmpty() && stack.getItem() != ItemInit.OIL_CAN.get() && !world.isClientSide) {
            player.displayClientMessage(new TextComponent("Requires an Oil Can to fuel!"), true);
        }

        // Hand extinguish
        if (Config.handUnlightLantern.get() && isLit) {
            if (!TorchTools.canLight(stack.getItem(), this.defaultBlockState())) {
                extinguish(world, pos, state, true);
                return InteractionResult.SUCCESS;
            }
        }

        return InteractionResult.PASS;
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

    public static boolean isLightItem(ItemStack stack) {
        if (stack.is(MainMod.FREE_LANTERN_LIGHT_ITEMS)) return true;
        if (stack.is(MainMod.DAMAGE_LANTERN_LIGHT_ITEMS)) return true;
        if (stack.is(MainMod.CONSUME_LANTERN_LIGHT_ITEMS)) return true;
        return false;
    }

    // region IFuelBlock
    @Override
    public void outOfFuel(Level world, BlockPos pos, BlockState state) {
        ((AbstractLanternBlock) world.getBlockState(pos).getBlock()).extinguish(world, pos, state, false);
    }
    //endregion

    // region Overridden methods for LanternBlock since I can't extend 2 classes
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext context) {
        return Blocks.LANTERN.getShape(state, getter, pos, context);
    }

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
    // endregion

    // region BlockEntity code
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
        return type == BlockEntityInit.LANTERN_BLOCK_ENTITY.get() ? (level, pos, blockState, be) -> ((LanternBlockEntity) be).tick() : super.getTicker(world, state, type);
    }

    @Override
    public RenderShape getRenderShape(BlockState p_49232_) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        LanternBlockEntity be = new LanternBlockEntity(pos, state);
        be.setFuel(Config.defLanternFuelItem.get());
        return be;
    }
    //endregion

    public boolean canSurvive(BlockState p_153479_, LevelReader p_153480_, BlockPos p_153481_) {
        return Blocks.LANTERN.canSurvive(p_153479_, p_153480_, p_153481_);
    }

    public PushReaction getPistonPushReaction(BlockState p_153494_) {
        return PushReaction.DESTROY;
    }

    public BlockState updateShape(BlockState p_153483_, Direction p_153484_, BlockState p_153485_, LevelAccessor p_153486_, BlockPos p_153487_, BlockPos p_153488_) {
        return Blocks.LANTERN.updateShape(p_153483_, p_153484_, p_153485_, p_153486_, p_153487_, p_153488_);
    }

    public FluidState getFluidState(BlockState p_153492_) {
        return Blocks.LANTERN.getFluidState(p_153492_);
    }

    public boolean isPathfindable(BlockState p_153469_, BlockGetter p_153470_, BlockPos p_153471_, PathComputationType p_153472_) {
        return Blocks.LANTERN.isPathfindable(p_153469_, p_153470_, p_153471_, p_153472_);
    }
}
