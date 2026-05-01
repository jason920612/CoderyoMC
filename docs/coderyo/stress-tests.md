# CoderyoMC Concurrency Stress Tests

These stress tests are required before any experimental parallel flag can be considered for production use.

## Test Matrix

Run each case with:

- all Coderyo experimental flags disabled;
- `coderyo.parallel-world-ticking.enabled=true`;
- one additional experimental flag enabled at a time.

## Regression Cases

### Cross-World Teleport

- 10 players or bots teleport between overworld, nether, and end every 5 seconds.
- Include portal travel, command teleport, respawn, and plugin-style scheduled teleport.
- Fail on lost players, duplicate entities, wrong world ownership, or async catcher violations.

### Vehicles And Passengers

- Spawn boats, minecarts, horses, pigs, and multi-passenger stacks.
- Move across chunk boundaries while chunks load and unload.
- Fail on detached passengers, entity leaks, or concurrent modification exceptions.

### Projectiles And Explosions

- Fire arrows, tridents, fireballs, and TNT across chunk boundaries.
- Run explosions near entities, block entities, fluids, and world borders.
- Fail on missing damage, duplicated drops, stale explosion density cache, or event-thread violations.

### Chunk Load/Unload/Save

- Move bots in opposite directions across generated and ungenerated terrain.
- Force plugin chunk tickets on and off while autosave runs.
- Fail on sync load spikes above configured threshold, lost block entities, or save exceptions.

### Async Scheduler Handoff

- Schedule async tasks that attempt unsafe world/entity access.
- Schedule valid async compute tasks that hand results back to the owner thread.
- Fail if unsafe access is not caught or valid handoff deadlocks.

## Result Template

Record:

- commit SHA;
- enabled flags;
- seed;
- bot count;
- duration;
- crash status;
- async catcher count;
- watchdog warnings;
- max MSPT;
- p95/p99 MSPT;
- chunk sync load warnings;
- notes and log links.
