package com.github.wolfiewaffle.hardcore_torches.blockentity;

import com.github.wolfiewaffle.hardcore_torches.block.HardcoreCampfire;
import com.github.wolfiewaffle.hardcore_torches.burnout.FuelMath;
import com.github.wolfiewaffle.hardcore_torches.burnout.ScheduledBurner;
import com.github.wolfiewaffle.hardcore_torches.burnout.ScheduledFuel;
import com.github.wolfiewaffle.hardcore_torches.config.Config;
import com.github.wolfiewaffle.hardcore_torches.init.BlockEntityInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class HardcoreCampfireBlockEntity extends CampfireBlockEntity implements ScheduledBurner {
    // The helper stores this reference only; scheduling starts in onLoad, not here.
    @SuppressWarnings("this-escape")
    private final ScheduledFuel fuelTimer = new ScheduledFuel(this);

    public HardcoreCampfireBlockEntity(BlockPos pos, BlockState state) {
        super(pos, state);
    }

    private enum FuelResult {
        HAD_SPACE,
        SOME_WASTE,
        FULL
    }

    public static void clientTick(Level world, BlockPos pos, BlockState state, HardcoreCampfireBlockEntity entity) {
        CampfireBlockEntity.particleTick(world, pos, state, entity);
    }

    public static void cookTick(Level world, BlockPos pos, BlockState state, HardcoreCampfireBlockEntity entity) {
        CampfireBlockEntity.cookTick(world, pos, state, entity);
    }

    public static void cooldownTick(Level world, BlockPos pos, BlockState state, HardcoreCampfireBlockEntity entity) {
        CampfireBlockEntity.cooldownTick(world, pos, state, entity);
    }

    /** Called by entityInside, never by a periodic world/entity-area scan. */
    public boolean tryAddFuel(ItemEntity item) {
        Level world = getLevel();
        if (world == null || world.isClientSide || isRemoved() || !item.isAlive()
                || getFuel() >= Config.campfireMaxFuel.get()) return false;
        ItemStack stack = item.getItem();
        if (stack.isEmpty()) return false;
        int added = FuelMath.scaledFuel(stack.getBurnTime(RecipeType.SMELTING), Config.campfireFuelFactor.get());
        if (added <= 0) return false;

        long requested = (long) getFuel() + added;
        FuelResult result = setFuel((int) Math.min(Integer.MAX_VALUE, requested));
        if (result == FuelResult.FULL) return false;

        // Do not delete a whole dropped stack for one item's burn time.
        ItemStack remaining = stack.copy();
        remaining.shrink(1);
        if (remaining.isEmpty()) item.discard();
        else item.setItem(remaining);
        world.playSound(null, getBlockPos(), SoundEvents.BONE_MEAL_USE, SoundSource.BLOCKS, 1f, 1f);
        if (requested > Config.campfireMaxFuel.get() && burnsContinuously(getBlockState())) {
            world.playSound(null, getBlockPos(), SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1f, 1.5f);
        }
        return true;
    }

    @Override
    public BlockEntityType<?> getType() {
        return BlockEntityInit.CAMPFIRE_BLOCK_ENTITY.get();
    }

    public int getFuel() {
        return fuelTimer.get();
    }

    public static boolean isLit(Level world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        return state.hasProperty(CampfireBlock.LIT) && state.getValue(CampfireBlock.LIT);
    }

    public FuelResult setFuel(int newValue) {
        int max = Math.max(0, Config.campfireMaxFuel.get());
        if (getFuel() >= max) return FuelResult.FULL;
        fuelTimer.set(Math.min(max, Math.max(0, newValue)));
        return newValue > max ? FuelResult.SOME_WASTE : FuelResult.HAD_SPACE;
    }

    @Override
    public BlockEntity burnoutBlockEntity() {
        return this;
    }

    @Override
    public boolean burnsContinuously(BlockState state) {
        return state.getBlock() instanceof HardcoreCampfire && state.getValue(CampfireBlock.LIT);
    }

    @Override
    public void onBurnoutCheck() {
        Level world = getLevel();
        if (world == null || world.isClientSide || isRemoved()) return;
        BlockState state = getBlockState();
        if (getFuel() == 0 && burnsContinuously(state)) {
            ((HardcoreCampfire) state.getBlock()).outOfFuel(world, getBlockPos(), state);
        }
        if (!isRemoved()) fuelTimer.refresh();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        fuelTimer.onLoad();
    }

    @Override
    public void onChunkUnloaded() {
        fuelTimer.onRemoved();
        super.onChunkUnloaded();
    }

    @Override
    public void setRemoved() {
        fuelTimer.onRemoved();
        super.setRemoved();
    }

    @Override
    public void setBlockState(BlockState state) {
        super.setBlockState(state);
        fuelTimer.refresh();
    }

    @Override
    public void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        super.loadAdditional(nbt, registries);
        fuelTimer.load(nbt);
    }

    @Override
    public void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        super.saveAdditional(nbt, registries);
        fuelTimer.save(nbt);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        fuelTimer.save(tag);
        return tag;
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
