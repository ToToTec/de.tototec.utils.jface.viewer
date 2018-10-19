// build.sc
import mill._
import mill.scalalib._
import mill.scalalib.publish._
import ammonite.ops._
import $ivy.`de.tototec::de.tobiasroeser.mill.osgi:0.0.2`
import de.tobiasroeser.mill.osgi._

object viewer
  extends JavaModule
  with OsgiBundleModule
  with PublishModule {

  def millSourcePath = super.millSourcePath / up / 'src /'main

  def sources = T.sources(millSourcePath / 'java)
  def resources = T.sources(millSourcePath / 'resources)

  def artifactName = "de.tototec.utils.jface.viewer"
  def publishVersion = "0.1.1-SNAPSHOT"

  def ivyDeps = Agg(
    ivy"org.eclipse:jface:3.3.0-I20070606-0010",
    ivy"org.eclipse:swt:3.3.0-v3346",
    ivy"org.eclipse.swt.gtk.linux:x86_64:3.3.0-v3346",
    ivy"org.eclipse.core:commands:3.2.0-I20060605-1400",
    ivy"org.eclipse.equinox:common:3.2.0-v20060603",
    ivy"org.slf4j:slf4j-api:1.7.25"
  )

  def osgiHeaders = T{
    super.osgiHeaders().copy(
      `Export-Package` = Seq(bundleSymbolicName())
    )
  }

  def includeResource = T{ super.includeResource() ++ Seq("README.adoc", "LICENSE.txt") }

  def includeSources = true

  def pomSettings = PomSettings(
    description = "Utility classes to work with SWT/JFace Viewer API",
    organization = "de.tototec",
    url = "https://github.com/ToToTec/de.tototec.utils.jface.viewer",
    licenses = Seq(License.`Apache-2.0`.copy(url = "https://www.apache.org/licenses/LICENSE-2.0")),
    versionControl = VersionControl.github("ToToTec", "de.tototec.utils.jface.viewer"),
    developers = Seq(Developer("lefou", "Tobias Roeser", "https://github.com/lefou"))
  )

  /** Publish to the local Maven repository */
  def publishM2Local(path: Path = home / ".m2" / "repository") = T.command {
    new LocalM2Publisher(path)
      .publish(
        jar = jar().path,
        sourcesJar = sourceJar().path,
        docJar = docJar().path,
        pom = pom().path,
        artifact = artifactMetadata()
      )
  }

}

class LocalM2Publisher(m2Repo: Path) {

  def publish(
    jar: Path,
    sourcesJar: Path,
    docJar: Path,
    pom: Path,
    artifact: Artifact
  ): Unit = {
    println("Publishing to " + m2Repo)
    val releaseDir = m2Repo / artifact.group.split("[.]") / artifact.id / artifact.version
    writeFiles(
      jar -> releaseDir / s"${artifact.id}-${artifact.version}.jar",
      sourcesJar -> releaseDir / s"${artifact.id}-${artifact.version}-sources.jar",
      docJar -> releaseDir / s"${artifact.id}-${artifact.version}-javadoc.jar",
      pom -> releaseDir / s"${artifact.id}-${artifact.version}.pom"
    )
  }

  private def writeFiles(fromTo: (Path, Path)*): Unit = {
    fromTo.foreach {
      case (from, to) =>
        mkdir(to / up)
        cp.over(from, to)
    }
  }

}

