lazy val yabtModule: Seq[Setting[?]] = Seq(
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

lazy val scalaModule: Seq[Setting[?]] = yabtModule ++ Seq(
  scalaVersion := "3.3.1"
)

lazy val root = (project in file("."))
  .settings(yabtModule)
  .settings(
    name := "yet-another-build-tool"
  )
  .aggregate(domain, api, implementation, core, app)

lazy val domain = project
  .in(file("domain"))
  .settings(scalaModule)

lazy val api = project
  .in(file("api"))
  .settings(yabtModule)
  .aggregate(projectResolverApi, taskApi, dependencyApi)

lazy val projectResolverApi = project
  .in(file("api/project-resolver"))
  .settings(scalaModule)
  .settings(name := "project-resolver")
  .dependsOn(domain)

lazy val taskApi = project
  .in(file("api/task-api"))
  .settings(scalaModule)
  .settings(name := "task-api")
  .dependsOn(domain)

lazy val dependencyApi = project
  .in(file("api/dependency-api"))
  .settings(scalaModule)
  .settings(name := "dependency-api")
  .dependsOn(domain)

lazy val implementation = project
  .in(file("implementation"))
  .settings(yabtModule)
  .aggregate(yamlProjectResolver)

lazy val yamlProjectResolver = project
  .in(file("implementation/yaml-project-resolver"))
  .settings(scalaModule)
  .settings(
    name := "yaml-project-resolver",
    libraryDependencies += "io.circe" %% "circe-yaml" % "0.15.1",
    libraryDependencies += "io.circe" %% "circe-generic" % "0.14.6",
    libraryDependencies += "io.circe" %% "circe-parser" % "0.14.6",
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.17" % Test
  )
  .dependsOn(projectResolverApi)

lazy val sequentialTaskEvaluator = project
  .in(file("implementation/sequential-task-evaluator"))
  .settings(scalaModule)
  .settings(name := "sequential-task-evaluator")
  .dependsOn(taskApi)

lazy val jvm = project
  .in(file("implementation/jvm"))
  .settings(scalaModule)
  .settings(
    libraryDependencies ++= Seq(
      "org.scala-sbt" %% "zinc" % "1.9.6" cross CrossVersion.for3Use2_13,
      "io.get-coursier" %% "coursier" % "2.1.9" cross CrossVersion.for3Use2_13
    )
  )
  .dependsOn(taskApi, dependencyApi)

lazy val core = project
  .in(file("core"))
  .settings(scalaModule)
  .dependsOn(projectResolverApi, taskApi)

lazy val app = project
  .in(file("app"))
  .settings(scalaModule)
  .dependsOn(core, yamlProjectResolver, sequentialTaskEvaluator, jvm)

lazy val testModule = project
  .in(file("test-module"))
  .settings(scalaModule)
  .settings(name := "test-module")
