package com.github.wolfiewaffle.hardcore_torches.config;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import net.minecraftforge.common.crafting.conditions.ICondition;

public class ConfigRecipeCondition implements ICondition
{
    public ConfigRecipeCondition() {
        System.out.println("CONSTRUCTED");
    }

    @Override
    public boolean test(IContext context)
    {
        System.out.println("REACHED");
        return Config.craftUnlit.get();
    }

    public static final Supplier<ConfigRecipeCondition> INSTANCE = Suppliers.memoize(ConfigRecipeCondition::new);
    public static final Codec<ConfigRecipeCondition> CODEC = Codec.unit(INSTANCE);

    @Override
    public Codec<? extends ICondition> codec() {
        return CODEC;
    }
}
