package com.github.wolfiewaffle.hardcore_torches.block;

import com.github.wolfiewaffle.hardcore_torches.MainMod;
import com.github.wolfiewaffle.hardcore_torches.config.Config;
import com.github.wolfiewaffle.hardcore_torches.util.ETorchState;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.particles.ParticleTypes;

public class SmolderingFloorTorchBlock extends HardcoreFloorTorchBlock {
    public static final MapCodec<SmolderingFloorTorchBlock> CODEC = simpleCodec(SmolderingFloorTorchBlock::new);

    public SmolderingFloorTorchBlock(Properties prop) {
        super(prop, ParticleTypes.SMALL_FLAME, ETorchState.SMOLDERING, MainMod.basicTorches, () -> Config.defaultTorchFuel.get());
    }

    public MapCodec<SmolderingFloorTorchBlock> codec() {
        return CODEC;
    }
}
