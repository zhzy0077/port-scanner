name := "port_scan"

version := "1.0"

scalaVersion := "2.12.1"

resolvers += Resolver.url("jb-bintray", url("http://dl.bintray.com/jetbrains/sbt-plugins"))(Resolver.ivyStylePatterns)

jfxSettings

JFX.mainClass := Some("com.example.Main")