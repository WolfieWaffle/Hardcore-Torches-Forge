package com.github.wolfiewaffle.hardcore_torches.compat.curio;

import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.type.capability.ICurio;

public class LanternCurioProvider implements ICapabilityProvider {
    LanternCurio curio = new LanternCurio();
    private final LazyOptional<ICurio> curioOpt = LazyOptional.of(() -> curio);

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return CuriosCapability.ITEM.orEmpty(cap, curioOpt);
    }
}
