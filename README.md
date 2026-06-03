# 🐋 智聘 — 智能就业服务平台

> 基于 Spring Boot 3 + Vue 3 的全栈招聘平台，集成 WebSocket 实时通信与 AI 简历优化

![Java](https://img.shields.io/badge/Java-17-orange) ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen) ![MySQL](https://img.shields.io/badge/MySQL-8.0-blue) ![Redis](https://img.shields.io/badge/Redis-7-red) ![Vue](https://img.shields.io/badge/Vue-3.x-42b883) ![License](https://img.shields.io/badge/License-MIT-yellow)

## 📖 项目简介

智聘是一个面向**校园招聘**场景的全栈 Web 应用，模拟 B 端（HR）+ C 端（求职者）+ 平台管理端的完整业务链路。

- **三角色业务闭环**：求职者投递简历、HR 审核职位、平台管理员管控全流程
- **实时通信**：基于 STOMP/WebSocket 实现站内信、面试通知的实时推送
- **AI 集成**：对接大语言模型实现 AI 简历优化、职位匹配摘要
- **云存储**：阿里云 OSS 完成简历附件上传与 PDF 预览

## ✨ 核心功能

### 👤 求职者端
- 注册 / 登录（支持用户名密码 + 手机验证码两种方式）
- 浏览 / 搜索 / 收藏职位
- 投递简历（支持 PDF / Word 附件）
- 实时聊天（HR ↔ 求职者）
- 查看面试通知
- AI 简历优化建议

### 🏢 HR 端
- 企业认证流程
- 职位发布 / 编辑 / 下架
- 简历库搜索与匹配
- 候选人沟通
- 面试邀约
- 数据看板（投递转化漏斗）

### 🔧 管理端
- 用户 / HR / 企业全量管理
- 职位审核
- 操作日志（基于 AOP 自动记录）
- 数据概览与趋势分析

## 🛠️ 技术栈

### 后端
| 类别 | 技术 |
|------|------|
| 框架 | Spring Boot 3 / Spring MVC |
| 持久层 | MyBatis |
| 数据库 | MySQL 8 |
| 缓存 | Redis (Lettuce) |
| 鉴权 | JWT (jjwt) |
| 实时通信 | WebSocket + STOMP |
| 切面编程 | Spring AOP |
| 工具 | Hutool / Fastjson2 / PageHelper / Lombok |
| 云服务 | 阿里云 OSS |
| AI 集成 | MiniMax API |

### 前端（独立仓库）
| 类别 | 技术 |
|------|------|
| 框架 | Vue 3 + Vite |
| UI 库 | Element Plus |
| 图表 | ECharts |
| 通信 | Axios + STOMP/SockJS |
| 路由 | Vue Router（按角色模块化） |

## 🏗️ 架构亮点

### 1. 基于 AOP + 自定义注解的操作日志

```java
@OperationLog("删除用户")
public Result<Void> deleteUser(Long id) {
    // 业务逻辑
}
```

通过 `@OperationLog` 注解 + `OperationLogAspect` 切面，**自动**记录"谁、什么时间、改了什么"，无需在业务代码中手动埋点。

### 2. 策略模式封装的 AI Prompt 体系

```
ai/
├── prompt/
│   ├── ResumePromptStrategy.java          # 接口
│   ├── DefaultResumePromptStrategy.java   # 简历优化实现
│   └── MatchSummaryPromptStrategy.java    # 职位匹配实现
└── capability/
    ├── ResumeOptimizeCapability.java
    └── MatchSummaryCapability.java
```

不同 AI 能力通过**策略模式**解耦，新增 Prompt 模板不影响业务代码。

### 3. JWT 完整鉴权流

- **后端**：登录签发 Token → 拦截器校验 → WebSocket 握手拦截器同步鉴权
- **前端**：localStorage 存储 → 路由守卫解析过期 → 请求拦截器自动携带 → 401 自动跳转登录

### 4. WebSocket 实时推送

- 站内信、面试通知、聊天消息走 STOMP 主题订阅
- 后端：`/ws` 端点 + `WebSocketHandshakeInterceptor` 握手鉴权
- 前端：SockJS 兼容 + 自动重连

## 📁 项目结构

```
smart-employment-platform/
├── src/main/java/com/itzk/SmartEmploymentPlatform/
│   ├── controller/    # 控制器（按 admin/recruitment/employment 三角色分包）
│   ├── service/       # 业务接口
│   │   └── Impl/      # 业务实现
│   ├── mapper/        # MyBatis 持久层
│   ├── pojo/
│   │   ├── entry/     # 数据库实体
│   │   ├── entryDTO/  # 入参 DTO
│   │   └── vo/        # 出参 VO
│   ├── config/        # WebSocket / Redis / 拦截器配置
│   ├── aspect/        # AOP 切面
│   ├── annotation/    # 自定义注解
│   ├── ai/            # AI 集成（prompt 策略 + capability）
│   ├── task/          # 定时任务
│   └── utils/         # 工具类
├── src/main/resources/
│   ├── application.yaml.example   # 配置模板（必读）
│   ├── application-docker.yaml   # Docker 部署配置
│   ├── com/.../mapper/*.xml       # MyBatis XML
│   └── static/ / templates/       # 静态资源 / 模板
└── src/test/                       # 单元测试
```

## 🚀 快速开始

### 环境要求
- JDK 17+
- Maven 3.8+
- MySQL 8.0+
- Redis 7+

### 启动步骤

**1. 克隆仓库**
```bash
git clone https://github.com/zengxiaokan/smart-employment-platform.git
cd smart-employment-platform
```

**2. 准备配置文件**
```bash
cp src/main/resources/application.yaml.example src/main/resources/application.yaml
```

然后编辑 `application.yaml`，填入你的：
- MySQL 连接信息（host / port / username / password）
- Redis 连接信息（同上）
- `jwt.secret`（建议 32 位以上随机字符串）
- 阿里云 OSS 配置（如需使用文件上传功能）
- AI API Key（如需使用 AI 优化功能）

**3. 初始化数据库**

根据 `src/main/java/.../pojo/entry/` 下的实体类创建对应的表结构。

**4. 启动后端**
```bash
mvn spring-boot:run
```

**5. 访问应用**

打开浏览器访问 `http://localhost:8080`

## 🔐 角色说明

| 角色 | 入口路径 | 说明 |
|------|----------|------|
| 求职者 | `/user` | 注册时选择"求职者" |
| HR | `/hr` | 注册时选择"HR"，需先完成企业认证 |
| 管理员 | `/admin` | 由数据库初始化指定 |

## ⚠️ 重要说明

- 仓库中**不含任何真实密钥**——所有敏感配置通过 `application.yaml.example` 模板管理
- 启动前**必须**复制 `.example` 到 `application.yaml` 并填入真实值
- `.gitignore` 已配置排除 `application*.yaml`、`target/`、`deploy/`、`docs/` 等本地/部署/内部资料
- 数据库初始化脚本、部署脚本、API 文档未包含在公开仓库中（避免敏感信息泄露）

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

---

> 🐋 登录页那只动态鲸鱼是本项目与 AI 编程协作的起点纪念
