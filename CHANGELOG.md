# Changelog

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
