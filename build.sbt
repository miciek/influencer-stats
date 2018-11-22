lazy val root = (project in file("."))
  .settings(
    name := "influencer-stats",
    organization := "miciek",
    version := "1.0",
    libraryDependencies ++= Seq(
      "com.github.mpilquist" %% "simulacrum"           % "0.14.+",
      "org.typelevel"        %% "cats-core"            % "1.4.+",
      "org.typelevel"        %% "cats-effect"          % "1.0.+",
      "org.typelevel"        %% "cats-mtl-core"        % "0.4.+",
      "com.typesafe.akka"    %% "akka-http-core"       % "10.1.+",
      "com.typesafe.akka"    %% "akka-http-spray-json" % "10.1.+",
      "org.http4s"           %% "http4s-dsl"           % "0.19.+",
      "org.http4s"           %% "http4s-blaze-server"  % "0.19.+",
      "org.http4s"           %% "http4s-blaze-client"  % "0.19.+",
      "org.http4s"           %% "http4s-circe"         % "0.19.+",
      "io.circe"             %% "circe-core"           % "0.10.+",
      "io.circe"             %% "circe-generic"        % "0.10.+",
      "io.circe"             %% "circe-parser"         % "0.10.+",
      "com.pepegar"          %% "hammock-core"         % "0.8.+",
      "com.pepegar"          %% "hammock-circe"        % "0.8.+",
      "de.heikoseeberger"    %% "akka-http-circe"      % "1.22.+",
      "io.monix"             %% "monix-execution"      % "2.3.+",
      "ch.qos.logback"       % "logback-classic"       % "1.2.+",
      "org.scalatest"        %% "scalatest"            % "3.0.+" % Test,
      "com.typesafe.akka"    %% "akka-http-testkit"    % "10.1.+" % Test
    ),
    scalaVersion := "2.12.7",
    scalacOptions ++= List(
      "-unchecked",
      "-deprecation",
      "-Ywarn-unused-import",
      "-Xfatal-warnings",
      "-Ypartial-unification",
      "-language:higherKinds",
      "-language:implicitConversions",
      "-Xlint"
    ),
    mainClass in assembly := Some("com.michalplachta.influencerstats.Main"),
    scalafmtOnCompile := true,
    addCommandAlias("formatAll", ";sbt:scalafmt;test:scalafmt;compile:scalafmt"),
    addCompilerPlugin("org.spire-math"  %% "kind-projector" % "0.9.6"),
    addCompilerPlugin("org.scalamacros" % "paradise"        % "2.1.0" cross CrossVersion.full)
  )
