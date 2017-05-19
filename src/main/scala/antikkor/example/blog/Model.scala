package antikkor.example.blog

import java.time.Instant

object Model {
  case class Author(name: String)
  case class Post(author: Author, date: Instant, message: String)
  case class Publish(post: Post)

  sealed trait Event
  case class Published(post: Post) extends Event

  case object AllPosts
  case class Posts(posts: List[Post])
}
