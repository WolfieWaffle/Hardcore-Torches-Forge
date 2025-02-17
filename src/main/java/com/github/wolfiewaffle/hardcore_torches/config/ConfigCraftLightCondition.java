package com.github.wolfiewaffle.hardcore_torches.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.conditions.ICondition;

public class ConfigCraftLightCondition implements ICondition
{
    public static final ResourceLocation NAME = new ResourceLocation("hardcore_torches", "config_craft_light");

    @Override
    public boolean test(IContext context)
    {
        return Config.craftLight.get();
    }

    @Override
    public String toString()
    {
        return NAME.toString();
    }

    public static final Codec<ConfigCraftLightCondition> CODEC = MapCodec.unit(ConfigCraftLightCondition::new).codec();

    @Override
    public Codec<? extends ICondition> codec() {
        return CODEC;
    }
}
