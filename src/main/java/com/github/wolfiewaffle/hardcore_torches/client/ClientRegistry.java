package com.github.wolfiewaffle.hardcore_torches.client;

import com.github.wolfiewaffle.hardcore_torches.MainMod;
import com.github.wolfiewaffle.hardcore_torches.init.BlockInit;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = MainMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientRegistry {
    @SubscribeEvent
    public static void setupClient(FMLClientSetupEvent event) {
        ItemBlockRenderTypes.setRenderLayer(BlockInit.LIT_LANTERN.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(BlockInit.UNLIT_LANTERN.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(BlockInit.LIT_SOUL_LANTERN.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(BlockInit.UNLIT_SOUL_LANTERN.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(BlockInit.LIT_TORCH.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(BlockInit.LIT_WALL_TORCH.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(BlockInit.UNLIT_TORCH.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(BlockInit.UNLIT_WALL_TORCH.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(BlockInit.SMOLDERING_TORCH.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(BlockInit.SMOLDERING_WALL_TORCH.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(BlockInit.BURNT_TORCH.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(BlockInit.BURNT_WALL_TORCH.get(), RenderType.cutout());
    }
}