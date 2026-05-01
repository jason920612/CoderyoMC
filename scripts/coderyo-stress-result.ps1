param(
    [Parameter(Mandatory = $true)]
    [string] $ServerDirectory,
    [Parameter(Mandatory = $true)]
    [string] $CaseName
)

$ErrorActionPreference = "Stop"

$resolvedServerDirectory = Resolve-Path $ServerDirectory
$resultDirectory = Join-Path $resolvedServerDirectory "coderyo-stress-results"
New-Item -ItemType Directory -Force -Path $resultDirectory | Out-Null

$timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
$resultFile = Join-Path $resultDirectory "$timestamp-$CaseName.md"
$commit = (& git rev-parse --short HEAD).Trim()

@"
# CoderyoMC Stress Result

- case: $CaseName
- commit: $commit
- started-at: $(Get-Date -Format o)
- server-directory: $resolvedServerDirectory

## Flags

- parallel-world-ticking:
- parallel-chunk-pipeline:
- async-entity-ai:
- network-io:

## Environment

- java:
- cpu:
- memory-flags:
- seed:
- bot-count:
- duration:

## Pass/Fail

- crashed:
- watchdog-warnings:
- async-catcher-count:
- max-mspt:
- p95-mspt:
- p99-mspt:
- sync-chunk-load-warnings:

## Notes

"@ | Set-Content -Encoding UTF8 $resultFile

Write-Host "Created stress result template: $resultFile"
