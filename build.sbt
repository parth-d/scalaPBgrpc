name := "scalaPBgrpc"

version := "0.1"

scalaVersion := "2.13.7"

Compile / PB.targets := Seq(
  scalapb.gen() -> (Compile / sourceManaged).value / "scalapb"
)

/** Versioning info */
val akkaVersion = "2.6.17"
val akkaHttpVersion = "10.2.6"
val scalacticVersion = "3.2.9"

/** Library imports */
libraryDependencies ++= Seq(
  "com.typesafe.akka"     %% "akka-stream"            % akkaVersion,
  "com.typesafe.akka"     %% "akka-http"              % akkaHttpVersion,
  "com.typesafe.akka"     %% "akka-http-spray-json"   % akkaHttpVersion,
  "com.thesamet.scalapb"  %% "scalapb-runtime"        % scalapb.compiler.Version.scalapbVersion   % "protobuf",
  "com.thesamet.scalapb"  %% "scalapb-runtime-grpc"   % scalapb.compiler.Version.scalapbVersion,
  "org.scalactic"         %% "scalactic"              % scalacticVersion,
  "org.scalatest"         %% "scalatest"              % scalacticVersion                           % Test,
  "org.scalatest"         %% "scalatest-featurespec"  % scalacticVersion                           % Test,
  "io.grpc"                % "grpc-netty"             % scalapb.compiler.Version.grpcJavaVersion
)