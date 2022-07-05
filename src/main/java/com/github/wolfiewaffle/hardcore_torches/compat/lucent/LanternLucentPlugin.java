package com.github.wolfiewaffle.hardcore_torches.compat.lucent;

import com.github.wolfiewaffle.hardcore_torches.MainMod;
import com.github.wolfiewaffle.hardcore_torches.block.AbstractLanternBlock;
import com.github.wolfiewaffle.hardcore_torches.init.BlockInit;
import com.github.wolfiewaffle.hardcore_torches.init.ItemInit;
import com.github.wolfiewaffle.hardcore_torches.item.LanternItem;
import com.legacy.lucent.api.EntityBrightness;
import com.legacy.lucent.api.plugin.ILucentPlugin;
import com.legacy.lucent.api.plugin.LucentPlugin;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.items.IItemHandlerModifiable;
import top.theillusivec4.curios.api.CuriosApi;

@LucentPlugin
public class LanternLucentPlugin implements ILucentPlugin {

    @Override
    public String ownerModID() {
        return MainMod.MOD_ID;
    }

    @Override
    public void getEntityLightLevel(EntityBrightness entityBrightness) {
        Entity entity = entityBrightness.getEntity();

        if (entity instanceof Player) {
            if (ModList.get().isLoaded("curios")) {
                LazyOptional<IItemHandlerModifiable> curios;
                curios = CuriosApi.getCuriosHelper().getEquippedCurios((Player) entity);

                curios.ifPresent((handlerModifiable) -> {
                    for (int i = 0; i < handlerModifiable.getSlots(); i++) {
                        if (handlerModifiable.getStackInSlot(i).getItem() == ItemInit.LIT_LANTERN.get()) {
                            entityBrightness.setLightLevel(AbstractLanternBlock.LANTERN_LIGHT_LEVEL);
                        }
                        if (handlerModifiable.getStackInSlot(i).getItem() == Items.LANTERN) {
                            entityBrightness.setLightLevel(15);
                        }
                        if (handlerModifiable.getStackInSlot(i).getItem() == Items.SOUL_LANTERN) {
                            entityBrightness.setLightLevel(10);
                        }
                    }
                });
            }
        }
    }
}
