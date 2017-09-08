import ReleaseTransformations._

val catsVersion = "0.9.0"

lazy val catsCheckSettings = Seq(
  organization := "org.mdedetrich",
  licenses += ("MIT", url("http://opensource.org/licenses/MIT")),
  homepage := Some(url("http://github.com/mdedetrich/cats-check")),

  scalaVersion := "2.12.1",
  crossScalaVersions := Seq("2.10.6", "2.11.8", "2.12.1"),

  scalacOptions ++= Seq(
    "-feature",
    "-deprecation",
    "-unchecked"
  ),

  libraryDependencies ++= Seq(
    "org.scalacheck" %%% "scalacheck" % "1.13.4",
    "org.typelevel" %%% "cats" % catsVersion,
    "org.typelevel" %%% "cats-laws" % catsVersion % "test",
    "org.scalatest" %%% "scalatest" % "3.0.0" % "test",
    "org.typelevel" %%% "discipline" % "0.7.3"      % "test"
  ),

  releaseCrossBuild := true,
  releasePublishArtifactsAction := PgpKeys.publishSigned.value,
  publishMavenStyle := true,
  publishArtifact in Test := false,
  pomIncludeRepository := Function.const(false),

  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
      Some("Snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("Releases" at nexus + "service/local/staging/deploy/maven2")
  },

  pomExtra := (
    <scm>
      <url>git@github.com:mdedetrich/cats-check.git</url>
      <connection>scm:git:git@github.com:mdedetrich/cats-check.git</connection>
    </scm>
    <developers>
      <developer>
        <id>d_m</id>
        <name>Erik Osheim</name>
        <url>http://github.com/non/</url>
      </developer>
      <developer>
        <id>mdedetrich</id>
        <name>Matthew de Detrich</name>
        <url>http://github.com/mdedetrich/</url>
      </developer>
    </developers>
  ),

  releaseProcess := Seq[ReleaseStep](
    checkSnapshotDependencies,
    inquireVersions,
    runClean,
    runTest,
    setReleaseVersion,
    commitReleaseVersion,
    tagRelease,
    publishArtifacts,
    setNextVersion,
    commitNextVersion,
    ReleaseStep(action = Command.process("sonatypeReleaseAll", _)),
    pushChanges))

lazy val noPublish = Seq(
  publish := {},
  publishLocal := {},
  publishArtifact := false)

lazy val root = project
  .in(file("."))
  .aggregate(catsCheckJS, catsCheckJVM)
  .settings(name := "cats-check-root")
  .settings(catsCheckSettings: _*)
  .settings(noPublish: _*)

lazy val catsCheck = crossProject
  .crossType(CrossType.Pure)
  .in(file("."))
  .settings(name := "cats-check")
  .settings(catsCheckSettings: _*)

lazy val catsCheckJVM = catsCheck.jvm

lazy val catsCheckJS = catsCheck.js
