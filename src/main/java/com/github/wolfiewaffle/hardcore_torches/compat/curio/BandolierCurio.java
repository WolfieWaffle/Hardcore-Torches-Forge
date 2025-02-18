package com.github.wolfiewaffle.hardcore_torches.compat.curio;

import com.github.wolfiewaffle.hardcore_torches.item.BandolierItem;
import com.github.wolfiewaffle.hardcore_torches.item.TorchItem;
import com.github.wolfiewaffle.hardcore_torches.util.ETorchState;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.phys.BlockHitResult;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

public class BandolierCurio implements ICurio {
    ItemStack stack;

    public BandolierCurio(ItemStack stack) {
        this.stack = stack;
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

    public static void handleRightClick(Player player, InteractionHand hand, BlockHitResult vec) {
        CuriosApi.getCuriosInventory(player).ifPresent(curiosInventory -> {
            curiosInventory.getStacksHandler("belt").ifPresent(slotInventory -> {

                IDynamicStackHandler stackHandler = slotInventory.getStacks();

                for (int i = 0; i < stackHandler.getSlots(); i++) {
                    ICurio curio = CuriosCapability.ITEM.getCapability(stackHandler.getStackInSlot(i), null);

                    if (curio instanceof BandolierCurio bandolier) {
                        ItemStack bandolierStack = bandolier.stack;
                        ItemStack torchStack = BandolierItem.getNextTorchOrEmpty(bandolierStack);

                        BlockPlaceContext context = new BlockPlaceContext(
                                player,
                                hand,
                                TorchItem.stateStack(torchStack, ETorchState.LIT),
                                vec
                        );

                        if (bandolier.tryPlace(player, context)) {
                            BandolierItem.deleteOneTorch(bandolierStack, torchStack);
                        }
                    }
                }
            });
        });
    }
}
