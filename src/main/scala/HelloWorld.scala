
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}
object HelloWorld  {
  def main(args: Array[String]): Unit = println(Runtime.unsafeRunSync(num))

  val program: IO[String] =for{
    _ <-IO.delay(println("Good morning, what is your name?"))
    name <- IO.delay(io.StdIn.readLine())
    _ <- IO.delay(println(s"Welcome $name"))
  } yield name


// a program that never ends when started
  val foo=IO.delay(println(" I am running forever")).forever
  val io1: IO[Int] =(1 to 100).foldLeft(IO.pure(0))((b, _)=>
    b.flatMap(elem=>foo.map{p =>println(elem+1);elem+1}))

  val error: IO[Nothing] = IO.raiseError(new NoSuchElementException)


  val num=(IO.pure(12+12).as("Hello").map(_.toUpperCase)).forever

 val futureValue=IO.fromFuture( IO.delay(Future(67)))
val asyncValue: IO[Int] =IO.async{ callback=>
  import scala.concurrent.ExecutionContext.Implicits.global
  val futureValue=Future(1232)
  futureValue.onComplete {
    case Failure(exception) => callback(Left(exception))
    case Success(value) => callback(Right(value))
  }
}


  val zip=foo.zip(num)
  val fromeither: IO[String] =IO.fromEither(Right("This is from Either"))
  val fromtry: IO[String] =IO.fromTry(Success("This is from Try"))
  val keepRight: IO[String] =fromeither
    .keepRight(fromtry)
    //.forever


}
