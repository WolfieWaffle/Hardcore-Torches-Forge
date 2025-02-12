package com.github.wolfiewaffle.hardcore_torches.block;

import com.github.wolfiewaffle.hardcore_torches.util.ETorchState;
import com.github.wolfiewaffle.hardcore_torches.util.TorchGroup;
import net.minecraft.core.particles.SimpleParticleType;

import java.util.function.IntSupplier;

public class HardcoreSoulWallTorch extends HardcoreWallTorchBlock {

    public HardcoreSoulWallTorch(Properties prop, SimpleParticleType fireParticle, SimpleParticleType smokeParticle, ETorchState burnState, TorchGroup group, IntSupplier maxFuel) {
        super(prop, fireParticle, smokeParticle, burnState, group, maxFuel);
    }
}
