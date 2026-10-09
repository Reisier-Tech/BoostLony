# BoostColony - MineColonies Addon (Minecraft Forge 1.20.1)

**BoostColony** is a Minecraft Forge addon for [MineColonies](https://www.minecolonies.com) that maximizes citizen levels and stats, and dramatically accelerates construction and clearing speed with expanded builder placement reach.

It achieves this dynamically at runtime using Mixin bytecode injection—**without modifying the original MineColonies configuration file**.

---

## ✨ Features

- **Max Citizen Skills (Level 100)**: All citizen skill checks (Athletics, Strength, Agility, Stamina, Dexterity, Intelligence, Mana, etc.) operate at maximum efficiency.
- **Auto-Sync Citizen Stats**: Citizen skill data is automatically synced to Level 100 so the in-game UI and scrolls display max stats.
- **Ultra-Fast Building**: Builder block placement delay reduced from 150 ticks down to **1 tick** (configurable).
- **Ultra-Fast Destroying/Clearing**: Worker mining and clearing delay reduced from 40 ticks down to **1 tick** (configurable).
- **Expanded Placement Range**: Builder placement reach increased from 5 blocks to **32 blocks** (configurable from 5 up to 128 blocks), eliminating stuck pathfinding and scaffolding requirements.
- **Colony Boost Commands**: Instantly max out all citizens and buildings in your colony with convenient commands.

---

## 🛠️ In-Game Commands

| Command | Permission | Description |
| :--- | :--- | :--- |
| `/boostcolony status` | OP / Level 2 | Displays current speeds, placement reach, and boost toggles. |
| `/boostcolony maxskills [colonyId]` | OP / Level 2 | Maxes all citizen skills to 100 in the target colony. |
| `/boostcolony maxbuildings [colonyId]` | OP / Level 2 | Upgrades all buildings in the target colony to max level (Level 5). |
| `/boostcolony maxall [colonyId]` | OP / Level 2 | Maxes out both citizens and buildings. |
| `/boostcolony speed <buildDelay> <mineDelay>` | OP / Level 2 | Dynamically updates building and mining delays (in ticks). |
| `/boostcolony range <blocks>` | OP / Level 2 | Dynamically updates builder placement reach (in blocks). |

---

## ⚙️ Configuration (`boostcolony-server.toml`)

Stored in your world save's `serverconfig/boostcolony-server.toml` (or default config):

```toml
[BoostColony]
    # If true, all citizen skills will act as max level (default: true)
    maxCitizenSkills = true

    # If true, automatically updates citizens' actual saved skill levels to 100 (default: true)
    autoMaxSkillsOnUpdate = true

    # Max skill level value (default: 100)
    maxSkillLevel = 100

    # Delay ticks between placing blocks for Builders (MineColonies default is 150)
    buildingDelay = 1

    # Delay ticks between breaking/destroying blocks (MineColonies default is 40)
    miningDelay = 1

    # Range in blocks from which builders can place blocks (MineColonies default is 5)
    placeBlockRange = 32
```

---

## 🚀 Building & Running

### Automated Scripts:
- **`build.bat`**: Double-click to compile and package the mod JAR into `dist/`.
- **`test_client.bat`**: Double-click to launch the Minecraft Forge development client with BoostColony.

### Gradle Commands:
```bash
./gradlew build
./gradlew runClient
```
The compiled mod JAR is placed in `dist/BoostColony-1.20.1-1.0.0-all.jar`. Simply drop it into your `.minecraft/mods` directory.
