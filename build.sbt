sourceDirectory := file("dummy source directory")

scalaVersionSettings

// When bumping to 1.14.1, remember to set mimaPreviousArtifacts to 1.14.0
lazy val versionNumber = "1.14.0"

lazy val isRelease = true

lazy val travisCommit = Option(System.getenv().get("TRAVIS_COMMIT"))

lazy val scalaVersionSettings = Seq(
  scalaVersion := "2.13.0-M4-pre-20d3c21"
)

lazy val sharedSettings = MimaSettings.settings ++ scalaVersionSettings ++ Seq(

  name := "scalacheck",

  version := {
    val suffix = "-newCollections"
    versionNumber + suffix
  },

  isSnapshot := !isRelease,

  organization := "org.scala-lang.modules",

  licenses := Seq("BSD-style" -> url("http://www.opensource.org/licenses/bsd-license.php")),

  homepage := Some(url("http://www.scalacheck.org")),

  credentials ++= (for {
    username <- Option(System.getenv().get("SONATYPE_USERNAME"))
    password <- Option(System.getenv().get("SONATYPE_PASSWORD"))
  } yield Credentials(
    "Sonatype Nexus Repository Manager",
    "oss.sonatype.org",
    username, password
  )).toSeq,

  unmanagedSourceDirectories in Compile += (baseDirectory in LocalRootProject).value / "src" / "main" / "scala",

  unmanagedSourceDirectories in Test += (baseDirectory in LocalRootProject).value / "src" / "test" / "scala",

  resolvers += "sonatype" at "https://oss.sonatype.org/content/repositories/releases",

  javacOptions += "-Xmx1024M",

  scalacOptions ++= Seq(
    "-deprecation",
    "-encoding", "UTF-8",
    "-feature",
    "-unchecked",
//    "-Xfatal-warnings",
    "-Xfuture",
    "-Yno-adapted-args",
    "-Ywarn-dead-code",
    "-Ywarn-inaccessible",
    "-Ywarn-nullary-override",
    "-Ywarn-nullary-unit",
    "-Ywarn-numeric-widen") ++ {
    scalaBinaryVersion.value match {
      case "2.10" => Seq("-Xlint")
      case "2.11" => Seq("-Xlint", "-Ywarn-infer-any", "-Ywarn-unused-import")
      case _      => Seq("-Xlint:-unused", "-Ywarn-infer-any", "-Ywarn-unused:imports,-patvars,-implicits,-locals,-privates")
    }
  },

  // HACK: without these lines, the console is basically unusable,
  // since all imports are reported as being unused (and then become
  // fatal errors).
  scalacOptions in (Compile, console) ~= {_.filterNot("-Ywarn-unused-import" == _)},
  scalacOptions in (Test, console) := (scalacOptions in (Compile, console)).value,

  // don't use fatal warnings in tests
  scalacOptions in Test ~= (_ filterNot (_ == "-Xfatal-warnings")),

  //mimaPreviousArtifacts := (
  //  if (CrossVersion isScalaApiCompatible scalaVersion.value)
  //    Set("org.scalacheck" %%% "scalacheck" % "1.14.0")
  //  else
  //    Set.empty
  //),

  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    val (name, path) = if (isSnapshot.value) ("snapshots", "content/repositories/snapshots")
                       else ("releases", "service/local/staging/deploy/maven2")
    Some(name at nexus + path)
  },

  publishMavenStyle := true,

  // Travis should only publish snapshots
  publishArtifact := !(isRelease && travisCommit.isDefined),

  publishArtifact in Test := false,

  pomIncludeRepository := { _ => false },

  pomExtra := {
    <scm>
      <url>https://github.com/rickynils/scalacheck</url>
      <connection>scm:git:git@github.com:rickynils/scalacheck.git</connection>
    </scm>
    <developers>
      <developer>
        <id>rickynils</id>
        <name>Rickard Nilsson</name>
      </developer>
    </developers>
  }
)

lazy val jvm = project.in(file("jvm"))
  .settings(sharedSettings: _*)
  .settings(
    libraryDependencies += "org.scala-sbt" %  "test-interface" % "1.0"
  )
