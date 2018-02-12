package me.blackness.taskflow.taskflow;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

import me.blackness.taskflow.ShutdownHandler;
import me.blackness.taskflow.Task;
import me.blackness.taskflow.TaskFlow;

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
public final class BasicTaskFlow<T> implements TaskFlow<T> {
    private static final ShutdownHandler shutdownHandler = new ShutdownHandler();

    private final String name;
    private final Plugin plugin;

    private final Queue<Task<T>> tasksQueue;
    private final Queue<T> tsQueue;

    private BukkitTask bukkitTask;

    private final List<Task<T>> startedTasks;

    private CountDownLatch finishSignal;

    public BasicTaskFlow(String name, Plugin plugin) {
        this.name = name;
        this.plugin = plugin;

        tasksQueue = new LinkedBlockingQueue<>();
        tsQueue = new LinkedBlockingQueue<>();
        shutdownHandler.register(plugin);

        startedTasks = new ArrayList<>();

        finishSignal = new CountDownLatch(1);
    }

    protected CountDownLatch finishSignal() {
        return finishSignal;
    }

    public void start(T t, Task<T> task) {
        if (!plugin.isEnabled()) {
            return;
        }

        tasksQueue.add(task);
        tsQueue.add(t);

        if (!isRunning()) {
            bukkitTask = Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                while (!tsQueue.isEmpty()) {
                    final Task<T> currentTask;
                    (currentTask = tasksQueue.poll()).execute(tsQueue.poll(), plugin);
                    startedTasks.add(currentTask);
                }

                finishSignal.countDown();
                finishSignal = new CountDownLatch(1);
            });
        }
    }

    public void stop() {
        for (Task<T> task : startedTasks) {
            task.cancel();
        }

        if (isRunning()) {
            bukkitTask.cancel();

            if (!isRunning() && finishSignal != null) {
                finishSignal.countDown();
                startedTasks.clear();
            }
        }
    }

    @Override
    public boolean isRunning() {
        if (bukkitTask == null) {
            return false;
        }

        for (Task<T> task : startedTasks) {
            if (task.isRunning()) {
                return true;
            }
        }

        final BukkitScheduler scheduler = Bukkit.getScheduler();
        final int taskId = bukkitTask.getTaskId();

        return scheduler.isCurrentlyRunning(taskId) || scheduler.isQueued(taskId);
    }

    public boolean equals(String name, String pluginName) {
        return this.name.equals(name) && this.plugin.getName().equals(pluginName);
    }
}
