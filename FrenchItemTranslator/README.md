# French Item Translator (Forge 1.20.1)

A client-side Minecraft Forge mod that adds a literal German word-by-word gloss directly below an item's French hover name.

Example (with Minecraft language set to **Français (France)**):

- `Bûche de chêne`
- `Stamm der Eiche`

## Requirements

- Minecraft 1.20.1
- Minecraft Forge 47.x (build configured for 47.2.0)
- Java 17
- Set Minecraft's language to **Français (France)**

The mod is client-side. A multiplayer server does not need to install it.

## Build

This source project uses ForgeGradle 6. The first build needs an internet connection so Gradle can download ForgeGradle and Minecraft/Forge dependencies.

On Windows, with Gradle 8.x installed:

```bat
gradle build
```

On Linux/macOS:

```sh
gradle build
```

The resulting mod JAR will be in `build/libs/`.

## Translation behavior

The mod reads the item's currently displayed French name and translates known French words using a small built-in literal glossary. Word order is intentionally preserved, so this is a learning aid rather than natural German. Unknown words remain in French. This means many vanilla names work immediately, while modded items may be only partly translated until their vocabulary is added.

Example: `Bûche de chêne` -> `Stamm der Eiche`.
