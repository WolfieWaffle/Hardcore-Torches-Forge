package com.github.wolfiewaffle.hardcore_torches.config;

import com.mojang.serialization.MapCodec;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.conditions.ICondition;

public class ConfigLanternsUseFuelCondition implements ICondition
{
    public static final ResourceLocation NAME = ResourceLocation.parse("hardcore_torches:lanterns_use_fuel");

    @Override
    public boolean test(IContext context)
    {
        return Config.lanternsUseFuel.get();
    }

    @Override
    public String toString()
    {
        return NAME.toString();
    }

    public static final MapCodec<ConfigLanternsUseFuelCondition> CODEC = MapCodec.unit(ConfigLanternsUseFuelCondition::new);

    @Override
    public MapCodec<? extends ICondition> codec() {
        return CODEC;
    }
}
