name := "wordcount"

version := "0.1"

scalaVersion := "2.12.7"

libraryDependencies ++= Seq(
  "co.fs2" %% "fs2-core" % "1.0.0",
  "org.typelevel" %% "cats-core" % "1.4.0",
  "org.typelevel" %% "cats-effect" % "1.0.0",
  "com.typesafe.akka" %% "akka-stream" % "2.5.18",
  "org.scalactic" %% "scalactic" % "3.0.5",
  "org.scalatest" %% "scalatest" % "3.0.5" % Test
)
