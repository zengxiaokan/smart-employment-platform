<script setup>
import { ref, computed, watch, onMounted, onUnmounted, nextTick } from "vue";
import * as echarts from "echarts";
import {
  Briefcase,
  Document,
  User,
  CircleCheck,
} from "@element-plus/icons-vue";
import {
  getAnalyticsOverview,
  getAnalyticsTrend,
  getAnalyticsFunnel,
} from "@/api/hr/analytics";

const loading = ref(false);

const fmtPercent = (v) => {
  if (v == null) return "0";
  if (typeof v === "string") return v.replace("%", "");
  return Math.round(v).toString();
};

const analyticsData = ref({
  totalJobs: 0,
  activeJobs: 0,
  totalApplications: 0,
  weekApplications: 0,
  interviewRate: 0,
  hireRate: 0,
});

const chartData = ref([]);
const positionData = ref([]);

const trendChartRef = ref(null);
let trendChart = null;

const dateRangeText = ref("");

const maxFunnelVal = computed(() => {
  if (!positionData.value.length) return 1;
  return Math.max(
    ...positionData.value.map((d) =>
      Math.max(d.applications || 0, d.interviews || 0, d.hired || 0)
    )
  );
});

const getDateRange = () => {
  const yesterday = new Date();
  yesterday.setDate(yesterday.getDate() - 1);
  const end = yesterday.toISOString().substring(0, 10);
  const start = new Date(yesterday);
  start.setDate(start.getDate() - 6);
  return { startDate: start.toISOString().substring(0, 10), endDate: end };
};

const fetchOverview = async () => {
  loading.value = true;
  try {
    const range = getDateRange();
    dateRangeText.value = range.startDate + " ~ " + range.endDate;
    const res = await getAnalyticsOverview(range.startDate, range.endDate);
    if (res.code === 1 && res.data) {
      analyticsData.value = res.data;
    }
  } catch {
  } finally {
    loading.value = false;
  }
};

const fetchTrend = async () => {
  try {
    const { startDate, endDate } = getDateRange();
    const res = await getAnalyticsTrend(startDate, endDate);
    if (res.code === 1 && res.data) {
      chartData.value = Array.isArray(res.data)
        ? res.data
        : res.data.records || [];
    }
  } catch {}
};

const fetchFunnel = async () => {
  try {
    const res = await getAnalyticsFunnel();
    if (res.code === 1 && res.data) {
      positionData.value = Array.isArray(res.data)
        ? res.data
        : res.data.records || [];
    }
  } catch {}
};

const initTrendChart = (data) => {
  if (!trendChartRef.value) return;
  if (!trendChart) {
    trendChart = echarts.init(trendChartRef.value);
  }
  const dates = data.map((d) => d.date?.substring(5) || d.date);
  trendChart.setOption({
    tooltip: { trigger: "axis" },
    legend: { data: ["投递数", "面试数"], bottom: 0 },
    grid: { left: 50, right: 30, top: 20, bottom: 35 },
    xAxis: { type: "category", data: dates, axisLabel: { fontSize: 11 } },
    yAxis: { type: "value", minInterval: 1 },
    series: [
      {
        name: "投递数",
        type: "line",
        data: data.map((d) => d.applications),
        smooth: true,
        itemStyle: { color: "#667eea" },
        lineStyle: { width: 2 },
      },
      {
        name: "面试数",
        type: "line",
        data: data.map((d) => d.interviews),
        smooth: true,
        itemStyle: { color: "#38ef7d" },
        lineStyle: { width: 2 },
      },
    ],
  });
};

const handleResize = () => {
  trendChart?.resize();
};

watch(chartData, (val) => {
  if (val.length) nextTick(() => initTrendChart(val));
});

onMounted(() => {
  fetchOverview();
  fetchTrend();
  fetchFunnel();
  window.addEventListener("resize", handleResize);
});

onUnmounted(() => {
  window.removeEventListener("resize", handleResize);
  trendChart?.dispose();
});
</script>

<template>
  <div class="data-analytics" v-loading="loading">
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon blue">
            <el-icon><Briefcase /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ analyticsData.totalJobs }}</div>
            <div class="stat-label">发布职位</div>
            <div class="stat-sub">在招 {{ analyticsData.activeJobs }} 个</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon green">
            <el-icon><Document /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ analyticsData.totalApplications }}</div>
            <div class="stat-label">累计简历</div>
            <div class="stat-sub">
              本周新增 {{ analyticsData.weekApplications }}
            </div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon purple">
            <el-icon><User /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">
              {{ fmtPercent(analyticsData.interviewRate)
              }}<span class="stat-unit">%</span>
            </div>
            <div class="stat-label">面试转化率</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon orange">
            <el-icon><CircleCheck /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">
              {{ fmtPercent(analyticsData.hireRate)
              }}<span class="stat-unit">%</span>
            </div>
            <div class="stat-label">入职成功率</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-bottom: 20px" class="chart-row">
      <el-col :span="12">
        <div class="chart-card">
          <h3 class="card-title">本周投递与面试趋势</h3>
          <div ref="trendChartRef" class="chart-box"></div>
        </div>
      </el-col>
      <el-col :span="12">
        <div class="chart-card chart-card-funnel">
          <h3 class="card-title">各职位招聘漏斗</h3>
          <div class="funnel-scroll" v-if="positionData.length">
            <div class="funnel-row funnel-head">
              <span class="funnel-name-hd">职位</span>
              <span class="funnel-col-hd">投递</span>
              <span class="funnel-col-hd">面试</span>
              <span class="funnel-col-hd">入职</span>
              <span class="funnel-rate-hd">面试率</span>
              <span class="funnel-rate-hd">入职率</span>
            </div>
            <div class="funnel-row" v-for="d in positionData" :key="d.position">
              <span class="funnel-name" :title="d.position">{{
                d.position
              }}</span>
              <div class="funnel-col">
                <div class="funnel-bar-track">
                  <div
                    class="funnel-bar-fill a"
                    :style="{
                      width: (d.applications / maxFunnelVal) * 100 + '%',
                    }"
                  ></div>
                </div>
                <span class="funnel-num n-app">{{ d.applications }}</span>
              </div>
              <div class="funnel-col">
                <div class="funnel-bar-track">
                  <div
                    class="funnel-bar-fill b"
                    :style="{
                      width: (d.interviews / maxFunnelVal) * 100 + '%',
                    }"
                  ></div>
                </div>
                <span class="funnel-num n-int">{{ d.interviews }}</span>
              </div>
              <div class="funnel-col">
                <div class="funnel-bar-track">
                  <div
                    class="funnel-bar-fill c"
                    :style="{ width: (d.hired / maxFunnelVal) * 100 + '%' }"
                  ></div>
                </div>
                <span class="funnel-num n-hire">{{ d.hired }}</span>
              </div>
              <div class="funnel-rate-col">
                <el-progress
                  :percentage="
                    d.applications
                      ? Math.round((d.interviews / d.applications) * 100)
                      : 0
                  "
                  :stroke-width="8"
                  :show-text="false"
                  color="#e6a23c"
                />
                <span class="funnel-pct y"
                  >{{
                    d.applications
                      ? Math.round((d.interviews / d.applications) * 100)
                      : 0
                  }}%</span
                >
              </div>
              <div class="funnel-rate-col">
                <el-progress
                  :percentage="
                    d.applications
                      ? Math.round((d.hired / d.applications) * 100)
                      : 0
                  "
                  :stroke-width="8"
                  :show-text="false"
                  color="#67c23a"
                />
                <span class="funnel-pct g"
                  >{{
                    d.applications
                      ? Math.round((d.hired / d.applications) * 100)
                      : 0
                  }}%</span
                >
              </div>
            </div>
          </div>
          <el-empty v-else description="暂无数据" />
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.data-analytics {
  padding: 0;
}

.stats-row {
  margin-bottom: 24px;
}
.stats-row .el-col {
  display: flex;
}
.stats-row .el-col > .stat-card {
  flex: 1;
}

.stat-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: flex-start;
  gap: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  transition: all 0.3s;
  min-height: 112px;
  box-sizing: border-box;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
}

.stat-icon.blue {
  background: #ecf5ff;
  color: #409eff;
}
.stat-icon.green {
  background: #f0f9eb;
  color: #67c23a;
}
.stat-icon.purple {
  background: #f4f0fe;
  color: #a855f7;
}
.stat-icon.orange {
  background: #fdf6ec;
  color: #e6a23c;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  color: #1a1a1a;
  line-height: 1;
  margin-bottom: 4px;
}

.stat-unit {
  font-size: 16px;
  font-weight: 400;
  color: #909399;
  margin-left: 2px;
}

.stat-label {
  font-size: 14px;
  color: #8c8c8c;
  margin-bottom: 6px;
}

.stat-sub {
  font-size: 12px;
  color: #a0a0a0;
}

.chart-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.chart-row .el-col {
  display: flex;
}
.chart-row .el-col > .chart-card {
  flex: 1;
  display: flex;
  flex-direction: column;
}
.chart-card-funnel {
  min-height: 400px;
}

.card-title {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 20px 0;
  flex-shrink: 0;
}

.chart-box {
  width: 100%;
  height: 320px;
}

.funnel-period {
  font-size: 12px;
  color: #909399;
  margin-bottom: 10px;
  flex-shrink: 0;
}
.funnel-scroll {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
}

.funnel-row {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 8px 0;
  border-bottom: 1px solid #f5f5f5;
}
.funnel-row:last-child {
  border-bottom: none;
}
.funnel-head {
  border-bottom: 2px solid #ebeef5;
  padding-bottom: 10px;
  margin-bottom: 2px;
  flex-shrink: 0;
}

.funnel-name-hd {
  flex: 1;
  min-width: 0;
  font-size: 12px;
  font-weight: 600;
  color: #909399;
}
.funnel-name {
  flex: 1;
  min-width: 0;
  font-size: 13px;
  font-weight: 500;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.funnel-col-hd {
  width: 74px;
  text-align: center;
  font-size: 12px;
  font-weight: 600;
  color: #909399;
  flex-shrink: 0;
}
.funnel-col {
  width: 74px;
  display: flex;
  align-items: center;
  gap: 4px;
  flex-shrink: 0;
}

.funnel-bar-track {
  flex: 1;
  height: 8px;
  background: #f0f2f5;
  border-radius: 4px;
  overflow: hidden;
}
.funnel-bar-fill {
  height: 100%;
  border-radius: 4px;
  transition: width 0.3s;
}
.funnel-bar-fill.a {
  background: #409eff;
}
.funnel-bar-fill.b {
  background: #e6a23c;
}
.funnel-bar-fill.c {
  background: #67c23a;
}

.funnel-num {
  font-size: 11px;
  font-weight: 600;
  flex-shrink: 0;
  width: 20px;
  text-align: right;
}
.funnel-num.n-app {
  color: #409eff;
}
.funnel-num.n-int {
  color: #e6a23c;
}
.funnel-num.n-hire {
  color: #67c23a;
}

.funnel-rate-hd {
  width: 86px;
  text-align: center;
  font-size: 12px;
  font-weight: 600;
  color: #909399;
  flex-shrink: 0;
}
.funnel-rate-col {
  width: 86px;
  display: flex;
  align-items: center;
  gap: 4px;
  flex-shrink: 0;
}
.funnel-rate-col .el-progress {
  flex: 1;
}
.funnel-pct {
  font-size: 12px;
  font-weight: 500;
  flex-shrink: 0;
  width: 28px;
  text-align: right;
}
.funnel-pct.y {
  color: #e6a23c;
}
.funnel-pct.g {
  color: #67c23a;
}
</style>