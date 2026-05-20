# 智慧 AI 平台 (Spring-AI)

基于 Spring AI 构建的智能服务平台，集成大语言模型对话、PDF 文档向量检索（RAG）、督查督办业务系统以及 Redis 持久化会话记忆等能力。

## 技术栈

| 类别 | 技术 |
|------|------|
| 框架 | Spring Boot 4.0.5、Spring AI 2.0.0-M4 |
| AI 模型 | 通义千问 qwen3.5-plus（通过 OpenAI 兼容接口）、text-embedding-v3 嵌入模型 |
| 向量存储 | Redis（RedisVectorStore，HNSW 算法，COSINE 距离） |
| 持久层 | MyBatis-Plus 3.5.12、MySQL |
| 会话记忆 | Redis Hash 持久化（替代内存） |
| 其他 | Lombok、Maven、Java 21 |

## 核心功能

### 1. 智能对话（Chat）

通用 AI 对话接口，系统角色为"小智"，支持多轮对话和多模态（图片/文件附件）输入。

- 基于 Redis 持久化会话记忆，每个会话最多保留 50 条消息
- 支持流式响应（Server-Sent Events）
- 支持上传附件进行多模态对话

### 2. PDF 文档问答（RAG）

上传 PDF 文件后，基于向量检索增强生成（RAG）实现针对文档内容的智能问答。

- PDF 按页拆分为 Document，每页一条，写入 Redis 向量库
- 使用 `QuestionAnswerAdvisor` 实现 RAG 检索，相似度阈值 0.6，取 Top 1 结果
- 通过 `file_name` 元数据字段过滤，确保只检索当前会话对应的文档
- 向量索引使用 TEXT 类型存储文件名，避免 TAG 对中文的兼容性问题

### 3. 智能客服（Service）

面向督查督办系统的业务助手，通过 **Function Calling（工具调用）** 直接操作数据库。

- 系统提示词定义完整的业务流程和安全规则
- 绑定 `SupervisionTools` 工具集：
  - `querySupervisionRecord` — 按条件查询督查记录
  - `queryHandleRecordBySupervisionId` — 查询督查处理记录
  - `createHandleRecord` — 新增督查处理记录
- （预留）`CourseTools` 工具集：课程查询、校区查询、课程预约（当前已注释）

### 4. 聊天历史管理

- 按类型（chat / service / pdf）分组管理会话 ID 列表（Redis Set）
- 查询指定会话的完整消息记录
- 消息从 Redis Hash 反序列化为标准格式返回

## 项目结构

```
src/main/java/com/example/springai/
├── SpringAiApplication.java              # 启动类
├── config/
│   ├── CommonConfiguration.java          # Spring AI Bean 配置（ChatClient、ChatMemory、VectorStore）
│   ├── MvcConfiguration.java             # Web MVC 配置（CORS）
│   └── MybatisConfig.java                # MyBatis-Plus SqlSessionFactory 配置
├── constants/
│   └── SystemConstants.java              # 系统提示词常量
├── controller/
│   ├── ChatController.java               # 通用对话 / 多模态对话
│   ├── PdfController.java                # PDF 上传、下载、文档问答
│   ├── CustomerServiceController.java    # 督查督办智能客服
│   └── ChatHistoryController.java        # 聊天历史查询
├── entity/
│   ├── po/                               # 持久化实体（Course, School, SupervisionRecord 等）
│   ├── query/                            # 查询条件对象
│   └── vo/                               # 视图对象（Result, MessageVo）
├── mapper/                               # MyBatis-Plus Mapper 接口
├── repository/
│   ├── ChatHistoryRepository.java        # 会话 ID 仓库接口
│   ├── RedisChatHistoryRepository.java   # 基于 Redis Set 的会话 ID 实现
│   ├── RedisChatMemoryRepository.java    # 基于 Redis Hash 的聊天消息持久化
│   ├── FileRepository.java               # 文件仓库接口
│   └── LocalPdfFileRepository.java       # 基于本地磁盘 + Redis 映射的 PDF 存储
├── service/ & service/impl/              # 业务服务层（课程、校区、督查记录等）
├── tools/
│   ├── CourseTools.java                  # 课程预约相关工具（Function Calling）
│   └── SupervisionTools.java             # 督查督办相关工具（Function Calling）
└── utils/
    └── VectorDistanceUtils.java          # 向量距离计算工具

src/main/resources/
├── application.yaml                      # 应用配置
└── mapper/                               # MyBatis XML 映射文件
```

## API 接口

### 通用对话

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/ai/chat?prompt=xxx&chatId=xxx&files=xxx` | 流式对话，支持可选附件（多模态） |

### PDF 文档问答

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/ai/pdf/upload/{chatId}` | 上传 PDF 文件，自动解析并写入向量库 |
| GET | `/ai/pdf/file/{chatId}` | 下载已上传的 PDF 文件 |
| POST | `/ai/pdf/chat?prompt=xxx&chatId=xxx` | 基于 PDF 内容的 RAG 问答 |

### 智能客服

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/ai/service?prompt=xxx&chatId=xxx` | 督查督办业务对话（工具调用） |

### 聊天历史

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/ai/history/{type}` | 查询指定类型的会话 ID 列表 |
| GET | `/ai/history/{type}/{chatId}` | 查询指定会话的消息记录 |

## 快速开始

### 前置条件

- Java 21+
- Maven 3.8+
- MySQL（需创建对应数据库）
- Redis（本地 6379 端口）

### 配置

编辑 `src/main/resources/application.yaml`，修改以下配置：

```yaml
spring:
  ai:
    openai:
      api-key: your-api-key          # 阿里云 DashScope API Key
      chat:
        options:
          model: qwen3.5-plus        # 可根据需要更换模型
  datasource:
    url: jdbc:mysql://localhost:3306/your-db   # MySQL 数据库地址
    username: root
    password: your-password
```

### 启动

```bash
mvn spring-boot:run
```

服务默认运行在 `http://localhost:8080`。

## 架构设计要点

### 会话记忆持久化

项目从内存存储迁移到 Redis 持久化存储，核心实现为 `RedisChatMemoryRepository`：

- 每个会话对应一个 Redis Hash：`spring_ai_chat_memory:{conversationId}`
- Hash 的 field 格式为 `{index}:type` 和 `{index}:text`，按索引排序保证消息顺序
- 所有会话 ID 通过一个 Redis Set（`spring_ai_chat_memory:__ids__`）维护，支持列表查询
- 通过 Spring AI 的 `ChatMemoryRepository` 接口接入 `MessageWindowChatMemory`，实现窗口化记忆（最多 50 条）

### 向量存储

使用 `RedisVectorStore` 作为向量数据库：

- 索引名称：`pdf-documents-index`，Key 前缀：`pdf_embedding:`
- 向量算法：HNSW，距离度量：COSINE
- 元数据字段：`file_name`（TEXT 类型，支持中文文件名过滤）
- PDF 读取器按页拆分文档，每页作为一个 Document 写入向量库

### 三个独立的 ChatClient

| Bean 名 | 用途 | 系统提示词 | 特殊能力 |
|---------|------|-----------|---------|
| `chatClient` | 通用对话 | "你是一个专业的智能助手，你的名字叫小智" | 多模态支持 |
| `pdfChatClient` | PDF 问答 | "请根据上下文回答问题..." | RAG 向量检索 |
| `serviceChatClient` | 业务客服 | 督查督办系统提示词 | Function Calling（SupervisionTools） |

## 已知问题

- PDF 文件存储到本地磁盘根目录（与项目同级），建议后续改为专门的上传目录
- CORS 配置允许所有来源（`*`），生产环境应限制具体域名
- `CourseTools` 及相关课程预约功能已实现但未启用（在 `CommonConfiguration` 中被注释）
