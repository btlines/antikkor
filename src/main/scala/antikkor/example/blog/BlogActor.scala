package antikkor.example.blog

import akka.persistence.PersistentActor
import antikkor.example.blog.Model._

class BlogActor extends PersistentActor with BlogAdapterActor {
  var posts: List[Post] = Nil

  override def persistenceId: String = "BlogActor"

  override def receiveCommand: Receive = {
    case AllPosts => sender() ! translate(Posts(posts))
    case Publish(post) => persist(Published(post)) { event =>
      updateState(event)
      sender() ! translate(event)
    }
  }

  override def receiveRecover: Receive = {
    case event: Published => updateState(event)
  }

  private def updateState(event: Published): Unit = posts ::= event.post
}
