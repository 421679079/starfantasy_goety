[CmdletBinding()]
param(
    [string]$MinecraftRoot = $env:STARFANTASY_MC_ROOT,
    [string]$GradlePath = $env:STARFANTASY_GRADLE,
    [string]$Version = '0.8.42'
)

$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest

$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$workspaceRoot = (Resolve-Path (Join-Path $repoRoot '..')).Path
$dependencyRoot = Join-Path $workspaceRoot 'private-deps/goety'
if (-not (Test-Path -LiteralPath (Join-Path $workspaceRoot 'star_fantasy_library'))) {
    throw "Goety必须从完整的star_fantasy_workspace中构建：$workspaceRoot"
}

if ([string]::IsNullOrWhiteSpace($MinecraftRoot)) {
    throw '请使用 -MinecraftRoot 指向包含 libraries/ 和 versions/ 的 Minecraft 目录，或设置 STARFANTASY_MC_ROOT。'
}
$MinecraftRoot = (Resolve-Path $MinecraftRoot).Path

if ([string]::IsNullOrWhiteSpace($GradlePath)) {
    $GradlePath = Join-Path $workspaceRoot 'gradlew.bat'
}
if (-not (Test-Path -LiteralPath $GradlePath)) {
    throw "找不到Gradle Wrapper：$GradlePath。请确认仓库通过Git LFS完整拉取。"
}
$GradlePath = (Resolve-Path -LiteralPath $GradlePath).Path

function Add-ClassPath([System.Collections.Generic.List[string]]$List, [string]$Path) {
    if ([string]::IsNullOrWhiteSpace($Path)) { return }
    if (-not (Test-Path -LiteralPath $Path)) { throw "缺少构建依赖：$Path" }
    $full = (Resolve-Path -LiteralPath $Path).Path
    if (-not $List.Contains($full)) { $List.Add($full) }
}

function Write-JavacArgs([string]$Path, [string]$OutputDirectory,
                         [string]$Classpath, [string[]]$Sources) {
    $lines = [System.Collections.Generic.List[string]]::new()
    $lines.Add('"-proc:none"')
    $lines.Add('"-encoding"')
    $lines.Add('"UTF-8"')
    $lines.Add('"--release"')
    $lines.Add('"17"')
    $lines.Add('"-classpath"')
    $lines.Add('"' + $Classpath.Replace('\', '/').Replace('"', '') + '"')
    $lines.Add('"-d"')
    $lines.Add('"' + $OutputDirectory.Replace('\', '/').Replace('"', '') + '"')
    foreach ($source in $Sources) { $lines.Add('"' + $source.Replace('\', '/').Replace('"', '') + '"') }
    Set-Content -LiteralPath $Path -Value $lines -Encoding utf8
}

$buildRoot = Join-Path $repoRoot "build/reproducible-$Version-$(Get-Date -Format yyyyMMddHHmmss)"
$mainClasses = Join-Path $buildRoot 'main-classes'
$transformedSources = Join-Path $buildRoot 'transformed-sources'
$mainStubs = Join-Path $buildRoot 'main-stubs'
$stage = Join-Path $buildRoot 'jar-stage'
New-Item -ItemType Directory -Path $mainClasses, $transformedSources, $mainStubs, $stage | Out-Null

$goetyJar = Join-Path $dependencyRoot 'goety-2.5.56.5.jar'
$geckoJar = Join-Path $dependencyRoot 'geckolib-forge-1.20.1-4.8.4.jar'
$jadeJar = Join-Path $dependencyRoot 'jade-11.13.2.jar'
$curiosJar = Join-Path $dependencyRoot 'curios-forge-5.14.1+1.20.1.jar'
$baselineJar = Join-Path $dependencyRoot 'starfantasy_goety-bootstrap-0.1.258.jar'
foreach ($dependency in @{
    goety = $goetyJar; geckolib = $geckoJar; jade = $jadeJar;
    curios = $curiosJar; baseline = $baselineJar
}.GetEnumerator()) {
    if ([string]::IsNullOrWhiteSpace($dependency.Value)) {
        throw "找不到$($dependency.Key)依赖。请确认private-deps已经通过Git LFS拉取。"
    }
    if (-not (Test-Path -LiteralPath $dependency.Value)) {
        throw "缺少$($dependency.Key)依赖：$($dependency.Value)。请运行git lfs pull。"
    }
}

$churchDeps = Join-Path $repoRoot 'build/church-deps'
New-Item -ItemType Directory -Path $churchDeps -Force | Out-Null
Copy-Item -LiteralPath $goetyJar -Destination (Join-Path $churchDeps 'goety-2.5.52.4.jar') -Force
Copy-Item -LiteralPath $geckoJar -Destination (Join-Path $churchDeps 'geckolib-4.8.4.jar') -Force
Copy-Item -LiteralPath $jadeJar -Destination (Join-Path $churchDeps 'jade-11.13.2.jar') -Force
Copy-Item -LiteralPath $baselineJar -Destination (Join-Path $churchDeps 'starfantasy_goety-0.1.258.jar') -Force

Push-Location $workspaceRoot
try {
    & $GradlePath --no-daemon --rerun-tasks :starfantasy_goety:jar
    if ($LASTEXITCODE -ne 0) { throw "教堂模块Gradle构建失败，退出码$LASTEXITCODE。" }
} finally { Pop-Location }

$churchReobfJar = Join-Path $repoRoot 'build/reobfJar/output.jar'
if (-not (Test-Path -LiteralPath $churchReobfJar)) { throw "找不到教堂重映射输出：$churchReobfJar" }

$forgeDir = Join-Path $MinecraftRoot 'libraries/net/minecraftforge/forge/1.20.1-47.4.10'
$forgeClient = Join-Path $forgeDir 'forge-1.20.1-47.4.10-client.jar'
$forgeUniversal = Join-Path $forgeDir 'forge-1.20.1-47.4.10-universal.jar'
$clientSrg = Get-ChildItem -LiteralPath (Join-Path $MinecraftRoot 'libraries/net/minecraft/client') -Recurse -File -Filter '*-srg.jar' |
    Where-Object { $_.FullName -match '1\.20\.1' } | Select-Object -First 1
if ($null -eq $clientSrg) { throw '找不到Minecraft 1.20.1 SRG client JAR。' }

$classpathItems = [System.Collections.Generic.List[string]]::new()
Add-ClassPath $classpathItems $forgeClient
Add-ClassPath $classpathItems $clientSrg.FullName
Add-ClassPath $classpathItems $forgeUniversal
$minecraftLibs = Get-ChildItem -LiteralPath (Join-Path $MinecraftRoot 'libraries') -Recurse -File -Filter '*.jar' |
    Where-Object { $_.FullName -ne $forgeClient -and $_.FullName -ne $forgeUniversal -and $_.FullName -ne $clientSrg.FullName }
foreach ($library in $minecraftLibs) { Add-ClassPath $classpathItems $library.FullName }
$libraryJar = Get-ChildItem -LiteralPath (Join-Path $workspaceRoot 'star_fantasy_library/build/libs') -File -Filter 'star_fantasy_library-*.jar' |
    Sort-Object LastWriteTime -Descending | Select-Object -First 1
if ($null -eq $libraryJar) { throw '找不到刚构建的Star Fantasy Library JAR。' }
foreach ($external in @($goetyJar, $geckoJar, $jadeJar, $curiosJar, $libraryJar.FullName, $churchReobfJar)) {
    Add-ClassPath $classpathItems $external
}
$classpath = [string]::Join([IO.Path]::PathSeparator, $classpathItems)

$rendererRel = @(
    'com/starfantasy/goety/client/ApollyonArenaBoundaryRenderer.java',
    'com/starfantasy/goety/client/ApollyonArenaBoundaryRenderType.java',
    'com/starfantasy/goety/client/ApollyonGeoRenderer.java'
)
$partialSources = Get-ChildItem -LiteralPath (Join-Path $repoRoot 'src/main/java') -Recurse -File -Filter '*.java' |
    Where-Object { $rendererRel -notcontains (($_.FullName.Substring((Join-Path $repoRoot 'src/main/java').Length + 1) -replace '\\', '/')) } |
    ForEach-Object FullName

$stubPath = Join-Path $mainStubs 'com/starfantasy/goety/client/ApollyonGeoRenderer.java'
New-Item -ItemType Directory -Path (Split-Path $stubPath -Parent) -Force | Out-Null
@'
package com.starfantasy.goety.client;

import com.starfantasy.goety.entity.ApollyonEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/** Build-only placeholder; replaced by the transformed renderer below. */
public final class ApollyonGeoRenderer extends GeoEntityRenderer<ApollyonEntity> {
    public ApollyonGeoRenderer(EntityRendererProvider.Context context) {
        super(context, (net.minecraft.world.entity.EntityType<ApollyonEntity>) null);
    }
}
'@ | Set-Content -LiteralPath $stubPath -Encoding utf8

$partialArgs = Join-Path $buildRoot 'main-partial.args'
Write-JavacArgs $partialArgs $mainClasses $classpath ($partialSources + $stubPath)
& javac "@$partialArgs"
if ($LASTEXITCODE -ne 0) { throw "主体SRG编译失败，退出码$LASTEXITCODE。" }

$transformMap = @{
    'ApollyonArenaBoundaryRenderType.java' = @(
        @('Util.memoize', 'Util.m_143827_'), @('create(', 'm_173215_('),
        @('texture.getPath()', 'texture.m_135815_()'), @('DefaultVertexFormat.NEW_ENTITY', 'DefaultVertexFormat.f_85812_'),
        @('CompositeState.builder()', 'CompositeState.m_110628_()'), @('.setTextureState(', '.m_173290_('),
        @('.setShaderState(', '.m_173292_('), @('.setTransparencyState(', '.m_110685_('),
        @('.setCullState(', '.m_110661_('), @('.setLightmapState(', '.m_110671_('),
        @('.setOverlayState(', '.m_110677_('), @('.setWriteMaskState(', '.m_110687_('),
        @('.setDepthTestState(', '.m_110663_('), @('.createCompositeState(', '.m_110691_('),
        @('RENDERTYPE_ENTITY_TRANSLUCENT_EMISSIVE_SHADER', 'f_234323_'),
        @('TRANSLUCENT_TRANSPARENCY', 'f_110139_'), @('NO_CULL', 'f_110110_'),
        @('LIGHTMAP', 'f_110152_'), @('OVERLAY', 'f_110154_'),
        @('COLOR_WRITE', 'f_110115_'), @('LEQUAL_DEPTH_TEST', 'f_110113_')
    )
    'ApollyonArenaBoundaryRenderer.java' = @(
        @('OverlayTexture.NO_OVERLAY', 'OverlayTexture.f_118083_'), @('LightTexture.FULL_BRIGHT', '0xF000F0'),
        @('.vertex(', '.m_252986_('), @('.color(', '.m_85950_('), @('.uv(', '.m_7421_('),
        @('.overlayCoords(', '.m_86008_('), @('.uv2(', '.m_85969_('), @('.normal(', '.m_252939_('),
        @('.endVertex()', '.m_5752_()')
    )
    'ApollyonGeoRenderer.java' = @(
        @('public boolean shouldRender(', 'public boolean m_5523_('), @('super.shouldRender(', 'super.m_5523_('),
        @('frustum.isVisible(', 'frustum.m_113029_(')
    )
}
foreach ($entry in $transformMap.GetEnumerator()) {
    $sourcePath = Join-Path $repoRoot "src/main/java/com/starfantasy/goety/client/$($entry.Key)"
    $targetPath = Join-Path $transformedSources "com/starfantasy/goety/client/$($entry.Key)"
    New-Item -ItemType Directory -Path (Split-Path $targetPath -Parent) -Force | Out-Null
    $text = Get-Content -LiteralPath $sourcePath -Raw
    foreach ($pair in $entry.Value) { $text = $text.Replace($pair[0], $pair[1]) }
    Set-Content -LiteralPath $targetPath -Value $text -Encoding utf8
}
$rendererArgs = Join-Path $buildRoot 'renderers.args'
$rendererSources = Get-ChildItem -LiteralPath $transformedSources -Recurse -File -Filter '*.java' | ForEach-Object FullName
$rendererClasspath = $classpath + [IO.Path]::PathSeparator + $mainClasses
Write-JavacArgs $rendererArgs $mainClasses $rendererClasspath ($rendererSources + @())
& javac "@$rendererArgs"
if ($LASTEXITCODE -ne 0) { throw "边界/亚波伦渲染源码转换编译失败，退出码$LASTEXITCODE。" }

Push-Location $stage
try { & jar xf $churchReobfJar } finally { Pop-Location }
Copy-Item -Path (Join-Path $mainClasses '*') -Destination $stage -Recurse -Force
Copy-Item -Path (Join-Path $repoRoot 'src/main/resources/*') -Destination $stage -Recurse -Force
Copy-Item -LiteralPath (Join-Path $repoRoot 'LICENSE') -Destination $stage -Force
$manifestPath = Join-Path $stage 'META-INF/MANIFEST.MF'
$manifestText = Get-Content -LiteralPath $manifestPath -Raw
$manifestText = [regex]::Replace($manifestText, '(?m)^Implementation-Version:.*$', "Implementation-Version: $Version")
$timestamp = (Get-Date).ToString("yyyy-MM-dd'T'HH:mm:sszzz")
$manifestText = [regex]::Replace($manifestText, '(?m)^Implementation-Timestamp:.*$', "Implementation-Timestamp: $timestamp")
Set-Content -LiteralPath $manifestPath -Value $manifestText -Encoding ascii

$releaseDir = Join-Path $repoRoot 'build/releases'
New-Item -ItemType Directory -Path $releaseDir -Force | Out-Null
$outputJar = Join-Path $releaseDir "starfantasy_goety-$Version-clean.jar"
if (Test-Path -LiteralPath $outputJar) {
    Remove-Item -LiteralPath $outputJar -Force
}
& jar cfm $outputJar $manifestPath -C $stage .
if ($LASTEXITCODE -ne 0) { throw "最终JAR打包失败，退出码$LASTEXITCODE。" }

Add-Type -AssemblyName System.IO.Compression.FileSystem
$zip = [IO.Compression.ZipFile]::OpenRead($outputJar)
try {
    $entries = @($zip.Entries | ForEach-Object FullName)
    $required = @(
        'com/starfantasy/goety/client/ApollyonArenaBoundaryRenderer.class',
        'com/starfantasy/goety/client/ApollyonArenaBoundaryRenderType.class',
        'com/starfantasy/goety/client/ApollyonGeoRenderer.class',
        'com/starfantasy/goety/church/ChurchContent.class',
        'com/starfantasy/goety/network/StarFantasyGoetyNetwork.class',
        'com/starfantasy/goety/mixin/ApostleAppearanceMixin.class',
        'com/starfantasy/goety/api/ApostleAppearanceAccess.class',
        'com/starfantasy/goety/config/ApostleConfig.class',
        'com/starfantasy/goety/client/apostle/ConfigurableApostleRenderer.class',
        'com/starfantasy/goety/client/apostle/ApostleSoundEvents.class',
        'com/starfantasy/goety/magic/focus/BattleFocusContent.class',
        'com/starfantasy/goety/magic/focus/BattleFocusSpell.class',
        'com/starfantasy/goety/magic/focus/FlowerArrowRain.class',
        'com/starfantasy/goety/magic/focus/EvernightCast.class',
        'com/starfantasy/goety/magic/focus/client/BattleFocusRenderers.class',
        'assets/starfantasy_goety/models/item/blooms_and_plumes_focus.json',
        'assets/starfantasy_goety/models/item/evernight_focus.json',
        'assets/starfantasy_goety/textures/item/blooms_and_plumes_focus.png',
        'assets/starfantasy_goety/textures/item/evernight_focus.png',
        'assets/starfantasy_goety/geo/entity/apollyon/apollyon.geo.json',
        'assets/starfantasy_goety/textures/entity/apollyon/apollyon.png',
        'assets/starfantasy_goety/animations/entity/apostle/apostle.animation.json',
        'data/forge/tags/entity_types/bosses.json',
        'THIRD_PARTY_NOTICES.md',
        'THIRD_PARTY/Mhzy/LICENSE.txt',
        'CREDITS.md',
        'THIRD_PARTY/Apostle_T-fix10/LICENSE.txt',
        'LICENSE',
        'META-INF/mods.toml'
    )
    foreach ($entry in $required) { if ($entries -notcontains $entry) { throw "最终JAR缺少必要条目：$entry" } }
    $apostleModels = @($entries | Where-Object { $_ -like 'assets/starfantasy_goety/geo/entity/apostle/*.geo.json' })
    $apostleTextures = @($entries | Where-Object { $_ -like 'assets/starfantasy_goety/textures/entity/apostle/*.png' })
    $apostleSounds = @($entries | Where-Object { $_ -like 'assets/starfantasy_goety/sounds/entity/apostle/*.ogg' })
    if ($apostleModels.Count -ne 13 -or $apostleTextures.Count -ne 13 -or $apostleSounds.Count -ne 4) {
        throw '使徒模型、贴图或音效数量不完整。'
    }
    if (@($entries | Where-Object { $_ -like '*starfantasy_doki_apostle*' -or $_ -like 'com/funits/*' -or $_ -eq 'assets/goety/sounds.json' }).Count -ne 0) {
        throw '发布包仍包含独立Doki命名空间或全局声音覆盖。'
    }
    $reader = [IO.StreamReader]::new($zip.GetEntry('META-INF/mods.toml').Open())
    try { $modMetadata = $reader.ReadToEnd() } finally { $reader.Dispose() }
    if ($modMetadata -match 'starfantasy_doki_apostle' -or
            [regex]::Matches($modMetadata, '(?m)^\[\[mods\]\]').Count -ne 1 -or
            $modMetadata -notmatch 'license="GPL-3.0-only for code; third-party assets under separate terms\. See CREDITS\.md and THIRD_PARTY_NOTICES\.md\."') {
        throw '模组注册、依赖或许可证声明不符合单模组合并要求。'
    }
    if (@($entries | Where-Object { $_ -like '*.java' -or $_ -like '*.bbmodel' }).Count -ne 0) { throw '最终JAR包含不应发布的源码或模型工程文件。' }
    [pscustomobject]@{
        Output = $outputJar
        Size = (Get-Item -LiteralPath $outputJar).Length
        Classes = @($entries | Where-Object { $_ -like '*.class' }).Count
        Structures = @($entries | Where-Object { $_ -like 'data/starfantasy_goety/structures/*.nbt' }).Count
        Version = $Version
    } | Format-List
} finally { $zip.Dispose() }
