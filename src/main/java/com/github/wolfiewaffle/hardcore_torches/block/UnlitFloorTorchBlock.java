package com.github.wolfiewaffle.hardcore_torches.block;

import com.github.wolfiewaffle.hardcore_torches.MainMod;
import com.github.wolfiewaffle.hardcore_torches.config.Config;
import com.github.wolfiewaffle.hardcore_torches.util.ETorchState;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.particles.ParticleTypes;

public class UnlitFloorTorchBlock extends HardcoreFloorTorchBlock {
    public static final MapCodec<UnlitFloorTorchBlock> CODEC = simpleCodec(UnlitFloorTorchBlock::new);

    public UnlitFloorTorchBlock(Properties prop) {
        super(prop, null, ETorchState.UNLIT, MainMod.basicTorches, () -> Config.defaultTorchFuel.get());
    }

    public MapCodec<UnlitFloorTorchBlock> codec() {
        return CODEC;
    }
}
