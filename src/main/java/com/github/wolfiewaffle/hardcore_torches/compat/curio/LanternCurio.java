package com.github.wolfiewaffle.hardcore_torches.compat.curio;

import com.github.wolfiewaffle.hardcore_torches.config.Config;
import com.github.wolfiewaffle.hardcore_torches.item.LanternItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

public class LanternCurio implements ICurio {
    ItemStack stack;

    public LanternCurio(ItemStack stack) {
        this.stack = stack;
    }

    @Override
    public ItemStack getStack() {
        return stack;
    }

    @Override
    public void curioTick(SlotContext slotContext) {
        String identifier = slotContext.identifier();
        int index = slotContext.index();
        LivingEntity entity = slotContext.entity();

        LazyOptional<ICuriosItemHandler> stackHandler = CuriosApi.getCuriosHelper().getCuriosHandler(entity);
        stackHandler.ifPresent((handler) -> {
            IDynamicStackHandler dynamicStackHandler = handler.getCurios().get(identifier).getStacks();

            if (Config.tickInInventory.get()) {
                ItemStack newStack = LanternItem.addFuel(dynamicStackHandler.getStackInSlot(index), entity.level, -1);
                dynamicStackHandler.setStackInSlot(index, newStack);
                this.stack = newStack;
            }
        });
    }
}
