plugins {
    id("java")
    id("net.minecrell.plugin-yml.bukkit") version "0.6.0"
    id("io.github.goooler.shadow") version "8.1.7"
}

configurations.all {
    resolutionStrategy.cacheChangingModulesFor(0, "seconds")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

dependencies {
    compileOnly("org.spigotmc:spigot:1.21-R0.1-SNAPSHOT")
    implementation("io.netty:netty-codec-http:4.1.97.Final")
}

bukkit {
    name = rootProject.name
    version = rootProject.version.toString()
    main = "me.jishuna.packprovider.TempMain"
    apiVersion = "1.21"
}

tasks.shadowJar {
    archiveClassifier.set("")
    archiveVersion.set("")
}

tasks.assemble {
    dependsOn(tasks.shadowJar)
    finalizedBy(tasks.named("copyJar"))
}

tasks.register("copyJar", Copy::class) {
    doNotTrackState("")
    val target = System.getenv("plugin-dir")

    if (target != null) {
        from(tasks.shadowJar)
        into(File(target))
    }
}