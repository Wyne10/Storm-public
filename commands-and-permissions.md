---
description: >-
  The /effects command and its subcommands—apply, clear, purchase and reload—and
  the permissions that guard them.
---

# Commands and permissions

With [CommandAPI](https://docs.commandapi.dev/) installed, Storm registers `/effects`. Without it the command is missing; effects, placeholders and the API still work.

| Command                                                                     | Permission         | What it does                                             |
| --------------------------------------------------------------------------- | ------------------ | -------------------------------------------------------- |
| `/effects apply <player> <effect> <duration>`                               | `effects.apply`    | Gives the player the effect for the duration.            |
| `/effects clear <player> [effect]`                                          | `effects.clear`    | Takes the effect away—or every effect, if none is named. |
| `/effects purchase <player> <effect> <duration> <currency> <price>`         | `effects.purchase` | Charges the player, then gives them the effect.          |
| `/effects purchase-many <player> <duration> <currency> <price> <effect>...` | `effects.purchase` | Charges the player once for several effects.             |
| `/effects reload`                                                           | `effects.reload`   | Re-reads the config, the effects and the language files. |

`effects.*` grants all four permissions. Every permission defaults to operators.

## Durations

A duration is one or more amounts, each followed by a unit: `30m`, `1h30m`, `2d`.

| Unit | Meaning      |
| ---- | ------------ |
| `ms` | milliseconds |
| `s`  | seconds      |
| `m`  | minutes      |
| `h`  | hours        |
| `d`  | days         |
| `t`  | ticks        |

A number with no unit counts as ticks, so `60` is three seconds, not a minute.

## Applying and clearing

`/effects apply` gives the effect for the duration, replacing any time the player had left on it. It's recorded in the history with the source `apply`.

The player can be offline, as long as they've joined the server before. The effect is stored and starts when they next join—for a `hard` effect, the clock starts right away. Applying to an offline player needs ConnectionSource, since without it there's nowhere to store the effect.

`/effects clear` takes the effect away, from the database too. Without an effect name it clears every effect Storm knows.

## Purchases

The purchase commands take the price from an [InfPoints](https://wyne.gitbook.io/wyne-docs/infpoints/) currency, so they need InfPoints installed. They're meant to be run from the console by a menu, NPC or shop plugin:

```
effects purchase %player_name% vip-day 1d coins 500
effects purchase-many %player_name% 1h coins 1200 vip-day fly
```

`<currency>` is the key of an InfPoints point. The player must be online, and every message goes to them rather than to whoever ran the command. A purchase:

1. Checks that the effects, the currency and the duration are valid. A mistake in any of them charges nothing.
2. Refuses if the player already has the effect—with `purchase-many`, any of the effects—unless [`allowPurchaseOverride`](configuration.md#commands) is on. Nothing is charged.
3. Takes the price from the player's balance, or refuses if they can't afford it.
4. Gives the effect for the duration, recorded in the history with the source `purchase`.

`purchase-many` charges one price for every effect listed at the end, separated by spaces, and gives each of them the full duration.

### When a plugin blocks the effect

A plugin can stop an effect from being applied by cancelling [`StormEffectApplyEvent`](using-it-from-your-plugin.md#events). The price has been taken by then, so Storm gives it back:

* `purchase` refunds the whole price and sends no success message.
* `purchase-many` splits the price evenly between the effects and refunds the share of each one that was blocked. If every effect was blocked, the whole price comes back and no success message is sent.

Telling the player why is up to the plugin that cancelled the event.

## Reloading

`/effects reload` re-reads `config.yml`, the files in `effect/` and the language files, and re-creates every effect. Players keep the effects they have. See [Applying changes](configuration.md#applying-changes).
