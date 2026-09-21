# Changelog

## Unreleased

### Other

- Updated to Minecraft 26.3: NeoForm `26.3-1`, Fabric API `0.161.0+26.3`, NeoForge
  `26.3.0.7-beta` (26.3 is beta-only upstream so far). ModDevGradle moved to 2.0.147 - the
  older NeoFormRuntime's Vineflower step runs out of memory decompiling the 26.3 jar.
- No source changes were needed: the vanilla portal APIs the mixin binds to are unchanged in
  26.3. Verified in game on a headless 26.3 Fabric dev server - a pig entering a portal with
  matching glazed terracotta corners reached the matching destination portal 12 blocks away
  rather than the nearer unmarked one, and the same rig without corners fell through to
  vanilla.

## 1.1.0

### Added

- NeoForge support. The mod now ships for both Fabric and NeoForge from a shared codebase.

## 1.0.1

### Performance

- Portal frames are now resolved once per portal instead of once per portal block. Large portals
  previously triggered a separate frame search for every block they contained.
- The matched portal's frame is reused instead of being recomputed, and candidates are scored once
  and picked in a single pass instead of being fully sorted.

### Other

- Updated to Minecraft 26.x. Now requires Java 25.
