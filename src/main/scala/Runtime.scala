import scala.concurrent.Promise
import scala.concurrent.duration.Duration


trait Runtime {
  def unsafeRunAsync[A](value: IO[A])(callback:Either[Throwable,A]=>Unit):Unit

 def unsafeRunSync[A] (value: IO[A]):Either[Throwable,A]

}
object  Runtime extends Runtime{
  import scala.concurrent.Await
  import scala.concurrent.ExecutionContext.Implicits.global
  import scala.concurrent.Future
  import IO._
def unsafeRunSync[A](value: IO[A]):Either[Throwable,A]={

   Await.ready(unsafeRunToFuture(value),Duration.Inf).value.get.toEither
 }

 private def unsafeRunToFuture[A](value: IO[A]):Future[A]={
    val promise= Promise[A]()
unsafeRunAsync(value)(_.fold(e=>promise.failure(e),promise.success))
    promise.future
 }
  def unsafeRunAsync[A](value: IO[A])(callback:Either[Throwable,A]=>Unit):Unit= runLoop(value)(callback)

  private[this] def runLoop[A](io:IO[A])(callback:Either[Throwable,A]=>Unit):Unit= io match {
    case Pure(a) =>println("Pure")
      callback(Right(a))

    case Delay(t) =>println(" Delay")
      callback(Right(t()))
    case FlatMap(iof, f1: (Any => IO[Any])) => println("Flatmap")
      iof match {
        case Pure(a) => runLoop(f1(a))(callback)
        case Delay(t) =>runLoop(f1(t()))(callback)
        case FlatMap(ioff, f: (Any => IO[Any])) =>
        runLoop(ioff.flatMap(a=>f(a) flatMap(f1)))(callback.asInstanceOf[Either[Throwable,Any]=>Unit])
        case Suspend(t) =>
        runLoop(t().flatMap(a=>f1(a)))(callback)
        case RaiseError(e) =>
        callback(Left(e))
        case Async(f) =>
        f(callback.asInstanceOf[Either[Throwable,Any]=>Unit])
       // case Attempt(io2) => io2.flatMap(a=>)

    }
    case Suspend(t) =>println("Suspend")
      runLoop(t())(callback)
    case RaiseError(e) =>println("RaiseError")
      callback(Left(e))
    case Async(f) =>println("Async")
      f(callback)
    //case Attempt(io) =>  runLoop(io)(callback.asInstanceOf[Either[Throwable,Any]=>Unit])

  }
  

}