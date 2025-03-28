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

    implementation("org.openjdk.jol:jol-core:0.17")
    implementation("org.jfree:jfreechart:1.5.3")
    implementation("org.jfree:jcommon:1.0.24")
}

tasks.test {
    useJUnit()
}
