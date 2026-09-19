---
description: >-
  Timed effects for your players—defined in config, granted by command or sold
  for an InfPoints currency, and given their behavior by effect types that
  plugins register.
---

# Storm

Storm is a Paper plugin for timed effects. You define each effect once in config, then give it to a player for a duration—by command, from a shop that charges them for it, or from another plugin through the API. Storm keeps track of the time left, restores it when the player comes back, and shows it in placeholders.

What an effect _does_ comes from its **type**. Storm ships two: one runs console commands, and one is a plain named timer. Everything else—an XP booster, double crop drops, a potion that lasts as long as the effect—is a type that a plugin registers through the API, and that server admins then configure like any other.

## What it gives you

* **Effects defined in config.** Each effect is a named, configured instance of an effect type: `vip-day` could be a plain timer that lasts a day. See [Defining effects](defining-effects.md).
* **Two built-in types.** `commands` runs console commands when the effect is applied; `empty` is a timer that other plugins and placeholders can check.
* **Timers that survive.** With [ConnectionSource](https://wyne.gitbook.io/wyne-docs/connectionsource/) installed, the time left is stored in the database and restored when the player joins. A timer runs only while the player is online—or, with `hard: true`, runs out at a fixed time whether they are or not.
* **Purchases.** `/effects purchase` takes the price from an [InfPoints](https://wyne.gitbook.io/wyne-docs/infpoints/) currency and gives the effect, so a menu or NPC plugin can sell effects with one console command. See [Commands and permissions](commands-and-permissions.md).
* **Placeholders.** The effect's name and the time left, for scoreboards, menus and holograms. See [Placeholders and messages](placeholders-and-messages.md).
* **An API.** Check, apply and clear effects from your plugin, and listen to the events Storm fires along the way—see [Using it from your plugin](using-it-from-your-plugin.md). Or add effect types of your own—see [Writing an effect type](writing-an-effect-type.md).

## Requirements

|        |                                        |
| ------ | -------------------------------------- |
| Server | Paper 1.16.5 or later, or a fork of it |
| Java   | 16 or later                            |

Storm uses Paper's API, so it doesn't run on plain Spigot or CraftBukkit.

Everything else is optional. Each plugin switches on a part of Storm:

| Plugin                                                                  | What it enables                                                                                                     |
| ----------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------- |
| [ConnectionSource](https://wyne.gitbook.io/wyne-docs/connectionsource/) | Storing effects and their history in a database. Without it, effects live in memory and end when the player leaves. |
| [CommandAPI](https://docs.commandapi.dev/)                              | The `/effects` command.                                                                                             |
| [InfPoints](https://wyne.gitbook.io/wyne-docs/infpoints/)               | `/effects purchase` and `/effects purchase-many`.                                                                   |
| [PlaceholderAPI](https://github.com/PlaceholderAPI/PlaceholderAPI)      | The `%effect_…%` placeholders, and PlaceholderAPI placeholders in messages and in `commands` effects.               |

## How it fits together

1. Storm enables, reads its effects from `config.yml` and the `effect/` folder, and registers its built-in types. Every effect whose type is registered is created.
2. Plugins that depend on Storm register their own types as they enable, and the effects of those types are created as each type arrives.
3. A command or a plugin applies an effect to a player for a duration. Storm fires `StormEffectApplyEvent`, stores the effect, and writes it to the history.
4. While the timer runs, the effect's type does its work: it reacts to events for players who have the effect, or ticks on a schedule.
5. When the player leaves, Storm saves the time left; when they join, it restores it. When the time runs out, the effect stops applying.

To compile the plugin yourself, see [Building from source](building-from-source.md).
