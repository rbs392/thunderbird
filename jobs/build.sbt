name := "jobs"

version := "1.0"

scalaVersion := "2.11.11"

lazy val thunderbird = ( project in file(".") )
  .settings(
    name := "thunderbird_jobs",
    organization := "",
    libraryDependencies ++= Seq(
      "org.apache.spark" %% "spark-core" % "2.2.0" % "provided"
    ),
    resolvers ++= Seq(
      "conjars repo" at "http://conjars.org/repo/",
      "maven central" at "http://central.maven.org/maven2/"
    )
  )
        