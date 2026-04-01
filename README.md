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

* 🎯 **实时语音交互** - 基于 WebSocket 的低延迟实时英语对话练习
* 🤖 **AI 口语私教** - 角色扮演式情景对话，智能纠错与引导
* 🎙️ **语音转文字翻译** - 支持多语种语音识别与翻译
* 📸 **OCR 文字识别** - 图片文字提取与翻译
* 📚 **智能单词本** - 完整的词汇学习体系与记忆曲线算法
* 📊 **学习追踪** - 打卡系统、学习统计与进度管理
---

## 📱 小程序体验（Demo）

### 🟢 微信扫码体验
<img width="215" height="215" alt="image" src="https://github.com/user-attachments/assets/e51e0781-7ed0-4655-a6e2-9f213d5dc638" />


> 使用微信扫描二维码即可体验完整功能（语音对话 / 跟读 / 翻译等）
---

### 🎬 功能演示视频

<video src="https://github.com/user-attachments/assets/e5d45ae4-e195-44a1-bcd7-27ef45802427" controls width="100%"></video>

> 演示内容：实时口语对话 + AI 回复 + 跟读评分

---

## 🚀 主要功能

### 1️⃣ 实时翻译服务

#### 音频翻译

* 上传音频文件，自动识别并翻译成目标语言
* 支持多种语言互译（中文 ↔ 英语 ↔ 日语 ↔ 韩语等）
* 返回翻译文本和合成音频的 URL

**API：**

```http
POST /api/translation/translate
```

#### 文本翻译

* 基于百度翻译 API 的快速文本翻译
* 支持 200+ 语言对

**API：**

```http
POST /api/translation/textTranslate
```

---

### 2️⃣ 实时口语陪练 (Real-time Speaking Partner)

基于阿里云 **Qwen3-Omni-Flash-Realtime** 模型，提供低延迟、自然流畅的英语对话体验。

#### 功能特性

* 🎭 **角色扮演** - 内置多种角色设定（教师 Lucy、朋友 Cherry 等）
* 🎯 **分级教学** - 根据 CEFR 标准（A1-C2）调整语言难度
* 🔄 **智能纠错** - 温柔指出错误并引导正确表达
* 🎵 **语音合成** - 自然逼真的 TTS 输出

#### 使用流程

**步骤 1：创建会话**

```bash
POST /api/dict/realtime/start
参数：
- openid（用户 ID）
- level（难度等级）
```

**步骤 2：发送音频**

```bash
POST /api/dict/realtime/sendMessage
参数：
- openid
- audio（音频文件）
```

**步骤 3：结束会话**

```bash
GET /api/dict/realtime/stop?openid={openid}
```

---

### 3️⃣ 单词学习系统

#### 完整的词汇数据库

* 词库覆盖小学到大学各级别
* 包含音标、释义、例句、同义词、短语搭配

#### 智能推荐算法

* 根据当前单词推荐相关词汇（同义、反义、形近）
* 随机抽词功能，支持按词性筛选

**API：**

```http
GET /api/word/search?query=apple
GET /api/word/detail/{wordId}
GET /api/word/recommend?word=hello
```

---

### 4️⃣ 影子跟读训练 (Shadow Reading)

#### 评估流程

1. 播放原文音频
2. 用户跟读并录音
3. AI 语音识别对比
4. 逐句相似度评分 + 差异分析

#### 评分算法

```text
相似度 = (最大长度 - 编辑距离) / 最大长度
```

#### 反馈结果

* 逐句相似度百分比
* 遗漏/错误单词标注
* 整体评分（0-100 分）

**API：**

```http
POST /api/shadow-reading/evaluate
```

---

### 5️⃣ OCR 文字识别

* 上传图片提取文字（支持中英文混合）
* 百度 OCR API 集成
* 识别结果可直接翻译

**API：**

```http
POST /api/ocr/recognize
```

---

### 6️⃣ 用户学习系统

#### 打卡签到

* 每日学习打卡
* 记录新学单词数、复习数

#### 学习统计

* 今日任务完成情况
* 单词本掌握进度（已学/已掌握/待学习）
* 最近 7 天学习曲线

#### 记忆强度算法

| 记忆强度        | 掌握程度   | 说明     |
| ----------- | ------ | ------ |
| ≥ 0.8       | 🟢 已掌握 | 可以长期记忆 |
| 0 < x < 0.8 | 🟡 复习中 | 需要定期复习 |
| = 0         | ⚪ 未学习  | 还未开始学习 |

---

### 7️⃣ 每日文章分享

* 精选英语短文（带中文翻译）
* 支持朗读音频

---

## 🛠️ 技术栈

### 后端框架

* Spring Boot 3.4.4
* Spring Data JPA
* Spring WebSocket
* Spring AI Alibaba

### 数据库

* MySQL 8.0

### AI 服务

| 服务商           | 功能     | 模型                        |
| ------------- | ------ | ------------------------- |
| 阿里云 DashScope | 实时语音对话 | Qwen3-Omni-Flash-Realtime |
| 阿里云 DashScope | 语音合成   | Qwen3-TTS-Flash-Realtime  |
| 百度翻译          | 文本翻译   | Baidu Translate API       |
| 百度 OCR        | 文字识别   | Baidu OCR API             |

### 工具

* FFmpeg
* Docker
* 阿里云 OSS

---

## 📦 项目结构

```text
communicate/
├── src/main/
│   ├── java/com/light/translate/communicate/
│   │   ├── ali/           # 阿里云服务集成
│   │   ├── baidu/         # 百度服务集成
│   │   ├── config/        # 配置类
│   │   ├── controller/    # REST API 控制器
│   │   ├── data/          # 实体类
│   │   ├── dto/           # 数据传输对象
│   │   ├── handler/       # WebSocket 处理器
│   │   ├── repository/    # 数据访问层
│   │   ├── services/      # 业务逻辑层
│   │   ├── translate/     # 翻译核心服务
│   │   └── utils/         # 工具类
│   └── resources/
│       ├── prompt/        # AI Prompt 模板
│       └── application*.properties
├── scripts/               # Python 辅助脚本
├── Dockerfile             # Docker 镜像构建
└── docker-compose.yml     # 编排配置
```

---

## 🚀 快速开始

### 环境要求

* JDK 17+
* Maven 3.6+
* Python 3.8+（可选）
* FFmpeg
* MySQL 8.0+

---

### 1️⃣ 克隆项目

```bash
git clone https://github.com/GUANGGE/light-translate.git
cd light-translate
```

---

### 2️⃣ 配置环境变量

```bash
# 阿里云 DashScope
ALI_API_KEY=sk-your-api-key

# 百度翻译
BAIDU_APP_ID=your-app-id
BAIDU_API_KEY=your-api-key

# 数据库
DB_HOST=localhost
DB_PORT=3306
DB_NAME=light_translate
DB_USERNAME=root
DB_PASSWORD=your-password
```

---

### 3️⃣ 安装 Python 依赖（可选）

```bash
cd scripts
pip install -r requirements.txt
```

---

### 4️⃣ 构建项目

```bash
mvn clean package -DskipTests
```

---

### 5️⃣ 运行应用

```bash
# 方式1
mvn spring-boot:run

# 方式2
java -jar target/communicate-0.0.1-SNAPSHOT.jar
```

应用将在：http://localhost:8080

---

## 🐳 Docker 部署

### 构建镜像

```bash
mvn clean package -DskipTests
docker build -t light-translate:latest .
```

---

### Docker Compose

```yaml
version: '3.8'

services:
  lightTranslate:
    image: light-translate:latest
    container_name: light-translate
    restart: unless-stopped
    ports:
      - "8080:8080"
    environment:
      - SPRING_DATASOURCE_URL=jdbc:mysql://host.docker.internal:3306/light_translate
      - SPRING_DATASOURCE_USERNAME=root
      - SPRING_DATASOURCE_PASSWORD=your-password
      - SPRING_AI_DASHSCOPE_API_KEY=sk-your-api-key
      - TZ=Asia/Shanghai
```

---

### 启动服务

```bash
docker-compose up -d
```

### 查看日志

```bash
docker-compose logs -f
```

### 停止服务

```bash
docker-compose down
```

---

## 📚 API 接口文档

### 微信登录

```http
POST /api/auth/wechat/login
Content-Type: application/json

{
  "code": "wechat_auth_code"
}
```

Response：

```json
{
  "openid": "user_openid",
  "token": "jwt_token"
}
```

---

### 实时口语接口

```http
POST /api/dict/realtime/start
POST /api/dict/realtime/sendMessage
GET  /api/dict/realtime/stop
```

---

### 单词接口

```http
GET /api/word/search
GET /api/word/detail/{wordId}
GET /api/word/recommend
```

---

### OCR 接口

```http
POST /api/ocr/recognize
```

---

### 跟读接口

```http
POST /api/shadow-reading/evaluate
```

---

## 🤝 贡献指南

欢迎提交 Issue 和 Pull Request

---

## 📄 开源协议

MIT License

---

## 👥 开发者

大连光哥

---

## 📞 联系方式

* GitHub: https://github.com/xgwangdl
* Email: [xgwangdl@163.com](xgwangdl@163.com)
