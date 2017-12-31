package me.blackness.taskflow.task;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.ReentrantLock;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import me.blackness.taskflow.Task;

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
public final class WaitingTask<T> implements Task<T> {
    private final long ticks;
    private final Task<T> baseTask;

    private final ReentrantLock lock;

    private BukkitTask bukkitTask;

    public WaitingTask(long ticks, Task<T> baseTask) {
        this.ticks = ticks;
        this.baseTask = baseTask;

        lock = new ReentrantLock(true);
    }

    @Override
    public synchronized boolean execute(T t, Plugin plugin) {
        if (!plugin.isEnabled()) {
            return false;
        }

        lock.lock();

        final CompletableFuture<T> future = new CompletableFuture<>();

        bukkitTask = new BukkitRunnable(){

            @Override
            public void run() {
                future.complete(t);
            }
        }.runTaskLaterAsynchronously(plugin, ticks);

        try {
            baseTask.execute(future.get(), plugin);
        } catch (Exception ex) {
            lock.unlock();
            return false;
        }

        lock.unlock();
        return true;
    }

    @Override
    public void cancel() {
        baseTask.cancel();

        if (bukkitTask != null && !bukkitTask.isCancelled()) {
            bukkitTask.cancel();
        }

        if (lock.isLocked()) {
            lock.unlock();
        }
    }
}
