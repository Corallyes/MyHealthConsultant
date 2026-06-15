# 青囊 — 智能家庭用药助手 / Smart Home Medication Assistant

> **本项目为 Android 课程设计作业，仅供学习交流使用。**
>
> **This project is an Android course design assignment, for learning and exchange purposes only.**

一款集**拍照识药、用药计划管理、家庭药箱、AI健康问答**于一体的 Android 健康管理应用。

An Android health management app integrating **drug recognition, medication planning, home medicine cabinet, and AI health consultation**.

---

## 功能简介 / Features

| 功能 | Feature | 说明 | Description |
|------|---------|------|-------------|
| 拍照识药 | Drug Recognition | 拍摄药品包装，OCR识别文字后匹配本地药品库或AI分析 | Photograph drug packaging, OCR text recognition with local database matching or AI analysis |
| 用药计划 | Medication Plan | 创建用药计划，支持三重提醒（提前/准时/补漏） | Create medication plans with triple reminders (advance / on-time / follow-up) |
| 用药打卡 | Check-in | 日历视图打卡，支持取消，实时统计连续天数/完成率 | Calendar-based check-in, supports undo, real-time stats (streak days / completion rate) |
| 家庭药箱 | Medicine Cabinet | 管理家中药品库存，有效期追踪，过期预警 | Manage home medicine inventory, expiry tracking, expiration alerts |
| AI健康助手 | AI Health Assistant | 接入GLM-4-Flash/Qwen3-8B大模型，支持健康问答 | Integrated with GLM-4-Flash / Qwen3-8B LLM for health Q&A |
| 药品库 | Drug Database | 64种常见药品信息查询，支持模糊搜索和分类浏览 | 64 common drugs info query, fuzzy search and category browsing |

---

## 运行环境要求 / Requirements

- Android 10.0 (API 29) 及以上 / or above
- Python 3.10+（OCR服务端 / OCR Server）

---

## 快速开始 / Getting Started

### 1. 克隆项目 / Clone

```bash
git clone https://github.com/Corallyes/MyHealthConsultant.git
cd MyHealthConsultant
```

### 2. 配置 API Key / Configure API Keys

在项目根目录的 `local.properties` 文件中配置（该文件已被 `.gitignore` 忽略，不会被提交）：

Configure in `local.properties` at the project root (this file is gitignored and will NOT be committed):

```properties
sdk.dir=你的Android SDK路径 / your Android SDK path
GLM_API_KEY=你的智谱API Key / your Zhipu API Key
SILICONFLOW_API_KEY=你的SiliconFlow API Key
```

**获取方式 / How to get**：
- **GLM_API_KEY**：注册 [智谱开放平台](https://open.bigmodel.cn/)，创建应用获取 API Key（GLM-4-Flash 免费）/ Register at [Zhipu Open Platform](https://open.bigmodel.cn/), create an app to get API Key (GLM-4-Flash is free)
- **SILICONFLOW_API_KEY**：注册 [SiliconFlow](https://siliconflow.cn/)，获取 API Key（Qwen3-8B 免费额度）/ Register at [SiliconFlow](https://siliconflow.cn/), get API Key (Qwen3-8B has free quota)

### 3. 部署 OCR 服务端 / Deploy OCR Server

拍照识药功能需要启动本地 OCR 服务。The drug recognition feature requires a local OCR server.

```bash
cd ocr_server
pip install fastapi uvicorn python-multipart rapidocr-onnxruntime
python main.py
```

服务启动后监听 `0.0.0.0:8000`。The server listens on `0.0.0.0:8000`.

通过 USB 数据线连接手机，执行端口转发。Connect phone via USB and forward the port:

```bash
adb reverse tcp:8000 tcp:8000
```

### 4. 编译运行 / Build & Run

用 Android Studio 打开项目，连接手机或启动模拟器，点击 Run。

Open the project in Android Studio, connect a phone or start an emulator, then click Run.

---

## 项目结构 / Project Structure

```
app/src/main/java/com/example/myhealthconsultant/
├── data/           # 数据层 Data layer (Room DB, Retrofit, Repository impl)
├── domain/         # 领域层 Domain layer (Repository interfaces)
├── di/             # 依赖注入 Dependency injection (Hilt Module)
├── presentation/   # 表现层 Presentation (Screens + ViewModels)
│   ├── ai/         # AI健康助手 AI Health Assistant
│   ├── auth/       # 登录注册 Authentication
│   ├── calendar/   # 用药日历打卡 Medication Calendar & Check-in
│   ├── cabinet/    # 家庭药箱 Home Medicine Cabinet
│   ├── camera/     # 拍照识药 Drug Recognition Camera
│   └── drugs/      # 药品库浏览 Drug Database Browser
├── ui/             # 主题、基础组件 Theme & base components
├── util/           # 工具类 Utilities (DataStore, WorkManager, etc.)
└── wxapi/          # 微信SDK回调 WeChat SDK callback
ocr_server/         # OCR服务端 OCR Server (Python FastAPI)
```

---

## 技术栈 / Tech Stack

| 技术 / Tech | 用途 / Purpose |
|-------------|---------------|
| Kotlin + Jetpack Compose | Android端开发语言和UI框架 / Language & UI framework |
| MVVM + Clean Architecture | 分层架构 / Layered architecture |
| Room | 本地数据库 / Local database |
| Hilt | 依赖注入 / Dependency injection |
| Retrofit + OkHttp | 网络请求 / HTTP client |
| WorkManager | 后台任务调度（用药提醒）/ Background task scheduling (medication reminders) |
| DataStore | 轻量级键值存储 / Key-value storage |
| RapidOCR | OCR文字识别引擎（Python端）/ OCR text recognition engine (Python) |
| FastAPI | OCR服务端Web框架 / OCR server web framework |

---

## 许可证 / License

本项目采用 [MIT 许可证](LICENSE) 开源。

This project is licensed under the [MIT License](LICENSE).

**重要声明 / Important Notice**：本项目为课程设计作业，代码和文档仅供学习交流。使用本项目时请遵守 MIT 许可证的全部条款。MIT 许可证允许自由使用、修改和分发，但需保留原始版权声明和许可证文本。

This project is a course design assignment. The code and documentation are for learning and exchange purposes only. When using this project, please comply with all terms of the MIT License. The MIT License permits free use, modification, and distribution, provided that the original copyright notice and license text are retained.

---

## 免责声明 / Disclaimer

- 本App中的AI健康问答功能基于通用大语言模型，**回复仅供参考，不构成医疗建议**。The AI health consultation feature is based on general-purpose LLMs. **Responses are for reference only and do NOT constitute medical advice.**
- 药品信息仅供学习参考，用药请遵医嘱。Drug information is for learning reference only. Please follow your doctor's advice for medication.
- 本项目为课程设计作品，不保证所有功能的完整性和准确性。This is a course design project. Completeness and accuracy of all features are not guaranteed.
