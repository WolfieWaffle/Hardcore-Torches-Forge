package com.github.wolfiewaffle.hardcore_torches.compat.curio;

import com.github.wolfiewaffle.hardcore_torches.item.LanternItem;
import net.minecraft.entity.LivingEntity;
import net.minecraftforge.common.util.LazyOptional;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

public class LanternCurio implements ICurio {

//    @Override
//    public ItemStack getStack() {
//        return null;
//    }

    @Override
    public void curioTick(String identifier, int index, LivingEntity entity) {
        LazyOptional<ICuriosItemHandler> stackHandler = CuriosApi.getCuriosHelper().getCuriosHandler(entity);
        stackHandler.ifPresent((handler) -> {
            IDynamicStackHandler dynamicStackHandler = handler.getCurios().get(identifier).getStacks();

            dynamicStackHandler.setStackInSlot(index, LanternItem.addFuel(dynamicStackHandler.getStackInSlot(index), entity.level, -1));
        });
    }
}