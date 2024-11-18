# Functional-Effects

Side effects are considered /impure/ in a functional programming sense, since they break the rules of totality and pureness: A pure function is one that takes an input and returns an output and will do that for all inputs deterministically. A total function has all is one that strictly maps one set of inputs onto a second set of outputs. A function with side effects is neither pure nor total since it is all foremost non deterministic in its behaviour.

`Functional effects are immutable data structures that describe side effects.`

The `interpreter` is in its simplest case a `run` function that runs over the immutable data structure and translates every described operation into an actual side effect — outside of the program we have written. This keeps the programmer in a purely functional mind set for the entirety of the software development process.

The `run` function takes the function that describes the side effect and evaluates it at the appropriate time.

## The Business Values - Why do we even bother?
- Sandboxing

To describe effects throughout our software allows us to sandbox the real world manipulation. That means we get composable time boxing and cancellation (for free).

- Composability

With functional effects it is possible to write functions that take effects as input and return effects as values. Those functions are composable and so effects can be transformed before they interact with the real world. Since functional effects behave just like values we can do all the functional transformation on effects that we can do on values. That includes somewhat easy concurrency and parallelism.

- Type Reasoning
Since it’s all pure functional programming we can easily reasoning about the type transformations along the program flow. That is not a direct benefit of functional effects, but of pure functional programming that is enabled through pushing the side effects into the interpreter of your program (the very edge of your software).

- Testability

Another benefit of pure functional programming is the very good testability of the entire code base. Since all functions are pure and total, unit and integration tests are building ups confidence in the code quite easily. And the code base is ready for property based testing as well.

[functional-effects](https://sandstorm.de/blog/posts/functional-effects)
## What are effects?
I defined pure functions as functions without any observable side effects. An effect is anything that can be observed from outside the program. The role of a function is to return a value, and a side effect is anything, besides the returned value, that’s observable from the outside of the function. It’s called a side effect because it comes in addition to the value that’s returned. An effect (without “side”) is like a side effect, but it’s the main (and generally unique) role of a program. Functional programming is about writing programs with pure functions (with no side effects) and pure effects in a functional way.

 Note that “observable” doesn’t always mean observable by a human operator. Often the result is observable by another program, which might then translate this effect into something observable by a human operator, either in synchronous or asynchronous form. Printing to the computer screen can be seen by the operator. Writing to a database, on the other hand, might not always be directly visible to a human user.

 In computer science, functional programming is a programming paradigm where programs are constructed by applying and composing functions. It is a declarative programming paradigm in which function definitions are trees of expressions that map values to other values, rather than a sequence of imperative statements which update the running state of the program.

In functional programming, functions are treated as first-class citizens, meaning that they can be bound to names (including local identifiers), passed as arguments, and returned from other functions, just as any other data type can. This allows programs to be written in a declarative and composable style, where small functions are combined in a modular manner.
## IO
A simple implementation of IO in Scala
IO is a data structure used to capture effects.Using various operators, we can transform side effecting code into pure descriptions 
This IO has operators to fork and join fibers

## ZIO
```scala
/*
An implementation of Fiber that maintains context necessary for evaluation.
*/
// it has a stack, whether interruptible or not,what callbacks are waiting on it, whether it has a result or not
final class FiberContext[E, A]()

// a fiber could have multiple callbacks

 //Accessed from multiple threads:
private val state = new AtomicReference[FiberState[E, A]](FiberState.initial)

 private[this] val stack             = Stack[Any => IO[Any, Any]]()
```  

`mkdir -p src/main/scala src/test/scala`
This command will create the entire path `src/main/scala` even if `src` and `src/main` don’t already exist. Without `-p`, mkdir would return an error if any part of the specified path is missing.



` case class Async[A](register: (A => Any) => Any) extends ZIO[Any, Nothing, A]`

`register` is a function A=>B, where `A` is a callback `(A=>Any)`. The `register` function takes a callback function `(A => Any)` as a parameter.

The `runloop` is a fiber

A fiber is an in-flight computation but can be interrupted etc