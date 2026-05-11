#Requires -Version 5.1
<#
.SYNOPSIS
  LICENSE を Java ファイル先頭にブロックコメント（// 形式）で付与する。
  再実行時はマーカー間の古いブロックを削除してから再付与する。

.EXAMPLE
  .\add-license-header.ps1
  .\add-license-header.ps1 -LicensePath ".\LICENSE" -SourceRoot ".\cpp2csharp\src", ".\pipeline-core\src"
#>

param(
    [string] $LicensePath = ".\LICENSE",
    [string[]] $SourceRoot = @(
        ".\cpp2csharp\src",
        ".\pipeline-core\src",
        ".\token-rule-engine\src"
    ),
    [switch] $WhatIf
)

$ErrorActionPreference = "Stop"
$LicenseStartMarker = "// === LICENSE_START ==="
$LicenseEndMarker   = "// === LICENSE_END ==="

# LICENSE を読み込み、行頭に "// " を付けた文字列の配列を生成
function Get-LicenseCommentLines {
    $fullPath = [System.IO.Path]::GetFullPath((Join-Path $PSScriptRoot $LicensePath))
    if (-not (Test-Path -LiteralPath $fullPath)) {
        throw "LICENSE not found: $fullPath"
    }
    $lines = Get-Content -Path $fullPath -Encoding UTF8
    $commentLines = @($LicenseStartMarker)
    foreach ($line in $lines) {
        $commentLines += "// " + $line
    }
    $commentLines += $LicenseEndMarker
    return $commentLines
}

# ファイル内容からマーカー間（START〜END 含む）を削除した文字列を返す
function Remove-ExistingLicenseBlock {
    param([string] $content)

    $idxStart = $content.IndexOf($LicenseStartMarker)
    if ($idxStart -lt 0) {
        return $null  # マーカーなし → 削除不要
    }

    $idxEnd = $content.IndexOf($LicenseEndMarker, $idxStart)
    if ($idxEnd -lt 0) {
        return $null  # END が見つからない場合は触らない
    }

    $endOfBlock = $idxEnd + $LicenseEndMarker.Length
    # LICENSE_END の直後の改行1つまでをブロックとして削除
    if ($endOfBlock -lt $content.Length -and $content[$endOfBlock] -eq "`n") {
        $endOfBlock += 1
    } elseif ($endOfBlock -lt $content.Length -and $content[$endOfBlock] -eq "`r" -and ($endOfBlock + 1) -lt $content.Length -and $content[$endOfBlock + 1] -eq "`n") {
        $endOfBlock += 2
    }

    $before = $content.Substring(0, $idxStart).TrimEnd()
    $after  = $content.Substring($endOfBlock) -replace '^[\r\n]+', ""
    return $before + "`r`n`r`n" + $after
}

# 先頭にライセンスブロックを付与した内容を返す
function Set-LicenseHeader {
    param(
        [string] $content,
        [string[]] $licenseLines
    )

    $block = $licenseLines -join "`r`n"
    $rest = $content.TrimStart()
    if ($rest.Length -eq 0) {
        return $block + "`r`n"
    }
    return $block + "`r`n`r`n" + $rest
}

# --- main ---
$srcRoots = foreach ($root in $SourceRoot) {
    $fullPath = [System.IO.Path]::GetFullPath((Join-Path $PSScriptRoot $root))
    if (-not (Test-Path -LiteralPath $fullPath)) {
        throw "Source root not found: $fullPath"
    }
    $fullPath
}

$licenseLines = Get-LicenseCommentLines
$javaFiles = foreach ($srcRoot in $srcRoots) {
    Get-ChildItem -Path $srcRoot -Filter "*.java" -Recurse -File
}
$count = 0

foreach ($file in $javaFiles) {
    $fullName = $file.FullName
    $content = [System.IO.File]::ReadAllText($fullName, [System.Text.Encoding]::UTF8)

    $withoutOld = Remove-ExistingLicenseBlock -content $content
    if ($null -ne $withoutOld) {
        $content = $withoutOld
    }

    $newContent = Set-LicenseHeader -content $content -licenseLines $licenseLines

    if ($newContent -ne $content) {
        $count += 1
        if (-not $WhatIf) {
            [System.IO.File]::WriteAllText($fullName, $newContent, (New-Object System.Text.UTF8Encoding $false))
        }
        $action = if ($WhatIf) { "Would update" } else { "Updated" }
        Write-Host "$action $($file.FullName)"
    }
}

Write-Host "`nDone. $count file(s) $(if ($WhatIf) { 'would be ' })updated."
