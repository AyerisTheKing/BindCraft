# Changelog

## 0.1.0-alpha

### Fixed

- Fixed `PlayerInventoryAccessor` unresolved symbol by correcting the import path to `com.ayeristheking.bindcraft.mixin.PlayerInventoryAccessor` in `ActionExecutor`.
- Fixed missing client entrypoint `AttributeSwapClient` in `fabric.mod.json`, causing the mod UI and keybindings not to load.
- Updated keybinding identifiers in `ModKeyBindings.java` to use the `bindcraft` namespace to properly load the "BindCraft" category translation.
- Fixed duplicate background rendering in GUI screens caused by explicit `renderBackground` calls alongside `super.render`.
