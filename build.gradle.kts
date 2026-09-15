plugins {
    java
    application
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "com.beatoraja"
version = "1.5.0"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

application {
    mainClass.set("com.beatoraja.screenshot.Main")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.2")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.17.2")
    implementation("org.xerial:sqlite-jdbc:3.46.1.3")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
}

tasks.test {
    useJUnitPlatform()
}

tasks.jar {
    enabled = false
    manifest {
        attributes["Main-Class"] = "com.beatoraja.screenshot.Main"
    }
}

tasks.shadowJar {
    archiveBaseName.set("beatoraja-screenshot-manager")
    archiveClassifier.set("")
    archiveVersion.set(version.toString())
    mergeServiceFiles()
    manifest {
        attributes["Main-Class"] = "com.beatoraja.screenshot.Main"
        attributes["Implementation-Version"] = version.toString()
    }
}

tasks.named("build") {
    dependsOn(tasks.shadowJar)
}

tasks.distZip {
    dependsOn(tasks.shadowJar)
}

tasks.distTar {
    dependsOn(tasks.shadowJar)
}

tasks.startScripts {
    dependsOn(tasks.shadowJar)
}

tasks.register<Exec>("jpackageApp") {
    dependsOn(tasks.shadowJar)
    group = "distribution"
    description = "Create Windows app-image with bundled JRE (requires JDK 17+ with jpackage)"

    val shadowJar = tasks.shadowJar.get().archiveFile.get().asFile
    val outputDir = layout.buildDirectory.dir("jpackage").get().asFile

    doFirst {
        outputDir.mkdirs()
    }

    val iconFile = project.file("packaging/app-icon.ico")

    commandLine(
        "jpackage",
        "--type", "app-image",
        "--name", "beatoraja-screenshot-manager",
        "--app-version", version.toString(),
        "--input", shadowJar.parentFile.absolutePath,
        "--main-jar", shadowJar.name,
        "--main-class", "com.beatoraja.screenshot.Main",
        "--dest", outputDir.absolutePath,
        "--java-options", "-Dapp.dir=\$APP_DIR",
        "--icon", iconFile.absolutePath,
        "--win-console"
    )
}
