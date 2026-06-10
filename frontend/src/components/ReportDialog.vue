<template>
  <el-dialog
    :model-value="visible"
    :title="'举报' + typeLabel"
    width="460px"
    :close-on-click-modal="false"
    @update:model-value="$emit('update:visible', $event)"
  >
    <p style="margin-bottom: 12px; color: #606266">
      {{ typeLabel }}：<strong>{{ targetName }}</strong>
    </p>
    <el-input
      v-model="reason"
      type="textarea"
      :rows="4"
      maxlength="300"
      show-word-limit
      placeholder="请详细描述举报原因..."
    />
    <template #footer>
      <el-button @click="$emit('update:visible', false)">取消</el-button>
      <el-button type="danger" :loading="submitting" @click="doSubmit">提交举报</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { submitFeedback } from '@/api/feedbacks'

const props = defineProps({
  visible: { type: Boolean, default: false },
  targetType: { type: Number, required: true },
  targetId: { type: [Number, String], default: null },
  targetName: { type: String, default: '' },
  label: { type: String, default: '职位' },
})

const emit = defineEmits(['update:visible'])

const reason = ref('')
const submitting = ref(false)

const typeLabel = computed(() => props.label)

const doSubmit = async () => {
  if (!reason.value.trim()) {
    ElMessage.warning('请填写举报原因')
    return
  }
  submitting.value = true
  try {
    await submitFeedback({
      type: 3,
      title: `举报${typeLabel.value}：${props.targetName || ''}`,
      content: reason.value,
      targetType: props.targetType,
      targetId: props.targetId ? Number(props.targetId) : null,
    })
    ElMessage.success('举报已提交')
    reason.value = ''
    emit('update:visible', false)
  } catch {
    /* */
  } finally {
    submitting.value = false
  }
}
</script>
