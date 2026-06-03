# 数据库设计文档

## 一、数据库概述

数据库名称：`recruitment_db`

技术选型：

- MySQL 8.0
- UTF8MB4 字符集
- InnoDB 存储引擎

设计原则：

- 采用逻辑外键，减少数据库级联压力
- 高频查询字段建立索引
- 适配 Redis 缓存与 AI 推荐业务
- 支持高并发招聘场景

核心模块：

- 用户与权限模块
- 企业与职位模块
- 简历模块（主表 + 子表）
- 职位投递模块
- 面试邀约模块
- 即时通讯模块
- AI 智能匹配模块
- 收藏与人才库模块
- 系统反馈与日志模块

---

## 二、数据表清单

| 序号 | 数据表名 | 中文名称 | 说明 |
|------|----------|----------|------|
| 1 | users | 用户表 | 求职者、HR、管理员统一用户中心 |
| 2 | companies | 企业信息表 | 企业认证、主页展示、招聘统计 |
| 3 | jobs | 职位表 | 招聘岗位，核心业务表 |
| 4 | resumes | 简历主表 | 求职者简历基本信息 |
| 5 | resume_educations | 简历教育经历表 | 简历子表，一对多 |
| 6 | resume_experiences | 简历工作经历表 | 简历子表，一对多 |
| 7 | resume_projects | 简历项目经历表 | 简历子表，一对多 |
| 8 | applications | 职位投递记录表 | 投递行为记录、状态流转 |
| 9 | interview | 面试邀约表 | 面试通知、状态流转 |
| 10 | job_favorites | 职位收藏表 | 用户收藏职位，多对多 |
| 11 | company_resume | 企业人才库表 | HR 收藏候选人，多对多 |
| 12 | conversations | 会话表 | WebSocket 聊天会话 |
| 13 | messages | 消息表 | 聊天消息记录 |
| 14 | match_records | AI 匹配记录表 | 人岗匹配评分 |
| 15 | match_skill_details | 技能匹配明细表 | 逐项技能匹配结果 |
| 16 | company_todos | 企业待办事项表 | HR 待办提醒 |
| 17 | feedbacks | 用户反馈工单表 | Bug 反馈、举报、建议 |
| 18 | operation_logs | 系统操作日志表 | 管理员操作审计 |

---

## 三、表结构详细说明

### 1. users（用户表）

| 字段名 | 数据类型 | 说明 | 备注 |
|--------|----------|------|------|
| id | bigint | 主键 | 自增 |
| username | varchar(50) | 用户名 | 唯一 |
| password | varchar(255) | 密码 | BCrypt 加密 |
| phone | char(11) | 手机号 | 唯一 |
| email | varchar(100) | 邮箱 | 唯一 |
| nickname | varchar(50) | 昵称 | |
| avatar_url | varchar(500) | 头像 URL | |
| role | tinyint | 用户角色 | 0=求职者, 1=HR, 2=管理员 |
| status | tinyint | 用户状态 | 0=正常, 1=封禁 |
| realname | varchar(20) | 真实姓名 | |
| gender | tinyint | 性别 | |
| city | varchar(100) | 所在城市 | |
| age | tinyint | 年龄 | |
| company_id | int | 所属企业 ID | HR 使用 |
| company_name | varchar(50) | 企业名称 | 冗余字段 |
| created_at | datetime | 注册时间 | |
| updated_at | datetime | 更新时间 | |

### 2. companies（企业信息表）

| 字段名 | 数据类型 | 说明 | 备注 |
|--------|----------|------|------|
| id | bigint | 企业 ID | 自增 |
| name | varchar(100) | 企业名称 | |
| description | text | 企业简介 | |
| industry | varchar(50) | 所属行业 | |
| size | varchar(30) | 企业规模 | 0=0-20人, 1=20-99人, 2=100-499人, 3=500-999人, 4=1000人以上 |
| city | varchar(50) | 所在城市 | |
| address | varchar(200) | 企业地址 | |
| logo_url | varchar(500) | 企业 Logo | |
| license_url | varchar(500) | 营业执照 | |
| audit_status | tinyint | 审核状态 | 0=审核中, 1=已通过 |
| financing_stage | tinyint | 融资阶段 | 1=种子轮 ~ 5=C轮以上 |
| official_web | varchar(50) | 企业官网 | |
| phone | varchar(30) | 联系电话 | 唯一 |
| user_id | bigint | 企业 HR 用户 ID | |
| job_count | int | 岗位数量 | 发布职位时 +1，下架/删除时 -1 |
| job_confirm | int | 已招聘人数 | 面试通过后 +1 |
| created_at | datetime | 创建时间 | |
| updated_at | datetime | 更新时间 | |

### 3. jobs（职位表）

| 字段名 | 数据类型 | 说明 | 备注 |
|--------|----------|------|------|
| id | bigint | 职位 ID | 自增 |
| company_id | bigint | 企业 ID | 逻辑外键 |
| hr_user_id | bigint | HR 用户 ID | |
| title | varchar(100) | 职位名称 | |
| category | varchar(50) | 职位分类 | |
| salary_min | int | 最低薪资 | K/月 |
| salary_max | int | 最高薪资 | K/月 |
| city | varchar(50) | 工作城市 | |
| address | varchar(200) | 工作地址 | |
| experience | varchar(30) | 经验要求 | |
| education | tinyint | 学历要求 | 0=不限 ~ 7=博士 |
| tags | varchar(255) | 职位标签 | 逗号分隔 |
| job_skills | varchar(255) | 技能要求 | |
| headcount | int | 招聘人数 | |
| duty_content | text | 岗位职责 | |
| require_content | text | 任职要求 | |
| benefits | text | 福利待遇 | |
| status | tinyint | 职位状态 | 0=审核中, 1=招聘中, 2=已下架, 3=强制下架 |
| view_count | int | 浏览次数 | |
| apply_count | int | 投递次数 | |
| has_count | int | 已招人数 | |
| created_at | datetime | 发布时间 | |
| updated_at | datetime | 更新时间 | |

### 4. resumes（简历主表）

| 字段名 | 数据类型 | 说明 | 备注 |
|--------|----------|------|------|
| id | bigint | 简历 ID | 自增 |
| user_id | bigint | 用户 ID | 逻辑外键 |
| resume_name | varchar(50) | 简历标题 | |
| name | varchar(50) | 姓名 | |
| phone | varchar(20) | 手机号 | |
| email | varchar(100) | 邮箱 | |
| age | int | 年龄 | |
| gender | tinyint | 性别 | 0=女, 1=男 |
| skills | varchar(255) | 技能列表 | 逗号分隔 |
| self_description | text | 自我评价 | |
| job_intention | varchar(100) | 求职意向 | |
| city | varchar(50) | 期望城市 | |
| salary_min | int | 期望最低薪资 | |
| salary_max | int | 期望最高薪资 | |
| character_avatar | varchar(255) | 头像地址 | |
| is_default | tinyint | 是否默认简历 | 0=否, 1=是 |
| available_from | varchar(30) | 到岗时间 | |
| job_type | tinyint | 工作类型 | 0=不限, 1=全职, 2=兼职, 3=实习 |
| industry | varchar(30) | 期望行业 | |
| graduation_school | varchar(50) | 毕业院校 | |
| total_work_years | int | 总工作年限 | 冗余字段，避免联表 |
| max_education | tinyint | 最高学历 | 冗余字段，0~6 |
| created_at | datetime | 创建时间 | |
| updated_at | datetime | 更新时间 | |

### 5. resume_educations（教育经历表）

| 字段名 | 数据类型 | 说明 | 备注 |
|--------|----------|------|------|
| id | bigint | 主键 | 自增 |
| resume_id | bigint | 简历 ID | 逻辑外键 |
| school | varchar(100) | 学校名称 | |
| major | varchar(100) | 专业 | |
| education | tinyint | 学历 | 0=初中 ~ 6=博士 |
| description | text | 奖励及任职情况 | |
| start_time | date | 入学时间 | |
| end_time | date | 毕业时间 | |
| created_at | datetime | 创建时间 | |

### 6. resume_experiences（工作经历表）

| 字段名 | 数据类型 | 说明 | 备注 |
|--------|----------|------|------|
| id | bigint | 主键 | 自增 |
| resume_id | bigint | 简历 ID | 逻辑外键 |
| company | varchar(100) | 公司名称 | |
| position | varchar(50) | 职位名称 | |
| start_time | date | 入职时间 | |
| end_time | date | 离职时间 | |
| description | text | 工作描述 | |
| created_at | datetime | 创建时间 | |

### 7. resume_projects（项目经历表）

| 字段名 | 数据类型 | 说明 | 备注 |
|--------|----------|------|------|
| id | bigint | 主键 | 自增 |
| resume_id | bigint | 简历 ID | 逻辑外键 |
| name | varchar(100) | 项目名称 | |
| role | varchar(50) | 项目角色 | |
| start_time | date | 开始时间 | |
| end_time | date | 结束时间 | |
| description | text | 项目描述 | |
| created_at | datetime | 创建时间 | |

### 8. applications（投递记录表）

| 字段名 | 数据类型 | 说明 | 备注 |
|--------|----------|------|------|
| id | bigint | 投递记录 ID | 自增 |
| job_id | bigint | 职位 ID | |
| resume_id | bigint | 简历 ID | |
| user_id | bigint | 求职者 ID | |
| company_id | bigint | 企业 ID | |
| status | tinyint | 投递状态 | 0=已投递, 1=已查看, 2=面试中, 3=不合适, 4=已取消, 5=已录用 |
| interview_time | datetime | 面试时间 | |
| hr_remark | varchar(255) | HR 备注 | |
| applied_at | datetime | 投递时间 | |
| viewed_at | datetime | HR 首次查看时间 | |
| updated_at | datetime | 更新时间 | |
| operator_id | bigint | 操作人 ID | |

### 9. interview（面试邀约表）

| 字段名 | 数据类型 | 说明 | 备注 |
|--------|----------|------|------|
| id | bigint | 面试 ID | 自增 |
| application_id | bigint | 投递记录 ID | |
| company_id | bigint | 企业 ID | 冗余 |
| job_id | bigint | 职位 ID | 冗余 |
| user_id | bigint | 求职者 ID | 冗余 |
| hr_user_id | bigint | HR 用户 ID | |
| interview_time | datetime | 面试时间 | |
| interview_location | varchar(200) | 面试地点 | |
| location_type | tinyint | 地点类型 | 0=线下, 1=线上 |
| contact_person | varchar(50) | 联系人 | |
| contact_phone | varchar(20) | 联系电话 | |
| remark | varchar(255) | HR 备注 | |
| status | tinyint | 面试状态 | 0=待确认, 1=已接受, 2=已拒绝, 3=已过期, 4=已取消 |
| candidate_remark | varchar(255) | 求职者备注 | |
| response_time | datetime | 回复时间 | |
| created_at | datetime | 创建时间 | |
| updated_at | datetime | 更新时间 | |

### 10. job_favorites（职位收藏表）

| 字段名 | 数据类型 | 说明 | 备注 |
|--------|----------|------|------|
| id | bigint | 主键 | 自增 |
| user_id | bigint | 用户 ID | |
| job_id | bigint | 职位 ID | unique(user_id, job_id) |
| created_at | datetime | 收藏时间 | |

### 11. company_resume（企业人才库表）

| 字段名 | 数据类型 | 说明 | 备注 |
|--------|----------|------|------|
| id | bigint | 主键 | 自增 |
| company_id | bigint | 企业 ID | |
| user_id | bigint | 求职者 ID | |
| resume_id | bigint | 简历 ID | |
| remark | varchar(255) | HR 备注 | |
| created_at | datetime | 创建时间 | |

### 12. conversations（会话表）

| 字段名 | 数据类型 | 说明 | 备注 |
|--------|----------|------|------|
| id | bigint | 会话 ID | 自增 |
| user1_id | bigint | 用户1 | user1_id < user2_id，保证唯一 |
| user2_id | bigint | 用户2 | |
| user1_nickname | varchar(50) | 用户1 昵称 | 冗余 |
| user1_avatar | varchar(500) | 用户1 头像 | 冗余 |
| user2_nickname | varchar(50) | 用户2 昵称 | 冗余 |
| user2_avatar | varchar(500) | 用户2 头像 | 冗余 |
| last_message | text | 最后一条消息 | 列表预览 |
| last_message_time | datetime | 最后消息时间 | 排序用 |
| user1_unread_count | int | 用户1 未读数 | |
| user2_unread_count | int | 用户2 未读数 | |
| created_at | datetime | 创建时间 | |
| updated_at | datetime | 更新时间 | |

### 13. messages（消息表）

| 字段名 | 数据类型 | 说明 | 备注 |
|--------|----------|------|------|
| id | bigint | 消息 ID | 自增 |
| conversation_id | bigint | 会话 ID | |
| sender_id | bigint | 发送者 ID | |
| receiver_id | bigint | 接收者 ID | |
| content | text | 消息内容 | |
| msg_type | tinyint | 消息类型 | 0=文字, 1=系统通知 |
| is_read | tinyint | 是否已读 | 0=未读, 1=已读 |
| sent_at | datetime | 发送时间 | |

### 14. match_records（AI 匹配记录表）

| 字段名 | 数据类型 | 说明 | 备注 |
|--------|----------|------|------|
| id | bigint | 主键 | 自增 |
| job_id | bigint | 岗位 ID | |
| resume_id | bigint | 简历 ID | unique(job_id, resume_id) |
| total_score | decimal(5,2) | 总分 | 满分 100 |
| skill_score | decimal(5,2) | 技能分 | 满分 60 |
| experience_score | decimal(5,2) | 经验分 | 满分 20 |
| education_score | decimal(5,2) | 学历分 | 满分 10 |
| salary_score | decimal(5,2) | 薪资分 | 满分 10 |
| match_summary | varchar(500) | AI 推荐语 | 懒加载生成，200 字内 |
| status | tinyint | 查看状态 | 0=待查看, 1=已查看, 2=已邀请 |
| created_at | datetime | 创建时间 | |

### 15. match_skill_details（技能匹配明细表）

| 字段名 | 数据类型 | 说明 | 备注 |
|--------|----------|------|------|
| id | bigint | 主键 | 自增 |
| match_id | bigint | 匹配记录 ID | |
| skill_name | varchar(50) | 技能名称 | |
| matched | tinyint | 是否匹配 | 0=否, 1=是 |

### 16. company_todos（企业待办事项表）

| 字段名 | 数据类型 | 说明 | 备注 |
|--------|----------|------|------|
| id | bigint | 主键 | 自增 |
| company_id | bigint | 企业 ID | |
| content | varchar(50) | 待办内容 | 最长 50 字 |
| event_time | datetime | 事件时间 | |
| created_at | datetime | 创建时间 | |

### 17. feedbacks（用户反馈工单表）

| 字段名 | 数据类型 | 说明 | 备注 |
|--------|----------|------|------|
| id | bigint | 工单 ID | 自增 |
| user_id | bigint | 用户 ID | |
| type | tinyint | 工单类型 | 0=Bug, 1=功能建议, 2=其他, 3=举报 |
| title | varchar(100) | 标题 | |
| content | text | 内容 | |
| images | varchar(1000) | 图片 URL | |
| status | tinyint | 工单状态 | 0=待处理, 1=处理中, 2=已解决, 3=已关闭 |
| reply | text | 管理员回复 | |
| replied_at | datetime | 回复时间 | |
| target_type | tinyint | 举报目标类型 | |
| target_id | bigint | 举报目标 ID | |
| created_at | datetime | 创建时间 | |
| updated_at | datetime | 更新时间 | |

### 18. operation_logs（系统操作日志表）

| 字段名 | 数据类型 | 说明 | 备注 |
|--------|----------|------|------|
| id | bigint | 日志 ID | 自增 |
| user_id | bigint | 操作人 ID | |
| operator_name | varchar(50) | 操作人名称 | |
| action | varchar(50) | 操作类型 | BAN_USER, DELETE_USER 等 |
| target_type | varchar(30) | 操作对象类型 | user, job 等 |
| target_id | bigint | 对象 ID | |
| target_name | varchar(100) | 对象名称 | |
| ip_address | varchar(45) | IP 地址 | 支持 IPv6 |
| remark | varchar(255) | 操作备注 | |
| created_at | datetime | 操作时间 | |

---

## 四、核心业务关系

### 4.1 用户（users）与企业（companies）

```
companies (1) ──── (N) users
```

HR 用户通过 `users.company_id` 绑定企业。一个企业可有多个 HR。

### 4.2 企业（companies）与职位（jobs）

```
companies (1) ──── (N) jobs
```

一个企业可发布多个职位。`jobs.company_id` 关联。

### 4.3 用户（users）与简历（resumes）

```
users (1) ──── (N) resumes
```

求职者可创建多份简历，`resumes.user_id` 关联。

### 4.4 简历主表与子表

```
resumes (1) ──── (N) resume_educations
resumes (1) ──── (N) resume_experiences
resumes (1) ──── (N) resume_projects
```

主表 + 子表结构，避免字段冗余，提高扩展性。

### 4.5 职位与投递（多对多）

```
users (N)                    jobs (N)
    \                          /
     ──── applications ──────
    /
resumes (N)
```

投递记录表（applications）作为中间表，实现用户与职位的多对多关系。

### 4.6 投递与面试

```
applications (1) ──── (N) interview
```

一次投递可产生多轮面试（一面、二面、终面）。

### 4.7 即时通讯

```
conversations (1) ──── (N) messages
```

会话保存参与者信息，消息记录聊天内容。user1_id < user2_id 保证唯一。

### 4.8 企业人才库

```
companies (N)                users (N)
    \                          /
     ──── company_resume ────
```

### 4.9 职位收藏

```
users (N)                   jobs (N)
    \                          /
     ──── job_favorites ─────
```

### 4.10 AI 智能匹配

```
jobs (N)                    resumes (N)
    \                          /
     ──── match_records ─────
            │
            └── (N) match_skill_details
```

match_records 存储综合评分，match_skill_details 存储逐项技能匹配明细。

### 4.11 反馈与日志

```
users (1) ──── (N) feedbacks
users (1) ──── (N) operation_logs
```

---

## 五、整体 ER 结构（简化）

```
users
 ├── resumes
 │    ├── resume_educations
 │    ├── resume_experiences
 │    └── resume_projects
 ├── applications
 │    └── interview
 ├── job_favorites
 ├── conversations
 │    └── messages
 ├── feedbacks
 └── operation_logs

companies
 ├── jobs
 │    └── match_records
 │         └── match_skill_details
 ├── users (HR)
 ├── applications
 └── company_resume
```

---

## 六、关键枚举值汇总

| 表 | 字段 | 枚举值 |
|----|------|--------|
| users | role | 0=求职者, 1=HR, 2=管理员 |
| users | status | 0=正常, 1=封禁 |
| companies | audit_status | 0=审核中, 1=已通过 |
| companies | size | 0=0-20人 ~ 4=1000人以上 |
| companies | financing_stage | 1=种子轮 ~ 5=C轮以上 |
| jobs | status | 0=审核中, 1=招聘中, 2=已下架, 3=强制下架 |
| jobs | education | 0=不限, 1=初中, 2=高中, 3=中专, 4=大专, 5=本科, 6=硕士, 7=博士 |
| applications | status | 0=已投递, 1=已查看, 2=面试中, 3=不合适, 4=已取消, 5=已录用 |
| interview | status | 0=待确认, 1=已接受, 2=已拒绝, 3=已过期, 4=已取消 |
| messages | msg_type | 0=文字, 1=系统通知 |
| feedbacks | type | 0=Bug反馈, 1=功能建议, 2=其他, 3=举报 |
| resumes | education | 0=初中, 1=高中, 2=中专, 3=大专, 4=本科, 5=硕士, 6=博士 |

---

> **最后更新**：2026年5月
