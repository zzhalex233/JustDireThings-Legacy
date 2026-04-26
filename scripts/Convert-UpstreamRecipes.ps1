param(
    [string]$Source = "source/JustDireThings-main/src/generated/resources/data/justdirethings/recipe",
    [string]$Target = "src/main/resources/assets/justdirethings/recipes"
)

$ErrorActionPreference = "Stop"

$tagOreMap = @{
    "c:dusts/redstone" = "dustRedstone"
    "minecraft:leaves" = "treeLeaves"
    "minecraft:wool" = "blockWool"
}

function Test-Property($Object, [string]$Name) {
    return $null -ne $Object -and $null -ne $Object.PSObject.Properties[$Name]
}

function Convert-Ingredient($Ingredient, [ref]$RequiresForge) {
    if (Test-Property $Ingredient "item") {
        return [ordered]@{
            item = [string]$Ingredient.item
        }
    }

    if (Test-Property $Ingredient "tag") {
        $tag = [string]$Ingredient.tag
        if (-not $tagOreMap.ContainsKey($tag)) {
            throw "Unsupported recipe tag '$tag'"
        }
        $RequiresForge.Value = $true
        return [ordered]@{
            type = "forge:ore_dict"
            ore = $tagOreMap[$tag]
        }
    }

    throw "Unsupported ingredient shape"
}

function Convert-Result($Result) {
    $item = $null
    $count = 1

    if ($Result -is [string]) {
        $item = $Result
    } else {
        if (Test-Property $Result "id") {
            $item = [string]$Result.id
        } elseif (Test-Property $Result "item") {
            $item = [string]$Result.item
        }
        if (Test-Property $Result "count") {
            $count = [int]$Result.count
        }
    }

    if ([string]::IsNullOrWhiteSpace($item)) {
        throw "Recipe result is missing an item id"
    }

    $converted = [ordered]@{
        item = $item
    }
    if ($count -ne 1) {
        $converted.count = $count
    }
    return $converted
}

function Convert-ShapedRecipe($Recipe) {
    $requiresForge = $false
    $requiresForgeRef = [ref]$requiresForge
    $key = [ordered]@{}

    foreach ($entry in $Recipe.key.PSObject.Properties) {
        $key[$entry.Name] = Convert-Ingredient $entry.Value $requiresForgeRef
    }

    $type = if ($requiresForgeRef.Value) { "forge:ore_shaped" } else { "minecraft:crafting_shaped" }
    $converted = [ordered]@{
        type = $type
    }
    if (Test-Property $Recipe "group") {
        $converted.group = [string]$Recipe.group
    }
    $converted.pattern = @($Recipe.pattern)
    $converted.key = $key
    $converted.result = Convert-Result $Recipe.result
    return $converted
}

function Convert-ShapelessRecipe($Recipe) {
    $requiresForge = $false
    $requiresForgeRef = [ref]$requiresForge
    $ingredients = @()

    foreach ($ingredient in $Recipe.ingredients) {
        $ingredients += Convert-Ingredient $ingredient $requiresForgeRef
    }

    $type = if ($requiresForgeRef.Value) { "forge:ore_shapeless" } else { "minecraft:crafting_shapeless" }
    $converted = [ordered]@{
        type = $type
    }
    if (Test-Property $Recipe "group") {
        $converted.group = [string]$Recipe.group
    }
    $converted.ingredients = $ingredients
    $converted.result = Convert-Result $Recipe.result
    return $converted
}

function Convert-SmeltingRecipe($Recipe) {
    $requiresForge = $false
    $ingredient = Convert-Ingredient $Recipe.ingredient ([ref]$requiresForge)
    if ($requiresForge) {
        throw "Forge ore-dict smelting recipes are not emitted by this converter yet"
    }

    $converted = [ordered]@{
        type = "minecraft:smelting"
        ingredient = $ingredient
        result = (Convert-Result $Recipe.result).item
    }
    if (Test-Property $Recipe "experience") {
        $converted.experience = [double]$Recipe.experience
    }
    if (Test-Property $Recipe "cookingtime") {
        $converted.cookingtime = [int]$Recipe.cookingtime
    }
    return $converted
}

if (-not (Test-Path -LiteralPath $Source)) {
    throw "Source recipe directory does not exist: $Source"
}

New-Item -ItemType Directory -Force -Path $Target | Out-Null

$convertedCount = 0
$skippedByType = @{}
$failed = @()

Get-ChildItem -Path $Source -Filter "*.json" | Sort-Object Name | ForEach-Object {
    $recipe = Get-Content -LiteralPath $_.FullName -Raw | ConvertFrom-Json
    $converted = $null

    try {
        switch ([string]$recipe.type) {
            "minecraft:crafting_shaped" {
                $converted = Convert-ShapedRecipe $recipe
            }
            "minecraft:crafting_shapeless" {
                $converted = Convert-ShapelessRecipe $recipe
            }
            "minecraft:smelting" {
                $converted = Convert-SmeltingRecipe $recipe
            }
            default {
                $type = [string]$recipe.type
                if ($skippedByType.ContainsKey($type)) {
                    $skippedByType[$type]++
                } else {
                    $skippedByType[$type] = 1
                }
            }
        }

        if ($null -ne $converted) {
            $targetPath = Join-Path $Target $_.Name
            $converted | ConvertTo-Json -Depth 20 | Set-Content -LiteralPath $targetPath -Encoding UTF8
            $convertedCount++
        }
    } catch {
        $failed += "$($_.Name): $($_.Exception.Message)"
    }
}

Write-Host "Converted recipes: $convertedCount"
if ($skippedByType.Count -gt 0) {
    Write-Host "Skipped recipe types:"
    $skippedByType.GetEnumerator() | Sort-Object Name | ForEach-Object {
        Write-Host ("  {0}: {1}" -f $_.Name, $_.Value)
    }
}
if ($failed.Count -gt 0) {
    Write-Host "Failed recipes:"
    $failed | ForEach-Object { Write-Host "  $_" }
    exit 1
}
