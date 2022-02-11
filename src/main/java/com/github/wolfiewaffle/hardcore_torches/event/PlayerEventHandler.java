package com.github.wolfiewaffle.hardcore_torches.event;

import com.github.wolfiewaffle.hardcore_torches.config.Config;
import com.github.wolfiewaffle.hardcore_torches.item.LanternItem;
import com.github.wolfiewaffle.hardcore_torches.item.TorchItem;
import com.github.wolfiewaffle.hardcore_torches.util.ETorchState;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Random;

public class PlayerEventHandler {
    private static Random random = new Random();

    @SubscribeEvent
    public void playerTick(TickEvent.PlayerTickEvent event) {
        Inventory inventory = event.player.getInventory();
        Level world = event.player.level;

        // There are 2 phases to tick event, apparently. I chose START arbitrarily.
        if (!world.isClientSide && event.phase == TickEvent.Phase.START) {
            BlockPos pos = event.player.getOnPos().above();
            int rainEffect = Config.invExtinguishInRain.get();
            boolean doRain = rainEffect > 0 && world.isRainingAt(pos);

            for (int i = 0; i < inventory.getContainerSize(); i++) {
                ItemStack stack = inventory.getItem(i);
                Item item = stack.getItem();

                // For all torches and lanterns
                if (item instanceof TorchItem) {
                    TorchItem torchItem = (TorchItem) item;
                    boolean rain = doRain;
                    boolean mainOrOffhand = (i == inventory.selected || inventory.offhand.get(0) == stack);
                    if (rainEffect == 1 && doRain) rain = mainOrOffhand ? true : false;

                    // Rain
                    if (rain) {
                        if (torchItem.burnState == ETorchState.LIT) {
                            if (Config.torchesSmolder.get()) {
                                inventory.setItem(i, TorchItem.stateStack(stack, ETorchState.SMOLDERING));
                                world.playSound(null, pos.above(), SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.5f, 1f);
                            } else {
                                inventory.setItem(i, TorchItem.stateStack(stack, ETorchState.UNLIT));
                                world.playSound(null, pos.above(), SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.5f, 1f);
                            }
                            continue;
                        } else if (torchItem.burnState == ETorchState.SMOLDERING) {
                            if (!Config.torchesSmolder.get()) {
                                inventory.setItem(i, TorchItem.stateStack(stack, ETorchState.UNLIT));
                                world.playSound(null, pos.above(), SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.5f, 1f);
                                continue;
                            }
                        }
                    }

                    // Water
                    if (Config.invExtinguishInWater.get() > 0) {
                        if (event.player.isUnderWater()) {
                            if (torchItem.burnState == ETorchState.LIT || torchItem.burnState == ETorchState.SMOLDERING) {
                                if ((Config.invExtinguishInWater.get() == 1 && mainOrOffhand) || Config.invExtinguishInWater.get() == 2) {
                                    inventory.setItem(i, TorchItem.stateStack(stack, ETorchState.UNLIT));
                                    world.playSound(null, pos.above(), SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.5f, 1f);
                                    continue;
                                }
                            }
                        }
                    }

                    // Fuel
                    if (Config.tickInInventory.get() && torchItem.burnState == ETorchState.LIT)
                        inventory.setItem(i, TorchItem.addFuel(stack, world, -1));
                    if (Config.tickInInventory.get() && torchItem.burnState == ETorchState.SMOLDERING)
                        if (random.nextInt(3) == 0) inventory.setItem(i, TorchItem.addFuel(stack, world, -1));

                } else if (item instanceof LanternItem) {

                    // Lantern
                    if (Config.tickInInventory.get() && ((LanternItem) item).isLit)
                        inventory.setItem(i, LanternItem.addFuel(stack, world, -1));
                }
            }
        }
    }
}
