package com.github.wolfiewaffle.hardcore_torches.compat.amendments;

import com.github.wolfiewaffle.hardcore_torches.block.AbstractLanternBlock;
import com.github.wolfiewaffle.hardcore_torches.blockentity.IFuelBlock;
import com.github.wolfiewaffle.hardcore_torches.blockentity.IFuelBlockEntity;
import com.github.wolfiewaffle.hardcore_torches.config.Config;
import com.github.wolfiewaffle.hardcore_torches.init.BlockEntityInit;
import com.github.wolfiewaffle.hardcore_torches.init.ItemInit;
import com.github.wolfiewaffle.hardcore_torches.item.LanternItem;
import com.github.wolfiewaffle.hardcore_torches.item.OilCanItem;
import com.github.wolfiewaffle.hardcore_torches.util.BlockStateTools;
import com.github.wolfiewaffle.hardcore_torches.util.ETorchState;
import com.github.wolfiewaffle.hardcore_torches.util.LanternGroup;
import com.github.wolfiewaffle.hardcore_torches.util.TorchTools;
import net.mehvahdjukaar.amendments.common.block.WallLanternBlock;
import net.mehvahdjukaar.amendments.common.tile.SwayingBlockTile;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
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
    private AbstractLanternBlock regularLantern;

    public HardcoreWallLantern(Properties properties, boolean isLit, LanternGroup group, IntSupplier maxFuel) {
        super(properties);
        this.group = group;
        this.isLit = isLit;
        this.registerDefaultState(this.defaultBlockState().setValue(LIT, isLit));
        this.maxFuel = maxFuel;
    }

    public void setVariants(HardcoreWallLantern lit, HardcoreWallLantern unlit) {
        this.litVariant = lit;
        this.unlitVariant = unlit;
    }

    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack stack = player.getItemInHand(hand);
        BlockEntity be = world.getBlockEntity(pos);
        if (player.isCrouching() && Config.pickUpLanterns.get()) {
            if (!world.isClientSide) {
                player.addItem(this.getStack(world, pos));
            }

            world.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
            if (!world.isClientSide) {
                world.playSound(null, pos, SoundEvents.LANTERN_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
            }

            player.swing(hand);
            return InteractionResult.SUCCESS;
        } else if (!this.isLit && this.itemValid(stack, ETorchState.LIT)) {
            return this.attemptLight(world, pos, state, player, hand);
        } else if (stack.is(ItemTags.COALS) && !(Boolean)Config.lanternsNeedCan.get()) {
            if (be instanceof WallLanternBlockEntity && !world.isClientSide) {
                int oldFuel = ((WallLanternBlockEntity)be).getFuel();
                if (oldFuel < this.getMaxFuel()) {
                    if (oldFuel + Config.defLanternFuelItem.get() < this.getMaxFuel()) {
                        world.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
                    } else {
                        world.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
                    }

                    stack.grow(-1);
                    ((WallLanternBlockEntity)be).setFuel(Math.min(oldFuel + Config.defLanternFuelItem.get(), this.getMaxFuel()));
                }
            }

            player.swing(hand);
            return InteractionResult.SUCCESS;
        } else if (stack.getItem() instanceof OilCanItem && Config.lanternsNeedCan.get()) {
            if (be instanceof IFuelBlockEntity && !world.isClientSide && OilCanItem.fuelBlock((IFuelBlockEntity) be, world, stack)) {
                world.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
            }

            player.swing(hand);
            return InteractionResult.SUCCESS;
        } else {
            boolean showFuel = (stack.isEmpty() || stack.getItem() == ItemInit.OIL_CAN.get()) && Config.fuelMessage.get();
            if (be.getType() == BlockEntityInit.LANTERN_BLOCK_ENTITY.get() && hand == InteractionHand.MAIN_HAND && !world.isClientSide && showFuel) {
                player.displayClientMessage(Component.literal("Fuel: " + ((WallLanternBlockEntity) be).getFuel()), true);
            }

            if (Config.lanternsNeedCan.get() && hand == InteractionHand.MAIN_HAND && !stack.isEmpty() && stack.getItem() != ItemInit.OIL_CAN.get() && !world.isClientSide) {
                player.displayClientMessage(Component.literal("Requires an Oil Can to fuel!"), true);
            }

            if (Config.handUnlightLantern.get() && this.isLit && !TorchTools.canLight(stack.getItem(), this.defaultBlockState())) {
                this.extinguish(world, pos, state, true);
                return InteractionResult.SUCCESS;
            } else {
                return super.use(state, world, pos, player, hand, hit);
            }
        }
    }

    protected ItemStack getStack(Level world, BlockPos pos) {
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

    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.setPlacedBy(world, pos, state, placer, itemStack);
        BlockEntity be = world.getBlockEntity(pos);
        if (be != null && be instanceof WallLanternBlockEntity && itemStack.getItem() instanceof LanternItem) {
            int fuel = LanternItem.getFuel(itemStack);
            ((WallLanternBlockEntity)be).setFuel(fuel);
        }

    }

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

    public int getMaxFuel() {
        return this.maxFuel.getAsInt();
    }

    public void outOfFuel(Level world, BlockPos pos, BlockState state) {
        this.extinguish(world, pos, state, true);
    }

    public InteractionResult attemptLight(Level world, BlockPos pos, BlockState state, Player player, InteractionHand hand) {
        if (!world.isClientSide) {
            if (((WallLanternBlockEntity)world.getBlockEntity(pos)).getFuel() < Config.minLanternIgnitionFuel.get()) {
                world.playSound(null, pos, SoundEvents.LANTERN_HIT, SoundSource.BLOCKS, 1.0F, 1.0F);
                player.displayClientMessage(Component.literal("Not enough fuel to ignite!"), true);
            } else if (this.attemptUseItem(player.getItemInHand(hand), player, hand, ETorchState.LIT)) {
                this.light(world, pos);
            }
        }

        player.swing(hand);
        return InteractionResult.SUCCESS;
    }

    public boolean canLight(Level world, BlockPos pos) {
        if (this.isLit) {
            return false;
        } else {
            return ((WallLanternBlockEntity)world.getBlockEntity(pos)).getFuel() > 0 && !this.isLit;
        }
    }

    public void extinguish(Level world, BlockPos pos, BlockState state, boolean playSound) {
        if (!world.isClientSide) {
            if (playSound) {
                world.playSound((Player)null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1.0F, 1.0F);
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
            world.setBlockAndUpdate(pos, BlockStateTools.changeBlock(oldState, this.unlitVariant).setValue(LIT, false));
            ((WallLanternBlockEntity)world.getBlockEntity(pos)).setFuel(newFuel);
        }

    }

    public void light(Level world, BlockPos pos) {
        if (!world.isClientSide) {
            world.playSound(null, pos, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
        }

        if (world.getBlockEntity(pos) != null && world.getBlockEntity(pos) instanceof WallLanternBlockEntity) {
            int newFuel = ((WallLanternBlockEntity)world.getBlockEntity(pos)).getFuel();
            BlockState oldState = world.getBlockState(pos);
            world.setBlockAndUpdate(pos, BlockStateTools.changeBlock(oldState, this.litVariant).setValue(LIT, true));
            ((WallLanternBlockEntity)world.getBlockEntity(pos)).setFuel(newFuel);
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
