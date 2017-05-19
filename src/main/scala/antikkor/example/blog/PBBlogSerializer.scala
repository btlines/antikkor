package antikkor.example.blog

import antikkor.example.blog.Persistence.PostPublished
import antikkor.serialization.PBAkkaSerializer
import cats.instances.option._
import pbdirect._

class PBBlogSerializer extends PBAkkaSerializer {

  override def identifier: Int = 332621158

  override def serialize: PartialFunction[AnyRef, Array[Byte]] = {
    case event: PostPublished => event.toPB
  }

  override def unserializeTo: PartialFunction[(Class[_], Array[Byte]), AnyRef] = {
    case (claSS, bytes) if claSS == classOf[PostPublished] => bytes.pbTo[PostPublished]
  }
}
