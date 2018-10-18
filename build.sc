// build.sc
import mill._
import mill.scalalib._
import mill.scalalib.publish._
import ammonite.ops._
import $ivy.`de.tototec::de.tobiasroeser.mill.osgi:0.0.2-SNAPSHOT`
import de.tobiasroeser.mill.osgi._

object viewer
  extends MavenModule
  with OsgiBundleModule
  with PublishModule {

  def millSourcePath = super.millSourcePath / up

  def artifactName = "de.tototec.utils.jface.viewer"
  def publishVersion = "0.1.0-SNAPSHOT"

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

  def includeResource = T{
    super.includeResource() ++ Seq(
      "README.adoc",
      "LICENSE.txt"
    )
  }

  def includeSources = true

  def pomSettings = PomSettings(
    description = "Utility classes to work with SWT/JFace Viewer API",
    organization = "de.tototec",
    url = "https://github.com/tototec/",
    licenses = Seq(License.`Apache-2.0`.copy(url = "http://www.apache.org/licenses/LICENSE-2.0")),
    versionControl = VersionControl.github("ToToTec", "de.tototec.utils.jface.viewer"),
    developers = Seq(
      Developer("lefou", "Tobias Roeser", "https://github.com/lefou")
    )
  )

}
