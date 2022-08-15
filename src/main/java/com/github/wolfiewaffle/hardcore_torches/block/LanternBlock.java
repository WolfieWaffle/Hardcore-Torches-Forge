package com.github.wolfiewaffle.hardcore_torches.block;

import com.github.wolfiewaffle.hardcore_torches.MainMod;
import net.minecraft.tags.TagKey;

import java.util.function.IntSupplier;

public class LanternBlock extends AbstractLanternBlock {

    public LanternBlock(Properties prop, boolean isLit, IntSupplier maxFuel) {
        super(prop, isLit, maxFuel);
        this.registerDefaultState(this.stateDefinition.any().setValue(HANGING, Boolean.valueOf(false)).setValue(WATERLOGGED, Boolean.valueOf(false)));
    }

    @Override
    public TagKey getFreeLightItems() {
        return MainMod.FREE_LANTERN_LIGHT_ITEMS;
    }

    @Override
    public TagKey getDamageLightItems() {
        return MainMod.DAMAGE_LANTERN_LIGHT_ITEMS;
    }

    @Override
    public TagKey getConsumeLightItems() {
        return MainMod.CONSUME_LANTERN_LIGHT_ITEMS;
    }
}
