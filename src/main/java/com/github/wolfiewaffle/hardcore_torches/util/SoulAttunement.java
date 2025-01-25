package com.github.wolfiewaffle.hardcore_torches.util;

import com.github.wolfiewaffle.hardcore_torches.MainMod;
import com.github.wolfiewaffle.hardcore_torches.blockentity.IFuelBlock;
import com.github.wolfiewaffle.hardcore_torches.blockentity.IFuelBlockEntity;
import com.github.wolfiewaffle.hardcore_torches.config.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ExperienceBottleItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class SoulAttunement {

    public static InteractionResult soulAttune(Level world, BlockPos pos, IFuelBlockEntity fuelBlockEntity, IFuelBlock fuelBlock, Player player, InteractionHand hand) {

        // Get variables
        ItemStack stack = player.getItemInHand(hand);

        // Adding fuel with Bottle o Enchanting
        if (stack.getItem() instanceof ExperienceBottleItem) {
            if (!world.isClientSide) {
                int oldFuel = fuelBlockEntity.getFuel();

                // Add fuel if more can be added
                if (fuelBlockEntity.canAddFuel()) {
                    if (oldFuel + Config.defLanternFuelItem.get() < fuelBlock.getMaxFuel()) {
                        world.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1f, 1.0f);
                    } else {
                        world.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1f, 1.0f);
                    }
                    stack.grow(-1);
                    fuelBlockEntity.setFuel((int) (oldFuel + (Config.bottleExpAmount.get() * Config.soulExpRatio.get())));
                    displaySpirit(player, fuelBlockEntity.getFuel());
                }
            }
            player.swing(hand);
            return InteractionResult.SUCCESS;
        }

        // Adding fuel with Attunement
        if (stack.is(MainMod.SOUL_ITEMS)) {
            if (!world.isClientSide) {
                int xp = getTotalXP(player);
                int maxIncrement = Math.min(xp, Config.expIncrement.get());
                int emptySpace = Math.max(0, fuelBlockEntity.getMaxFuel() - fuelBlockEntity.getFuel());
                int addAmount = Math.min(emptySpace, (int) (maxIncrement * Config.soulExpRatio.get()));
                int takeAmount = (int) Math.max(0, Math.ceil(addAmount / Config.soulExpRatio.get()));

                if (xp > 0) {
                    if (addAmount > 0 && fuelBlockEntity.canAddFuel()) {
                        fuelBlockEntity.setFuel(fuelBlockEntity.getFuel() + addAmount);
                        player.giveExperiencePoints(-takeAmount);
                        world.playSound(null, pos, SoundEvents.BOTTLE_FILL_DRAGONBREATH, SoundSource.BLOCKS, 1f, 1f);
                        displaySpirit(player, fuelBlockEntity.getFuel());
                    }
                } else {
                    player.displayClientMessage(Component.literal("You don't have enough XP!"), true);
                }
            }
            player.swing(hand);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.SUCCESS;
    }

    public static void displaySpirit(Player player, int amount) {
        player.displayClientMessage(Component.literal("Spirit: " + amount), true);
    }

    private static int getTotalXP(Player player) {
        int level = player.experienceLevel;
        int levelTotal = 0;

        if (level >= 1 && level <= 16) { // 1-16
            levelTotal = (int) (Math.pow(level, 2) + 6 * level);
        } else if (level >= 17 && level <= 31) { // 17-31
            levelTotal = (int) (2.5d * Math.pow(level, 2) - 40.5d * level + 360);
        } else if (level >= 32) { // 32+
            levelTotal = (int) (4.5d * Math.pow(level, 2) - 162.5 * level + 2220);
        }

        return levelTotal + player.totalExperience;
    }
}
