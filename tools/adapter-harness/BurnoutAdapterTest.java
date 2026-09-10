package com.github.wolfiewaffle.hardcore_torches.burnout;

import com.github.wolfiewaffle.hardcore_torches.block.*;
import com.github.wolfiewaffle.hardcore_torches.blockentity.*;
import com.github.wolfiewaffle.hardcore_torches.config.Config;
import com.github.wolfiewaffle.hardcore_torches.util.ETorchState;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

/** Actual adapter code, minimal stand-in engine types: NOT a Minecraft/GameTest run. */
public final class BurnoutAdapterTest {
    private static int tests;
    private static void check(boolean value,String reason){if(!value)throw new AssertionError(reason);}
    private static void test(String name,Runnable action){
        BurnoutScheduler.onServerStopped(new ServerStoppedEvent());
        Config.torchesRain.set(false); Config.campfireMaxFuel.set(24000); Config.campfireFuelFactor.set(8.0);
        action.run(); tests++; System.out.println("PASS "+name);
    }
    private static BlockState lanternState(boolean lit){return new BlockState(new AbstractLanternBlock(lit),lit);}
    private static BlockState torchState(ETorchState s){return new BlockState(new AbstractHardcoreTorchBlock(s),s==ETorchState.LIT);}
    private static void attach(ServerLevel world,BlockEntity entity){entity.setLevel(world);world.entities.put(entity.getBlockPos(),entity);entity.onLoad();}
    private static void tick(ServerLevel world,long now){world.time=now;BurnoutScheduler.onLevelTick(new LevelTickEvent.Post(world));}
    private static LanternBlockEntity lantern(ServerLevel w,int x,int fuel,boolean lit){
        var b=new LanternBlockEntity(new BlockPos(x,64,0),lanternState(lit));b.setFuel(fuel);attach(w,b);return b;
    }
    private static HardcoreCampfireBlockEntity campfire(ServerLevel w,int fuel,boolean lit){
        var b=new HardcoreCampfireBlockEntity(new BlockPos(0,64,0),new BlockState(new HardcoreCampfire(),lit));b.setFuel(fuel);attach(w,b);return b;
    }
    private static void seedTorchRandom(long seed){
        try {
            var field=FuelBlockEntity.class.getDeclaredField("random");field.setAccessible(true);
            ((java.util.Random)field.get(null)).setSeed(seed);
        } catch (ReflectiveOperationException ex) {throw new AssertionError(ex);}
    }
    public static void main(String[] args){
        test("unlit lantern has no periodic work or fuel loss",()->{
            var w=new ServerLevel();var b=lantern(w,0,100,false);int dirty=b.dirty;
            for(int i=1;i<1000;i++)tick(w,i);
            check(b.getFuel()==100 && b.dirty==dirty && w.getChunkSource().lookups==0,"idle work");
        });
        test("lantern expires at deadline without a BE ticker",()->{
            var w=new ServerLevel();var b=lantern(w,0,100,true);int dirty=b.dirty;
            tick(w,99);check(b.getFuel()==1&&b.dirty==dirty,"early/dirty");
            tick(w,100);check(!((AbstractLanternBlock)b.getBlockState().getBlock()).isLit&&b.getFuel()==0,"expiry");
        });
        test("extinguish/relight preserves exact remaining fuel",()->{
            var w=new ServerLevel();var b=lantern(w,0,100,true);w.time=25;b.setBlockState(lanternState(false));
            tick(w,1000);check(b.getFuel()==75,"pause");b.setBlockState(lanternState(true));tick(w,1074);check(b.getFuel()==1,"resume");tick(w,1075);check(b.getFuel()==0,"expiry");
        });
        test("refuel invalidates the original deadline",()->{
            var w=new ServerLevel();var b=lantern(w,0,100,true);w.time=50;b.setFuel(b.getFuel()+100);
            tick(w,100);check(b.getFuel()==100 && ((AbstractLanternBlock)b.getBlockState().getBlock()).isLit,"old event");tick(w,200);check(b.getFuel()==0,"new event");
        });
        test("earlier replacement deadline fires sooner",()->{
            var w=new ServerLevel();var b=lantern(w,0,1000,true);w.time=10;b.setFuel(5);tick(w,15);check(b.getFuel()==0,"earlier deadline");
        });
        test("unload cancels callback but saved deadline ages",()->{
            var w=new ServerLevel();var old=lantern(w,0,100,true);var n=new CompoundTag();old.saveAdditional(n,null);
            old.onChunkUnloaded();old.setRemoved();w.entities.clear();w.getChunkSource().loaded=false;tick(w,150);
            check(w.getChunkSource().lookups==0,"unloaded callback");
            var loaded=new LanternBlockEntity(new BlockPos(0,64,0),lanternState(true));loaded.loadAdditional(n,null);w.getChunkSource().loaded=true;attach(w,loaded);tick(w,151);
            check(loaded.getFuel()==0 && !((AbstractLanternBlock)loaded.getBlockState().getBlock()).isLit,"catch up");
        });
        test("old clean autosave does not refund fuel on restart",()->{
            var oldWorld=new ServerLevel();var old=lantern(oldWorld,0,1000,true);var n=new CompoundTag();old.saveAdditional(n,null);
            BurnoutScheduler.onServerStopped(new ServerStoppedEvent());var w=new ServerLevel();w.time=300;
            var b=new LanternBlockEntity(new BlockPos(0,64,0),lanternState(true));b.loadAdditional(n,null);attach(w,b);
            check(b.getFuel()==700,"stale Fuel refunded time");tick(w,1000);check(b.getFuel()==0,"restart deadline");
        });
        test("legacy Fuel-only NBT migrates without immediate loss",()->{
            var n=new CompoundTag();n.putInt("Fuel",120);var w=new ServerLevel();w.time=5000;
            var b=new LanternBlockEntity(new BlockPos(0,64,0),lanternState(true));b.loadAdditional(n,null);attach(w,b);check(b.getFuel()==120,"migration");tick(w,5120);check(b.getFuel()==0,"legacy expiry");
        });
        test("obsolete event cannot affect replacement block entity",()->{
            var w=new ServerLevel();var old=lantern(w,0,2,true);old.setRemoved();var b=lantern(w,0,100,true);tick(w,2);check(b.getFuel()==98,"replacement");
        });
        test("scheduler refuses absent chunks",()->{
            var w=new ServerLevel();var b=lantern(w,0,1,true);w.getChunkSource().loaded=false;tick(w,1);
            check(((AbstractLanternBlock)b.getBlockState().getBlock()).isLit,"touched absent chunk");
        });
        test("frozen world defers callbacks",()->{
            var w=new ServerLevel();var b=lantern(w,0,1,true);w.tickRateManager().running=false;tick(w,1);
            check(((AbstractLanternBlock)b.getBlockState().getBlock()).isLit,"frozen callback");w.tickRateManager().running=true;tick(w,1);check(b.getFuel()==0,"unfreeze");
        });
        test("world unload and stop release queues",()->{
            var w=new ServerLevel();var b=lantern(w,0,1,true);BurnoutScheduler.onLevelUnload(new LevelEvent.Unload(w));tick(w,1);
            check(w.getChunkSource().lookups==0,"world retained");b.onLoad();BurnoutScheduler.onServerStopped(new ServerStoppedEvent());tick(w,2);check(w.getChunkSource().lookups==0,"server retained");
        });
        test("simultaneous expiry observes per-level callback budget",()->{
            var w=new ServerLevel();for(int i=0;i<2050;i++)lantern(w,i,1,true);tick(w,1);
            long out=w.entities.values().stream().filter(e->!((AbstractLanternBlock)e.getBlockState().getBlock()).isLit).count();
            check(out==1024,"unbounded burst: "+out);tick(w,2);tick(w,3);
            check(w.entities.values().stream().noneMatch(e->((AbstractLanternBlock)e.getBlockState().getBlock()).isLit),"backlog stuck");
        });
        test("campfire contact consumes one item and cannot overfill",()->{
            Config.campfireMaxFuel.set(100);var w=new ServerLevel();var b=campfire(w,0,false);var item=new ItemEntity(16,20);
            check(b.tryAddFuel(item)&&b.getFuel()==100&&item.getItem().getCount()==15,"stack/fuel");
            check(!b.tryAddFuel(item)&&item.getItem().getCount()==15,"full consumes");
        });
        test("campfire rejects dead, empty and nonfuel items",()->{
            var w=new ServerLevel();var b=campfire(w,0,false);var dead=new ItemEntity(1,20);dead.discard();
            check(!b.tryAddFuel(dead)&&!b.tryAddFuel(new ItemEntity(0,20))&&!b.tryAddFuel(new ItemEntity(4,0)),"invalid fuel");
            w.isClientSide=true;check(!b.tryAddFuel(new ItemEntity(4,20)),"client mutation");
        });
        test("campfire fuel expiry is independent of cooking callbacks",()->{
            var w=new ServerLevel();var b=campfire(w,100,true);tick(w,100);check(!b.getBlockState().getValue(CampfireBlock.LIT),"sleeping fuel");
        });
        test("campfire same-block LIT changes pause and resume",()->{
            var w=new ServerLevel();var b=campfire(w,100,true);w.time=20;b.setBlockState(b.getBlockState().setValue(CampfireBlock.LIT,false));
            tick(w,500);check(b.getFuel()==80,"unlit lost fuel");b.setBlockState(b.getBlockState().setValue(CampfireBlock.LIT,true));tick(w,580);check(b.getFuel()==0,"relight deadline");
        });
        test("campfire wrappers preserve vanilla cooking and particles",()->{
            var w=new ServerLevel();var b=campfire(w,100,true);int c=CampfireBlockEntity.cooked,d=CampfireBlockEntity.cooled,p=CampfireBlockEntity.particles;
            HardcoreCampfireBlockEntity.cookTick(w,b.getBlockPos(),b.getBlockState(),b);HardcoreCampfireBlockEntity.cooldownTick(w,b.getBlockPos(),b.getBlockState(),b);HardcoreCampfireBlockEntity.clientTick(w,b.getBlockPos(),b.getBlockState(),b);
            check(CampfireBlockEntity.cooked==c+1&&CampfireBlockEntity.cooled==d+1&&CampfireBlockEntity.particles==p+1&&b.getFuel()==100,"wrapper");
        });
        test("update tags preserve campfire superclass fields",()->{
            var w=new ServerLevel();var b=campfire(w,100,true);var n=b.getUpdateTag(null);check(n.getInt("Fuel")==100&&n.getInt("CookingMarker")==7&&n.getLong("BurnoutDeadline")==100,"sync tag");
        });
        test("lit torch without rain has no periodic per-block work",()->{
            var w=new ServerLevel();var b=new TorchBlockEntity(new BlockPos(0,64,0),torchState(ETorchState.LIT));b.setFuel(1000);attach(w,b);int dirty=b.dirty;
            for(int i=1;i<1000;i++)tick(w,i);check(w.rainChecks==0&&w.getChunkSource().lookups==0&&b.dirty==dirty,"idle torch");tick(w,1000);check(((AbstractHardcoreTorchBlock)b.getBlockState().getBlock()).burnState==ETorchState.BURNT,"torch deadline");
        });
        test("geometric rain opportunity transitions lit torch to smoldering",()->{
            Config.torchesRain.set(true);Config.torchesSmolder.set(true);seedTorchRandom(0);
            int due=FuelMath.geometricDelay(new java.util.Random(0).nextDouble(),1.0/200.0);
            var w=new ServerLevel();w.rain=true;var b=new TorchBlockEntity(new BlockPos(0,64,0),torchState(ETorchState.LIT));b.setFuel(1000);attach(w,b);
            tick(w,due-1);check(w.rainChecks==0,"early rain check");tick(w,due);
            check(((AbstractHardcoreTorchBlock)b.getBlockState().getBlock()).burnState==ETorchState.SMOLDERING&&b.getFuel()==1000-due,"rain transition");
        });
        test("rain can extinguish directly without resetting fuel",()->{
            Config.torchesRain.set(true);Config.torchesSmolder.set(false);seedTorchRandom(0);
            int due=FuelMath.geometricDelay(new java.util.Random(0).nextDouble(),1.0/200.0);
            var w=new ServerLevel();w.rain=true;var b=new TorchBlockEntity(new BlockPos(0,64,0),torchState(ETorchState.LIT));b.setFuel(1000);attach(w,b);tick(w,due);
            check(((AbstractHardcoreTorchBlock)b.getBlockState().getBlock()).burnState==ETorchState.UNLIT&&b.getFuel()==1000-due,"rain extinguish");
            tick(w,10000);check(b.getFuel()==1000-due,"unlit rainfall consumes fuel");
        });
        test("dry torches schedule sparse opportunities, not per-tick checks",()->{
            Config.torchesRain.set(true);seedTorchRandom(0);var w=new ServerLevel();
            var b=new TorchBlockEntity(new BlockPos(0,64,0),torchState(ETorchState.LIT));b.setFuel(10000);attach(w,b);
            for(int i=1;i<=1000;i++)tick(w,i);
            check(w.rainChecks>0&&w.rainChecks<30&&b.getFuel()==9000,"rain polling regression");
        });
        test("smoldering retains the original seeded one-third stochastic burn",()->{
            seedTorchRandom(0);var reference=new java.util.Random(0);var w=new ServerLevel();
            var b=new TorchBlockEntity(new BlockPos(0,64,0),torchState(ETorchState.SMOLDERING));b.setFuel(1000);attach(w,b);int burned=0;
            for(int i=1;i<=300;i++){w.time=i;if(reference.nextInt(3)==0)burned++;b.tick();}
            check(b.getFuel()==1000-burned,"smoldering stochastic behavior changed");
        });
        System.out.println("PASS: "+tests+" adapter test groups (stand-in engine, NOT Minecraft)");
    }
}
