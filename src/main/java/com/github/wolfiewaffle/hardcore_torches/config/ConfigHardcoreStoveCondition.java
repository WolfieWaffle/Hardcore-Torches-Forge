package com.github.wolfiewaffle.hardcore_torches.config;

import com.mojang.serialization.MapCodec;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.conditions.ICondition;

public class ConfigHardcoreStoveCondition implements ICondition
{
    public static final ResourceLocation NAME = ResourceLocation.parse("hardcore_torches:craft_hardcore_stove");

    @Override
    public boolean test(IContext context)
    {
        return Config.craftHardcoreStove.get();
    }

    @Override
    public String toString()
    {
        return NAME.toString();
    }

    public static final MapCodec<ConfigHardcoreStoveCondition> CODEC = MapCodec.unit(ConfigHardcoreStoveCondition::new);

    @Override
    public MapCodec<? extends ICondition> codec() {
        return CODEC;
    }
}
