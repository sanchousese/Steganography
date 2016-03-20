import _root_.sbt.Keys._
import _root_.sbt._
import android.Dependencies._
import android.Keys._
import android.Dependencies.{LibraryDependency, aar}

android.Plugin.androidBuild

platformTarget in Android := "android-21"

name := "macroid-starter"

scalaVersion := "2.11.6"

run <<= run in Android

resolvers ++= Seq(
  Resolver.sonatypeRepo("releases"),
  "jcenter" at "http://jcenter.bintray.com"
)

scalacOptions in (Compile, compile) ++=
  (dependencyClasspath in Compile).value.files.map("-P:wartremover:cp:" + _.toURI.toURL)

scalacOptions in (Compile, compile) ++= Seq(
  "-P:wartremover:traverser:macroid.warts.CheckUi"
)

javacOptions ++= Seq("-source", "1.7", "-target", "1.7")
scalacOptions ++= Seq("-feature", "-deprecation", "-target:jvm-1.7")

libraryDependencies ++= Seq(
  "com.fortysevendeg" % "macroid-extras_2.11" % "0.1",
  "com.squareup.retrofit" % "retrofit" % "1.9.0",
  "com.squareup.okhttp" % "okhttp" % "2.5.0",
  "com.android.support" % "multidex" % "1.0.0",
  aar("org.macroid" %% "macroid" % "2.0.0-M4"),
  aar("org.macroid" %% "macroid-viewable" % "2.0.0-M4"),
  aar("com.android.support" % "support-v4" % "21.0.3"),  // implicit in picasso there is some error because
  aar("com.android.support" % "appcompat-v7" % "22.2.1"),
  aar("com.android.support" % "design" % "22.2.1"),
  compilerPlugin("org.brianmckenna" %% "wartremover" % "0.10")
)

libraryDependencies += "org.apmem.tools" % "layouts" % "1.10"

proguardScala in Android := true

proguardOptions in Android ++= Seq(
  "-ignorewarnings",
  "-keep class scala.Dynamic"
)
