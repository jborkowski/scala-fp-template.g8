lazy val root = (project in file(".")).
  settings(
    commonSettings,
    consoleSettings,
    compilerOptions,
    typeSystemEnhancements,
    betterMonadicFor,
    dependencies,
    tests
  )

addCommandAlias("fmt", ";scalafmt ;test:scalafmt ;it:scalafmt")

lazy val commonSettings = Seq(
  name := "$name;format="lower,word"$",
  organization := "$organization;format="lower,wors"$",
  scalaVersion := "2.12.7"
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
  addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.9")

lazy val betterMonadicFor =
  addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.2.4")

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

  $if(doobieenabled.truthy)$
  val doobie = dep("org.tpolecat")("$doobieVersion$")(
    "doobie-core",
    "doobie-h2",               // H2 driver 1.4.197 + type mappings.
    "doobie-hikari",           // HikariCP transactor.
    "doobie-postgres",          // Postgres driver 42.2.5 + type mappings.
  ) :+ ("org.flywaydb"   % "flyway-core" % "5.2.0")
  $endif$

  $if(http4senabled.truthy)$
  val http4s = dep("org.http4s")("$http4sVersion$")(
    "http4s-dsl",
    "http4s-blaze-server",
    "http4s-blaze-client"
  )
  $endif$

  $if(circeenabled.truthy)$
  val circe = dep("io.circe")("$circeVersion$")(
    "circe-core",
    "circe-generic",
    "circe-parser"
  )
  $endif$

  val mixed = Seq(
    "com.github.pureconfig" %% "pureconfig"       % "0.10.0",
    "ch.qos.logback"       %  "logback-classic" % "1.2.3",
    "com.olegpy"           %% "meow-mtl"        % "0.1.1"
  )

  def extraResolvers =
    resolvers ++= Seq(
      Resolver.sonatypeRepo("releases"),
      Resolver.sonatypeRepo("snapshots")
    )

  val deps =
    libraryDependencies ++= Seq(
      fs2,
      $if(http4senabled.truthy)$
      http4s,
      $endif$
      $if(doobieenabled.truthy)$
      doobie,
      $endif$
      $if(circeenabled.truthy)$
      circe,
      $endif$
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
      $if(doobieenabled.truthy)$
      "org.tpolecat" %% "$doobieVersion$" % "doobie-scalatest",
      $endif$
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
