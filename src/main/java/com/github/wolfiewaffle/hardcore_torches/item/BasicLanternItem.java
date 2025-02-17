package com.github.wolfiewaffle.hardcore_torches.item;

import com.github.wolfiewaffle.hardcore_torches.config.Config;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BasicLanternItem extends LanternItem {

    public BasicLanternItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> list, TooltipFlag flag) {
        if (Config.lanternsNeedCan.get()) list.add(Component.literal("Requires an Oil Can").withStyle(ChatFormatting.GRAY));
        list.add(Component.literal("Light with Flint and Steel").withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, world, list, flag);
    }
}
