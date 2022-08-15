package com.github.wolfiewaffle.hardcore_torches.block;

import com.github.wolfiewaffle.hardcore_torches.MainMod;
import com.github.wolfiewaffle.hardcore_torches.blockentity.FuelBlockEntity;
import com.github.wolfiewaffle.hardcore_torches.config.Config;
import com.github.wolfiewaffle.hardcore_torches.init.BlockEntityInit;
import com.github.wolfiewaffle.hardcore_torches.util.ETorchState;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ExperienceBottleItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.function.IntSupplier;

public class SoulLanternBlock extends AbstractLanternBlock {
    public static final int SOUL_LANTERN_LIGHT_LEVEL = 10;
    public static final int bottleAmount = 100;

    public SoulLanternBlock(Properties prop, boolean isLit, IntSupplier maxFuel) {
        super(prop, isLit, maxFuel);
        this.registerDefaultState(this.stateDefinition.any().setValue(HANGING, Boolean.valueOf(false)).setValue(WATERLOGGED, Boolean.valueOf(false)));
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack stack = player.getItemInHand(hand);
        BlockEntity be = world.getBlockEntity(pos);

        // Pick up lantern
        if (player.isCrouching() && Config.pickUpLanterns.get()) {
            if (!world.isClientSide) player.addItem(getStack(world, pos));
            world.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
            if (!world.isClientSide) world.playSound(null, pos, SoundEvents.LANTERN_PLACE, SoundSource.BLOCKS, 1f, 1f);
            player.swing(hand);
            return InteractionResult.SUCCESS;
        }

        // Adding fuel with Bottle o Enchanting
        if (stack.getItem() instanceof ExperienceBottleItem) {
            if (be instanceof FuelBlockEntity && !world.isClientSide) {
                fuelBlock((FuelBlockEntity) be, world, stack, bottleAmount, true);
                world.playSound(null, pos, SoundEvents.BOTTLE_FILL_DRAGONBREATH, SoundSource.BLOCKS, 1f, 1f);
                displayFuel(player, ((FuelBlockEntity) be).getFuel());
            }
            player.swing(hand);
            return InteractionResult.SUCCESS;
        }

        // Adding fuel with Attunement
        if (stack.is(MainMod.SOUL_ITEMS)) {
            if (be instanceof FuelBlockEntity && !world.isClientSide) {
                int maxExp = Math.min(player.totalExperience, Config.expIncrement.get());
                int maxAmount = Math.max(0, getMaxFuel() - ((FuelBlockEntity) be).getFuel());
                int addAmount = Math.min(maxAmount, (int) (maxExp * Config.soulExpRatio.get()));
                int takeAmount = (int) Math.max(0, Math.ceil(addAmount / Config.soulExpRatio.get()));

                if (addAmount > 0) {
                    fuelBlock((FuelBlockEntity) be, world, stack, addAmount, false);
                    player.giveExperiencePoints(-takeAmount);
                    world.playSound(null, pos, SoundEvents.BOTTLE_FILL_DRAGONBREATH, SoundSource.BLOCKS, 1f, 1f);
                    displayFuel(player, ((FuelBlockEntity) be).getFuel());
                }
            }
            player.swing(hand);
            return InteractionResult.SUCCESS;
        }

        boolean showFuel = Config.fuelMessage.get();

        // Fuel message
        if (be.getType() == BlockEntityInit.LANTERN_BLOCK_ENTITY.get() && hand == InteractionHand.MAIN_HAND && !world.isClientSide && showFuel) {
            displayFuel(player, ((FuelBlockEntity) be).getFuel());
        }

        // Igniting
        if (!this.isLit && itemValid(stack, ETorchState.LIT)) {
            return attemptLight(world, pos, state, player, stack, hand);
        }

        return InteractionResult.PASS;
    }

    private void displayFuel(Player player, int amount) {
        player.displayClientMessage(Component.literal("Spirit: " + amount), true);
    }

    public static boolean fuelBlock(FuelBlockEntity be, Level world, ItemStack stack, int amount, boolean consume) {
        if (!world.isClientSide) {
            if (consume) stack.grow(-1);
            be.setFuel(be.getFuel() + amount);
        }
        return true;
    }

    @Override
    public TagKey getFreeLightItems() {
        return MainMod.FREE_LANTERN_LIGHT_ITEMS;
    }

    @Override
    public TagKey getDamageLightItems() {
        return MainMod.DAMAGE_LANTERN_LIGHT_ITEMS;
    }

    @Override
    public TagKey getConsumeLightItems() {
        return MainMod.CONSUME_LANTERN_LIGHT_ITEMS;
    }
}
