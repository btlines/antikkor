package antikkor.example.auth

/**
  * Business model for the Authentication service
  * This is the model used by the AuthActor
  */
object Model {

  case class User(name: String)
  case class Token(id: Int)

  case class Authenticate(user: User)
  case class Verify(token: Token)
  case class Invalidate(token: Token)

  sealed trait Event
  case class Authenticated(user: User, token: Token) extends Event
  case class Verified(user: User, token: Token) extends Event
  case class Invalidated(user: User, token: Token) extends Event
  case class InvalidUser(user: User)
  case class InvalidToken(token: Token)

}
