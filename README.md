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

## 技术难点

这是开发过程中最大的痛点。我最初的设计构想要比现在实现的版本 ambitious 得多，但因为找不到合适的国内开放API，不得不多次退而求其次。

This was the biggest pain point during development. My original design vision was far more ambitious than what ended up being built, but the lack of suitable open APIs in China forced me to make compromises repeatedly.

> **一个App的功能上限，往往不是由开发能力决定的，而是由可用的数据和服务决定的。**
>
> **The capability ceiling of an app is often determined not by development skills, but by the available data and services.**

### 1. 药品库搜索 / Drug Database Search

中国有国家药品监督管理局（NMPA）的药品数据库，理论上包含了所有批准上市的药品信息。我希望能在App中接入这个数据源

China has the NMPA (National Medical Products Administration) drug database, which theoretically contains information on all approved drugs. I hoped to integrate this data source into the app, but after researching, I found that almost every path was a dead end:

| 方案 / Approach | 调研结果 / Findings | 结论 / Conclusion |
|----------------|--------------------|--------------------|
| NMPA官网 / NMPA Website | 提供网页查询界面，但没有公开API接口；数据以网页形式呈现，没有结构化的JSON/XML接口 | 无法直接调用 / No public API |
| 国家基础药物目录 / National Essential Medicines List | 政府公开文件（PDF/Excel），只有约685种药品，数据格式需要大量清洗 | 部分可用但繁琐 / Partially usable but tedious |
| 药智网 / Yaozh | 提供药品数据库查询，有部分API，但需要付费（企业版年费数千元），个人开发者无法申请 | 成本过高 / Too expensive |
| 丁香园用药助手 / DXY Drug Helper | 数据最全面（数万种药品），但不提供API，数据通过自家App封闭使用 | 不可用 / Unavailable |
| 开源药品数据库 / Open-source drug DB | GitHub上找到一些零散的药品数据集，但数据质量参差不齐，字段不统一 | 非常麻烦 / Very problematic |

**最终方案 / Final approach**：手动录入常见家庭用药数据。

Manually entered data for some common household drugs. 

### 2. 医疗诊断AI / Medical Diagnostic AI

最初设想是接入一个专门训练过的医疗诊断模型——学习了海量医患对话记录，能像经验丰富的全科医生那样给出诊断建议。但调研结果令人失望：

My original vision was to integrate a specialized medical diagnostic AI — one trained on massive doctor-patient dialogue data, capable of providing diagnostic suggestions like an experienced physician. But the research results were disappointing:

| 方案 / Approach | 调研结果 / Findings | 结论 / Conclusion |
|----------------|--------------------|--------------------|
| 微医/平安好医生 / WeDoctor / Ping An Good Doctor | 内部有训练过的医疗AI模型，但完全不对外开放API | 不可用 / Closed API |
| 百度灵医智惠 / Baidu Lingyi Zhihui | 提供辅助诊断能力，但需要医疗机构资质认证，个人开发者无法使用 | 资质门槛 / Qualification barrier |
| 阿里健康 / Ali Health | 有智能用药提醒和药品查询能力，但API不对个人开放 | 不可用 / Unavailable |
| 开源医疗大模型 / Open-source medical LLM | 华佗GPT、BianQue等，但需要GPU服务器部署，推理成本高 | 部署成本高 / High deployment cost |

**最终方案 / Final approach**：目前只有免费的智谱GLM-4-Flash和通义千问Qwen3-8B通用大模型。待开发完善

Currently, only the free general-purpose large models Zhipu GLM-4-Flash and Tongyi Qwen3-8B are available. Further development and optimization are ongoing.

### 3. 药品图像识别 / Drug Image Recognition

最初的想法是能识别中药材、药丸颗粒等有明显特征的药物——对于散装中药饮片或丢失包装的药丸，拍个照就能识别出来，实用性非常强。

My original idea was to recognize traditional Chinese medicine, herbal pills, and other visually distinctive drugs — for loose herbal slices or pills without packaging, a photo could identify them instantly. 

| 方案 / Approach | 调研结果 / Findings | 结论 / Conclusion |
|----------------|--------------------|--------------------|
| 百度AI开放平台 / Baidu AI Platform | 有"通用物体识别"，但没有专门的药品/中药材识别模型；属于智慧医疗模块，需要企业资质申请 | 个人开发者无法使用 / Unavailable to individuals |
| 阿里云视觉智能 / Alibaba Cloud Vision | 有"植物识别"可识别部分中药材原植物，但对炮制后的中药饮片、药丸颗粒无法识别 | 不适用 / Not applicable |
| 华为HMS ML Kit / Huawei HMS ML Kit | 提供通用图像分类，没有药品垂直领域的训练数据 | 不适用 / Not applicable |
| GitHub开源项目 / GitHub open-source | 找到几个中药材识别的毕业设计项目，但训练数据集不公开，模型精度不够，无法直接集成 | 不可用 / Unusable |

**最终方案 / Final approach**：放弃中药材识别，退而求其次做基于OCR文字的药品识别。待开发完善

Gave up traditional Chinese medicine recognition. Fell back to OCR-based drug identification — photograph text on drug packaging, then match against the drug database. 待开发完善


**未来展望 / Future Outlook**：
1. 接入更多药品数据源，扩大药品库覆盖范围 / Integrate more drug data sources to expand coverage
2. 研究开源医疗大模型的轻量化部署方案 / Research lightweight deployment of open-source medical LLMs
3. 探索中药材图像识别的可行性 / Explore feasibility of traditional Chinese medicine image recognition
4. 接入真实短信验证码服务，完善登录体验 / Integrate real SMS verification for better login experience

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
