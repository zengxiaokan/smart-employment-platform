<template>
  <el-dialog v-model="visible" title="企业详情" width="860px" :close-on-click-modal="false" @open="load">
    <el-descriptions v-if="data" :column="3" border size="large" v-loading="loading">
      <el-descriptions-item label="企业名称" :span="2">
        <strong>{{ data.name }}</strong>
      </el-descriptions-item>
      <el-descriptions-item label="认证状态">
        <el-tag :type="data.auditStatus === 1 ? 'success' : data.auditStatus === 2 ? 'danger' : 'warning'" size="small">
          {{ auditMap[data.auditStatus] || '待审核' }}
        </el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="所属行业">{{ data.industry || '--' }}</el-descriptions-item>
      <el-descriptions-item label="企业规模">{{ data.size || '--' }}</el-descriptions-item>
      <el-descriptions-item label="融资阶段">{{ financeLabel(data.financingStage) }}</el-descriptions-item>
      <el-descriptions-item label="所在城市">{{ data.city || '--' }}</el-descriptions-item>
      <el-descriptions-item label="联系电话">{{ data.phone || '--' }}</el-descriptions-item>
      <el-descriptions-item label="岗位总数">{{ data.jobCount ?? 0 }}</el-descriptions-item>
      <el-descriptions-item label="已入职数">{{ data.jobConfirm ?? 0 }}</el-descriptions-item>
      <el-descriptions-item label="注册人">{{ data.registrant || '--' }}</el-descriptions-item>
      <el-descriptions-item label="入驻时间">{{ data.createdAt || '--' }}</el-descriptions-item>
      <el-descriptions-item label="企业官网">
        <a v-if="data.officialWeb" :href="data.officialWeb" target="_blank" style="color:#409eff">{{ data.officialWeb }}</a>
        <span v-else>--</span>
      </el-descriptions-item>
      <el-descriptions-item label="详细地址" :span="3">{{ data.address || '--' }}</el-descriptions-item>
      <el-descriptions-item label="Logo">
        <el-image v-if="data.logoUrl" :src="data.logoUrl" style="width:60px;height:60px;border-radius:6px" fit="cover" :preview-src-list="[data.logoUrl]" />
        <span v-else>--</span>
      </el-descriptions-item>
      <el-descriptions-item label="营业执照">
        <el-image v-if="data.licenseUrl" :src="data.licenseUrl" style="width:60px;height:60px;border-radius:6px" fit="cover" :preview-src-list="[data.licenseUrl]" />
        <span v-else>--</span>
      </el-descriptions-item>
      <el-descriptions-item label="企业简介" :span="3">
        <div class="desc-text">{{ data.description || '--' }}</div>
      </el-descriptions-item>
    </el-descriptions>
  </el-dialog>
</template>

<script setup>
import { ref, computed } from 'vue'
import { getCompanyDetail } from '@/api/admin/companies'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  companyId: { type: Number, default: null }
})

const emit = defineEmits(['update:modelValue'])

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v)
})

const data = ref(null)
const loading = ref(false)

const auditMap = { 0: '待审核', 1: '已通过', 2: '未通过' }
const financeLabel = (v) => {
  const m = { 0: '未融资', 1: '种子轮', 2: '天使轮', 3: 'A轮', 4: 'B轮', 5: 'C轮及以上' }
  return m[v] ?? '--'
}

const load = async () => {
  if (!props.companyId) return
  data.value = null
  loading.value = true
  try {
    const res = await getCompanyDetail(props.companyId)
    if (res.code === 1) data.value = res.data
  } catch { data.value = null }
  finally { loading.value = false }
}
</script>

<style scoped>
.desc-text { white-space: pre-wrap; word-break: break-all; line-height: 1.7; }
</style>
