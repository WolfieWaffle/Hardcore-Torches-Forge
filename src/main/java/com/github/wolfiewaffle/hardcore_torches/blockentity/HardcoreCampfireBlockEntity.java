package com.github.wolfiewaffle.hardcore_torches.blockentity;

import com.github.wolfiewaffle.hardcore_torches.block.HardcoreCampfire;
import com.github.wolfiewaffle.hardcore_torches.config.Config;
import com.github.wolfiewaffle.hardcore_torches.init.BlockEntityInit;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.ForgeHooks;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;

public class HardcoreCampfireBlockEntity extends CampfireBlockEntity {
    protected int fuel;

    public HardcoreCampfireBlockEntity(BlockPos pos, BlockState state) {
        super(pos, state);
        fuel = 0;
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

        // Decrease fuel
        if (entity.fuel <= 0) {
            if (state.getBlock() instanceof HardcoreCampfire campfire) {
                campfire.outOfFuel(world, pos, state);
            }
        } else {
            entity.fuel--;
        }

        takeFuelItems(world, pos, entity);

        CampfireBlockEntity.cookTick(world, pos, state, entity);
    }

    public static void cooldownTick(Level world, BlockPos pos, BlockState state, HardcoreCampfireBlockEntity entity) {
        takeFuelItems(world, pos, entity);

        CampfireBlockEntity.cooldownTick(world, pos, state, entity);
    }

    private static void takeFuelItems(Level world, BlockPos pos, HardcoreCampfireBlockEntity entity) {
        for(ItemEntity itementity : getItemsAtAndAbove(world, pos)) {
            int burnTime = ForgeHooks.getBurnTime(itementity.getItem(), RecipeType.SMELTING);
            if (burnTime > 0) {
                FuelResult result = entity.setFuel((int) (entity.getFuel() + (burnTime * Config.campfireFuelFactor.get())));

                if (result != FuelResult.FULL) {
                    itementity.kill();
                    world.playSound(null, pos, SoundEvents.BONE_MEAL_USE, SoundSource.BLOCKS, 1f, 1f);
                    if (result == FuelResult.SOME_WASTE && isLit(world, pos)) world.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1f, 1.5f);
                }
            }
        }
    }

    @Override
    public BlockEntityType<?> getType() {
        return BlockEntityInit.CAMPFIRE_BLOCK_ENTITY.get();
    }

    public int getFuel() {
        return fuel;
    }

    public static boolean isLit(Level world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);

        if (state.hasProperty(CampfireBlock.LIT)) return state.getValue(CampfireBlock.LIT);

        return false;
    }

    public FuelResult setFuel(int newValue) {
        if (fuel >= Config.campfireMaxFuel.get()) return FuelResult.FULL;

        fuel = newValue;
        if (fuel > Config.campfireMaxFuel.get()) {
            fuel = Config.campfireMaxFuel.get();
            return FuelResult.SOME_WASTE;
        }

        return FuelResult.HAD_SPACE;
    }

    public static List<ItemEntity> getItemsAtAndAbove(Level world, BlockPos pos) {
        VoxelShape INSIDE = Block.box(2.0D, 11.0D, 2.0D, 14.0D, 16.0D, 14.0D);
        VoxelShape ABOVE = Block.box(0.0D, 16.0D, 0.0D, 16.0D, 32.0D, 16.0D);
        VoxelShape BOTH = Shapes.or(INSIDE, ABOVE);

        return BOTH.toAabbs().stream().flatMap((aabb) -> world.getEntitiesOfClass(ItemEntity.class, aabb.move(pos.getX() - 0.5D, pos.getY() - 0.5D, pos.getZ() - 0.5D), EntitySelector.ENTITY_STILL_ALIVE).stream()).collect(Collectors.toList());
    }

    // region necessary methods
    @Override
    public void load(CompoundTag nbt) {
        if (nbt != null) {
            super.load(nbt);

            fuel = nbt.getInt("Fuel");
        }
    }

    @Override
    public void saveAdditional(CompoundTag nbt) {
        super.saveAdditional(nbt);

        nbt.putInt("Fuel", fuel);
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        this.load(pkt.getTag());
    }
    // endregion
}
