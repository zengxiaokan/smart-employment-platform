<script setup>
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";
import {
  Plus,
  Edit,
  Delete,
  Briefcase,
  Location,
  Money,
  Clock,
  Star,
} from "@element-plus/icons-vue";
import {
  getResumeList,
  deleteResume,
  setDefaultResume,
} from "@/api/user/resume";
import { parseSkills } from "@/utils/format";

const router = useRouter();
const loading = ref(false);
const resumeList = ref([]);

// 获取数据
const fetchData = async () => {
  loading.value = true;
  try {
    const res = await getResumeList();
    if (res.code === 1) {
      resumeList.value = res.data.list || [];
    } else {
      resumeList.value = [
        {
          id: 1,
          resumeName: "我的前端开发简历",
          jobIntention: "前端开发工程师",
          gender: 1,
          city: "武汉",
          salaryMin: 15000,
          salaryMax: 25000,
          createdAt: "2026-05-08",
          updatedAt: "2026-05-08",
        },
        {
          id: 2,
          resumeName: "产品经理简历-应聘大厂专用",
          jobIntention: "产品经理",
          gender: 0,
          city: "北京",
          salaryMin: 20000,
          salaryMax: 35000,
          createdAt: "2026-05-07",
          updatedAt: "2026-05-07",
        },
      ];
    }
  } catch (error) {
    console.error("获取简历列表失败:", error);
    resumeList.value = [
      {
        id: 1,
        resumeName: "我的前端开发简历",
        jobIntention: "前端开发工程师",
        gender: 1,
        city: "武汉",
        salaryMin: 15000,
        salaryMax: 25000,
        createdAt: "2026-05-08",
        updatedAt: "2026-05-08",
      },
      {
        id: 2,
        resumeName: "产品经理简历-应聘大厂专用",
        jobIntention: "产品经理",
        gender: 0,
        city: "北京",
        salaryMin: 20000,
        salaryMax: 35000,
        createdAt: "2026-05-07",
        updatedAt: "2026-05-07",
      },
    ];
  } finally {
    loading.value = false;
  }
};

// 新增简历
const handleAdd = () => {
  router.push("/user/resume/edit");
};

// 编辑简历
const handleEdit = (id) => {
  router.push(`/user/resume/edit?id=${id}`);
};

// 删除简历
const handleDelete = (id) => {
  ElMessageBox.confirm("确定要删除这份简历吗？", "提示", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  }).then(async () => {
    try {
      const res = await deleteResume(id);
      if (res.code === 1) {
        ElMessage.success("删除成功");
        fetchData();
      }
    } catch (error) {
      ElMessage.error("删除失败");
    }
  });
};

// 设为默认
const handleSetDefault = async (id) => {
  try {
    const res = await setDefaultResume(id);
    if (res.code === 1) {
      ElMessage.success("设置默认成功");
      fetchData();
    }
  } catch (error) {
    ElMessage.error("设置失败");
  }
};

onMounted(() => {
  fetchData();
});
</script>

<template>
  <div class="resume-list-container" v-loading="loading">
    <div class="page-header">
      <div class="header-left">
        <h2 class="title">我的简历</h2>
        <p class="subtitle">
          共 {{ resumeList.length }} 份简历，默认简历将优先推荐给招聘者
        </p>
      </div>
      <el-button type="primary" :icon="Plus" size="large" @click="handleAdd">
        新增简历
      </el-button>
    </div>

    <div class="resume-grid" v-if="resumeList.length > 0">
      <el-card
        v-for="item in resumeList"
        :key="item.id"
        class="resume-card"
        :class="{ 'is-default': item.isDefault === 1 }"
        shadow="hover"
      >
        <div class="card-header">
          <div class="avatar-wrap">
            <el-avatar
              :size="56"
              :src="item.characterAvatar || ''"
              class="avatar"
              :class="{ 'avatar-default': item.isDefault === 1 }"
            >
              {{ item.name?.charAt(0) || "A" }}
            </el-avatar>
            <div v-if="item.isDefault === 1" class="default-badge">默认</div>
          </div>
          <div class="header-info">
            <h3 class="resume-name">
              {{ item.resumeName }}
              <el-tag
                v-if="item.isDefault === 1"
                type="success"
                size="small"
                effect="dark"
                >默认简历</el-tag
              >
            </h3>
            <span class="job-tag">
              <el-icon><Briefcase /></el-icon>
              {{ item.jobIntention }}
            </span>
          </div>
        </div>

        <div class="card-body">
          <div class="info-row">
            <span class="info-item">
              <el-icon><Location /></el-icon>
              {{ item.city }}
            </span>
            <span class="info-item">
              <el-icon><Money /></el-icon>
              {{ item.salaryMin }}-{{ item.salaryMax }}
            </span>
          </div>
          <div class="skills-row" v-if="item.skills">
            <el-tag
              v-for="skill in parseSkills(item.skills)"
              :key="skill"
              size="small"
              type="success"
              effect="light"
            >
              {{ skill }}
            </el-tag>
          </div>
          <div class="update-time">
            <el-icon><Clock /></el-icon>
            更新于 {{ item.updatedAt }}
          </div>
        </div>

        <div
          class="card-footer"
          :class="{ 'card-footer-default': item.isDefault === 1 }"
        >
          <el-button
            v-if="item.isDefault !== 1"
            type="success"
            plain
            :icon="Star"
            @click="handleSetDefault(item.id)"
          >
            设为默认
          </el-button>
          <el-tag v-else type="success" effect="plain">
            <el-icon><StarFilled /></el-icon> 当前默认
          </el-tag>
          <el-button
            type="primary"
            plain
            :icon="Edit"
            @click="handleEdit(item.id)"
            >编辑</el-button
          >
          <el-button
            type="danger"
            plain
            :icon="Delete"
            @click="handleDelete(item.id)"
            >删除</el-button
          >
        </div>
      </el-card>
    </div>

    <el-empty v-else description="您还没有创建简历，快去新增一份吧" />
  </div>
</template>

<style scoped>
.resume-list-container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.title {
  font-size: 24px;
  font-weight: 700;
  color: #333;
  margin: 0 0 8px 0;
}

.subtitle {
  font-size: 14px;
  color: #909399;
  margin: 0;
}

.resume-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 24px;
}

.resume-card {
  border-radius: 16px;
  border: none;
  background: #e8f4fd;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
}

.resume-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px rgba(59, 130, 246, 0.15);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px 20px 16px;
  border-bottom: 1px solid rgba(59, 130, 246, 0.15);
}

.resume-card {
  border-radius: 16px;
  border: none;
  background: #e8f4fd;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
  position: relative;
}

.resume-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px rgba(59, 130, 246, 0.15);
}

/* 默认简历特殊样式 */
.resume-card.is-default {
  background: linear-gradient(135deg, #f0fdf4 0%, #dcfce7 100%);
  border: 2px solid #22c55e;
}

.resume-card.is-default:hover {
  box-shadow: 0 12px 24px rgba(34, 197, 94, 0.25);
}

.resume-card.is-default::before {
  content: "";
  position: absolute;
  top: 0;
  right: 0;
  border-width: 0 40px 40px 0;
  border-style: solid;
  border-color: transparent #22c55e transparent transparent;
}

.avatar-wrap {
  position: relative;
  flex-shrink: 0;
}

.avatar {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  flex-shrink: 0;
}

.avatar :deep(img) {
  border-radius: 12px;
}

.avatar-default {
  box-shadow: 0 4px 12px rgba(34, 197, 94, 0.4);
}

.default-badge {
  position: absolute;
  bottom: -8px;
  left: 50%;
  transform: translateX(-50%);
  background: #22c55e;
  color: white;
  font-size: 10px;
  font-weight: 600;
  padding: 2px 6px;
  border-radius: 8px;
  white-space: nowrap;
}

.header-info {
  flex: 1;
  min-width: 0;
}

.resume-name {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 8px 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.job-tag {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #3b82f6;
  background: rgba(59, 130, 246, 0.1);
  padding: 4px 10px;
  border-radius: 6px;
}

.job-tag .el-icon {
  font-size: 14px;
}

.card-body {
  padding: 16px 20px;
}

.info-row {
  display: flex;
  gap: 20px;
  margin-bottom: 12px;
}

.info-item {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: #666;
}

.info-item .el-icon {
  color: #999;
}

.update-time {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
}

.skills-row {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  margin-top: 8px;
}

.update-time .el-icon {
  font-size: 14px;
}

.card-footer {
  display: flex;
  gap: 12px;
  padding: 16px 20px;
  background: #e8f4fd;
  border-top: 1px solid rgba(59, 130, 246, 0.15);
}

.card-footer-default {
  background: #f0fdf4;
  border-top: 1px solid rgba(34, 197, 94, 0.2);
}

.card-footer .el-button {
  flex: 1;
  border-radius: 8px;
}

.card-footer .el-tag {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
}

@media (max-width: 768px) {
  .resume-grid {
    grid-template-columns: 1fr;
  }

  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }
}
</style>
