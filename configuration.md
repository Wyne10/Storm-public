---
description: >-
  The language and logging settings, the purchase rule, the server name, and the
  database tables Storm keeps—the whole of plugins/Storm/config.yml.
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

storage:
  # Name of this server, used to tell this server's effect sessions from another's and stored with every history entry
  serverName: 'server'
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

## Storage

| Key          | What it does                                                                                                                                                                                                |
| ------------ | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `serverName` | Identifies this server. Stored with every history entry, and—when several servers share one database—used to tell this server's effect sessions from another's. Give each server on a network its own name. |

## Applying changes

Edit the file, then run [`/effects reload`](commands-and-permissions.md) or restart the server. A reload re-reads `config.yml`, the files in `effect/` and the language files, and re-creates every effect. Effects that players already have keep running—a reload doesn't touch their timers.

{% hint style="info" %}
`/effects reload` never waits on the database. The connection and the tables are prepared on Storm's own worker thread, and writes queued before the reload still run afterwards. A `/connection reload` needs no follow-up either: Storm picks the new connection up on its next operation.
{% endhint %}

## Database

With [ConnectionSource](https://wyne.gitbook.io/wyne-docs/connectionsource/) installed, Storm creates two tables in the shared database:

| Table                   | Contents                                                                                                                                                                                                                                                                                                                       |
| ----------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| `storm_effects`         | One row per player per effect they have: the full duration in `total_ms`, how much of it has been used in `consumed_ms`, and when it was applied in `timestamp`. `session_start` and `session_owner` name the server currently spending the effect's time, if any. A row is deleted when the effect is cleared or has run out. |
| `storm_effects_history` | One row every time an effect is applied or cleared: the player, the effect, the `action` (`APPLY` or `CLEAR`), `duration_ms`, `source` (`apply` or `purchase`, empty for a clear), the `server` it happened on, and `timestamp`. Storm never deletes these.                                                                    |

Without ConnectionSource, Storm logs a warning at startup and keeps effects in memory only: they work, but end when the player leaves or the server stops, and no history is written.

### How a soft timer is counted

An effect with `hard: false` only runs down while its player is online, so Storm accounts the time instead of writing a countdown back. Joining opens a **session** on the row; leaving closes it and adds the time spent online to `consumed_ms`. The time left is always `total_ms` minus `consumed_ms`.

That makes two things safe that a stored countdown couldn't be:

* **A crash.** A session the server never closed is closed and charged by the next server the player joins, so a timer can't quietly reset itself to its full duration.
* **Several servers on one database.** Joining a second server closes whatever session the first one left open, and a late write from the server the player already left is refused rather than counted twice. This is what `serverName` distinguishes—two servers sharing a name can't tell their sessions apart.

Effects with `hard: true` need none of it: their end time is fixed when they are applied, and their rows are never rewritten.

## Files the plugin writes

| Path                       | Contents                                                                                                                                                                                                                                                     |
| -------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| `plugins/Storm/config.yml` | Your config. Add `regenerate: true` to it and on the next startup Storm merges in the keys a new version added, keeps your values, and removes the flag again. The bundled config ships with the flag set, so the first startup adds the `commands` section. |
| `plugins/Storm/effect/`    | Optional: one file per effect. Created empty.                                                                                                                                                                                                                |
| `plugins/Storm/lang/`      | The language files.                                                                                                                                                                                                                                          |
| `plugins/Storm/defaults/`  | The generated defaults the merges work from. Don't edit them.                                                                                                                                                                                                |
| `plugins/Storm/backups/`   | A copy of `config.yml` from before each regeneration.                                                                                                                                                                                                        |
