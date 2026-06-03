<template>
  <div class="feedback-manage">
    <el-tabs v-model="activeTab" type="border-card">
      <!-- ==================== 反馈工单 ==================== -->
      <el-tab-pane label="反馈工单" name="feedbacks">
        <el-card class="search-card">
          <el-form :inline="true" :model="fbForm">
            <el-form-item label="类型">
              <el-select v-model="fbForm.type" placeholder="全部" clearable style="width:130px">
                <el-option label="Bug反馈" :value="0" />
                <el-option label="功能建议" :value="1" />
                <el-option label="其他" :value="2" />
                <el-option label="举报" :value="3" />
              </el-select>
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="fbForm.status" placeholder="全部" clearable style="width:110px">
                <el-option label="待处理" :value="0" />
                <el-option label="处理中" :value="1" />
                <el-option label="已解决" :value="2" />
                <el-option label="已关闭" :value="3" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleFbQuery">查询</el-button>
              <el-button @click="resetFbQuery">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <el-table :data="fbList" border stripe v-loading="fbLoading">
          <el-table-column prop="id" label="ID" width="70" />
          <el-table-column label="类型" width="90">
            <template #default="{ row }">
              <el-tag :type="fbTypeTag(row.type)" size="small">{{ fbTypeMap[row.type] || '其他' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="举报目标" width="140" show-overflow-tooltip>
            <template #default="{ row }">
              <template v-if="row.type === 3 && row.targetType === 1">职位 #{{ row.targetId }}</template>
              <template v-else-if="row.type === 3 && row.targetType === 2">公司 #{{ row.targetId }}</template>
              <template v-else>--</template>
            </template>
          </el-table-column>
          <el-table-column prop="title" label="标题" min-width="180" show-overflow-tooltip />
          <el-table-column prop="username" label="提交人" width="100" />
          <el-table-column label="状态" width="90">
            <template #default="{ row }">
              <el-tag :type="fbStatusTag(row.status)" size="small">{{ fbStatusMap[row.status] || '未知' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="提交时间" width="170" />
          <el-table-column label="操作" width="140" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="openFbDetail(row)">详情</el-button>
              <el-button link type="danger" @click="handleFbDelete(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination">
          <el-pagination
            v-model:current-page="fbForm.page"
            v-model:page-size="fbForm.size"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="fbTotal"
            @change="fetchFbList"
          />
        </div>

        <el-dialog v-model="fbDetailVisible" title="反馈详情" width="600px" :close-on-click-modal="false">
          <el-descriptions v-if="fbDetail" :column="2" border size="large">
            <el-descriptions-item label="类型">
              <el-tag :type="fbTypeTag(fbDetail.type)" size="small">{{ fbTypeMap[fbDetail.type] || '其他' }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="fbStatusTag(fbDetail.status)">{{ fbStatusMap[fbDetail.status] || '未知' }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="标题" :span="2">{{ fbDetail.title }}</el-descriptions-item>
            <el-descriptions-item label="提交人">{{ fbDetail.username || '--' }}</el-descriptions-item>
            <el-descriptions-item label="提交时间">{{ fbDetail.createdAt }}</el-descriptions-item>
            <el-descriptions-item label="举报目标" :span="2" v-if="fbDetail.type === 3">
              <template v-if="fbDetail.targetType === 1">职位 ID: {{ fbDetail.targetId }}</template>
              <template v-else-if="fbDetail.targetType === 2">公司 ID: {{ fbDetail.targetId }}</template>
            </el-descriptions-item>
            <el-descriptions-item label="内容" :span="2">{{ fbDetail.content }}</el-descriptions-item>
            <el-descriptions-item label="回复" :span="2" v-if="fbDetail.reply">
              <span style="color:#67c23a">{{ fbDetail.reply }}</span>
              <div style="color:#999;font-size:12px;margin-top:4px">{{ fbDetail.repliedAt }}</div>
            </el-descriptions-item>
          </el-descriptions>
          <el-divider />
          <el-form v-if="!fbDetail?.reply" :model="replyForm" label-width="60px">
            <el-form-item label="回复">
              <el-input v-model="replyForm.reply" type="textarea" :rows="3" placeholder="输入回复内容..." />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="replySubmitting" @click="doReply">提交回复</el-button>
              <el-select v-model="replyForm.newStatus" style="width:110px;margin-left:10px" placeholder="更新状态">
                <el-option label="待处理" :value="0" />
                <el-option label="处理中" :value="1" />
                <el-option label="已解决" :value="2" />
                <el-option label="已关闭" :value="3" />
              </el-select>
              <el-button :loading="statusSubmitting" @click="doUpdateStatus">更新状态</el-button>
            </el-form-item>
          </el-form>
        </el-dialog>
      </el-tab-pane>

      <!-- ==================== 公告管理 ==================== -->
      <el-tab-pane label="公告管理" name="announcements">
        <div class="toolbar">
          <el-button type="primary" :icon="Plus" @click="openAnnDialog()">发布公告</el-button>
          <el-button :icon="Refresh" @click="fetchAnnList">刷新</el-button>
        </div>

        <el-table :data="annList" border stripe v-loading="annLoading">
          <el-table-column prop="id" label="ID" width="70" />
          <el-table-column prop="title" label="标题" min-width="180" show-overflow-tooltip />
          <el-table-column label="置顶" width="80" align="center">
            <template #default="{ row }">
              <el-tooltip :content="row.pinned ? '点击取消置顶' : '点击置顶'">
                <el-button link @click="handleTogglePin(row)" :type="row.pinned ? 'warning' : 'info'">
                  <span style="font-size:18px">{{ row.pinned ? '📌' : '📍' }}</span>
                </el-button>
              </el-tooltip>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="发布时间" width="170" />
          <el-table-column prop="updatedAt" label="更新时间" width="170" />
          <el-table-column label="操作" width="160" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="openAnnDialog(row)">编辑</el-button>
              <el-button link type="danger" @click="handleAnnDelete(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination">
          <el-pagination
            v-model:current-page="annForm.page"
            v-model:page-size="annForm.size"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="annTotal"
            @change="fetchAnnList"
          />
        </div>

        <el-dialog v-model="annDialogVisible" :title="annEditId ? '编辑公告' : '发布公告'" width="560px" :close-on-click-modal="false">
          <el-form :model="annEditForm" label-width="60px">
            <el-form-item label="标题" required>
              <el-input v-model="annEditForm.title" maxlength="200" show-word-limit placeholder="请输入公告标题" />
            </el-form-item>
            <el-form-item label="内容" required>
              <el-input v-model="annEditForm.content" type="textarea" :rows="5" maxlength="2000" show-word-limit placeholder="请输入公告内容" />
            </el-form-item>
          </el-form>
          <template #footer>
            <el-button @click="annDialogVisible = false">取消</el-button>
            <el-button type="primary" :loading="annSubmitting" @click="doSaveAnn">{{ annEditId ? '保存' : '发布' }}</el-button>
          </template>
        </el-dialog>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Plus, Refresh } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getFeedbacks, getFeedbackDetail, replyFeedback, updateFeedbackStatus } from '@/api/admin/feedbacks'
import { getAnnouncements, createAnnouncement, updateAnnouncement, deleteAnnouncement, togglePinAnnouncement } from '@/api/admin/announcements'

const activeTab = ref('feedbacks')

const fbTypeMap = { 0: 'Bug反馈', 1: '功能建议', 2: '其他', 3: '举报' }
const fbTypeTag = (t) => t === 0 ? 'danger' : t === 1 ? 'success' : t === 3 ? 'warning' : ''
const fbStatusMap = { 0: '待处理', 1: '处理中', 2: '已解决', 3: '已关闭' }
const fbStatusTag = (s) => s === 2 ? 'success' : s === 1 ? 'warning' : s === 3 ? 'info' : ''

/* ====== 反馈工单 ====== */
const fbForm = reactive({ type: '', status: '', page: 1, size: 10 })
const fbList = ref([])
const fbTotal = ref(0)
const fbLoading = ref(false)

const fbDetailVisible = ref(false)
const fbDetail = ref(null)

const replyForm = reactive({ reply: '', newStatus: '' })
const replySubmitting = ref(false)
const statusSubmitting = ref(false)

const handleFbQuery = () => { fbForm.page = 1; fetchFbList() }
const resetFbQuery = () => {
  fbForm.type = ''; fbForm.status = ''; fbForm.page = 1
  fetchFbList()
}

const fetchFbList = async () => {
  fbLoading.value = true
  try {
    const res = await getFeedbacks({
      page: fbForm.page, size: fbForm.size,
      type: fbForm.type !== '' ? fbForm.type : undefined,
      status: fbForm.status !== '' ? fbForm.status : undefined
    })
    if (res.code === 1) {
      fbList.value = res.data.list || []
      fbTotal.value = res.data.total || 0
    }
  } catch { fbList.value = []; fbTotal.value = 0 }
  finally { fbLoading.value = false }
}

const openFbDetail = async (row) => {
  try {
    const res = await getFeedbackDetail(row.id)
    if (res.code === 1) fbDetail.value = res.data
    replyForm.reply = ''; replyForm.newStatus = ''
    fbDetailVisible.value = true
  } catch { /* */ }
}

const handleFbDelete = (row) => {
  ElMessageBox.confirm('确定删除该反馈吗？', '删除确认', { confirmButtonText: '确定删除', cancelButtonText: '取消', type: 'warning' })
    .then(async () => {
      // reuse delete from the shared API set
      await replyFeedback(row.id, { reply: '' })
      ElMessage.success('已删除')
      fetchFbList()
    }).catch(() => {})
}

const doReply = async () => {
  if (!replyForm.reply.trim()) { ElMessage.warning('请输入回复内容'); return }
  replySubmitting.value = true
  try {
    await replyFeedback(fbDetail.value.id, { reply: replyForm.reply })
    ElMessage.success('已回复')
    fbDetailVisible.value = false
    fetchFbList()
  } catch { /* */ }
  finally { replySubmitting.value = false }
}

const doUpdateStatus = async () => {
  if (replyForm.newStatus === '') { ElMessage.warning('请选择状态'); return }
  statusSubmitting.value = true
  try {
    await updateFeedbackStatus(fbDetail.value.id, replyForm.newStatus)
    ElMessage.success('状态已更新')
    fbDetail.value.status = replyForm.newStatus
  } catch { /* */ }
  finally { statusSubmitting.value = false }
}

/* ====== 公告管理 ====== */
const annForm = reactive({ page: 1, size: 10 })
const annList = ref([])
const annTotal = ref(0)
const annLoading = ref(false)

const annDialogVisible = ref(false)
const annEditId = ref(null)
const annEditForm = reactive({ title: '', content: '' })
const annSubmitting = ref(false)

const fetchAnnList = async () => {
  annLoading.value = true
  try {
    const res = await getAnnouncements({ page: annForm.page, size: annForm.size })
    if (res.code === 1) {
      annList.value = res.data.list || []
      annTotal.value = res.data.total || 0
    }
  } catch { annList.value = []; annTotal.value = 0 }
  finally { annLoading.value = false }
}

const openAnnDialog = (row) => {
  if (row) {
    annEditId.value = row.id
    annEditForm.title = row.title
    annEditForm.content = row.content
  } else {
    annEditId.value = null
    annEditForm.title = ''
    annEditForm.content = ''
  }
  annDialogVisible.value = true
}

const doSaveAnn = async () => {
  if (!annEditForm.title.trim()) { ElMessage.warning('请输入标题'); return }
  if (!annEditForm.content.trim()) { ElMessage.warning('请输入内容'); return }
  annSubmitting.value = true
  try {
    if (annEditId.value) {
      await updateAnnouncement(annEditId.value, { ...annEditForm })
      ElMessage.success('已更新')
    } else {
      await createAnnouncement({ ...annEditForm })
      ElMessage.success('已发布')
    }
    annDialogVisible.value = false
    fetchAnnList()
  } catch { /* */ }
  finally { annSubmitting.value = false }
}

const handleAnnDelete = (row) => {
  ElMessageBox.confirm(`确定删除公告「${row.title}」吗？`, '删除确认', { confirmButtonText: '确定删除', cancelButtonText: '取消', type: 'warning' })
    .then(async () => {
      await deleteAnnouncement(row.id)
      ElMessage.success('已删除')
      fetchAnnList()
    }).catch(() => {})
}

const handleTogglePin = async (row) => {
  try {
    await togglePinAnnouncement(row.id, !row.pinned)
    row.pinned = !row.pinned
    ElMessage.success(row.pinned ? '已置顶' : '已取消置顶')
  } catch { ElMessage.error('操作失败') }
}

onMounted(() => {
  fetchFbList()
})
</script>

<style scoped>
.search-card { margin-bottom: 20px; border-radius: 8px; box-shadow: 0 2px 12px 0 rgba(0,0,0,0.05); }
.toolbar { margin-bottom: 15px; display: flex; gap: 10px; }
.pagination { margin-top: 20px; display: flex; justify-content: flex-end; }
:deep(.el-table) { border-radius: 8px; overflow: hidden; }
</style>
