<template>
  <div class="mr-root" v-loading="loading">
    <!-- 标题 -->
    <div class="sub-hero">
      <span class="sub-hero-icon">📄</span>
      <div>
        <h2>我的简历</h2>
        <p>管理您的简历，提升求职竞争力</p>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-if="!loading && list.length === 0" class="mr-empty">
      <el-button type="primary" size="large" @click="goCreate">创建简历</el-button>
    </div>

    <!-- 卡片列表 -->
    <div v-else class="mr-grid">
      <div
        v-for="r in displayList"
        :key="r.id"
        class="mr-card"
        :class="{ 'is-default': r.isDefault === 1 }"
        @click="openPreview(r)"
      >
        <!-- 第一行：头像 + 姓名区 -->
        <div class="mr-top">
          <el-avatar :size="48" :src="r.characterAvatar" class="mr-ava">
            {{ r.name?.charAt(0) || 'A' }}
          </el-avatar>
          <div class="mr-title">
            <span class="mr-name">{{ r.name || '未命名' }}</span>
            <el-tag v-if="r.isDefault === 1" type="success" size="small" effect="plain" class="mr-default-tag">默认</el-tag>
          </div>
          <span class="mr-salary" v-if="r.salaryMin || r.salaryMax">{{ fmtSalary(r) }}</span>
        </div>

        <!-- 第二行：基本信息 -->
        <div class="mr-info">
          <span>{{ r.gender === 1 ? '男' : '女' }}</span>
          <i>·</i>
          <span>{{ r.age || '?' }}岁</span>
          <i>·</i>
          <span>{{ educationLabel(r.education) }}</span>
          <i>·</i>
          <span v-if="r.totalWorkYears != null">{{ r.totalWorkYears }}年</span>
          <i v-if="r.totalWorkYears != null">·</i>
          <span v-if="r.maxEducation != null">{{ maxEducationLabel(r.maxEducation) }}</span>
          <i v-if="r.maxEducation != null">·</i>
          <span>{{ r.phone || '未填写' }}</span>
          <i>·</i>
          <span class="mr-email">{{ r.email || '未填写' }}</span>
        </div>

        <!-- 第三行：技能标签 -->
        <div class="mr-skills" v-if="r.skills">
          <span v-for="s in showSkills(r)" :key="s" class="mr-skill">{{ s }}</span>
          <span v-if="skillMore(r)" class="mr-skill-more">+{{ skillMore(r) }}</span>
        </div>

        <!-- 第四行：自我描述 -->
        <div class="mr-desc" v-if="r.selfDescription">{{ r.selfDescription }}</div>

        <!-- 底部 -->
        <div class="mr-foot">
          <span class="mr-foot-stat" v-if="r.educations?.length">🎓 {{ r.educations.length }}</span>
          <span class="mr-foot-stat" v-if="r.experiences?.length">💼 {{ r.experiences.length }}</span>
          <span class="mr-foot-stat" v-if="r.projects?.length">📁 {{ r.projects.length }}</span>
          <span class="mr-foot-hint">点击预览 →</span>
        </div>
      </div>
    </div>

    <!-- 底部按钮 -->
    <div class="mr-actions" v-if="list.length > 0">
      <el-button type="primary" @click="goCreate">创建新简历</el-button>
    </div>

    <!-- 预览弹窗 -->
    <el-dialog v-model="previewVisible" :title="'简历预览 · ' + (currentResume?.name || '')" width="700px" top="3vh" destroy-on-close>
      <div class="pv-box" v-if="currentResume">
        <div class="pv-hero">
          <el-avatar :size="64" :src="currentResume.characterAvatar">{{ currentResume.name?.charAt(0) }}</el-avatar>
          <div>
            <h2>{{ currentResume.name }}</h2>
            <div class="pv-meta">
              <span>{{ currentResume.gender === 1 ? '男' : '女' }}</span>
              <span>{{ currentResume.age }}岁</span>
              <span>{{ educationLabel(currentResume.education) }}</span>
              <span v-if="currentResume.graduationSchool">{{ currentResume.graduationSchool }}</span>
              <span v-if="currentResume.totalWorkYears != null">{{ currentResume.totalWorkYears }}年</span>
              <span v-if="currentResume.maxEducation != null">{{ maxEducationLabel(currentResume.maxEducation) }}</span>
              <span>{{ currentResume.phone }}</span>
              <span>{{ currentResume.email }}</span>
            </div>
          </div>
        </div>
        <div class="pv-section" v-if="currentResume.jobIntention">
          <h4>求职意向</h4>
          <p>{{ currentResume.jobIntention }} | {{ currentResume.city }} | {{ fmtSalary(currentResume) }}</p>
        </div>
        <div class="pv-section" v-if="currentResume.selfDescription"><h4>自我描述</h4><p>{{ currentResume.selfDescription }}</p></div>
        <div class="pv-section" v-if="currentResume.skills">
          <h4>技能标签</h4>
          <div class="pv-tags"><span v-for="s in parseSkills(currentResume.skills)" :key="s" class="pv-tag">{{ s }}</span></div>
        </div>
        <div class="pv-section" v-if="currentResume.educations?.length">
          <h4>教育经历</h4>
          <div v-for="(e,i) in currentResume.educations" :key="i" class="pv-item"><strong>{{ e.school }}</strong> · {{ e.major }} · {{ educationLabel(e.education) }} · {{ e.startTime }} — {{ e.endTime }}<p v-if="e.description">{{ e.description }}</p></div>
        </div>
        <div class="pv-section" v-if="currentResume.experiences?.length">
          <h4>工作经历</h4>
          <div v-for="(e,i) in currentResume.experiences" :key="i" class="pv-item"><strong>{{ e.company }}</strong> · {{ e.position }} · {{ e.startTime }} — {{ e.endTime }}<p v-if="e.description">{{ e.description }}</p></div>
        </div>
        <div class="pv-section" v-if="currentResume.projects?.length">
          <h4>项目经历</h4>
          <div v-for="(e,i) in currentResume.projects" :key="i" class="pv-item"><strong>{{ e.name }}</strong> · {{ e.role }} · {{ e.startTime }} — {{ e.endTime }}<p v-if="e.description">{{ e.description }}</p></div>
        </div>
      </div>
      <template #footer>
        <el-button @click="doExportPDF">导出 PDF</el-button>
        <el-button type="warning" :loading="optimizing" @click="doAiOptimize">AI 优化简历</el-button>
        <el-button type="primary" @click="previewVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getResumeList, getResumeDetail, exportResumePdf, optimizeResume } from '@/api/user/resume'
import { parseSkills, maxEducationLabel, educationLabel } from '@/utils/format'

const router = useRouter()
const loading = ref(true)
const list = ref([])
const previewVisible = ref(false)
const currentResume = ref(null)
const MAX_SKILLS = 3

const displayList = computed(() => {
  return [...list.value].sort((a, b) => {
    if (a.isDefault === 1 && b.isDefault !== 1) return -1
    if (b.isDefault === 1 && a.isDefault !== 1) return 1
    return 0
  })
})

const fmtSalary = (r) => {
  if (!r.salaryMin && !r.salaryMax) return ''
  return Math.round((r.salaryMin || 0) / 1000) + 'K-' + Math.round((r.salaryMax || 0) / 1000) + 'K'
}

const showSkills = (r) => {
  const arr = parseSkills(r.skills)
  return arr.slice(0, MAX_SKILLS)
}

const skillMore = (r) => {
  const arr = parseSkills(r.skills)
  return arr.length > MAX_SKILLS ? arr.length - MAX_SKILLS : 0
}

const fetchAll = async () => {
  loading.value = true
  try {
    const res = await getResumeList()
    if (res.code === 1 && res.data?.list?.length > 0) {
      list.value = res.data.list
    }
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}

const openPreview = async (r) => {
  try {
    const detail = await getResumeDetail(r.id)
    currentResume.value = detail.code === 1 ? detail.data : r
  } catch {
    currentResume.value = r
  }
  previewVisible.value = true
}

const goCreate = () => router.push('/user/resume/edit')

const doExportPDF = async () => {
  if (!currentResume.value || !currentResume.value.id) {
    ElMessage.warning('请先选择一份简历')
    return
  }
  try {
    const res = await exportResumePdf(currentResume.value.id)
    const blob = new Blob([res.data], { type: 'application/pdf' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `${currentResume.value.resumeName || '我的简历'}.pdf`
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
    URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (e) {
    ElMessage.error('导出失败')
  }
}

const optimizing = ref(false)

const doAiOptimize = async () => {
  if (!currentResume.value || !currentResume.value.id) {
    ElMessage.warning('请先选择一份简历')
    return
  }
  try {
    await ElMessageBox.confirm(
      'AI 将根据您的简历内容进行智能优化，优化后可在编辑页对比查看。是否继续？',
      'AI 优化简历',
      { confirmButtonText: '开始优化', cancelButtonText: '取消', type: 'info' }
    )
  } catch {
    return
  }
  optimizing.value = true
  try {
    const r = currentResume.value
    const payload = {
      id: r.id,
      resumeName: r.resumeName,
      name: r.name,
      phone: r.phone,
      email: r.email,
      age: r.age,
      gender: r.gender,
      skills: r.skills,
      selfDescription: r.selfDescription,
      jobIntention: r.jobIntention,
      city: r.city,
      salaryMin: r.salaryMin,
      salaryMax: r.salaryMax,
      jobType: r.jobType,
      characterAvatar: r.characterAvatar,
      educations: r.educations || [],
      experiences: r.experiences || [],
      projects: r.projects || [],
    }
    const res = await optimizeResume(payload)
    if (res.code === 1 && res.data) {
      ElMessage.success('AI 优化完成，即将跳转编辑页')
      previewVisible.value = false
      router.push({ path: '/user/resume/edit', query: { id: r.id, optimized: '1' } })
    } else {
      ElMessage.error(res.msg || 'AI 优化失败')
    }
  } catch {
    ElMessage.error('AI 优化请求失败')
  } finally {
    optimizing.value = false
  }
}

onMounted(fetchAll)
</script>

<style scoped>
.mr-root {  }

.sub-hero { display:flex; align-items:center; gap:16px; padding:24px 28px; background:linear-gradient(135deg,#f0fdf4,#ecfdf5); border-radius:14px; margin-bottom:28px; }
.sub-hero-icon { font-size:40px; }
.sub-hero h2 { font-size:20px; font-weight:700; color:#065f46; margin:0 0 4px; }
.sub-hero p { font-size:13px; color:#64748b; margin:0; }

.mr-empty { text-align:center; padding:40px 0; }

/* 网格 */
.mr-grid { display:grid; grid-template-columns:1fr 1fr; gap:16px; }
@media (max-width:767px) { .mr-grid { grid-template-columns:1fr; } }

/* 卡片 */
.mr-card {
  background:#fff; border:1px solid #edf2f7; border-radius:14px; padding:18px 20px;
  cursor:pointer; transition:all .25s;
}
.mr-card:hover { border-color:#86efac; box-shadow:0 6px 20px rgba(0,0,0,.05); transform:translateY(-2px); }
.mr-card.is-default { border-color:#a7f3d0; background:linear-gradient(135deg,#fafdfb,#f0fdf4); }

/* 第一行 */
.mr-top { display:flex; align-items:center; gap:12px; margin-bottom:12px; }
.mr-ava { flex-shrink:0; }
.mr-title { flex:1; min-width:0; display:flex; align-items:center; gap:8px; }
.mr-name { font-size:15px; font-weight:700; color:#0f172a; white-space:nowrap; overflow:hidden; text-overflow:ellipsis; }
.mr-default-tag { flex-shrink:0; }
.mr-salary { font-size:14px; font-weight:700; color:#ef4444; white-space:nowrap; flex-shrink:0; }

/* 第二行 */
.mr-info { display:flex; align-items:center; gap:6px; font-size:12px; color:#64748b; margin-bottom:10px; flex-wrap:wrap; }
.mr-info i { color:#cbd5e1; font-style:normal; }
.mr-info span { white-space:nowrap; }
.mr-email { overflow:hidden; text-overflow:ellipsis; max-width:150px; }

/* 第三行 */
.mr-skills { display:flex; flex-wrap:wrap; gap:6px; margin-bottom:10px; }
.mr-skill {
  font-size:11px; background:#ecfdf5; color:#059669; padding:2px 10px; border-radius:20px;
  border:1px solid #a7f3d0;
}
.mr-skill-more { font-size:11px; color:#94a3b8; }

/* 第四行 */
.mr-desc {
  font-size:12px; color:#94a3b8; line-height:1.6; display:-webkit-box;
  -webkit-line-clamp:2; -webkit-box-orient:vertical; overflow:hidden; margin-bottom:10px;
}

/* 底部 */
.mr-foot { display:flex; align-items:center; gap:8px; padding-top:10px; border-top:1px solid #f1f5f9; }
.mr-foot-stat { font-size:11px; color:#94a3b8; }
.mr-foot-hint { margin-left:auto; font-size:11px; color:#cbd5e1; }

.mr-actions { display:flex; justify-content:center; margin-top:24px; }

/* 预览 */
.pv-box { padding:8px 16px; }
.pv-hero { display:flex; align-items:center; gap:20px; margin-bottom:24px; padding-bottom:20px; border-bottom:2px solid #e2e8f0; }
.pv-hero h2 { font-size:22px; font-weight:700; color:#0f172a; margin:0 0 8px; }
.pv-meta { display:flex; gap:14px; font-size:13px; color:#64748b; flex-wrap:wrap; }
.pv-section { margin-bottom:20px; }
.pv-section h4 { font-size:15px; font-weight:700; color:#059669; margin:0 0 10px; }
.pv-section p { font-size:14px; color:#475569; line-height:1.8; margin:0; }
.pv-tags { display:flex; flex-wrap:wrap; gap:8px; }
.pv-tag { font-size:13px; background:#ecfdf5; color:#059669; padding:4px 14px; border-radius:20px; border:1px solid #a7f3d0; }
.pv-item { font-size:14px; color:#475569; line-height:1.8; margin-bottom:8px; }
.pv-item p { margin:4px 0 0; padding-left:12px; color:#94a3b8; border-left:2px solid #e2e8f0; }
</style>
