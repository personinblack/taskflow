package me.blackness.taskflow.taskflow;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.plugin.Plugin;

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
public final class LinkedTaskFlow<T> implements TaskFlow<T> {
    private static final List<BasicTaskFlow<?>> taskFlows = new ArrayList<>();

    private final BasicTaskFlow<T> taskFlow;

    @SuppressWarnings("unchecked")
    public LinkedTaskFlow(String name, Plugin plugin) {
        for (BasicTaskFlow<?> loopFlow : taskFlows) {
            if (loopFlow.equals(name, plugin.getName())) {
                taskFlow = (BasicTaskFlow<T>) loopFlow;
                return;
            }
        }

        taskFlows.add((taskFlow = new BasicTaskFlow<>(name, plugin)));
    }

    public void start(T t, Task<T> task) {
        taskFlow.start(t, task);
    }

    public void stop() {
        taskFlow.stop();
    }

    public void destroy() {
        stop();
        taskFlows.remove(taskFlow);
    }

    @Override
    public boolean isRunning() {
        return taskFlow.isRunning();
    }
}
