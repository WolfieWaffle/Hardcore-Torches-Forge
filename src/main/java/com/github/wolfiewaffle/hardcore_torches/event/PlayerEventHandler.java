package com.github.wolfiewaffle.hardcore_torches.event;

import com.github.wolfiewaffle.hardcore_torches.config.Config;
import com.github.wolfiewaffle.hardcore_torches.item.LanternItem;
import com.github.wolfiewaffle.hardcore_torches.item.TorchItem;
import com.github.wolfiewaffle.hardcore_torches.util.ETorchState;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Random;

public class PlayerEventHandler {
    private static Random random = new Random();

    @SubscribeEvent
    public void playerTick(TickEvent.PlayerTickEvent event) {
        PlayerInventory inventory = event.player.inventory;
        World world = event.player.level;

        // There are 2 phases to tick event, apparently. I chose START arbitrarily.
        if (!world.isClientSide && event.phase == TickEvent.Phase.START) {

            for (int i = 0; i < inventory.getContainerSize(); i++) {
                ItemStack stack = inventory.getItem(i);

                if (stack.getItem() instanceof TorchItem) {
                    if (Config.tickInInventory.get() && ((TorchItem) stack.getItem()).burnState == ETorchState.LIT)
                        inventory.setItem(i, TorchItem.addFuel(stack, world, -1));
                    if (Config.tickInInventory.get() && ((TorchItem) stack.getItem()).burnState == ETorchState.SMOLDERING)
                        if (random.nextInt(3) == 0) inventory.setItem(i, TorchItem.addFuel(stack, world, -1));
                } else if (stack.getItem() instanceof LanternItem) {
                    if (Config.tickInInventory.get() && ((LanternItem) stack.getItem()).isLit)
                        inventory.setItem(i, LanternItem.addFuel(stack, world, -1));
                }
            }
        }
    }
}
