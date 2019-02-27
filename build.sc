// build.sc
import mill._
import mill.scalalib._
import mill.scalalib.publish._
import $ivy.`de.tototec::de.tobiasroeser.mill.osgi:0.0.6`
import de.tobiasroeser.mill.osgi._
import $ivy.`de.tototec::de.tobiasroeser.mill.publishM2:0.1.0`
import de.tobiasroeser.mill.publishM2._
import mill.define.Target

object viewer
  extends JavaModule
  with OsgiBundleModule
  with PublishM2Module {

  def millSourcePath = super.millSourcePath / os.up / 'src / 'main

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

  def osgiHeaders = T {
    super.osgiHeaders().copy(
      `Export-Package` = Seq(bundleSymbolicName())
    )
  }

  def includeResource = T { super.includeResource() ++ Seq("README.adoc", "LICENSE.txt") }

  def includeSources = true

  def pomSettings = PomSettings(
    description = "Utility classes to work with SWT/JFace Viewer API",
    organization = "de.tototec",
    url = "https://github.com/ToToTec/de.tototec.utils.jface.viewer",
    licenses = Seq(License.`Apache-2.0`.copy(url = "https://www.apache.org/licenses/LICENSE-2.0")),
    versionControl = VersionControl.github("ToToTec", "de.tototec.utils.jface.viewer"),
    developers = Seq(Developer("lefou", "Tobias Roeser", "https://github.com/lefou"))
  )

  override def javacOptions: Target[Seq[String]] = T {
    super.javacOptions() ++ Seq(
      "-encoding", "UTF-8",
      "-source", "8",
      "-target", "8"
    )
  }

}

