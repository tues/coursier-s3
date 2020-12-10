import sbt.Keys._

organization := "rtfpessoa"

name := """coursier-s3"""

version := "0.1.0"

scalaVersion := "2.12.8"

crossScalaVersions := Seq("2.11.12", "2.12.8", "2.13.3")

libraryDependencies ++= Seq(
  "com.github.seratch" %% "awscala" % "0.8.4"
)

licenses += ("MIT", url("http://opensource.org/licenses/MIT"))
