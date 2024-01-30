ThisBuild / version := "0.1.0-SNAPSHOT"

lazy val root = (project in file("."))
  .settings(
    name := "yet-another-build-tool"
  )
  .aggregate(domain, api, integration)

lazy val domain = project
  .in(file("domain"))
  .settings(scalaVersion := "3.3.1")

lazy val api = project
  .in(file("api"))
  .aggregate(projectProviderApi)

lazy val projectProviderApi = project
  .in(file("api/project-provider"))
  .settings(name := "project-provider", scalaVersion := "3.3.1", scalacOptions ++= Seq("-Xmax-inlines", "100"))
  .dependsOn(domain)

lazy val integration = project
  .in(file("integration"))
  .aggregate(yamlProjectProvider)

lazy val yamlProjectProvider = project
  .in(file("integration/yaml-project-provider"))
  .settings(
    name := "yaml-project-provider",
    scalaVersion := "3.3.1",
    libraryDependencies += "io.circe" %% "circe-yaml" % "0.15.1",
    libraryDependencies += "io.circe" %% "circe-generic" % "0.14.6",
    libraryDependencies += "io.circe" %% "circe-parser" % "0.14.6"
  )
  .dependsOn(projectProviderApi)
