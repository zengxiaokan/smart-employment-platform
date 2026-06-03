<template>
  <div class="jf-root">
    <div class="jf-card">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px" class="jf-form" label-position="top" v-loading="pageLoading">
        <el-row :gutter="24">
          <el-col :span="16">
            <el-form-item label="职位名称" prop="title">
              <el-input v-model="form.title" placeholder="如：高级前端开发工程师" size="large" maxlength="100" show-word-limit />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 创建模式：招聘人数 + 在招岗位数 + 职位类别 + 经验 -->
        <el-row :gutter="24" v-if="!isEdit">
          <el-col :span="6">
            <el-form-item label="招聘人数" prop="headcount">
              <el-input-number v-model="form.headcount" :min="1" :max="99" size="large" style="width:100%" controls-position="right" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="在招岗位数" prop="hasCount">
              <el-input-number v-model="form.hasCount" :min="1" :max="99" size="large" style="width:100%" controls-position="right" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="职位类别" prop="category">
              <el-select v-model="form.category" placeholder="请选择" size="large" style="width:100%">
                <el-option v-for="c in categoryOptions" :key="c" :label="c" :value="c" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="经验要求">
              <el-select v-model="form.experience" placeholder="请选择" size="large" style="width:100%">
                <el-option v-for="e in experienceOptions" :key="e" :label="e" :value="e" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 编辑模式：调整岗位数 + 招聘人数(只读) + 职位类别 + 经验 -->
        <el-row :gutter="24" v-if="isEdit">
          <el-col :span="6">
            <el-form-item label="调整岗位数">
              <el-input-number v-model="jobDelta" :min="-form.hasCount" :max="99" size="large" style="width:100%" controls-position="right" />
              <div class="jf-delta-hint">当前在招 {{ form.hasCount }} 个岗位</div>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="招聘人数">
              <div class="jf-readonly-num">{{ form.headcount }}</div>
              <div class="jf-delta-hint">调整后 {{ form.headcount + jobDelta }} 人</div>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="职位类别" prop="category">
              <el-select v-model="form.category" placeholder="请选择" size="large" style="width:100%">
                <el-option v-for="c in categoryOptions" :key="c" :label="c" :value="c" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="经验要求">
              <el-select v-model="form.experience" placeholder="请选择" size="large" style="width:100%">
                <el-option v-for="e in experienceOptions" :key="e" :label="e" :value="e" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="24">
          <el-col :span="8">
            <el-form-item label="最低薪资(K)" prop="salaryMin">
              <el-input-number v-model="form.salaryMin" :min="0" :max="99" :step="1" size="large" style="width:100%" controls-position="right" placeholder="K/月" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="最高薪资(K)" prop="salaryMax">
              <el-input-number v-model="form.salaryMax" :min="0" :max="99" :step="1" size="large" style="width:100%" controls-position="right" placeholder="K/月" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="学历要求">
              <el-select v-model="form.education" placeholder="请选择" size="large" style="width:100%">
                <el-option label="不限" :value="0" />
                <el-option label="初中" :value="1" />
                <el-option label="高中" :value="2" />
                <el-option label="中专" :value="3" />
                <el-option label="大专" :value="4" />
                <el-option label="本科" :value="5" />
                <el-option label="硕士" :value="6" />
                <el-option label="博士" :value="7" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="工作城市" prop="city">
              <el-input v-model="form.city" placeholder="如：武汉" size="large" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="详细地址">
              <el-input v-model="form.address" placeholder="详细工作地址" size="large" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="职位标签">
          <el-input v-model="tagsInput" placeholder="多个标签用逗号分隔：五险一金,双休,弹性工作" size="large" @change="syncTags" />
        </el-form-item>

        <el-form-item label="技能标签">
          <el-select
            v-model="jobSkillList"
            multiple
            filterable
            allow-create
            default-first-option
            placeholder="输入技能标签，如 Java,Python,Vue"
            size="large"
            style="width:100%"
            @change="syncJobSkills"
          />
        </el-form-item>

        <el-form-item label="岗位职责" prop="dutyContent">
          <div class="jf-textarea-hint">每行一条，前端自动拆成列表展示</div>
          <el-input v-model="form.dutyContent" type="textarea" :rows="5" placeholder="1. 负责前端架构设计与核心模块开发&#10;2. 参与需求评审和技术方案设计&#10;3. 指导初级工程师成长" />
        </el-form-item>

        <el-form-item label="任职要求" prop="requireContent">
          <div class="jf-textarea-hint">每行一条</div>
          <el-input v-model="form.requireContent" type="textarea" :rows="5" placeholder="1. 3年以上前端开发经验&#10;2. 精通Vue3、TypeScript&#10;3. 有大型项目架构经验优先" />
        </el-form-item>

        <el-form-item label="福利待遇">
          <el-input v-model="form.benefits" type="textarea" :rows="3" placeholder="五险一金,年终奖,带薪年假,定期团建等" />
        </el-form-item>

        <el-divider />
        <div class="jf-actions">
          <el-button size="large" @click="goBack">取消</el-button>
          <el-button type="primary" size="large" :loading="submitting" @click="submit">{{ isEdit ? '保存修改' : '立即发布' }}</el-button>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from "vue";
import { useRouter, useRoute } from "vue-router";
import { ElMessage } from "element-plus";
import { saveJob } from "@/api/hr/job";

const router = useRouter();
const route = useRoute();
const formRef = ref(null);
const submitting = ref(false);
const pageLoading = ref(false);

const isEdit = computed(() => !!route.query.id);
const jobId = computed(() => route.query.id ? Number(route.query.id) : null);

const categoryOptions = ["前端", "后端", "测试", "产品", "设计", "运维", "数据", "算法", "Android", "iOS", "全栈", "其他"];
const experienceOptions = ["应届", "1-3年", "3-5年", "5-10年", "10年以上", "不限"];

const form = reactive({
  title: "",
  category: "",
  salaryMin: null,
  salaryMax: null,
  city: "",
  address: "",
  experience: "",
  education: 0,
  tags: "",
  headcount: 1,
  hasCount: 0,
  dutyContent: "",
  requireContent: "",
  benefits: "",
  jobSkills: "",
});
const tagsInput = ref("");
const jobSkillList = ref([]);
const jobDelta = ref(0);

const rules = {
  title: [{ required: true, message: "请输入职位名称", trigger: "blur" }],
  category: [{ required: true, message: "请选择职位类别", trigger: "change" }],
  salaryMin: [{ required: true, message: "请输入最低薪资", trigger: "blur" }],
  salaryMax: [{ required: true, message: "请输入最高薪资", trigger: "blur" }],
  city: [{ required: true, message: "请输入工作城市", trigger: "blur" }],
  dutyContent: [{ required: true, message: "请输入岗位职责", trigger: "blur" }],
  requireContent: [{ required: true, message: "请输入任职要求", trigger: "blur" }],
  hasCount: [{ required: true, message: "请设置在招岗位数", trigger: "blur" }],
};

const syncTags = () => {
  form.tags = tagsInput.value.split(",").map(s => s.trim()).filter(Boolean).join(",");
};

const syncJobSkills = () => {
  form.jobSkills = jobSkillList.value.join(",");
};

const loadDetail = () => {
  if (!isEdit.value) return;
  const raw = sessionStorage.getItem("editJobData");
  if (!raw) {
    ElMessage.warning("未找到职位数据，请从列表页进入");
    router.replace("/hr/jobs");
    return;
  }
  try {
    const d = JSON.parse(raw);
    Object.assign(form, {
      title: d.title || "", category: d.category || "",
      salaryMin: d.salaryMin ?? null, salaryMax: d.salaryMax ?? null,
      city: d.city || "", address: d.address || "",
      experience: d.experience || "", education: d.education ?? 0,
      tags: d.tags || "", headcount: d.headcount ?? 1, hasCount: d.hasCount ?? 0,
      dutyContent: d.dutyContent || "", requireContent: d.requireContent || "",
      benefits: d.benefits || "",
      jobSkills: d.jobSkills || "",
    });
    tagsInput.value = (d.tags || "").replace(/,/g, ", ");
    jobSkillList.value = (d.jobSkills || "").split(",").filter(Boolean);
    jobDelta.value = 0;
    sessionStorage.removeItem("editJobData");
  } catch { ElMessage.error("数据解析失败"); }
};

const submit = async () => {
  const valid = await formRef.value.validate().catch(() => false);
  if (!valid) return;
  if (form.salaryMax <= form.salaryMin) { ElMessage.warning("最高薪资应大于最低薪资"); return; }
  if (isEdit.value && jobDelta.value < 0 && Math.abs(jobDelta.value) > form.hasCount) {
    ElMessage.warning(`减少岗位数不能超过当前在招岗位数(${form.hasCount})`);
    return;
  }

  submitting.value = true;
  try {
    const payload = { ...form };
    if (isEdit.value) {
      payload.id = jobId.value;
      payload.delta = jobDelta.value;
    }
    const res = await saveJob(payload);
    if (res.code === 1) {
      ElMessage.success(isEdit.value ? "修改成功" : "发布成功");
      router.replace("/hr/jobs");
    } else {
      ElMessage.error(res.msg || "操作失败");
    }
  } catch { ElMessage.error("网络异常"); }
  finally { submitting.value = false; }
};

const goBack = () => router.back();

onMounted(loadDetail);
</script>

<style scoped>
.jf-root {  }
.jf-card { background:#fff; border-radius:14px; padding:32px; box-shadow:0 1px 4px rgba(0,0,0,.03); }
.jf-form { max-width:900px; }
.jf-textarea-hint { font-size:12px; color:#94a3b8; margin-bottom:6px; }
.jf-delta-hint { font-size:12px; color:#94a3b8; margin-top:6px; }
.jf-readonly-num { font-size:24px; font-weight:700; color:#1e40af; line-height:40px; }
.jf-actions { display:flex; justify-content:flex-end; gap:12px; }
</style>
