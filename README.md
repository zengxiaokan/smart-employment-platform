# 🐋 智聘 — 智能就业服务平台

> 基于 **Spring Boot 3 + Vue 3** 的全栈招聘平台，集成 WebSocket 实时通信与 AI 简历优化

![Java](https://img.shields.io/badge/Java-17-orange) ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen) ![MySQL](https://img.shields.io/badge/MySQL-8.0-blue) ![Redis](https://img.shields.io/badge/Redis-7-red) ![Vue](https://img.shields.io/badge/Vue-3.x-42b883) ![License](https://img.shields.io/badge/License-MIT-yellow)

## 📂 仓库结构

本仓库是 **mono-repo**，包含两个独立子项目：

| 子项目 | 技术栈 | 说明 |
|--------|--------|------|
| [`backend/`](backend/) | Spring Boot 3 + MyBatis + MySQL + Redis + WebSocket + JWT | 后端 API 服务 |
| [`frontend/`](frontend/) | Vue 3 + Vite + Element Plus + ECharts + STOMP | 前端 SPA + 已构建的 dist/ |

> 💡 **想直接看效果？** 打开 [`frontend/dist/index.html`](frontend/dist/index.html) 即可预览（无需启动后端，仅前端静态页面）

## ✨ 核心功能

- 👤 **三角色业务闭环**：求职者投递简历、HR 审核职位、平台管理员管控全流程
- 💬 **实时通信**：基于 STOMP/WebSocket 实现站内信、面试通知、聊天的实时推送
- 🤖 **AI 集成**：对接大语言模型实现 AI 简历优化、职位匹配摘要
- ☁️ **云存储**：阿里云 OSS 完成简历附件上传与 PDF 预览
- 📊 **AOP 操作日志**：基于 `@OperationLog` 自定义注解 + 切面自动记录

## 🚀 快速开始

### 后端

```bash
cd backend
cp src/main/resources/application.yaml.example src/main/resources/application.yaml
# 编辑 application.yaml 填入 MySQL/Redis/JWT/OSS/AI 密钥
mvn spring-boot:run
```

详细说明见 [backend/README.md](backend/README.md)

### 前端

```bash
cd frontend
npm install
npm run dev          # 开发模式
# 或直接打开 dist/index.html 看已构建版本
```

详细说明见 [frontend/README.md](frontend/README.md)

## 🏗️ 架构亮点

### AOP + 自定义注解
```java
@OperationLog("删除用户")
public Result<Void> deleteUser(Long id) { ... }
```

### 策略模式封装的 AI Prompt
```
ai/prompt/
├── ResumePromptStrategy.java          # 接口
├── DefaultResumePromptStrategy.java   # 简历优化
└── MatchSummaryPromptStrategy.java    # 职位匹配
```

### JWT 完整鉴权流
后端登录签发 → 拦截器校验 → WebSocket 握手拦截器同步鉴权；前端路由守卫解析过期 + 401 自动跳转登录。

## 🛠️ 技术栈一览

**后端**：Spring Boot 3 / Spring MVC / MyBatis / MySQL 8 / Redis 7 / JWT / WebSocket+STOMP / Spring AOP / 阿里云 OSS / MiniMax AI

**前端**：Vue 3 / Vite / Element Plus / ECharts / Axios / STOMP-SockJS / Vue Router

## 📸 界面预览

> 🐋 登录页那只动态鲸鱼是本项目与 AI 编程协作的起点纪念
>
> 打开 [`frontend/dist/index.html`](frontend/dist/index.html) 即可看到效果

## 🛣️ 路线图

**已实现**
- ✅ 三角色业务闭环
- ✅ JWT 鉴权 + WebSocket 实时通信
- ✅ AOP 操作日志
- ✅ AI 简历优化 / 职位匹配

**计划中**
- 🔲 消息队列（RabbitMQ）异步化 AI 任务
- 🔲 Elasticsearch 全文搜索
- 🔲 Redisson 分布式锁（防重复投递）
- 🔲 Spring Security / Sa-Token 权限框架

## 📝 License

本项目基于 [MIT License](LICENSE) 开源 — 随便用、随便改、能帮就帮、能学就学。
