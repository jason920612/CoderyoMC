# Chunk Pipeline Concurrency Plan

Paper already carries Moonrise chunk pipeline work. Coderyo should improve this area by reducing forced waits and making remaining synchronous operations visible before adding new concurrency.

## Safe First Steps

- log slow synchronous chunk loads with world, chunk coordinates, and reason;
- measure main-thread chunk task bursts during `MinecraftServer#runAllTasksAtTickStart`;
- preserve Moonrise ticket and holder ownership;
- avoid changing plugin chunk ticket semantics until tests cover them;
- keep save and shutdown barriers explicit.

## Unsafe Without More Work

- mutating chunk sections off the owning tick thread;
- firing chunk load/unload events from arbitrary worker threads;
- completing generation futures while holding locks used by plugin callbacks;
- changing unload timing while entities or block entities are still visible to plugins;
- making region file flush asynchronous during final shutdown.

## Implementation Order

1. instrumentation for slow sync loads and queue depth;
2. stress tests for load/generate/unload/save cycles;
3. reduce unnecessary sync load callers;
4. batch or cap main-thread chunk task execution per tick;
5. only then consider new worker ownership boundaries.
