name := "jobs"

version := "1.0"

scalaVersion := "2.11.11"

lazy val thunderbird = ( project in file(".") )
  .settings(
    name := "thunderbird_jobs",
    organization := "",
    libraryDependencies ++= Seq(
      "scalding-ext" %% "scalding-ext" % "3.0.23",
      "org.elasticsearch" % "elasticsearch-hadoop-cascading" % "5.6.3",
      "models" %% "models" % "3.0.167",
      "irene" %% "irene" % "4.1.54" exclude("com.twitter", "scalding-args_2.10") exclude("models", "models_2.10"),
      "commons-httpclient" % "commons-httpclient" % "3.1",
      "org.scalatest" %% "scalatest" % "3.0.4" % "test"
    ),
    resolvers ++= Seq(
      "conjars repo" at "http://conjars.org/repo/",
      "maven central" at "http://central.maven.org/maven2/",
      "indix artifactory" at "http://artifacts.indix.tv:8081/artifactory/libs-release-local/"
    ),
    assemblyMergeStrategy in assembly := {
      case PathList("META-INF", xs @ _*) =>
        (xs map {_.toLowerCase}) match {
          case ("manifest.mf" :: Nil) => MergeStrategy.discard
          case _ => MergeStrategy.filterDistinctLines
        }
      case _ => MergeStrategy.first

    }
  )
        