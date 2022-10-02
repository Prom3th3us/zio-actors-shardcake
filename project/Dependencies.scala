import sbt._

object Dependencies {
  object Versions {
    val typeSafeConfig     = "1.4.2"
    val scalaTest          = "3.2.13"
    val logback            = "1.2.11"
    val logbackEncoder     = "7.2"
    val jacksonScalaModule = "2.13.3"
    val zio                = "2.0.2"
    val zioActors          = "0.1.0"
    val zioTest            = "2.0.0"
    val zioGrpc            = "0.6.0-test4"
    val shardcake          = "2.0.0"
  }

  object TypeSafe {
    val config = "com.typesafe" % "config" % Versions.typeSafeConfig

    val all = Seq(
      config
    )
  }

  object Testing {
    val scalaTest =
      "org.scalatest" %% "scalatest" % Versions.scalaTest % Test

    val all = Seq(
      scalaTest
    )
  }

  object Logging {
    val logback = "ch.qos.logback" % "logback-classic" % Versions.logback
    val logbackEncoder =
      "net.logstash.logback" % "logstash-logback-encoder" % Versions.logbackEncoder % Runtime
    val jacksonScalaModule =
      "com.fasterxml.jackson.module" %% "jackson-module-scala" % Versions.jacksonScalaModule % Runtime

    val all = Seq(
      logback,
      logbackEncoder,
      jacksonScalaModule
    )
  }

  object Zio {
    val zio                  = "dev.zio"                       %% "zio"                    % Versions.zio
    val zioActors            = "dev.zio"                       %% "zio-actors"             % Versions.zioActors
    val zioActorsPersistence = "dev.zio"                       %% "zio-actors-persistence" % Versions.zioActors
    val zioTest              = "dev.zio"                       %% "zio-test"               % Versions.zioTest % Test
    val zioGrpc              = "com.thesamet.scalapb.zio-grpc" %% "zio-grpc-core"          % Versions.zioGrpc

    val all = Seq(
      zio,
      zioActors,
      zioActorsPersistence,
      zioTest,
      zioGrpc
    )
  }

  object Shardcake {
    val shardcake         = "com.devsisters" %% "shardcake-core"               % Versions.shardcake
    val shardcakeEntities = "com.devsisters" %% "shardcake-entities"           % Versions.shardcake
    val shardcakeManager  = "com.devsisters" %% "shardcake-manager"            % Versions.shardcake
    val shardcakeK8s      = "com.devsisters" %% "shardcake-health-k8s"         % Versions.shardcake
    val shardcakeGrpc     = "com.devsisters" %% "shardcake-protocol-grpc"      % Versions.shardcake
    val shardcakeKryo     = "com.devsisters" %% "shardcake-serialization-kryo" % Versions.shardcake
    val shardcakeRedis    = "com.devsisters" %% "shardcake-storage-redis"      % Versions.shardcake

    val all = Seq(
      shardcake,
      shardcakeEntities,
      shardcakeManager,
      shardcakeK8s,
      shardcakeGrpc,
      shardcakeKryo,
      shardcakeRedis
    )
  }

}
