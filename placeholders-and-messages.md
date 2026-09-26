---
description: >-
  The %effect_…% PlaceholderAPI placeholders, formatting the time left, and how
  to edit or translate the plugin's messages.
---

# Placeholders and messages

## Placeholders

With [PlaceholderAPI](https://github.com/PlaceholderAPI/PlaceholderAPI) installed, every effect provides placeholders of the form `%effect_<key>_<value>%`:

| Placeholder                       | Shows                                                                                                               |
| --------------------------------- | ------------------------------------------------------------------------------------------------------------------- |
| `%effect_<key>_name%`             | The effect's `name`.                                                                                                |
| `%effect_<key>_remaining%`        | The time the player has left, in ticks—20 per second. `0` when they don't have the effect.                          |
| `%effect_<key>_remaining-format%` | The language file's `format-effect-active` while the player has the effect, and `format-effect-inactive` otherwise. |

The `remaining` placeholders are only filled in for online players.

`name` also takes a suffix that picks the output format, for use in plugins that expect one in particular:

| Suffix       | Format                                                      | Example                           |
| ------------ | ----------------------------------------------------------- | --------------------------------- |
| `-legacy`    | Legacy `&` codes                                            | `%effect_vip-day_name-legacy%`    |
| `-parsed`    | Legacy `§` codes                                            | `%effect_vip-day_name-parsed%`    |
| `-mm`        | MiniMessage                                                 | `%effect_vip-day_name-mm%`        |
| `-plain`     | Plain text, formatting removed                              | `%effect_vip-day_name-plain%`     |
| `-plainText` | Plain text, through Adventure's newer plain-text serializer | `%effect_vip-day_name-plainText%` |
| `-gson`      | JSON text component                                         | `%effect_vip-day_name-gson%`      |

### Formatting the time left

`%effect_<key>_remaining%` is a raw tick count, which is right for other plugins to compute with and wrong for players to read. `%effect_<key>_remaining-format%` exists to fix that: it shows `format-effect-active` from the language file, with `<key>` replaced by the effect's key and PlaceholderAPI placeholders filled in, so the format can pass the tick count to a placeholder that formats durations.

The bundled `en.yml` does exactly that, and shows `01:23:00` for an hour and 23 minutes left:

```yaml
format-effect-active: "%dtf_duration_{effect_<key>_remaining}_HH:mm:ss%"
format-effect-inactive: "- - -"
```

`%dtf_…%` comes from [DateTimeFormatExpansion](https://wyne.gitbook.io/wyne-docs/datetimeformatexpansion/), a separate PlaceholderAPI expansion; install it, or point `format-effect-active` at the duration formatter your server has. Any [duration format](https://wyne.gitbook.io/wyne-docs/datetimeformatexpansion/durations#format) DateTimeFormatExpansion accepts works in place of `HH:mm:ss`—`WORDSLT` gives `1 hour 23 minutes`, for example.

## Messages

Every message the plugin sends is in `plugins/Storm/lang/`, one file per language. `en.yml` and `ru.yml` are bundled; `lang` in `config.yml` picks the default, and `usePlayerLanguage` answers each player in their client's language when a file for it exists. Messages are written in the format set by `serializer`—MiniMessage by default.

| Key                           | Sent when                                                         | Replacements       |
| ----------------------------- | ----------------------------------------------------------------- | ------------------ |
| `format-effect-active`        | Shown by `%effect_<key>_remaining-format%` while the effect runs. | `<key>`            |
| `format-effect-inactive`      | Shown by `%effect_<key>_remaining-format%` otherwise.             | `<key>`            |
| `error-effect-not-found`      | A command names an effect that doesn't exist.                     | `<key>`            |
| `error-player-not-found`      | A command names a player who has never joined.                    | `<name>`           |
| `error-currency-not-found`    | A purchase names a currency InfPoints doesn't have.               | `<currency>`       |
| `error-insufficient-funds`    | The player can't afford a purchase.                               | `<price>`, `<key>` |
| `error-effect-already-active` | The player already has an effect they're buying.                  | `<key>`            |
| `success-plugin-reload`       | `/effects reload` finished.                                       |                    |
| `success-effect-apply`        | `/effects apply` gave an effect.                                  | `<key>`            |
| `success-effect-clear`        | `/effects clear` took one effect away.                            | `<key>`            |
| `success-effects-clear`       | `/effects clear` took every effect away.                          |                    |
| `success-effect-purchase`     | A player bought one effect.                                       | `<key>`, `<price>` |
| `success-effects-purchase`    | A player bought several effects with `purchase-many`.             | `<price>`          |

`error-insufficient-funds` only gets `<key>` from `purchase`, not from `purchase-many`.

Messages can also use PlaceholderAPI placeholders, filled in for the player who receives the message. The bundled ones use `%effect_<key>_name%`, where `<key>` is replaced first, to show the effect's name.

### Adding a language

Copy `en.yml` to a new file in `lang/`—`de.yml`, say—and translate the values. Set `lang: 'de.yml'` in `config.yml` to make it the default, or leave `usePlayerLanguage` on to serve it to players whose client is in that language.

### Editing messages

Edit the file and run [`/effects reload`](commands-and-permissions.md). When a new version adds messages, they are merged into your files and the ones you edited are kept. Delete a file to get the current defaults back.
