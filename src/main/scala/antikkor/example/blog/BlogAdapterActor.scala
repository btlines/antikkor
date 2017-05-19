package antikkor.example.blog

import java.time.Instant

import akka.AdapterActor
import cats.instances.option._
import fluent._

trait BlogAdapterActor extends AdapterActor {

  implicit def timestampToInstant(timestamp: Long): Instant = Instant.ofEpochMilli(timestamp)
  implicit def instantToTimestamp(instant: Instant): Long = instant.toEpochMilli

  override def translate: PartialFunction[Any, Any] = {
    case Protocol.AllPosts         => Model.AllPosts
    case message: Protocol.Post    => message.transformTo[Model.Publish]
    case message: Model.Published  => message.transformTo[Protocol.Published.type]
    case message: Model.Posts      => message.posts.map(_.transformTo[Protocol.Post])
  }

}
