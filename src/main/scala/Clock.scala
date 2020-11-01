import java.util.{Timer, TimerTask}

import scala.concurrent.duration.Duration
object Clock {
  private val timer= new Timer("IO-Timer",true)
def sleep[A](duration: Duration):IO[Unit]={
  IO.async{ onComplete=>
    timer.schedule(new TimerTask {
      override def run(): Unit = onComplete(Right(()))
    },duration.toMillis)

  }
}
}
