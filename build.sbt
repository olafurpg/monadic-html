import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}

val scalajsdom = "1.1.0"
val scalatest  = "3.2.2"
val cats       = "2.2.0"

scalaJSLinkerConfig in ThisBuild ~= { _.withSourceMap(true) }
crossScalaVersions  in ThisBuild := Seq("2.13.3", "2.12.12")
scalaVersion        in ThisBuild := crossScalaVersions.value.head
organization        in ThisBuild := "com.geirsson"

lazy val root = project.in(file("."))
  .aggregate(
    `monadic-html`,
    `monadic-rxJS`,
    `monadic-rxJVM`,
    `monadic-rx-catsJS`,
    `monadic-rx-catsJVM`,
    `examples`
  )
  .settings(noPublishSettings: _*)

lazy val `monadic-html` = project
  .enablePlugins(ScalaJSPlugin)
  .dependsOn(`monadic-rxJS`)
  .settings(publishSettings: _*)
  .settings(testSettings: _*)
  .settings(libraryDependencies += "org.scala-js"  %%% "scalajs-dom" % scalajsdom)
  .settings(libraryDependencies += "org.scalatest" %%% "scalatest" % scalatest % Test)
  .dependsOn(`monadic-rx-catsJS` % "test->compile")

lazy val `monadic-rxJS`  = `monadic-rx`.js
lazy val `monadic-rxJVM` = `monadic-rx`.jvm
lazy val `monadic-rx`    = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Full)
  .jsSettings(publishSettings: _*)
  .jvmSettings(noPublishSettings: _*)
  .jsSettings(testSettings: _*)
  .settings(libraryDependencies += "org.scalatest" %%% "scalatest" % scalatest % Test)

lazy val `monadic-rx-catsJS`  = `monadic-rx-cats`.js
lazy val `monadic-rx-catsJVM` = `monadic-rx-cats`.jvm
lazy val `monadic-rx-cats`    = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .jsSettings(publishSettings: _*)
  .jvmSettings(noPublishSettings: _*)
  .dependsOn(`monadic-rx`)
  .settings(libraryDependencies += "org.typelevel" %%% "cats-core" % cats)

lazy val `examples` = project
  .enablePlugins(ScalaJSPlugin)
  .dependsOn(`monadic-html`, `monadic-rx-catsJS`)
  .settings(noPublishSettings: _*)
  .settings(
    test in Test := {},
    libraryDependencies += "com.github.japgolly.scalacss" %%% "core" % "0.6.1",
    artifactPath in (Compile, fastLinkJS) :=
      ((crossTarget in (Compile, fastLinkJS)).value /
        ((moduleName in fastLinkJS).value + "-opt.js")))

scalacOptions in ThisBuild := Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-feature",
  "-unchecked",
  // "-Xfatal-warnings", see Cancelable#cancel
  "-Xlint",
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard")


lazy val testSettings = Seq(
  testOptions  in Test += Tests.Argument(TestFrameworks.ScalaTest, "-oDF"),
  scalaJSStage in Test := FastOptStage,
  jsEnv        in Test := new org.scalajs.jsenv.jsdomnodejs.JSDOMNodeJSEnv())

lazy val publishSettings = Seq(
  publishMavenStyle := true,
  publishArtifact in Test := false,
  pomIncludeRepository := Function.const(false),
  licenses := Seq(("MIT", url("http://opensource.org/licenses/MIT"))),
  homepage := Some(url("https://github.com/OlivierBlanvillain/monadic-html")),
  publishMavenStyle := true,
  publishTo := {
    if (isSnapshot.value) Some("snapshots" at "https://oss.sonatype.org/content/repositories/snapshots")
    else                  Some("releases"  at "https://oss.sonatype.org/service/local/staging/deploy/maven2")
  },
  pomExtra := {
    <scm>
      <url>git@github.com:OlivierBlanvillain/monadic-html.git</url>
      <connection>scm:git:git@github.com:OlivierBlanvillain/monadic-html.git</connection>
    </scm>
    <developers>
      <developer>
        <id>OlivierBlanvillain</id>
        <name>Olivier Blanvillain</name>
        <url>https://github.com/OlivierBlanvillain/</url>
      </developer>
    </developers>
  }
)

lazy val noPublishSettings = Seq(
  publish := {},
  publishLocal := {},
  publishArtifact := false,
  skip in publish := true
)
