package antikkor.example.auth

import akka.AdapterActor
import cats.instances.option._
import fluent._

trait AuthAdapterActor extends AdapterActor {

  override protected def translate: PartialFunction[Any, Any] = {
    case message: Protocol.StartSession => message.transformTo[Model.Authenticate]
    case message: Protocol.Verify       => message.transformTo[Model.Verify]
    case message: Protocol.EndSession   => message.transformTo[Model.Invalidate]

    case message: Model.Authenticated   => message.transformTo[Protocol.SessionStarted]
    case message: Model.Verified        => message.transformTo[Protocol.SessionVerified]
    case message: Model.Invalidated     => message.transformTo[Protocol.SessionEnded]
    case message: Model.InvalidUser     => message.transformTo[Protocol.InvalidUser]
    case message: Model.InvalidToken    => message.transformTo[Protocol.InvalidSession]
  }

}
