## S3 Coursier Plugin

[Coursier](https://github.com/alexarchambault/coursier) plugin adding support for S3 dependency resolution. The main purpose of this fork is to enable S3 resolution in Pants 1.27.0+. Also, the original 0.1.0 doesn't work with Coursier 1.1.0-M14 (used by Pants 1.27.0).

This fork uses standard `s3://` scheme unlike the original (which uses `s3c://`), so it's probably not compatible with [fm-sbt-s3-resolver](https://github.com/frugalmechanic/fm-sbt-s3-resolver).

### Credentials

* Environment

```sh
AWS_ACCESS_KEY_ID="myKey"
AWS_SECRET_ACCESS_KEY="myVeryS3cret"
AWS_DEFAULT_REGION="eu-east-1"
```

* File

> File named `.s3credentials` can be placed in one of the following locations: `current directory`, `$HOME`, `$HOME/.sbt`, `$HOME/.coursier`

```ini
# Credentials
accessKey = myKey
secretKey = myVeryS3cret

# Region
region = eu-east-1
```

### Usage

```shell
$ coursier bootstrap coursier:1.1.0-M14 rtfpessoa:coursier-s3_2.12:0.2.0-SNAPSHOT --assembly -o coursier-1.1.0-M14-s3.sh
$ tail -c +458 coursier-1.1.0-M14-s3.sh > coursier-1.1.0-M14-s3.jar
```

Now you can tell Pants to use your custom Coursier version instead of the official one:

```ini
[coursier]
repos = """
+[
    's3://s3.amazonaws.com/your-bucket/some/path',
  ]
"""
bootstrap_jar_urls = ['file:///path/to/coursier-1.1.0-M14-s3.jar']
```

You may need to `rm "$HOME/.cache/pants/bin/coursier/1.1.0.cf365ea27a710d5f09db1f0a6feee129aa1fc417/coursier` if Pants has already downloaded the official JAR.
