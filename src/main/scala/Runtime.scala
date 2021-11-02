import scala.concurrent.Promise
import scala.util.Failure
import scala.util.Success
import scala.util.Try
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.Executors
import IO._
import scala.concurrent.Future

object Runtime {
private def executor(a: =>Unit):Unit={
    Executors.newWorkStealingPool.submit(
        new Runnable{
            override def run():Unit= a
        } 
    )
}

  def run[A](t:IO[A])=unsafeRunToFuture(t).value.get

  def runAsync[A](io:IO[A])(cb:(Try[A]=>Unit))= new RuntimeFiber(io).register(cb).start()


  def unsafeRunToFuture[A](io:IO[A]): Future[A]={
      val promise= Promise[A]()
      runAsync(io)((promise.tryComplete _).asInstanceOf[Try[A]=>Unit])// we capture the callback here
      Await.ready(promise.future,Duration.Inf) 
      promise.future   

  }

final  class RuntimeFiber[A](io:IO[A]) extends Fiber[A]{ self=>
   type Callback[B]=Try[B]=>Unit 

   private  val joined: AtomicReference[Set[Callback[A]]]= new AtomicReference[Set[Callback[A]]](Set.empty)

   private val result: AtomicReference[Option[Try[A]]]= new AtomicReference[Option[Try[A]]](None)


   def register(cb:Callback[A]): RuntimeFiber[A]={
       joined.updateAndGet(_+cb)
       result.get().foreach(cb)
       self
   }

   def fiberDone(a:Try[Any])={
       result.set(Some(a.asInstanceOf[Try[A]]))
       joined.get.foreach(cb=>cb(a.asInstanceOf[Try[A]]))
      //joined.get.foreach(println) for better understanding 
      //println("Hello after fiber")
   }



   def start(): RuntimeFiber[A]={
       eval(io)(fiberDone)
       self
   }


   def eval[A](io:IO[A])(cb:(Try[A])=>Unit):Unit= executor{
       io match {
           case Pure(a) => cb(Success(a))
           case Suspend(t) => eval(t()){
             case Failure(exception) => cb(Failure(exception))
            case Success(value) =>cb(Success(value))
           }
           case Fork(tio) => cb(Success(new RuntimeFiber(tio).start()))
           case FlatMap(self:IO[Any], f) => eval(self){
               case Failure(exception) => cb(Failure(exception))
                case Success(value) => eval(f(value.asInstanceOf[A]))(cb)
           }
           case HandleErrorWith(self:IO[Any], f) =>
               eval(self){
                    case Success(value) => cb(Success(value.asInstanceOf[A]))
                    case Failure(exception) => eval(f(exception))(cb)
               }

           case RaiseError(e) =>cb(Failure(e))
           case Delay(b) =>cb(Success(b()))
           case Join(fi) => fi.asInstanceOf[RuntimeFiber[A]].register(cb)
           case Async(register) =>register(cb)
       }
    }
   }


}