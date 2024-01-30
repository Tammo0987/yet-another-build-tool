package com.github.tammo.yabt.project

import com.github.tammo.yabt.Error.{CyclicReference, MissingReference}
import com.github.tammo.yabt.ResolvedProject.*
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers

class DefaultProjectVerifierTest extends AnyFlatSpecLike with Matchers {

  private val emptyProject = ResolvedProject(
    name = Name(""),
    organization = Organization(""),
    version = Version(""),
    scalaVersion = "",
    plugins = Set.empty,
    modules = Map.empty
  )

  private def emptyModule(name: String)(dependsOn: String*)(
      aggregates: String*
  ): ResolvedModule = ResolvedModule(
    name = Name(name),
    organization = Organization(""),
    version = Version(""),
    directory = "",
    scalaVersion = "",
    dependencies = Set.empty,
    dependsOn = dependsOn.toSet.map(ModuleReference(_)),
    aggregates = aggregates.toSet.map(ModuleReference(_)),
    plugins = Set.empty
  )

  extension (project: ResolvedProject) {

    def withModule(module: ResolvedModule): ResolvedProject =
      project.copy(modules = project.modules + (module.name -> module))

  }

  behavior of "verify references"

  it should "verify references without modules without an error" in {
    DefaultProjectVerifier.verifyProject(emptyProject) shouldBe Right(
      emptyProject
    )
  }

  it should "verify references with missing depends on with an error" in {
    val module = emptyModule("module")("wrong-reference")()
    val project = emptyProject.withModule(module)
    DefaultProjectVerifier.verifyProject(project) shouldBe Left(
      MissingReference("wrong-reference")
    )
  }

  it should "verify references with missing aggregate with an error" in {
    val module = emptyModule("module")()("wrong-reference")
    val project = emptyProject.withModule(module)
    DefaultProjectVerifier.verifyProject(project) shouldBe Left(
      MissingReference("wrong-reference")
    )
  }

  it should "verify references with valid modules without an error" in {
    val dependsOn = emptyModule("dependsOn")()()
    val aggregate = emptyModule("aggregate")()()
    val module = emptyModule("module")("dependsOn")("aggregate")

    val project = emptyProject
      .withModule(dependsOn)
      .withModule(aggregate)
      .withModule(module)

    DefaultProjectVerifier.verifyProject(project) shouldBe Right(project)
  }

  behavior of "verify depends on"

  it should "detect cycle for depends on relationships" in {
    val moduleA = emptyModule("module-a")("module-b")()
    val moduleB = emptyModule("module-b")("module-a")()

    val project = emptyProject
      .withModule(moduleA)
      .withModule(moduleB)

    DefaultProjectVerifier.verifyProject(project) shouldBe Left(
      CyclicReference(Seq("module-a", "module-b", "module-a"))
    )
  }

  it should "detect transitive cycles for depends on relationships" in {
    val moduleA = emptyModule("module-a")("module-b")()
    val moduleB = emptyModule("module-b")("module-c")()
    val moduleC = emptyModule("module-c")("module-a")()

    val project = emptyProject
      .withModule(moduleA)
      .withModule(moduleB)
      .withModule(moduleC)

    DefaultProjectVerifier.verifyProject(project) shouldBe Left(
      CyclicReference(Seq("module-a", "module-b", "module-c", "module-a"))
    )
  }

  behavior of "verify aggregates"

  it should "detect cycle for aggregates relationships" in {
    val moduleA = emptyModule("module-a")()("module-b")
    val moduleB = emptyModule("module-b")()("module-a")

    val project = emptyProject
      .withModule(moduleA)
      .withModule(moduleB)

    DefaultProjectVerifier.verifyProject(project) shouldBe Left(
      CyclicReference(Seq("module-a", "module-b", "module-a"))
    )
  }

  it should "detect transitive cycles for aggregates relationships" in {
    val moduleA = emptyModule("module-a")()("module-b")
    val moduleB = emptyModule("module-b")()("module-c")
    val moduleC = emptyModule("module-c")()("module-a")

    val project = emptyProject
      .withModule(moduleA)
      .withModule(moduleB)
      .withModule(moduleC)

    DefaultProjectVerifier.verifyProject(project) shouldBe Left(
      CyclicReference(Seq("module-a", "module-b", "module-c", "module-a"))
    )
  }
}
