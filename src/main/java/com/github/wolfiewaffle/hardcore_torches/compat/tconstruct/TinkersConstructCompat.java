package com.github.wolfiewaffle.hardcore_torches.compat.tconstruct;

import net.minecraft.world.item.ItemStack;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

public class TinkersConstructCompat {

    public static boolean isBroken(ItemStack stack) {
        ToolStack tool = ToolStack.from(stack);
        return tool.isBroken();
    }

}
