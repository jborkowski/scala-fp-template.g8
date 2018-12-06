lazy val root = (project in file(".")).
  settings(
    commonSettings,
    consoleSettings,
    compilerOptions,
    typeSystemEnhancements,
    dependencies,
    tests
  )

addCommandAlias("fmt", ";scalafmt ;test:scalafmt ;it:scalafmt")

lazy val commonSettings = Seq(
  name := "$name;format="lower,word"$",
  organization := "$organization;format="lower,wors"$",
  scalaVersion := "2.12.7"
  //crossScalaVersions := Seq("2.11.12", "2.12.7")
)

val consoleSettings = Seq(
  initialCommands := s"import $defaultImportPath$",
  scalacOptions in (Compile, console) -= "-Ywarn-unused-import"
)


lazy val compilerOptions =
  scalacOptions ++= Seq(
    "-unchecked",
    "-deprecation",
    "-encoding",
    "utf8",
    "-target:jvm-1.8",
    "-feature",
    "-language:_",
    "-Ypartial-unification",
    "-Ywarn-unused-import",
    "-Ywarn-value-discard"
  )

lazy val typeSystemEnhancements =
  addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.3")

def dep(org: String)(version: String)(modules: String*) =
    Seq(modules:_*) map { name =>
      org %% name % version
    }

lazy val dependencies = {
  // brings in cats and cats-effect
  val fs2 = dep("co.fs2")("$fs2Version$")(
    "fs2-core",
    "fs2-io"
  )

  val http4s = dep("org.http4s")("$http4sVersion$")(
    "http4s-dsl",
    "http4s-blaze-server",
    "http4s-blaze-client"
  )

  val doobie = dep("org.tpolecat")("$doobieVersion$")(
    "doobie-core",
    // And add any of these as needed
    "doobie-h2",                // H2 driver 1.4.197 + type mappings.
    "doobie-hikari",            // HikariCP transactor.
    "doobie-postgres"          // Postgres driver 42.2.5 + type mappings.
    //"doobie-scalatest" % "test" // ScalaTest support for typechecking statements
  )

  val circe = dep("io.circe")("$circeVersion$")(
    "circe-core",
    "circe-generic",
    "circe-parser"
  )

  val mixed = Seq(
    "com.github.pureconfig" %% "pureconfig" % "0.10.0",
    "ch.qos.logback" % "logback-classic"    % "1.2.3",
    "org.flywaydb"   % "flyway-core"        % "5.2.0",
    "com.olegpy"     %% "meow-mtl"          % "0.1.1",
  )

  def extraResolvers =
    resolvers ++= Seq(
      Resolver.sonatypeRepo("releases"),
      Resolver.sonatypeRepo("snapshots")
    )

  val deps =
    libraryDependencies ++= Seq(
      fs2,
      http4s,
      doobie,
      circe,
      mixed
    ).flatten

  Seq(deps, extraResolvers)
}

lazy val tests = {
  val dependencies = {
    val scalatest = dep("org.scalatest")("$scalatestVersion$")(
       "scalatest"
    )

    val mixed = Seq(
      "org.scalacheck" %% "scalacheck" % "$scalacheckVersion$"
    )

    libraryDependencies ++= Seq(
      scalatest,
      mixed
    ).flatten.map(_ % "test")
  }

  val frameworks =
    testFrameworks := Seq(TestFrameworks.ScalaTest)

  Seq(dependencies, frameworks)
}
