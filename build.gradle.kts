plugins {
    java
    application
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "com.beatoraja"
version = "2.0.3"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

// javac otherwise reads .java source files using the build machine's platform default
// charset (JDK 17 predates JEP 400's UTF-8-by-default). The source files here are UTF-8
// with embedded Japanese text, so building on a non-UTF-8 default locale (e.g. the
// windows-latest GitHub Actions runner) silently corrupts every Japanese string literal
// baked into the compiled classes, without failing the build.
tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

application {
    mainClass.set("com.beatoraja.screenshot.Main")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.formdev:flatlaf:3.6")
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
        // $APPDIR is the "app" subfolder (containing the jar/cfg only) - the launcher
        // .exe and the bundled tools/ folder live one level up, in $BINDIR (verified by
        // logging both at runtime against an actual jpackage app-image; $APPDIR would
        // silently break update-checking and the bundled clix.exe lookup).
        "--java-options", "-Dapp.dir=\$BINDIR",
        "--java-options", "-Dfile.encoding=UTF-8",
        "--icon", iconFile.absolutePath,
        "--win-console",
        // --add-modules replaces jpackage's automatic jdeps-based module detection
        // rather than adding to it, so the full required set must be listed explicitly
        // (verified via `jdeps --print-module-deps` against the shadow jar) plus
        // jdk.localedata, which jdeps never reports since it's a resource-only module
        // with no bytecode dependency - without it the bundled runtime's Japanese
        // font/locale resolution silently falls back to a CJK-incapable font.
        "--add-modules", "java.base,java.desktop,java.net.http,java.sql,jdk.localedata"
    )
}
