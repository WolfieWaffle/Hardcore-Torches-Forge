package com.github.wolfiewaffle.hardcore_torches.config;

import com.mojang.serialization.MapCodec;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.conditions.ICondition;

public class ConfigOilcanFatCondition implements ICondition
{
    public static final ResourceLocation NAME = ResourceLocation.parse("hardcore_torches:config_can_fat");

    @Override
    public boolean test(IContext context)
    {
        return (Config.oilRecipeType.get() == 0 || Config.oilRecipeType.get() == 2);
    }

    @Override
    public String toString()
    {
        return NAME.toString();
    }

    public static final MapCodec<ConfigOilcanFatCondition> CODEC = MapCodec.unit(ConfigOilcanFatCondition::new);

    @Override
    public MapCodec<? extends ICondition> codec() {
        return CODEC;
    }
}
