package com.github.wolfiewaffle.hardcore_torches.event;

import com.github.wolfiewaffle.hardcore_torches.compat.curio.BandolierCurio;
import com.github.wolfiewaffle.hardcore_torches.config.Config;
import com.github.wolfiewaffle.hardcore_torches.item.BandolierItem;
import com.github.wolfiewaffle.hardcore_torches.item.LanternItem;
import com.github.wolfiewaffle.hardcore_torches.item.TorchItem;
import com.github.wolfiewaffle.hardcore_torches.util.ETorchState;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.event.entity.player.UseItemOnBlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.Random;

public class PlayerEventHandler {
    private static Random random = new Random();

    @SubscribeEvent
    public void playerTick(PlayerTickEvent.Pre event) {
        Player player = event.getEntity();
        Inventory inventory = player.getInventory();
        Level world = player.level();

        // There are 2 phases to tick event, apparently. I chose START arbitrarily.
        BlockPos pos = player.getOnPos().above();
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
                    if (player.isUnderWater()) {
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

            } else if (item instanceof BandolierItem bandolier) {
                if (Config.tickInInventory.get()) {
                    inventory.setItem(i, BandolierItem.getTickedBandolier(stack));
                }
            }
        }
    }

    @SubscribeEvent
    public void playerInteract(UseItemOnBlockEvent event) {
        if (Config.bandolierInteractMode.get() == 0) return;
        if (event.getCancellationResult() == ItemInteractionResult.CONSUME || event.getCancellationResult() == ItemInteractionResult.SUCCESS || event.getCancellationResult() == ItemInteractionResult.FAIL) return;

        // Detection
        int mode = Config.bandolierInteractMode.get();
        switch (mode) {
            case 1:
                if (event.getHand() == InteractionHand.MAIN_HAND) return;
                break;
            case 2:
                if (event.getHand() == InteractionHand.MAIN_HAND) return;
                if (!event.getPlayer().getItemInHand(InteractionHand.MAIN_HAND).isEmpty()) return;
                break;
        }

        if (!ModList.get().isLoaded("curios")) return;



        if (mode != 0) {

            ItemStack item = event.getItemStack();
            if (mode == 1 || item.isEmpty()) {

                if (event.getCancellationResult() == ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION) {
                    if (Minecraft.getInstance().hitResult instanceof BlockHitResult result) {
                        BandolierCurio.handleRightClick(event, result);
                    }
                }
            }
        }
    }

//    @SubscribeEvent
//    public void playerInteract(PlayerInteractEvent.RightClickBlock event) {
//        if (!Config.placeHardcoreCampfire.get()) {
//            return;
//        } else {
//            System.out.println("SIDE " + event.getSide());
//            if (event.getItemStack().getItem() == Items.CAMPFIRE) {
//                event.setCanceled(true);
//                InteractionResult result = ItemInit.UNLIT_CAMPFIRE.get().useOn(new UseOnContext(event.getEntity(), event.getHand(), event.getHitVec()));
//                if (result == InteractionResult.SUCCESS) {
//                    event.getEntity().swing(event.getHand());
//                }
//            }
//        }
//    }
}
