<template>
  <div class="ap-root">
    <el-tabs v-model="activeTab" class="ap-tabs">
      <!-- ============ 企业信息 ============ -->
      <el-tab-pane label="企业信息" name="company">
        <div class="ci-card">
          <div class="ci-card-head">
            <h3>企业基本信息</h3>
            <el-button
              type="primary"
              :icon="Edit"
              size="small"
              @click="editMode = !editMode"
            >
              {{ editMode ? "取消编辑" : "编辑" }}
            </el-button>
          </div>

          <el-descriptions v-if="!editMode" :column="2" border size="large">
            <el-descriptions-item label="企业名称" :span="2">
              <div class="ci-name-row">
                <el-avatar :size="40" :src="info.logoUrl">{{
                  info.name?.charAt(0)
                }}</el-avatar>
                <strong>{{ info.name }}</strong>
                <el-tag
                  v-if="info.auditStatus === 1"
                  type="success"
                  size="small"
                  >已认证</el-tag
                >
                <el-tag v-else type="warning" size="small">审核中</el-tag>
              </div>
            </el-descriptions-item>
            <el-descriptions-item label="联系电话">{{
              info.phone || "-"
            }}</el-descriptions-item>
            <el-descriptions-item label="所在城市">{{
              info.city || "-"
            }}</el-descriptions-item>
            <el-descriptions-item label="所属行业">{{
              info.industry || "-"
            }}</el-descriptions-item>
            <el-descriptions-item label="企业规模">{{
              info.size || "-"
            }}</el-descriptions-item>
            <el-descriptions-item label="融资阶段">{{
              financeLabel(info.financingStage)
            }}</el-descriptions-item>
            <el-descriptions-item label="企业官网">{{
              info.officialWeb || "-"
            }}</el-descriptions-item>
            <el-descriptions-item label="在招职位"
              >{{ info.jobCount ?? 0 }} 个</el-descriptions-item
            >
            <el-descriptions-item label="注册时间">{{
              info.createdAt?.substring(0, 10) || "-"
            }}</el-descriptions-item>
            <el-descriptions-item label="详细地址" :span="2">{{
              info.address || "-"
            }}</el-descriptions-item>
            <el-descriptions-item label="企业简介" :span="2">{{
              info.description || "暂无简介"
            }}</el-descriptions-item>
            <el-descriptions-item label="企业LOGO">
              <el-image
                v-if="info.logoUrl"
                :src="info.logoUrl"
                style="width: 80px; height: 80px; border-radius: 8px"
                fit="cover"
              />
              <span v-else>-</span>
            </el-descriptions-item>
            <el-descriptions-item label="营业执照">
              <el-image
                v-if="info.licenseUrl"
                :src="info.licenseUrl"
                style="width: 80px; height: 80px; border-radius: 8px"
                fit="cover"
              />
              <span v-else>-</span>
            </el-descriptions-item>
          </el-descriptions>

          <el-form v-else :model="editForm" label-width="100px" class="ci-form">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="企业名称"
                  ><el-input v-model="editForm.name"
                /></el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="联系电话"
                  ><el-input v-model="editForm.phone"
                /></el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="所在城市"
                  ><el-input v-model="editForm.city"
                /></el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="所属行业">
                  <el-select v-model="editForm.industry" style="width: 100%">
                    <el-option
                      v-for="ind in industryOptions"
                      :key="ind"
                      :label="ind"
                      :value="ind"
                    />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="企业规模">
                  <el-select v-model="editForm.size" style="width: 100%">
                    <el-option
                      v-for="s in sizeOptions"
                      :key="s"
                      :label="s"
                      :value="s"
                    />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="融资阶段">
                  <el-select
                    v-model="editForm.financingStage"
                    style="width: 100%"
                  >
                    <el-option
                      v-for="f in financeOptions"
                      :key="f.value"
                      :label="f.label"
                      :value="f.value"
                    />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item label="企业官网"
              ><el-input v-model="editForm.officialWeb"
            /></el-form-item>
            <el-form-item label="详细地址"
              ><el-input v-model="editForm.address"
            /></el-form-item>
            <el-form-item label="企业LOGO">
              <div class="logo-upload-row">
                <el-avatar
                  v-if="editForm.logoUrl"
                  :size="72"
                  :src="editForm.logoUrl"
                  shape="square"
                />
                <el-upload
                  :show-file-list="false"
                  :http-request="handleLogoUpload"
                  accept="image/*"
                >
                  <el-button size="small">
                    <el-icon><Upload /></el-icon>
                    {{ editForm.logoUrl ? "更换LOGO" : "上传LOGO" }}
                  </el-button>
                </el-upload>
                <el-button
                  v-if="editForm.logoUrl"
                  size="small"
                  type="danger"
                  @click="editForm.logoUrl = ''"
                >
                  <el-icon><Delete /></el-icon> 移除
                </el-button>
              </div>
            </el-form-item>
            <el-form-item label="企业简介"
              ><el-input
                v-model="editForm.description"
                type="textarea"
                :rows="3"
            /></el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="saving" @click="saveInfo"
                >保存</el-button
              >
            </el-form-item>
          </el-form>
        </div>
      </el-tab-pane>

      <!-- ============ 个人资料 ============ -->
      <el-tab-pane label="个人资料" name="profile">
        <div class="pe-form-wrap">
          <div class="pe-avatar-row">
            <el-avatar :size="72" :src="profile.avatarUrl">{{
              profile.realname?.charAt(0) || profile.nickname?.charAt(0)
            }}</el-avatar>
            <div class="pe-avatar-info">
              <div class="pe-avatar-title">个人头像</div>
              <el-upload
                :show-file-list="false"
                :http-request="handleAvatarUpload"
                accept="image/*"
              >
                <el-button size="small" type="primary">
                  <el-icon><Upload /></el-icon> 更换头像
                </el-button>
              </el-upload>
              <div class="pe-avatar-tip">支持 JPG/PNG，<2MB</div>
            </div>
          </div>
          <div class="pe-block">
            <h4 class="pe-label">基本信息</h4>
            <div class="pe-grid">
              <div class="pe-field">
                <label>真实姓名</label
                ><el-input
                  v-model="profile.realname"
                  placeholder="请输入姓名"
                  size="large"
                />
              </div>
              <div class="pe-field">
                <label>昵称</label
                ><el-input
                  v-model="profile.nickname"
                  placeholder="请输入昵称"
                  size="large"
                />
              </div>
              <div class="pe-field">
                <label>性别</label>
                <div class="pe-radio-group">
                  <span
                    class="pe-radio"
                    :class="{ on: profile.gender === 1 }"
                    @click="profile.gender = 1"
                    >👨 男</span
                  >
                  <span
                    class="pe-radio"
                    :class="{ on: profile.gender === 0 }"
                    @click="profile.gender = 0"
                    >👩 女</span
                  >
                </div>
              </div>
              <div class="pe-field">
                <label>年龄</label
                ><el-input-number
                  v-model="profile.age"
                  :min="16"
                  :max="65"
                  size="large"
                  style="width: 100%"
                  controls-position="right"
                />
              </div>
            </div>
          </div>
          <div class="pe-block">
            <h4 class="pe-label">联系方式</h4>
            <div class="pe-grid">
              <div class="pe-field">
                <label>手机号</label
                ><el-input
                  v-model="profile.phone"
                  placeholder="手机号"
                  size="large"
                />
              </div>
              <div class="pe-field">
                <label>邮箱</label
                ><el-input
                  v-model="profile.email"
                  placeholder="邮箱"
                  size="large"
                />
              </div>
              <div class="pe-field">
                <label>所在城市</label
                ><el-input
                  v-model="profile.city"
                  placeholder="如：武汉"
                  size="large"
                />
              </div>
            </div>
          </div>
          <div class="pe-actions">
            <el-button
              type="primary"
              size="large"
              :loading="profileSaving"
              @click="saveProfile"
              >保存资料</el-button
            >
          </div>
        </div>
      </el-tab-pane>

      <!-- ============ 修改密码 ============ -->
      <el-tab-pane label="修改密码" name="password">
        <el-form :model="pwdForm" label-width="100px" class="pwd-form">
          <el-form-item label="原密码"
            ><el-input
              v-model="pwdForm.oldPwd"
              type="password"
              show-password
              size="large"
          /></el-form-item>
          <el-form-item label="新密码"
            ><el-input
              v-model="pwdForm.newPwd"
              type="password"
              show-password
              size="large"
          /></el-form-item>
          <el-form-item label="确认密码"
            ><el-input
              v-model="pwdForm.confirmPwd"
              type="password"
              show-password
              size="large"
          /></el-form-item>
          <el-form-item>
            <el-button
              type="primary"
              size="large"
              :loading="pwdSaving"
              @click="changePwd"
              >确认修改</el-button
            >
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <!-- ============ 意见反馈 ============ -->
      <el-tab-pane label="意见反馈" name="feedback">
        <FeedbackView />
      </el-tab-pane>

      <!-- ============ 使用帮助 ============ -->
      <el-tab-pane label="使用帮助" name="help">
        <HelpGuide />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from "vue";
import { Edit, Plus, Delete, Upload } from "@element-plus/icons-vue";
import { ElMessage } from "element-plus";
import request from "@/utils/request";
import { uploadLicense } from "@/api/hr/company";
import { getHrProfile, updateHrProfile } from "@/api/hr/profile";
import FeedbackView from "@/views/user/profile/FeedbackView.vue";
import HelpGuide from "./HelpGuide.vue";

const activeTab = ref("company");

const industryOptions = [
  "互联网",
  "金融",
  "教育",
  "医疗",
  "制造",
  "房地产",
  "电商",
  "物流",
  "新能源",
  "人工智能",
];
const sizeOptions = [
  "少于50人",
  "50-99人",
  "100-499人",
  "500-999人",
  "1000人以上",
];
const financeOptions = [
  { label: "未融资", value: 0 },
  { label: "种子轮", value: 1 },
  { label: "天使轮", value: 2 },
  { label: "A轮", value: 3 },
  { label: "B轮", value: 4 },
  { label: "C轮及以上", value: 5 },
];
const financeLabel = (v) => {
  const m = {
    0: "未融资",
    1: "种子轮",
    2: "天使轮",
    3: "A轮",
    4: "B轮",
    5: "C轮及以上",
  };
  return m[v] ?? "-";
};

/* ====== 企业信息 ====== */
const editMode = ref(false);
const saving = ref(false);
const info = reactive({
  id: null,
  name: "",
  phone: "",
  city: "",
  industry: "",
  size: "",
  financingStage: null,
  officialWeb: "",
  address: "",
  description: "",
  logoUrl: "",
  licenseUrl: "",
  auditStatus: 0,
  jobCount: 0,
  createdAt: "",
});
const editForm = reactive({
  id: null,
  name: "",
  phone: "",
  city: "",
  industry: "",
  size: "",
  financingStage: null,
  officialWeb: "",
  address: "",
  description: "",
  logoUrl: "",
});

const loadCompany = () => {
  try {
    const raw = localStorage.getItem("companyInfo");
    if (raw) {
      const d = JSON.parse(raw);
      Object.assign(info, d);
      Object.assign(editForm, {
        id: d.id ?? null,
        name: d.name || "",
        phone: d.phone || "",
        city: d.city || "",
        industry: d.industry || "",
        size: d.size || "",
        financingStage: d.financingStage ?? null,
        officialWeb: d.officialWeb || "",
        address: d.address || "",
        description: d.description || "",
        logoUrl: d.logoUrl || "",
      });
    }
  } catch {}
};

const saveInfo = async () => {
  saving.value = true;
  try {
    const res = await request({
      url: "/hr/company/info",
      method: "put",
      data: { ...editForm },
    });
    if (res.code === 1) {
      Object.assign(info, editForm);
      localStorage.setItem(
        "companyInfo",
        JSON.stringify({ ...info, ...editForm })
      );
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

/* ====== 个人资料 ====== */
const profileSaving = ref(false);
const profile = reactive({
  realname: "",
  nickname: "",
  gender: 1,
  age: null,
  phone: "",
  email: "",
  city: "",
  avatarUrl: "",
});
const loadProfile = async () => {
  try {
    const res = await getHrProfile();
    if (res.code === 1 && res.data) {
      Object.assign(profile, res.data);
    }
  } catch {
    ElMessage.error("获取个人资料失败");
  }
};
const saveProfile = async () => {
  if (!(profile.realname || "").trim()) {
    ElMessage.warning("请输入姓名");
    return;
  }
  profileSaving.value = true;
  try {
    const res = await updateHrProfile({ ...profile });
    if (res.code === 1) {
      const user = JSON.parse(localStorage.getItem("loginUser") || "{}");
      user.nickname = profile.nickname;
      user.avatarUrl = profile.avatarUrl;
      localStorage.setItem("loginUser", JSON.stringify(user));
      ElMessage.success("资料保存成功");
    } else {
      ElMessage.error(res.msg || "保存失败");
    }
  } catch {
    ElMessage.error("网络异常");
  } finally {
    profileSaving.value = false;
  }
};

const handleLogoUpload = async (options) => {
  const file = options.file;
  if (!file.type.startsWith("image/")) {
    ElMessage.warning("只能上传图片文件");
    return;
  }
  if (file.size / 1024 / 1024 > 2) {
    ElMessage.warning("图片大小不能超过 2MB");
    return;
  }
  const fd = new FormData();
  fd.append("file", file);
  try {
    const res = await uploadLicense(fd);
    if (res.code === 1 && res.data) {
      editForm.logoUrl = res.data;
      ElMessage.success("企业LOGO上传成功");
    } else ElMessage.error(res.msg || "上传失败");
  } catch {
    ElMessage.error("上传失败");
  }
};

const handleAvatarUpload = async (options) => {
  const file = options.file;
  if (!file.type.startsWith("image/")) {
    ElMessage.warning("只能上传图片文件");
    return;
  }
  if (file.size / 1024 / 1024 > 2) {
    ElMessage.warning("图片大小不能超过 2MB");
    return;
  }
  const fd = new FormData();
  fd.append("file", file);
  try {
    const res = await request({
      url: "/upload/avatar",
      method: "post",
      data: fd,
      headers: { "Content-Type": "multipart/form-data" },
    });
    if (res.code === 1 && res.data) {
      profile.avatarUrl = res.data;
      ElMessage.success("头像上传成功");
    } else ElMessage.error(res.msg || "上传失败");
  } catch {
    ElMessage.error("头像上传失败");
  }
};

/* ====== 修改密码 ====== */
const pwdSaving = ref(false);
const pwdForm = reactive({ oldPwd: "", newPwd: "", confirmPwd: "" });
const changePwd = async () => {
  if (pwdForm.newPwd !== pwdForm.confirmPwd) {
    ElMessage.error("两次输入的密码不一致");
    return;
  }
  pwdSaving.value = true;
  try {
    const res = await request({
      url: "/login/changepwd",
      method: "post",
      data: { oldPassword: pwdForm.oldPwd, newPassword: pwdForm.newPwd },
    });
    if (res.code === 1) {
      ElMessage.success("密码修改成功");
      pwdForm.oldPwd = "";
      pwdForm.newPwd = "";
      pwdForm.confirmPwd = "";
    } else ElMessage.error(res.msg || "修改失败");
  } catch {
    ElMessage.error("网络异常");
  } finally {
    pwdSaving.value = false;
  }
};

onMounted(() => {
  loadCompany();
  loadProfile();
});
</script>

<style scoped>
.ap-root {
}
.ap-tabs {
}
.ap-tabs :deep(.el-tabs__header) {
  margin-bottom: 20px;
}
.ap-tabs :deep(.el-tabs__item) {
  font-size: 15px;
  font-weight: 600;
}

/* 企业信息 */
.ci-card {
}
.ci-card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}
.ci-card-head h3 {
  font-size: 17px;
  font-weight: 700;
  color: #0f172a;
  margin: 0;
}
.ci-name-row {
  display: flex;
  align-items: center;
  gap: 12px;
}
.ci-name-row strong {
  font-size: 16px;
  color: #0f172a;
}
.ci-form {
  max-width: 700px;
}
.logo-upload-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

/* 个人资料 */
.pe-form-wrap {
  max-width: 640px;
}
.pe-avatar-row {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
  padding-bottom: 20px;
  border-bottom: 1px solid #f0f0f0;
}
.pe-avatar-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.pe-avatar-title {
  font-size: 14px;
  font-weight: 600;
  color: #0f172a;
}
.pe-avatar-tip {
  font-size: 12px;
  color: #94a3b8;
}
.pe-block {
  margin-bottom: 28px;
}
.pe-label {
  font-size: 15px;
  font-weight: 700;
  color: #0f172a;
  margin: 0 0 18px;
  padding-bottom: 10px;
  border-bottom: 2px solid #dbeafe;
}
.pe-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}
.pe-field {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.pe-field > label {
  font-size: 13px;
  font-weight: 600;
  color: #64748b;
}
.pe-radio-group {
  display: flex;
  gap: 12px;
}
.pe-radio {
  flex: 1;
  padding: 10px 16px;
  border: 2px solid #e2e8f0;
  border-radius: 10px;
  text-align: center;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  color: #64748b;
  transition: all 0.2s;
}
.pe-radio:hover {
  border-color: #93c5fd;
}
.pe-radio.on {
  border-color: #3b82f6;
  background: #eff6ff;
  color: #1d4ed8;
  font-weight: 600;
}
.pe-actions {
  display: flex;
  gap: 12px;
  padding-top: 8px;
}

/* 密码 */
.pwd-form {
  max-width: 480px;
}

@media (max-width: 767px) {
  .pe-grid {
    grid-template-columns: 1fr;
  }
}
</style>
