# 求职者端功能文档

本文档面向管理员，详细说明求职者端（用户端）的功能架构、页面结构和操作逻辑。

---

## 页面路由

| 路由 | 页面 | 说明 |
|------|------|------|
| `/` | 首页 | 推荐岗位 + 热门岗位 + 统计数据 |
| `/jobs` | 职位广场 | 搜索、筛选、投递 |
| `/job/:id` | 职位详情 | 职位完整信息 |
| `/companies` | 公司列表 | 企业浏览与搜索 |
| `/company/:id` | 公司详情 | 企业信息与在招职位 |
| `/profile` | 个人中心 | 多功能侧边栏布局 |

---

## 功能模块

### 首页

**路由**：`/`

- 顶部搜索栏：输入职位关键词/城市跳转到职位广场搜索
- 统计数据：平台实时四组数据（在招岗位、入驻企业、求职者数、成功匹配数），数据来自 Redis 缓存（key: `home:stats`），TTL 30 分钟
- 推荐岗位：调用 `/jobs/recommend` 接口，AI 技能匹配排序，展示匹配分数（绿色高/黄色中/灰色低），卡片含浏览数、投递数
- 热门岗位：按投递数和浏览量排序，卡片展示浏览/投递数据

### 职位广场

**路由**：`/jobs`

- 搜索条件：职位名称关键词、城市、学历要求、经验要求
- 列表卡片：薪资范围、城市、经验、技能标签、浏览数/投递数
- 详情弹窗：点击卡片弹出，展示完整职位信息
- 投递操作：在弹窗内点击"投递简历"，选择一份简历完成投递
- 立即沟通：点击后创建/打开与 HR 的实时聊天会话

### 公司浏览

**路由**：`/companies`、`/company/:id`

- 企业列表：按行业、规模等筛选
- 企业详情：基本信息（Logo、名称、行业、规模、融资阶段、官网、地址）、在招职位列表
- 收藏功能：收藏感兴趣的公司

### 个人中心

**路由**：`/profile`

左侧菜单项：

| 菜单 | 组件 | 功能 |
|------|------|------|
| 个人资料编辑 | `profile/ProfileEdit.vue` | 头像上传、基本信息、联系方式 |
| 求职意向表 | `profile/JobIntention.vue` | 期望职位、城市、薪资、工作性质、到岗时间 |
| 我的简历 | `profile/ResumeList.vue` | 多份简历管理 |
| 岗位投递记录 | `profile/ApplicationList.vue` | 投递状态跟踪 |
| 面试通知 | `profile/InterviewList.vue` | 面试邀约管理 |
| 收藏岗位 | `profile/FavoriteList.vue` | 已收藏职位 |
| 修改密码 | `profile/ChangePassword.vue` | 密码修改 |
| 意见反馈 | `profile/Feedback.vue` | 提交反馈 |
| 使用帮助 | `profile/HelpGuide.vue` | Markdown 使用指南 |

### 简历管理

**路由**：`/resume`、`/resume/create`、`/resume/edit/:id`

- 简历主表：resume_name、姓名、手机、邮箱、年龄、性别、技能标签、自我评价、求职意向、期望城市、薪资
- 教育经历子表：学校、专业、学历（0-7）、入学/毕业时间
- 工作经历子表：公司、职位、起止时间、工作描述
- 项目经历子表：项目名称、角色、起止时间、项目描述
- AI 优化：智能优化简历描述内容
- PDF 导出：调用后端 OpenHTMLToPDF 生成 PDF 文件

### 投递状态流转

```
已投递(0) → HR已查看(1) → 面试中(2) → 已录用(5)
                       ↘ 不合适(3)
                       ↘ 已取消(4)
```

投递记录可重新投递（对于"不合适"或"已取消"状态）。

### 面试管理

面试状态流转（求职者视角）：

```
待确认(0) → 已接受(1) / 已拒绝(2) / 已过期(3) / 已取消(4)
```

- 待确认：HR 发来的面试邀约，等待求职者回复
- 接受/拒绝：求职者操作，可填写备注
- 取消面试：求职者取消已接受的面试
- 重新投递：对于已过期/已拒绝的面试，可在"已结束"标签页重新投递

### 即时通讯

- 技术实现：STOMP over WebSocket
- 入口：职位详情弹窗"立即沟通"、顶部导航栏消息下拉
- 会话管理：conversations 表（双向唯一去重）、messages 表
- 消息类型：文字消息（0）、系统通知（1）
- 未读计数：user1_unread_count / user2_unread_count
- 消息状态：实时送达，支持未读提醒

---

## 数据结构关联

### 核心数据流

```
users (求职者 role=0)
  ├── resumes (简历主表)
  │     ├── resume_educations
  │     ├── resume_experiences
  │     └── resume_projects
  ├── applications (投递记录)
  │     └── interview (面试邀约)
  ├── job_favorites (收藏职位)
  ├── conversations → messages (聊天)
  └── feedbacks (用户反馈)
```

### 关键 API

| 接口 | 方法 | 说明 |
|------|------|------|
| `/jobs/list` | GET | 职位列表（分页、搜索、筛选） |
| `/jobs/recommend` | GET | AI 推荐岗位 |
| `/jobs/hot` | GET | 热门岗位 |
| `/jobs/detail` | GET | 职位详情 |
| `/jobs/apply` | POST | 投递简历 |
| `/resume/list` | GET | 简历列表 |
| `/resume/save` | POST | 创建/更新简历 |
| `/resume/export-pdf` | GET | 简历 PDF 导出 |
| `/application/list` | GET | 投递记录 |
| `/interview/list` | GET | 面试列表 |
| `/interview/reply` | PUT | 回复面试邀约 |
| `/user/profile` | GET/PUT | 个人资料 |
| `/company/list` | GET | 企业列表 |
| `/feedback/submit` | POST | 提交反馈 |

---

> **最后更新**：2026年5月
