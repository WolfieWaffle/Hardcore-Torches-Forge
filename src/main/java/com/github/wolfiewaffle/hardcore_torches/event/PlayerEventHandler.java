package com.github.wolfiewaffle.hardcore_torches.event;

import com.github.wolfiewaffle.hardcore_torches.config.Config;
import com.github.wolfiewaffle.hardcore_torches.item.LanternItem;
import com.github.wolfiewaffle.hardcore_torches.item.TorchItem;
import com.github.wolfiewaffle.hardcore_torches.util.ETorchState;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class PlayerEventHandler {

    @SubscribeEvent
    public void playerTick(TickEvent.PlayerTickEvent event) {
        Inventory inventory = event.player.getInventory();
        Level world = event.player.level;

        if (!world.isClientSide) {

            for (int i = 0; i < inventory.getContainerSize(); i++) {
                ItemStack stack = inventory.getItem(i);

                if (stack.getItem() instanceof TorchItem) {
                    if (Config.tickInInventory.get() && ((TorchItem) stack.getItem()).burnState == ETorchState.LIT)
                        inventory.setItem(i, TorchItem.addFuel(stack, world, -1));
                } else if (stack.getItem() instanceof LanternItem) {
                    if (Config.tickInInventory.get() && ((LanternItem) stack.getItem()).isLit)
                        inventory.setItem(i, LanternItem.addFuel(stack, world, -1));
                }
            }
        }
    }
}
