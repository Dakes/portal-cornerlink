# Portal Cornerlink

Control Nether portal linking by placing glazed terracotta at portal corners. Portals connect to destinations with matching corner patterns.

**Fork of**: [Corner Portal Linking](https://gitlab.com/mc-starbidou/corner-portal-linking) by starbidou
**This version**: Minecraft 26.x on **Fabric and NeoForge**, with performance improvements (4-8x faster portal matching)

> Minecraft 26.x is unobfuscated, so this branch uses Mojang's official mappings rather than Yarn
> and requires Java 25. The jars are built against 26.2 but load on all of 26.x, since the vanilla
> portal APIs they bind to are unchanged across those versions. The `1.21.10` branch remains for
> older versions; a jar built for 1.21.x will not load on 26.x, and vice versa.

## How It Works

1. Place any colored glazed terracotta at the 4 corners of a Nether portal
2. On the other side, portals with matching corner patterns link together
3. No corners = vanilla behavior

Patterns are matched by block type, orientation doesn't matter (flipped portals still match).

## Project Layout

A [MultiLoader-Template](https://github.com/jaredlll08/MultiLoader-Template) style split — no
Architectury, since the mod needs no cross-platform abstraction layer.

```
common/     all the logic + the mixin (loader-agnostic, ~77% of the code)
fabric/     ModInitializer entrypoint + fabric.mod.json
neoforge/   @Mod entrypoint + neoforge.mods.toml
```

The mixin lives in `common` and is byte-identical for both loaders — possible only because 26.x is
unobfuscated, so Fabric and NeoForge now share the same vanilla names.

Widening the private vanilla method the mixin calls needs one file per loader, since the formats
differ. They are kept in `common` and must stay in sync:

| Loader | File |
|---|---|
| Fabric | `common/src/main/resources/dakes_cornerlink.classtweaker` |
| NeoForge | `common/src/main/resources/META-INF/accesstransformer.cfg` |

## Build

```bash
./gradlew build
```

Output:
- `fabric/build/libs/dakes_cornerlink-fabric-26.2-1.1.0.jar`
- `neoforge/build/libs/dakes_cornerlink-neoforge-26.2-1.1.0.jar`

### Development

```bash
./gradlew :fabric:runClient     # or :fabric:runServer
./gradlew :neoforge:runClient   # or :neoforge:runServer
```

Note: NeoForge's `runServer` does not forward stdin, so it cannot be driven from a pipe. Use
`./gradlew :neoforge:createServerLaunchScript` and run `neoforge/build/moddev/runServer.sh`
directly if you need console input.

### Nix Shell

```bash
nix develop  # Enter dev environment with JDK 25 and Gradle
```

## Requirements

- Minecraft 26.1 – 26.x
- Java 25
- **Fabric**: Fabric Loader ≥0.19.3 and **Fabric API**
- **NeoForge**: NeoForge for 26.x (no extra dependencies)

Fabric API is required on Fabric only: its resource loader is what makes the `cornerlink` block tag
load. Without it the mod silently falls back to vanilla portal linking. NeoForge loads mod data
packs natively, so the NeoForge build has no such dependency.

## License

LGPL v2.1
