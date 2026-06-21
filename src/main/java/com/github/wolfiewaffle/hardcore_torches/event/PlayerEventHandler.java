package com.github.wolfiewaffle.hardcore_torches.event;

import com.github.wolfiewaffle.hardcore_torches.compat.curio.BandolierCurio;
import com.github.wolfiewaffle.hardcore_torches.config.Config;
import com.github.wolfiewaffle.hardcore_torches.item.BandolierItem;
import com.github.wolfiewaffle.hardcore_torches.item.LanternItem;
import com.github.wolfiewaffle.hardcore_torches.item.TorchItem;
import com.github.wolfiewaffle.hardcore_torches.util.ETorchState;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.UseItemOnBlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.ArrayList;
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
    public void joinWorld(EntityJoinLevelEvent event) {
        boolean foundOp = false;
        String modName = "";

        if (event.getLevel().isClientSide && event.getEntity() instanceof Player player && Config.optimizerWarning.get()) {
            if (ModList.get().isLoaded("lithium")) {
                foundOp = true;
                modName = "Lithium";
            }

            if (foundOp) player.sendSystemMessage(Component.literal("WARNING: Hardcore Torches has detected " + modName + ". Some configuration options for this mod must be changed, or the campfire will not work. See the mod page for more information. This message can be disabled in the config.").withStyle(ChatFormatting.DARK_RED));
        }
    }

    @SubscribeEvent
    public void playerInteract(UseItemOnBlockEvent event) {
        // IDK why we need this but we do
        if (event.getUsePhase() != UseItemOnBlockEvent.UsePhase.ITEM_BEFORE_BLOCK) return;

        // Mode zero in the config is no placement
        int mode = Config.bandolierInteractMode.get();
        if (mode == 0) return;

        // We don't run on mainhand
        if (event.getHand() == InteractionHand.MAIN_HAND) return;

        System.out.println("INTERACT");

        Player player = event.getPlayer();
        if (player == null) return;

        if (event.getCancellationResult() == ItemInteractionResult.CONSUME || event.getCancellationResult() == ItemInteractionResult.SUCCESS || event.getCancellationResult() == ItemInteractionResult.FAIL) return;

        // Return if we are in empty mainhand mode and its not empty
        if (mode == 2 && !player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty()) return;


        if (!ModList.get().isLoaded("curios")) return;

        ItemStack item = event.getItemStack();
        if (mode == 1 || item.isEmpty()) {
            if (event.getCancellationResult() == ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION) {
                if (event.getLevel().isClientSide) return;
                BlockHitResult blockhitresult = item.getItem().getPlayerPOVHitResult(event.getLevel(), player, ClipContext.Fluid.NONE);

                if (blockhitresult != null && blockhitresult instanceof BlockHitResult result) {
                    BandolierCurio.handleRightClick(event, result);
                }
            }
        }
    }
}
