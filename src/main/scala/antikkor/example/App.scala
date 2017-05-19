package antikkor.example

import java.time.Instant

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout
import antikkor.example.Supervisor._
import antikkor.example.auth.AuthActor
import antikkor.example.auth.Protocol._
import antikkor.example.blog.BlogActor
import antikkor.example.blog.Protocol._
import com.typesafe.config.ConfigFactory

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

class Supervisor extends Actor {
  var blog: ActorRef = _
  var auth: ActorRef = _
  override def receive: Receive = {
    case StartBlogActor =>
      blog = context.actorOf(Props(classOf[BlogActor]))
      sender() ! BlogActorStarted(blog)
    case StartAuthActor =>
      auth = context.actorOf(Props(classOf[AuthActor]))
      sender() ! AuthActorStarted(auth)
    case StopBlogActor => context.stop(blog)
    case StopAuthActor => context.stop(auth)
  }
}

object Supervisor {
  case object StartBlogActor
  case object StartAuthActor
  case object StopBlogActor
  case object StopAuthActor
  case class BlogActorStarted(actor: ActorRef)
  case class AuthActorStarted(actor: ActorRef)
}

object App extends App {
  val system = ActorSystem("Blogger", ConfigFactory.load("akka.conf"))
  val supervisor = system.actorOf(Props(classOf[Supervisor]))

  implicit val timeout = Timeout(2.seconds)
  implicit val ec = system.dispatcher

  val scenario: Future[Unit] = for {
    // start the actors
    auth    <- (supervisor ? StartAuthActor).mapTo[AuthActorStarted]
    blog    <- (supervisor ? StartBlogActor).mapTo[BlogActorStarted]
    // start session
    session <- (auth.actor ? StartSession(Some("John"))).mapTo[SessionStarted]
    _       <- (auth.actor ? Verify(session.token)).mapTo[SessionVerified]
    // post some messages
    _       <- blog.actor ? Post(session.user, Some(System.currentTimeMillis), Some("Hello there!"))
    _       <- blog.actor ? Post(session.user, Some(System.currentTimeMillis), Some("How are you?"))
    // stop the actors to simulate a failure
    _       =  supervisor ! StopAuthActor
    _       =  supervisor ! StopBlogActor
    // then restart the auth actor
    auth2   <- (supervisor ? StartAuthActor).mapTo[AuthActorStarted]
    // the user session should still be valid
    _       <- (auth2.actor ? Verify(session.token)).mapTo[SessionVerified]
    // restart the blog actor
    blog2   <- (supervisor ? StartBlogActor).mapTo[BlogActorStarted]
    // and post more messages
    _       <- blog2.actor ? Post(session.user, Some(System.currentTimeMillis), Some("Something bad happened"))
    _       <- blog2.actor ? Post(session.user, Some(System.currentTimeMillis), Some("but we're alright!"))
    // retrieve all posted messages (before and after the failure)
    posts   <- (blog2.actor ? AllPosts).mapTo[List[Post]]
  } yield posts.reverse.foreach { post =>
    // and print them
    val message = post.message.get
    val author  = post.name.get
    val date    = Instant.ofEpochMilli(post.date.get)
    println(s"[$date] $author: $message")
  }

  scenario.onComplete {
    case _ => system.terminate()
  }

  Await.result(scenario, Duration.Inf)
}
