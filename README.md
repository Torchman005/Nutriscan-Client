# NutriScan Android Client

NutriScan 是一个基于 Android + Jetpack Compose 开发的食品营养 AI 助手客户端，面向健康饮食、健身控重、幼儿辅食等场景，提供食品识别、社区交流、健康分析和个人档案管理能力。

> 当前仓库是 Android 客户端工程，不包含后端服务源码。

## 项目简介

本项目主要解决两个问题：

- 帮助用户通过拍照或相册图片快速识别食品内容，获取营养分析、过敏原提示和食用建议
- 帮助用户围绕饮食健康进行交流，记录体重、热量等数据，并持续追踪个人健康目标

项目中存在多套历史命名：

- 仓库和文档中常见名称：`NutriScan`
- 部分代码包名：`com.example.foodnutritionaiassistant`
- 部分目录路径：`com/luminous/nutriscan`

这属于项目演进中的遗留现象，阅读代码和配置时需要注意。

## 核心功能

### 1. 登录与注册

- 支持手机号 + 验证码登录
- 支持新用户注册和首次资料补全
- 支持本地登录状态持久化

### 2. 食品识别

- 支持拍照识别
- 支持从相册选择图片识别
- 支持查看识别历史
- 支持展示结构化分析结果，包括菜品名称、主要原材料、潜在过敏原、风险评估和食用建议

### 3. 社区论坛

- 支持按养生、健身、幼儿等频道浏览内容
- 支持搜索、已关注筛选、最新/最热排序
- 支持发帖、编辑、草稿、点赞、收藏、评论、回复
- 支持关注作者、查看浏览记录和我的发布

### 4. 健康分析

- 展示 BMI、BMR、目标达成率等指标
- 展示近 7 天体重变化图
- 支持体重周报查看
- 支持手动记录热量

### 5. 个人中心

- 支持编辑昵称、头像、性别、生日、地区、简介
- 支持设置目标体重
- 支持意见反馈、帮助中心、设置等页面

## 技术栈

- 开发语言：Kotlin
- UI 框架：Jetpack Compose、Material 3
- 架构模式：MVVM
- 状态管理：ViewModel + Compose State
- 网络请求：Retrofit + Gson
- 图片加载：Coil
- 对象存储：MinIO
- 构建工具：Gradle
- JDK：17

## 开发环境

建议使用以下环境打开和运行项目：

- Android Studio：较新稳定版即可
- Android Gradle Plugin：`8.12.0`
- Kotlin：`2.0.21`
- compileSdk：`36`
- targetSdk：`36`
- minSdk：`26`
- Java：`17`

## 快速开始

### 1. 克隆项目

```bash
git clone <your-repo-url>
```

### 2. 使用 Android Studio 打开

直接打开项目根目录：

```text
Nutriscan-Client
```

首次打开后等待 Gradle Sync 完成。

### 3. 运行 App

选择 `app` 模块，连接 Android 真机或启动模拟器后直接运行。

也可以在命令行使用：

```bash
./gradlew assembleDebug
```

Windows:

```powershell
.\gradlew.bat assembleDebug
```

## 运行要求

- Android 8.0 及以上设备或模拟器
- 需要网络连接
- 食品识别功能需要相机权限
- 从相册识别功能依赖图片读取能力

项目中已经声明了以下关键权限：

- `INTERNET`
- `CAMERA`

## 配置说明

项目当前通过 [AppConfig.kt](file:///D:/itJinYu_toolkit/Nutriscan/Nutriscan-Client1/Nutriscan-Client/app/src/main/java/com/luminous/nutriscan/config/AppConfig.kt) 配置后端与对象存储地址，包括：

- 后端 API 基地址
- MinIO endpoint
- MinIO bucket 名称

如果你需要切换到自己的测试环境，优先修改该文件中的配置项。

## 默认行为说明

当前客户端中有一些便于联调或演示的默认行为，使用前建议了解：

- 验证码发送逻辑目前带有前端 mock 行为
- 某些登录失败码会被识别为“未注册用户，进入首登流程”
- 应用启用了 `usesCleartextTraffic=true`，说明当前允许明文 HTTP 通信

如果用于生产环境，建议统一梳理认证、网络安全和配置管理逻辑。

## 项目结构

```text
Nutriscan-Client/
├─ app/                         Android 客户端源码
│  └─ src/main/
│     ├─ java/.../config/       运行配置
│     ├─ java/.../data/         数据模型、网络层、仓库层、存储层
│     ├─ java/.../ui/screens/   页面 UI
│     ├─ java/.../ui/viewmodel/ 状态管理
│     ├─ java/.../ui/analysis/  分析页面
│     └─ res/                   资源文件
├─ database/                    数据库脚本
├─ gradle/                      Gradle Wrapper 与版本目录
├─ NutriScan_User_Manual.md     使用手册
└─ README.md                    项目说明
```

## 数据库说明

本仓库虽然是客户端，但仓库内保留了数据库初始化脚本，方便联调和交付说明。

### MongoDB

脚本文件：

- [init\_mongodb.js](file:///D:/itJinYu_toolkit/Nutriscan/Nutriscan-Client1/Nutriscan-Client/database/init_mongodb.js)

主要用于：

- 初始化 `users`
- 初始化 `posts`
- 初始化 `comments`
- 创建关键索引

### MySQL

脚本文件：

- [user\_schema.sql](file:///D:/itJinYu_toolkit/Nutriscan/Nutriscan-Client1/Nutriscan-Client/database/user_schema.sql)

主要表包括：

- `app_user`
- `sms_verification_code`
- `user_calorie_log`
- `user_weight_log`

## 文档入口

- 用户使用手册：[NutriScan\_User\_Manual.md](file:///D:/itJinYu_toolkit/Nutriscan/Nutriscan-Client1/Nutriscan-Client/NutriScan_User_Manual.md)
- Android 构建配置：[build.gradle.kts](file:///D:/itJinYu_toolkit/Nutriscan/Nutriscan-Client1/Nutriscan-Client/app/build.gradle.kts)
- 依赖版本目录：[libs.versions.toml](file:///D:/itJinYu_toolkit/Nutriscan/Nutriscan-Client1/Nutriscan-Client/gradle/libs.versions.toml)
- Android 清单文件：[AndroidManifest.xml](file:///D:/itJinYu_toolkit/Nutriscan/Nutriscan-Client1/Nutriscan-Client/app/src/main/AndroidManifest.xml)

## 适合先看的代码

- 应用入口：[MainActivity.kt](file:///D:/itJinYu_toolkit/Nutriscan/Nutriscan-Client1/Nutriscan-Client/app/src/main/java/com/luminous/nutriscan/MainActivity.kt)
- 社区页面：[CommunityScreen.kt](file:///D:/itJinYu_toolkit/Nutriscan/Nutriscan-Client1/Nutriscan-Client/app/src/main/java/com/luminous/nutriscan/ui/screens/CommunityScreen.kt)
- 识别页面：[ScanScreen.kt](file:///D:/itJinYu_toolkit/Nutriscan/Nutriscan-Client1/Nutriscan-Client/app/src/main/java/com/luminous/nutriscan/ui/screens/ScanScreen.kt)
- 分析页面：[AnalysisScreen.kt](file:///D:/itJinYu_toolkit/Nutriscan/Nutriscan-Client1/Nutriscan-Client/app/src/main/java/com/luminous/nutriscan/ui/analysis/AnalysisScreen.kt)
- 用户状态管理：[UserViewModel.kt](file:///D:/itJinYu_toolkit/Nutriscan/Nutriscan-Client1/Nutriscan-Client/app/src/main/java/com/luminous/nutriscan/ui/viewmodel/UserViewModel.kt)

## 已知注意事项

- 包名、目录名和产品名目前不完全统一
- 后端地址和对象存储地址写在客户端配置中，迁移环境时需要手工修改
- 本仓库没有后端服务源码，运行效果依赖远端接口可用性
- 社区、识别、图片上传等功能都依赖网络环境

