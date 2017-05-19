package pbakka

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.pattern.ask
import akka.persistence.PersistentActor
import akka.testkit.{DefaultTimeout, ImplicitSender, TestKit}
import org.scalatest.{FlatSpecLike, Matchers}
import com.typesafe.config.ConfigFactory

class DomainActor extends PersistentActor {

  import DomainModel._

  var nextId: Int = 1
  var messages: List[DomainModel.Post] = Nil
  var sessions: Map[Int, DomainModel.Session] = Map.empty

  override val persistenceId: String = getClass.getSimpleName

  override def receiveRecover: Receive = {
    case event: DomainModel.Event =>
      println(s"Recovering event: $event")
      updateState(event)
  }

  override def receiveCommand: Receive = {
    case Restart => throw new RuntimeException("Restart me!")
    case GetMessages => sender() ! messages
    case event: StartSession =>
      sessions.values.find(_.user == event.user) match {
        case Some(existingSession) => sender() ! SessionStarted(existingSession)
        case None =>
          val session = DomainModel.Session(nextId, event.user)
          persist(SessionStarted(session)) { event =>
            updateState(event)
            sender() ! event
          }
      }
    case post: DomainModel.Post =>
      sessions.get(post.session.id) match {
        case None => ()
        case Some(session) =>
          persist(PostPublished(session, post.message)) { event =>
          updateState(event)
          sender() ! event
        }
      }
    case event: DomainModel.EndSession =>
      sessions.get(event.session.id) match {
        case None => sender() ! SessionEnded(event.session)
        case Some(existingSession) =>
          persist(SessionEnded(existingSession)) { event =>
            updateState(event)
            sender() ! event
          }
      }
  }

  def updateState(event: DomainModel.Event): Unit = event match {
    case DomainModel.SessionStarted(session) => nextId = session.id + 1; sessions += (session.id -> session)
    case DomainModel.PostPublished(session, message) => messages ::= Post(session, message)
    case DomainModel.SessionEnded(session) => sessions -= session.id
  }
}

class ProxyActor(domainActor: ActorRef, clientActor: ActorRef) extends Actor {
  import fluent._
  import cats.instances.list._
  import cats.instances.option._
  override def receive: Receive = {
    case command: ServiceProtocol.Join  => domainActor ! command.transformTo[DomainModel.StartSession]
    case command: ServiceProtocol.Leave => domainActor ! command.transformTo[DomainModel.EndSession]
    case command: ServiceProtocol.Post  => domainActor ! command.transformTo[DomainModel.Post]
    case DomainModel.GetMessages => domainActor ! DomainModel.GetMessages
    case DomainModel.Restart     => domainActor ! DomainModel.Restart

    case event: DomainModel.SessionStarted => clientActor ! event.transformTo[ServiceProtocol.Joined]
    case event: DomainModel.SessionEnded   => clientActor ! event.transformTo[ServiceProtocol.Left]
    case event: DomainModel.PostPublished  => clientActor ! event.transformTo[ServiceProtocol.Posted]

    case message => clientActor ! message
  }
}

class PersistentActorSpec
  extends TestKit(ActorSystem("ActorSystemTest", ConfigFactory.load("akka.conf")))
    with FlatSpecLike
    with Matchers
    with ImplicitSender
    with DefaultTimeout{

  val domainActor = system.actorOf(Props[DomainActor], "DomainActor")
  val proxyActor = system.actorOf(Props(classOf[ProxyActor], domainActor, self))


  it should "persist and recover messages" in {
    import ServiceProtocol._
    proxyActor ! Join(Some("John"))
    val joined = expectMsgType[Joined]
    proxyActor !  Post(joined.session, Some("First message"))
    expectMsgType[Posted]
    proxyActor !  Post(joined.session, Some("Second message"))
    expectMsgType[Posted]
    proxyActor ! Leave(joined.session)
    expectMsgType[Left]
    proxyActor ! DomainModel.GetMessages
    val messages = expectMsgType[List[DomainModel.Post]]
    proxyActor ! DomainModel.Restart
    proxyActor ! DomainModel.GetMessages
    val recoveredMessages = expectMsgType[List[DomainModel.Post]]
    recoveredMessages shouldBe messages
  }

}
