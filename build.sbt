name := "wordcount"

version := "0.1"

scalaVersion := "2.12.7"

scalacOptions ++= Seq(
  "-language:postfixOps"
)

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-stream" % "2.5.18",
  "org.scalactic" %% "scalactic" % "3.0.5",
  "org.scalatest" %% "scalatest" % "3.0.5" % Test
)
