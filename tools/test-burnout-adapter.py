#!/usr/bin/env python3
"""Execute actual adapter classes against minimal stand-in types, NOT Minecraft.
This verifies our lifecycle/control flow; it does not establish NeoForge ABI,
world-loading order, collision behavior or mod compatibility. Run Gradle and the
manual game matrix as separate required checks.
"""
from pathlib import Path
import subprocess
import tempfile

ROOT = Path(__file__).resolve().parents[1]
P = 'com.github.wolfiewaffle.hardcore_torches'
SOURCES = {
'net.minecraft.core.BlockPos': 'public record BlockPos(int x,int y,int z) { public int getX(){return x;} public int getZ(){return z;} }',
'net.minecraft.core.HolderLookup': 'public interface HolderLookup { interface Provider {} }',
'net.minecraft.nbt.Tag': 'public interface Tag { int TAG_LONG=4; }',
'net.minecraft.nbt.CompoundTag': '''public class CompoundTag {
 private final java.util.Map<String,Number> values=new java.util.HashMap<>();
 public int getInt(String k){return values.getOrDefault(k,0).intValue();}
 public long getLong(String k){return values.getOrDefault(k,0L).longValue();}
 public void putInt(String k,int v){values.put(k,v);} public void putLong(String k,long v){values.put(k,v);}
 public boolean contains(String k,int type){return values.get(k) instanceof Long;}
 public void remove(String k){values.remove(k);}
}''',
'net.minecraft.world.level.block.Block': 'public class Block {}',
'net.minecraft.world.level.block.CampfireBlock': 'public class CampfireBlock extends Block { public static final Object LIT=new Object(); }',
'net.minecraft.world.level.block.state.BlockState': '''public record BlockState(net.minecraft.world.level.block.Block block,boolean lit) {
 public net.minecraft.world.level.block.Block getBlock(){return block;}
 public boolean hasProperty(Object p){return true;} public Boolean getValue(Object p){return lit;}
 public BlockState setValue(Object p,boolean value){return new BlockState(block,value);}
}''',
'net.minecraft.world.level.Level': '''public class Level {
 public boolean isClientSide; public long time; public boolean rain; public int rainChecks,sounds;
 public final java.util.Map<net.minecraft.core.BlockPos,net.minecraft.world.level.block.entity.BlockEntity> entities=new java.util.HashMap<>();
 public long getGameTime(){return time;}
 public net.minecraft.world.level.block.state.BlockState getBlockState(net.minecraft.core.BlockPos p){return entities.get(p).getBlockState();}
 public boolean isRainingAt(net.minecraft.core.BlockPos p){rainChecks++;return rain;}
 public void setBlockAndUpdate(net.minecraft.core.BlockPos p,net.minecraft.world.level.block.state.BlockState s){entities.get(p).setBlockState(s);}
 public void playSound(Object player,net.minecraft.core.BlockPos p,String sound,net.minecraft.sounds.SoundSource source,float a,float b){sounds++;}
}''',
'net.minecraft.world.level.TickRateManager': 'public class TickRateManager { public boolean running=true; public boolean runsNormally(){return running;} }',
'net.minecraft.world.level.chunk.LevelChunk': '''public class LevelChunk {
 private final net.minecraft.world.level.Level level;
 public LevelChunk(net.minecraft.world.level.Level l){level=l;}
 public java.util.Map<net.minecraft.core.BlockPos,net.minecraft.world.level.block.entity.BlockEntity> getBlockEntities(){return level.entities;}
}''',
'net.minecraft.server.level.ServerChunkCache': '''public class ServerChunkCache {
 public boolean loaded=true; public int lookups; private final net.minecraft.world.level.chunk.LevelChunk chunk;
 public ServerChunkCache(net.minecraft.world.level.Level l){chunk=new net.minecraft.world.level.chunk.LevelChunk(l);}
 public net.minecraft.world.level.chunk.LevelChunk getChunkNow(int x,int z){lookups++;return loaded?chunk:null;}
}''',
'net.minecraft.server.level.ServerLevel': '''public final class ServerLevel extends net.minecraft.world.level.Level {
 private final ServerChunkCache chunks=new ServerChunkCache(this);
 private final net.minecraft.world.level.TickRateManager ticks=new net.minecraft.world.level.TickRateManager();
 public ServerChunkCache getChunkSource(){return chunks;} public net.minecraft.world.level.TickRateManager tickRateManager(){return ticks;}
}''',
'net.minecraft.world.level.block.entity.BlockEntityType': 'public class BlockEntityType<T extends BlockEntity> {}',
'net.minecraft.world.level.block.entity.BlockEntity': '''public class BlockEntity {
 private net.minecraft.world.level.Level level; private final net.minecraft.core.BlockPos pos;
 private net.minecraft.world.level.block.state.BlockState state; private boolean removed; public int dirty;
 public BlockEntity(BlockEntityType<?> t,net.minecraft.core.BlockPos p,net.minecraft.world.level.block.state.BlockState s){pos=p;state=s;}
 public net.minecraft.world.level.Level getLevel(){return level;} public void setLevel(net.minecraft.world.level.Level l){level=l;}
 public net.minecraft.core.BlockPos getBlockPos(){return pos;} public net.minecraft.world.level.block.state.BlockState getBlockState(){return state;}
 public boolean isRemoved(){return removed;} public void setRemoved(){removed=true;} public void clearRemoved(){removed=false;}
 public void setChanged(){dirty++;} public void onLoad(){} public void onChunkUnloaded(){}
 public void setBlockState(net.minecraft.world.level.block.state.BlockState s){state=s;}
 public void loadAdditional(net.minecraft.nbt.CompoundTag n,net.minecraft.core.HolderLookup.Provider p){}
 public void saveAdditional(net.minecraft.nbt.CompoundTag n,net.minecraft.core.HolderLookup.Provider p){}
 public net.minecraft.nbt.CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider p){return new net.minecraft.nbt.CompoundTag();}
 public net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket getUpdatePacket(){return null;}
 public BlockEntityType<?> getType(){return new BlockEntityType<>();}
}''',
'net.minecraft.world.level.block.entity.CampfireBlockEntity': '''public class CampfireBlockEntity extends BlockEntity {
 public static int cooked,cooled,particles;
 public CampfireBlockEntity(net.minecraft.core.BlockPos p,net.minecraft.world.level.block.state.BlockState s){super(new BlockEntityType<>(),p,s);}
 public static void cookTick(net.minecraft.world.level.Level w,net.minecraft.core.BlockPos p,net.minecraft.world.level.block.state.BlockState s,CampfireBlockEntity e){cooked++;}
 public static void cooldownTick(net.minecraft.world.level.Level w,net.minecraft.core.BlockPos p,net.minecraft.world.level.block.state.BlockState s,CampfireBlockEntity e){cooled++;}
 public static void particleTick(net.minecraft.world.level.Level w,net.minecraft.core.BlockPos p,net.minecraft.world.level.block.state.BlockState s,CampfireBlockEntity e){particles++;}
 @Override public net.minecraft.nbt.CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider p){var n=super.getUpdateTag(p);n.putInt("CookingMarker",7);return n;}
}''',
'net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket': 'public class ClientboundBlockEntityDataPacket { public static ClientboundBlockEntityDataPacket create(net.minecraft.world.level.block.entity.BlockEntity b){return new ClientboundBlockEntityDataPacket();} }',
'net.minecraft.sounds.SoundEvents': 'public class SoundEvents { public static final String BONE_MEAL_USE="fuel",FIRE_EXTINGUISH="out"; }',
'net.minecraft.sounds.SoundSource': 'public enum SoundSource { BLOCKS }',
'net.minecraft.world.item.crafting.RecipeType': 'public class RecipeType { public static final RecipeType SMELTING=new RecipeType(); }',
'net.minecraft.world.item.ItemStack': '''public class ItemStack {
 private int count; private final int burn;
 public ItemStack(int c,int b){count=c;burn=b;} public int getCount(){return count;}
 public boolean isEmpty(){return count<=0;} public int getBurnTime(net.minecraft.world.item.crafting.RecipeType t){return burn;}
 public ItemStack copy(){return new ItemStack(count,burn);} public void shrink(int n){count-=n;}
}''',
'net.minecraft.world.entity.item.ItemEntity': '''public class ItemEntity {
 private net.minecraft.world.item.ItemStack stack; private boolean alive=true;
 public ItemEntity(int c,int b){stack=new net.minecraft.world.item.ItemStack(c,b);}
 public boolean isAlive(){return alive;} public void discard(){alive=false;}
 public net.minecraft.world.item.ItemStack getItem(){return stack;} public void setItem(net.minecraft.world.item.ItemStack s){stack=s;}
}''',
'javax.annotation.Nullable': 'public @interface Nullable {}',
'net.neoforged.bus.api.SubscribeEvent': 'public @interface SubscribeEvent {}',
'net.neoforged.fml.common.EventBusSubscriber': 'public @interface EventBusSubscriber { String modid(); Bus bus(); enum Bus {GAME} }',
'net.neoforged.neoforge.event.tick.LevelTickEvent': 'public class LevelTickEvent { public record Post(net.minecraft.world.level.Level level){public net.minecraft.world.level.Level getLevel(){return level;}} }',
'net.neoforged.neoforge.event.level.LevelEvent': 'public class LevelEvent { public record Unload(net.minecraft.world.level.Level level){public net.minecraft.world.level.Level getLevel(){return level;}} }',
'net.neoforged.neoforge.event.server.ServerStoppedEvent': 'public class ServerStoppedEvent {}',
P+'.HardcoreTorches': 'public class HardcoreTorches { public static final String MOD_ID="hardcore_torches"; }',
P+'.blockentity.IFuelBlockEntity': 'public interface IFuelBlockEntity { int getFuel();int getMaxFuel();void setFuel(int v);boolean canAddFuel(); }',
P+'.blockentity.IFuelBlock': 'public interface IFuelBlock { void outOfFuel(net.minecraft.world.level.Level w,net.minecraft.core.BlockPos p,net.minecraft.world.level.block.state.BlockState s); }',
P+'.util.ETorchState': 'public enum ETorchState { LIT,UNLIT,SMOLDERING,BURNT }',
P+'.config.Config': '''public class Config {
 public static class Value<T>{private T v; public Value(T v){this.v=v;} public T get(){return v;} public void set(T n){v=n;}}
 public static final Value<Integer> defaultTorchFuel=new Value<>(48000),defaultLanternFuel=new Value<>(144000),startingLanternFuel=new Value<>(0),campfireMaxFuel=new Value<>(24000);
 public static final Value<Double> campfireFuelFactor=new Value<>(8.0);
 public static final Value<Boolean> torchesRain=new Value<>(false),torchesSmolder=new Value<>(true);
}''',
P+'.init.BlockEntityInit': '''public class BlockEntityInit {
 public static final java.util.function.Supplier<net.minecraft.world.level.block.entity.BlockEntityType<@P@.blockentity.TorchBlockEntity>> TORCH_BLOCK_ENTITY=()->new net.minecraft.world.level.block.entity.BlockEntityType<>();
 public static final java.util.function.Supplier<net.minecraft.world.level.block.entity.BlockEntityType<@P@.blockentity.LanternBlockEntity>> LANTERN_BLOCK_ENTITY=()->new net.minecraft.world.level.block.entity.BlockEntityType<>();
 public static final java.util.function.Supplier<net.minecraft.world.level.block.entity.BlockEntityType<@P@.blockentity.HardcoreCampfireBlockEntity>> CAMPFIRE_BLOCK_ENTITY=()->new net.minecraft.world.level.block.entity.BlockEntityType<>();
}''',
P+'.block.AbstractLanternBlock': '''public class AbstractLanternBlock extends net.minecraft.world.level.block.Block implements @P@.blockentity.IFuelBlock {
 public final boolean isLit; public AbstractLanternBlock(boolean l){isLit=l;}
 public void outOfFuel(net.minecraft.world.level.Level w,net.minecraft.core.BlockPos p,net.minecraft.world.level.block.state.BlockState s){w.setBlockAndUpdate(p,new net.minecraft.world.level.block.state.BlockState(new AbstractLanternBlock(false),false));}
}''',
P+'.block.AbstractHardcoreTorchBlock': '''public class AbstractHardcoreTorchBlock extends net.minecraft.world.level.block.Block implements @P@.blockentity.IFuelBlock {
 public final @P@.util.ETorchState burnState; public final java.util.function.IntSupplier maxFuel=()->48000;
 public AbstractHardcoreTorchBlock(@P@.util.ETorchState s){burnState=s;}
 private void change(net.minecraft.world.level.Level w,net.minecraft.core.BlockPos p,@P@.util.ETorchState s){w.setBlockAndUpdate(p,new net.minecraft.world.level.block.state.BlockState(new AbstractHardcoreTorchBlock(s),s==@P@.util.ETorchState.LIT));}
 public void smother(net.minecraft.world.level.Level w,net.minecraft.core.BlockPos p,net.minecraft.world.level.block.state.BlockState s){change(w,p,@P@.util.ETorchState.SMOLDERING);}
 public void extinguish(net.minecraft.world.level.Level w,net.minecraft.core.BlockPos p,net.minecraft.world.level.block.state.BlockState s,boolean sound){change(w,p,@P@.util.ETorchState.UNLIT);}
 public void burnOut(net.minecraft.world.level.Level w,net.minecraft.core.BlockPos p,net.minecraft.world.level.block.state.BlockState s,boolean sound){change(w,p,@P@.util.ETorchState.BURNT);}
 public void outOfFuel(net.minecraft.world.level.Level w,net.minecraft.core.BlockPos p,net.minecraft.world.level.block.state.BlockState s){burnOut(w,p,s,false);}
}''',
P+'.block.HardcoreCampfire': '''public class HardcoreCampfire extends net.minecraft.world.level.block.CampfireBlock implements @P@.blockentity.IFuelBlock {
 public void outOfFuel(net.minecraft.world.level.Level w,net.minecraft.core.BlockPos p,net.minecraft.world.level.block.state.BlockState s){w.setBlockAndUpdate(p,s.setValue(LIT,false));}
}''',
}

def main():
    with tempfile.TemporaryDirectory(prefix='burnout-adapter-') as tmp:
        tmp = Path(tmp)
        files=[]
        for name, body in SOURCES.items():
            path=tmp/'stubs'/Path(name.replace('.', '/')+'.java')
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_text('package '+name.rsplit('.',1)[0]+';\n'+body.replace('@P@',P)+'\n')
            files.append(str(path))
        base=ROOT/'src/main/java'/P.replace('.','/')
        files += [str(p) for p in (base/'burnout').glob('*.java')]
        files += [str(base/'blockentity'/f) for f in ['FuelBlockEntity.java','TorchBlockEntity.java','LanternBlockEntity.java','HardcoreCampfireBlockEntity.java']]
        files.append(str(ROOT/'tools/adapter-harness/BurnoutAdapterTest.java'))
        subprocess.run(['javac','--release','21','-Xlint:all','-Werror','-d',str(tmp/'classes'),*files],check=True)
        subprocess.run(['java','-ea','-cp',str(tmp/'classes'),P+'.burnout.BurnoutAdapterTest'],check=True)
if __name__=='__main__':
    main()
