# SmartSpawner - Fork of NighterDevelopment/SmartSpawner

## Branch Structure - READ THIS FIRST

This is a fork. There are THREE branches with distinct purposes:

| Branch | Purpose | Base |
|--------|---------|------|
| `main` | **Upstream mirror** - Nighter's original code. DO NOT commit custom changes here. Pull from `upstream` only. | upstream/main |
| `feature/database-storage-support` | **Database PR** - Pending pull request to upstream. Contains database storage support (MySQL/SQLite/YAML migration). | main |
| `feature/citizens-npc-activation` | **Custom build for DonutSMP** - Everything from database branch PLUS Citizens NPC spawner activation for ShadowAFK compatibility. | feature/database-storage-support |

### What this means in practice:
- **Upstream sync**: Pull `upstream/main` into `main`, then merge `main` into `feature/database-storage-support`, then merge that into `feature/citizens-npc-activation`
- **Database PR work**: Commit on `feature/database-storage-support`, then merge into `feature/citizens-npc-activation`
- **Custom/DonutSMP-only changes**: Commit on `feature/citizens-npc-activation` only
- **NEVER** put custom (non-PR) changes on `main` or `feature/database-storage-support`

### Remotes:
- `origin` = `bedge117/SmartSpawner` (the fork)
- `upstream` = `NighterDevelopment/SmartSpawner` (the original)

## Citizens NPC Activation (feature/citizens-npc-activation only)

This branch adds a Citizens API hook so that ShadowAFK shadow NPCs activate nearby spawners.

**How it works:** SmartSpawner's range checker (`SpawnerRangeChecker.getRangePlayers()`) normally only checks `Bukkit.getOnlinePlayers()`. The Citizens hook adds NPC positions (filtered by name suffix `-AFK`) to the range check array, so spawners within range of an AFK shadow NPC will activate and generate loot.

**Files unique to this branch:**
- `core/src/main/java/github/nighter/smartspawner/hooks/npcs/CitizensHook.java` - The hook class
- Modifications to: `IntegrationManager.java`, `SpawnerRangeChecker.java`, `PlayerRangeWrapper.java`, `build.gradle`, `paper-plugin.yml`, `plugin.yml`

**Config option** (in SmartSpawner config.yml):
```yaml
citizens_npc_activation:
  name_suffix: "-AFK"  # Only NPCs whose name ends with this are counted
```
Default is `-AFK` which matches ShadowAFK's `npc-name-format: "%name%-AFK"`.

## Build

```
gradlew shadowJar
```
Output: `core/build/libs/SmartSpawner-<version>.jar`
Deploy: Copy to `Plugin_Deploy/SmartSpawner.jar`

## Key Info

- Package: `github.nighter.smartspawner`
- Author: NighterDevelopment (upstream), cd3daddy (fork modifications)
- Java 21, Paper API 1.21+
- Uses Lombok `@Getter` on main class and IntegrationManager
- Folia-compatible (uses `Scheduler` abstraction for region-specific tasks)
