package me.blackness.taskflow.task;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

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
public final class RepeatingSyncTask<T, U> implements Task<T> {
    private final int amount;
    private final long period;

    private final Function<T, U> task;
    private final Task<U> baseTask;

    private final ReentrantLock lock;

    private BukkitTask bukkitTask;

    public RepeatingSyncTask(int amount, long period, Function<T, U> task, Task<U> baseTask) {
        this.amount = amount - 1;
        this.period = period;

        this.task = task;
        this.baseTask = baseTask;

        lock = new ReentrantLock(true);
    }

    @Override
    public synchronized boolean execute(T t, Plugin plugin) {
        if (!plugin.isEnabled()) {
            return false;
        }

        lock.lock();

        final CompletableFuture<U> future = new CompletableFuture<>();

        bukkitTask = new BukkitRunnable(){
            private int index;

            @Override
            public void run() {
                if (index < amount) {
                    task.apply(t);
                    index++;
                } else {
                    future.complete(task.apply(t));
                    this.cancel();
                }
            }
        }.runTaskTimer(plugin, 0, period);

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
