package antikkor.serialization

import akka.serialization.Serializer

trait PBAkkaSerializer extends Serializer {
  def serialize: PartialFunction[AnyRef, Array[Byte]]
  def unserializeTo: PartialFunction[(Class[_], Array[Byte]), AnyRef]

  override def toBinary(o: AnyRef): Array[Byte] =
    if (serialize.isDefinedAt(o)) serialize.apply(o)
    else throw new IllegalArgumentException(s"Can't serialize ${o.getClass.getName}")

  override def includeManifest: Boolean = true

  override def fromBinary(bytes: Array[Byte], manifest: Option[Class[_]]): AnyRef =
    manifest match {
      case Some(claSS) if (unserializeTo.isDefinedAt((claSS, bytes))) => unserializeTo.apply(claSS, bytes)
      case Some(claSS) => throw new IllegalArgumentException(s"Don't know how to deserialize to ${claSS.getName} in ${this.getClass.getName}")
      case None        => throw new IllegalArgumentException("Need a protobuf serializable class")
    }
}
