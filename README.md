# Light Translate - 智能语言学习平台

<div align="center">

![Java](https://img.shields.io/badge/Java-17-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.4-brightgreen)
![License](https://img.shields.io/badge/License-MIT-yellow.svg)

**AI 驱动的实时翻译与英语学习平台**

[Features](#-主要功能) • [Quick Start](#-快速开始) • [API Docs](#-api-接口) • [Tech Stack](#-技术栈)

</div>

---

## 📖 项目简介

**Light Translate** 是一款功能强大的 AI 驱动语言学习平台，集成了实时语音翻译、英语口语陪练、单词学习、影子跟读等多种功能于一体。通过集成阿里云通义千问、百度翻译、OCR 识别等先进 AI 服务，为用户提供沉浸式、智能化的语言学习体验。

### ✨ 核心亮点

- 🎯 **实时语音交互** - 基于 WebSocket 的低延迟实时英语对话练习
- 🤖 **AI 口语私教** - 角色扮演式情景对话，智能纠错与引导
- 🎙️ **语音转文字翻译** - 支持多语种语音识别与翻译
- 📸 **OCR 文字识别** - 图片文字提取与翻译
- 📚 **智能单词本** - 完整的词汇学习体系与记忆曲线算法
- 📊 **学习追踪** - 打卡系统、学习统计与进度管理

---

## 🚀 主要功能

### 1️⃣ 实时翻译服务

#### 音频翻译
- 上传音频文件，自动识别并翻译成目标语言
- 支持多种语言互译（中文 ↔ 英语 ↔ 日语 ↔ 韩语等）
- 返回翻译文本和合成音频的 URL

**API:** `POST /api/translation/translate`

#### 文本翻译
- 基于百度翻译 API 的快速文本翻译
- 支持 200+ 语言对

**API:** `POST /api/translation/textTranslate`

---

### 2️⃣ 实时口语陪练 (Real-time Speaking Partner)

基于阿里云 **Qwen3-Omni-Flash-Realtime** 模型，提供低延迟、自然流畅的英语对话体验。

#### 功能特性
- 🎭 **角色扮演** - 内置多种角色设定（教师 Lucy、朋友 Cherry 等）
- 🎯 **分级教学** - 根据 CEFR 标准（A1-C2）调整语言难度
- 🔄 **智能纠错** - 温柔指出错误并引导正确表达
- 🎵 **语音合成** - 自然逼真的 TTS 输出

#### 使用流程
---

### 3️⃣ 单词学习系统

#### 完整的词汇数据库
- 词库覆盖小学到大学各级别
- 包含音标、释义、例句、同义词、短语搭配
- 词根词缀、句型结构等深度解析

#### 智能推荐算法
- 根据当前单词推荐相关词汇（同义、反义、形近）
- 随机抽词功能，支持按词性筛选
- 干扰项生成（用于选择题）

**API:** 
- `GET /api/word/search?query=apple` - 搜索单词
- `GET /api/word/detail/{wordId}` - 获取单词详情
- `GET /api/word/recommend?word=hello` - 推荐相关词

---

### 4️⃣ 影子跟读训练 (Shadow Reading)

#### 评估流程
1. 播放原文音频
2. 用户跟读并录音
3. AI 语音识别对比
4. 逐句相似度评分 + 差异分析

#### 评分算法
- 基于 Levenshtein 距离计算句子相似度
- 标注遗漏/错误的单词
- 可视化反馈

**API:** `POST /api/shadow-reading/evaluate`

---

### 5️⃣ OCR 文字识别

#### 功能
- 上传图片提取文字（支持中英文混合）
- 百度 OCR API 集成
- 识别结果可直接翻译

**API:** `POST /api/ocr/recognize`

---

### 6️⃣ 用户学习系统

#### 打卡签到
- 每日学习打卡
- 记录新学单词数、复习数
- 连续打卡天数统计

#### 学习统计
- 今日任务完成情况
- 单词本掌握进度（已学/已掌握/待学习）
- 最近 7 天学习曲线

#### 记忆强度算法
---

### 7️⃣ 每日文章分享

- 精选英语短文（带中文翻译）
- 支持朗读音频
- 可分享到社交平台

---

## 🛠️ 技术栈

### 后端框架
- **Spring Boot 3.4.4** - 核心框架
- **Spring Data JPA** - ORM 数据访问
- **Spring WebSocket** - 实时双向通信
- **Spring AI Alibaba** - AI 服务集成

### 数据库
- **MySQL 8.0** - 关系型数据库
- **JPA Repository** - 数据持久层

### AI 服务集成
| 服务商 | 功能 | 模型/API |
|--------|------|----------|
| **阿里云 DashScope** | 实时语音对话 | Qwen3-Omni-Flash-Realtime |
| **阿里云 DashScope** | 语音合成 | Qwen3-TTS-Flash-Realtime |
| **阿里云 DashScope** | 语音识别 | Gummy-Realtime-V1 |
| **阿里云 DashScope** | 文生图 | Wanx2.1-T2I-Plus |
| **百度翻译** | 文本翻译 | Baidu Translate API |
| **百度 OCR** | 文字识别 | Baidu OCR API |

### 音频处理
- **FFmpeg** - 音频格式转换（PCM ↔ MP3 ↔ WAV）
- **PyDub** - Python 音频处理库
- **Edge-TTS** - 微软语音合成

### 工具 & 中间件
- **Maven** - 依赖管理
- **Docker & Docker Compose** - 容器化部署
- **阿里云 OSS** - 对象存储（音频、图片资源）
- **Jackson** - JSON 序列化

### Python 脚本
 scripts/
- ├── tts.py # 文本转语音（Edge-TTS） 
- ├── generate_image.py # AI 文生图 
- └── requirements.txt # Python 依赖

---

## 🚀 快速开始

### 环境要求

- **JDK 17+**
- **Maven 3.6+**
- **Python 3.8+** (可选，用于 TTS 和图片生成)
- **FFmpeg** (音频处理)
- **MySQL 8.0+**

### 1. 克隆项目
bash git clone https://github.com/your-username/light-translate.git cd light-translate
### 2. 配置环境变量
