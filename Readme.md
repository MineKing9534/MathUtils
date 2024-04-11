![[Java CI]](https://github.com/MineKing9534/MathUtils/actions/workflows/check.yml/badge.svg)
![[Latest Version]](https://maven.mineking.dev/api/badge/latest/releases/de/mineking/MathUtils?prefix=v&name=Latest%20Version&color=0374b5)

# Installation

MathUtils is hosted on a custom repository at [https://maven.mineking.dev](https://maven.mineking.dev/#/releases/de/mineking/MathUtils). Replace VERSION with the lastest version (without the `v` prefix).
Alternatively, you can download the artifacts from jitpack (not recommended).

### Gradle

```groovy
repositories {
    maven { url "https://maven.mineking.dev/releases" }
}

dependencies {
    implementation "de.mineking:MathUtils:VERSION"
}
```

### Maven

```xml

<repositories>
    <repository>
        <id>mineking</id>
        <url>https://maven.mineking.dev/releases</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>de.mineking</groupId>
        <artifactId>MathUtils</artifactId>
        <version>VERSION</version>
    </dependency>
</dependencies>
```