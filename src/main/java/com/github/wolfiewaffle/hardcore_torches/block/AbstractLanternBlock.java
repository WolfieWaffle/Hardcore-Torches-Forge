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
import com.github.wolfiewaffle.hardcore_torches.util.TorchTools;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tags.ItemTags;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public abstract class AbstractLanternBlock extends Block implements IFuelBlock {
    public static final BooleanProperty HANGING;
    public static final BooleanProperty WATERLOGGED;
    public static final int LANTERN_LIGHT_LEVEL = 15;
    public boolean isLit;

    static {
        HANGING = BlockStateProperties.HANGING;
        WATERLOGGED = BlockStateProperties.WATERLOGGED;
    }

    protected AbstractLanternBlock(Properties prop, boolean isLit) {
        super(prop);
        this.isLit = isLit;
    }

    public void extinguish(World world, BlockPos pos, BlockState state, boolean playSound) {
        if (!world.isClientSide) {
            if (playSound) world.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundCategory.BLOCKS, 1f, 1f);
            TorchTools.displayParticle(ParticleTypes.LARGE_SMOKE, state, world, pos);
            TorchTools.displayParticle(ParticleTypes.LARGE_SMOKE, state, world, pos);
            TorchTools.displayParticle(ParticleTypes.SMOKE, state, world, pos);
            TorchTools.displayParticle(ParticleTypes.SMOKE, state, world, pos);
            setState(world, pos, false);
        }
    }

    public void light(World world, BlockPos pos, BlockState state) {
        if (!world.isClientSide) {
            world.playSound(null, pos, SoundEvents.FIRECHARGE_USE, SoundCategory.BLOCKS, 1f, 1f);
            setState(world, pos, true);
        }
    }

    public void setState(World world, BlockPos pos, boolean lit) {
        BlockState oldState = world.getBlockState(pos);
        BlockState newState = lit ? BlockInit.LIT_LANTERN.get().defaultBlockState() : BlockInit.UNLIT_LANTERN.get().defaultBlockState();
        newState = newState.setValue(HANGING, oldState.getValue(HANGING)).setValue(WATERLOGGED, oldState.getValue(WATERLOGGED));
        int newFuel = 0;

        if (world.getBlockEntity(pos) != null) newFuel = ((FuelBlockEntity) world.getBlockEntity(pos)).getFuel();
        world.setBlockAndUpdate(pos, newState);
        if (world.getBlockEntity(pos) != null) ((FuelBlockEntity) world.getBlockEntity(pos)).setFuel(newFuel);
    }

    protected ItemStack getStack(World world, BlockPos pos) {
        ItemStack stack = new ItemStack(world.getBlockState(pos).getBlock().asItem());
        TileEntity blockEntity = world.getBlockEntity(pos);
        int remainingFuel;

        // Set fuel
        if (blockEntity != null && blockEntity instanceof FuelBlockEntity) {
            remainingFuel = ((FuelBlockEntity) blockEntity).getFuel();
            CompoundNBT nbt = new CompoundNBT();
            nbt.putInt("Fuel", (remainingFuel));
            stack.setTag(nbt);
        }

        return stack;
    }

    @Override
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        ItemStack stack = player.getItemInHand(hand);
        TileEntity be = world.getBlockEntity(pos);

        // Pick up lantern
        if (player.isCrouching()) {
            if (!world.isClientSide) player.addItem(getStack(world, pos));
            world.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
            if (!world.isClientSide) world.playSound(null, pos, SoundEvents.LANTERN_PLACE, SoundCategory.BLOCKS, 1f, 1f);
            player.swing(hand);
            return ActionResultType.SUCCESS;
        }

        // Igniting
        if (!this.isLit && itemValid(stack, MainMod.FREE_LANTERN_LIGHT_ITEMS, MainMod.DAMAGE_LANTERN_LIGHT_ITEMS, MainMod.CONSUME_LANTERN_LIGHT_ITEMS)) {

            // If not enough fuel to light
            if (!world.isClientSide) {
                if (((FuelBlockEntity) world.getBlockEntity(pos)).getFuel() < Config.minLanternIgnitionFuel.get()) {
                    world.playSound(null, pos, SoundEvents.LANTERN_HIT, SoundCategory.BLOCKS, 1f, 1f);
                    player.displayClientMessage(new StringTextComponent("Not enough fuel to ignite!"), true);
                } else if (attemptUse(stack, player, hand, MainMod.FREE_LANTERN_LIGHT_ITEMS, MainMod.DAMAGE_LANTERN_LIGHT_ITEMS, MainMod.CONSUME_LANTERN_LIGHT_ITEMS)) {
                    light(world, pos, state);
                }
            }

            player.swing(hand);
            return ActionResultType.SUCCESS;
        }

        // Adding fuel
        if (ItemTags.COALS.contains(stack.getItem()) && !Config.lanternsNeedCan.get()) {
            if (be instanceof FuelBlockEntity && !world.isClientSide) {
                int oldFuel = ((FuelBlockEntity) be).getFuel();

                if (oldFuel < Config.defaultLanternFuel.get()) {
                    if (oldFuel + Config.defLanternFuelItem.get() < Config.defaultLanternFuel.get()) {
                        world.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundCategory.BLOCKS, 1f, 1f);
                    } else {
                        world.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundCategory.BLOCKS, 1f, 1f);
                    }

                    stack.grow(-1);
                    ((FuelBlockEntity) be).setFuel(Math.min(oldFuel + Config.defLanternFuelItem.get(), Config.defaultLanternFuel.get()));
                }
            }
            player.swing(hand);
            return ActionResultType.SUCCESS;
        }

        // Adding fuel with can
        if (stack.getItem() instanceof OilCanItem && Config.lanternsNeedCan.get()) {
            if (be instanceof FuelBlockEntity && !world.isClientSide) {
                if (OilCanItem.fuelBlock((FuelBlockEntity) be, world, stack)) {
                    world.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundCategory.BLOCKS, 1f, 1f);
                }
            }
            player.swing(hand);
            return ActionResultType.SUCCESS;
        }

        boolean showFuel = (stack.isEmpty() || stack.getItem() == ItemInit.OIL_CAN.get()) && Config.fuelMessage.get();

        // Fuel message
        if (be.getType() == BlockEntityInit.LANTERN_BLOCK_ENTITY.get() && hand == Hand.MAIN_HAND && !world.isClientSide && showFuel) {
            player.displayClientMessage(new StringTextComponent("Fuel: " + ((FuelBlockEntity) be).getFuel()), true);
        }

        if (Config.lanternsNeedCan.get() && hand == Hand.MAIN_HAND && !stack.isEmpty() && stack.getItem() != ItemInit.OIL_CAN.get() && !world.isClientSide) {
            player.displayClientMessage(new StringTextComponent("Requires an Oil Can to fuel!"), true);
        }

        // Hand extinguish
        if (Config.handUnlightLantern.get() && isLit) {
            if (!TorchTools.canLight(stack.getItem(), this)) {
                extinguish(world, pos, state, true);
                return ActionResultType.SUCCESS;
            }
        }

        return ActionResultType.PASS;
    }

    @Override
    public void setPlacedBy(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.setPlacedBy(world, pos, state, placer, itemStack);

        TileEntity be = world.getBlockEntity(pos);

        if (be != null && be instanceof FuelBlockEntity && itemStack.getItem() instanceof LanternItem) {
            int fuel = LanternItem.getFuel(itemStack);

            ((FuelBlockEntity) be).setFuel(fuel);
        }
    }

    public static boolean isLightItem(Item item) {
        if (MainMod.FREE_LANTERN_LIGHT_ITEMS.contains(item)) return true;
        if (MainMod.DAMAGE_LANTERN_LIGHT_ITEMS.contains(item)) return true;
        if (MainMod.CONSUME_LANTERN_LIGHT_ITEMS.contains(item)) return true;
        return false;
    }

    // region IFuelBlock
    @Override
    public void outOfFuel(World world, BlockPos pos, BlockState state) {
        ((AbstractLanternBlock) world.getBlockState(pos).getBlock()).extinguish(world, pos, state, false);
    }
    //endregion

    // region Overridden methods for LanternBlock since I can't extend 2 classes
    @Override
    public VoxelShape getShape(BlockState state, IBlockReader getter, BlockPos pos, ISelectionContext context) {
        return Blocks.LANTERN.getShape(state, getter, pos, context);
    }

    @Nullable
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        BlockState state = Blocks.LANTERN.getStateForPlacement(context);
        BlockState newState = null;
        if (state != null) newState = defaultBlockState().setValue(HANGING, state.getValue(HANGING)).setValue(WATERLOGGED, state.getValue(WATERLOGGED));
        return newState;
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> stateDefinition) {
        stateDefinition.add(HANGING, WATERLOGGED);
    }
    // endregion

    // region BlockEntity code
    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        LanternBlockEntity be = BlockEntityInit.LANTERN_BLOCK_ENTITY.get().create();
        be.setFuel(Config.defaultLanternFuel.get());
        return be;
    }

    @Override
    public BlockRenderType getRenderShape(BlockState p_49232_) {
        return BlockRenderType.MODEL;
    }
    //endregion

    public boolean canSurvive(BlockState p_153479_, IWorldReader p_153480_, BlockPos p_153481_) {
        return Blocks.LANTERN.canSurvive(p_153479_, p_153480_, p_153481_);
    }

    public PushReaction getPistonPushReaction(BlockState p_153494_) {
        return PushReaction.DESTROY;
    }

    public BlockState updateShape(BlockState p_153483_, Direction p_153484_, BlockState p_153485_, IWorld p_153486_, BlockPos p_153487_, BlockPos p_153488_) {
        return Blocks.LANTERN.updateShape(p_153483_, p_153484_, p_153485_, p_153486_, p_153487_, p_153488_);
    }

    public FluidState getFluidState(BlockState p_153492_) {
        return Blocks.LANTERN.getFluidState(p_153492_);
    }

    public boolean isPathfindable(BlockState p_153469_, IBlockReader p_153470_, BlockPos p_153471_, PathType p_153472_) {
        return Blocks.LANTERN.isPathfindable(p_153469_, p_153470_, p_153471_, p_153472_);
    }
}
