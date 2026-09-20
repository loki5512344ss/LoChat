plugins {
    java
    id("com.gradleup.shadow") version "9.0.0"
    jacoco
    checkstyle
}

group = "com.loki"
// Universal jar: compiled against Spigot 1.20.1 (lowest common denominator).
// Single jar loads on both Spigot/Mohist 1.20.1 and Paper 1.20.x-1.21.x
// because Paper is a superset of Spigot API. Adventure is bundled & relocated.
version = "1.5.5-1.20.1"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://repo.rosewooddev.io/repository/public/")
    maven("https://jitpack.io")
    maven("https://nexus.skinsrestorer.net/repository/maven-public/")
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.20.1-R0.1-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.11.6")
    compileOnly("org.black_ixx:playerpoints:3.2.7")
    compileOnly("net.luckperms:api:5.4")
    implementation("com.google.code.gson:gson:2.10.1")
    // Adventure bundled & relocated -> works on Spigot (no server Adventure) and Paper (relocated avoids clash)
    implementation("net.kyori:adventure-api:4.17.0")
    implementation("net.kyori:adventure-text-minimessage:4.17.0")
    implementation("net.kyori:adventure-text-serializer-legacy:4.17.0")
    implementation("net.kyori:adventure-text-serializer-plain:4.17.0")
    implementation("net.kyori:adventure-text-serializer-gson:4.17.0")
    implementation("net.kyori:adventure-platform-bukkit:4.3.4")
    
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.10.0")
    testImplementation("org.mockito:mockito-core:5.11.0")
    testImplementation("org.spigotmc:spigot-api:1.20.1-R0.1-SNAPSHOT")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.10.0")
}

checkstyle {
    toolVersion = "10.21.4"
    configFile = file("config/checkstyle/checkstyle.xml")
    isIgnoreFailures = false
    maxErrors = 0
    maxWarnings = 0
}

tasks.withType<Checkstyle>().configureEach {
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(listOf("-Xlint:all", "-Xlint:-processing"))
    options.isFork = true
    options.forkOptions.jvmArgs = listOf("-XX:+UseParallelGC")
}

tasks.test {
    useJUnitPlatform()
    maxParallelForks = Runtime.getRuntime().availableProcessors()
    setForkEvery(100)
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

jacoco {
    toolVersion = "0.8.11"
}

tasks.jar {
    archiveClassifier.set("plain")
    archiveFileName.set("LoChat-${project.version}.jar")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.shadowJar {
    archiveClassifier.set("")
    archiveFileName.set("LoChat-${project.version}.jar")
    mergeServiceFiles()
    relocate("net.kyori", "com.loki.lochat.libs.kyori")
    relocate("com.google.gson", "com.loki.lochat.libs.gson")
    // do NOT minimize(): strips ServiceLoader classes Adventure needs
}
tasks.build {
    dependsOn(tasks.shadowJar)
}
