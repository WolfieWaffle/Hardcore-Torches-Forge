package com.github.wolfiewaffle.hardcore_torches.compat.lucent;

import com.github.wolfiewaffle.hardcore_torches.block.AbstractLanternBlock;
import com.github.wolfiewaffle.hardcore_torches.init.ItemInit;
import com.legacy.lucent.api.EntityBrightness;
import com.legacy.lucent.api.plugin.ILucentPlugin;
import com.legacy.lucent.api.plugin.LucentPlugin;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.items.IItemHandlerModifiable;
import top.theillusivec4.curios.api.CuriosApi;

@LucentPlugin
public class LanternLucentPlugin implements ILucentPlugin {

    @Override
    public void getEntityLightLevel(EntityBrightness entityBrightness) {
        Entity entity = entityBrightness.getEntity();

        if (entity instanceof PlayerEntity) {
            if (ModList.get().isLoaded("curios")) {
                LazyOptional<IItemHandlerModifiable> curios;
                curios = CuriosApi.getCuriosHelper().getEquippedCurios((PlayerEntity) entity);

                curios.ifPresent((handlerModifiable) -> {
                    for (int i = 0; i < handlerModifiable.getSlots(); i++) {
                        if (handlerModifiable.getStackInSlot(i).getItem() == ItemInit.LIT_LANTERN.get()) {
                            entityBrightness.setLightLevel(AbstractLanternBlock.LANTERN_LIGHT_LEVEL);
                        }
                    }
                });
            }
        }
    }
}