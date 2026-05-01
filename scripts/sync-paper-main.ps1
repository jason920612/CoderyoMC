param(
    [string] $PaperRemote = "paper",
    [string] $PaperUrl = "https://github.com/PaperMC/Paper.git",
    [string] $Branch = "coderyo"
)

$ErrorActionPreference = "Stop"

function Run-Git {
    param([Parameter(ValueFromRemainingArguments = $true)][string[]] $Arguments)
    & git @Arguments
    if ($LASTEXITCODE -ne 0) {
        throw "git $($Arguments -join ' ') failed with exit code $LASTEXITCODE"
    }
}

$currentBranch = (& git branch --show-current).Trim()
if ($currentBranch -ne $Branch) {
    throw "Expected branch '$Branch', but current branch is '$currentBranch'."
}

$status = (& git status --porcelain)
if ($status) {
    throw "Worktree is not clean. Commit or stash changes before syncing Paper upstream."
}

$remoteUrl = (& git remote get-url $PaperRemote 2>$null)
if ($LASTEXITCODE -ne 0) {
    Run-Git remote add $PaperRemote $PaperUrl
} elseif ($remoteUrl.Trim() -ne $PaperUrl) {
    Run-Git remote set-url $PaperRemote $PaperUrl
}

Run-Git fetch $PaperRemote main --tags
Run-Git merge --ff-only "$PaperRemote/main"

& .\gradlew.bat applyPatches
if ($LASTEXITCODE -ne 0) {
    throw "gradlew.bat applyPatches failed with exit code $LASTEXITCODE"
}

$baseline = (& git rev-parse --short HEAD).Trim()
Write-Host "CoderyoMC is synced to Paper baseline $baseline."
