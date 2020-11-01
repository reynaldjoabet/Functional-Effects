
/*
trait Timer {
def sleep(delay:FiniteDuration,runnable: Runnable):Runnable
}
object  Timer {
  def fromScheduledExecutor(scheduler:ScheduledExecutorService):Timer= new Timer {
    override def sleep(delay: FiniteDuration, runnable: Runnable): Runnable = {
      val future = scheduler.schedule(runnable,delay.length,delay.unit)

      future.cancel(false)
      ()
    }
  }
}

 */
