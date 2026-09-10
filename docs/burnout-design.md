# Event-driven burnout for Minecraft 1.21.1 / NeoForge 21.1.219

## Status and scope

Draft optimization patch against WolfieWaffle/Hardcore-Torches-Forge,
branch `1.21.0`, commit `febae6993e4b1b73f526a1308c53cc6e0acc7e0e`.
The fork targets branch `1.21.1`; the upstream branch name is historical only.
Minecraft is restricted to `[1.21.1]`, NeoForge to `[21.1.219,21.2)`, and the
artifact version is `1.21.1-2.5-perf.1`. Existing NeoForge and Parchment
dependency pins are retained.

This patch optimizes **placed Hardcore Torches torches, lanterns and campfires**.
It is not a general replacement of every mod's light sources. Handheld/inventory
item ticking, Farmer's Delight stoves, electric lamps, shaders and dynamic
flashlight rendering are outside its scope. Block IDs, recipes, item components,
textures and existing normal/soul fuel-limit behavior are not redesigned.

The standalone Java tests and a stand-in adapter harness run locally. The actual
NeoForge build, Minecraft runtime and compatibility tests still need to run.
A GitHub Actions workflow is included for the real build. Do not describe this
patch as release-ready, fully compatible, benchmarked or proven fastest.

## Why this design

Upstream's lit torches decrement fuel and call `setChanged()` every tick.
Lanterns have a ticker even while unlit. Both campfire server-tick paths search
for dropped items, rebuild shapes and collect entity-query results. Keeping a
countdown ticking merely to discover a future deadline is unnecessary.

The new `FuelClock` stores a remaining-fuel baseline plus an absolute game-time
expiry. The getter calculates remaining fuel; it does not mutate the block or
mark the chunk dirty. Actual refueling and lit/unlit transitions still call
`setChanged()`. NBT contains both `Fuel` and `BurnoutDeadline`. Saving only the
current remaining fuel would be incorrect: an otherwise clean chunk might not
be saved again while the server's game time continues to advance.

`ScheduledFuel` integrates that clock with block-entity loading, removal,
same-block state changes and NBT. `BurnoutScheduler` holds an indexed min-heap
per ServerLevel. Each burner has at most one pending event. Refuel, extinguish
and removal update or cancel that entry immediately rather than leaving stale
generation-stamped entries in a plain PriorityQueue.

When no event is due, the manager does one queue-head check per ticking server
level, with no per-burner scan. Insert/reschedule/cancel/pop are O(log N), reads
are O(1), and space is O(N) for scheduled burners. This is not a byte-size or
MSPT benchmark. Empty queues remain only until their level unloads.

Vanilla scheduled block ticks are a reasonable simpler option for coarse
non-refuelable torches. The indexed queue was selected here to support earlier
and later refuel deadlines, immediate cancellation, independent rain events,
and an explicit expiry-callback budget without a second per-block poller.

## Torch and lantern behavior

Lit torches and both lantern states return no block-entity ticker, on either
side. Unlit and burnt torches remain idle. Lit sources still have block entities
for fuel, saves and existing interactions; this is not a zero-BE implementation.

When rain handling is enabled, a lit torch samples the next geometric waiting
time with probability 1/200 per tick, then checks rain exposure at that
opportunity. The next event is the earlier of fuel expiry and this opportunity.
For uninterrupted loaded ticking, without budget deferrals, this models the
same independent per-tick success probability as the original code, but not the
same PRNG sequence. A dry torch therefore still has occasional checks, including
underground torches; it is not entirely idle with rain handling enabled.

Smoldering torches deliberately retain the original random 1/3-per-tick fuel
consumption. Their server-side ticker is the explicit exception. Dirty marking
now happens only when fuel actually changes. Smoldering does **not** accrue
unloaded time, unlike continuously burning sources. A deterministic three-times
lifetime would be cheaper, but would change the original stochastic mechanic.

For a permanently enclosed underground pack, disabling `torch.torchesRain` and,
when that gameplay choice is acceptable, `torch.torchesSmolder` removes these
weather-related costs. Restart the server after changing the rain setting:
this patch does not enumerate loaded torches to apply a live false-to-true
config change. Relight or reload also reinitializes an individual torch.

## Campfire behavior

Fuel expiry is independent of `cookTick`/`cooldownTick`. Those wrappers continue
to invoke vanilla cooking/cooldown, and the client wrapper retains particles.
Vanilla campfire tickers are **not** eliminated by this patch.

Periodic fuel-area searches, shape unions, streams and entity-result list
construction are removed. A dropped item fuels the campfire through the
campfire block's `entityInside` hook. No nearby entity triggers are registered
on each block, and no global item-entity scan is introduced.

A successful contact consumes one item from a copy of its stack, synchronizes
that stack via `setItem`, or discards the entity when the last item is consumed.
Full campfires, dead/empty/non-fuel items, client calls and invalid fuel factors
do not consume fuel items. Additions use overflow-safe clamping. Existing
wasted-overfill behavior and sound feedback are retained.

This deliberately changes the interaction volume: fuel must intersect the
campfire's block cell, instead of being pulled from the old surrounding/above
query volume. A falling stack may supply several items on successive contacts,
until the campfire reaches capacity. No right-click refueling is added. Vanilla
behavior is delegated to `super.entityInside` when the fuel hook does not accept
the item. Test the actual collision behavior in Minecraft; a stand-in harness
cannot prove that the engine dispatches the hook in every relevant situation.

## Persistence, scheduling and load spikes

Continuously burning sources use **game time, not wall-clock time**. Their fuel
continues to age while a chunk is unloaded and its world keeps advancing. They
do not consume real hours while the server is stopped. This is an intentional
change from upstream's loaded-tick countdown, not a behavior-preserving hotfix.

On unloading/removal, the queued reference is cancelled, without erasing the
persisted deadline. On loading, the entry is recreated. Expired sources are
processed on the next eligible scheduler tick; the block can remain visually
lit until that callback runs. Legacy NBT without the deadline starts a new
clock from its saved `Fuel` on first load. An inactive block ignores an obsolete
deadline. Back up the world before migration; downgrading loses the new deadline
semantics because upstream only understands `Fuel`.

Same-block `LIT` changes, including a campfire becoming unlit through other
vanilla interactions, refresh the timer via `setBlockState`. Replacing a torch
or lantern block uses the original fuel-transfer code. The scheduler checks
that the exact block-entity instance still owns its position.

A due-event lookup uses `getChunkNow` and an existing block-entity map entry;
it never requests chunk creation/loading. Level unload and server stop clear
all retained queues. `/tick freeze` is guarded by `runsNormally()`; actual
engine stepping behavior remains on the manual test matrix.

The scheduler limits **callbacks** to 1024 per level per tick. The limit is not
a hard time budget. Large simultaneous expiries can defer block/lighting
transitions; stored fuel can already be zero before the transition is applied.
Lighting-engine work, sounds and block updates remain real costs. The patch
must not be advertised as guaranteeing a particular MSPT or FPS.

## Compatibility boundaries

Keeping fuel independent of cooking removes this mod's need to run fuel logic
inside every vanilla campfire cooking/cooldown tick. It is a plausible basis
for cooperation with sleeping optimizations, not proof of Lithium/Canary
compatibility. Keep upstream optimizer warnings unchanged until actual testing
is complete. Do not silently instruct users to remove their existing exclusions.

The protected raw `fuel` field in FuelBlockEntity and the unused campfire
`getItemsAtAndAbove` helper are replaced/removed. Addons that directly access
those internals need review. Normal public fuel getters/setters remain, but no
binary compatibility promise is made for third-party subclasses. The Farmer's
Delight stove has its own fuel implementation and is not optimized here.

The source's existing normal/soul max-fuel lookup inconsistency, container-item
fuel semantics and other unrelated gameplay issues are not addressed by this
performance patch.
