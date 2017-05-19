// Needs to be in akka package in order to override aroundReceive
package akka

import akka.actor.Actor

/**
  * Translate messages before they are received by the actor
  */
trait AdapterActor extends Actor {

  protected def translate: PartialFunction[Any, Any]

  override protected[akka] def aroundReceive(receive: Receive, msg: Any): Unit = {
    val adaptedReceive =
      if (translate.isDefinedAt(msg)) translate andThen receive
      else receive
    super.aroundReceive(adaptedReceive, msg)
  }

}
