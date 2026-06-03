<template>
  <div class="jm-root">
    <div class="jm-actions">
      <el-button type="primary" @click="goCreate">发布新职位</el-button>
      <el-button @click="doRefresh">刷新</el-button>
    </div>

    <!-- 搜索栏 -->
    <div class="jm-search">
      <el-form :model="query" inline>
        <el-form-item label="职位名称">
          <el-input
            v-model="query.keyword"
            placeholder="输入关键字"
            clearable
            style="width: 180px"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select
            v-model="query.status"
            placeholder="全部"
            clearable
            style="width: 130px"
          >
            <el-option label="审核中" :value="0" />
            <el-option label="审核成功" :value="1" />
            <el-option label="已下架" :value="2" />
            <el-option label="强制下架" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="发布时间">
          <el-date-picker
            v-model="query.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 260px"
            :shortcuts="dateShortcuts"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="doSearch">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 列表 -->
    <div class="jm-card">
      <el-table :data="list" stripe v-loading="loading" class="jm-table">
        <el-table-column label="编号" width="70">
          <template #default="{ $index }">{{
            (page - 1) * size + $index + 1
          }}</template>
        </el-table-column>
        <el-table-column
          prop="title"
          label="职位名称"
          min-width="180"
          show-overflow-tooltip
        />
        <el-table-column label="薪资" width="130">
          <template #default="{ row }">
            <span class="jm-salary" v-if="row.salaryMin || row.salaryMax">{{
              formatSalary(row.salaryMin, row.salaryMax)
            }}</span>
            <span v-else>面议</span>
          </template>
        </el-table-column>
        <el-table-column prop="city" label="地点" width="100" />
        <el-table-column label="学历" width="100">
          <template #default="{ row }">{{ eduLabel(row.education) }}</template>
        </el-table-column>
        <el-table-column label="经验" width="100">
          <template #default="{ row }">{{ row.experience || "-" }}</template>
        </el-table-column>
        <el-table-column label="岗位数" width="80">
          <template #default="{ row }">{{ row.hasCount ?? "-" }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">
              {{ statusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="浏览/投递" width="110">
          <template #default="{ row }">
            {{ row.viewCount || 0 }} / {{ row.applyCount || 0 }}
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="发布时间" width="120">
          <template #default="{ row }">{{
            row.createdAt?.substring(0, 10) || "-"
          }}</template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="goEdit(row)">编辑</el-button>
            <el-button
              v-if="row.status === 1"
              type="warning"
              size="small"
              @click="toggleStatus(row, 2)"
              >下架</el-button
            >
            <el-button
              v-else-if="row.status === 2"
              type="success"
              size="small"
              @click="toggleStatus(row, 1)"
              >上架</el-button
            >
            <el-button
              v-else-if="row.status === 3"
              type="primary"
              size="small"
              @click="toggleStatus(row, 0)"
              >申请上架</el-button
            >
          </template>
        </el-table-column>
      </el-table>

      <div class="jm-pager" v-if="total > 0">
        <span class="jm-total">共 {{ total }} 条</span>
        <el-pagination
          background
          layout="prev, pager, next"
          :total="total"
          :page-size="size"
          v-model:current-page="page"
          @current-change="fetch"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from "vue";
import { useRouter } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";
import { getJobList, saveJob } from "@/api/hr/job";
import { formatSalary } from "@/utils/format";

const router = useRouter();
const loading = ref(false);
const list = ref([]);
const page = ref(1);
const size = ref(10);
const total = ref(0);

const query = reactive({ keyword: "", status: null, dateRange: null });

const dateShortcuts = [
  {
    text: "最近一周",
    value: () => {
      const e = new Date();
      const s = new Date();
      s.setDate(s.getDate() - 7);
      return [s, e];
    },
  },
  {
    text: "最近一个月",
    value: () => {
      const e = new Date();
      const s = new Date();
      s.setMonth(s.getMonth() - 1);
      return [s, e];
    },
  },
  {
    text: "最近三个月",
    value: () => {
      const e = new Date();
      const s = new Date();
      s.setMonth(s.getMonth() - 3);
      return [s, e];
    },
  },
  {
    text: "最近半年",
    value: () => {
      const e = new Date();
      const s = new Date();
      s.setMonth(s.getMonth() - 6);
      return [s, e];
    },
  },
  {
    text: "最近一年",
    value: () => {
      const e = new Date();
      const s = new Date();
      s.setFullYear(s.getFullYear() - 1);
      return [s, e];
    },
  },
];

const eduLabel = (v) =>
  ({
    0: "不限",
    1: "初中",
    2: "高中",
    3: "中专",
    4: "大专",
    5: "本科",
    6: "硕士",
    7: "博士",
  }[v] || "-");

const statusLabel = (v) =>
  ({ 0: "审核中", 1: "审核成功", 2: "已下架", 3: "强制下架" }[v] || "-");
const statusType = (v) =>
  ({ 0: "warning", 1: "success", 2: "info", 3: "danger" }[v] || "info");

const buildParams = () => {
  const p = { page: page.value, size: size.value };
  if (query.keyword) p.keyword = query.keyword;
  if (query.status !== null && query.status !== "") p.status = query.status;
  if (query.dateRange && query.dateRange.length === 2) {
    p.dateFrom = query.dateRange[0];
    p.dateTo = query.dateRange[1];
  }
  return p;
};

const fetch = async () => {
  loading.value = true;
  try {
    const res = await getJobList(buildParams());
    if (res.code === 1 && res.data) {
      list.value = res.data.list || [];
      total.value = res.data.total || 0;
    } else {
      list.value = [];
      total.value = 0;
    }
  } catch {
    list.value = [];
    total.value = 0;
  } finally {
    loading.value = false;
  }
};

const doSearch = () => {
  page.value = 1;
  fetch();
};
const doRefresh = () => {
  page.value = 1;
  fetch();
};
const resetSearch = () => {
  query.keyword = "";
  query.status = null;
  query.dateRange = null;
  page.value = 1;
  fetch();
};

const goCreate = () => router.push("/hr/jobs/create");
const goEdit = (row) => {
  sessionStorage.setItem("editJobData", JSON.stringify(row));
  router.push(`/hr/jobs/edit?id=${row.id}`);
};

const toggleStatus = (row, status) => {
  const label = status === 2 ? "下架" : status === 0 ? "申请上架" : "上架";
  ElMessageBox.confirm(`确定${label}该职位？`, "提示", { type: "warning" })
    .then(async () => {
      try {
        const res = await saveJob({ id: row.id, status });
        if (res.code === 1) {
          row.status = status;
          ElMessage.success(`${label}成功`);
        } else ElMessage.error(res.msg || "操作失败");
      } catch {
        ElMessage.error("网络异常");
      }
    })
    .catch(() => {});
};

onMounted(fetch);
</script>

<style scoped>
.jm-root {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.jm-actions {
  display: flex;
  gap: 10px;
  flex-shrink: 0;
  justify-content: flex-end;
}

.jm-search,
.jm-card {
  background: #ffffff;
  border-radius: 20px;
  padding: 20px 24px;
  border: 1px solid #e2e8f0;
  box-shadow: 0 8px 20px rgba(15, 23, 42, 0.04);
}

.jm-salary {
  color: #334155;
  font-weight: 700;
}

.jm-pager {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 16px;
}

.jm-total {
  font-size: 13px;
  color: #64748b;
}

.jm-search :deep(.el-form-item__label) {
  color: #64748b;
}

.jm-search :deep(.el-input__wrapper),
.jm-search :deep(.el-select__wrapper),
.jm-search :deep(.el-textarea__inner),
.jm-search :deep(.el-range-editor) {
  background: #ffffff !important;
  box-shadow: none !important;
  border: 1px solid #e2e8f0 !important;
}

.jm-search :deep(.el-input__inner),
.jm-search :deep(.el-select__selected-item),
.jm-search :deep(.el-range-input),
.jm-search :deep(.el-range-separator),
.jm-search :deep(.el-input__inner::placeholder) {
  color: #334155 !important;
}

.jm-card :deep(.el-table) {
  --el-table-bg-color: transparent;
  --el-table-tr-bg-color: transparent;
  --el-table-border-color: #e2e8f0;
  --el-table-header-bg-color: #f8fafc;
  --el-table-row-hover-bg-color: #f8fafc;
  --el-table-text-color: #334155;
  --el-table-header-text-color: #64748b;
}

.jm-card :deep(.el-table th.el-table__cell),
.jm-card :deep(.el-table td.el-table__cell) {
  background: transparent;
}

@media (max-width: 840px) {
  .jm-actions {
    width: 100%;
    flex-wrap: wrap;
  }

  .jm-pager {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }
}
</style>
