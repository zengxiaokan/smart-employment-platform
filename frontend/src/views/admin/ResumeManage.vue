<template>
  <div class="resume-manage">
    <el-card class="search-card">
      <el-form :inline="true" :model="queryForm">
        <el-form-item label="姓名">
          <el-input v-model="queryForm.name" placeholder="请输入姓名" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="求职意向">
          <el-input v-model="queryForm.jobIntention" placeholder="请输入求职意向" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <div class="toolbar">
      <el-button :icon="Refresh" @click="fetchList">刷新</el-button>
    </div>

    <el-table :data="list" border stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="姓名" width="100" />
      <el-table-column prop="phone" label="手机号" width="140" />
      <el-table-column label="学历" width="80">
        <template #default="{ row }">
          {{ educationMap[row.maxEducation] || '--' }}
        </template>
      </el-table-column>
      <el-table-column prop="graduationSchool" label="毕业院校" min-width="160" show-overflow-tooltip />
      <el-table-column prop="jobIntention" label="期望职位" width="150" show-overflow-tooltip />
      <el-table-column label="期望薪资" width="130">
        <template #default="{ row }">
          <template v-if="row.salaryMin && row.salaryMax">{{ Math.round(row.salaryMin / 1000) }}K-{{ Math.round(row.salaryMax / 1000) }}K</template>
          <template v-else-if="row.salaryMin">{{ Math.round(row.salaryMin / 1000) }}K起</template>
          <template v-else-if="row.salaryMax">最高{{ Math.round(row.salaryMax / 1000) }}K</template>
          <template v-else>面议</template>
        </template>
      </el-table-column>
      <el-table-column label="默认" width="70">
        <template #default="{ row }">
          <el-tag :type="row.isDefault ? 'success' : 'info'" size="small">{{ row.isDefault ? '是' : '否' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="updatedAt" label="更新时间" width="170" />
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" :icon="View" @click="detailRow = row; detailVisible = true">查看</el-button>
          <el-button link type="danger" :icon="Delete" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination">
      <el-pagination
        v-model:current-page="queryForm.page"
        v-model:page-size="queryForm.size"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        @change="fetchList"
      />
    </div>

    <el-dialog v-model="detailVisible" title="个人简历" width="760px" :close-on-click-modal="false">
      <div v-if="detailRow" class="resume-card">
        <div class="resume-header">个人简历</div>
        <table class="resume-table">
          <tbody>
            <tr>
              <td class="label">姓名</td><td class="value">{{ detailRow.name || '--' }}</td>
              <td class="label">性别</td><td class="value">{{ detailRow.gender === 1 ? '男' : detailRow.gender === 0 ? '女' : '--' }}</td>
              <td class="label" rowspan="4" style="width:120px;vertical-align:middle;text-align:center;background:#fafafa;">
                <el-image v-if="detailRow.characterAvatar" :src="detailRow.characterAvatar" fit="cover" style="width:90px;height:110px;border-radius:4px;" />
                <span v-else style="color:#c0c4cc;font-size:13px;">暂无照片</span>
              </td>
            </tr>
            <tr>
              <td class="label">年龄</td><td class="value">{{ detailRow.age ?? '--' }}</td>
              <td class="label">学历</td><td class="value">{{ educationMap[detailRow.maxEducation] || '--' }}</td>
            </tr>
            <tr>
              <td class="label">手机号</td><td class="value">{{ detailRow.phone || '--' }}</td>
              <td class="label">邮箱</td><td class="value">{{ detailRow.email || '--' }}</td>
            </tr>
            <tr>
              <td class="label">求职意向</td><td colspan="3" class="value">{{ detailRow.jobIntention || '--' }}</td>
            </tr>
            <tr>
              <td class="label">期望城市</td><td class="value">{{ detailRow.city || '--' }}</td>
              <td class="label">期望薪资</td><td class="value">{{ detailRow.salaryMin ? Math.round(detailRow.salaryMin / 1000) + 'K' : '--' }} - {{ detailRow.salaryMax ? Math.round(detailRow.salaryMax / 1000) + 'K' : '--' }}</td>
            </tr>
            <tr>
              <td class="label">求职类型</td><td class="value">{{ jobTypeMap[detailRow.jobType] || '不限' }}</td>
              <td class="label">期望行业</td><td class="value">{{ detailRow.industry || '--' }}</td>
            </tr>
            <tr>
              <td class="label">毕业院校</td><td colspan="3" class="value">{{ detailRow.graduationSchool || '--' }}</td>
            </tr>
            <tr>
              <td class="label">工作年限</td><td class="value">{{ detailRow.totalWorkYears ? detailRow.totalWorkYears + '年' : '--' }}</td>
              <td class="label">到岗时间</td><td class="value">{{ detailRow.availableFrom || '--' }}</td>
            </tr>
          </tbody>
        </table>

        <div class="resume-section-title">技能特长</div>
        <div class="resume-section-body">{{ detailRow.skills || '--' }}</div>

        <div class="resume-section-title">自我评价</div>
        <div class="resume-section-body" style="white-space:pre-wrap;line-height:1.8;">{{ detailRow.selfDescription || '--' }}</div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Refresh, Delete, View } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getResumes, deleteResume } from '@/api/admin/resumes'

const educationMap = { 0: '初中', 1: '高中', 2: '中专', 3: '大专', 4: '本科', 5: '硕士', 6: '博士' }
const jobTypeMap = { 0: '不限', 1: '全职', 2: '兼职', 3: '实习' }

const queryForm = reactive({ name: '', jobIntention: '', page: 1, size: 10 })
const list = ref([])
const total = ref(0)
const loading = ref(false)

const detailVisible = ref(false)
const detailRow = ref(null)

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定删除「${row.name}」的简历吗？`, '删除确认', {
    confirmButtonText: '确定删除', cancelButtonText: '取消', type: 'warning'
  }).then(async () => {
    await deleteResume(row.id)
    ElMessage.success('已删除')
    fetchList()
  }).catch(() => {})
}

const handleQuery = () => { queryForm.page = 1; fetchList() }

const resetQuery = () => {
  queryForm.name = ''; queryForm.jobIntention = ''
  queryForm.page = 1; fetchList()
}

const fetchList = async () => {
  loading.value = true
  try {
    const res = await getResumes({
      page: queryForm.page, size: queryForm.size,
      name: queryForm.name || undefined,
      jobIntention: queryForm.jobIntention || undefined
    })
    if (res.code === 1) {
      list.value = res.data.list || []
      total.value = res.data.total || 0
    }
  } catch { list.value = []; total.value = 0 }
  finally { loading.value = false }
}

onMounted(fetchList)
</script>

<style scoped>
.search-card { margin-bottom: 20px; border-radius: 8px; box-shadow: 0 2px 12px 0 rgba(0,0,0,0.05); }
.toolbar { margin-bottom: 15px; display: flex; gap: 10px; }
.pagination { margin-top: 20px; display: flex; justify-content: flex-end; }
:deep(.el-table) { border-radius: 8px; overflow: hidden; }

.resume-card { background:#fff; }
.resume-header { text-align:center;font-size:22px;font-weight:700;color:#1a1a2e;padding:18px 0 14px;border-bottom:2px solid #e4e7ed;letter-spacing:6px; }
.resume-table { width:100%;border-collapse:collapse;font-size:14px; }
.resume-table td { border:1px solid #dcdfe6;padding:10px 12px;line-height:1.5; }
.resume-table .label { background:#f5f7fa;width:90px;text-align:center;font-weight:600;color:#606266;white-space:nowrap; }
.resume-table .value { color:#303133;background:#fff; }
.resume-section-title { font-size:15px;font-weight:700;color:#1a1a2e;padding:16px 12px 8px;border-bottom:1px solid #e4e7ed;margin-top:4px; }
.resume-section-body { padding:10px 14px 16px;color:#4a4a5a;line-height:1.7;font-size:14px;border:1px solid #dcdfe6;border-top:none;min-height:36px; }
</style>
