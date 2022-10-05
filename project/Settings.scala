import sbt.Keys._
import sbt._

import scalafix.sbt.ScalafixPlugin.autoImport._
import com.typesafe.sbt.SbtNativePackager.autoImport._
import com.typesafe.sbt.packager.docker.DockerPlugin.autoImport._
import com.typesafe.sbt.packager.docker._

object Settings extends CommonScalac {
  lazy val commonSettings = Seq(
    run / fork                := true,
    Test / testForkedParallel := true,
    libraryDependencies ++= Seq(
      Dependencies.Logging.all,
      Dependencies.TypeSafe.all,
      Dependencies.Testing.all
    ).flatten
  )

  lazy val scalafixSettings: Seq[Setting[_]] = Seq(
    addCompilerPlugin(scalafixSemanticdb),
    semanticdbEnabled := true,
    scalafixOnCompile := true
  )

  lazy val dockerSettings = Seq(
    dockerUsername        := sys.props.get("docker.username"),
    dockerRepository      := sys.props.get("docker.registry"),
    Docker / version      := "latest",
    Docker / organization := "prom3theus",
    dockerBaseImage       := "azul/zulu-openjdk:17"
  )
}
