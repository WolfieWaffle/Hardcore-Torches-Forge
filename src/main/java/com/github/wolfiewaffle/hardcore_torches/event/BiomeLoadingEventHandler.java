package com.github.wolfiewaffle.hardcore_torches.event;

import com.github.wolfiewaffle.hardcore_torches.MainMod;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class BiomeLoadingEventHandler {

    @SubscribeEvent
    public void biomeLoad(BiomeLoadingEvent event) {
        System.out.println("BIOME LOAD");
        event.getGeneration().addFeature(GenerationStep.Decoration.TOP_LAYER_MODIFICATION, MainMod.REPLACE_ALL_PLACED_FEATURE.getHolder().get());
        System.out.println("BIOME LOAD " + event.getGeneration().getFeatures(GenerationStep.Decoration.TOP_LAYER_MODIFICATION));
    }
}
