package coop.rchain.rspace.pure

import cats.Id
import cats.effect.Sync
import coop.rchain.rspace.ISpace.IdISpace
import coop.rchain.rspace._

import scala.collection.immutable.Seq

trait PureRSpace[F[_], C, P, E, A, R, K] {
  def consume(
      channels: Seq[C],
      patterns: Seq[P],
      continuation: K,
      persist: Boolean
  ): F[Either[E, Option[(ContResult[C, P, K], Seq[Result[R]])]]]

  def install(channels: Seq[C], patterns: Seq[P], continuation: K): F[Option[(K, Seq[R])]]

  def produce(
      channel: C,
      data: A,
      persist: Boolean
  ): F[Either[E, Option[(ContResult[C, P, K], Seq[Result[R]])]]]

  def createCheckpoint(): F[Checkpoint]

  def reset(hash: Blake2b256Hash): F[Unit]

  def close(): F[Unit]
}

object PureRSpace {
  def apply[F[_]](implicit F: Sync[F]): PureRSpaceApplyBuilders[F] = new PureRSpaceApplyBuilders(F)

  final class PureRSpaceApplyBuilders[F[_]](val F: Sync[F]) extends AnyVal {
    def of[C, P, E, A, R, K](
        space: ISpace[Id, C, P, E, A, R, K]
    )(implicit mat: Match[P, E, A, R]): PureRSpace[F, C, P, E, A, R, K] =
      new PureRSpace[F, C, P, E, A, R, K] {
        def consume(
            channels: Seq[C],
            patterns: Seq[P],
            continuation: K,
            persist: Boolean
        ): F[Either[E, Option[(ContResult[C, P, K], Seq[Result[R]])]]] =
          F.delay(space.consume(channels, patterns, continuation, persist))

        def install(channels: Seq[C], patterns: Seq[P], continuation: K): F[Option[(K, Seq[R])]] =
          F.delay(space.install(channels, patterns, continuation))

        def produce(
            channel: C,
            data: A,
            persist: Boolean
        ): F[Either[E, Option[(ContResult[C, P, K], Seq[Result[R]])]]] =
          F.delay(space.produce(channel, data, persist))

        def createCheckpoint(): F[Checkpoint] = F.delay(space.createCheckpoint())

        def reset(hash: Blake2b256Hash): F[Unit] = F.delay(space.reset(hash))

        def close(): F[Unit] = F.delay(space.close())
      }
  }
}
