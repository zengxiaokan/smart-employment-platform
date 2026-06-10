<template>
  <div>
    <div class="sub-hero">
      <span class="sub-hero-icon">✨</span>
      <div>
        <h2>AI优化简历</h2>
        <p>借助AI智能优化您的简历内容，提升求职竞争力</p>
      </div>
    </div>

    <div v-if="!optimizedData" class="optimize-start">
      <el-alert
        title="AI 将自动分析您的简历并给出优化建议"
        type="info"
        :closable="false"
        show-icon
        style="margin-bottom: 24px"
      />
      <el-button
        type="primary"
        size="large"
        :icon="MagicStick"
        @click="handleOptimize"
      >
        开始AI优化
      </el-button>
    </div>

    <el-dialog
      v-model="loadingVisible"
      title="AI 优化简历"
      width="420px"
      :show-close="false"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      center
    >
      <div class="loading-dialog-body">
        <el-icon class="loading-spin" :size="48"><Loading /></el-icon>
        <p class="loading-text">AI正在优化中，请稍候...</p>
        <p class="loading-sub">正在分析简历内容并生成优化建议</p>
      </div>
    </el-dialog>

    <div v-else class="optimize-result">
      <el-alert
        title="AI已生成优化建议，请对比查看后决定是否采纳"
        type="success"
        :closable="false"
        show-icon
        style="margin-bottom: 24px"
      />
      <div class="compare-container">
        <div class="compare-panel original">
          <el-tag class="compare-tag" type="info" size="large">优化前</el-tag>
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item
              v-for="item in originalDescriptions"
              :key="item.label"
              :label="item.label"
              :span="item.span || 1"
            >{{ item.value }}</el-descriptions-item>
          </el-descriptions>
          <!-- 经历列表：原值 -->
          <div v-for="(group, gi) in originalExperienceGroups" :key="'o-'+gi" class="exp-block">
            <div class="exp-block-title">{{ group.title }}</div>
            <el-descriptions v-if="group.items.length" :column="1" border size="small">
              <el-descriptions-item v-for="(it, ii) in group.items" :key="ii" :label="it.label">
                <pre class="exp-desc">{{ it.value }}</pre>
              </el-descriptions-item>
            </el-descriptions>
            <div v-else class="exp-empty">暂无{{ group.title }}</div>
          </div>
        </div>
        <div class="compare-arrow">
          <el-icon :size="28"><Right /></el-icon>
        </div>
        <div class="compare-panel optimized">
          <el-tag class="compare-tag" type="warning" size="large">AI优化后</el-tag>
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item
              v-for="item in optimizedDescriptions"
              :key="item.label"
              :label="item.label"
              :span="item.span || 1"
            >
              <span class="optimized-value">{{ item.value }}</span>
            </el-descriptions-item>
          </el-descriptions>
          <!-- 经历列表：AI 优化后 -->
          <div v-for="(group, gi) in optimizedExperienceGroups" :key="'p-'+gi" class="exp-block">
            <div class="exp-block-title">{{ group.title }}</div>
            <el-descriptions v-if="group.items.length" :column="1" border size="small">
              <el-descriptions-item v-for="(it, ii) in group.items" :key="ii" :label="it.label">
                <pre class="exp-desc optimized-value">{{ it.value }}</pre>
              </el-descriptions-item>
            </el-descriptions>
            <div v-else class="exp-empty">暂无{{ group.title }}</div>
          </div>
        </div>
      </div>

      <div class="optimize-actions">
        <el-button type="success" size="large" :loading="accepting" @click="handleAccept">
          采纳优化结果
        </el-button>
        <el-button size="large" @click="handleReject">
          放弃，保留原简历
        </el-button>
        <el-button size="large" @click="handleOptimize">
          重新优化
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { MagicStick, Right, Loading } from '@element-plus/icons-vue'
import { aiOptimizeResume, acceptAiResume } from '@/api/user/user'

const loadingVisible = ref(false)
const accepting = ref(false)
const optimizedData = ref(null)

const originalDescriptions = computed(() => {
  if (!optimizedData.value) return []
  const original = optimizedData.value.original || {}
  return buildDescriptions(original)
})

const optimizedDescriptions = computed(() => {
  if (!optimizedData.value) return []
  const optimized = optimizedData.value.optimized || {}
  return buildDescriptions(optimized)
})

const buildDescriptions = (data) => {
  const items = []
  if (data.name) items.push({ label: '姓名', value: data.name })
  if (data.gender !== undefined && data.gender !== null) items.push({ label: '性别', value: data.gender === 1 ? '男' : data.gender === 0 ? '女' : data.gender })
  if (data.age) items.push({ label: '年龄', value: data.age })
  if (data.phone) items.push({ label: '手机号', value: data.phone })
  if (data.email) items.push({ label: '邮箱', value: data.email })
  if (data.jobIntention) items.push({ label: '期望职位', value: data.jobIntention, span: 2 })
  if (data.city) items.push({ label: '期望城市', value: data.city })
  if (data.salaryMin || data.salaryMax) {
    items.push({ label: '期望薪资', value: `${data.salaryMin || '?'} - ${data.salaryMax || '?'} K`, span: 2 })
  }
  if (data.skills) items.push({ label: '技能特长', value: data.skills, span: 2 })
  // 修正：后端字段是 selfDescription，不是 selfEvaluation
  if (data.selfDescription) items.push({ label: '自我评价', value: data.selfDescription, span: 2 })
  return items
}

// 经历列表（教育 / 工作 / 项目）：原值 vs 优化后并列
const buildExperienceGroups = (data) => {
  const groups = []
  const edus = Array.isArray(data?.educations) ? data.educations : []
  const exps = Array.isArray(data?.experiences) ? data.experiences : []
  const projs = Array.isArray(data?.projects) ? data.projects : []
  groups.push({
    title: '教育经历',
    items: edus.map(e => ({
      label: `${e.school || '未知学校'} · ${e.major || ''} · ${e.education || ''} · ${[e.startTime, e.endTime].filter(Boolean).join(' ~ ') || ''}`,
      value: e.description || '(无描述)',
    })),
  })
  groups.push({
    title: '工作经历',
    items: exps.map(e => ({
      label: `${e.company || '未知公司'} · ${e.position || ''} · ${[e.startTime, e.endTime].filter(Boolean).join(' ~ ') || ''}`,
      value: e.description || '(无描述)',
    })),
  })
  groups.push({
    title: '项目经验',
    items: projs.map(p => ({
      label: `${p.name || '未知项目'}${p.role ? ' · ' + p.role : ''} · ${[p.startTime, p.endTime].filter(Boolean).join(' ~ ') || ''}`,
      value: p.description || '(无描述)',
    })),
  })
  return groups
}

const originalExperienceGroups = computed(() =>
  optimizedData.value ? buildExperienceGroups(optimizedData.value.original || {}) : []
)
const optimizedExperienceGroups = computed(() =>
  optimizedData.value ? buildExperienceGroups(optimizedData.value.optimized || {}) : []
)

const handleOptimize = async () => {
  loadingVisible.value = true
  optimizedData.value = null
  try {
    const res = await aiOptimizeResume()
    loadingVisible.value = false
    if (res.code === 1 && res.data) {
      optimizedData.value = res.data
      ElMessage.success('AI优化完成，请查看对比结果')
    } else {
      ElMessage.error(res.msg || 'AI优化失败，请重试')
    }
  } catch {
    loadingVisible.value = false
    ElMessage.error('AI优化请求失败')
  }
}

const handleAccept = () => {
  ElMessageBox.confirm('确定要采纳AI优化结果吗？此操作将覆盖您当前的简历。', '确认采纳', {
    confirmButtonText: '确定采纳',
    cancelButtonText: '再想想',
    type: 'warning',
  }).then(async () => {
    accepting.value = true
    try {
      const res = await acceptAiResume(optimizedData.value.optimized)
      if (res.code === 1) {
        ElMessage.success('简历已更新为AI优化版本')
        optimizedData.value = null
      } else {
        ElMessage.error(res.msg || '更新失败')
      }
    } catch {
      ElMessage.error('更新请求失败')
    }
    accepting.value = false
  }).catch(() => {})
}

const handleReject = () => {
  optimizedData.value = null
  ElMessage.info('已放弃AI优化结果，保留原简历')
}
</script>

<style scoped>
.sub-hero { display:flex; align-items:center; gap:16px; padding:24px 28px; background:linear-gradient(135deg,#f0fdf4,#ecfdf5); border-radius:14px; margin-bottom:28px; }
.sub-hero-icon { font-size:40px; }
.sub-hero h2 { font-size:20px; font-weight:700; color:#065f46; margin:0 0 4px; }
.sub-hero p { font-size:13px; color:#64748b; margin:0; }

.optimize-start {
  text-align: center;
  padding: 20px 0;
}

.compare-container {
  display: flex;
  align-items: flex-start;
  gap: 20px;
  margin-bottom: 24px;
}

.compare-panel {
  flex: 1;
  min-width: 0;
}

.compare-tag {
  margin-bottom: 12px;
}

.compare-arrow {
  display: flex;
  align-items: center;
  justify-content: center;
  padding-top: 40px;
  color: #3b82f6;
  flex-shrink: 0;
}

.optimized-value {
  color: #059669;
  font-weight: 600;
}

.exp-block {
  margin-top: 14px;
  padding: 12px;
  background: rgba(248, 250, 252, 0.6);
  border-radius: 12px;
  border: 1px solid rgba(148, 163, 184, 0.12);
}

.exp-block-title {
  font-size: 13px;
  font-weight: 700;
  color: #475569;
  margin-bottom: 8px;
  letter-spacing: 0.04em;
}

.exp-desc {
  margin: 0;
  font-family: inherit;
  font-size: 13px;
  line-height: 1.7;
  color: #334155;
  white-space: pre-wrap;
  word-break: break-word;
}

.exp-empty {
  font-size: 12px;
  color: #94a3b8;
  font-style: italic;
  padding: 6px 2px;
}

.optimize-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.loading-dialog-body {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px 0;
}

.loading-spin {
  color: #409eff;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.loading-text {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 20px 0 8px;
}

.loading-sub {
  font-size: 13px;
  color: #909399;
  margin: 0;
}

@media (max-width: 767px) {

  .compare-container {
    flex-direction: column;
  }

  .compare-arrow {
    transform: rotate(90deg);
    padding-top: 0;
  }
}
</style>
