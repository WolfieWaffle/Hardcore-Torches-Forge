package com.github.wolfiewaffle.hardcore_torches.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.conditions.ICondition;

public class ConfigOilcanCoalCondition implements ICondition
{
    public static final ResourceLocation NAME = new ResourceLocation("hardcore_torches", "config_can_coal");

    @Override
    public boolean test(IContext context)
    {
        return (Config.oilRecipeType.get() == 1 || Config.oilRecipeType.get() == 2);
    }

    @Override
    public String toString()
    {
        return NAME.toString();
    }

    public static final Codec<ConfigOilcanCoalCondition> CODEC = MapCodec.unit(ConfigOilcanCoalCondition::new).codec();

    @Override
    public Codec<? extends ICondition> codec() {
        return CODEC;
    }
}
