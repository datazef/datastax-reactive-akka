name := "datastax-reactive-akka"

version := "0.1"

scalaVersion := "2.12.6"

val akkaVersion = "2.6.4"
val circeVersion = "0.12.3"
val akkaHttpVersion = "10.1.11"
val dseDriverVersion = "4.5.1"

libraryDependencies ++=
  Seq(
    "ch.qos.logback" % "logback-classic" % "1.2.3",
    "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
    "org.tuxdude.logback.extensions" % "logback-colorizer" % "1.0.1",
    "de.heikoseeberger" %% "akka-http-circe" % "1.32.0",
    "io.circe" %% "circe-core" % "0.13.0") ++
  Seq(
    "com.typesafe.akka" %% "akka-actor",
    "com.typesafe.akka" %% "akka-stream",
    "com.typesafe.akka" %% "akka-slf4j").map(_ % akkaVersion) ++
  Seq(
    "io.circe" %% "circe-core",
    "io.circe" %% "circe-generic",
    "io.circe" %% "circe-parser").map(_ % circeVersion) ++
  Seq(
    "com.datastax.oss" % "java-driver-core",
    "com.datastax.oss" % "java-driver-query-builder",
    "com.datastax.oss" % "java-driver-mapper-runtime").map(_ % dseDriverVersion) ++
  Seq(
    "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test)

scalacOptions ++= Seq(
    "-Xfatal-warnings",
    "-deprecation",
    "-feature",
    "-unchecked",
    "-language:implicitConversions",
    "-language:higherKinds",
    "-language:existentials",
    "-language:postfixOps"
)

resolvers += Resolver.sonatypeRepo("releases")

addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full)