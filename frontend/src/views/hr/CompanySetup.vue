<template>
  <div class="setup-page">
    <div class="setup-card">
      <div class="setup-header">
        <div class="header-icon">🏢</div>
        <h1>注册企业信息</h1>
        <p>您好，HR 在注册账号后需要先注册企业信息，审核通过后即可使用全部功能</p>
      </div>

      <el-form label-position="top" class="create-form">
        <el-form-item label="企业名称" required>
          <el-input v-model="form.companyName" placeholder="请输入企业名称" size="large" />
        </el-form-item>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="联系电话" required>
              <el-input v-model="form.phone" placeholder="企业联系电话" size="large" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所在城市" required>
              <el-input v-model="form.city" placeholder="如：武汉" size="large" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="所属行业" required>
              <el-select v-model="form.industry" placeholder="请选择" size="large" style="width:100%">
                <el-option v-for="ind in industryOptions" :key="ind" :label="ind" :value="ind" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="企业规模" required>
              <el-select v-model="form.size" placeholder="请选择" size="large" style="width:100%">
                <el-option v-for="s in sizeOptions" :key="s" :label="s" :value="s" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="融资阶段">
              <el-select v-model="form.financingStage" placeholder="请选择" size="large" style="width:100%">
                <el-option v-for="f in financeOptions" :key="f.value" :label="f.label" :value="f.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="企业官网">
              <el-input v-model="form.officialWeb" placeholder="选填" size="large" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="详细地址">
          <el-input v-model="form.address" placeholder="请输入详细地址" size="large" />
        </el-form-item>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="企业LOGO" required>
              <div class="upload-box">
                <el-upload
                  :show-file-list="false"
                  :http-request="handleLogoUpload"
                  accept="image/*"
                >
                  <el-button type="primary" plain size="small">
                    <el-icon><Plus /></el-icon>选择LOGO
                  </el-button>
                </el-upload>
                <div class="upload-preview" v-if="logoUrl">
                  <el-image :src="logoUrl" fit="cover" style="width:80px;height:80px;border-radius:8px" />
                  <el-icon class="upload-remove" @click.stop="logoUrl=''"><CircleClose /></el-icon>
                </div>
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="营业执照" required>
              <div class="upload-box">
                <el-upload
                  :show-file-list="false"
                  :http-request="handleLicenseUpload"
                  accept="image/*"
                >
                  <el-button type="primary" plain size="small">
                    <el-icon><Plus /></el-icon>选择执照
                  </el-button>
                </el-upload>
                <div class="upload-preview" v-if="licenseUrl">
                  <el-image :src="licenseUrl" fit="cover" style="width:80px;height:80px;border-radius:8px" />
                  <el-icon class="upload-remove" @click.stop="licenseUrl=''"><CircleClose /></el-icon>
                </div>
              </div>
            </el-form-item>
          </el-col>
        </el-row>

        <el-button type="primary" size="large" class="submit-btn" :loading="submitting"
          :disabled="!form.companyName || !form.phone || !form.city || !form.industry || !form.size || !licenseUrl || !logoUrl"
          @click="submitCreate">
          提交审核
        </el-button>
      </el-form>

      <div class="setup-footer">
        <el-button @click="handleLogout" :icon="SwitchButton">退出登录</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { SwitchButton, Plus, CircleClose } from "@element-plus/icons-vue";
import { submitCompanyApplication, uploadLicense } from "@/api/hr/company";
import { disconnectStomp } from "@/services/stomp.js"
import { resetChat } from "@/services/chatInit"

const router = useRouter();
const submitting = ref(false);

const industryOptions = ["互联网", "金融", "教育", "医疗", "制造", "房地产", "电商", "物流", "新能源", "人工智能"];
const sizeOptions = ["少于50人", "50-99人", "100-499人", "500-999人", "1000人以上"];
const financeOptions = [
  { label: "未融资", value: 0 }, { label: "种子轮", value: 1 }, { label: "天使轮", value: 2 },
  { label: "A轮", value: 3 }, { label: "B轮", value: 4 }, { label: "C轮及以上", value: 5 },
];

const form = reactive({
  companyName: "", phone: "", city: "", industry: "", size: "",
  financingStage: null, officialWeb: "", address: "",
});
const licenseUrl = ref("");
const logoUrl = ref("");

const handleLicenseUpload = async (options) => {
  const file = options.file;
  if (!file.type.startsWith("image/")) { ElMessage.warning("只能上传图片文件"); return; }
  if (file.size / 1024 / 1024 > 5) { ElMessage.warning("图片大小不能超过 5MB"); return; }
  const fd = new FormData(); fd.append("file", file);
  try {
    const res = await uploadLicense(fd);
    if (res.code === 1 && res.data) { licenseUrl.value = res.data; ElMessage.success("营业执照上传成功"); }
    else ElMessage.error(res.msg || "上传失败");
  } catch { ElMessage.error("上传失败"); }
};

const handleLogoUpload = async (options) => {
  const file = options.file;
  if (!file.type.startsWith("image/")) { ElMessage.warning("只能上传图片文件"); return; }
  if (file.size / 1024 / 1024 > 5) { ElMessage.warning("图片大小不能超过 5MB"); return; }
  const fd = new FormData(); fd.append("file", file);
  try {
    const res = await uploadLicense(fd);
    if (res.code === 1 && res.data) { logoUrl.value = res.data; ElMessage.success("企业LOGO上传成功"); }
    else ElMessage.error(res.msg || "上传失败");
  } catch { ElMessage.error("上传失败"); }
};

const submitCreate = async () => {
  if (!licenseUrl.value) { ElMessage.warning("请上传营业执照"); return; }
  if (!logoUrl.value) { ElMessage.warning("请上传企业LOGO"); return; }
  submitting.value = true;
  try {
    const res = await submitCompanyApplication({
      companyName: form.companyName, phone: form.phone, city: form.city,
      industry: form.industry, size: form.size, financingStage: form.financingStage,
      officialWeb: form.officialWeb, address: form.address,
      licenseUrl: licenseUrl.value, logoUrl: logoUrl.value,
    });
    if (res.code === 1) { ElMessage.success("申请已提交，请耐心等待审核"); router.replace("/hr/setup/pending"); }
    else ElMessage.error(res.msg || "提交失败");
  } catch { ElMessage.error("网络异常"); }
  finally { submitting.value = false; }
};

const handleLogout = () => { disconnectStomp(); resetChat(); localStorage.clear(); router.push("/login"); };
</script>

<style scoped>
.setup-page {
  min-height: 100vh; display: flex; align-items: center; justify-content: center;
  background: linear-gradient(150deg, #eff6ff 0%, #dbeafe 50%, #bfdbfe 100%);
  padding: 40px 20px;
}
.setup-card {
  width: 100%; max-width: 620px;
  background: rgba(255,255,255,0.92); backdrop-filter: blur(20px); border-radius: 20px;
  padding: 40px 38px; box-shadow: 0 16px 48px rgba(59,130,246,0.08), 0 0 0 1px rgba(255,255,255,0.6) inset;
}
.setup-header { text-align: center; margin-bottom: 32px; }
.header-icon { font-size: 52px; margin-bottom: 12px; }
.setup-header h1 { font-size: 24px; font-weight: 700; color: #1e293b; margin: 0 0 8px; }
.setup-header p { font-size: 14px; color: #64748b; margin: 0; line-height: 1.7; max-width: 480px; margin: 0 auto; }
.create-form {  }
.create-form :deep(.el-form-item__label) { font-size: 13px; font-weight: 600; color: #475569; }
.upload-box { display:flex; flex-direction:column; gap:10px; }
.upload-preview { position:relative; display:inline-block; }
.upload-remove { position:absolute; top:-6px; right:-6px; color:#ef4444; cursor:pointer; font-size:18px; background:#fff; border-radius:50%; }
.submit-btn { width: 100%; height: 48px; border-radius: 12px; font-size: 16px; font-weight: 600; margin-top: 8px; }
.setup-footer { margin-top: 24px; text-align: center; padding-top: 20px; border-top: 1px solid #f0f2f5; }
</style>