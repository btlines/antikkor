package antikkor.example.blog

import antikkor.serialization.PBSerializable

object Persistence {
  sealed trait Event extends PBSerializable
  case class PostPublished(
    author: Option[String],
    date: Option[Long],
    message: Option[String]
  ) extends Event
}
