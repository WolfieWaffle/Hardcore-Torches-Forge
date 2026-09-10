# Source comparison

Inspected on 2026-09-11. Source inspection, not a comparative FPS benchmark.
No implementation from another mod has been copied into this patch.

## Torches Burn Out

Repository: https://github.com/armet0451/torches_burn_out
File: `src/main/java/armet/torch_burnout/LitTorch.java`
Inspected blob: `33d76a687e9b6950d96d9fdfb03d6c801a1f9e60`.

`LitTorch` extends FaceAttachedHorizontalDirectionalBlock rather than an entity
block. Its randomTick checks LIT and a configured random probability, then
unlights/removes the block. It has no per-torch fuel countdown. Its scheduled
tick deals with waterlogging, not timed fuel expiry. This is a lightweight
option when stochastic burnout is the intended mechanic. It does not provide
a precise remaining-fuel value or the lantern/campfire system needed here.

## Realistic Torches

Repository: https://github.com/MattCzyr/RealisticTorches
Revision: `87c1aac6b696b24916128cf6f30c8a64bcf7ea9b`
File: `src/main/java/com/chaosthedude/realistictorches/blocks/RealisticTorchBlock.java`
Inspected blob: `3779ef3bf1bd13174b44a7a3bbaf79615f53ca67`.

The inspected implementation uses `TICK_INTERVAL = 1200`, scheduled block ticks,
and integer BlockState properties for burn time and lit/smoldering state. It
reduces remaining burn time by one unit per scheduled callback and reschedules.
This avoids a countdown running twenty times per second, at coarse granularity.
Rain is evaluated at those callbacks. The burn-time property expands the possible
block-state space with the configured lifetime. Its Forge implementation is a
reference design, not evidence that this particular source is a NeoForge 1.21.1
release.

## TerraFirmaCraft

Repository: https://github.com/TerraFirmaCraft/TerraFirmaCraft
Branch inspected: `1.21.x`
File: `src/main/java/net/dries007/tfc/common/blocks/TFCTorchBlock.java`
Inspected blob: `ac80c984fdb63ef93dd1a4d5057b4d764edf1586`.

`onRandomTick` retrieves a TickCounterBlockEntity and compares
`getTicksSinceUpdate()` with the configured torch lifetime. The block is replaced
when the age exceeds the limit. `setPlacedBy` resets the counter. The useful
architectural separation is between stored age and checking whether it has
expired. Actual replacement still waits for a random tick. The referenced
TFCTorchBlock file alone is not a full audit of every TFC timer or calendar.

## Hardcore Torches baseline

Repository: https://github.com/WolfieWaffle/Hardcore-Torches-Forge
Base: `febae6993e4b1b73f526a1308c53cc6e0acc7e0e`, branch `1.21.0`.

The relevant source is under
`src/main/java/com/github/wolfiewaffle/hardcore_torches/`:
- `blockentity/TorchBlockEntity.java`: per-tick lit fuel decrement/dirty marking,
  rain test, and stochastic smoldering decrement.
- `blockentity/LanternBlockEntity.java`, `block/AbstractLanternBlock.java`:
  ticker supplied independently of lit state; dirty marking even when unlit.
- `blockentity/HardcoreCampfireBlockEntity.java`: both server tick paths call
  takeFuelItems; the pickup method builds/merges shapes, obtains AABBs and runs
  entity queries/streams. It kills a whole ItemEntity for one item's fuel value.
- `compat/farmersdelight/HardcoreStoveBlockEntity.java`: separate stove fuel
  field/ticking implementation; not changed by the patch.
- `gradle.properties`: NeoForge 21.1.219 paired with Minecraft metadata `[1.21]`.

The patch combines lazily evaluated age/deadlines with a cancellable event queue.
It does not claim that using a custom queue is universally faster than vanilla
scheduled ticks; the cancellation/refuel/persistence requirements motivate it.

## Engine API references

NeoForge 1.21.1 block entities:
https://docs.neoforged.net/docs/1.21.1/blockentities/

NeoForge 1.21.1 lifecycle extension:
https://github.com/neoforged/NeoForge/blob/1.21.1/src/main/java/net/neoforged/neoforge/common/extensions/IBlockEntityExtension.java

NeoForge 1.21.1 level tick event:
https://github.com/neoforged/NeoForge/blob/1.21.1/src/main/java/net/neoforged/neoforge/event/tick/LevelTickEvent.java
