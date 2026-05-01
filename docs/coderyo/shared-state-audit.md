# Shared Mutable State Audit

This audit tracks mutable state that can be touched while worlds, chunks, entities, blocks, or plugin events are executing. Parallel systems must either keep these areas on one owner thread or add an explicit handoff.

## Server-Global State

- player list and player connection collections;
- command dispatcher, function manager, datapack reload state;
- recipe manager and registries exposed to plugins;
- scoreboard manager and teams;
- bossbars and advancements;
- Bukkit services, plugin manager, permissions, metadata stores;
- server process queue and synchronous scheduler queues.

Rule: keep mutation in serialized server phases until an owner-specific API exists.

## World State

- `ServerLevel` entity lookup and entity section storage;
- chunk source, distance manager, ticket storage, chunk holders;
- block ticks, fluid ticks, block events, explosions, raids, spawners;
- POI/village data and maps tied to world storage;
- world border, weather, time, game rules, spawn data.

Rule: world-local mutation may run on the world owner. Cross-world reads must use snapshots or serialized phases.

## Chunk State

- chunk sections and block states;
- block entities and pending block entity tickers;
- heightmaps, light data, biome data;
- chunk dirty flags and unsaved state;
- entity slices attached to chunks;
- plugin chunk tickets.

Rule: chunk state is owned by the world owner until region ownership is implemented.

## Entity State

- position, velocity, passengers, vehicle links;
- navigation and path state;
- brain memories, sensors, goal selectors, target selectors;
- equipment, inventory, effects, combat tracker;
- Bukkit entity wrapper and metadata;
- tracker visibility and packet state.

Rule: entity mutation must happen on the entity owner. Async AI may only produce immutable candidate results.

## Block And Event State

- block physics propagation;
- neighbor update queues;
- redstone and fluid updates;
- explosion density caches;
- synchronous Bukkit/Paper events;
- listener registration lists.

Rule: never hold subsystem locks while dispatching plugin events. Event-visible mutation remains owner-thread-only.

## Plugin Interaction Hotspots

- `BukkitScheduler` synchronous tasks;
- `callEvent` paths inside tick logic;
- metadata APIs;
- persistent data containers;
- scoreboard/entity/world APIs called from async tasks;
- plugin chunk tickets and forced chunks.

Rule: async plugin interaction must be rejected, logged, or rerouted to the owner thread.

## Required Before Enabling Parallel World Ticking By Default

- verify all synchronous events fired during world tick stay on the correct owner;
- prevent direct cross-world mutation from event handlers or queue it to destination owner;
- audit static caches used by entity AI/pathfinding and block updates;
- add stress tests for teleport, portal, vehicle, passenger, projectile, explosion, and chunk unload interactions;
- document known unsupported plugin patterns.
