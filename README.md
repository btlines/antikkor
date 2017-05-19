# Antikkor

This repo is an example (proof of concept) on how to build anti corruption layers for Akka. It aims at segregating the business domain inside the core actors.

The `App` creates 2 services: Blog and Auth and simulates calls to both of them.

## Business domain

Each service actor is using its internal domain model (defined in `Model.scala`).

## Persistence

These core actors are persisted (they extend `PersistentActor`). However they don't persist their internal core model.
Instead the core model is translated in a persistence model (defined in `Persistence.scala`).
It's the job of the `EventAdapter` to translate between the domain model and the persistence model.

Then their associated `PBAkkaSerializer` deals with the serialization of the persistence model into Protobuf binary format.

## Protocol

Similarly clients using these services do not use their domain model but a protocol (defined in `Protocol.scala`).

Akka doesn't provide any abstraction to handle the translation between the protocol and the domain model.
However it's quite easy to create a proxy actor in charge of the translation between the protocol and the domain model.

This repo took a different approach where the core actors extends (`AdapterActor`) which intercepts any incoming messages (using the `aroundReceive` method) and translate them into the domain model before calling the actor's receive method.