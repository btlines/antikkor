package antikkor.example.blog

object Protocol {
  case object AllPosts
  case class Post(name: Option[String], date: Option[Long], message: Option[String])
  case object Published
}
