package com.github.wolfiewaffle.hardcore_torches.compat.amendments;

import com.github.wolfiewaffle.hardcore_torches.MainMod;
import com.github.wolfiewaffle.hardcore_torches.blockentity.IFuelBlock;
import com.github.wolfiewaffle.hardcore_torches.blockentity.IFuelBlockEntity;
import com.github.wolfiewaffle.hardcore_torches.item.LanternItem;
import com.github.wolfiewaffle.hardcore_torches.util.BlockStateTools;
import com.github.wolfiewaffle.hardcore_torches.util.LanternGroup;
import com.github.wolfiewaffle.hardcore_torches.util.LanternTools;
import com.github.wolfiewaffle.hardcore_torches.util.TorchTools;
import net.mehvahdjukaar.amendments.common.block.WallLanternBlock;
import net.mehvahdjukaar.amendments.common.tile.SwayingBlockTile;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams.Builder;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntSupplier;

public class HardcoreWallLantern extends WallLanternBlock implements IFuelBlock {
    private LanternGroup group;
    boolean isLit;
    public IntSupplier maxFuel;
    private HardcoreWallLantern litVariant;
    private HardcoreWallLantern unlitVariant;
    private Block regularLantern;

    public HardcoreWallLantern(Properties properties, boolean isLit, LanternGroup group, IntSupplier maxFuel, Block regularLantern) {
        super(properties);
        this.group = group;
        this.isLit = isLit;
        this.registerDefaultState(this.defaultBlockState().setValue(LIT, isLit));
        this.regularLantern = regularLantern;
        this.maxFuel = maxFuel;
    }

    public void setVariants(HardcoreWallLantern lit, HardcoreWallLantern unlit) {
        this.litVariant = lit;
        this.unlitVariant = unlit;
    }

    @Override
    public boolean isSoulVariant() {
        return group == MainMod.soulLanterns;
    }

    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {

        return LanternTools.interactLantern(state, world, pos, player, hand);
    }

    @Override
    public ItemStack getStack(Level world, BlockPos pos) {
        ItemStack stack = new ItemStack(this.group.getLanternBlock(this.isLit).asItem());
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity != null && blockEntity instanceof WallLanternBlockEntity lantern) {

            int remainingFuel = lantern.getFuel();

            if (!(this.isLit && remainingFuel >= lantern.getMaxFuel())) {
                CompoundTag nbt = new CompoundTag();
                nbt.putInt("Fuel", remainingFuel);
                stack.setTag(nbt);
            }
        }

        return stack;
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.setPlacedBy(world, pos, state, placer, itemStack);
        BlockEntity be = world.getBlockEntity(pos);
        if (be != null && be instanceof WallLanternBlockEntity && itemStack.getItem() instanceof LanternItem) {
            int fuel = LanternItem.getFuel(itemStack);
            ((WallLanternBlockEntity)be).setFuel(fuel);
        }

    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        WallLanternBlockEntity te = new WallLanternBlockEntity(pos, state);
        te.isLit = this.isLit;
        te.group = this.group;
        return te;
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
        return Utils.getTicker(type, AmendmentsCommonCompat.WALL_LANTERN_BLOCK_ENTITY.get(), world.isClientSide ? SwayingBlockTile::clientTick : WallLanternBlockEntity::tick);
    }

    @Override
    public int getMaxFuel() {
        return this.maxFuel.getAsInt();
    }

    @Override
    public void outOfFuel(Level world, BlockPos pos, BlockState state) {
        this.extinguish(world, pos, state, true);
    }

    @Override
    public InteractionResult attemptLight(Level world, BlockPos pos, BlockState state, Player player, ItemStack stack, InteractionHand hand) {
        return LanternTools.basicAttemptLight(world, pos, player, stack, hand);
    }

    @Override
    public boolean isLit() {
        return isLit;
    }

    @Override
    public boolean canLight(Level world, BlockPos pos) {
        if (this.isLit) {
            return false;
        } else {
            return ((WallLanternBlockEntity)world.getBlockEntity(pos)).getFuel() > 0 && !this.isLit;
        }
    }

    @Override
    public void extinguish(Level world, BlockPos pos, BlockState state, boolean playSound) {
        if (!world.isClientSide) {
            if (playSound) {
                world.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
        } else {
            TorchTools.displayParticle(ParticleTypes.LARGE_SMOKE, state, world, pos);
            TorchTools.displayParticle(ParticleTypes.LARGE_SMOKE, state, world, pos);
            TorchTools.displayParticle(ParticleTypes.SMOKE, state, world, pos);
            TorchTools.displayParticle(ParticleTypes.SMOKE, state, world, pos);
        }

        if (world.getBlockEntity(pos) != null && world.getBlockEntity(pos) instanceof WallLanternBlockEntity) {
            int newFuel = ((WallLanternBlockEntity)world.getBlockEntity(pos)).getFuel();
            BlockState oldState = world.getBlockState(pos);
            world.setBlock(pos, BlockStateTools.changeBlock(oldState, this.unlitVariant).setValue(LIT, false), 2);
            ((WallLanternBlockEntity)world.getBlockEntity(pos)).setFuel(newFuel);
            ((WallLanternBlockEntity)world.getBlockEntity(pos)).setHeldBlock(unlitVariant.regularLantern.defaultBlockState());
        }
    }

    public void light(Level world, BlockPos pos) {
        if (!world.isClientSide) {
            world.playSound(null, pos, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
        }

        if (world.getBlockEntity(pos) != null && world.getBlockEntity(pos) instanceof WallLanternBlockEntity) {
            int newFuel = ((WallLanternBlockEntity)world.getBlockEntity(pos)).getFuel();
            BlockState oldState = world.getBlockState(pos);
            world.setBlock(pos, BlockStateTools.changeBlock(oldState, this.litVariant).setValue(LIT, true), 2);
            ((WallLanternBlockEntity)world.getBlockEntity(pos)).setFuel(newFuel);
            ((WallLanternBlockEntity)world.getBlockEntity(pos)).setHeldBlock(litVariant.regularLantern.defaultBlockState());
        }

    }

    public List<ItemStack> getDrops(BlockState state, Builder builder) {
        BlockEntity blockEntity = builder.getParameter(LootContextParams.BLOCK_ENTITY);
        ItemStack stack = new ItemStack(this.group.getLanternBlock(this.isLit));
        List<ItemStack> stacks = new ArrayList();
        if (blockEntity != null && blockEntity instanceof IFuelBlockEntity lantern) {

            int remainingFuel = lantern.getFuel();

            if (!(this.isLit && remainingFuel >= lantern.getMaxFuel())) {
                CompoundTag nbt = new CompoundTag();
                nbt.putInt("Fuel", remainingFuel);
                stack.setTag(nbt);
            }
        }

        stacks.add(stack);
        return stacks;
    }
}
