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

public class SoulLanternItem extends LanternItem {

    public SoulLanternItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> list, TooltipFlag flag) {
        list.add(Component.literal("Place this on the ground, then").withStyle(ChatFormatting.GRAY));
        list.add(Component.literal("hold a candle and right click it").withStyle(ChatFormatting.GRAY));
        list.add(Component.literal("to transfer your XP as fuel").withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, world, list, flag);
    }
}
