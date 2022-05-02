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
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.function.IntSupplier;

public abstract class AbstractHardcoreTorchBlock extends BaseEntityBlock implements IFuelBlock, EntityBlock {

    public SimpleParticleType particle;
    public ETorchState burnState;
    public TorchGroup group;
    public static final BlockEntityTicker<TorchBlockEntity> TICKER = (level, pos, state, be) -> be.tick();
    public IntSupplier maxFuel;

    public AbstractHardcoreTorchBlock(Properties prop, SimpleParticleType particle, ETorchState burnState, TorchGroup group, IntSupplier maxFuel) {
        super(prop);
        this.particle = particle;
        this.burnState = burnState;
        this.group = group;
        this.maxFuel = maxFuel;
    }

    public abstract boolean isWall();

    @Override
    public int getMaxFuel() {
        return maxFuel.getAsInt();
    }

    @Override
    public boolean canLight(Level world, BlockPos pos) {
        return burnState != ETorchState.BURNT && burnState != ETorchState.LIT;
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack stack = player.getItemInHand(hand);

        if (burnState == ETorchState.LIT) {
            if (attemptUse(stack, player, hand, MainMod.FREE_TORCH_EXTINGUISH_ITEMS, MainMod.DAMAGE_TORCH_EXTINGUISH_ITEMS, MainMod.CONSUME_TORCH_EXTINGUISH_ITEMS)) {
                extinguish(world, pos, state);
                player.swing(hand);
                return InteractionResult.SUCCESS;
            }

            if (attemptUse(stack, player, hand, MainMod.FREE_TORCH_SMOTHER_ITEMS, MainMod.DAMAGE_TORCH_SMOTHER_ITEMS, MainMod.CONSUME_TORCH_SMOTHER_ITEMS)) {
                smother(world, pos, state);
                player.swing(hand);
                return InteractionResult.SUCCESS;
            }
        }

        if (burnState == ETorchState.SMOLDERING || burnState == ETorchState.UNLIT) {
            if (attemptUse(stack, player, hand, MainMod.FREE_TORCH_LIGHT_ITEMS, MainMod.DAMAGE_TORCH_LIGHT_ITEMS, MainMod.CONSUME_TORCH_LIGHT_ITEMS)) {
                light(world, pos, state);
                player.swing(hand);
                return InteractionResult.SUCCESS;
            }
        }

        // Fuel message
        BlockEntity be = world.getBlockEntity(pos);
        if (be.getType() == BlockEntityInit.TORCH_BLOCK_ENTITY.get() && !world.isClientSide && Config.fuelMessage.get() && stack.isEmpty()) {
            player.displayClientMessage(new TextComponent("Fuel: " + ((TorchBlockEntity) be).getFuel()), true);
        }

        // Oil Can
        if (Config.torchesUseCan.get() && burnState != ETorchState.BURNT && !world.isClientSide) {
            if (OilCanItem.fuelBlock((FuelBlockEntity) be, world, stack)) {
                world.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1f, 1f);
            }
        }

        // Hand extinguish
        if (Config.handUnlightTorch.get() && (burnState == ETorchState.LIT || burnState == ETorchState.SMOLDERING)) {
            if (!TorchTools.canLight(stack.getItem(), this.defaultBlockState())) {
                extinguish(world, pos, state);
                return InteractionResult.SUCCESS;
            }
        }

        return InteractionResult.PASS;
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.setPlacedBy(world, pos, state, placer, itemStack);

        BlockEntity be = world.getBlockEntity(pos);

        if (be != null && be instanceof FuelBlockEntity && itemStack.getItem() instanceof TorchItem) {
            int fuel = TorchItem.getFuel(itemStack);

            if (fuel == 0) {
                ((FuelBlockEntity) be).setFuel(getMaxFuel());
            } else {
                ((FuelBlockEntity) be).setFuel(fuel);
            }
        }
    }

    public static boolean isLightItem(ItemStack stack) {
        if (stack.is(MainMod.FREE_TORCH_LIGHT_ITEMS)) return true;
        if (stack.is(MainMod.DAMAGE_TORCH_LIGHT_ITEMS)) return true;
        if (stack.is(MainMod.CONSUME_TORCH_LIGHT_ITEMS)) return true;
        return false;
    }

    // region state methods
    public void smother(Level world, BlockPos pos, BlockState state) {
        if (!world.isClientSide) {
            world.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1f, 1f);
            TorchTools.displayParticle(ParticleTypes.LARGE_SMOKE, state, world, pos);
            TorchTools.displayParticle(ParticleTypes.LARGE_SMOKE, state, world, pos);
            TorchTools.displayParticle(ParticleTypes.SMOKE, state, world, pos);
            TorchTools.displayParticle(ParticleTypes.SMOKE, state, world, pos);
            changeTorch(world, pos, state, ETorchState.SMOLDERING);
        }
    }

    public void extinguish(Level world, BlockPos pos, BlockState state) {
        if (!world.isClientSide) {
            world.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1f, 1f);
            TorchTools.displayParticle(ParticleTypes.LARGE_SMOKE, state, world, pos);
            TorchTools.displayParticle(ParticleTypes.LARGE_SMOKE, state, world, pos);
            TorchTools.displayParticle(ParticleTypes.SMOKE, state, world, pos);
            TorchTools.displayParticle(ParticleTypes.SMOKE, state, world, pos);
            changeTorch(world, pos, state, ETorchState.UNLIT);
        }
    }

    public void burnOut(Level world, BlockPos pos, BlockState state, boolean playSound) {
        if (!world.isClientSide) {
            if (playSound) world.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1f, 1f);
            TorchTools.displayParticle(ParticleTypes.LARGE_SMOKE, state, world, pos);
            TorchTools.displayParticle(ParticleTypes.LARGE_SMOKE, state, world, pos);
            TorchTools.displayParticle(ParticleTypes.SMOKE, state, world, pos);
            TorchTools.displayParticle(ParticleTypes.SMOKE, state, world, pos);
            changeTorch(world, pos, state, ETorchState.BURNT);
        }
    }

    public void light(Level world, BlockPos pos, BlockState state) {
        if (!world.isClientSide) {
            world.playSound(null, pos, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 0.5f, 1.2f);
            TorchTools.displayParticle(ParticleTypes.LAVA, state, world, pos);
            TorchTools.displayParticle(ParticleTypes.FLAME, state, world, pos);
            changeTorch(world, pos, state, ETorchState.LIT);
        }
    }

    public void changeTorch(Level world, BlockPos pos, BlockState curState, ETorchState newType) {
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
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
        if (burnState == ETorchState.LIT || burnState == ETorchState.SMOLDERING) {
             return type == BlockEntityInit.TORCH_BLOCK_ENTITY.get() ? (level, pos, blockState, be) -> ((TorchBlockEntity) be).tick() : super.getTicker(world, state, type);
        } else {
            return null;
        }
    }

    @Override
    public RenderShape getRenderShape(BlockState p_49232_) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        TorchBlockEntity be = new TorchBlockEntity(pos, state);
        be.setFuel(getMaxFuel());
        return be;
    }
    // endregion

    // region IFuelBlock
    @Override
    public void outOfFuel(Level world, BlockPos pos, BlockState state) {
        burnOut(world, pos, state, false);
    }
    // endregion
}
