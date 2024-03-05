lazy val failingTask = taskKey[Unit]("Failing task")

failingTask := {
    val selfReferencingException = new Exception("BOOM self")
    selfReferencingException.initCause(
        new Exception("BOOM cause", selfReferencingException)
    )
    throw selfReferencingException
}
