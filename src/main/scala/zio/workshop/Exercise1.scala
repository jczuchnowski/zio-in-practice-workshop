package zio.workshop

import zio._
import java.io.IOException
import zio.duration.Duration
import java.util.concurrent.TimeUnit

object Exercise1 {
  
  object Wrapping {

    import collection.JavaConverters._

    def getLines(path: java.nio.file.Path): List[String] = 
      java.nio.file.Files.readAllLines(path).asScala.toList

    /**
      * 
      */
    def wrapped(path: java.nio.file.Path): IO[java.io.IOException, List[String]] =
      ZIO.effect(getLines(path)).refineToOrDie[IOException]
      
  }

  object Errors {

    class ServiceError(m: String)

    /**
      * Map error from Throwable to ServiceError.
      * Use `mapError`.
      */
    def transformError(funcIO: Task[Int]): IO[ServiceError, Int] =
      funcIO.mapError(e => new ServiceError(e.getMessage()))
      
    /**
      * Map error from Throwable to ServiceError.
      * Use `flip`.
      */
    def transformError2(funcIO: Task[Int]): IO[ServiceError, Int] =
      funcIO.flip.map(e => new ServiceError(e.getMessage())).flip

    /**
      * Map both error and value.
      */
    def transform(funcIO: Task[Int]): IO[ServiceError, String] =
      funcIO.bimap(
        e => new ServiceError(e.getMessage),
        a => a.toString()
      )

  }

  object Option {

    case class UserId(id: Long) extends AnyVal
    case class AssetId(id: Long) extends AnyVal
    case class Asset(name: String, value: Int)
    case class Portfolio(name: String, assets: Set[AssetId])

    case class RepositoryError(msg: String)

    object FutureExample {
      
      import scala.concurrent.Future
      
      implicit val ec = scala.concurrent.ExecutionContext.global

      private def getPortfolioByUserId(userId: UserId): Future[Option[Portfolio]] = ???
      private def getAssets(ids: Set[AssetId]): Future[Set[Asset]] = ???

      def getPortfolioValue(userId: UserId): Future[Option[Int]] =
        for {
          portfolioOpt <- getPortfolioByUserId(userId)
          assetsOpt    <- portfolioOpt.map(p => getAssets(p.assets)) match {
                            case Some(fut) => fut.map(Some(_))
                            case None      => Future.successful(None)
                          }
        } yield assetsOpt.map(l => l.foldLeft(0) { case (a, v) => a + v.value })
    }

    object ZioWay {
      
      private def getPortfolioByUserId(userId: UserId): IO[RepositoryError, Option[Portfolio]] = ???
      private def getAssets(ids: Set[AssetId]): IO[RepositoryError, Set[Asset]] = ???

      /**
        * Return total assets value of a given user.
        * Use `some`, `optional` and `mapError`
        * 
        * consider these isomorphisms:
        * Option[A] ~ Either[Unit, A]
        * Either[A, Option[B]] ~ Either[Option[A], B]
        */
      def getPortfolioValue(userId: UserId): IO[RepositoryError, Option[Int]] =
        (for {
          portfolio <- getPortfolioByUserId(userId).some
          assets    <- getAssets(portfolio.assets).mapError(Some(_))
        } yield assets.foldLeft(0) { case (a, v) => a + v.value }).optional

    }
  }

  object Environment {

    case class User(name: String, assets: Set[Long])
    case class Asset(name: String, value: String)

    trait Logger {
      def log(messge: String): Task[Unit]
    }

    trait UserRepository {
      def getUser: Task[User]
    }

    trait AssetRepository {
      def getAssets(ids: Set[Long]): Task[Set[Asset]]
    }

    type Repositories = UserRepository with AssetRepository
    /**
      * Return user's assets and log a message.
      * What is the return type.
      */
    def getAssets: ZIO[Repositories with Logger, Throwable, Set[Asset]] =
      for {
        user   <- ZIO.accessM[UserRepository](_.getUser)
        _      <- ZIO.accessM[Logger](_.log("found user"))
        assets <- ZIO.accessM[AssetRepository](_.getAssets(user.assets)) 
      } yield assets

  }

  object Temporal {

    import zio.clock.Clock
    
    trait Database
    trait DbError
    trait Connection

    val getConnection: ZIO[Database, DbError, Connection] = ???

    import zio.duration._
    import zio.duration.Duration._
    /**
      * Retry every 10 secends.
      */
    val retry1: ZIO[Database with Clock, DbError, Connection] =
      getConnection.retry(Schedule.fixed(Duration.apply(10, TimeUnit.SECONDS)))

    /**
      * Retry 5 times.
      */
    val retry2: ZIO[Database, DbError, Connection] =
      getConnection.retry(Schedule.recurs(5))

    val mySchedule = Schedule.fixed(Duration.apply(5, TimeUnit.SECONDS)) && Schedule.recurs(5)

    /**
      * Retry every 5 seconds 5 times.
      */
    val retry3: ZIO[Database with Clock, DbError, Connection] =
      getConnection.retry(mySchedule)

  }

}
