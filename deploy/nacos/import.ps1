# 将 deploy/nacos/*.yml 导入本地 Nacos（PowerShell 版）
$ErrorActionPreference = 'Stop'
$Nacos = if ($env:NACOS_SERVER) { $env:NACOS_SERVER } else { '127.0.0.1:8848' }
$Group = if ($env:NACOS_GROUP) { $env:NACOS_GROUP } else { 'DEFAULT_GROUP' }
$Dir = Split-Path -Parent $MyInvocation.MyCommand.Path
Get-ChildItem -Path (Join-Path $Dir 'nacos') -Filter *.yml | ForEach-Object {
    $dataId = $_.Name
    $content = Get-Content -Raw -Encoding UTF8 $_.FullName
    $body = @{
        dataId      = $dataId
        group       = $Group
        type        = 'yaml'
        content     = $content
        namespaceId = 'public'
    }
    Write-Host "导入 $dataId -> $Nacos ($Group)"
    Invoke-RestMethod -Method Post -Uri "http://$Nacos/nacos/v1/cs/configs" -Body $body | Out-Null
}
Write-Host "完成。可在控制台 http://$Nacos/nacos 查看。"
