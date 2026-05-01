# CoderyoMC Threading Model

CoderyoMC concurrency work must preserve correctness before increasing parallelism. New parallel systems should define an owner, an allowed mutation set, and a synchronization boundary before they are enabled by default.

## Ownership Rules

- Server-global state is owned by the server tick thread unless a subsystem explicitly documents a different owner.
- A world is owned by its world tick worker only while experimental parallel world ticking is enabled and that world is inside its tick phase.
- A region or chunk may only become an owner after region-level ownership is implemented and `TickThread.isTickThreadFor(...)` is updated to enforce it.
- Entities are owned by the tick owner of their current world or future region owner.
- Plugin callbacks run on the owner thread of the event source unless the event is explicitly asynchronous.

## Mutation Rules

- World, block, entity, inventory, scoreboard, bossbar, advancement, recipe, and command state must not be mutated from arbitrary async threads.
- Async work may compute immutable snapshots, path candidates, serialization payloads, compression output, and database or file IO results.
- Async work must apply game-state changes by scheduling back to the owner thread.
- Cross-world mutation during parallel world ticking must be treated as unsafe unless it is routed through a serialized server-global phase.

## Tick Phases

The initial safe shape for parallelization is:

1. serialized server pre-world phase: scheduler heartbeat, global queues, command functions, clocks, global state;
2. parallel world phase: each loaded world ticks its own entities, chunks, block updates, and local events;
3. serialized server post-world phase: connection tick, status, autosave orchestration, global monitoring.

Do not move plugin-visible global behavior into the parallel phase until the ownership rule is explicit and tested.

## Event Dispatch

- Synchronous Bukkit/Paper events must fire on the owner thread.
- Event handlers may call APIs that are valid for the event owner.
- Event handlers must not directly mutate another world that is concurrently ticking.
- If compatibility requires cross-world mutation, the call should enqueue work to the destination owner and document the delayed behavior.

## Locking Rules

- Prefer ownership and message passing over broad locks.
- Do not hold locks while calling plugin code.
- Do not hold chunk, entity, or scheduler locks while waiting for a `CompletableFuture` that can schedule back to the same owner.
- Any blocking wait on tick-owned work must include a timeout or a documented shutdown-only reason.

## Configuration Gates

- New parallel behavior must be behind a global config flag.
- Experimental flags default to disabled.
- A flag may be enabled by default only after stress tests, compatibility notes, and rollback instructions exist.

## Required Review For New Parallel Code

Every new parallel subsystem must answer:

- what thread owns the data being read or mutated;
- whether plugin code can run inside the parallel section;
- how cross-owner calls are routed;
- how shutdown drains queued work;
- what tests or profiling scenarios cover the behavior.
