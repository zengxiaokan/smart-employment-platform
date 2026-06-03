<template>
  <div class="system-settings">
    <el-tabs v-model="activeTab" type="border-card" @tab-change="onTabChange">
      <el-tab-pane label="基本设置" name="basic">
        <el-form :model="basicForm" label-width="120px" class="settings-form">
          <el-form-item label="系统名称">
            <el-input v-model="basicForm.systemName" />
          </el-form-item>
          <el-form-item label="系统Logo">
            <el-upload action="#" :show-file-list="false">
              <el-button type="primary">上传Logo</el-button>
            </el-upload>
          </el-form-item>
          <el-form-item label="客服电话">
            <el-input v-model="basicForm.servicePhone" />
          </el-form-item>
          <el-form-item label="客服邮箱">
            <el-input v-model="basicForm.serviceEmail" />
          </el-form-item>
          <el-form-item label="系统公告">
            <el-input v-model="basicForm.announcement" type="textarea" :rows="3" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="saveBasic">保存设置</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <el-tab-pane label="安全设置" name="security">
        <el-form :model="securityForm" label-width="140px" class="settings-form">
          <el-form-item label="登录失败锁定次数">
            <el-input-number v-model="securityForm.lockCount" :min="3" :max="10" />
          </el-form-item>
          <el-form-item label="锁定时间(分钟)">
            <el-input-number v-model="securityForm.lockTime" :min="5" :max="60" />
          </el-form-item>
          <el-form-item label="会话超时(分钟)">
            <el-input-number v-model="securityForm.sessionTimeout" :min="10" :max="120" />
          </el-form-item>
          <el-form-item label="密码最小长度">
            <el-input-number v-model="securityForm.passwordMinLen" :min="6" :max="20" />
          </el-form-item>
          <el-form-item label="强制密码复杂度">
            <el-switch v-model="securityForm.strongPassword" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="saveSecurity">保存设置</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <el-tab-pane label="公告管理" name="announcement">
        <div class="ann-section">
          <div class="ann-toolbar">
            <el-button type="primary" @click="openAnnCreate">发布公告</el-button>
          </div>
          <el-table :data="annList" border stripe v-loading="annLoading">
            <el-table-column prop="id" label="ID" width="70" />
            <el-table-column prop="title" label="标题" min-width="180" show-overflow-tooltip />
            <el-table-column label="置顶" width="80" align="center">
              <template #default="{ row }">
                <el-tooltip :content="row.pinned ? '点击取消置顶' : '点击置顶'">
                  <el-button
                    link
                    @click="handleTogglePin(row)"
                    :type="row.pinned ? 'warning' : 'info'"
                  >
                    <span style="font-size:18px">{{ row.pinned ? '📌' : '📍' }}</span>
                  </el-button>
                </el-tooltip>
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" label="发布时间" width="170" />
            <el-table-column label="操作" width="160" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="openAnnEdit(row)">编辑</el-button>
                <el-button link type="danger" @click="handleDeleteAnn(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination">
            <el-pagination
              v-model:current-page="annPage"
              v-model:page-size="annSize"
              :page-sizes="[10, 20, 50]"
              layout="total, sizes, prev, pager, next"
              :total="annTotal"
              @change="fetchAnnouncements"
            />
          </div>
        </div>
      </el-tab-pane>

      <el-tab-pane label="邮件设置" name="email">
        <el-form :model="emailForm" label-width="120px" class="settings-form">
          <el-form-item label="SMTP服务器">
            <el-input v-model="emailForm.smtpHost" />
          </el-form-item>
          <el-form-item label="SMTP端口">
            <el-input-number v-model="emailForm.smtpPort" :min="1" :max="65535" />
          </el-form-item>
          <el-form-item label="发件人邮箱">
            <el-input v-model="emailForm.fromEmail" />
          </el-form-item>
          <el-form-item label="邮箱密码">
            <el-input v-model="emailForm.password" type="password" show-password />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="saveEmail">保存设置</el-button>
            <el-button @click="testEmail">测试发送</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="annFormVisible" :title="annEditingId ? '编辑公告' : '发布公告'" width="560px" destroy-on-close>
      <el-form label-width="80px">
        <el-form-item label="标题">
          <el-input v-model="annFormTitle" placeholder="请输入公告标题" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="annFormContent" type="textarea" :rows="6" placeholder="请输入公告内容" maxlength="2000" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="annFormVisible = false">取消</el-button>
        <el-button type="primary" @click="saveAnn">{{ annEditingId ? '保存' : '发布' }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { getAnnouncements, createAnnouncement, updateAnnouncement, deleteAnnouncement, togglePinAnnouncement } from '@/api/admin/announcements'

const activeTab = ref("basic");

const basicForm = reactive({
  systemName: "智能就业服务平台",
  servicePhone: "400-888-8888",
  serviceEmail: "service@smartjob.com",
  announcement: "欢迎使用智能就业服务平台！",
});

const securityForm = reactive({
  lockCount: 5,
  lockTime: 30,
  sessionTimeout: 60,
  passwordMinLen: 8,
  strongPassword: true,
});

const emailForm = reactive({
  smtpHost: "smtp.example.com",
  smtpPort: 465,
  fromEmail: "noreply@smartjob.com",
  password: "",
});

const saveBasic = () => ElMessage.success("基本设置保存成功");
const saveSecurity = () => ElMessage.success("安全设置保存成功");
const saveEmail = () => ElMessage.success("邮件设置保存成功");
const testEmail = () => ElMessage.info("测试邮件已发送");

// ---- 公告管理 ----
const annList = ref([])
const annLoading = ref(false)
const annPage = ref(1)
const annTotal = ref(0)
const annSize = ref(10)
const annFormVisible = ref(false)
const annFormTitle = ref('')
const annFormContent = ref('')
const annEditingId = ref(null)

const fetchAnnouncements = async () => {
  annLoading.value = true
  try {
    const res = await getAnnouncements({ page: annPage.value, size: annSize.value })
    if (res.code === 1 && res.data) {
      annList.value = res.data.list || []
      annTotal.value = res.data.total || 0
    }
  } catch {} finally { annLoading.value = false }
}

const openAnnCreate = () => {
  annEditingId.value = null
  annFormTitle.value = ''
  annFormContent.value = ''
  annFormVisible.value = true
}

const openAnnEdit = (row) => {
  annEditingId.value = row.id
  annFormTitle.value = row.title || ''
  annFormContent.value = row.content || ''
  annFormVisible.value = true
}

const saveAnn = async () => {
  if (!annFormTitle.value.trim()) { ElMessage.warning('请输入公告标题'); return }
  if (!annFormContent.value.trim()) { ElMessage.warning('请输入公告内容'); return }
  try {
    if (annEditingId.value) {
      await updateAnnouncement(annEditingId.value, { title: annFormTitle.value, content: annFormContent.value })
      ElMessage.success('已更新')
    } else {
      await createAnnouncement({ title: annFormTitle.value, content: annFormContent.value })
      ElMessage.success('已发布')
    }
    annFormVisible.value = false
    annPage.value = 1
    fetchAnnouncements()
  } catch { ElMessage.error('操作失败') }
}

const handleDeleteAnn = (row) => {
  ElMessageBox.confirm('确定删除该公告？', '提示', { type: 'warning' }).then(async () => {
    try {
      await deleteAnnouncement(row.id)
      ElMessage.success('已删除')
      fetchAnnouncements()
    } catch { ElMessage.error('删除失败') }
  }).catch(() => {})
}

const handleTogglePin = async (row) => {
  try {
    await togglePinAnnouncement(row.id, !row.pinned)
    row.pinned = !row.pinned
    ElMessage.success(row.pinned ? '已置顶' : '已取消置顶')
  } catch { ElMessage.error('操作失败') }
}

const onTabChange = (tab) => {
  if (tab === 'announcement' && annList.value.length === 0) fetchAnnouncements()
}

onMounted(() => {
  if (activeTab.value === 'announcement') fetchAnnouncements()
})
</script>

<style scoped>
.settings-form { max-width: 600px; padding: 20px 0; }
.ann-section { padding: 10px 0; }
.ann-toolbar { margin-bottom: 16px; }
.pagination { margin-top: 20px; display: flex; justify-content: flex-end; }
</style>