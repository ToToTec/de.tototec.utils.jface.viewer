import org.sonatype.maven.polyglot.scala.model._
import scala.collection.immutable.Seq

Model(
  gav = "de.tototec" % "de.tototec.utils.jface.viewer" % "0.1.1-SNAPSHOT",
  description = "Utility classes to work with SWT/JFace Viewer API",
  packaging = "bundle",
  properties = Map(
    "project.build.sourceEncoding" -> "UTF-8",
    "maven.compiler.source" -> "1.8",
    "maven.compiler.target" -> "1.8"
  ),
  dependencies = Seq(
    "org.eclipse" % "jface" % "3.3.0-I20070606-0010",
    "org.eclipse" % "swt" % "3.3.0-v3346",
    "org.eclipse.swt.gtk.linux" % "x86_64" % "3.3.0-v3346",
    "org.eclipse.core" % "commands" % "3.2.0-I20060605-1400",
    "org.eclipse.equinox" % "common" % "3.2.0-v20060603",
    "org.slf4j" % "slf4j-api" % "1.7.25"
  ).map(_.intransitive),
  build = Build(
    plugins = Seq(
      Plugin(
        gav = "org.apache.felix" % "maven-bundle-plugin" % "3.3.0",
        extensions = true
      ),
      Plugin(
        gav = "io.github.zlika" % "reproducible-build-maven-plugin" % "0.7",
        executions = Seq(
          Execution(
            goals = Seq("strip-jar")
          )
        )
      )
    )
  )
)
