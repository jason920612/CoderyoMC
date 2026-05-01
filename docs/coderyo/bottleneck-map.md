# CoderyoMC Bottleneck Map

This map identifies the first high-impact single-threaded areas to measure and isolate before replacing serialized Paper behavior with parallel execution.

## Tick Loop

- `MinecraftServer#tickServer` and `MinecraftServer#tickChildren` serialize world ticking, connection ticking, scheduler heartbeats, command functions, autosave, and process queue draining.
- Risk: these stages share global server state and plugin callbacks.
- First target: preserve tick phase ordering while splitting only world-local work behind explicit flags.

## World Ticking

- Each `ServerLevel` currently ticks in sequence inside the server tick.
- Expensive work includes chunk source tick, entity ticking, block events, fluid/block random ticks, weather, raids, custom spawners, POI/village systems, and scheduled block ticks.
- First target: parallelize per-world tick bodies only when global pre/post phases remain serialized.

## Chunk Lifecycle

- Paper already includes Moonrise chunk system concurrency, but main-thread ticket updates and recently queued main-thread tasks still run inside server tick phases.
- Expensive work includes sync chunk loads, chunk holder updates, generation continuations, unload processing, serialization, and region file IO flushes.
- First target: reduce main-thread chunk task bursts and expose profiling around forced sync loads.

## Entity AI And Pathfinding

- Entity ticking, goal selectors, sensors, brains, navigation, target selection, collisions, and tracker updates run as tick-thread work.
- Expensive work often scales with active entity count and nearby block/entity queries.
- First target: optimize hot loops and identify AI/pathfinding tasks that can compute snapshots off-thread before applying results on the owning tick thread.

## Block And Entity Updates

- Block physics, neighbor updates, scheduled block ticks, fluid ticks, explosions, collision checks, and entity movement events are thread-sensitive.
- Risk: plugins can observe and mutate world state from event callbacks.
- First target: keep event dispatch on the owning tick thread and avoid off-thread world mutation.

## Scheduler

- Bukkit scheduler, Folia global scheduler, entity scheduler, process queue, and main-thread handoff utilities are central ordering points.
- Risk: allowing async tasks to enter main-thread-only APIs hides data races and causes deadlocks.
- First target: make thread ownership checks clearer and add stress coverage around async handoff paths.

## Networking

- Netty packet IO is asynchronous, but game packet handling, player state mutation, chunk sends, tracker sends, and login/configuration transitions synchronize with server tick state.
- First target: keep decode/encode/compression async and isolate state mutation onto the correct tick owner.

## IO

- Region file IO, player data saves, stats, advancements, profile cache, logs, and plugin data can stall ticks when flushed or synchronized.
- Paper already has async paths for several systems.
- First target: identify remaining forced waits and ensure shutdown/save barriers are explicit.

## Measurement Priority

1. tick phase timing: server tick, world tick, connection tick, scheduler, autosave;
2. per-world timing: chunk source, entities, block ticks, raids/spawners, tracker;
3. entity timing: activation, AI goals, sensors, pathfinding, collision, events;
4. chunk timing: ticket updates, sync loads, generation, saves, unloads;
5. IO timing: region file queue depth, flush latency, player data saves.
