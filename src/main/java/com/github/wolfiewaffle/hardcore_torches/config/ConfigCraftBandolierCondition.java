package com.github.wolfiewaffle.hardcore_torches.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.conditions.ICondition;

public class ConfigCraftBandolierCondition implements ICondition
{
    public static final ResourceLocation NAME = ResourceLocation.parse("hardcore_torches:config_craft_bandolier");

    @Override
    public boolean test(IContext context)
    {
        return Config.craftBandolier.get();
    }

    @Override
    public String toString()
    {
        return NAME.toString();
    }

    public static final MapCodec<ConfigCraftBandolierCondition> CODEC = MapCodec.unit(ConfigCraftBandolierCondition::new);

    @Override
    public MapCodec<? extends ICondition> codec() {
        return CODEC;
    }
}
