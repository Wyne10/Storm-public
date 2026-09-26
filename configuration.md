---
description: >-
  The language and logging settings, the purchase rule, and the database tables
  Storm keeps—the whole of plugins/Storm/config.yml.
---

# Configuration

Plugin-wide settings live in `plugins/Storm/config.yml`, which Storm generates on first startup. The same file can also hold your effects, under `effect:`—see [Defining effects](defining-effects.md).

```yaml
lang: 'en.yml'
usePlayerLanguage: true
# LEGACY/ENHANCED_LEGACY/MINI_MESSAGE
serializer: MINI_MESSAGE
# OFF/FATAL/ERROR/WARN/INFO/DEBUG/TRACE/ALL
logLevel: INFO

effect:
  vip-day:
    name: "<light_purple>VIP Day"
    effect: empty
    hard: true
  welcome-gift:
    name: "<gold>Welcome Gift"
    effect: commands
    commands:
      - 'give %player_name% minecraft:cake 1'

commands:
  # If true, allows purchasing effects even while they are still active
  allowPurchaseOverride: false
```

## General

| Key                 | What it does                                                                                                                         |
| ------------------- | ------------------------------------------------------------------------------------------------------------------------------------ |
| `lang`              | The language file under `lang/` used for messages. `en.yml` and `ru.yml` are bundled.                                                |
| `usePlayerLanguage` | Answer each player in their client's language when a matching file exists, falling back to `lang`.                                   |
| `serializer`        | How messages and effect names are written: `MINI_MESSAGE` (`<green>`), `LEGACY` (`&a`), or `ENHANCED_LEGACY` (`&a` plus hex colors). |
| `logLevel`          | Threshold for the plugin's own logging. `DEBUG` logs every effect as it loads.                                                       |
| `effect`            | Effects, keyed by name. See [Defining effects](defining-effects.md).                                                                 |

## Commands

| Key                     | What it does                                                                                                                                                                                                                            |
| ----------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `allowPurchaseOverride` | Let [`/effects purchase`](commands-and-permissions.md#purchases) sell an effect the player already has. The new duration replaces the time left rather than adding to it. When `false`, the purchase is refused and nothing is charged. |

## Applying changes

Edit the file, then run [`/effects reload`](commands-and-permissions.md) or restart the server. A reload re-reads `config.yml`, the files in `effect/` and the language files, and re-creates every effect. Effects that players already have keep running—a reload doesn't touch their timers.

{% hint style="warning" %}
After a `/connection reload`, run `/effects reload` as well. Storm takes its database connection from ConnectionSource when it loads, and keeps using the old, closed one until it reloads.
{% endhint %}

## Database

With [ConnectionSource](https://wyne.gitbook.io/wyne-docs/connectionsource/) installed, Storm creates two tables in the shared database:

| Table                   | Contents                                                                                                                                                                      |
| ----------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `storm_effects`         | One row per player per effect they have: the time left in `remaining_ms`, and when it was applied in `timestamp`. A row is deleted when the effect is cleared or has run out. |
| `storm_effects_history` | One row every time an effect is applied: the player, the effect, `duration_ms`, `source` (`apply` or `purchase`), and `timestamp`. Storm never deletes these.                 |

Without ConnectionSource, Storm logs a warning at startup and keeps effects in memory only: they work, but end when the player leaves or the server stops, and no history is written.

## Files the plugin writes

| Path                       | Contents                                                                                                                                                                                                                                                     |
| -------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| `plugins/Storm/config.yml` | Your config. Add `regenerate: true` to it and on the next startup Storm merges in the keys a new version added, keeps your values, and removes the flag again. The bundled config ships with the flag set, so the first startup adds the `commands` section. |
| `plugins/Storm/effect/`    | Optional: one file per effect. Created empty.                                                                                                                                                                                                                |
| `plugins/Storm/lang/`      | The language files.                                                                                                                                                                                                                                          |
| `plugins/Storm/defaults/`  | The generated defaults the merges work from. Don't edit them.                                                                                                                                                                                                |
| `plugins/Storm/backups/`   | A copy of `config.yml` from before each regeneration.                                                                                                                                                                                                        |
