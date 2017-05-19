name := "antikkor"

version := "0.0.0"

scalaVersion := "2.12.2"

resolvers ++= Seq(
  Resolver.bintrayRepo("beyondthelines", "maven"),
  Resolver.jcenterRepo
)

libraryDependencies ++= Seq(
  "com.typesafe.akka"   %% "akka-actor"                % "2.5.1",
  "com.typesafe.akka"   %% "akka-persistence"          % "2.5.1",
  "beyondthelines"      %% "pbdirect"                  % "0.0.2",
  "beyondthelines"      %% "fluent"                    % "0.0.4",
  "com.github.dnvriend" %% "akka-persistence-inmemory" % "2.5.1.0"
)