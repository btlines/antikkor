package antikkor.example.auth

import antikkor.example.auth.Persistence.{Authenticated, Terminated}
import antikkor.serialization.PBAkkaSerializer
import cats.instances.option._
import pbdirect._

class PBAuthSerializer extends PBAkkaSerializer {

  override def identifier: Int = 1340982252

  override def serialize: PartialFunction[AnyRef, Array[Byte]] = {
    case event: Authenticated => event.toPB
    case event: Terminated    => event.toPB
  }

  override def unserializeTo: PartialFunction[(Class[_], Array[Byte]), AnyRef] = {
    case (claSS, bytes) if claSS == classOf[Authenticated] => bytes.pbTo[Authenticated]
    case (claSS, bytes) if claSS == classOf[Terminated]    => bytes.pbTo[Terminated]
  }
}
