package com.github.wolfiewaffle.hardcore_torches.block;

import com.github.wolfiewaffle.hardcore_torches.MainMod;
import com.github.wolfiewaffle.hardcore_torches.config.Config;
import com.github.wolfiewaffle.hardcore_torches.util.ETorchState;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.particles.ParticleTypes;

public class LitFloorTorchBlock extends HardcoreFloorTorchBlock {
    public static final MapCodec<LitFloorTorchBlock> CODEC = simpleCodec(LitFloorTorchBlock::new);

    public LitFloorTorchBlock(Properties prop) {
        super(prop, ParticleTypes.FLAME, ETorchState.LIT, MainMod.basicTorches, () -> Config.defaultTorchFuel.get());
    }

    public MapCodec<LitFloorTorchBlock> codec() {
        return CODEC;
    }
}
