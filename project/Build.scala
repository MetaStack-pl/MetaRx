import sbt._
import sbt.Keys._
import xerial.sbt.Sonatype.sonatypeSettings
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import sbtbuildinfo.BuildInfoPlugin
import sbtbuildinfo.BuildInfoPlugin.autoImport._

object Build extends sbt.Build {
  object Dependencies {
    val ScalaTest = "3.0.0-M16-SNAP4"
    val MetaDocs  = "0.1.1"
    val Upickle   = "0.4.0"
  }

  val SharedSettings = Seq(
    name := "MetaRx",
    organization := "pl.metastack",
    scalaVersion := "2.11.8",
    // crossScalaVersions := Seq("2.12.0-M4", "2.11.8"),
    scalacOptions := Seq(
      "-unchecked",
      "-deprecation",
      "-encoding", "utf8"
    ),
    pomExtra :=
      <url>https://github.com/MetaStack-pl/MetaRx</url>
      <licenses>
        <license>
          <name>Apache-2.0</name>
          <url>https://www.apache.org/licenses/LICENSE-2.0.html</url>
        </license>
      </licenses>
      <scm>
        <url>git@github.com:MetaStack-pl/MetaRx.git</url>
      </scm>
      <developers>
        <developer>
          <id>tindzk</id>
          <name>Tim Nieradzik</name>
          <url>http://github.com/tindzk/</url>
        </developer>
      </developers>
  )

  lazy val root = project.in(file("."))
    .aggregate(js, jvm, upickle.js, upickle.jvm)
    .settings(SharedSettings: _*)
    .settings(publishArtifact := false)

  lazy val metaRx = crossProject.in(file("."))
    .settings(SharedSettings: _*)
    .settings(sonatypeSettings: _*)
    .settings(
      autoAPIMappings := true,
      apiMappings += (scalaInstance.value.libraryJar -> url(s"http://www.scala-lang.org/api/${scalaVersion.value}/"))
    )
    .jsConfigure(_.enablePlugins(com.thoughtworks.sbtScalaJsMap.ScalaJsMap))
    .jsSettings(
      libraryDependencies +=
        "org.scalatest" %%% "scalatest" % Dependencies.ScalaTest % "test",

      scalaJSStage in Global := FastOptStage,

      /* Use io.js for faster compilation of test cases */
      scalaJSUseRhino in Global := false
    )
    .jvmSettings(
      libraryDependencies +=
        "org.scalatest" %% "scalatest" % Dependencies.ScalaTest % "test"
    )

  lazy val upickle = crossProject
    .crossType(CrossType.Pure)
    .in(file("upickle"))
    .settings(SharedSettings: _*)
    .settings(name := "metarx-upickle")
    .dependsOn(metaRx)
    .jsSettings(
      libraryDependencies ++= Seq(
        "com.lihaoyi" %%% "upickle" % Dependencies.Upickle,
        "org.scalatest" %%% "scalatest" % Dependencies.ScalaTest % "test"
      )
    )
    .jvmSettings(
      libraryDependencies ++= Seq(
        "com.lihaoyi" %% "upickle" % Dependencies.Upickle,
        "org.scalatest" %% "scalatest" % Dependencies.ScalaTest % "test"
      )
    )

  lazy val js = metaRx.js
  lazy val jvm = metaRx.jvm

  lazy val upickleJS = upickle.js
  lazy val upickleJVM = upickle.jvm

  lazy val manual = project.in(file("manual"))
    .dependsOn(jvm, upickleJVM)
    .enablePlugins(BuildInfoPlugin)
    .settings(SharedSettings: _*)
    .settings(
      publishArtifact := false,
      libraryDependencies ++= Seq(
        "pl.metastack" %% "metadocs" % Dependencies.MetaDocs,
        "org.eclipse.jgit" % "org.eclipse.jgit" % "4.1.1.201511131810-r"),
      buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion),
      buildInfoPackage := "pl.metastack.metarx",
      name := "MetaRx manual")
}
