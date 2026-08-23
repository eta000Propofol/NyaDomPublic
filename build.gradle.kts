plugins {
    java
}

group = "com.nyadom"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://jitpack.io")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:26.1.2.build.74-stable")
    compileOnly("cn.lunadeer:DominionAPI:4.9.4")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7") {
        exclude(group = "org.bukkit")
        exclude(group = "org.spigotmc")
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(25)
}

tasks.jar {
    archiveBaseName.set("NyaDomPublic")
}

