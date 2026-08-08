<p align="center">
  <strong>中文</strong> | <a href="README.md">English</a>
</p>

# CrypticLib

一个 Minecraft 插件开发库，简化 Bukkit、BungeeCord 和 Velocity 平台的跨平台插件开发。

## 特性

### 平台层

| 模块 | Artifact ID | 说明 |
|------|-----------|------|
| **common** | `common` | 跨平台核心抽象 |
| **bukkit** | `bukkit` | Bukkit / Spigot / Paper / Folia 适配器 |
| **bungee** | `bungee` | BungeeCord 适配器 |
| **velocity** | `velocity` | Velocity 适配器 |

### 扩展模块

| 模块 | Artifact ID | 平台 | 说明 |
|------|------------|------|------|
| **database** | `common-database` | 通用 | 基于 HikariCP 的数据库连接管理 |
| **script** | `common-script` | 通用 | 内置脚本引擎（词法分析、语法分析、编译器、虚拟机） |
| **compat** | `common-compat` | 通用 | 版本兼容性工具 |
| **ui** | `bukkit-ui` | Bukkit | GUI 菜单系统，支持布局引擎、图标、多页 |
| **conversation** | `bukkit-conversation` | Bukkit | 多步玩家对话 / 提示系统，支持超时 |
| **i18n** | `bukkit-i18n` | Bukkit | 国际化支持，按语言文件管理翻译 |
| **particle** | `bukkit-particle` | Bukkit | 粒子效果渲染：几何图形、贝塞尔曲线、方程渲染器 |

### 核心能力

- **生命周期管理** - 声明式生命周期钩子（`INIT`、`LOAD`、`ENABLE`、`RELOAD`、`ACTIVE`、`DISABLE`），通过注解设置优先级
- **命令系统** - 基于注解的命令注册，支持子命令树
- **调度器** - 跨平台统一调度器接口（同步/异步/定时），支持 Folia 区域感知调度
- **配置系统** - 注解驱动的配置容器，自动加载和保存
- **依赖管理** - 运行时自动解析 Maven 依赖，支持重定位
- **权限系统** - 跨平台权限管理
- **消息系统** - 统一消息发送器，支持颜色代码处理和 PlaceholderAPI 集成

## 环境要求

- Java 8 或更高版本
- Minecraft 服务器：Bukkit / Spigot / Paper / Folia、BungeeCord 或 Velocity

## 快速开始

### Gradle (Kotlin DSL)

```kotlin
repositories {
    maven("https://repo.crypticlib.com/repository/maven-public/")
}

dependencies {
    implementation("com.crypticlib:bukkit:${crypticlibVersion}")
    // 按需添加扩展模块:
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

> **重要：** 你必须对 `crypticlib` 包进行 `relocate`，以避免与其他插件冲突。

完整的工作示例请参考 [CrypticLibExample](https://github.com/YufiriaMazenta/CrypticLibExample)。

## 使用示例

### 生命周期任务

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
                // 插件启用逻辑
                break;
            case DISABLE:
                // 插件禁用逻辑
                break;
        }
    }
}
```

### 脚本引擎

```java
// 条件判断
boolean result = ScriptEngine.INSTANCE.evaluate(
    "perm \"my.permission\"",
    ScriptContext.builder().player(player).build()
);

// 执行动作
ScriptEngine.INSTANCE.execute(
    "command \"give %player% diamond\"",
    ScriptContext.builder().player(player).build()
);
```

## 从源码构建

```bash
git clone https://github.com/YufiriaMazenta/CrypticLib.git
cd CrypticLib
./gradlew build
```

## 版本号规则

格式：`主版本号.次版本号.修订号.额外版本号`

- **主版本号** - 全项目重构时更新
- **次版本号** - 添加模块或有较大更改时更新
- **修订号** - 有 API 变动（新增/删除/更改声明）时更新
- **额外版本号** - 仅内部修改（如 BUG 修复）时更新

## 许可证

本项目基于 [GNU 通用公共许可证 v3.0](LICENSE) 授权。

## 链接

- [示例插件](https://github.com/YufiriaMazenta/CrypticLibExample)
- [问题反馈](https://github.com/YufiriaMazenta/CrypticLib/issues)
