package coursier.cache.protocol

import java.io.InputStream
import java.net.{URL, URLConnection, URLStreamHandler, URLStreamHandlerFactory}
import java.nio.charset.CodingErrorAction
import java.nio.file.{Path, Paths}

import awscala.Credentials
import awscala.s3.{Bucket, S3, S3Object}
import com.amazonaws.services.s3.model.GetObjectRequest

import scala.collection.breakOut
import scala.io.{Codec, Source}
import scala.util.control.NonFatal
import scala.util.{Properties, Try}

/*
 * To avoid collision with `fm-sbt-s3-resolver` we added a different URL
 * format starting with `s3c` (S3 coursier).
 *
 * This way you can have your resolver URL with `s3c` and your publish URL with `s3`.
 *
 * Our handler only supports one kind of URL:
 * s3c://s3-<region>.amazonaws.com/<bucket-name>
 *
 * For now the region in the url is being ignored.
 *
 * It does not support credentials in the URLs for security reasons.
 * You should provide them as environment variables or
 * in `.s3credentials` in $HOME, $HOME/.sbt, $HOME/.coursier
 */

class S3cHandler extends URLStreamHandler {

  override def openConnection(url: URL): URLConnection = {
    new URLConnection(url) {
      override def getInputStream: InputStream = {
        getClient.map { s3Client =>
          val subPaths = url.getPath.stripPrefix("/").split("/")

          // Bucket
          val bucketName = subPaths.head

          // Key
          val key = subPaths.tail.mkString("/")

          val bucket = Bucket(bucketName)

          try {
            S3Object(bucket, s3Client.getObject(new GetObjectRequest(bucket.name, key))).content
          } catch {
            case e: Throwable =>
              e.printStackTrace()
              throw e
          }
        }
      }.getOrElse {
        throw new Exception("Failed to retrieve credentials")
      }

      override def connect() {}

    }
  }

  private def getClient: Option[S3] = {
    readFromEnv
      .orElse(readFromFile(Paths.get("").toAbsolutePath))
      .orElse(readFromFile(Paths.get(Properties.userHome)))
      .orElse(readFromFile(Paths.get(Properties.userHome).resolve(".sbt")))
      .orElse(readFromFile(Paths.get(Properties.userHome).resolve(".coursier")))
  }

  private def readFromEnv: Option[S3] = {
    for {
      accessKey <- sys.env.get("AWS_ACCESS_KEY_ID")
      secretKey <- sys.env.get("AWS_SECRET_ACCESS_KEY")
    } yield {
      val region = sys.env.get("AWS_DEFAULT_REGION")
        .map(awscala.Region.apply)
        .getOrElse(awscala.Region.EU_WEST_1)

      S3(Credentials(accessKey, secretKey))(region)
    }
  }

  private def readFromFile(path: Path): Option[S3] = {
    val file = path.resolve(".s3credentials").toFile

    implicit val codec = Codec("UTF-8")
    codec.onMalformedInput(CodingErrorAction.REPLACE)
    codec.onUnmappableCharacter(CodingErrorAction.REPLACE)

    val sourceOpt = Try(Source.fromFile(file)).toOption

    try {
      sourceOpt.flatMap { f =>
        val cleanLines = f.getLines().toList
          .map(_.trim)
          .filter(l => l.nonEmpty && !l.startsWith("#"))

        val credentials: Map[String, String] =
          cleanLines.flatMap { l =>
            val values = l.split("=").map(_.trim)
            for {
              key <- values.lift(0)
              value <- values.lift(1)
            } yield key -> value
          }(breakOut)

        for {
          accessKey <- credentials.get("accessKey")
          secretKey <- credentials.get("secretKey")
        } yield {
          val region = credentials.get("region")
            .map(awscala.Region.apply)
            .getOrElse(awscala.Region.EU_WEST_1)
          S3(Credentials(accessKey, secretKey))(region)
        }
      }
    } catch {
      case NonFatal(e) =>
        None
    } finally {
      sourceOpt.foreach(_.close())
    }

  }

}

object S3cHandler {

  private object S3URLStreamHandlerFactory extends URLStreamHandlerFactory {
    def createURLStreamHandler(protocol: String): URLStreamHandler = protocol match {
      case "s3c" => new S3cHandler()
      case _ => null
    }
  }

  def setupS3Handler() = URL.setURLStreamHandlerFactory(S3URLStreamHandlerFactory)

}
