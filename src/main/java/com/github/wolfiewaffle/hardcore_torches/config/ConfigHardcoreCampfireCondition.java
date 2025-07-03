package com.github.wolfiewaffle.hardcore_torches.config;

import com.mojang.serialization.MapCodec;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.conditions.ICondition;

public class ConfigHardcoreCampfireCondition implements ICondition
{
    public static final ResourceLocation NAME = ResourceLocation.parse("hardcore_torches:config_hardcore_campfire");

    @Override
    public boolean test(IContext context)
    {
        return Config.craftHardcoreCampfire.get();
    }

    @Override
    public String toString()
    {
        return NAME.toString();
    }

    public static final MapCodec<ConfigHardcoreCampfireCondition> CODEC = MapCodec.unit(ConfigHardcoreCampfireCondition::new);

    @Override
    public MapCodec<? extends ICondition> codec() {
        return CODEC;
    }
}
