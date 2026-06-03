<template>
  <div class="ji-root">
    <div class="sub-hero">
      <span class="sub-hero-icon">🎯</span>
      <div>
        <h2>求职意向表</h2>
        <p>完善求职意向，让企业更快找到你</p>
      </div>
    </div>

    <div class="ji-card" v-loading="loading">
      <el-form :model="form" label-width="90px" class="ji-form">
        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="姓名" required>
              <el-input v-model="form.name" placeholder="请输入姓名" size="large" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="性别">
              <el-radio-group v-model="form.gender" size="large">
                <el-radio :label="1">男</el-radio>
                <el-radio :label="0">女</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="手机号">
              <el-input v-model="form.phone" placeholder="请输入手机号" size="large" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="邮箱">
              <el-input v-model="form.email" placeholder="请输入邮箱" size="large" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="年龄">
              <el-input v-model="form.age" placeholder="请输入年龄" size="large" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="学历">
              <el-select v-model="form.education" placeholder="请选择学历" size="large" style="width:100%">
                <el-option v-for="item in educationOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="毕业院校">
              <el-input v-model="form.graduationSchool" placeholder="请输入毕业院校" size="large" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="工作城市" required>
              <el-input v-model="form.city" placeholder="如：武汉" size="large" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="总工作年限">
              <el-input-number v-model="form.totalWorkYears" :min="0" :step="1" :precision="1" size="large" style="width:100%" controls-position="right" placeholder="如：3.5" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="最高学历">
              <el-select v-model="form.maxEducation" placeholder="请选择" size="large" style="width:100%">
                <el-option v-for="item in maxEducationOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="期望职位" required>
              <el-input v-model="form.jobIntention" placeholder="如：前端开发工程师" size="large" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="期望行业">
              <el-input v-model="form.industry" placeholder="如：互联网、教育、金融" size="large" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="最低薪资">
              <el-input-number v-model="form.salaryMin" :min="0" :step="1000" size="large" style="width:100%" controls-position="right" placeholder="例如：10000" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="最高薪资">
              <el-input-number v-model="form.salaryMax" :min="0" :step="1000" size="large" style="width:100%" controls-position="right" placeholder="例如：20000" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="工作类型">
              <el-select v-model="form.jobType" placeholder="请选择" size="large" style="width:100%">
                <el-option label="全职" :value="1" />
                <el-option label="兼职" :value="2" />
                <el-option label="实习" :value="3" />
                <el-option label="不限" :value="0" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="到岗时间" required>
              <el-select v-model="form.availableFrom" placeholder="请选择到岗时间" size="large" style="width:100%">
                <el-option label="随时到岗" value="随时到岗" />
                <el-option label="一周内" value="一周内" />
                <el-option label="一个月内" value="一个月内" />
                <el-option label="三个月内" value="三个月内" />
                <el-option label="待定" value="待定" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="技能标签">
          <el-select
            v-model="skillsInput"
            multiple
            filterable
            allow-create
            default-first-option
            placeholder="选择或输入技能，按回车添加"
            size="large"
            style="width:100%"
            @remove-tag="handleRemoveSkill"
            @change="handleSkillsChange"
          >
            <el-option
              v-for="skill in availableSkills"
              :key="skill"
              :label="skill"
              :value="skill"
            />
          </el-select>
          <div class="skills-tags" v-if="form.skills && form.skills.length > 0">
            <el-tag
              v-for="skill in form.skills"
              :key="skill"
              size="small"
              type="success"
              effect="light"
              closable
              @close="handleRemoveSkill(skill)"
            >
              {{ skill }}
            </el-tag>
          </div>
        </el-form-item>

        <el-form-item label="自我描述">
          <el-input v-model="form.selfDescription" type="textarea" :rows="3" size="large" placeholder="一句话介绍自己，如：3年前端经验，精通Vue3和TypeScript" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" size="large" :loading="saving" @click="handleSave">保存求职意向</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getDefaultResume, saveJobIntention } from '@/api/user/resume'
import { parseSkills, formatSkills } from '@/utils/format'

const loading = ref(false)
const saving = ref(false)

const skillsInput = ref([])
const educationOptions = [
  { label: '初中', value: 0 },
  { label: '高中', value: 1 },
  { label: '中专', value: 2 },
  { label: '大专', value: 3 },
  { label: '本科', value: 4 },
  { label: '硕士', value: 5 },
  { label: '博士', value: 6 },
]
const maxEducationOptions = [
  { label: '初中', value: 0 },
  { label: '高中', value: 1 },
  { label: '中专', value: 2 },
  { label: '大专', value: 3 },
  { label: '本科', value: 4 },
  { label: '硕士', value: 5 },
  { label: '博士', value: 6 },
]
const availableSkills = [
  'JavaScript', 'TypeScript', 'Vue', 'Vue3', 'React', 'Angular', 'Node.js',
  'Java', 'Python', 'Go', 'PHP', 'C++', 'C#', 'Ruby', 'Rust', 'Kotlin', 'Swift',
  'HTML', 'CSS', 'Sass', 'Less', 'Tailwind CSS', 'Bootstrap',
  'Element Plus', 'Ant Design', 'Naive UI', 'Vant',
  'Axios', 'Fetch', 'WebSocket', 'GraphQL', 'RESTful API',
  'MySQL', 'PostgreSQL', 'MongoDB', 'Redis', 'Elasticsearch', 'Oracle',
  'Git', 'Docker', 'Kubernetes', 'Linux', 'Nginx', 'CI/CD',
  'Spring Boot', 'Spring Cloud', 'MyBatis', 'Hibernate', 'Maven', 'Gradle',
  'Webpack', 'Vite', 'Gulp', 'Rollup', 'ESBuild',
  'Jest', 'Mocha', 'Cypress', 'Selenium',
  'AWS', 'Azure', 'GCP', '阿里云', '腾讯云',
  'Figma', 'Sketch', 'Photoshop', 'Illustrator',
  '小程序开发', 'UniApp', 'Taro', 'Flutter', 'React Native', 'Electron',
  '性能优化', 'SEO', '单元测试', '微服务', '大数据', 'AI/机器学习',
]

const form = reactive({
  id: null,
  name: '',
  phone: '',
  email: '',
  gender: 1,
  age: null,
  education: 4,
  jobIntention: '',
  city: '',
  salaryMin: null,
  salaryMax: null,
  jobType: 1,
  skills: [],
  industry: '',
  availableFrom: '随时到岗',
  selfDescription: '',
  graduationSchool: '',
  totalWorkYears: null,
  maxEducation: null,
})

onMounted(async () => {
  loading.value = true
  try {
    const res = await getDefaultResume()
    if (res.code === 1 && res.data) {
      const d = res.data
      form.id = d.id ?? null
      form.name = d.name || ''
      form.phone = d.phone || ''
      form.email = d.email || ''
      form.gender = d.gender ?? 1
      form.age = d.age ?? null
      form.education = d.education ?? 4
      form.jobIntention = d.jobIntention || ''
      form.city = d.city || ''
      form.salaryMin = d.salaryMin ?? null
      form.salaryMax = d.salaryMax ?? null
      form.jobType = d.jobType ?? 1
      form.skills = parseSkills(d.skills || '')
      skillsInput.value = [...form.skills]
      form.industry = d.industry || ''
      form.availableFrom = d.availableFrom || '随时到岗'
      form.selfDescription = d.selfDescription || ''
      form.graduationSchool = d.graduationSchool || ''
      form.totalWorkYears = d.totalWorkYears ?? null
      form.maxEducation = d.maxEducation ?? null
    }
  } catch {
    ElMessage.error('获取简历信息失败')
  } finally {
    loading.value = false
  }
})

const handleRemoveSkill = (skill) => {
  const idx = form.skills.indexOf(skill)
  if (idx > -1) {
    form.skills.splice(idx, 1)
    skillsInput.value = [...form.skills]
  }
}

const handleSkillsChange = (val) => {
  form.skills = val || []
}

const handleSave = async () => {
  if (!form.name.trim()) return ElMessage.warning('请填写姓名')
  if (!form.jobIntention.trim()) return ElMessage.warning('请填写期望职位')
  if (!form.city.trim()) return ElMessage.warning('请填写工作城市')
  if (!form.availableFrom) return ElMessage.warning('请选择到岗时间')
  saving.value = true
  try {
    const submitData = { ...form, skills: formatSkills(form.skills) }
    const res = await saveJobIntention(submitData)
    if (res.code === 1) {
      ElMessage.success('求职意向已保存')
    } else {
      ElMessage.error(res.msg || '保存失败')
    }
  } catch {
    ElMessage.error('网络异常')
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.sub-hero { display:flex; align-items:center; gap:16px; padding:24px 28px; background:linear-gradient(135deg,#eff6ff,#dbeafe); border-radius:14px; margin-bottom:28px; }
.sub-hero-icon { font-size:40px; }
.sub-hero h2 { font-size:20px; font-weight:700; color:#1e40af; margin:0 0 4px; }
.sub-hero p { font-size:13px; color:#64748b; margin:0; }

.ji-card { background:#fff; border-radius:14px; padding:28px; box-shadow:0 1px 4px rgba(0,0,0,.03); }
.skills-tags { display:flex; flex-wrap:wrap; gap:8px; margin-top:10px; }
</style>
