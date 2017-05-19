package antikkor.example.auth

import akka.persistence.journal.{EventAdapter, EventSeq}
import cats.instances.option._
import fluent._

class AuthEventAdapter extends EventAdapter {
  override def manifest(event: Any): String = event.getClass.getName
  override def fromJournal(event: Any, manifest: String): EventSeq = event match {
    case e: Persistence.Authenticated => EventSeq(e.transformTo[Model.Authenticated])
    case e: Persistence.Terminated    => EventSeq(e.transformTo[Model.Invalidated])
  }

  override def toJournal(event: Any): Any = event match {
    case e: Model.Authenticated => e.transformTo[Persistence.Authenticated]
    case e: Model.Invalidated   => e.transformTo[Persistence.Terminated]
  }
}
