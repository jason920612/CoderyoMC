param(
    [Parameter(Mandatory = $true)]
    [string] $ServerDirectory,
    [string] $Scenario = "idle-multi-world",
    [int] $DurationMinutes = 15
)

$ErrorActionPreference = "Stop"

$resolvedServerDirectory = Resolve-Path $ServerDirectory
$resultDirectory = Join-Path $resolvedServerDirectory "coderyo-benchmark-results"
New-Item -ItemType Directory -Force -Path $resultDirectory | Out-Null

$timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
$resultFile = Join-Path $resultDirectory "$timestamp-$Scenario.md"
$commit = (& git rev-parse --short HEAD).Trim()
$javaVersion = (& java -version 2>&1 | Select-Object -First 1)
$cpu = (Get-CimInstance Win32_Processor | Select-Object -First 1).Name
$cores = (Get-CimInstance Win32_Processor | Measure-Object -Property NumberOfLogicalProcessors -Sum).Sum

@"
# CoderyoMC Benchmark Result

- scenario: $Scenario
- duration-minutes: $DurationMinutes
- commit: $commit
- java: $javaVersion
- cpu: $cpu
- logical-processors: $cores
- server-directory: $resolvedServerDirectory
- started-at: $(Get-Date -Format o)

## Coderyo Flags

Record `config/paper-global.yml` Coderyo section here.

## Results

- 1m TPS/MSPT:
- 5m TPS/MSPT:
- 15m TPS/MSPT:
- p95 tick:
- p99 tick:
- entity count:
- ticking entity count:
- chunk load/generation rate:
- region IO observations:

## Attachments

- latest.log:
- spark/timings output:
- profiler output:

## Notes

"@ | Set-Content -Encoding UTF8 $resultFile

Write-Host "Created benchmark result template: $resultFile"
