package antikkor.example.auth

import antikkor.serialization.PBSerializable

/**
  * This is the model used for persisting events
  */
object Persistence {
  case class User(name: Option[String])
  sealed trait Event extends PBSerializable
  case class Authenticated(token: Option[Int], user: Option[User]) extends Event
  case class Terminated(token: Option[Int], user: Option[User]) extends Event
}
