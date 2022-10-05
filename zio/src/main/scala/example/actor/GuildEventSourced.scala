package example.actor

import zio.actors.Context
import zio.actors.persistence.{ Command, EventSourcedStateful, PersistenceId }
import zio.{ UIO, ZIO }

import scala.util.{ Failure, Success, Try }
import zio.actors.ActorSystem
import zio.actors.Supervisor
import zio.actors.ActorRef
import infra.Layers.ActorSystemZ

object GuildEventSourced {
  sealed trait GuildMessage[+_]
  case class Join(userId: String)  extends GuildMessage[Try[Set[String]]]
  case class Leave(userId: String) extends GuildMessage[Unit]
  case object Get                  extends GuildMessage[GuildState]

  sealed trait GuildEvent
  case class JoinedEvent(userId: String) extends GuildEvent
  case class LeftEvent(userId: String)   extends GuildEvent

  case class GuildState(members: Set[String])
  object GuildState {
    def empty: GuildState = GuildState(members = Set.empty)
  }

  def handler(persistenceId: String): EventSourcedStateful[Any, GuildState, GuildMessage, GuildEvent] =
    new EventSourcedStateful[Any, GuildState, GuildMessage, GuildEvent](
      PersistenceId(persistenceId)
    ) {
      override def receive[A](
          state: GuildState,
          msg: GuildMessage[A],
          context: Context
      ): UIO[(Command[GuildEvent], GuildState => A)] =
        msg match {
          case Join(userId) =>
            if (state.members.size >= 5) {
              ZIO.succeed((Command.ignore, _ => Failure(new Exception("Guild is already full!")).asInstanceOf[A]))
            } else {
              ZIO.succeed((Command.persist(JoinedEvent(userId)), st => Success(st.members).asInstanceOf[A]))
            }
          case Leave(userId) => ZIO.succeed((Command.persist(LeftEvent(userId)), _ => ().asInstanceOf[A]))
          case Get           => ZIO.succeed((Command.ignore, _ => state.asInstanceOf[A]))
        }

      override def sourceEvent(state: GuildState, event: GuildEvent): GuildState =
        event match {
          case JoinedEvent(userId) =>
            state.copy(
              members = state.members + userId
            )
          case LeftEvent(userId) =>
            state.copy(
              members = state.members - userId
            )
        }
    }

  def actorRef(
      entityId: String
  ): ZIO[ActorSystemZ, Throwable, ActorRef[GuildEventSourced.GuildMessage]] =
    ZIO.serviceWithZIO[ActorSystemZ] { actorSystemZ =>
      actorSystemZ.system
        .select[GuildEventSourced.GuildMessage](actorSystemZ.basePath + entityId)
        .orElse(
          actorSystemZ.system
            .make(
              entityId,
              Supervisor.none,
              GuildState.empty,
              GuildEventSourced.handler(entityId)
            )
        )
    }
}
