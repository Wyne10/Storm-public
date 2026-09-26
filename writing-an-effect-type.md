---
description: >-
  Extend StormEffect, register it under a type key, and server admins can
  configure as many effects of your type as they like—event-driven, ticking, or
  both.
---

# Writing an effect type

An effect type is a class that extends `StormEffect`. You register it under a type key; from then on, every effect in the config whose `effect` is that key gets its own instance of your class, built from that effect's config section. Set up the dependency first, as in [Using it from your plugin](using-it-from-your-plugin.md#adding-the-dependency).

## A first effect type

This one cancels fall damage:

```java
public class NoFallEffect extends StormEffect {

    public NoFallEffect(ConfigurationSection config) {
        super(config);
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (event.getCause() != EntityDamageEvent.DamageCause.FALL) return;
        if (!(event.getEntity() instanceof Player player)) return;
        if (!isAffected(player)) return;
        event.setCancelled(true);
    }

}
```

Register it in your plugin's `onEnable`:

```java
@Override
public void onEnable() {
    StormApi.getEffectRegistry().register(NoFallEffect.class, "no-fall");
}
```

And server admins can use it like any built-in type:

```yaml
effect:
  feather:
    name: "<white>Feather Falling"
    effect: no-fall
```

## The rules

1. **Expose a public constructor that takes one `ConfigurationSection`.** Storm creates each instance through it by reflection, passing that effect's section. Read your settings there.
2. **Check `isAffected` in every event handler.** Storm registers each instance as a listener, and a listener receives every matching event on the server—not only those of players who have the effect. A handler that skips the check applies your effect to everyone.
3. **Register each type once.** Register in `onEnable` and nowhere else. Registering creates the instances right away; `/effects reload` re-creates them from the registered class on its own. Registering the same type again, on your own plugin's reload say, creates a second set of instances while the first keeps listening.
4. **Keep no state you can't lose.** Every `/effects reload` throws your instances away and creates new ones. Anything that must outlast a reload—per-player data, say—belongs somewhere else.

## What a type can use

`StormEffect` gives its subclasses:

| Method                        | Returns                                                                                  |
| ----------------------------- | ---------------------------------------------------------------------------------------- |
| `getConfig()`                 | This effect's config section, as passed to the constructor.                              |
| `getEffectInstanceKey()`      | This effect's key, such as `feather`.                                                    |
| `isAffected(player)`          | Whether the player has this effect right now. `false` for `null`.                        |
| `getRemainingMillis(player)`  | How long the player has left on this effect. `0` for `null`, or when they don't have it. |
| `getPlugin()` / `getLogger()` | The Storm plugin and its logger.                                                         |

## Reacting to the effect's lifecycle

To do something the moment an effect is applied, cleared or restored on join, listen to the [Storm events](using-it-from-your-plugin.md#events). Every instance of every type receives them, so compare the key first:

```java
@EventHandler(ignoreCancelled = true)
public void onApply(StormEffectApplyEvent event) {
    if (!event.getEffectInstance().key().equals(getEffectInstanceKey())) return;
    Player player = event.getPlayer().getPlayer();
    if (player == null) return; // Applied to an offline player.
    Bukkit.getScheduler().runTask(getPlugin(), () -> start(player));
}
```

`StormEffectApplyEvent` fires **before** the effect is stored: inside the handler, `isAffected` and `getRemainingMillis` still describe the old state, and another listener may yet cancel the event. That's why the example waits a tick—by then the effect is applied, and `getRemainingMillis` is the new duration.

For `StormEffectJoinEvent`, which carries a UUID and an effect key rather than a player and an instance, compare `event.getEffectInstanceKey()` and look the player up with `Bukkit.getPlayer(event.getPlayer())`. To undo your effect when the player leaves, listen to `PlayerQuitEvent` as usual.

## Ticking effects

An effect that works on a schedule rather than on events implements `TickableStormEffect` as well:

```java
public class RegenerationEffect extends StormEffect implements TickableStormEffect {

    private final double health;

    public RegenerationEffect(ConfigurationSection config) {
        super(config);
        this.health = config.getDouble("health", 1.0);
    }

    @Override
    public long getPeriodTicks() {
        return 100;
    }

    @Override
    public void tick(@NotNull Player player) {
        double max = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        player.setHealth(Math.min(max, player.getHealth() + health));
    }

}
```

`tick` runs on the main thread, once per period, for each online player who has the effect. Storm does that filtering itself, so `tick` doesn't call `isAffected`.

All effects of one type share a single repeating task, and the first one loaded sets its period. Return a constant from `getPeriodTicks`: a period read from each effect's config would be ignored for all but one of them.

A ticking type is still a listener, so it can have event handlers too—and those still need `isAffected`.

## When the type isn't registered yet

Storm loads its effects before your plugin enables. An effect whose type isn't registered yet still exists: it can be applied, and its placeholders work, but nothing happens until you register the type—at which point its instances are created straight away. If your plugin is missing entirely, `/effects reload` logs `No effect with a key '<type>' is registered` for each such effect.
