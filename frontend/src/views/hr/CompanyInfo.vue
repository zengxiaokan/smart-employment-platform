<template>
  <div class="ci-root">
    <!-- 基本信息卡片 -->
    <div class="ci-card">
      <div class="ci-card-head">
        <h3>企业基本信息</h3>
        <el-button type="primary" :icon="Edit" size="small" @click="editMode = !editMode">
          {{ editMode ? '取消编辑' : '编辑' }}
        </el-button>
      </div>

      <!-- 展示模式 -->
      <el-descriptions v-if="!editMode" :column="2" border size="large" class="ci-desc">
        <el-descriptions-item label="企业名称" :span="2">
          <div class="ci-name-row">
            <el-avatar :size="40" :src="info.logoUrl">{{ info.name?.charAt(0) }}</el-avatar>
            <strong>{{ info.name }}</strong>
            <el-tag v-if="info.auditStatus === 1" type="success" size="small">已认证</el-tag>
            <el-tag v-else type="warning" size="small">审核中</el-tag>
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ info.phone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="所在城市">{{ info.city || '-' }}</el-descriptions-item>
        <el-descriptions-item label="所属行业">{{ info.industry || '-' }}</el-descriptions-item>
        <el-descriptions-item label="企业规模">{{ info.size || '-' }}</el-descriptions-item>
        <el-descriptions-item label="融资阶段">{{ financeLabel(info.financingStage) }}</el-descriptions-item>
        <el-descriptions-item label="企业官网">{{ info.officialWeb || '-' }}</el-descriptions-item>
        <el-descriptions-item label="在招职位">{{ info.jobCount ?? 0 }} 个</el-descriptions-item>
        <el-descriptions-item label="注册时间">{{ info.createdAt?.substring(0,10) || '-' }}</el-descriptions-item>
        <el-descriptions-item label="详细地址" :span="2">{{ info.address || '-' }}</el-descriptions-item>
        <el-descriptions-item label="企业简介" :span="2">{{ info.description || '暂无简介' }}</el-descriptions-item>
        <el-descriptions-item label="企业LOGO">
          <el-image v-if="info.logoUrl" :src="info.logoUrl" style="width:80px;height:80px;border-radius:8px" fit="cover" />
          <span v-else>-</span>
        </el-descriptions-item>
        <el-descriptions-item label="营业执照">
          <el-image v-if="info.licenseUrl" :src="info.licenseUrl" style="width:80px;height:80px;border-radius:8px" fit="cover" />
          <span v-else>-</span>
        </el-descriptions-item>
      </el-descriptions>

      <!-- 编辑模式 -->
      <el-form v-else :model="editForm" label-width="100px" class="ci-form">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="企业名称">
              <el-input v-model="editForm.name" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话">
              <el-input v-model="editForm.phone" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="所在城市">
              <el-input v-model="editForm.city" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所属行业">
              <el-select v-model="editForm.industry" style="width:100%">
                <el-option v-for="ind in industryOptions" :key="ind" :label="ind" :value="ind" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="企业规模">
              <el-select v-model="editForm.size" style="width:100%">
                <el-option v-for="s in sizeOptions" :key="s" :label="s" :value="s" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="融资阶段">
              <el-select v-model="editForm.financingStage" style="width:100%">
                <el-option v-for="f in financeOptions" :key="f.value" :label="f.label" :value="f.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="企业官网">
          <el-input v-model="editForm.officialWeb" />
        </el-form-item>
        <el-form-item label="详细地址">
          <el-input v-model="editForm.address" />
        </el-form-item>
        <el-form-item label="企业简介">
          <el-input v-model="editForm.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="saving" @click="saveInfo">保存</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from "vue";
import { Edit } from "@element-plus/icons-vue";
import { ElMessage } from "element-plus";
import request from "@/utils/request";

const editMode = ref(false);
const saving = ref(false);

const industryOptions = ["互联网", "金融", "教育", "医疗", "制造", "房地产", "电商", "物流", "新能源", "人工智能"];
const sizeOptions = ["少于50人", "50-99人", "100-499人", "500-999人", "1000人以上"];
const financeOptions = [
  { label: "未融资", value: 0 }, { label: "种子轮", value: 1 }, { label: "天使轮", value: 2 },
  { label: "A轮", value: 3 }, { label: "B轮", value: 4 }, { label: "C轮及以上", value: 5 },
];

const financeLabel = (v) => {
  const m = { 0: "未融资", 1: "种子轮", 2: "天使轮", 3: "A轮", 4: "B轮", 5: "C轮及以上" };
  return m[v] ?? "-";
};

const info = reactive({
  name: "", phone: "", city: "", industry: "", size: "", financingStage: null,
  officialWeb: "", address: "", description: "", logoUrl: "", licenseUrl: "",
  auditStatus: 0, jobCount: 0, createdAt: "",
});

const editForm = reactive({
  id: null,
  name: "", phone: "", city: "", industry: "", size: "", financingStage: null,
  officialWeb: "", address: "", description: "",
});

const loadInfo = () => {
  try {
    const raw = localStorage.getItem("companyInfo");
    if (raw) {
      const d = JSON.parse(raw);
      Object.assign(info, d);
      Object.assign(editForm, {
        id: d.id ?? null,
        name: d.name || "", phone: d.phone || "", city: d.city || "",
        industry: d.industry || "", size: d.size || "",
        financingStage: d.financingStage ?? null,
        officialWeb: d.officialWeb || "", address: d.address || "",
        description: d.description || "",
      });
    }
  } catch {}
};

const saveInfo = async () => {
  saving.value = true;
  try {
    const res = await request({ url: "/hr/company/info", method: "put", data: editForm });
    if (res.code === 1) {
      Object.assign(info, editForm);
      const d = { ...info, ...editForm };
      localStorage.setItem("companyInfo", JSON.stringify(d));
      ElMessage.success("保存成功");
      editMode.value = false;
    } else {
      ElMessage.error(res.msg || "保存失败");
    }
  } catch {
    ElMessage.error("网络异常");
  } finally {
    saving.value = false;
  }
};

onMounted(loadInfo);
</script>

<style scoped>
.ci-root {  }

.ci-card {
  background: #fff; border-radius: 16px; padding: 32px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.03);
}

.ci-card-head {
  display: flex; align-items: center; justify-content: space-between; margin-bottom: 24px;
}
.ci-card-head h3 { font-size: 18px; font-weight: 700; color: #0f172a; margin: 0; }

.ci-desc {  }
.ci-name-row { display: flex; align-items: center; gap: 12px; }
.ci-name-row strong { font-size: 16px; color: #0f172a; }

.ci-form { max-width: 700px; }
</style>
