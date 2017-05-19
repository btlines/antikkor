package antikkor.example.auth

/**
  * This is the model used by the clients
  * of the Auth service
  */
object Protocol {

  case class StartSession(user: Option[String])
  case class SessionStarted(user: Option[String], token: Option[Int])
  case class InvalidUser(user: Option[String])

  case class Verify(token: Option[Int])
  case class SessionVerified(token: Option[Int], user: Option[String])
  case class InvalidSession(token: Option[Int])

  case class EndSession(token: Option[Int])
  case class SessionEnded(token: Option[Int])

}
