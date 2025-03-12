plugins {
    id("java")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("net.bytebuddy:byte-buddy:1.14.7")
    implementation("net.bytebuddy:byte-buddy-agent:1.14.7")

    testImplementation("junit:junit:4.13.2")
}

tasks.test {
    useJUnit()
}
