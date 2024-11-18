import java.util.concurrent.CompletableFuture

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.{Failure, Success, Try}

sealed trait IO[+A] {
  self =>

  import IO._

  def as[B](value: => B): IO[B] = map(_ => value)

  def map[B](f: A => B): IO[B] = flatMap(a => pure(f(a)))

  def flatMap[B](f: A => IO[B]): IO[B] = FlatMap(this, f)

  def flatten[B](implicit ev: A <:< IO[B]): IO[B] = flatMap(a => a)

  // def option:IO[Option[A]]= ???
  def handleErrorWith[B >: A](t: Throwable => IO[B]): IO[B] = HandleErrorWith(self, t)
  def fork: IO[Fiber[A]]                                    = IO.Fork(self)

  def unit: IO[Unit] = map(_ => ())

  def keepRight[B](that: IO[B]): IO[B] = self.flatMap(_ => that)
  // alias for keepRight
  def *>[B](that: IO[B]): IO[B]       = keepRight(that)
  def keepLeft[B](that: IO[B]): IO[A] = flatMap(that.as(_))
  def <*[B](that: IO[B]): IO[A]       = keepLeft(that)
  def zip[B](that: IO[B]): IO[(A, B)] = self.flatMap(a => that.map(b => (a, b)))

  // def attempt:IO[Either[Throwable,A]]=IO.Attempt(self)

  def forever: IO[A] = self.flatMap(_ => forever)
  // apply function recover in case of failure or map in case of success
  // def redeem[B](recover:Throwable=>B,map:A=>B):IO[B]=attempt.map(_.fold(recover,map))

  // def redeemWith[B](recover:Throwable=>IO[B],map:A=>IO[B]):IO[B]=attempt.flatMap(_.fold(recover,map))

}

object IO {

  def apply[A](a: => A): IO[A] = delay(a)
  // val  canceled: IO[Nothing] =IO.Canceled
  def unit: IO[Unit]         = pure(())
  def none[A]: IO[Option[A]] = pure(None)
  // used to lift an already computed value into IO
  def pure[A](a: => A): IO[A] = Pure(a)

//val never:Async[Nothing]=async(_=>())

  // used to suspend a side effect in IO
  def succeed[A](t: => A): IO[A] = delay(t)
  // used to suspend side effects that produce an IO in IO
  def suspend[A](t: => IO[A]): IO[A] = Suspend(() => t)
  def delay[A](t: => A): IO[A]       = Delay(() => t)
// constructs an IO that wraps this exception
  // def raiseError[A](throwable:Throwable):IO[A]=RaiseError(throwable)
  def raiseError(throwable: Throwable): IO[Nothing] = RaiseError(throwable)

  // constructs an IO from Either

  def fromEither2[A](either: Either[Throwable, A]): IO[A] = either match {
    case Left(value)  => raiseError(value)
    case Right(value) => pure(value)
  }

  def foreach[A, B](xs: Iterable[A])(f: A => IO[B]): IO[Iterable[B]] =
    xs.foldLeft(IO.succeed(Vector.empty[B]))((acc, curr) =>
      for {
        soFar <- acc
        x     <- f(curr)
      } yield soFar :+ x
    )

  def foreachPar[A, B](xs: Iterable[A])(f: A => IO[B]): IO[Iterable[B]] =
    foreach(xs)(x => f(x).fork).flatMap(fibers => foreach(fibers)(_.join))

  def fromEither[A](either: => Either[Throwable, A]): IO[A] = either.fold(raiseError(_), pure(_))

  // constructs an IO from Try
  /*
     def fromTry[A](t:Try[A]):IO[A]= t match {
         case Failure(exception) => raiseError(exception)
         case Success(value) => pure(value)
     }

   */
  def fromTry[A](t: => Try[A]): IO[A] = t.fold(raiseError(_), pure(_))

  // constructs an IO from Option

  def fromOption[A](opt: => Option[A])(exception: => Throwable): IO[A] = opt match {
    case Some(value) => pure(value)
    case None        => raiseError(exception)
  }

  def async[A](f: (Try[A] => Unit) => Unit): IO[A] = Async(f)
  // def cancellable[A](callback:(Either[Throwable,A]=>Unit)=>IO[Unit]):IO[A]= ???

  // System.exit()

  def never: IO[Nothing] = IO.async(_ => ())

  def fromFuture[A](future: => IO[Future[A]])(implicit ec: ExecutionContext): IO[A] = async[A] {
    cb =>
      val promise = Promise[A]()
      future.map { fut =>
        fut.onComplete {

          case Failure(exception) => cb(Failure(exception)); promise.failure(exception)
          case Success(value)     => cb(Success(value)); promise.success(value)
        }

      }
      promise.future.value

  }

  def fromCompletableFuture[A](f: => CompletableFuture[A]): IO[A] = {
    IO.async { callback =>
      f.whenComplete { (result: A, error: Throwable) =>
        if (error == null) callback(Success(result)) else callback(Failure(error))
      }

    }
  }

  final case class Pure[+A](a: A)                           extends IO[A]
  final case class Delay[+A](t: () => A)                    extends IO[A]
  final case class FlatMap[A, +B](io: IO[A], f: A => IO[B]) extends IO[B]

  final case class Suspend[+A](t: () => IO[A]) extends IO[A]
  // final case object   Canceled extends IO[Nothing]
  // final case class RaiseError[+A](e:Throwable) extends IO[A]
  // since A is covariant, we can write the following
  final case class RaiseError(e: Throwable)                                extends IO[Nothing]
  final case class Async[+A](f: (Try[A] => Unit) => Unit)                  extends IO[A]
  final case class Join[+A](fi: Fiber[A])                                  extends IO[A]
  final case class Fork[+A](tio: IO[A])                                    extends IO[Fiber[A]]
  final case class HandleErrorWith[+A](self: IO[A], f: Throwable => IO[A]) extends IO[A]

//final case class Attempt[A](io:IO[A]) extends IO[Either[Throwable,A]]
}

abstract class Fiber[+A] { self =>

  def join: IO[A] = IO.Join(self)
}
