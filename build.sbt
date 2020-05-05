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
val http4sVersion = "0.20.21"
val circeVersion = "0.11.1"
val doobieVersion = "0.7.1"
val mysqlConnector = "8.0.20"

resolvers += Resolver.sonatypeRepo("releases")

libraryDependencies ++= Seq(
  compilerPlugin("org.typelevel" %% "kind-projector" % "0.11.0" cross CrossVersion.full),
  "org.http4s" %% "http4s-dsl" % http4sVersion,
  "org.http4s" %% "http4s-blaze-client" % http4sVersion,
  "org.http4s" %% "http4s-circe" % http4sVersion,
  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-generic-extras" % circeVersion,
  "org.typelevel" %% "cats-core" % catsVersion,
  "org.typelevel" %% "cats-effect" % catsVersion,
  "com.bot4s" %% "telegram-core" % "4.4.0-RC2",
  "com.softwaremill.sttp" %% "async-http-client-backend-cats" % "1.7.2",
  "com.typesafe.scala-logging" %% "scala-logging" % scalaLoggingVersion,
  "org.slf4j" % "slf4j-api" % slf4jVersion,
  "org.slf4j" % "slf4j-simple" % slf4jVersion,
  "com.propensive" %% "kaleidoscope" % "0.1.0",
  "io.chrisdavenport" %% "log4cats-core" % "1.0.1",
  "io.chrisdavenport" %% "log4cats-slf4j" % "1.0.1",
  "mysql" % "mysql-connector-java" % mysqlConnector,
  "org.tpolecat" %% "doobie-core" % doobieVersion,
  "org.tpolecat" %% "doobie-hikari" % doobieVersion,
  "com.typesafe" % "config" % "1.4.0",
  "org.scalatest" %% "scalatest" % "3.1.1" % Test,
  "org.scalamock" %% "scalamock" % "4.4.0" % Test,
)
