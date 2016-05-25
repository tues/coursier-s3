import sbt.Keys._

organization := "com.rtfpessoa"

name := """coursier-s3"""

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.10.6"

crossScalaVersions := Seq("2.10.6", "2.11.8")

libraryDependencies ++= Seq(
  "com.github.seratch" %% "awscala" % "0.5.5"
)

