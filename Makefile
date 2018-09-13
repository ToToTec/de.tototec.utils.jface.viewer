.PHONY: help # List of targets with descriptions
help:
	@grep '^.PHONY: .* #' Makefile | sed 's/\.PHONY: \(.*\) # \(.*\)/\1\t\2/' | expand -t20

.PHONY: eclipse # Generate Eclipse project files
eclipse: pom.xml
	mvn initialize de.tototec:de.tobiasroeser.eclipse-maven-plugin:0.1.1:eclipse

.PHONY: pom-xml # Generate pom.xml from pom.scala
pom-xml: pom.xml

pom.xml: pom.scala
	mvn io.takari.polyglot:polyglot-translate-plugin:0.3.1:translate -Dinput=pom.scala -Doutput=pom.xml

.PHONY: clean # Clean the target directory
clean:
	-rm -rf target

.PHONY: build # Build the project (mvn install)
build:
	mvn install

.PHONY: clean-pom-xml # Remove the generated pom.xml
clean-pom-xml:
	-rm -rf pom.xml

.PHONY: clean-eclipse # Clean the eclipse settings files
clean-eclipse:
	-rm .project .classpath .settings/*

.PHONY: clean-all # Clean all generated and built files
clean-all: clean clean-pom-xml clean-eclipse
