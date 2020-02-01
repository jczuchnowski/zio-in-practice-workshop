package zio.workshop

import zio._

object Exercise1 {
  
  object Wrapping {

    import collection.JavaConverters._

    def getLines(path: java.nio.file.Path): List[String] = 
      java.nio.file.Files.readAllLines(path).asScala.toList

    /**
      * 
      *
      */
    def wrapped(path: java.nio.file.Path): IO[java.io.IOException, List[String]] = ???
  }

  object Errors {

    class ServiceError(m: String)

    /**
      * Map error from Throwable to ServiceError.
      * Use `mapError`.
      */
    def transformError(funcIO: Task[Int]): IO[ServiceError, Int] = ???
      
    /**
      * Map error from Throwable to ServiceError.
      * Use `flip`.
      */
    def transformError2(funcIO: Task[Int]): IO[ServiceError, Int] = ???
  
  }

  object Option {

    case class UserId(id: Long) extends AnyVal
    case class AssetId(id: Long) extends AnyVal
    case class Asset(name: String, value: Int)
    case class Portfolio(name: String, assets: List[AssetId])

    case class DbError()

    private def getPortfolioByUserId(userId: UserId): IO[Exception, Option[Portfolio]] = ???
    private def getAssets(ids: List[AssetId]): IO[Exception, List[Asset]] = ???

    /**
      * Return total assets value of a given user.
      */
    def getPortfolioValue(userId: UserId): IO[Exception, Option[Int]] = ???

  }

  object Environment {

    case class User(name: String, assetIds: List[Long])
    case class Asset(name: String, value: String)

    trait Logger {
      def log(messge: String): Task[Unit] = ???
    }

    trait UserRepository {
      def getUser: Task[User] = ???
    }

    trait AssetRepository {
      def getAssets(ids: List[Long]): Task[List[Asset]]
    }

    /**
      * Return user's assets and log a message.
      * What is the return type.
      */
    def getAssets = ???

  }

}
