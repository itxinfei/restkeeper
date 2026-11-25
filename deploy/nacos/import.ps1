# 将 deploy/nacos/*.yml 导入 Nacos（PowerShell 版）
# 地址/凭据以《部署手册-中间件与数据》§3 为准：默认 http://192.168.20.88:8848。
# Nacos 3.x 已下线 /nacos/v1/cs/configs（404），改用 v3 Admin API；失败回退 v1。
# 鉴权开启时先用 NACOS_USERNAME/NACOS_PASSWORD 登录取 accessToken（或直接给 NACOS_ACCESS_TOKEN）。
$ErrorActionPreference = 'Stop'
$Nacos = if ($env:NACOS_SERVER) { $env:NACOS_SERVER } else { '192.168.20.88:8848' }
$Group = if ($env:NACOS_GROUP) { $env:NACOS_GROUP } else { 'DEFAULT_GROUP' }
$Dir = Split-Path -Parent $MyInvocation.MyCommand.Path

$Token = $env:NACOS_ACCESS_TOKEN
if (-not $Token) {
    $user = if ($env:NACOS_USERNAME) { $env:NACOS_USERNAME } else { 'nacos' }
    $pass = if ($env:NACOS_PASSWORD) { $env:NACOS_PASSWORD } else { '123456' }
    try {
        $login = Invoke-RestMethod -Method Post -Uri "http://$Nacos/nacos/v1/auth/login" -Body @{ username = $user; password = $pass }
        $Token = $login.accessToken
        Write-Host "Nacos 登录成功（$user）"
    } catch {
        Write-Host "Nacos 登录失败：$($_.Exception.Message)（将以匿名方式尝试）"
    }
}

function Publish-Config([string]$api, [string]$groupParam, [string]$dataId, [string]$group, [string]$content, [string]$token) {
    $body = @{
        dataId      = $dataId
        $groupParam = $group
        type        = 'yaml'
        content     = $content
    }
    if ($token) { $body['accessToken'] = $token }
    Invoke-RestMethod -Method Post -Uri $api -ContentType 'application/x-www-form-urlencoded; charset=UTF-8' -Body $body
}

Get-ChildItem -Path $Dir -Filter *.yml | ForEach-Object {
    $dataId = $_.Name
    $content = Get-Content -Raw -Encoding UTF8 $_.FullName
    try {
        Write-Host "导入 $dataId -> $Nacos ($Group)"
        Publish-Config "http://$Nacos/nacos/v3/admin/cs/config" 'groupName' $dataId $Group $content $Token | Out-Null
    } catch {
        Write-Host "  v3 失败，回退 v1：$($_.Exception.Message)"
        Publish-Config "http://$Nacos/nacos/v1/cs/configs" 'group' $dataId $Group $content $Token | Out-Null
    }
}
Write-Host "完成。可在控制台查看（Nacos 3.x 控制台端口 8080：http://$Nacos/nacos → 或 :8080/nacos）。"
