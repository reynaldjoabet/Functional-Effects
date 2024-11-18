trait IOApp {

  def run: IO[Any]

  final def main(args: Array[String]): Unit = Runtime.run(run).get

}
