# 🎵 EasyMusic (易乐) — 智能AI音乐创作与高并发实时通信平台

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-green.svg)]()
[![Netty](https://img.shields.io/badge/Netty-4.1.x-blue.svg)]()
[![Milvus](https://img.shields.io/badge/Milvus-2.3.x-cyan.svg)]()
[![RabbitMQ](https://img.shields.io/badge/RabbitMQ-3.12-orange.svg)]()
[![Vue](https://img.shields.io/badge/Vue-3.x-brightgreen.svg)]()
[![Docker](https://img.shields.io/badge/Docker-Orchestrated-blue.svg)]()

`EasyMusic (易乐)` 是一款集 **AI 音乐个性化创作、智能推荐、社交聊天与歌曲点评** 于一体的现代化 Web 3.0 音乐互动平台。项目采用前沿的 AI 智能体（Agent）设计，结合高性能网络通信架构与高可靠性分布式系统设计，致力于提供极致流畅的 AI 生成式音乐体验。

---

## 🏗 系统架构与核心技术拓扑

本系统采用经典的分层分布式架构，融合了消息驱动设计（Event-Driven）与向量检索增强生成（RAG）：

```mermaid
graph TD
    subgraph 客户端 (Client)
        VueWeb[用户端: Vue 3 / Vite]
        VueAdmin[管理端: Vue 3 / Vite]
    end

    subgraph 网关与代理 (Gateway / Proxy)
        NginxWeb[easymusic-front-web Nginx]
        NginxAdmin[easymusic-front-admin Nginx]
    end

    subgraph 后端核心服务 (Core Backend)
        JavaWeb[easymusic-web: HTTP & Netty WS]
        JavaAdmin[easymusic-admin: 管理 API]
    end

    subgraph 分布式中间件 & 数据库 (Middleware & Storage)
        MySQL[(MySQL 8.0 主库)]
        Redis[(Redis 7.0 路由与缓存)]
        RabbitMQ[(RabbitMQ 3.12 消息总线)]
        Milvus[(Milvus 2.3.5 向量检索)]
    end

    VueWeb -->|Port 80| NginxWeb
    VueAdmin -->|Port 8082| NginxAdmin
    
    NginxWeb -->|Proxy API /api| JavaWeb
    NginxWeb -->|Proxy WebSocket /ws| JavaWeb
    
    JavaWeb --> MySQL
    JavaWeb --> Redis
    JavaWeb --> RabbitMQ
    JavaWeb --> Milvus
```

---

## 🌟 核心功能与技术亮点

### 1. 🤖 个性化 AI 音乐推荐智能体 (RAG Agent)
基于大语言模型（LLM）与向量数据库构建的音乐风格推荐智能体，在用户开启创作时主动推荐灵感。
- **RAG 检索增强生成**：将平台生成的歌曲 Prompt、风格标签进行词向量化（Embedding），同步存储于 **Milvus 向量数据库** 中，根据用户的偏好画像进行近邻检索（ANN Search）。
- **增量偏好画像更新 (增量修改法)**：用户点赞、收藏歌曲时，系统设置 Redis 脏标记并延迟触发画像更新。通过 `last_action_id` 游标实现**增量数据过滤**，仅将新动作的差量（Delta）通过 Kimi LLM 融合进用户画像，**规避了大模型上下文窗口溢出，降低了 Token 消耗，并防止了历史偏好被完全覆盖**。

### 2. 📬 基于发件箱模式的可靠 AI 音乐生成引擎 (Outbox Pattern)
保障 AI 歌曲生成与积分扣减在强并发下的最终一致性。
- **发件箱模式（Outbox Pattern）**：扣减积分、生成音乐记录与插入本地消息表（`local_message`）在同一个 MySQL 事务中提交，确保消息 100% 不丢失。
- **双重补偿与延迟重试**：利用 Spring 事务同步监听器（Transaction Synchronization）在事务提交后向 RabbitMQ 发送消息。通过 **RabbitMQ 延迟队列**（30秒轮询检查、5分钟强制超时）与后台定时任务兜底，实现 AI 生成超时自动退款与重试。

### 3. ⚡ 高并发实时 IM & 点评通信引擎 (Netty + WebSocket)
基于 Netty 构建的专用 WebSocket 实时通信通道（运行在独立端口 `8099`），支持单节点万级并发。
- **主从 Reactor 线程模型**：`bossGroup` 负责处理连接接入，`workerGroup` 配合 `PooledByteBufAllocator` 进行内存池化管理，调优 TCP 参数（`SO_BACKLOG = 1024`, `SO_KEEPALIVE = true`, `TCP_NODELAY = true`）最大化提升吞吐量。
- **精细化连接管理**：采用 `IdleStateHandler` 进行 60 秒心跳检测，秒级剔除空闲或“假在线”连接，防止死连接占用系统文件描述符（FD）。
- **精准路由与跨节点投递**：基于 Redis 维护 `im:route:{userId} -> {nodeAddress}` 动态路由表。每个 Netty 节点在启动时动态向 RabbitMQ 声明一个**临时、排他、自动删除**的队列，绑定 Direct 交换机进行点对点跨节点投递，绑定 Fanout 交换机进行歌曲点评房间的实时弹幕广播。
- **可靠消息投递 (Manual Ack)**：开启 RabbitMQ 手动确认模式，在 WebSocket 发送成功且 MySQL 消息状态更新为已读后方执行 Ack。用户离线时消息保留在 MySQL，重连时自动触发离线消息补偿推送。

### 4. 🐳 一键式容器化项目管理 (Docker Compose)
整个项目支持一键 Docker 部署。
- 包含了对前端 Nginx 多阶段构建、后台 JRE 21 运行环境、数据库自动导入 SQL 结构，以及包含 Milvus 及其 Etcd/MinIO 依赖在内的全栈式环境编排。

---

## 🛠 技术栈一览

| 维度 | 技术选型 |
| :--- | :--- |
| **后端核心** | Spring Boot 3.x, Spring MVC, MyBatis |
| **网络通信** | Netty 4.1.x (WebSocket, Socket Options Tuning, Memory Pooling) |
| **数据存储** | MySQL 8.0, Redis 7.0 (Lettuce), MinIO (对象存储) |
| **向量检索** | Milvus 2.3.5 (Vector Search), LangChain4j (RAG, Embedding) |
| **消息队列** | RabbitMQ 3.12 (Direct/Fanout, Manual Ack, Delay Queue) |
| **前端框架** | Vue 3, Vite, Element Plus, TailwindCSS, Axios |
| **开发与部署** | Docker, Docker Compose, Nginx |

---

## 🚀 快速启动与部署

### 前提条件
- 确保本地已安装并启动 Docker 与 Docker Desktop（Windows 环境下需开启 WSL 2 支持）。
- 确保本地已配置 JDK 21+ 与 Maven 环境。

### 部署步骤

#### 1. 克隆并进入项目根目录
```bash
git clone <your-github-repo-url>
cd easymusic
```

#### 2. 本地编译打包 Java 模块
```bash
cd easymusic-java
mvn clean package -DskipTests
cd ..
```

#### 3. 启动 Docker Compose 一键编排
在项目根目录下执行：
```bash
docker-compose up --build -d
```
> 容器启动时会自动读取并执行根目录下的 `easymusic.sql` 脚本，无需手动创建表和导入数据。

#### 4. 查看容器状态
```bash
docker-compose ps
```

---

## 🔗 服务端口与管理地址

容器编排启动成功后，您可以通过以下地址访问各系统及管理面板：

| 服务名称 | 访问地址 | 说明 |
| :--- | :--- | :--- |
| **用户端前端主页** | [http://localhost](http://localhost) (Port 80) | 提供音乐播放、AI创作及实时聊天互动 |
| **管理后台前端主页** | [http://localhost:8082](http://localhost:8082) (Port 8082) | 系统管理及作品审核 |
| **RabbitMQ 控制台** | [http://localhost:15672](http://localhost:15672) | 账号密码：`guest`/`guest` |
| **MinIO 存储控制台** | [http://localhost:9001](http://localhost:9001) | 账号密码：`minioadmin`/`minioadmin` |
| **Netty WebSocket 端口** | `ws://localhost:8099/ws` | IM 引擎直接通信端口 |

---

## 🧪 自动化测试与功能验证

本项目在 `C:\Users\sgy\.gemini\antigravity\brain\<conversation-id>\scratch\` 目录下自带了 WebSocket 和消息投递的集成测试脚本。

在部署好 Docker 环境后，您可以使用 Node.js 运行集成测试：
```bash
node ./easymusic-front/easymusic-front-web/test_im_engine.js
# 或使用本地对应的 scratch 测试脚本路径
```
**测试项覆盖**：
1. 双客户端成功接入及握手 Token 校验。
2. 歌曲点评房间（`JOIN_ROOM`）实时弹幕广播验证。
3. 精准路由下点对点私聊消息的可靠投递。
4. 客户端离线期间消息在 MySQL 暂存，客户端上线后可靠重传机制验证。
