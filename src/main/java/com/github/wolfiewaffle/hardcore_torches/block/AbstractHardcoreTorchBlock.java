package com.github.wolfiewaffle.hardcore_torches.block;

import com.github.wolfiewaffle.hardcore_torches.MainMod;
import com.github.wolfiewaffle.hardcore_torches.blockentity.FuelBlockEntity;
import com.github.wolfiewaffle.hardcore_torches.blockentity.IFuelBlock;
import com.github.wolfiewaffle.hardcore_torches.blockentity.TorchBlockEntity;
import com.github.wolfiewaffle.hardcore_torches.config.Config;
import com.github.wolfiewaffle.hardcore_torches.init.BlockEntityInit;
import com.github.wolfiewaffle.hardcore_torches.item.OilCanItem;
import com.github.wolfiewaffle.hardcore_torches.item.TorchItem;
import com.github.wolfiewaffle.hardcore_torches.util.ETorchState;
import com.github.wolfiewaffle.hardcore_torches.util.TorchGroup;
import com.github.wolfiewaffle.hardcore_torches.util.TorchTools;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.ContainerBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public abstract class AbstractHardcoreTorchBlock extends Block implements IFuelBlock {

    public BasicParticleType particle;
    public ETorchState burnState;
    public TorchGroup group;

    public AbstractHardcoreTorchBlock(Properties prop, BasicParticleType particle, ETorchState burnState, TorchGroup group) {
        super(prop);
        this.particle = particle;
        this.burnState = burnState;
        this.group = group;
    }

    public abstract boolean isWall();

    @Override
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        ItemStack stack = player.getItemInHand(hand);

        if (burnState == ETorchState.LIT) {
            if (attemptUse(stack, player, hand, MainMod.FREE_TORCH_EXTINGUISH_ITEMS, MainMod.DAMAGE_TORCH_EXTINGUISH_ITEMS, MainMod.CONSUME_TORCH_EXTINGUISH_ITEMS)) {
                extinguish(world, pos, state);
                player.swing(hand);
                return ActionResultType.SUCCESS;
            }

            if (attemptUse(stack, player, hand, MainMod.FREE_TORCH_SMOTHER_ITEMS, MainMod.DAMAGE_TORCH_SMOTHER_ITEMS, MainMod.CONSUME_TORCH_SMOTHER_ITEMS)) {
                smother(world, pos, state);
                player.swing(hand);
                return ActionResultType.SUCCESS;
            }
        }

        if (burnState == ETorchState.SMOLDERING || burnState == ETorchState.UNLIT) {
            if (attemptUse(stack, player, hand, MainMod.FREE_TORCH_LIGHT_ITEMS, MainMod.DAMAGE_TORCH_LIGHT_ITEMS, MainMod.CONSUME_TORCH_LIGHT_ITEMS)) {
                light(world, pos, state);
                player.swing(hand);
                return ActionResultType.SUCCESS;
            }
        }

        // Fuel message
        TileEntity be = world.getBlockEntity(pos);
        if (be.getType() == BlockEntityInit.TORCH_BLOCK_ENTITY.get() && !world.isClientSide && Config.fuelMessage.get() && stack.isEmpty()) {
            player.displayClientMessage(new StringTextComponent("Fuel: " + ((TorchBlockEntity) be).getFuel()), true);
        }

        // Oil Can
        if (Config.torchesUseCan.get() && burnState != ETorchState.BURNT && !world.isClientSide) {
            if (OilCanItem.fuelBlock((FuelBlockEntity) be, world, stack)) {
                world.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundCategory.BLOCKS, 1f, 1f);
            }
        }

        // Hand extinguish
        if (Config.handUnlightTorch.get() && (burnState == ETorchState.LIT || burnState == ETorchState.SMOLDERING)) {
            if (!TorchTools.canLight(stack.getItem(), this)) {
                extinguish(world, pos, state);
                return ActionResultType.SUCCESS;
            }
        }

        return ActionResultType.PASS;
    }

    @Override
    public void setPlacedBy(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.setPlacedBy(world, pos, state, placer, itemStack);

        TileEntity be = world.getBlockEntity(pos);

        if (be != null && be instanceof FuelBlockEntity && itemStack.getItem() instanceof TorchItem) {
            int fuel = TorchItem.getFuel(itemStack);

            if (fuel == 0) {
                ((FuelBlockEntity) be).setFuel(Config.defaultTorchFuel.get());
            } else {
                ((FuelBlockEntity) be).setFuel(fuel);
            }
        }
    }

    public static boolean isLightItem(Item item) {
        if (MainMod.FREE_TORCH_LIGHT_ITEMS.contains(item)) return true;
        if (MainMod.DAMAGE_TORCH_LIGHT_ITEMS.contains(item)) return true;
        if (MainMod.CONSUME_TORCH_LIGHT_ITEMS.contains(item)) return true;
        return false;
    }

    // region state methods
    public void smother(World world, BlockPos pos, BlockState state) {
        if (!world.isClientSide) {
            world.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundCategory.BLOCKS, 1f, 1f);
            TorchTools.displayParticle(ParticleTypes.LARGE_SMOKE, state, world, pos);
            TorchTools.displayParticle(ParticleTypes.LARGE_SMOKE, state, world, pos);
            TorchTools.displayParticle(ParticleTypes.SMOKE, state, world, pos);
            TorchTools.displayParticle(ParticleTypes.SMOKE, state, world, pos);
            changeTorch(world, pos, state, ETorchState.SMOLDERING);
        }
    }

    public void extinguish(World world, BlockPos pos, BlockState state) {
        if (!world.isClientSide) {
            world.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundCategory.BLOCKS, 1f, 1f);
            TorchTools.displayParticle(ParticleTypes.LARGE_SMOKE, state, world, pos);
            TorchTools.displayParticle(ParticleTypes.LARGE_SMOKE, state, world, pos);
            TorchTools.displayParticle(ParticleTypes.SMOKE, state, world, pos);
            TorchTools.displayParticle(ParticleTypes.SMOKE, state, world, pos);
            changeTorch(world, pos, state, ETorchState.UNLIT);
        }
    }

    public void burnOut(World world, BlockPos pos, BlockState state) {
        if (!world.isClientSide) {
            world.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundCategory.BLOCKS, 1f, 1f);
            TorchTools.displayParticle(ParticleTypes.LARGE_SMOKE, state, world, pos);
            TorchTools.displayParticle(ParticleTypes.LARGE_SMOKE, state, world, pos);
            TorchTools.displayParticle(ParticleTypes.SMOKE, state, world, pos);
            TorchTools.displayParticle(ParticleTypes.SMOKE, state, world, pos);
            changeTorch(world, pos, state, ETorchState.BURNT);
        }
    }

    public void light(World world, BlockPos pos, BlockState state) {
        if (!world.isClientSide) {
            world.playSound(null, pos, SoundEvents.FIRECHARGE_USE, SoundCategory.BLOCKS, 0.5f, 1.2f);
            TorchTools.displayParticle(ParticleTypes.LAVA, state, world, pos);
            TorchTools.displayParticle(ParticleTypes.FLAME, state, world, pos);
            changeTorch(world, pos, state, ETorchState.LIT);
        }
    }

    public void changeTorch(World world, BlockPos pos, BlockState curState, ETorchState newType) {
        BlockState newState;

        if (isWall()) {
            newState = group.getWallTorch(newType).defaultBlockState().setValue(HardcoreWallTorchBlock.FACING, curState.getValue(HardcoreWallTorchBlock.FACING));
        } else {
            newState = group.getStandingTorch(newType).defaultBlockState();
        }

        int newFuel = 0;
        if (world.getBlockEntity(pos) != null) newFuel = ((FuelBlockEntity) world.getBlockEntity(pos)).getFuel();
        world.setBlockAndUpdate(pos, newState);
        if (world.getBlockEntity(pos) != null) ((FuelBlockEntity) world.getBlockEntity(pos)).setFuel(newFuel);
    }
    // endregion

    // region BlockEntity
    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        TorchBlockEntity be = BlockEntityInit.TORCH_BLOCK_ENTITY.get().create();
        be.setFuel(Config.defaultTorchFuel.get());
        return be;
    }

    @Override
    public BlockRenderType getRenderShape(BlockState p_49232_) {
        return BlockRenderType.MODEL;
    }
    // endregion

    // region IFuelBlock
    @Override
    public void outOfFuel(World world, BlockPos pos, BlockState state) {
        burnOut(world, pos, state);
    }
    // endregion
}
