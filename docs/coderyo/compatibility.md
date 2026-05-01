# CoderyoMC Compatibility And Migration Notes

CoderyoMC experimental parallel systems are disabled by default. They are intended for profiling and controlled testing until ownership checks, stress tests, and plugin compatibility notes are complete.

## Current Experimental Flags

Path: `config/paper-global.yml`

- `coderyo.parallel-world-ticking.enabled`
- `coderyo.parallel-world-ticking.threads`
- `coderyo.parallel-chunk-pipeline.enabled`
- `coderyo.parallel-chunk-pipeline.sync-load-warn-millis`
- `coderyo.async-entity-ai.enabled`
- `coderyo.async-entity-ai.threads`
- `coderyo.scheduler-safety.log-async-api-violations`
- `coderyo.scheduler-safety.fail-fast-async-api-violations`
- `coderyo.network-io.enabled`

## Known Unsafe Plugin Patterns

- calling world, entity, inventory, scoreboard, or command APIs from async tasks;
- mutating one world from an event fired by another world during parallel world ticking;
- blocking the server thread waiting for async work that schedules back to the server thread;
- relying on exact cross-world tick ordering;
- keeping raw NMS entity, chunk, or block entity references and using them from worker threads;
- modifying listener registration while events are being dispatched from tick logic.

## Parallel World Ticking Notes

When enabled, loaded worlds may tick concurrently. Global pre-world and post-world phases remain serialized, but code inside a world tick can run on a Coderyo `TickThread` worker.

Expected compatibility risks:

- plugins assuming all synchronous events run on the original server thread;
- plugins mutating another world directly inside a world event;
- shared static caches in plugins without synchronization;
- NMS access that checks only thread name instead of Bukkit ownership APIs.

Rollback:

```yaml
coderyo:
  parallel-world-ticking:
    enabled: false
```

Restart the server after changing this flag.

## Chunk Pipeline Notes

Coderyo currently adds slow synchronous chunk load instrumentation. It does not replace Moonrise chunk ownership or change save barriers.

Use `sync-load-warn-millis` to find plugin or server paths forcing synchronous chunk work:

```yaml
coderyo:
  parallel-chunk-pipeline:
    sync-load-warn-millis: 50
```

## Scheduler Safety Notes

`log-async-api-violations` controls stack trace logging for async API violations guarded by `AsyncCatcher`. Existing fail-fast behavior is preserved for guarded APIs.

Keep logging enabled while testing parallel systems.

## Migration Guidance For Plugin Developers

- Use Bukkit scheduler or Folia-style region/entity schedulers for owner-thread handoff.
- Treat async tasks as compute or IO only.
- Capture immutable data before leaving the owner thread.
- Apply results back on the owner thread.
- Avoid global mutable static state or guard it with explicit synchronization.
- Do not assume cross-world event ordering.

## Production Guidance

Do not enable experimental parallel flags on production servers until:

- stress tests pass for the server's plugin set;
- async catcher logs are clean;
- benchmark results show a measurable gain;
- rollback config is prepared;
- backups and save integrity checks are verified.
