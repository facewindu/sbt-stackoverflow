Reproducer for issue TBD

The build contains a `failingTask` task, which throws a self-referencing exception

```
lazy val failingTask = taskKey[Unit]("Failing task")

failingTask := {
    val selfReferencingException = new Exception("BOOM self")
    selfReferencingException.initCause(
        new Exception("BOOM cause", selfReferencingException)
    )
    throw selfReferencingException
}
```

When executing `sbt failingTask`, the build will throw a `java.lang.StackOverflowError`
Looking at [the sbt code](https://github.com/sbt/sbt/blob/6c032b8283e5fc3d88256be10a0a8b1e851800c0/internal/util-logging/src/main/scala/sbt/internal/util/StackTrace.scala#L65), the `while loop` is protected against self-causation, but not against deeper recursion.

Java itself protects against this when printing a stacktrace with a identity set of already seen `Throwable`.
See `Throwable#printStackTrace`
```
// Guard against malicious overrides of Throwable.equals by
// using a Set with identity equality semantics.
Set<Throwable> dejaVu = Collections.newSetFromMap(new IdentityHashMap<Throwable, Boolean>());

...

if (dejaVu.contains(this)) {
  s.println(prefix + caption + "[CIRCULAR REFERENCE: " + this + "]"); // <---- This gets the code out of the recursion
}
```