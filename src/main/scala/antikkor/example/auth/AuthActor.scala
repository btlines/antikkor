package antikkor.example.auth

import akka.persistence.PersistentActor
import Model._

class AuthActor extends PersistentActor with AuthAdapterActor {
  override val persistenceId: String        = "AuthActor"
  private  var authenticator: Authenticator = Authenticator()

  override def receiveCommand: Receive = {
    case Authenticate(user: User) => authenticate(user)
    case Verify(token) => verify(token)
    case Invalidate(token) => invalidate(token)
  }

  private def authenticate(user: User): Unit =
    if (authenticator.isValidUser(user)) {
      persist(Authenticated(user, authenticator.nextToken)) { event =>
        updateState(event)
        sender() ! translate(event)
      }
    } else {
      sender() ! translate(InvalidUser(user))
    }

  private def verify(token: Token): Unit =
    authenticator.validateToken(token) match {
      case None => sender() ! translate(InvalidToken(token))
      case Some(user) => sender() ! translate(Verified(user, token))
    }

  private def invalidate(token: Token): Unit =
    authenticator.validateToken(token) match {
      case None => sender() ! translate(InvalidToken(token))
      case Some(user) => persist(Invalidated(user, token)) { event =>
        updateState(event)
        sender() ! translate(event)
      }
    }

  private def updateState(event: Event): Unit = event match {
    case Authenticated(user, token) => authenticator = authenticator.signIn(user, token)
    case Verified(user, token) => ()
    case Invalidated(user, token) => authenticator = authenticator.signOut(token)
  }

  override def receiveRecover: Receive = {
    case event: Event => updateState(event)
  }
}
