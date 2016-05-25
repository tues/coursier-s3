package coursier.cache.protocol

import java.io.InputStream
import java.net.{URL, URLConnection, URLStreamHandler}

import awscala.Credentials
import awscala.s3.{Bucket, S3, S3Object}
import com.amazonaws.services.s3.model.GetObjectRequest

/*
 * URL Supported:
 * s3://s3-<region>.amazonaws.com/<bucket-name>
 */

class S3Handler extends URLStreamHandler {

  override def openConnection(url: URL): URLConnection = {
    new URLConnection(url) {
      override def getInputStream: InputStream = {
        // Keys
        for {
          accessKey <- sys.env.get("AWS_ACCESS_KEY_ID")
          secretKey <- sys.env.get("AWS_SECRET_ACCESS_KEY")
        } yield {
          // Region
          val region = sys.env.get("AWS_DEFAULT_REGION")
            .map(awscala.Region.apply)
            .getOrElse(awscala.Region.EU_WEST_1)

          val subPaths = url.getPath.stripPrefix("/").split("/")

          // Bucket
          val bucketName = subPaths.head

          // Key
          val key = subPaths.tail.mkString("/")

          val bucket = Bucket(bucketName)
          val s3Client = S3(Credentials(accessKey, secretKey))(region)

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

}
