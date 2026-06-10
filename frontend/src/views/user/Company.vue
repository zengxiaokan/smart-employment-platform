<template>
  <div class="company-page" v-loading="loading">
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">全部公司</h2>
        <span class="company-count">共 {{ total }} 家公司</span>
      </div>
      <div class="header-right">
        <el-input
          v-model="searchText"
          placeholder="搜索公司名称"
          :prefix-icon="Search"
          clearable
          class="search-input"
          @keyup.enter="handleSearch"
        />
        <el-select
          v-model="filterIndustry"
          placeholder="行业筛选"
          clearable
          class="filter-select"
        >
          <el-option
            v-for="industry in industryOptions"
            :key="industry"
            :label="industry"
            :value="industry"
          />
        </el-select>
        <el-select
          v-model="filterSize"
          placeholder="规模筛选"
          clearable
          class="filter-select"
        >
          <el-option
            v-for="size in sizeOptions"
            :key="size"
            :label="size"
            :value="size"
          />
        </el-select>
        <el-button type="primary" :icon="Search" @click="handleSearch"
          >查询</el-button
        >
        <el-button @click="resetFilter">重置</el-button>
      </div>
    </div>

    <div class="company-grid" v-if="filteredCompanies.length > 0">
      <div
        v-for="company in filteredCompanies"
        :key="company.id"
        class="company-card"
        @click="handleCompanyClick(company)"
      >
        <div class="card-top">
          <img :src="company.logoUrl" class="company-logo" />
          <div class="company-main">
            <div class="company-name-row">
              <h3 class="company-name">{{ company.name }}</h3>
              <el-button size="small" text type="warning" :icon="WarningFilled" class="btn-report" @click.stop="handleReport(company)">举报</el-button>
            </div>
            <div class="company-tags">
              <el-tag size="small" type="info">{{ company.industry }}</el-tag>
              <el-tag size="small">{{ company.size }}</el-tag>
              <el-tag
                v-if="company.status === '已上市'"
                size="small"
                type="success"
                >已上市</el-tag
              >
              <el-tag
                v-else-if="company.status === '未融资'"
                size="small"
                type="warning"
                >未融资</el-tag
              >
              <el-tag v-else size="small" type="danger">{{
                company.status
              }}</el-tag>
            </div>
          </div>
          <span class="job-count">{{ company.jobCount }}个职位</span>
        </div>
        <p class="company-desc">{{ company.description }}</p>
      </div>
    </div>

    <el-empty v-else description="暂无匹配的公司">
      <el-button type="primary" @click="resetFilter">重置筛选</el-button>
    </el-empty>

    <div class="pagination-wrapper" v-if="total > pageSize">
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        layout="prev, pager, next, total"
        @current-change="handlePageChange"
        background
      />
    </div>
    <ReportDialog v-model:visible="reportVisible" :target-type="2" :target-id="reportTarget?.id" :target-name="reportTarget?.name || ''" label="公司" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import { useRouter } from "vue-router";
import { Search, WarningFilled } from "@element-plus/icons-vue";
import { getCompanyList, getCompanyFilters } from "@/api/user/company";
import { ElMessage } from "element-plus";
import ReportDialog from "@/components/ReportDialog.vue";

const router = useRouter();

const searchText = ref("");
const filterIndustry = ref("");
const filterSize = ref("");
const loading = ref(false);
const companies = ref([]);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);

// 动态筛选选项
const industryOptions = ref([]);
const sizeOptions = ref([]);

const filteredCompanies = computed(() => {
  return companies.value;
});

const handleCompanyClick = (company) => {
  router.push(`/user/company/${company.id}`);
};

// ==================== 举报 ====================
const reportVisible = ref(false);
const reportTarget = ref(null);

const handleReport = (company) => {
  reportTarget.value = company;
  reportVisible.value = true;
};

const fetchFilters = async () => {
  try {
    const res = await getCompanyFilters();
    if (res.code === 1 && res.data) {
      industryOptions.value = res.data.industries || [];
      sizeOptions.value = res.data.sizes || [];
    }
  } catch (error) {
    console.error("获取筛选选项失败:", error);
  }
};

const fetchCompanies = async () => {
  loading.value = true;
  try {
    const params = {
      page: currentPage.value,
      pageSize: pageSize.value,
      keyword: searchText.value || undefined,
      industry: filterIndustry.value || undefined,
      companySize: filterSize.value || undefined,
    };
    const res = await getCompanyList(params);
    if (res.code === 1 && res.data) {
      companies.value = res.data.list || res.data.records || [];
      total.value = res.data.total || 0;
    }
  } catch (error) {
    console.error("获取公司列表失败:", error);
  } finally {
    loading.value = false;
  }
};

const handleSearch = () => {
  currentPage.value = 1;
  fetchCompanies();
};

const handlePageChange = (page) => {
  currentPage.value = page;
  fetchCompanies();
};

const resetFilter = () => {
  searchText.value = "";
  filterIndustry.value = "";
  filterSize.value = "";
  currentPage.value = 1;
  fetchCompanies();
};

onMounted(() => {
  fetchFilters();
  fetchCompanies();
});
</script>

<style scoped>
.company-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 16px;
  margin-bottom: 24px;
}

.header-left {
  display: flex;
  align-items: baseline;
  gap: 12px;
}

.page-title {
  font-size: 22px;
  font-weight: 700;
  color: #1e293b;
  margin: 0;
}

.company-count {
  font-size: 14px;
  color: #909399;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.search-input {
  width: 220px;
}

.filter-select {
  width: 130px;
}

.header-right .el-button {
  border-radius: 8px;
  font-weight: 500;
}

.header-right .el-button--primary {
  background: linear-gradient(135deg, #22c55e 0%, #16a34a 100%);
  border: none;
}

.header-right .el-button--primary:hover {
  background: linear-gradient(135deg, #16a34a 0%, #15803d 100%);
}

.header-right .el-button:not(.el-button--primary) {
  background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
  border: none;
  color: white;
}

.header-right .el-button:not(.el-button--primary):hover {
  background: linear-gradient(135deg, #d97706 0%, #b45309 100%);
}

.company-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.company-card {
  background: #fff;
  border-radius: 10px;
  padding: 20px 24px;
  border: 1px solid #f0f2f5;
  transition: all 0.2s;
  cursor: pointer;
}

.company-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  transform: translateY(-2px);
  border-color: #e0e7ff;
}

.card-top {
  display: flex;
  align-items: center;
  gap: 16px;
}

.company-logo {
  width: 56px;
  height: 56px;
  border-radius: 10px;
  object-fit: cover;
  border: 1px solid #f0f2f5;
  flex-shrink: 0;
}

.company-main {
  flex: 1;
  min-width: 0;
}

.company-name-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.company-name {
  font-size: 17px;
  font-weight: 700;
  color: #1e293b;
  margin: 0;
}

.btn-report {
  flex-shrink: 0;
  font-size: 12px;
  color: #c0c4cc;
  padding: 2px 6px;
  transition: color 0.2s;
}
.btn-report:hover { color: #f56c6c !important; }
.btn-report :deep(.el-icon) { font-size: 14px; }

.company-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.company-desc {
  font-size: 13px;
  color: #64748b;
  line-height: 1.7;
  margin: 12px 0 0 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.job-count {
  font-size: 14px;
  color: #00bfa5;
  font-weight: 600;
  white-space: nowrap;
  flex-shrink: 0;
  margin-left: auto;
}

@media (max-width: 900px) {
  .company-grid {
    grid-template-columns: 1fr;
  }

  .page-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .header-right {
    width: 100%;
    flex-wrap: wrap;
  }

  .search-input {
    width: 100%;
    flex: 1;
  }
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 30px;
  padding: 20px 0;
}
</style>
