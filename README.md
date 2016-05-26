## S3 Coursier Plugin

[coursier](https://github.com/alexarchambault/coursier) plugin adding support for s3 dependency resolution.

For compatibility purposes with [fm-sbt-s3-resolver](https://github.com/frugalmechanic/fm-sbt-s3-resolver),
urls use `s3c://` instead of `s3://`.

### Credentials

* Environment

```sh
AWS_ACCESS_KEY_ID="myKey"
AWS_SECRET_ACCESS_KEY="myVeryS3cret"
AWS_DEFAULT_REGION="EU_WEST_1"
```

* File

> File named `.s3credentials` can be placed in one of the following locations: `current directory`, `$HOME`, `$HOME/.sbt`, `$HOME/.coursier`

```ini
# Credentials
accessKey = myKey
secretKey = myVeryS3cret

# Region
region = EU_WEST_1
```

### Usage

1. Add the plugin as a library dependency in `project/plugins.sbt`

    > NO VERSION RELEASED YET, do `sbt +publishLocal` for test purposes

    ```sbt
    libraryDependencies += "com.rtfpessoa" %% "coursier-s3" % "0.1.0-SNAPSHOT"
    ```

2. Setup support for `s3c` urls

    > This step is required to add support for `s3c` URLs in the JVM

    * **Option 1** - create object in `project/Common.scala`

    ```scala
    import coursier.cache.protocol.S3cHandler

    object Common {
        S3cHandler.setupS3Handler()
    }
    ```

    * **Option 2** -  add it to `build.sbt`

    ```scala
    import coursier.cache.protocol.S3cHandler
    S3cHandler.setupS3Handler()
    ```

3. Add s3 resolvers, without or with ivy patterns (use `s3c` to prefix the url)

    ```sbt
    resolvers += "S3 resolver" at "s3c://s3-eu-west-1.amazonaws.com/private.mvn.example.com"
    ```

    ```sbt
    resolvers += Resolver.url("S3 resolver", url("s3c://s3-eu-west-1.amazonaws.com/private.mvn.example.com"))(Resolver.defaultIvyPatterns)
    ```
