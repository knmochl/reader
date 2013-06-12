name := "My Reader"

version := "0.1"

scalaVersion := "2.10.1"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies += "com.typesafe.akka" % "akka-actor_2.10" % "2.1.2"

libraryDependencies += "com.ning" % "async-http-client" % "1.6.5"

libraryDependencies += "org.scalatest" %% "scalatest" % "1.9.1" % "test"

libraryDependencies += "org.eclipse.jetty" % "jetty-server" % "8.1.11.v20130520"

libraryDependencies += "org.eclipse.jetty" % "jetty-servlet" % "8.1.11.v20130520"

libraryDependencies += "org.eclipse.jetty" % "jetty-server" % "8.1.11.v20130520" % "test"
