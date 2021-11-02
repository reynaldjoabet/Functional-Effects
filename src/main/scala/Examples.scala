  import scala.concurrent.duration._
  import scala.util.Try
  import java.util.Timer
  import java.util.TimerTask
  import scala.util.Success
  import scala.io.StdIn._
  import scala.concurrent.Future
  import scala.concurrent.ExecutionContext
  import scala.util.Failure
  import  scala.io.Source.fromBytes
  import scala.util.Random
  import java.time.Instant

  object Examples extends IOApp {
    def sleep(duration:Duration)={
        IO.async{cb:(Try[Any]=>Unit)=>
      new Timer().schedule(
          new TimerTask{
              override def run():Unit=cb(Success(()))
          },duration.toMillis
      )
        }
    }

    val now=IO.delay(System.currentTimeMillis())

    val sleepExample = for {

          _ <- IO.delay(println(s"[${System.currentTimeMillis()} ] start"))
          _ <- sleep(9.seconds)
          _ <- IO.delay(println(s"[${System.currentTimeMillis()} ] after sleep"))
        } yield ()




    val hello = for {
         _ <- IO.delay(println("What is your name ?"))
         name <- IO.delay(readLine())
         _ <- IO.delay(println(s" Hello $name"))
       } yield ()



          trait API{
            def compute:Future[Int]= ???
          }

          def doSomething[A](a:API)(implicit ec:ExecutionContext)={
            IO.async{ cb:(Try[Int]=>Unit)=>
            a.compute.onComplete{
              case Success(value) => cb(Success(value))
                case Failure(exception) => cb(Failure(exception))
            }
            }
          }

           val sl=for {
             _ <- IO.delay(println(s"[${{System.currentTimeMillis()}}] running first effect on ${Thread.currentThread.getName}"))
             _ <- sleep(4.seconds)
             _ <- IO.delay(println(s"[${{System.currentTimeMillis()}}] running second effect on ${Thread.currentThread.getName}"))
           } yield ()



           val forkjoin=for {
               _      <- IO.delay(println("1"))
               fiber1 <- (sleep(2.seconds) *> IO.delay(println(("2")) )*> IO.delay(1)).fork
               _      <- IO.delay(println("3"))
               i   <- fiber1.join
               _      <- IO.delay(println((s"fiber1 done: $i")))
             } yield ()


    val foreachParExample={
    val numbers = 1 to 100
    val random = new Random()
    // sleep up to 1 second, and return the duration slept
    val sleepRandomTime = IO.delay(random.nextInt(1000).millis)
                         .flatMap(t => sleep(t) *> IO.pure(t))
    def program = {
      for {
        _ <- IO.delay(s"[${Instant.now}] foreach:")
        _ <- IO.foreach(numbers)(i => IO.delay(i.toString))
        _ <- IO.delay(s"[${Instant.now}] foreachPar:")
        _ <- IO.foreachPar(numbers)(i => 
                sleepRandomTime.flatMap(t => IO.delay(s"$i after $t")))
        _ <- IO.delay(s"[${Instant.now}] foreachPar done")
      } yield ()
    }
    program
}

val foreach=IO.foreach((1 to 1000).toList)(x=>IO.delay(println(x)).fork)

        



      val fail= IO.raiseError( new Exception).handleErrorWith(_=>IO.delay(12))
      val suc=IO.delay(12).handleErrorWith(_=>IO.delay(809)).fork
      val eff= IO.async{cb:(Try[Int]=>Unit)=>
           cb(Success(78))
            }
            eff.fork
              //println(suc)
      //println(fail.fork)



override def run: IO[Any] = foreach



  }


