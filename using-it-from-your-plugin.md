---
description: >-
  Compile against storm-api, check whether a player has an effect, apply and
  clear effects, and react to the events Storm fires along the way.
---

# Using it from your plugin

## Adding the dependency

The API artifact is published to Maven Central:

```kotlin
repositories {
    mavenCentral()
}

dependencies {
    compileOnly("io.github.wyne10:storm-api:1.2.0")
}
```

Keep it `compileOnly`: the API classes ship inside the Storm plugin jar at their real package names, so shading your own copy leaves you with two unrelated `StormEffect` classes and effects that never load.

Everything lives in `me.wyne.storm.api` and `me.wyne.storm.api.event`. The API is compiled against the Paper API, which your plugin needs anyway.

Then declare the plugin dependency in `plugin.yml`:

```yaml
depend: [Storm]
```

Use `softdepend` if your plugin can run without Storm, and check that it's enabled before touching the API. Either way the declaration matters: it makes the server enable Storm first, and the API is empty before that.

## Getting the API

Storm fills in `StormApi` during its own `onEnable`, and registers the same two objects with the Bukkit services manager:

```java
// Static access point.
StormEffectManager effects = StormApi.getEffectManager();
StormEffectRegistry registry = StormApi.getEffectRegistry();

// Bukkit services manager, registered at ServicePriority.Normal.
StormEffectManager effects = Bukkit.getServicesManager()
        .getRegistration(StormEffectManager.class)
        .getProvider();
```

| Type                  | What it's for                                                                                                |
| --------------------- | ------------------------------------------------------------------------------------------------------------ |
| `StormEffectManager`  | Which players have which effects: checking, applying and clearing.                                           |
| `StormEffectRegistry` | Which effects exist: looking up an effect by key, and [registering effect types](writing-an-effect-type.md). |

## Checking effects

```java
StormEffectManager effects = StormApi.getEffectManager();

if (effects.isAffected(player, "vip-day")) {
    long millisLeft = effects.getRemainingMillis(player, "vip-day");
    // ...
}
```

Both take an online `Player`. Storm only keeps the timers of online players in memory, so for anyone else—or `null`—they answer "not affected" and `0`.

To learn about the effect itself, look it up in the registry. You get back the `StormEffectInstance` loaded from config, or `null` if no effect has that key:

```java
StormEffectInstance instance = StormApi.getEffectRegistry().getEffectInstance("vip-day");
if (instance != null) {
    String type = instance.effectKey(); // "empty"
    String name = instance.name();      // "<light_purple>VIP Day"
    ConfigurationSection config = instance.config();
}
```

## Applying and clearing

```java
effects.setEffect(player, "vip-day", TimeUnit.DAYS.toMillis(1), EffectSource.APPLY);
effects.clearEffect(player, "vip-day");
```

These behave exactly like [`/effects apply` and `/effects clear`](commands-and-permissions.md#applying-and-clearing), and take an `OfflinePlayer`:

* `setEffect` **replaces** the time left rather than adding to it. To extend an effect, add `getRemainingMillis` to the duration yourself.
* `setEffect` throws `IllegalArgumentException` for a key no effect has. `clearEffect` ignores one.
* The `EffectSource`—`APPLY` or `PURCHASE`—is recorded in the history and passed on to listeners.
* Call both from the main thread. They fire Bukkit events, and the effect's type reacts to them.

## Events

| Event                   | Fired                                                                                                                                                         | Cancellable |
| ----------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------- | ----------- |
| `StormEffectApplyEvent` | Before an effect is applied, with the player, the `StormEffectInstance`, the duration in milliseconds, and the `EffectSource`.                                | Yes         |
| `StormEffectClearEvent` | After an effect is cleared, with the player and the `StormEffectInstance`.                                                                                    | No          |
| `StormEffectJoinEvent`  | When a player joins, once for every effect restored from the database, with the player's `UUID`, the effect's key, and when it expires as epoch milliseconds. | No          |

Cancelling `StormEffectApplyEvent` stops the effect from being applied, stored, or written to the history. If the effect was being bought through `/effects purchase`, Storm refunds the player—see [When a plugin blocks the effect](commands-and-permissions.md#when-a-plugin-blocks-the-effect)—so tell them why:

```java
@EventHandler(ignoreCancelled = true)
public void onApply(StormEffectApplyEvent event) {
    if (!event.getEffectInstance().key().equals("vip-day")) return;
    Player player = event.getPlayer().getPlayer();
    if (player != null && player.getWorld().getName().equals("event")) {
        event.setCancelled(true);
        player.sendMessage("VIP Day can't start during the event.");
    }
}
```

`getDurationMillis()` is the duration being applied, which will replace the time the player has left. The event fires before anything changes, so inside the handler `getRemainingMillis` still returns the old time left—compare the two to tell a fresh effect from one being cut short or extended:

```java
@EventHandler(ignoreCancelled = true)
public void onPurchase(StormEffectApplyEvent event) {
    if (event.getSource() != EffectSource.PURCHASE) return;
    long hours = TimeUnit.MILLISECONDS.toHours(event.getDurationMillis());
    getLogger().info(event.getPlayer().getName() + " bought " + event.getEffectInstance().key() + " for " + hours + "h");
}
```

A few things to know about the others:

* `StormEffectClearEvent` fires for every clear, whether or not the player had the effect. `/effects clear <player>` without an effect fires it once for every effect Storm knows.
* By the time `StormEffectJoinEvent` fires, `isAffected` already returns `true` for the restored effect.
* No event fires when an effect runs out. It simply stops counting as affected—check `isAffected` or `getRemainingMillis` when you need to know.
