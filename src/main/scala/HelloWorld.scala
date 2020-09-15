
object HelloWorld  {

  val program: IO[Unit] =for{
    _ <-IO.delay(println("Good morning, what is your name?"))
    name <- IO.delay(io.StdIn.readLine())
    _ <- IO.delay(println(s"Welcome $name"))
  } yield ()


  val foo=IO.delay(println("foo")).as(0)
  val io1: IO[Int] =(1 to 3).foldLeft(IO.pure(0))((b, _)=>
    b.flatMap(elem=>foo.map(_ =>elem+1)))
  //println(io1)
  val error: IO[Nothing] = IO.raiseError(new NoSuchElementException)
  //println(error)
  
  val num=(IO.pure(12+12).as("Hello").map(_.toUpperCase))
println(num)


  

}
