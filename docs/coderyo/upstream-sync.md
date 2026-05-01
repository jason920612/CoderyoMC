# CoderyoMC Upstream Sync

CoderyoMC tracks `PaperMC/Paper:main` on the `coderyo` branch. Upstream syncs should be small, explicit commits so later concurrency work can be rebased or bisected without mixing Paper changes with Coderyo changes.

## Remote Layout

- `origin`: `https://github.com/jason920612/CoderyoMC.git`
- `paper`: `https://github.com/PaperMC/Paper.git`
- active downstream branch: `coderyo`
- upstream branch: `paper/main`

## Sync Command

From a clean `coderyo` worktree:

```powershell
powershell -ExecutionPolicy Bypass -File scripts/sync-paper-main.ps1
```

The script:

1. verifies the current branch is `coderyo`;
2. verifies the worktree has no pending changes;
3. ensures the `paper` remote points at PaperMC/Paper;
4. fetches `paper main --tags`;
5. fast-forwards `coderyo` to `paper/main`;
6. runs `gradlew.bat applyPatches`;
7. prints the new upstream baseline commit.

If the fast-forward fails, stop and inspect the divergence before merging. Do not combine conflict resolution with feature development in the same commit.

## Commit Policy

Use one commit for each successful upstream sync:

```powershell
git commit --allow-empty -m "Sync Paper upstream to <short-sha>"
```

Use an empty commit only when the branch fast-forward already contains Paper's commits and a visible downstream marker is needed.
