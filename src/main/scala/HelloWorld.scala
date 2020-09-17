import scala.util.Success

object HelloWorld  {
  def main(args: Array[String]): Unit = Runtime.unsafeRunAsync(num)(_ =>())

  val program: IO[Unit] =for{
    _ <-IO.delay(println("Good morning, what is your name?"))
    name <- IO.delay(io.StdIn.readLine())
    _ <- IO.delay(println(s"Welcome $name"))
  } yield ()


// a program that never ends when started
  val foo=IO.delay(println("foo")).forever
  val io1: IO[Int] =(1 to 100).foldLeft(IO.pure(0))((b, _)=>
    b.flatMap(elem=>foo.map{p =>println(elem+1);elem+1}))

  val error: IO[Nothing] = IO.raiseError(new NoSuchElementException)


  val num=(IO.pure(12+12).as("Hello").map(_.toUpperCase)).forever

  val zip=foo.zip(num)
  val fromeither: IO[String] =IO.fromEither(Right("This is from Either"))
  val fromtry: IO[String] =IO.fromTry(Success("This is from Try"))
  val keepRight=fromeither.keepRight(fromtry)


}
