# 📖 CobbleDailyRewards Complete Wiki & Configuration Guide

Welcome to the **CobbleDailyRewards** documentation! CobbleDailyRewards is a flexible, highly customizable daily reward and recurring gift system designed for Minecraft Cobblemon servers. It allows server administrators to set up multiple independent reward buttons inside a clean GUI—each with its own cooldown timers, VIP permission scaling, dynamic button appearances, and rich loot tables.

---

## 📌 Table of Contents
1. [Overview & Core Architecture](#-overview--core-architecture)
2. [Commands & Permissions](#-commands--permissions)
3. [GUI Display & Cooldown Mechanics](#-gui-display--cooldown-mechanics)
   - [Dual-State Button Display](#dual-state-button-display)
   - [Automated Claim Reminders](#automated-claim-reminders)
   - [Permission-Based Cooldown Scaling](#permission-based-cooldown-scaling)
4. [Directory & File Organization](#-directory--file-organization)
5. [Complete JSON Configuration Schema](#-complete-json-configuration-schema)
   - [Global Settings (`config.json`)](#global-settings-configjson)
   - [Reward Definitions (`rewards/<id>.json`)](#reward-definitions-rewardsidjson)
   - [ItemModel Display Schema](#itemmodel-display-schema)
   - [AdvancedItemChance Reward Schema](#advanceditemchance-reward-schema)
6. [Production JSON Configuration Examples](#-production-json-configuration-examples)
   - [Example 1: Standard Daily Gift with VIP Cooldown Boost (`daily_common.json`)](#example-1-standard-daily-gift-with-vip-cooldown-boost-daily_commonjson)
   - [Example 2: Weekly Master Mystery Chest (`weekly_mega.json`)](#example-2-weekly-master-mystery-chest-weekly_megajson)
   - [Example 3: VIP Rank Exclusive Daily (`vip_exclusive.json`)](#example-3-vip-rank-exclusive-daily-vip_exclusivejson)
7. [Database & Storage Engine](#-database--storage-engine)
8. [Troubleshooting & FAQ](#-troubleshooting--faq)

---

## 🌟 Overview & Core Architecture

- **Multi-Reward Grid**: Rather than restricting players to a single daily streak, CobbleDailyRewards supports an arbitrary number of simultaneous reward tiers configured inside a customizable chest GUI. You can run hourly, daily, weekly, or rank-exclusive rewards simultaneously.
- **Dual Visual Button States**: Each reward button displays two completely distinct `ItemModel` designs: one when ready to claim (e.g. an animated golden chest) and one when locked on cooldown (e.g. a gray minecart showing the live countdown).
- **VIP Cooldown Multipliers**: Using LuckPerms groups, server owners can grant faster cooldowns to ranked players (e.g. standard players wait 24 hours, VIPs wait 18 hours, and MVPs wait 12 hours).
- **Background Alert Task**: CobbleDailyRewards runs a periodic background check (default every 15 minutes). When an online player has any unclaimed reward off cooldown, it gently alerts them in chat so rewards are never forgotten.
- **Thread-Safe Async Processing**: User cooldown checks and database saves execute on a dedicated 4-thread pool (`CobbleDailyRewards-Executor-%d`), ensuring zero tick lag when players join or claim rewards.

---

## 💻 Commands & Permissions

Base command aliases (configurable in `config.json`): `/dailyrewards`

### Player Commands
| Command | Permission | Description |
| :--- | :--- | :--- |
| `/dailyrewards` | *None (All Players)* | Opens the interactive daily rewards menu. |

### Staff & Admin Commands
| Command | Arguments | Permission Node | Default Level | Description |
| :--- | :--- | :--- | :--- | :--- |
| `/dailyrewards reload` | *None* | `cobbledailyrewards.reload` | Level 2 (OP) | Hot-reloads all configuration files, language strings, and reward files without a server restart. |
| `/dailyrewards other <player>` | `<player>` | `cobbledailyrewards.other` | Level 2 (OP) | Opens the daily rewards menu on behalf of another online player. |
| `/dailyrewards restart [player]` | `[player]` | `cobbledailyrewards.restart` / `cobbledailyrewards.admin` | Level 2 (OP) | Resets all active reward cooldowns for yourself or the specified target player. |

---

## ⚡ GUI Display & Cooldown Mechanics

### Dual-State Button Display
Every reward configured in `rewards/` defines two visual states:
1. **`withoutCooldown`**: Visible when the player is eligible to click and claim the reward.
2. **`withCooldown`**: Visible while the player is waiting for the timer to elapse.

Both states support dynamic format placeholders in their display names and lore:
- `%cooldown%`: Replaced with the remaining formatted time (e.g. `4h 12m 30s`) or `Ready!` when claimable.
- `%permission%`: Displays the required permission node or rank needed to access this reward slot.

### Automated Claim Reminders
In `config.json`, the `"checkReward"` parameter defines an interval in minutes (default: `15`). Every interval, the server checks all online players. If a player has at least one reward ready to claim, they receive an alert notification defined in `lang.json`.

### Permission-Based Cooldown Scaling
Inside each reward file, the `cooldowns` map allows configuring different cooldown durations based on LuckPerms permissions:
```json
"cooldown": "24h",
"cooldowns": {
  "": "24h",
  "group.vip": "18h",
  "group.mvp": "12h"
}
```
When evaluating a player's timer, the mod checks their permissions from top to bottom and applies the most beneficial (shortest) cooldown matched.

---

## 📁 Directory & File Organization

All configurations and data files reside inside `config/cobbledailyrewards/`:

```text
config/cobbledailyrewards/
├── config.json          # Main settings, GUI dimensions, alert frequency, database
├── WIKI.md              # Auto-extracted in-game documentation guide
├── lang/
│   └── en.json          # Translatable messages, titles, prefixes, and alert notices
├── rewards/             # Individual reward definition files
│   ├── daily_common.json
│   ├── weekly_mega.json
│   └── vip_exclusive.json
└── data/
    └── users/           # Player cooldown history (when using JSON storage)
        └── <uuid>.json
```

---

## 📝 Complete JSON Configuration Schema

### Global Settings (`config/cobbledailyrewards/config.json`)

```json
{
  "debug": false,
  "active": true,
  "lang": "en",
  "commands": [
    "dailyrewards",
    "rewards",
    "daily"
  ],
  "database": {
    "type": "JSON",
    "database": "dailyrewards",
    "url": "mongodb://localhost:27017",
    "user": "user",
    "password": "password"
  },
  "rows": 3,
  "checkReward": 15
}
```

| Field | Type | Default | Description |
| :--- | :--- | :--- | :--- |
| `debug` | `Boolean` | `false` | Enable verbose debug logging in the server console. |
| `active` | `Boolean` | `true` | Master switch to enable or disable the entire daily reward system. |
| `lang` | `String` | `"en"` | Language file identifier loaded from `lang/<lang>.json`. |
| `commands` | `List<String>` | `["dailyrewards"]` | List of command aliases registered in Minecraft to open the GUI. |
| `database` | `Object` | *DataBaseConfig* | Storage backend configuration (`JSON` or `MONGODB`). |
| `rows` | `Short` | `3` | Number of chest GUI rows (1 to 6). Determines how many slots are available (`rows * 9`). |
| `checkReward` | `Integer` | `15` | Minutes between automatic chat reminders sent to online players with ready rewards. |

---

### Reward Definitions (`config/cobbledailyrewards/rewards/<id>.json`)

```json
{
  "id": "daily_common",
  "slot": 11,
  "cooldown": "24h",
  "cooldowns": {
    "": "24h",
    "group.vip": "18h",
    "group.mvp": "12h"
  },
  "permission": "",
  "withoutCooldown": { ... },
  "withCooldown": { ... },
  "rewards": { ... }
}
```

| Field | Type | Default | Description |
| :--- | :--- | :--- | :--- |
| `id` | `String` | `"Default"` | Unique identifier for this reward. Corresponds to the filename (without `.json`). |
| `slot` | `Short` | `0` | GUI slot index where this button appears (0-indexed: row 1 is 0-8, row 2 is 9-17, etc.). |
| `cooldown` | `String` | `"24h"` | Default fallback duration string (`"24h"`, `"7d"`, `"30m"`). |
| `cooldowns` | `Map<String, String>` | `{"": "24h"}` | Permission-based cooldown overrides. `""` represents standard non-VIP players. |
| `permission` | `String` | `""` | Optional permission node required to view and claim this reward (e.g. `"cobbledailyrewards.vip"`). Leave empty for everyone. |
| `withoutCooldown` | `Object` | *ItemModel* | Icon appearance when the reward is ready to be claimed. |
| `withCooldown` | `Object` | *ItemModel* | Icon appearance while waiting on cooldown. |
| `rewards` | `Object` | *AdvancedItemChance* | Complete reward distribution engine (items, commands, currency, sounds, animations). |

---

### ItemModel Display Schema
Defines the visual item shown in the GUI:

```json
{
  "item": "minecraft:chest_minecart",
  "customModelData": 0,
  "displayname": "<gold><bold>Daily PokéLoot</bold></gold>",
  "lore": [
    "<gray>Click to claim your daily rewards!",
    "",
    "<yellow>Cooldown: <green>%cooldown%"
  ]
}
```

| Field | Type | Default | Description |
| :--- | :--- | :--- | :--- |
| `item` | `String` | `"minecraft:paper"` | Resource location of the Minecraft or Cobblemon item. |
| `customModelData` | `Integer` | `0` | Custom Model Data integer for custom resource pack models. |
| `displayname` | `String` | `""` | Button label. Supports MiniMessage (`<green>`, `<bold>`) or standard color codes (`&a`, `&l`). |
| `lore` | `List<String>` | `[]` | List of lore lines shown when hovering over the button. Supports `%cooldown%` and `%permission%`. |

---

### AdvancedItemChance Reward Schema
Full reward execution engine:

```json
{
  "id": "daily_payout",
  "showMenu": true,
  "title": "<gold><bold>Daily Rewards Claimed!</bold></gold>",
  "giveAll": true,
  "cumulativeLootTable": false,
  "amountRewardsPermission": {
    "": 1
  },
  "animation": "NONE",
  "newSound": {
    "sound": "minecraft:entity.player.levelup",
    "volume": 1.0,
    "pitch": 1.2
  },
  "particle": {
    "particle": "minecraft:totem_of_undying",
    "count": 25,
    "speed": 0.15,
    "offsetX": 0.5,
    "offsetY": 0.5,
    "offsetZ": 0.5
  },
  "lootTable": {
    "": [ ... ]
  }
}
```

---

## 💡 Production JSON Configuration Examples

### Example 1: Standard Daily Gift with VIP Cooldown Boost (`daily_common.json`)
A daily gift located in slot 11 (second row, third column) that regular players claim every 24h, but VIPs can claim every 12h:

```json
{
  "id": "daily_common",
  "slot": 11,
  "cooldown": "24h",
  "cooldowns": {
    "": "24h",
    "group.vip": "16h",
    "group.mvp": "12h"
  },
  "permission": "",
  "withoutCooldown": {
    "item": "cobblemon:poke_ball",
    "customModelData": 0,
    "displayname": "<green><bold>Daily Trainer Supplies</bold></green>",
    "lore": [
      "<gray>Your daily package is ready to open!",
      "",
      "<yellow>Includes PokéBalls, Potions, and Currency.",
      "<green>Click to claim!"
    ]
  },
  "withCooldown": {
    "item": "minecraft:gray_dye",
    "customModelData": 0,
    "displayname": "<red><bold>Daily Supplies (On Cooldown)</bold></red>",
    "lore": [
      "<gray>You have already claimed this today.",
      "",
      "<yellow>Available in: <red>%cooldown%",
      "<gray>VIP ranks receive reduced cooldowns!"
    ]
  },
  "rewards": {
    "id": "daily_common_rewards",
    "showMenu": true,
    "title": "<green>Daily Rewards",
    "giveAll": true,
    "cumulativeLootTable": false,
    "amountRewardsPermission": { "": 1 },
    "animation": "NONE",
    "newSound": {
      "sound": "minecraft:entity.experience_orb.pickup",
      "volume": 1.0,
      "pitch": 1.0
    },
    "lootTable": {
      "": [
        {
          "chance": 100.0,
          "item": "cobblemon:ultra_ball",
          "minAmount": 5,
          "maxAmount": 5,
          "displayname": "<aqua>Ultra Ball",
          "commands": [
            "eco give %player% 500"
          ]
        },
        {
          "chance": 100.0,
          "item": "cobblemon:rare_candy",
          "minAmount": 1,
          "maxAmount": 2,
          "commands": []
        }
      ]
    }
  }
}
```

---

### Example 2: Weekly Master Mystery Chest (`weekly_mega.json`)
A weekly high-tier reward in slot 13 (center of row 2) with a 7-day cooldown and CS:GO roll animation:

```json
{
  "id": "weekly_mega",
  "slot": 13,
  "cooldown": "7d",
  "cooldowns": {
    "": "7d"
  },
  "permission": "",
  "withoutCooldown": {
    "item": "cobblemon:master_ball",
    "customModelData": 0,
    "displayname": "<gold><bold>Weekly Grand Mystery Box</bold></gold>",
    "lore": [
      "<gray>A massive weekly cache of rare items!",
      "",
      "<yellow>Cooldown: <aqua>7 Days",
      "<green><bold>READY TO CLAIM! CLICK TO OPEN!"
    ]
  },
  "withCooldown": {
    "item": "minecraft:iron_bars",
    "customModelData": 0,
    "displayname": "<red><bold>Weekly Grand Box (Locked)</bold></red>",
    "lore": [
      "<gray>Resets once per week.",
      "",
      "<yellow>Next claim in: <red>%cooldown%"
    ]
  },
  "rewards": {
    "id": "weekly_mega_loot",
    "showMenu": true,
    "title": "<gold><bold>Weekly Grand Box Opened!</bold></gold>",
    "giveAll": false,
    "cumulativeLootTable": false,
    "amountRewardsPermission": { "": 2 },
    "animation": "CSGO",
    "newSound": {
      "sound": "minecraft:ui.toast.challenge_complete",
      "volume": 1.0,
      "pitch": 1.0
    },
    "lootTable": {
      "": [
        {
          "chance": 40.0,
          "item": "cobblemon:master_ball",
          "minAmount": 1,
          "maxAmount": 1,
          "displayname": "<purple><bold>Master Ball</bold></purple>",
          "commands": [
            "broadcast <gold>%player% <yellow>found a <purple>Master Ball <yellow>in their Weekly Grand Box!"
          ]
        },
        {
          "chance": 60.0,
          "item": "cobblemon:ability_patch",
          "minAmount": 1,
          "maxAmount": 2,
          "commands": [
            "eco give %player% 5000"
          ]
        }
      ]
    }
  }
}
```

---

### Example 3: VIP Rank Exclusive Daily (`vip_exclusive.json`)
A daily gift in slot 15 that requires the `cobbledailyrewards.vip` permission:

```json
{
  "id": "vip_exclusive",
  "slot": 15,
  "cooldown": "24h",
  "cooldowns": {
    "": "24h"
  },
  "permission": "cobbledailyrewards.vip",
  "withoutCooldown": {
    "item": "minecraft:nether_star",
    "customModelData": 0,
    "displayname": "<light_purple><bold>VIP Daily Bonus</bold></light_purple>",
    "lore": [
      "<gray>Exclusive daily reward for VIP patrons.",
      "",
      "<green>Click to claim your VIP bonus!"
    ]
  },
  "withCooldown": {
    "item": "minecraft:coal",
    "customModelData": 0,
    "displayname": "<dark_gray><bold>VIP Daily Bonus (Unavailable)</bold></dark_gray>",
    "lore": [
      "<gray>Requires VIP rank or on cooldown.",
      "",
      "<yellow>Cooldown: <red>%cooldown%",
      "<yellow>Permission: <light_purple>%permission%"
    ]
  },
  "rewards": {
    "id": "vip_rewards",
    "showMenu": true,
    "giveAll": true,
    "amountRewardsPermission": { "": 1 },
    "lootTable": {
      "": [
        {
          "chance": 100.0,
          "item": "minecraft:diamond_block",
          "minAmount": 2,
          "maxAmount": 4,
          "commands": [
            "eco give %player% 2500"
          ]
        }
      ]
    }
  }
}
```

---

## 💾 Database & Storage Engine

CobbleDailyRewards supports two data storage backends:
1. **JSON (`type: "JSON"`)**: File-based storage saved under `config/cobbledailyrewards/data/users/<uuid>.json`. Ideal for standalone servers.
2. **MongoDB (`type: "MONGODB"`)**: Centralized NoSQL database for bungee/velocity networks where players jump between multiple servers and share daily reward cooldowns.

---

## ❓ Troubleshooting & FAQ

### How do I reset a player's cooldown so they can test rewards?
Admins with permission can execute:
```bash
/dailyrewards restart <player>
```
To reset your own cooldown, simply run `/dailyrewards restart`.

### Can I change the GUI size?
Yes. Adjust `"rows"` in `config.json` (values from `1` to `6`). A 3-row GUI provides slots `0` through `26`.

### Why does a player not see their reduced VIP cooldown?
Ensure that the player possesses the exact LuckPerms permission specified in the `cooldowns` map (e.g. `group.vip`).
