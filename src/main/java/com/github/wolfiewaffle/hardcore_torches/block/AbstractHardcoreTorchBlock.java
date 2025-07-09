package com.github.wolfiewaffle.hardcore_torches.block;

import com.github.wolfiewaffle.hardcore_torches.HardcoreTorches;
import com.github.wolfiewaffle.hardcore_torches.blockentity.FuelBlockEntity;
import com.github.wolfiewaffle.hardcore_torches.blockentity.IFuelBlock;
import com.github.wolfiewaffle.hardcore_torches.blockentity.IFuelBlockEntity;
import com.github.wolfiewaffle.hardcore_torches.blockentity.TorchBlockEntity;
import com.github.wolfiewaffle.hardcore_torches.config.Config;
import com.github.wolfiewaffle.hardcore_torches.init.BlockEntityInit;
import com.github.wolfiewaffle.hardcore_torches.item.OilCanItem;
import com.github.wolfiewaffle.hardcore_torches.item.TorchItem;
import com.github.wolfiewaffle.hardcore_torches.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
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

    public SimpleParticleType fireParticle;
    public SimpleParticleType smokeParticle;
    public ETorchState burnState;
    public TorchGroup group;
    public static final BlockEntityTicker<TorchBlockEntity> TICKER = (level, pos, state, be) -> be.tick();
    public IntSupplier maxFuel;

    public AbstractHardcoreTorchBlock(Properties prop, SimpleParticleType fireParticle, SimpleParticleType smokeParticle, ETorchState burnState, TorchGroup group, IntSupplier maxFuel) {
        super(prop);
        this.fireParticle = fireParticle;
        this.smokeParticle = smokeParticle;
        this.burnState = burnState;
        this.group = group;
        this.maxFuel = maxFuel;
    }

    public abstract boolean isWall();

    @Override
    public boolean isSoulVariant() {
        return group == HardcoreTorches.soulTorches;
    }

    @Override
    public int getMaxFuel() {
        return maxFuel.getAsInt();
    }

    @Override
    public boolean canLight(Level world, BlockPos pos) {
        return burnState != ETorchState.BURNT && burnState != ETorchState.LIT;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {

        if (this.burnState == ETorchState.LIT) {
            if (this.attemptUseItem(stack, player, hand, ETorchState.UNLIT)) {
                this.extinguish(world, pos, state, true);
                player.swing(hand);
                return ItemInteractionResult.SUCCESS;
            }

            if (this.attemptUseItem(stack, player, hand, ETorchState.SMOLDERING)) {
                this.smother(world, pos, state);
                player.swing(hand);
                return ItemInteractionResult.SUCCESS;
            }
        }

        BlockEntity be = world.getBlockEntity(pos);

        // Try to light this torch
        if ((this.burnState == ETorchState.SMOLDERING || this.burnState == ETorchState.UNLIT) && this.attemptUseItem(stack, player, hand, ETorchState.LIT)) {
            if (isSoulVariant()) {
                if (be instanceof  IFuelBlockEntity fuelBlockEntity) {
                    if (fuelBlockEntity.getFuel() > 0) this.light(world, pos);
                    else player.displayClientMessage(Component.literal("Needs XP from an Amethyst Shard"), true);
                }
            } else {
                this.light(world, pos);
            }
            player.swing(hand);
            return ItemInteractionResult.SUCCESS;

        } else { // Other cases, when torch was not lit

            // Hand extinguish
            if (Config.handUnlightTorch.get() && (this.burnState == ETorchState.LIT || this.burnState == ETorchState.SMOLDERING) && !TorchTools.canLight(stack.getItem(), this.defaultBlockState())) {
                this.extinguish(world, pos, state, true);
                return ItemInteractionResult.SUCCESS;
            }

            // Soul
            if (isSoulVariant()) {
                return SoulAttunement.soulAttune(world, pos, (IFuelBlockEntity) be, (IFuelBlock) state.getBlock(), player, hand);
            }

            // Fueling a torch with oil can
            if (Config.torchesUseCan.get() && this.burnState != ETorchState.BURNT && !world.isClientSide && OilCanItem.fuelBlock((IFuelBlockEntity) be, world, stack)) {
                world.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
                return ItemInteractionResult.SUCCESS;
            }
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hitResult) {
        BlockEntity be = world.getBlockEntity(pos);

        // Message
        if (be.getType() == BlockEntityInit.TORCH_BLOCK_ENTITY.get() && !world.isClientSide && Config.fuelMessage.get()) {
            player.displayClientMessage(Component.literal("Fuel: " + ((TorchBlockEntity)be).getFuel()), true);
        }

        // Hand extinguish
        if (Config.handUnlightTorch.get() && (this.burnState == ETorchState.LIT || this.burnState == ETorchState.SMOLDERING)) {
            this.extinguish(world, pos, state, true);
            return InteractionResult.SUCCESS;
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
        if (stack.is(HardcoreTorches.FREE_TORCH_LIGHT_ITEMS)) return true;
        if (stack.is(HardcoreTorches.DAMAGE_TORCH_LIGHT_ITEMS)) return true;
        if (stack.is(HardcoreTorches.CONSUME_TORCH_LIGHT_ITEMS)) return true;
        return false;
    }

    // region state methods
    public void smother(Level world, BlockPos pos, BlockState state) {
        if (group == HardcoreTorches.soulTorches) extinguish(world, pos, state, true);
        else if (!world.isClientSide) {
            world.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1f, 1f);
            TorchTools.displayParticle(ParticleTypes.LARGE_SMOKE, state, world, pos);
            TorchTools.displayParticle(ParticleTypes.LARGE_SMOKE, state, world, pos);
            TorchTools.displayParticle(ParticleTypes.SMOKE, state, world, pos);
            TorchTools.displayParticle(ParticleTypes.SMOKE, state, world, pos);
            changeTorch(world, pos, state, ETorchState.SMOLDERING);
        }
    }

    public void burnOut(Level world, BlockPos pos, BlockState state, boolean playSound) {
        if (group == HardcoreTorches.soulTorches) extinguish(world, pos, state, true);
        else if (!world.isClientSide) {
            if (playSound) world.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1f, 1f);
            TorchTools.displayParticle(ParticleTypes.LARGE_SMOKE, state, world, pos);
            TorchTools.displayParticle(ParticleTypes.LARGE_SMOKE, state, world, pos);
            TorchTools.displayParticle(ParticleTypes.SMOKE, state, world, pos);
            TorchTools.displayParticle(ParticleTypes.SMOKE, state, world, pos);
            changeTorch(world, pos, state, ETorchState.BURNT);
        }
    }

    @Override
    public void light(Level world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);

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
            newState = group.getWallTorch(newType).withPropertiesOf(curState);
        } else {
            newState = group.getStandingTorch(newType).withPropertiesOf(curState);
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
        if (state.getBlock() instanceof AbstractHardcoreTorchBlock torch) {
            if (torch.burnState != ETorchState.BURNT) {
                TorchBlockEntity be = new TorchBlockEntity(pos, state);
                be.setFuel(getMaxFuel());
                return be;
            }
        }
        return null;
    }
    // endregion

    // region IFuelBlock
    @Override
    public void outOfFuel(Level world, BlockPos pos, BlockState state) {
        if (group == HardcoreTorches.soulTorches) extinguish(world, pos, state, true);
        else burnOut(world, pos, state, false);
    }

    @Override
    public void extinguish(Level world, BlockPos pos, BlockState state, boolean playSound) {
        if (!world.isClientSide) {
            world.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1f, 1f);
            TorchTools.displayParticle(ParticleTypes.LARGE_SMOKE, state, world, pos);
            TorchTools.displayParticle(ParticleTypes.LARGE_SMOKE, state, world, pos);
            TorchTools.displayParticle(ParticleTypes.SMOKE, state, world, pos);
            TorchTools.displayParticle(ParticleTypes.SMOKE, state, world, pos);
            changeTorch(world, pos, state, ETorchState.UNLIT);
        }
    }

    // DONT USE THIS
    @Override
    public ItemStack getStack(Level world, BlockPos pos) {
        return new ItemStack(world.getBlockState(pos).getBlock());
    }

    // DONT USE THIS
    @Override
    public ItemInteractionResult attemptLight(Level world, BlockPos pos, BlockState state, Player player, ItemStack stack, InteractionHand hand) {
        attemptUseItem(stack, player, hand, ETorchState.LIT);
        return ItemInteractionResult.SUCCESS;
    }

    // DONT USE THIS
    @Override
    public boolean isLit() {
        return burnState == ETorchState.LIT;
    }
    // endregion
}
