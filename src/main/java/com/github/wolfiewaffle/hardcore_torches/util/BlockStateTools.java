package com.github.wolfiewaffle.hardcore_torches.util;

import java.util.Iterator;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

public class BlockStateTools {
    public BlockStateTools() {
    }

    public static BlockState changeBlock(BlockState oldState, Block newBlock) {
        BlockState newState = newBlock.defaultBlockState();

        Property property;
        for(Iterator var3 = oldState.getProperties().iterator(); var3.hasNext(); newState = copyProperty(oldState, newState, property)) {
            property = (Property)var3.next();
        }

        return newState;
    }

    public static <T extends Comparable<T>> BlockState copyProperty(BlockState from, BlockState to, Property<T> property) {
        return to.setValue(property, from.getValue(property));
    }
}
