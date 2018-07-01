import com.jfrog.bintray.gradle.*

plugins {
	java
	jacoco
	`maven-publish`
	kotlin("jvm") version "1.2.50" apply false
	id("com.jfrog.bintray") version "1.7.3"
}

var isCI: Boolean by extra
isCI = !System.getenv("CI").isNullOrBlank()

allprojects {
	group = "org.ice1000.textseq"
	version = "v0.1"

	apply {
		plugin("java")
		plugin("jacoco")
	}

	repositories {
		mavenCentral()
		jcenter()
	}

	tasks.withType<JavaCompile> {
		sourceCompatibility = "1.8"
		targetCompatibility = "1.8"
		options.apply {
			isDeprecation = true
			isWarnings = true
			isDebug = !isCI
			compilerArgs.add("-Xlint:unchecked")
		}
	}

	val sourcesJar = task<Jar>("sourcesJar") {
		group = tasks["jar"].group
		from(java.sourceSets["main"].allSource)
		classifier = "sources"
	}

	artifacts { add("archives", sourcesJar) }
}

subprojects {
	apply {
		plugin("maven")
		plugin("maven-publish")
		plugin("com.jfrog.bintray")
	}

	bintray {
		user = "ice1000"
		key = findProperty("key").toString()
		setConfigurations("archives")
		pkg.apply {
			name = rootProject.name
			repo = "ice1000"
			githubRepo = "ice1000/text-sequence"
			publicDownloadNumbers = true
			vcsUrl = "https://github.com/ice1000/text-sequence.git"
			version.apply {
				vcsTag = "${project.version}"
				name = vcsTag
				websiteUrl = "https://github.com/ice1000/text-sequence/releases/tag/$vcsTag"
			}
		}
	}

	publishing {
		(publications) {
			"mavenJava"(MavenPublication::class) {
				from(components["java"])
				groupId = project.group.toString()
				artifactId = "${rootProject.name}-${project.name}"
				version = project.version.toString()
				artifact(tasks["sourcesJar"])
				pom.withXml {
					val root = asNode()
					root.appendNode("description", "Text sequence data structures")
					root.appendNode("name", project.name)
					root.appendNode("url", "https://github.com/ice1000/text-sequence")
					root.children().last()
				}
			}
		}
	}
}

task<JacocoReport>("codecov") {
	executionData(fileTree(project.rootDir.absolutePath).include("**/build/jacoco/*.exec"))
	subprojects.forEach { sourceSets(it.java.sourceSets["main"]) }
	reports {
		xml.isEnabled = true
		xml.destination = file("$buildDir/reports/jacoco/report.xml")
		html.isEnabled = false
		csv.isEnabled = false
	}
	onlyIf { true }
	doFirst {
		subprojects.filter { it.pluginManager.hasPlugin("java") }.forEach { subproject ->
			additionalSourceDirs(files(subproject.java.sourceSets["main"].allJava.srcDirs as Set<File>))
			additionalClassDirs(subproject.java.sourceSets["main"].output as FileCollection)
			if (subproject.pluginManager.hasPlugin("jacoco")) {
				val jacocoTestReport: JacocoReport by subproject.tasks
				executionData(jacocoTestReport.executionData)
			}
		}
		executionData = files(executionData.filter { it.exists() })
	}
	dependsOn(*subprojects.map { it.tasks["test"] }.toTypedArray())
}
