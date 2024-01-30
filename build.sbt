lazy val yabtModule: Seq[Setting[_]] = Seq(
  organization := "com.github.tammo",
  version := "0.1.0-SNAPSHOT",
  developers += Developer(
    "tammo-steffens",
    "Tammo Steffens",
    "",
    url("https://github.com/Tammo0987")
  ),
  licenses := Seq(
    "MIT License" -> url(
      "https://raw.githubusercontent.com/Tammo0987/yet-another-build-tool/main/LICENSE"
    )
  )
)

lazy val scalaModule: Seq[Setting[_]] = yabtModule ++ Seq(
  scalaVersion := "3.3.1"
)

lazy val root = (project in file("."))
  .settings(yabtModule)
  .settings(
    name := "yet-another-build-tool"
  )
  .aggregate(domain, api, integration)

lazy val domain = project
  .in(file("domain"))
  .settings(scalaModule)

lazy val api = project
  .in(file("api"))
  .settings(yabtModule)
  .aggregate(projectResolverApi)

lazy val projectResolverApi = project
  .in(file("api/project-resolver"))
  .settings(scalaModule)
  .settings(name := "project-resolver")
  .dependsOn(domain)

lazy val integration = project
  .in(file("integration"))
  .settings(yabtModule)
  .aggregate(yamlProjectProvider)

lazy val yamlProjectProvider = project
  .in(file("integration/yaml-project-provider"))
  .settings(scalaModule)
  .settings(
    name := "yaml-project-provider",
    libraryDependencies += "io.circe" %% "circe-yaml" % "0.15.1",
    libraryDependencies += "io.circe" %% "circe-generic" % "0.14.6",
    libraryDependencies += "io.circe" %% "circe-parser" % "0.14.6"
  )
  .dependsOn(projectResolverApi)
