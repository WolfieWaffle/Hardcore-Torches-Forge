package com.github.wolfiewaffle.hardcore_torches.compat.curio;

import com.github.wolfiewaffle.hardcore_torches.config.Config;
import com.github.wolfiewaffle.hardcore_torches.item.BandolierItem;
import com.github.wolfiewaffle.hardcore_torches.item.TorchItem;
import com.github.wolfiewaffle.hardcore_torches.util.ETorchState;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.event.entity.player.UseItemOnBlockEvent;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import java.util.Optional;

public class BandolierCurio implements ICurio {
    ItemStack stack;

    public BandolierCurio(ItemStack stack) {
        this.stack = stack;
    }

    @Override
    public void curioTick(SlotContext slotContext) {
        if (!Config.tickInInventory.get()) return;

        String identifier = slotContext.identifier();
        int index = slotContext.index();
        LivingEntity entity = slotContext.entity();

        Optional<ICuriosItemHandler> stackHandler = CuriosApi.getCuriosInventory(entity);

        stackHandler.ifPresent((handler) -> {
            IDynamicStackHandler dynamicStackHandler = handler.getCurios().get(identifier).getStacks();

            dynamicStackHandler.setStackInSlot(index, BandolierItem.getTickedBandolier(dynamicStackHandler.getStackInSlot(index)));
        });
    }

    @Override
    public ItemStack getStack() {
        return stack;
    }

    public boolean tryPlace(Player player, BlockPlaceContext placeContext) {
        ItemStack stack = placeContext.getItemInHand();

        if (stack.getItem() instanceof BlockItem blockItem) {
            InteractionResult result = blockItem.place(placeContext);
            return result != InteractionResult.FAIL;
        }

        return false;
    }

    public static void handleRightClick(UseItemOnBlockEvent event, BlockHitResult hitResult) {
        Player player = event.getPlayer();
        InteractionHand hand = event.getHand();

        CuriosApi.getCuriosInventory(player).ifPresent(curiosInventory -> {
            curiosInventory.getStacksHandler("belt").ifPresent(slotInventory -> {

                IDynamicStackHandler stackHandler = slotInventory.getStacks();

                for (int i = 0; i < stackHandler.getSlots(); i++) {
                    ICurio curio = CuriosCapability.ITEM.getCapability(stackHandler.getStackInSlot(i), null);

                    if (curio instanceof BandolierCurio bandolier) {
                        ItemStack bandolierStack = bandolier.stack;
                        ItemStack torchStack = BandolierItem.getNextTorchOrEmpty(bandolierStack, true);

                        if (torchStack.getItem() instanceof TorchItem torchItem) {
                            ItemStack placementStack = TorchItem.stateStack(torchStack, torchItem.burnState != ETorchState.BURNT ? ETorchState.LIT : ETorchState.BURNT);

                            BlockPlaceContext context = new BlockPlaceContext(
                                    player,
                                    hand,
                                    placementStack,
                                    hitResult
                            );

                            if (bandolier.tryPlace(player, context)) {
                                BandolierItem.deleteOneTorch(bandolierStack, torchStack);
                                player.swing(InteractionHand.MAIN_HAND);
                                event.setCanceled(true);
                            }
                        }
                    }
                }
            });
        });
    }
}
