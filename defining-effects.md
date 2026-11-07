---
description: >-
  One entry per effect—which type it uses, what it's called, whether its timer
  runs while the player is offline—and the options of the built-in types.
---

# Defining effects

An **effect** is a named, configured instance of an **effect type**. The type is code—`commands` runs console commands—and the effect is what you give players: `welcome-gift`, a `commands` effect that hands out a cake, called "Welcome Gift". One type can back any number of effects, each with its own settings.

The effect's key—`welcome-gift` here—is how commands, placeholders and the API refer to it.

## Where effects go

Put effects in `config.yml`, under `effect:`:

```yaml
effect:
  welcome-gift:
    name: "<gold>Welcome Gift"
    effect: commands
    commands:
      - 'give %player_name% minecraft:cake 1'
```

Or give each one its own file in `plugins/Storm/effect/`, named after its key. The file holds the same section, key included:

```yaml
# plugins/Storm/effect/welcome-gift.yml
welcome-gift:
  name: "<gold>Welcome Gift"
  effect: commands
  commands:
    - 'give %player_name% minecraft:cake 1'
```

The file name without `.yml` must match the section inside it; otherwise the effect fails to load, with an error in the console. When a file and `config.yml` both define the same key, the file wins.

## Options every effect takes

| Key      | What it does                                                                                                                                                                                                            |
| -------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `effect` | Required. The effect type: one of the [built-in types](defining-effects.md#built-in-types), or a [type another plugin registered](defining-effects.md#types-from-other-plugins).                                        |
| `name`   | The display name, written in the format set by `serializer`. Shown by `%effect_<key>_name%` and in messages. Defaults to the key.                                                                                       |
| `hard`   | `false` by default: the timer only runs while the player is online, and pauses when they leave. With `true`, the effect ends at a fixed time—when it was applied plus its duration—whether the player is online or not. |

Applying an effect a player already has starts it over with the new duration. It doesn't add to the time left.

A soft timer is counted rather than stored as a countdown: Storm records how much of the duration a player has spent online, so the time left survives a crash and stays correct when several servers share one database. See [How a soft timer is counted](configuration.md#how-a-soft-timer-is-counted).

{% hint style="info" %}
Timers only survive the player leaving when [ConnectionSource](configuration.md#database) is installed. Without it, every effect ends when the player leaves, `hard` or not.
{% endhint %}

## Built-in types

### `commands`

Console commands, run when the effect is applied.

| Key        | Default | What it does                                                                                                                                                                                                 |
| ---------- | ------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| `commands` |         | Commands without the leading `/`, run from the console each time the effect is applied—by `/effects apply`, by a purchase, or by a plugin. Nothing runs when the effect is restored on join or when it ends. |

With PlaceholderAPI installed, placeholders in the commands are filled in for the player, so `%player_name%` works. Pair it with a plugin that has its own timers—for example a LuckPerms permission that expires with the effect:

```yaml
effect:
  fly:
    name: "<aqua>Flight"
    effect: commands
    hard: true
    commands:
      - 'lp user %player_name% permission settemp essentials.fly true 1h'
```

### `empty`

Does nothing on its own. Use it as a timer with a name: something another plugin checks [through the API](using-it-from-your-plugin.md), or a scoreboard shows through [placeholders](placeholders-and-messages.md).

```yaml
effect:
  vip-day:
    name: "<light_purple>VIP Day"
    effect: empty
    hard: true
```

## Types from other plugins

Any other behavior comes from types that plugins register—see [Writing an effect type](writing-an-effect-type.md). Such a type is configured like a built-in one: `effect` names the type key the plugin registered, and the rest of the section holds whatever options that type reads.

```yaml
effect:
  xp-boost:
    name: "<green>XP Booster"
    effect: hunter      # Registered by another plugin
    multiplier: 1.5     # Read by that type
```

An effect whose type hasn't been registered yet still loads: commands and placeholders work with it, and it can be applied, but it does nothing until the plugin that provides the type registers it.
