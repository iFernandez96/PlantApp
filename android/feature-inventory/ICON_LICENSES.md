# Wizard icon licenses

## Species icons (`ic_species_*.xml`)
Source: **openfarmcc/open-crop-icons** — <https://github.com/openfarmcc/open-crop-icons>
License: **CC0 1.0 / public domain** (no attribution required; recorded here for provenance).
Converted from the upstream SVGs (`icons/<crop>/<crop>.svg`, branch `mainline`) to Android vector
drawables (styles inlined with SVGO, then SVG→vector-drawable). Mapping:

| drawable | upstream crop |
|---|---|
| `ic_species_tomato.xml` | tomato |
| `ic_species_basil.xml` | basil |
| `ic_species_strawberry.xml` | strawberry |
| `ic_species_tomatillo.xml` | tomatillo |
| `ic_species_passionfruit.xml` | generic-plant (the set has no passion fruit) |
| `ic_species_default.xml` | generic-plant (fallback) |

## Pot + location icons
Source: **Material Symbols / Material Icons** (`androidx.compose.material:material-icons-extended`).
License: **Apache License 2.0**. Used as `ImageVector`s in `WizardIcons.kt` (no asset files).
Per the Apache license, this notice preserves the attribution.
