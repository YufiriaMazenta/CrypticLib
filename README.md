<p align="center">
  <a href="README-CN.md">中文</a> | <strong>English</strong>
</p>

# CrypticLib

A Minecraft plugin development library that simplifies cross-platform plugin development for Bukkit, BungeeCord, and Velocity.

## Features

### Platform Layer

| Module | Artifact ID | Description |
|--------|-------------|-------------|
| **common** | `common`    | Core abstractions shared across all platforms |
| **bukkit** | `bukkit`    | Bukkit / Spigot / Paper / Folia adapter |
| **bungee** | `bungee`    | BungeeCord adapter |
| **velocity** | `velocity`  | Velocity adapter |

### Extension Modules

| Module | Artifact ID | Platform | Description |
|--------|------------|----------|-------------|
| **database** | `common-database` | Common | Database connectivity via HikariCP |
| **script** | `common-script` | Common | Built-in scripting engine with lexer, parser, compiler, and VM |
| **compat** | `common-compat` | Common | Compatibility utilities |
| **ui** | `bukkit-ui` | Bukkit | GUI menu system with layout engine, icons, multi-page support |
| **conversation** | `bukkit-conversation` | Bukkit | Multi-step player conversation / prompt system with timeout |
| **i18n** | `bukkit-i18n` | Bukkit | Internationalization with per-locale language files |
| **particle** | `bukkit-particle` | Bukkit | Particle effect rendering: geometric shapes, Bezier curves, equation renderers |

### Core Capabilities

- **Lifecycle Management** - Declarative lifecycle hooks (`INIT`, `LOAD`, `ENABLE`, `RELOAD`, `ACTIVE`, `DISABLE`) with priority ordering via annotations
- **Command System** - Annotation-based command registration with subcommand trees
- **Scheduler** - Unified scheduler interface across all platforms (sync/async/timer), with Folia region-aware scheduling support
- **Config System** - Annotation-driven configuration containers with automatic loading and saving
- **Dependency Management** - Automatic Maven dependency resolution at runtime with relocation support
- **Permission System** - Cross-platform permission management
- **Messaging** - Unified message sender with color code processing and PlaceholderAPI integration

## Requirements

- Java 8 or higher
- Minecraft server running Bukkit / Spigot / Paper / Folia, BungeeCord, or Velocity

## Quick Start

### Gradle (Kotlin DSL)

```kotlin
repositories {
    maven("https://repo.crypticlib.com/repository/maven-public/")
}

dependencies {
    implementation("com.crypticlib:bukkit:${crypticlibVersion}")
    // Add extension modules as needed:
    // implementation("com.crypticlib:bukkit-ui:${crypticlibVersion}")
    // implementation("com.crypticlib:bukkit-i18n:${crypticlibVersion}")
    // implementation("com.crypticlib:common-script:${crypticlibVersion}")
}
```

### Gradle (Groovy DSL)

```groovy
repositories {
    maven {
        url = "https://repo.crypticlib.com/repository/maven-public/"
    }
}

dependencies {
    implementation "com.crypticlib:bukkit:${crypticlibVersion}"
}
```

### Maven

```xml
<repositories>
    <repository>
        <id>crypticlib</id>
        <url>https://repo.crypticlib.com/repository/maven-public/</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.crypticlib</groupId>
        <artifactId>bukkit</artifactId>
        <version>${crypticlibVersion}</version>
    </dependency>
</dependencies>
```

> **Important:** You must `relocate` the `crypticlib` package to avoid conflicts with other plugins.

For a complete working example, see [CrypticLibExample](https://github.com/YufiriaMazenta/CrypticLibExample).

## Usage Examples

### Lifecycle Tasks

```java
@LifecycleTaskSettings(rules = {
    @LifecycleRule(lifeCycle = Lifecycle.ENABLE, priority = 0),
    @LifecycleRule(lifeCycle = Lifecycle.DISABLE)
})
public class MyTask implements LifecycleTask {

    @Override
    public void lifecycle(CrypticLibPlugin plugin, Lifecycle lifeCycle) {
        switch (lifeCycle) {
            case ENABLE:
                // Plugin enable logic
                break;
            case DISABLE:
                // Plugin disable logic
                break;
        }
    }
}
```

### Script Engine

```java
// Evaluate a condition
boolean result = ScriptEngine.INSTANCE.evaluate(
    "perm(\"my.permission\")",
    ScriptContext.builder().player(player).build()
);

// Execute an action
ScriptEngine.INSTANCE.execute(
    "command(\"give %player% diamond\")",
    ScriptContext.builder().player(player).build()
);
```

## Building from Source

```bash
git clone https://github.com/YufiriaMazenta/CrypticLib.git
cd CrypticLib
./gradlew build
```

## License

This project is licensed under the [GNU General Public License v3.0](LICENSE).

## Links

- [Example Plugin](https://github.com/YufiriaMazenta/CrypticLibExample)
- [Issue Tracker](https://github.com/YufiriaMazenta/CrypticLib/issues)
