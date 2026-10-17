# Chinese School Mod

一个面向 **Minecraft 26.3** 的中式学校主题 [NeoForge](https://neoforged.net/) 模组。

| | |
| --- | --- |
| 模组 ID | `chinese_school` |
| 模组名称 | Chinese School Mod |
| Minecraft | 26.3 |
| NeoForge | 26.3.0.76 |
| Java | 25 |
| 许可证 | MIT |
| 作者 | [CR400BF-5033-C2021](https://github.com/CR400BF-5033-C2021) |

## 环境要求

- JDK 25（编译目标；Gradle 可用 JDK 21 启动，工具链会自动选择 25）
- 无需单独安装 Gradle，仓库自带 Gradle Wrapper（9.2.1）

## 常用命令

```bash
./gradlew build          # 编译并打包（产物在 build/libs/）
./gradlew runClient      # 启动带模组的客户端
./gradlew runServer      # 启动带模组的服务端
./gradlew runData        # 运行数据生成器，输出到 src/generated/resources
./gradlew --refresh-dependencies   # 依赖缓存异常时刷新
```

Windows 下把 `./gradlew` 换成 `gradlew.bat` 或 `.\gradlew.bat`。

## 项目结构

```
build.gradle                                   构建脚本（ModDevGradle 2.0.148）
settings.gradle                                工程设置
gradle.properties                              版本与模组元信息
src/main/templates/META-INF/neoforge.mods.toml 模组元数据模板（构建时展开 ${...}）
src/main/java/com/cr400bf5033/chineseschool/
├── ChineseSchoolMod.java                      主入口，负责挂载各注册表
├── client/ChineseSchoolClient.java            客户端入口（仅客户端加载）
└── registry/
    ├── ModBlocks.java                         方块注册表
    ├── ModItems.java                          物品注册表
    └── ModCreativeModeTabs.java               创造模式物品栏
src/main/resources/assets/chinese_school/lang/ 语言文件（en_us / zh_cn）
```

## 后续开发

- 新增方块 / 物品：在 `ModBlocks` / `ModItems` 中添加静态字段，模组主物品栏会自动收录 `ModItems` 里的内容。
- 新增资源：贴图放在 `src/main/resources/assets/chinese_school/` 下，语言文本同步补充到 `lang/en_us.json` 与 `lang/zh_cn.json`。
- 客户端专用内容（渲染、界面、按键）放在 `client` 包中。

## 说明

- 模组元数据中的版本号、作者等信息统一由 `gradle.properties` 提供，无需直接修改 TOML 文件。
- 本仓库使用 [ModDevGradle](https://github.com/neoforged/moddevgradle) 插件；官方模板见 [NeoForgeMDKs](https://github.com/NeoForgeMDKs)。
