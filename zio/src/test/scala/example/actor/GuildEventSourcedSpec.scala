package example.actor

import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import zio.actors.{ ActorSystem, Supervisor }
import zio.{ Unsafe, _ }

import java.io.File

class GuildEventSourcedSpec extends AsyncWordSpec with Matchers with BeforeAndAfterAll {

  "Persistent actors should be recover state after complete system shutdown" in {
    import example.actor.GuildEventSourced._
    val io = for {
      actorSystem <- ActorSystem("GuildSystem", Some(new File("zio/src/main/resources/application.conf")))
      // Scenario 1
      user1 <- Random.nextUUID.map(_.toString)
      user2 <- Random.nextUUID.map(_.toString)
      persistenceId1 = "guild1"
      guild1   <- actorSystem.make(persistenceId1, Supervisor.none, GuildState.empty, handler(persistenceId1))
      _        <- guild1 ? Join(user1)
      _        <- guild1 ? Join(user2)
      members1 <- guild1 ? Get
      _        <- Console.printLine(s"members1: $members1")
      _        <- guild1.stop
      // Scenario 2
      persistenceId2 = "guild2"
      guild2   <- actorSystem.make(persistenceId2, Supervisor.none, GuildState.empty, handler(persistenceId2))
      _        <- guild2 ? Join(user1)
      _        <- guild2 ? Join(user2)
      members2 <- guild2 ? Get
      _        <- Console.printLine(s"members2: $members2")
      _        <- guild2.stop
      // Scenario 3
      persistenceId1 = "guild1"
      guild1B  <- actorSystem.make(persistenceId1, Supervisor.none, GuildState.empty, handler(persistenceId1))
      user3    <- Random.nextUUID.map(_.toString)
      user4    <- Random.nextUUID.map(_.toString)
      _        <- guild1B ? Join(user3)
      _        <- guild1B ? Join(user4)
      members1 <- guild1B ? Get
      _        <- guild1B.stop
      _        <- actorSystem.shutdown
    } yield members1.members == (members2.members ++ Set(user3, user4))

    val runtime = zio.Runtime.default
    Unsafe.unsafe { implicit unsafe =>
      runtime.unsafe
        .runToFuture(io)
        .future
        .map(_ should be(true))
    }
  }
}
