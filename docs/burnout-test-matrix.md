# Required verification before release

## Already executable without game dependencies

```
bash tools/test-burnout-core.sh
python3 tools/test-burnout-adapter.py
python3 tools/check-burnout-contracts.py
```

Core: 26 test groups covering deadline arithmetic, paused/active state, saves,
legacy migration, indexed cancellation/reordering, 100,000 same-key reschedules,
100,000 randomized operations against a reference model and rain sampling.

Adapter harness: 24 groups execute the actual timer manager and modified block
entity classes against deliberately minimal stand-in engine types. They cover
lifecycle, persisted deadlines, unlit inactivity, exact/later/earlier expiry,
replacement ownership, absent chunks, world/server cleanup, frozen callbacks,
a 2050-event burst, contact-fueling stack consumption, cooking delegation,
packet fields and seeded rain/smoldering transitions.

Static checks: 6 assertions on integration contracts. They detect accidentally
restored tickers/scans and the metadata mismatch. They are not behavior tests.

The adapter harness intentionally does not compile the actual block subclasses
or real engine dependencies. It cannot validate registry initialization, full
source compatibility, event delivery order, collision dispatch, network packets,
light propagation, saves on disk or another mod's mixins.

## Real build: NOT executed in the preparation environment

Use the complete upstream repository with the patch, JDK 21 and network access:

```
bash gradlew --no-daemon clean build
```

The new GitHub Actions workflow runs this after the standalone checks. A successful
standalone run is not a substitute. Preserve logs and jar checksums from the real
build. The locally prepared patch is not a compiled mod jar.

## In-game matrix: NOT executed

| Area | Cases and acceptance criteria |
|---|---|
| Loading | Dedicated server and client both start on Minecraft 1.21.1 / NeoForge 21.1.219; registries retain existing IDs. |
| Torch variants | Floor/wall and normal/soul: burn, unlight, relight, rain, smolder, break/place, oil refill and soul attunement. Fuel survives transitions and limits remain as in upstream. |
| Lanterns | Standing/hanging, normal/soul, waterlogged: fuel expires, unlit pauses, pickup/drop/place preserves fuel, refueling before/after a pending deadline works. |
| Campfire contact | Drop one coal, a stack, nonfuel, and several ItemEntities on lit/unlit/full campfires; test center and edges, resting and falling items. One fuel item is consumed per accepted contact, none when full. |
| Campfire state | Flint and steel, projectile ignition, shovel, rain behavior, waterlogging and mod interactions; same-block LIT changes must freeze/resume the deadline. |
| Cooking | Cook a full set of items; put out and relight mid-cook; verify cooling/progress and client particles. Fuel expiry may occur with no food present. |
| Saves | Save with burning, smoldering and unlit sources; continue without changing the chunk; restart; verify no fuel refund from the older clean autosave. |
| Migration | Load a copy of a pre-patch world with Fuel-only NBT; remaining fuel is preserved on first migration. Back up before testing downgrade. |
| Unload | Leave chunks long enough to unload; revisit before and after expiry; repeat while replacing sources. No forced chunk loads and no retained old BE references. |
| Freeze/step | `/tick freeze`, step, unfreeze and server pause; no unrequested timer callbacks while frozen. Game-time semantics hold under actual engine behavior. |
| Sleeping mods | Repeat campfire fueling/expiry/cooking with the pack's exact Lithium version and settings. Test both with upstream exclusions and with the intended sleeping setting. Do not remove warnings/exclusions based on the stand-in tests. |
| Other compatibility | Farmer's Delight, Curios, Amendments and any fuel-inspecting HUD in the actual pack. Stove behavior is unchanged and may still have its original ticking costs. |

## Performance experiment: results intentionally blank until measured

Compare unmodified base and patch using the same world copy, JVM flags, Java
version, heap size, view/simulation distance, render settings and mod versions.
Use a fixed world seed and fully generated/loaded test chunks. Warm both runs
before sampling; repeat each case, alternate the A/B order and keep raw profiles.

Test 100, 1000 and 10000 sources, separating: lit torches with rain disabled;
lit torches with rain enabled in dry/rainy weather; smoldering torches; unlit and
lit lanterns; idle campfires; cooking campfires; contact-refueling campfires.
Include synchronous large expiry bursts and repeated unload/reload/refueling.

Record median/p95/p99 MSPT, server CPU, allocations and GC, queue size and chunk
loading behavior. Profile both this mod and the lighting engine. A few seconds
of FPS observation are not evidence of a server-side improvement. Measure client
FPS separately when testing shaders or dynamic flashlights.

For a useful sanity check, 1000 upstream lanterns cause 20,000 BE ticker calls
per second at 20 TPS. The patch removes those per-lantern callbacks, but this is
an operation-count calculation, NOT a prediction of an FPS or MSPT speedup.
