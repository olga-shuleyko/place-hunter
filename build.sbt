name := "place-hunter"

version := "0.1"

scalaVersion := "2.12.11"

scalacOptions ++= Seq(
  "-deprecation",
  "-feature",
  "-language:higherKinds"
)

val catsVersion = "2.1.0"
val sttpVersion =  "1.7.2"
val slf4jVersion = "1.7.5"
val scalaLoggingVersion = "3.9.2"

libraryDependencies ++= Seq(
  compilerPlugin("org.typelevel" %% "kind-projector" % "0.11.0" cross CrossVersion.full),
  "org.typelevel" %% "cats-core" % catsVersion,
  "org.typelevel" %% "cats-effect" % catsVersion,
  "com.bot4s" %% "telegram-core" % "4.4.0-RC2",
  "com.softwaremill.sttp" %% "async-http-client-backend-cats" % "1.7.2",
  "com.softwaremill.macwire" %% "macros" % "2.3.3" % "provided",
  "com.typesafe.scala-logging" %% "scala-logging" % scalaLoggingVersion,
  "org.slf4j" % "slf4j-api" % slf4jVersion,
  "org.slf4j" % "slf4j-simple" % slf4jVersion,
)