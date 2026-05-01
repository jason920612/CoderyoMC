# CoderyoMC Benchmark And Profiling Scenarios

Benchmarks should compare Paper baseline, Coderyo with experimental flags disabled, and Coderyo with one experimental flag enabled at a time.

## Required Result Fields

Record these fields for every run:

- commit SHA;
- Java version;
- CPU model and core count;
- memory flags;
- world seed and world count;
- plugin list;
- enabled Coderyo flags;
- average TPS and MSPT over 1m, 5m, and 15m windows;
- p95 and p99 tick duration if available;
- chunk load/generation rate;
- player count or bot count;
- entity count and ticking entity count;
- region file IO queue/flush observations.

## Scenario A: Idle Multi-World Baseline

Purpose: verify overhead of additional worlds and parallel world tick scheduling.

Setup:

- 3 worlds: overworld, nether, end;
- no plugins except required test harness;
- no players for first 5 minutes;
- 1 player or bot idling in each world for next 10 minutes.

Measure:

- world tick time per world;
- scheduler heartbeat time;
- connection tick time;
- total MSPT.

## Scenario B: Entity AI And Pathfinding

Purpose: expose entity activation, AI, sensor, navigation, and collision hot paths.

Setup:

- fixed arena chunks;
- spawn controlled groups of villagers, zombies, skeletons, animals, and item entities;
- run 5 minutes per entity scale step.

Scale steps:

- 250 active mobs;
- 500 active mobs;
- 1000 active mobs;
- 2000 active mobs.

Measure:

- entity tick time;
- pathfinding time;
- collision checks;
- activation range skips;
- tracker packet volume.

## Scenario C: Chunk Lifecycle

Purpose: isolate chunk load, generation, ticket updates, unloads, saves, and region IO.

Setup:

- pregenerated world for load/unload run;
- fresh seed for generation run;
- bot path moving in a spiral at fixed speed.

Measure:

- sync chunk load warnings;
- chunk generation throughput;
- ticket update time;
- main-thread chunk task time;
- region file write and flush latency.

## Scenario D: Block Updates And Explosions

Purpose: measure block physics, neighbor updates, fluid ticks, redstone, and explosion density cache behavior.

Setup:

- redstone clock grid;
- flowing water/lava grid;
- repeated TNT explosion arena.

Measure:

- scheduled block tick time;
- block physics event count;
- explosion processing time;
- plugin event dispatch time.

## Scenario E: Networking And Tracker

Purpose: measure packet processing, tracker updates, chunk sends, and compression overhead.

Setup:

- bot clients joining in batches;
- fixed view distance and simulation distance;
- scripted movement through loaded and newly generated terrain.

Measure:

- login/configuration latency;
- packets handled per tick;
- chunk packets sent per second;
- tracker update time;
- compression CPU time if available.

## Baseline Procedure

1. Build with `gradlew.bat createPaperclipJar`.
2. Start from a clean server directory.
3. Run each scenario once as warmup and once as the recorded run.
4. Keep experimental flags disabled for the first Coderyo baseline.
5. Enable one flag at a time for comparison.
6. Attach logs, config, timings/spark output, and commit SHA to the tracking issue.
