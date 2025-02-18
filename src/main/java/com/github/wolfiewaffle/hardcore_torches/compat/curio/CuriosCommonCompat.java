package com.github.wolfiewaffle.hardcore_torches.compat.curio;

import com.github.wolfiewaffle.hardcore_torches.init.ItemInit;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import top.theillusivec4.curios.api.CuriosCapability;

public class CuriosCommonCompat {

    public static void attachCapabilities(final RegisterCapabilitiesEvent event) {
        event.registerItem(CuriosCapability.ITEM, (stack, context) -> new LanternCurio(stack), ItemInit.LIT_LANTERN.get(), ItemInit.LIT_SOUL_LANTERN.get());
        event.registerItem(CuriosCapability.ITEM, (stack, context) -> new BandolierCurio(stack), ItemInit.BANDOLIER.get());
    }
}
