lazy val root = (project in file("."))
  .settings(
    name := "influencer-stats",
    organization := "miciek",
    version := "1.0",
    libraryDependencies ++= Seq(
      "org.typelevel"     %% "cats-core"            % "1.4.+",
      "org.typelevel"     %% "cats-effect"          % "1.0.+",
      "com.typesafe.akka" %% "akka-http-core"       % "10.1.+",
      "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.+",
      "io.circe"          %% "circe-generic"        % "0.10.+",
      "io.circe"          %% "circe-parser"         % "0.10.+",
      "de.heikoseeberger" %% "akka-http-circe"      % "1.22.+",
      "org.scalatest"     %% "scalatest"            % "3.0.+" % Test,
      "com.typesafe.akka" %% "akka-http-testkit"    % "10.1.+" % Test
    ),
    scalaVersion := "2.12.7",
    scalacOptions ++= List(
      "-unchecked",
      "-Ywarn-unused-import",
      "-Xfatal-warnings",
      "-Ypartial-unification",
      "-language:higherKinds",
      "-Xlint"
    ),
    mainClass in assembly := Some("com.michalplachta.influencerstats.Main"),
    scalafmtOnCompile := true,
    addCommandAlias("formatAll", ";sbt:scalafmt;test:scalafmt;compile:scalafmt"),
    addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.6")
  )
