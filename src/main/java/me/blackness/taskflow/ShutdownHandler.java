package me.blackness.taskflow;

import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.IllegalPluginAccessException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.scheduler.BukkitWorker;

/*
       .                                                    .
    .$"                                    $o.      $o.  _o"
   .o$$o.    .o$o.    .o$o.    .o$o.   .o$$$$$  .o$$$$$ $$P `4$$$$P'   .o$o.
  .$$| $$$  $$' $$$  $$' $$$  $$' $$$ $$$| $$$ $$$| $$$ ($o $$$: $$$  $$' $$$
  """  """ """  """ """  """ """  """ """  """ """  """  "  """  """ """  """
.oOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOo.
  ooo_ ooo ooo. ... ooo. ... ooo.  .. `4ooo.  .`4ooo.   ooo.ooo. ooo ooo.  ..
  $$$"$$$$ $$$| ... $$$| ... $$$$$$ ..    "$$o     "$$o $$$|$$$| $$$ $$$|   .
  $$$| $$$ $$$|     $$$|     $$$|     $$$: $$$ $$$: $$$ $$$|$$$| $$$ $$$|
  $$$| $$$ $$$| $o. $$$| $o. $$$| $o. $$$| $$$ $$$| $$$ $$$|$$$| $$$ $$$| $.
  $$$| $$$ $$$| $$$ $$$| $$$ $$$| $$$ $$$| $$$ $$$| $$$ $$$|$$$| $$$ $$$| $o.
  $$$| $$$ $$$| $$$ $$$| $$$ $$$| $$$ $$$| $$$ $$$| $$$ $$$|$$$| $$$ $$$| $$$
  $$$| $$$  $$. $$$  $$. $$$  $$. $$$ $$$| $$$ $$$| $$$ $$$|$$$| $$$  $$. $$$
  $$$: $P'  `4$$$Ü'__`4$$$Ü'  `4$$$Ü' $$$$$P'  $$$$$P'  $$$|$$$: $P' __`4$$$Ü'
 _ _______/∖______/  ∖______/∖______________/|________ "$P' _______/  ∖_____ _
                                                        i"  personinblack
                                                        |
 */
public final class ShutdownHandler implements Listener {
    private final HashSet<Plugin> registeredPlugins;

    public ShutdownHandler() {
        registeredPlugins = new HashSet<>();
    }

    public void register(Plugin plugin) {
        registeredPlugins.add(plugin);

        for (RegisteredListener registeredListener :
                PluginDisableEvent.getHandlerList().getRegisteredListeners()) {

            if (registeredListener.getListener().equals(this)) {
                return;
            }
        }

        try {
            Bukkit.getPluginManager().registerEvents(this, plugin);
        } catch (IllegalPluginAccessException ex) {
            return;
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void shutdownEvent(PluginDisableEvent event) {
        for (Plugin plugin : registeredPlugins) {
            if (event.getPlugin().getName().equals(plugin.getName())) {
                for (BukkitWorker worker : Bukkit.getScheduler().getActiveWorkers()) {
                    if (worker.getOwner().getName().equals(plugin.getName())) {
                        Bukkit.getScheduler().cancelTask(worker.getTaskId());
                        worker.getThread().interrupt();
                    }
                }

                registeredPlugins.remove(plugin);
            }
        }
    }
}
