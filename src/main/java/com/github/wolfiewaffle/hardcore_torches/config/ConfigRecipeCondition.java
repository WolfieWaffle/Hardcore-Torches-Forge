package com.github.wolfiewaffle.hardcore_torches.config;

import com.google.common.base.Supplier;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;

public class ConfigRecipeCondition implements ICondition
{
    private static final ResourceLocation NAME = new ResourceLocation("hardcore_torches", "config");
    private final Supplier<Boolean> bool;

    // Supplier is a functional interface that returns a boolean
    public ConfigRecipeCondition(Supplier<Boolean> bool) {
        this.bool = bool;
    }

    @Override
    public ResourceLocation getID()
    {
        return NAME;
    }

    @Override
    public boolean test(IContext context)
    {
        return bool.get();
    }

    @Override
    public String toString()
    {
        return "hardcore_torches:config";
    }

    public static class Serializer implements IConditionSerializer<ConfigRecipeCondition>
    {
        Supplier<Boolean> bool;
        ResourceLocation id;

        public Serializer(Supplier<Boolean> bool, ResourceLocation id) {
            this.bool = bool;
            this.id = id;
        }

        @Override
        public void write(JsonObject json, ConfigRecipeCondition value)
        {

        }

        @Override
        public ConfigRecipeCondition read(JsonObject json)
        {
            return new ConfigRecipeCondition(bool);
        }

        @Override
        public ResourceLocation getID()
        {
            return id;
        }
    }
}
