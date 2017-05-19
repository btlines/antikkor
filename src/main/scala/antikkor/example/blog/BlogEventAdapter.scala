package antikkor.example.blog

import java.time.Instant

import akka.persistence.journal.{EventAdapter, EventSeq}
import cats.instances.list._
import cats.instances.option._
import fluent._

class BlogEventAdapter extends EventAdapter {

  implicit def timestampToInstant(timestamp: Long): Instant = Instant.ofEpochMilli(timestamp)
  implicit def instantToTimestamp(instant: Instant): Long = instant.toEpochMilli

  override def manifest(event: Any): String = event.getClass.getName

  override def toJournal(event: Any): Any = event match {
    case e: Model.Published => e.post.transformTo[Persistence.PostPublished]
  }

  override def fromJournal(event: Any, manifest: String): EventSeq = event match {
    case e: Persistence.PostPublished => EventSeq(e.transformTo[Model.Published])
  }
}
