import java.util.concurrent.ScheduledExecutorService

import scala.concurrent.duration.FiniteDuration

trait Timer {
  def sleep(delay: FiniteDuration, runnable: Runnable): Unit
}

object Timer {

  def fromScheduledExecutor(scheduler: ScheduledExecutorService): Timer = new Timer {
    override def sleep(delay: FiniteDuration, runnable: Runnable): Unit = {
      val future = scheduler.schedule(runnable, delay.length, delay.unit)
      future.cancel(false)
      ()
    }
  }

}
