# HR 端功能文档

本文档面向管理员，详细说明 HR 端（招聘端）的功能架构、页面结构和业务逻辑。

---

## 页面路由

| 路由 | 页面 | 说明 |
|------|------|------|
| `/hr` | 首页概览 | 统计数据 + 最新投递 |
| `/hr/account` | 账户设置 | 企业信息、个人资料、密码、反馈 |
| `/hr/jobs` | 职位管理 | CRUD + 状态流转 |
| `/hr/jobs/create` | 发布职位 | 职位表单 |
| `/hr/jobs/edit` | 编辑职位 | 读取 sessionStorage |
| `/hr/resumes` | 简历管理 | 投递处理 |
| `/hr/interviews` | 面试管理 | 面试流程 |
| `/hr/match` | 智能分析 | AI 匹配 |
| `/hr/search` | 人才搜索 | 搜索求职者 |
| `/hr/favorites` | 收藏夹 | 人才池 |
| `/hr/analytics` | 数据分析 | 图表与漏斗 |

---

## 功能模块

### 首页概览

**路由**：`/hr`

- 统计卡片（全量累计数据）：
  - 发布职位：公司历史累计发布岗位数（取自 company.job_count）
  - 在招职位：当前 status=1 的职位数量
  - 累计简历：投递总数
  - 面试转化率：面试人数 / 总投递数
  - 入职成功率：录用人数 / 总投递数
- 最新投递：最近 5 条投递记录，可快速处理

### 账户设置

**路由**：`/hr/account`

四个标签页：

| 标签 | 功能 |
|------|------|
| 企业信息 | 编辑企业名称、行业、规模、融资阶段、官网、地址、Logo、营业执照、简介、联系电话。保存后需管理员审核 |
| 个人资料 | 修改 HR 个人信息 |
| 修改密码 | 输入原密码和新密码 |
| 意见反馈 | 提交反馈给管理员 |
| 使用帮助 | Markdown 操作指南 |

> **重要**：新注册 HR 需先完成企业信息绑定并通过审核（auditStatus=1），否则无法使用招聘功能。

### 职位管理

**路由**：`/hr/jobs`

- 列表：编号、名称、薪资、地点、学历、经验、岗位数、状态、浏览/投递数、发布时间
- 搜索：职位名称关键词、状态、发布时间范围
- 发布职位：填写基本信息、薪资要求、技能标签、岗位职责/任职要求/福利
- 编辑职位：修改职位信息
- 状态操作：
  - 招聘中 → 下架（status=2）
  - 已下架 → 上架（status=1）
  - 强制下架 → 申请上架（status=0，等待审核）

职位状态流转（HR 视角）：

```
审核中(0) → 招聘中(1) ⇄ 已下架(2)
                  ↘ 强制下架(3) → 申请上架 → 审核中(0)
```

### 简历管理（投递处理）

**路由**：`/hr/resumes`

- 投递列表：按职位、状态、时间筛选
- 状态流转操作：

```
待处理(0) → 已查看(1) → 发起面试 → 面试中(2) → 已录用(5)
                    ↘ 不合适(3)
```

- 简历预览：查看求职者完整简历
- HR 备注：添加内部备注（hr_remark 字段）

### 面试管理

**路由**：`/hr/interviews`

- 发起面试邀约：
  - 填写面试时间（不早于当前时间）
  - 选择面试方式（线下/线上）
  - 填写地点/链接、联系人、联系电话
  - 添加 HR 备注
- 面试状态管理：

| 状态值 | 状态名 | 触发方 |
|--------|--------|--------|
| 0 | 待确认 | - |
| 1 | 已接受 | 求职者 |
| 2 | 已拒绝 | 求职者 |
| 3 | 已过期 | 系统/时间 |
| 4 | 已取消 | 求职者/HR |

- 面试通过处理（PASSED → 自动联动）：
  1. 投递状态变为"已录用"(5)
  2. 岗位 hasCount（剩余招聘数）减 1
  3. 公司 jobConfirm（已招聘人数）加 1
  4. 公司岗位数（jobCount）减 1
  5. Redis 首页缓存 `home:stats` 删除（下次访问重新计算）
  6. 若岗位招满则自动下架

### 智能分析（AI 匹配）

**路由**：`/hr/match`

- 选择职位 → 系统读取职位技能要求
- 自动匹配平台求职者简历：技能分（60分）、经验分（20分）、学历分（10分）、薪资分（10分）
- 匹配结果展示：总分、各分项、AI 推荐语、技能匹配明细
- 可直接发起面试邀约

### 人才搜索

**路由**：`/hr/search`

- 搜索维度：技能标签、学历、期望城市、薪资范围
- 搜索结果：姓名、技能、学历、期望职位、简历摘要
- 操作：发起面试、收藏到人才池

### 收藏夹（人才池）

**路由**：`/hr/favorites`

- 查看已收藏候选人（company_resume 表）
- 批量操作：发起面试、移出收藏

### 数据分析

**路由**：`/hr/analytics`

- 概览卡片：全量累计数据（不受日期筛选影响）
- 日期选择器：筛选用于趋势图和漏斗图的时间范围
- 投递趋势图：每日投递量变化
- 面试趋势图：每日面试发起量变化
- 岗位漏斗表：按职位展示 投递→面试→录用 三阶段转化

---

## 数据结构关联

### 核心数据流

```
companies (企业)
  ├── jobs (职位)
  ├── users (HR role=1)
  └── applications (投递)
        └── interview (面试)

users (HR role=1)
  ├── company_id → companies.id
  └── company_name (冗余)
```

### HR 数据表关系

| HR 操作 | 影响表 | 影响字段 |
|---------|--------|----------|
| 发布职位 | jobs, companies | jobs.insert, companies.job_count +1 |
| 下架职位 | jobs, companies | jobs.status=2, companies.job_count -1 |
| 发起面试 | interview | interview.insert (status=0) |
| 面试通过 | interview, applications, jobs, companies | interview.status=通过, applications.status=5, jobs.has_count-1, companies.job_confirm+1 |
| 收藏人才 | company_resume | company_resume.insert |

### 关键 API

| 接口 | 方法 | 说明 |
|------|------|------|
| `/recruitment/job/list` | GET | 职位列表 |
| `/recruitment/job/save` | POST | 发布/编辑职位 |
| `/recruitment/application/list` | GET | 投递列表 |
| `/recruitment/application/update` | PUT | 更新投递状态 |
| `/recruitment/interview/list` | GET | 面试列表 |
| `/recruitment/interview/send` | POST | 发起面试邀约 |
| `/recruitment/interview/update` | PUT | 更新面试状态 |
| `/recruitment/match/list` | GET | AI 匹配结果 |
| `/recruitment/search` | GET | 人才搜索 |
| `/recruitment/analytics/overview` | GET | 数据概览 |
| `/recruitment/analytics/trends` | GET | 趋势数据 |
| `/recruitment/analytics/funnel` | GET | 漏斗数据 |
| `/recruitment/company/info` | GET/PUT | 企业信息 |
| `/recruitment/favorites` | GET/POST/DELETE | 人才收藏 |

---

> **最后更新**：2026年5月
