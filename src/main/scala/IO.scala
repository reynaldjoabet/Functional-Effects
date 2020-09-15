
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

sealed trait IO[+A]{ self=>
    import IO._
    def as[B](value: =>B):IO[B]= map(_=>value)
  def map[B](f:A=>B):IO[B]=  flatMap(a=>pure(f(a)))
  
  def flatMap[B](f:A=>IO[B]):IO[B]=FlatMap(this,f)
 def flatten[B](implicit ev : A<:< IO[B]):IO[B]=flatMap(a=>a)
 //def option:IO[Option[A]]= ???
// def handleErrorWith[A](t: Throwable=>IO[A]):IO[A]=HandleErrorWith(self,t)
 def void :IO[Unit]=map(_=>())

 def keepRight[B](that:IO[B]):IO[B]=flatMap(_ =>that)

  def keepLeft[B](that:IO[B]):IO[A]=flatMap(that.as(_))

 def zip[B](that:IO[B]):IO[(A,B)]=self.flatMap(a=>that.map(b=>(a,b)))
 //zips both IOs and discards the value of the first
 def forever:IO[A]=self.flatMap(_ =>forever)

}

  object  IO{
    //val  canceled: IO[Nothing] =IO.Canceled
      def unit:IO[Unit]=pure(())
      def none[A]:IO[Option[A]]=pure(None)
      //used to lift an already computed value into IO
    def pure[A](a: =>A):IO[A]=  Pure(a)

val never:Async[Nothing]=async(_=>())

    //used to suspend a side effect in IO
    def succeed[A]( t : =>A):IO[A]= delay(t)
    // used to suspend side effects that produce an IO in IO
    def suspend[A](t: => IO[A]):IO[A]=Suspend(()=>t)
 def delay[A](t: =>A):IO[A]=Delay(()=>t)
// constructs an IO that wraps this exception
   // def raiseError[A](throwable:Throwable):IO[A]=RaiseError(throwable)
   def raiseError(throwable:Throwable):IO[Nothing]=RaiseError(throwable)
    // constructs an IO from Either
    /*
    def fromEither[A](either: Either[Throwable,A]):IO[A]= either match {
        case Left(value) => raiseError(value)
        case Right(value) => pure(value)
    }

     */
    def async[A](f:(Either[Throwable,A]=>Unit)=>Unit):IO[A]= Async(f)

    def fromEither[A](either: Either[Throwable,A]):IO[A]=either.fold(raiseError(_),pure(_))

     // constructs an IO from Try
    /*
     def fromTry[A](t:Try[A]):IO[A]= t match {
         case Failure(exception) => raiseError(exception)
         case Success(value) => pure(value)
     }

     */
    def fromTry[A](t:Try[A]):IO[A]= t.fold(raiseError(_),pure(_))

      // constructs an IO from Option

      def fromOption[A](opt:Option[A])(exception: =>Throwable):IO[A]= opt match {
          case Some(value) => pure(value)
          case None => raiseError(exception)
      }

def never: IO[Nothing] =IO.async(_=>())

    def fromFuture[A](future : => Future[A])(implicit ec:ExecutionContext) = async[A]{ cb=>
      future.onComplete {
        case Failure(exception) => cb(Left(exception))
        case Success(value) =>cb(Right(value))
      }

    }
    final case class Pure[+A](a: A) extends IO[A]
    final case class Delay[+A](t: ()=>A) extends IO[A]
    final case class FlatMap[A,+B](io:IO[A],f:A=>IO[B]) extends IO[B]

    final case class Suspend[+A](t: ()=>IO[A]) extends IO[A]
 //final case object   Canceled extends IO[Nothing]
   // final case class RaiseError[+A](e:Throwable) extends IO[A]
   // since A is covariant, we can write the following
   final case class RaiseError(e:Throwable) extends IO[Nothing]
    final case class Async[+A](f:(Either[Throwable,A]=>Unit)=>Unit) extends IO[A]
//final case class HandleErrorWith[A](io:IO[A],f:Throwable=>IO[A]) extends IO[A]









  }