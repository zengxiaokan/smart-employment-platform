<template>
  <div>
    <div class="sub-hero">
      <span class="sub-hero-icon">💬</span>
      <div>
        <h2>意见反馈</h2>
        <p>提交Bug反馈、功能建议或举报不当内容</p>
      </div>
    </div>

    <el-card class="submit-card">
      <template #header><span style="font-weight:600">提交反馈</span></template>
      <el-form :model="form" label-width="80px" @submit.prevent="handleSubmit">
        <el-form-item label="类型" required>
          <el-select v-model="form.type" style="width:200px">
            <el-option label="Bug反馈" :value="0" />
            <el-option label="功能建议" :value="1" />
            <el-option label="其他" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="标题" required>
          <el-input v-model="form.title" maxlength="100" show-word-limit placeholder="请简要描述问题" />
        </el-form-item>
        <el-form-item label="内容" required>
          <el-input v-model="form.content" type="textarea" :rows="4" maxlength="500" show-word-limit placeholder="请详细描述..." />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">提交反馈</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <div style="margin-top:20px">
      <div class="sub-hero" style="margin-bottom:12px">
        <span class="sub-hero-icon">📝</span>
        <div>
          <h2>我的反馈</h2>
          <p>查看已提交的反馈及处理进度</p>
        </div>
      </div>
      <el-table :data="list" stripe v-loading="loading" empty-text="暂无反馈">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="typeTag(row.type)" size="small">{{ typeMap[row.type] || '其他' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="180" show-overflow-tooltip />
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.status)" size="small">{{ statusMap[row.status] || '未知' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="提交时间" width="170" />
        <el-table-column label="操作" width="80">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="detailVisible" title="反馈详情" width="560px" :close-on-click-modal="false">
      <el-descriptions v-if="detail" :column="1" border size="large">
        <el-descriptions-item label="类型">
          <el-tag :type="typeTag(detail.type)" size="small">{{ typeMap[detail.type] || '其他' }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="标题">{{ detail.title }}</el-descriptions-item>
        <el-descriptions-item label="内容">{{ detail.content }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusTag(detail.status)">{{ statusMap[detail.status] || '未知' }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="提交时间">{{ detail.createdAt }}</el-descriptions-item>
        <el-descriptions-item label="管理员回复" v-if="detail.reply">
          <span style="color:#67c23a">{{ detail.reply }}</span>
          <div style="color:#999;font-size:12px;margin-top:4px">{{ detail.repliedAt }}</div>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMyFeedbacks, getFeedbackDetail, submitFeedback } from '@/api/feedbacks'

const typeMap = { 0: 'Bug反馈', 1: '功能建议', 2: '其他', 3: '举报' }
const typeTag = (t) => t === 0 ? 'danger' : t === 1 ? 'success' : t === 3 ? 'warning' : ''
const statusMap = { 0: '待处理', 1: '处理中', 2: '已解决', 3: '已关闭' }
const statusTag = (s) => s === 2 ? 'success' : s === 1 ? 'warning' : s === 3 ? 'info' : ''

const form = reactive({ type: 0, title: '', content: '' })
const list = ref([])
const loading = ref(false)
const submitting = ref(false)

const detailVisible = ref(false)
const detail = ref(null)

const handleSubmit = async () => {
  if (!form.title.trim()) { ElMessage.warning('请输入标题'); return }
  if (!form.content.trim()) { ElMessage.warning('请输入内容'); return }
  submitting.value = true
  try {
    await submitFeedback({ ...form, targetType: 0 })
    ElMessage.success('提交成功')
    form.title = ''; form.content = ''; form.type = 0
    fetchList()
  } catch { /* */ }
  finally { submitting.value = false }
}

const openDetail = async (row) => {
  try {
    const res = await getFeedbackDetail(row.id)
    if (res.code === 1) detail.value = res.data
    detailVisible.value = true
  } catch { /* */ }
}

const fetchList = async () => {
  loading.value = true
  try {
    const res = await getMyFeedbacks({ page: 1, size: 50 })
    if (res.code === 1) list.value = res.data.list || []
  } catch { list.value = [] }
  finally { loading.value = false }
}

onMounted(fetchList)
</script>

<style scoped>
.sub-hero { display:flex; align-items:center; gap:14px; margin-bottom:20px }
.sub-hero-icon { font-size:32px }
.sub-hero h2 { margin:0; font-size:18px; font-weight:600 }
.sub-hero p { margin:4px 0 0; color:#909399; font-size:13px }
.submit-card { margin-bottom:10px }
</style>
