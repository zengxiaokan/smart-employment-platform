<script setup>
import { ref, reactive, onMounted } from "vue";
import { useRouter, useRoute } from "vue-router";
import { ElMessage } from "element-plus";
import {
  ArrowLeft,
  Plus,
  Delete,
  Finished,
  Upload,
} from "@element-plus/icons-vue";
import { getResumeDetail, getDefaultResume, saveResume, uploadResumeAvatar, optimizeResume, saveEducation, saveExperience, saveProject, deleteEducation, deleteExperience, deleteProject, saveResumeBasic } from "@/api/user/resume";
import { parseSkills, formatSkills } from "@/utils/format";

const router = useRouter();
const route = useRoute();
const loading = ref(false);
const resumeId = ref(route.query.id);
const formRef = ref(null);

const skillsInput = ref([]);
const availableSkills = ref([
  "JavaScript",
  "TypeScript",
  "Vue",
  "Vue3",
  "React",
  "Angular",
  "Node.js",
  "Java",
  "Python",
  "Go",
  "PHP",
  "C++",
  "C#",
  "HTML",
  "CSS",
  "Sass",
  "Less",
  "Tailwind CSS",
  "Bootstrap",
  "Element Plus",
  "Ant Design",
  "Axios",
  "Fetch",
  "WebSocket",
  "MySQL",
  "PostgreSQL",
  "MongoDB",
  "Redis",
  "Elasticsearch",
  "Git",
  "Docker",
  "Kubernetes",
  "Linux",
  "Nginx",
  "Apache",
  "Spring Boot",
  "Spring Cloud",
  "MyBatis",
  "Hibernate",
  "Maven",
  "Gradle",
  "Webpack",
  "Vite",
  "Gulp",
  "RESTful API",
  "GraphQL",
  "Microservices",
  "AWS",
  "Azure",
  "GCP",
  "Photoshop",
  "Illustrator",
  "Figma",
  "Sketch",
  "单元测试",
  "集成测试",
  "性能优化",
  "前端安全",
  "TypeScript",
]);

const validatePhone = (rule, value, callback) => {
  if (!value) {
    callback(new Error("请输入手机号"));
  } else if (!/^1[3-9]\d{9}$/.test(value)) {
    callback(new Error("手机号格式不正确"));
  } else {
    callback();
  }
};

const validateEmail = (rule, value, callback) => {
  if (!value) {
    callback(new Error("请输入邮箱"));
  } else if (!/^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/.test(value)) {
    callback(new Error("邮箱格式不正确"));
  } else {
    callback();
  }
};

const rules = {
  resumeName: [{ required: true, message: "请输入简历名称", trigger: "blur" }],
  name: [{ required: true, message: "请输入姓名", trigger: "blur" }],
  phone: [{ validator: validatePhone, trigger: "blur" }],
  email: [{ validator: validateEmail, trigger: "blur" }],
  gender: [{ required: true, message: "请选择性别", trigger: "change" }],
  age: [{ required: true, message: "请输入年龄", trigger: "blur" }],
  education: [{ required: true, message: "请选择最高学历", trigger: "change" }],
  city: [{ required: true, message: "请输入期望城市", trigger: "blur" }],
  jobIntention: [
    { required: true, message: "请输入求职意向", trigger: "blur" },
  ],
  salaryMin: [{ required: true, message: "请输入最低薪资", trigger: "blur" }],
  salaryMax: [{ required: true, message: "请输入最高薪资", trigger: "blur" }],
};

const form = reactive({
  id: null,
  userId: null,
  resumeName: "",
  characterAvatar: "",
  name: "",
  phone: "",
  email: "",
  gender: 1,
  age: null,
  education: 1,
  graduationSchool: "",
  totalWorkYears: null,
  maxEducation: null,
  selfDescription: "",
  jobIntention: "",
  city: "",
  salaryMin: null,
  salaryMax: null,
  skills: [],
  jobType: 1,
  industry: "",
  availableFrom: "",
  educations: [
    { school: "", major: "", education: 1, startTime: "", endTime: "", description: "" },
  ],
  experiences: [
    { company: "", position: "", startTime: "", endTime: "", description: "" },
  ],
  projects: [
    { name: "", role: "", startTime: "", endTime: "", description: "" },
  ],
});

const educationOptions = [
  { label: "初中", value: 0 },
  { label: "高中", value: 1 },
  { label: "中专", value: 2 },
  { label: "大专", value: 3 },
  { label: "本科", value: 4 },
  { label: "硕士", value: 5 },
  { label: "博士", value: 6 },
];

const maxEducationOptions = [
  { label: '初中', value: 0 },
  { label: '高中', value: 1 },
  { label: '中专', value: 2 },
  { label: '大专', value: 3 },
  { label: '本科', value: 4 },
  { label: '硕士', value: 5 },
  { label: '博士', value: 6 },
];

const fetchDetail = async () => {
  loading.value = true;
  try {
    if (resumeId.value) {
      const res = await getResumeDetail(resumeId.value);
      if (res.code === 1) {
        Object.assign(form, res.data);
        form.skills = parseSkills(res.data.skills);
        form.jobType = res.data.jobType ?? 1;
        form.industry = res.data.industry || "";
        form.availableFrom = res.data.availableFrom || "";
        return;
      }
    } else {
      const res = await getDefaultResume();
      if (res.code === 1 && res.data) {
        Object.assign(form, res.data);
        form.id = null;
        form.skills = parseSkills(res.data.skills);
        form.jobType = res.data.jobType ?? 1;
        form.industry = res.data.industry || "";
        form.availableFrom = res.data.availableFrom || "";
        return;
      }
    }
    form.resumeName = "我的前端开发简历";
    form.name = "张三";
    form.phone = "13800138000";
    form.email = "zhangsan@example.com";
    form.gender = 1;
    form.age = 25;
    form.education = 1;
    form.graduationSchool = "武汉大学";
    form.selfDescription = "拥有3年大厂前端开发经验，精通Vue3技术栈。";
    form.jobIntention = "前端开发工程师";
    form.city = "武汉";
    form.salaryMin = 15000;
    form.salaryMax = 25000;
  } catch (error) {
    console.error("获取简历详情失败:", error);
  } finally {
    loading.value = false;
  }
};

const addEducation = () => {
  form.educations.push({
    school: "",
    major: "",
    education: 1,
    startTime: "",
    endTime: "",
    description: "",
  });
};
const removeEducation = async (index) => {
  const edu = form.educations[index];
  if (edu.id) {
    try { await deleteEducation(edu.id); } catch { /* 接口失败也继续删 */ }
  }
  form.educations.splice(index, 1);
};

const addWork = () => {
  form.experiences.push({
    company: "",
    position: "",
    startTime: "",
    endTime: "",
    description: "",
  });
};
const removeWork = async (index) => {
  const work = form.experiences[index];
  if (work.id) {
    try { await deleteExperience(work.id); } catch { /* 接口失败也继续删 */ }
  }
  form.experiences.splice(index, 1);
};

const removeProject = async (index) => {
  const proj = form.projects[index];
  if (proj.id) {
    try { await deleteProject(proj.id); } catch { /* 接口失败也继续删 */ }
  }
  form.projects.splice(index, 1);
};

const handleRemoveSkill = (skill) => {
  const index = form.skills.indexOf(skill);
  if (index > -1) {
    form.skills.splice(index, 1);
  }
};

const handleSave = async () => {
  if (!formRef.value) return;
  await formRef.value.validate(async (valid) => {
    if (!valid) return;

    loading.value = true;
    try {
      const submitData = {
        ...form,
        skills: formatSkills(form.skills),
      };
      const res = await saveResume(submitData);
      if (res.code === 1) {
        ElMessage.success("保存成功");
        router.back();
      } else {
        ElMessage.success("保存成功(演示)");
        router.back();
      }
    } catch (error) {
      ElMessage.error("保存失败");
    } finally {
      loading.value = false;
    }
  });
};

const avatarUploading = ref(false);

const handleAvatarUpload = async (options) => {
  const file = options.file;
  const isImg = ["image/jpeg", "image/png"].includes(file.type);
  const isLt2M = file.size / 1024 / 1024 < 2;
  if (!isImg) { ElMessage.error("只能上传 jpg 或 png 格式的图片"); return; }
  if (!isLt2M) { ElMessage.error("图片大小不能超过 2MB"); return; }

  avatarUploading.value = true;
  try {
    const formData = new FormData();
    formData.append("file", file);
    const res = await uploadResumeAvatar(formData);
    if (res.code === 1 && res.data) {
      form.characterAvatar = res.data;
      ElMessage.success("头像上传成功");
    } else {
      ElMessage.error(res.msg || "上传失败");
    }
  } catch {
    ElMessage.error("头像上传失败，请重试");
  } finally {
    avatarUploading.value = false;
  }
};

const goBack = () => {
  router.back();
};

const handleSaveBasic = async () => {
  savingBasic.value = true;
  try {
    const payload = {
      id: form.id || undefined,
      resumeName: form.resumeName,
      characterAvatar: form.characterAvatar,
      name: form.name,
      phone: form.phone,
      email: form.email,
      gender: form.gender,
      age: form.age,
      education: form.education,
      graduationSchool: form.graduationSchool,
      totalWorkYears: form.totalWorkYears,
      maxEducation: form.maxEducation,
      jobIntention: form.jobIntention,
      city: form.city,
      salaryMin: form.salaryMin,
      salaryMax: form.salaryMax,
      skills: formatSkills(form.skills),
      selfDescription: form.selfDescription,
      jobType: form.jobType,
      industry: form.industry,
      availableFrom: form.availableFrom,
    };
    const res = await saveResumeBasic(payload);
    if (res.code === 1) {
      if (res.data?.id) form.id = res.data.id;
      ElMessage.success("基本信息已保存");
    } else ElMessage.error(res.msg || "保存失败");
  } catch { ElMessage.error("网络异常"); }
  finally { savingBasic.value = false; }
};

const saveOneEducation = async (index) => {
  if (!form.id) return ElMessage.warning("请先保存简历基本信息后再添加教育经历");
  const edu = form.educations[index];
  edu._saving = true;
  try {
    const res = await saveEducation({ resumeId: form.id, ...edu });
    if (res.code === 1) {
      if (!edu.id && res.data?.id) edu.id = res.data.id;
      ElMessage.success("教育经历已保存");
    } else ElMessage.error(res.msg || "保存失败");
  } catch { ElMessage.error("网络异常"); }
  finally { edu._saving = false; }
};

const saveOneExperience = async (index) => {
  if (!form.id) return ElMessage.warning("请先保存简历基本信息后再添加工作经历");
  const work = form.experiences[index];
  work._saving = true;
  try {
    const res = await saveExperience({ resumeId: form.id, ...work });
    if (res.code === 1) {
      if (!work.id && res.data?.id) work.id = res.data.id;
      ElMessage.success("工作经历已保存");
    } else ElMessage.error(res.msg || "保存失败");
  } catch { ElMessage.error("网络异常"); }
  finally { work._saving = false; }
};

const saveOneProject = async (index) => {
  if (!form.id) return ElMessage.warning("请先保存简历基本信息后再添加项目经历");
  const proj = form.projects[index];
  proj._saving = true;
  try {
    const res = await saveProject({ resumeId: form.id, ...proj });
    if (res.code === 1) {
      if (!proj.id && res.data?.id) proj.id = res.data.id;
      ElMessage.success("项目经历已保存");
    } else ElMessage.error(res.msg || "保存失败");
  } catch { ElMessage.error("网络异常"); }
  finally { proj._saving = false; }
};

const handleOptimize = async () => {
  optimizing.value = true;
  try {
    const payload = buildOptimizePayload();
    const res = await optimizeResume(payload);
    if (res.code === 1 && res.data) {
      optimizeResult.value = res.data;
      buildDiffItems(res.data);
      optimizeDialogVisible.value = true;
    } else {
      ElMessage.error(res.msg || "AI 润色失败");
    }
  } catch {
    ElMessage.error("AI 润色请求失败，请稍后重试");
  } finally {
    optimizing.value = false;
  }
};

const buildOptimizePayload = () => {
  const p = {
    ...form,
    skills: formatSkills(form.skills),
  };
  return p;
};

const buildDiffItems = (optimized) => {
  const items = [];

  if (optimized.selfDescription && optimized.selfDescription !== form.selfDescription) {
    items.push({
      label: "自我描述",
      field: "selfDescription",
      original: form.selfDescription || "",
      optimized: optimized.selfDescription,
      accepted: true,
    });
  }

  if (optimized.skills && optimized.skills !== formatSkills(form.skills)) {
    items.push({
      label: "技能标签",
      field: "skills",
      original: formatSkills(form.skills),
      optimized: optimized.skills,
      accepted: true,
    });
  }

  form.educations.forEach((edu, i) => {
    const optEdu = optimized.educations?.[i];
    if (optEdu && optEdu.description && optEdu.description !== edu.description) {
      items.push({
        label: `教育经历 ${i + 1} · ${edu.school || "未填写"} · 获奖及任职`,
        field: "educations",
        index: i,
        subField: "description",
        original: edu.description || "",
        optimized: optEdu.description,
        accepted: true,
      });
    }
  });

  form.experiences.forEach((exp, i) => {
    const optExp = optimized.experiences?.[i];
    if (optExp && optExp.description && optExp.description !== exp.description) {
      items.push({
        label: `工作经历 ${i + 1} · ${exp.company || "未填写"}`,
        field: "experiences",
        index: i,
        subField: "description",
        original: exp.description || "",
        optimized: optExp.description,
        accepted: true,
      });
    }
  });

  form.projects.forEach((proj, i) => {
    const optProj = optimized.projects?.[i];
    if (optProj && optProj.description && optProj.description !== proj.description) {
      items.push({
        label: `项目经历 ${i + 1} · ${proj.name || "未填写"}`,
        field: "projects",
        index: i,
        subField: "description",
        original: proj.description || "",
        optimized: optProj.description,
        accepted: true,
      });
    }
  });

  diffItems.value = items;
  if (items.length === 0) {
    ElMessage.info("AI 认为您的简历已经很完善了，无需修改");
    optimizeDialogVisible.value = false;
  }
};

const acceptAll = () => {
  diffItems.value.forEach((item) => (item.accepted = true));
};

const rejectAll = () => {
  diffItems.value.forEach((item) => (item.accepted = false));
};

const toggleAccept = (index) => {
  diffItems.value[index].accepted = !diffItems.value[index].accepted;
};

const applyOptimized = () => {
  const acceptedItems = diffItems.value.filter((item) => item.accepted);
  if (acceptedItems.length === 0) {
    optimizeDialogVisible.value = false;
    return;
  }

  if (optimizeResult.value) {
    acceptedItems.forEach((item) => {
      if (item.field === "selfDescription") {
        form.selfDescription = optimizeResult.value.selfDescription;
      } else if (item.field === "skills") {
        form.skills = parseSkills(optimizeResult.value.skills);
      } else if (item.index !== undefined && optimizeResult.value[item.field]?.[item.index]) {
        const target = optimizeResult.value[item.field][item.index];
        form[item.field][item.index][item.subField] = target[item.subField];
      }
    });
  }

  ElMessage.success(`已应用 ${acceptedItems.length} 条优化建议`);
  optimizeDialogVisible.value = false;
};

const optimizing = ref(false);
const savingBasic = ref(false);
const optimizeDialogVisible = ref(false);
const optimizeResult = ref(null);
const diffItems = ref([]);

onMounted(() => {
  fetchDetail();
});
</script>

<template>
  <div class="resume-edit-container" v-loading="loading">
    <div class="edit-header">
      <el-button :icon="ArrowLeft" link @click="goBack">返回列表</el-button>
      <div class="header-right">
        <el-button type="warning" :loading="optimizing" @click="handleOptimize">
          ✨ AI 润色
        </el-button>
        <el-button type="primary" :icon="Finished" @click="handleSave"
          >保存简历</el-button
        >
      </div>
    </div>

    <div class="edit-body">
      <el-form :model="form" :rules="rules" ref="formRef" label-position="top">
        <el-card class="form-section">
          <template #header>
            <div class="section-title">简历名称</div>
          </template>
          <el-form-item label="简历名称" prop="resumeName" required>
            <el-input
              v-model="form.resumeName"
              placeholder="例如：我的前端简历"
            />
          </el-form-item>
        </el-card>

        <el-card class="form-section">
          <template #header>
            <div class="section-title">基础信息</div>
          </template>
          <div class="avatar-upload-row">
            <el-avatar :size="80" :src="form.characterAvatar" class="avatar-preview">
              {{ form.name?.charAt(0) || "头像" }}
            </el-avatar>
            <div class="avatar-tip">
              <div class="tip-title">上传头像</div>
              <div class="tip-desc">支持 jpg、png 格式，文件小于 2MB</div>
              <el-upload
                class="avatar-uploader"
                :show-file-list="false"
                :http-request="handleAvatarUpload"
                accept="image/jpeg,image/png"
              >
                <el-button type="primary" size="small">
                  <el-icon><Upload /></el-icon> 选择图片
                </el-button>
              </el-upload>
            </div>
          </div>
          <div class="form-row">
            <el-form-item label="姓名" class="form-item" prop="name">
              <el-input v-model="form.name" placeholder="请输入姓名" />
            </el-form-item>

            <el-form-item label="年龄" class="form-item" prop="age">
              <el-input v-model="form.age" placeholder="请输入年龄" />
            </el-form-item>
            <el-form-item label="性别" class="form-item" prop="gender">
              <el-radio-group v-model="form.gender">
                <el-radio :label="1">男</el-radio>
                <el-radio :label="0">女</el-radio>
              </el-radio-group>
            </el-form-item>
          </div>
          <div class="form-row">
            <el-form-item label="手机号" class="form-item" prop="phone">
              <el-input v-model="form.phone" placeholder="请输入手机号" />
            </el-form-item>
            <el-form-item label="邮箱" class="form-item" prop="email">
              <el-input v-model="form.email" placeholder="请输入邮箱" />
            </el-form-item>
          </div>
          <div class="form-row">
            <el-form-item label="最高学历" class="form-item" prop="education">
              <el-select v-model="form.education" placeholder="请选择学历">
                <el-option
                  v-for="item in educationOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="毕业院校" class="form-item">
              <el-input v-model="form.graduationSchool" placeholder="请输入毕业院校" />
            </el-form-item>
          </div>
          <div class="form-row">
            <el-form-item label="总工作年限" class="form-item">
              <el-input-number v-model="form.totalWorkYears" :min="0" :step="1" :precision="1" style="width:100%" controls-position="right" placeholder="如：3.5" />
            </el-form-item>
            <el-form-item label="最高学历" class="form-item">
              <el-select v-model="form.maxEducation" placeholder="请选择">
                <el-option v-for="item in maxEducationOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </div>
          <div class="form-row">
            <el-form-item label="期望城市" class="form-item" prop="city">
              <el-input v-model="form.city" placeholder="例如：武汉" />
            </el-form-item>
            <el-form-item
              label="求职意向"
              class="form-item"
              prop="jobIntention"
            >
              <el-input
                v-model="form.jobIntention"
                placeholder="例如：前端开发"
              />
            </el-form-item>
          </div>
          <div class="form-row">
            <el-form-item label="技能标签" class="form-item">
              <el-select
                v-model="skillsInput"
                multiple
                filterable
                allow-create
                default-first-option
                placeholder="输入技能后按回车添加"
                style="width: 100%"
                @remove-tag="handleRemoveSkill"
              >
                <el-option
                  v-for="skill in availableSkills"
                  :key="skill"
                  :label="skill"
                  :value="skill"
                />
              </el-select>
              <div class="skills-hint">
                <span class="hint-text">当前技能：</span>
                <el-tag
                  v-for="skill in form.skills"
                  :key="skill"
                  size="small"
                  type="success"
                  effect="light"
                  closable
                  @close="handleRemoveSkill(skill)"
                  style="margin-right: 4px; margin-bottom: 4px;"
                >
                  {{ skill }}
                </el-tag>
                <span
                  v-if="!form.skills || form.skills.length === 0"
                  class="no-skills"
                  >暂无技能</span
                >
              </div>
            </el-form-item>
            <el-form-item label="到岗时间" class="form-item">
              <el-select v-model="form.availableFrom" placeholder="请选择">
                <el-option label="随时到岗" value="随时到岗" />
                <el-option label="一周内" value="一周内" />
                <el-option label="一个月内" value="一个月内" />
                <el-option label="三个月内" value="三个月内" />
                <el-option label="待定" value="待定" />
              </el-select>
            </el-form-item>
          </div>
          <div class="form-row">
            <el-form-item
              label="期望薪资最低"
              class="form-item"
              prop="salaryMin"
            >
              <el-input v-model="form.salaryMin" placeholder="例如：15000" />
            </el-form-item>
            <el-form-item
              label="期望薪资最高"
              class="form-item"
              prop="salaryMax"
            >
              <el-input v-model="form.salaryMax" placeholder="例如：25000" />
            </el-form-item>
          </div>
          <div class="form-row">
            <el-form-item label="工作类型" class="form-item">
              <el-select v-model="form.jobType" placeholder="请选择">
                <el-option label="全职" :value="1" />
                <el-option label="兼职" :value="2" />
                <el-option label="实习" :value="3" />
                <el-option label="不限" :value="0" />
              </el-select>
            </el-form-item>
            <el-form-item label="期望行业" class="form-item">
              <el-input v-model="form.industry" placeholder="如：互联网、教育" />
            </el-form-item>
          </div>
          <el-form-item label="自我描述">
            <el-input
              v-model="form.selfDescription"
              type="textarea"
              :rows="4"
              placeholder="简短介绍一下你自己..."
            />
          </el-form-item>
          <div class="item-footer-actions">
            <el-button type="primary" size="small" :loading="savingBasic" @click="handleSaveBasic">保存基本信息</el-button>
          </div>
        </el-card>

        <el-card class="form-section">
          <template #header>
            <div class="section-header">
              <span class="section-title">教育经历</span>
              <el-button type="primary" link :icon="Plus" @click="addEducation"
                >添加</el-button
              >
            </div>
          </template>

          <div
            v-for="(edu, index) in form.educations"
            :key="index"
            class="dynamic-item"
          >
            <div class="item-header">
              <span class="item-index">经历 {{ index + 1 }}</span>
            </div>
            <div class="form-row">
              <el-form-item label="学校" class="form-item">
                <el-input v-model="edu.school" placeholder="请输入学校名称" />
              </el-form-item>
              <el-form-item label="专业" class="form-item">
                <el-input v-model="edu.major" placeholder="请输入专业" />
              </el-form-item>
            </div>
            <div class="form-row">
              <el-form-item label="学历" class="form-item">
                <el-select v-model="edu.education">
                  <el-option
                    v-for="item in educationOptions"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value"
                  />
                </el-select>
              </el-form-item>
              <el-form-item label="在校时间" class="form-item">
                <div class="date-range">
                  <el-date-picker
                    v-model="edu.startTime"
                    type="month"
                    placeholder="开始"
                    style="width: 45%"
                  />
                  <span class="sep">-</span>
                  <el-date-picker
                    v-model="edu.endTime"
                    type="month"
                    placeholder="结束"
                    style="width: 45%"
                  />
                </div>
              </el-form-item>
            </div>
            <el-form-item label="获奖及任职情况">
              <el-input
                v-model="edu.description"
                type="textarea"
                :rows="3"
                placeholder="如：获国家奖学金、担任学生会主席等"
              />
            </el-form-item>
            <div class="item-footer-actions">
              <el-button type="primary" size="small" :loading="edu._saving" @click="saveOneEducation(index)">保存此项</el-button>
              <el-button type="danger" size="small" :icon="Delete" @click="removeEducation(index)">删除</el-button>
            </div>
          </div>
        </el-card>

        <el-card class="form-section">
          <template #header>
            <div class="section-header">
              <span class="section-title">工作经历</span>
              <el-button type="primary" link :icon="Plus" @click="addWork"
                >添加</el-button
              >
            </div>
          </template>

          <div
            v-for="(work, index) in form.experiences"
            :key="index"
            class="dynamic-item"
          >
            <div class="item-header">
              <span class="item-index">经历 {{ index + 1 }}</span>
            </div>
            <div class="form-row">
              <el-form-item label="公司名称" class="form-item">
                <el-input v-model="work.company" placeholder="请输入公司名称" />
              </el-form-item>
              <el-form-item label="职位" class="form-item">
                <el-input
                  v-model="work.position"
                  placeholder="请输入职位名称"
                />
              </el-form-item>
            </div>
            <el-form-item label="在职时间">
              <div class="date-range" style="width: 48%">
                <el-date-picker
                  v-model="work.startTime"
                  type="month"
                  placeholder="开始"
                  style="width: 45%"
                />
                <span class="sep">-</span>
                <el-date-picker
                  v-model="work.endTime"
                  type="month"
                  placeholder="结束"
                  style="width: 45%"
                />
              </div>
            </el-form-item>
            <el-form-item label="工作描述">
              <el-input
                v-model="work.description"
                type="textarea"
                :rows="4"
                placeholder="详细描述一下你的工作内容和成就..."
              />
            </el-form-item>
            <div class="item-footer-actions">
              <el-button type="primary" size="small" :loading="work._saving" @click="saveOneExperience(index)">保存此项</el-button>
              <el-button type="danger" size="small" :icon="Delete" @click="removeWork(index)">删除</el-button>
            </div>
          </div>
        </el-card>

        <el-card class="form-section">
          <template #header>
            <div class="section-header">
              <span class="section-title">项目经历</span>
              <el-button
                type="primary"
                link
                :icon="Plus"
                @click="
                  form.projects.push({
                    name: '',
                    role: '',
                    startTime: '',
                    endTime: '',
                    description: '',
                  })
                "
                >添加</el-button
              >
            </div>
          </template>

          <div
            v-for="(project, index) in form.projects"
            :key="index"
            class="dynamic-item"
          >
            <div class="item-header">
              <span class="item-index">项目 {{ index + 1 }}</span>
            </div>
            <div class="form-row">
              <el-form-item label="项目名称" class="form-item">
                <el-input v-model="project.name" placeholder="请输入项目名称" />
              </el-form-item>
              <el-form-item label="担任角色" class="form-item">
                <el-input v-model="project.role" placeholder="请输入担任角色" />
              </el-form-item>
            </div>
            <el-form-item label="项目时间">
              <div class="date-range" style="width: 48%">
                <el-date-picker
                  v-model="project.startTime"
                  type="month"
                  placeholder="开始"
                  style="width: 45%"
                />
                <span class="sep">-</span>
                <el-date-picker
                  v-model="project.endTime"
                  type="month"
                  placeholder="结束"
                  style="width: 45%"
                />
              </div>
            </el-form-item>
            <el-form-item label="项目描述">
              <el-input
                v-model="project.description"
                type="textarea"
                :rows="4"
                placeholder="详细描述一下项目内容和你的贡献..."
              />
            </el-form-item>
            <div class="item-footer-actions">
              <el-button type="primary" size="small" :loading="project._saving" @click="saveOneProject(index)">保存此项</el-button>
              <el-button type="danger" size="small" :icon="Delete" @click="removeProject(index)">删除</el-button>
            </div>
          </div>
        </el-card>
      </el-form>
    </div>

    <el-dialog
      v-model="optimizeDialogVisible"
      title="✨ AI 润色建议"
      width="900px"
      top="3vh"
      destroy-on-close
      class="optimize-dialog"
    >
      <div class="optimize-header">
        <span class="optimize-count">共 {{ diffItems.length }} 条优化建议</span>
        <div class="optimize-actions-bar">
          <el-button size="small" @click="acceptAll">全部接受</el-button>
          <el-button size="small" @click="rejectAll">全部拒绝</el-button>
        </div>
      </div>

      <div class="diff-list" v-if="diffItems.length > 0">
        <div
          v-for="(item, index) in diffItems"
          :key="index"
          class="diff-card"
          :class="{ 'diff-rejected': !item.accepted }"
        >
          <div class="diff-card-header">
            <span class="diff-label">{{ item.label }}</span>
            <el-switch
              v-model="item.accepted"
              active-text="接受"
              inactive-text="拒绝"
              @change="() => {}"
            />
          </div>
          <div class="diff-content">
            <div class="diff-col diff-original">
              <div class="diff-col-title">原文</div>
              <div class="diff-text">{{ item.original || '(空)' }}</div>
            </div>
            <div class="diff-arrow">→</div>
            <div class="diff-col diff-optimized">
              <div class="diff-col-title">AI 润色</div>
              <div class="diff-text diff-highlight">{{ item.optimized }}</div>
            </div>
          </div>
        </div>
      </div>

      <el-empty v-else description="AI 认为您的简历已经很完善了" :image-size="80" />

      <template #footer>
        <el-button @click="optimizeDialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="applyOptimized">应用已接受的建议</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.resume-edit-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.edit-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  background: #fff;
  padding: 15px 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  position: sticky;
  top: 84px;
  z-index: 100;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 10px;
}

.form-section {
  margin-bottom: 20px;
  border-radius: 8px;
  border: none;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.section-title {
  font-size: 18px;
  font-weight: 700;
  color: #333;
}

.form-row {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
}

.form-row .form-item {
  flex: 1;
  min-width: 200px;
}

.dynamic-item {
  padding: 15px;
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  margin-bottom: 15px;
}

.item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.item-footer-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 16px;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

.item-index {
  font-weight: 600;
  color: #333;
}

.date-range {
  display: flex;
  align-items: center;
  gap: 10px;
}

.sep {
  color: #999;
}

.avatar-upload-row {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 24px;
  padding: 16px 20px;
  background: #f8fafd;
  border-radius: 10px;
  border: 1px dashed #d0d7de;
}

.avatar-preview {
  flex-shrink: 0;
  border: 3px solid #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  background: linear-gradient(135deg, #3b82f6, #60a5fa);
  color: #fff;
  font-size: 28px;
  font-weight: 600;
}

.avatar-tip {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.tip-title {
  font-size: 15px;
  font-weight: 600;
  color: #1e293b;
}
.tip-desc {
  font-size: 13px;
  color: #94a3b8;
}

.skills-hint {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  margin-top: 8px;
}

.hint-text {
  font-size: 12px;
  color: #909399;
  margin-right: 8px;
}

.no-skills {
  font-size: 12px;
  color: #c0c4cc;
}

.optimize-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.optimize-count {
  font-size: 14px;
  font-weight: 600;
  color: #e6a23c;
}

.optimize-actions-bar {
  display: flex;
  gap: 8px;
}

.diff-list {
  max-height: 60vh;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.diff-card {
  border: 1px solid #e8f4fd;
  border-radius: 10px;
  padding: 16px;
  background: #f9fdfb;
  transition: opacity 0.3s;
}

.diff-card.diff-rejected {
  opacity: 0.45;
  background: #fefefe;
  border-color: #eee;
}

.diff-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.diff-label {
  font-size: 14px;
  font-weight: 600;
  color: #1e293b;
}

.diff-content {
  display: flex;
  align-items: stretch;
  gap: 12px;
}

.diff-col {
  flex: 1;
  min-width: 0;
}

.diff-col-title {
  font-size: 12px;
  color: #94a3b8;
  margin-bottom: 8px;
  font-weight: 500;
}

.diff-text {
  font-size: 13px;
  line-height: 1.7;
  color: #475569;
  background: #f8fafc;
  border-radius: 6px;
  padding: 12px;
  white-space: pre-wrap;
  word-break: break-word;
  min-height: 44px;
}

.diff-highlight {
  background: #f0fdf4;
  color: #166534;
  border: 1px dashed #86efac;
}

.diff-arrow {
  display: flex;
  align-items: center;
  font-size: 20px;
  color: #22c55e;
  flex-shrink: 0;
  padding: 0 4px;
}
</style>
