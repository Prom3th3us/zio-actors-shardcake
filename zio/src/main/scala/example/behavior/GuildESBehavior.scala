package example.behavior

import com.devsisters.shardcake.Messenger.Replier
import com.devsisters.shardcake.{ EntityType, Sharding }
import example.actor.GuildEventSourced
import GuildEventSourced.GuildState
import zio.actors.{ ActorRef, ActorSystem, Supervisor }
import zio.{ Dequeue, RIO, ZIO }

import scala.util.Try
import infra.Layers.ActorSystemZ

object GuildESBehavior {
  sealed trait GuildESMessage

  object GuildESMessage {
    case class Join(userId: String, replier: Replier[Try[Set[String]]]) extends GuildESMessage
    case class Leave(userId: String)                                    extends GuildESMessage
  }

  object GuildES extends EntityType[GuildESMessage]("guildES")

  def behavior(
      entityId: String,
      messages: Dequeue[GuildESMessage]
  ): RIO[Sharding with ActorSystemZ, Nothing] =
    ZIO.logInfo(s"Started entity $entityId") *>
      messages.take.flatMap(handleMessage(entityId, _)).forever

  def handleMessage(
      entityId: String,
      message: GuildESMessage
  ): RIO[Sharding with ActorSystemZ, Unit] =
    GuildEventSourced
      .actorRef(entityId)
      .flatMap { actor =>
        message match {
          case GuildESMessage.Join(userId, replier) =>
            actor
              .?(GuildEventSourced.Join(userId))
              .flatMap { tryMembers =>
                replier.reply(tryMembers)
              }
          case GuildESMessage.Leave(userId) =>
            actor
              .?(GuildEventSourced.Leave(userId))
              .unit
        }
      }
}
