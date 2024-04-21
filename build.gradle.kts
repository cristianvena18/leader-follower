plugins {
	kotlin("jvm") version "1.9.0"
	id("org.sonarqube") version "3.3"
	id("org.owasp.dependencycheck") version "7.3.2"
	application
	jacoco
}

group = ""
version = "1.0-SNAPSHOT"

apply(plugin = "org.sonarqube")
apply(plugin = "org.owasp.dependencycheck")

jacoco {
	toolVersion = "0.8.7"
}

repositories {
	mavenCentral()
}

application {
	mainClass.set("MainAppKt")
}

dependencies {
	/* Kotlin - Spark */
	implementation(kotlin("stdlib"))
	implementation("com.sparkjava:spark-core:2.9.4")

	/* DB - Mongo - Redis */
	implementation("org.mongodb:mongo-java-driver:3.12.11")
	implementation("redis.clients:jedis:4.3.0")

	/* Others */
	implementation("com.google.code.gson:gson:2.10")

	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.4")

	/* UniRest - Rest api libs */
	implementation ("com.konghq:unirest-java:3.13.12")

	/* logger */
	implementation("org.slf4j:slf4j-api:2.0.3")

	/* logback para kibana */
	implementation("ch.qos.logback:logback-classic:1.4.4")
	implementation("ch.qos.logback:logback-core:1.4.4")
	implementation("net.logstash.logback:logstash-logback-encoder:7.2")

	/* Qr Data Creation */
	implementation ("com.github.mvallim:emv-qrcode:0.1.2")

	// HTML to PDF lib
	implementation("com.itextpdf:html2pdf:2.1.6")

	implementation("com.google.zxing:core:3.4.1")
	implementation("com.google.zxing:javase:3.4.1")
	implementation("org.apache.xmlgraphics:batik-transcoder:1.14")

	/* AWS SQS */

	implementation("aws.sdk.kotlin:sqs:1.0.12")
	implementation("aws.sdk.kotlin:sns:1.0.0")
	implementation("aws.sdk.kotlin:secretsmanager:1.0.17")
	implementation("aws.smithy.kotlin:http-client-engine-okhttp:0.30.0")
	implementation("aws.smithy.kotlin:http-client-engine-crt:0.30.0")

	/* test */
	testImplementation("org.junit.jupiter:junit-jupiter:5.9.1")
	testImplementation("io.mockk:mockk:1.13.2")
	testImplementation("org.assertj:assertj-core:3.23.1")

	testImplementation ("org.mockito:mockito-inline:3.12.4")
	testImplementation ("org.mockito:mockito-junit-jupiter:3.12.4")

	/* coverage para sonarqube */
	testImplementation("org.jacoco:org.jacoco.agent:0.8.8")




}

tasks.test {
	useJUnitPlatform()
	
	testLogging {
		events("passed", "skipped", "failed")
	}

	finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
	reports {
		xml.isEnabled = true
		html.isEnabled = true
		html.destination = file("${buildDir}/target")
	}

	dependsOn(tasks.test)
}

tasks.jacocoTestCoverageVerification {
	violationRules {
		rule {
			limit {
				minimum = "0.7".toBigDecimal()
			}
		}
	}
}

val testCoverage by tasks.registering {
	group = "verification"
	description = "Runs the unit tests with coverage."

	dependsOn(":test", ":jacocoTestReport", ":jacocoTestCoverageVerification")
	val jacocoTestReport = tasks.findByName("jacocoTestReport")
	jacocoTestReport?.mustRunAfter(tasks.findByName("test"))
	tasks.findByName("jacocoTestCoverageVerification")?.mustRunAfter(jacocoTestReport)
}

tasks.jar {
	// jar base name "app" + [Version] + ".jar"
	project.setProperty("archivesBaseName", "app")
	duplicatesStrategy = DuplicatesStrategy.EXCLUDE

	manifest {
		attributes["Main-Class"] = "MainAppKt"
	}

	from(configurations.runtimeClasspath.get().onEach {
		println("add from dependencies: ${it.name}")
	}.map {
		if (it.isDirectory) it else zipTree(it)
	})

	val sourcesMain = sourceSets.main.get()
	sourcesMain.allSource.forEach {
		println("add from sources: ${it.name}")
	}
	from(sourcesMain.output)
}
