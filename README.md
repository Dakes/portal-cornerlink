# Portal Cornerlink

Control Nether portal linking by placing glazed terracotta at portal corners. Portals connect to destinations with matching corner patterns.

**Fork of**: [Corner Portal Linking](https://gitlab.com/mc-starbidou/corner-portal-linking) by starbidou
**This version**: Updated to 26.2 with performance improvements (4-8x faster portal matching)

> Minecraft 26.x is unobfuscated, so this branch uses Mojang's official mappings rather than Yarn
> and requires Java 25. The `1.21.10` branch remains for older versions; a jar built for 1.21.x will
> not load on 26.x, and vice versa.

## How It Works

1. Place any colored glazed terracotta at the 4 corners of a Nether portal
2. On the other side, portals with matching corner patterns link together
3. No corners = vanilla behavior

Patterns are matched by block type, orientation doesn't matter (flipped portals still match).

## Build

### Standard Build
```bash
./gradlew build
```
Output: `build/libs/dakes-cornerlink-fabric-1.0.1.jar`

### Development
```bash
./gradlew runClient  # Launch client with mod
./gradlew runServer  # Launch server with mod
```

### Nix Shell
```bash
nix develop  # Enter dev environment with JDK 25 and Gradle
```

The flake provides a reproducible development environment with all required dependencies.

## Requirements

- Minecraft 26.2
- Fabric Loader ≥0.19.3
- Fabric API ≥0.158.0
- Java 25

Fabric API is required: its resource loader is what makes the `cornerlink` block tag load. Without
it the mod silently falls back to vanilla portal linking.

## License

LGPL v2.1
