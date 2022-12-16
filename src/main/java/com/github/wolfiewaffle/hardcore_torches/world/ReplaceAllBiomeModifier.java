package com.github.wolfiewaffle.hardcore_torches.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ModifiableBiomeInfo;

public class ReplaceAllBiomeModifier implements BiomeModifier
{
    public Holder<PlacedFeature> feature;

    public static Codec<ReplaceAllBiomeModifier> CODEC = PlacedFeature.CODEC.fieldOf("feature").xmap(ReplaceAllBiomeModifier::new, (o) -> o.feature).codec();

    private ReplaceAllBiomeModifier(Holder<PlacedFeature> feature) {
        this.feature = feature;
    }

    public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder)
    {
        if (phase == Phase.AFTER_EVERYTHING) {
            builder.getGenerationSettings().addFeature(GenerationStep.Decoration.TOP_LAYER_MODIFICATION, feature);
        }
    }

    public Codec<? extends BiomeModifier> codec()
    {
        return CODEC;
    }
}