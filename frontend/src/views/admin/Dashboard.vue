<template>
  <div class="dashboard">
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content" v-loading="loading.overview">
            <div class="stat-info">
              <div class="stat-label">求职者数</div>
              <div class="stat-value">{{ overview.jobSeekerCount ?? '--' }}</div>
            </div>
            <div class="stat-icon blue">
              <el-icon :size="36"><User /></el-icon>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content" v-loading="loading.overview">
            <div class="stat-info">
              <div class="stat-label">已注册公司</div>
              <div class="stat-value">{{ overview.companyCount ?? '--' }}</div>
            </div>
            <div class="stat-icon green">
              <el-icon :size="36"><OfficeBuilding /></el-icon>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content" v-loading="loading.overview">
            <div class="stat-info">
              <div class="stat-label">在招职位</div>
              <div class="stat-value">{{ overview.activeJobCount ?? '--' }}</div>
            </div>
            <div class="stat-icon orange">
              <el-icon :size="36"><Briefcase /></el-icon>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content" v-loading="loading.overview">
            <div class="stat-info">
              <div class="stat-label">累计投递</div>
              <div class="stat-value">{{ overview.totalApplicationCount ?? '--' }}</div>
            </div>
            <div class="stat-icon purple">
              <el-icon :size="36"><Ticket /></el-icon>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="chart-row">
      <el-col :span="16">
        <el-card class="chart-card" v-loading="loading.trends">
          <template #header>
            <div class="chart-header">
              <span>近{{ trendDays }}日数据趋势</span>
              <el-segmented v-model="trendDays" :options="[7, 14, 30]" size="small" @change="fetchTrends" />
            </div>
          </template>

          <div v-if="trends.length" class="bar-chart-area">
            <div class="y-axis">
              <span v-for="n in 5" :key="n" class="y-tick">{{ maxTrendVal - Math.round((maxTrendVal * (n-1)) / 4) }}</span>
            </div>
            <div class="bars-container">
              <div v-for="item in trends" :key="item.date" class="bar-col">
                <div class="col-bars">
                  <div class="col-bar-wrap">
                    <span v-if="item.newUsers > 0" class="bar-tip user-num">{{ item.newUsers }}</span>
                    <div
                      class="col-bar bar-user"
                      :style="{ height: barPx(item.newUsers) + 'px' }"
                    ></div>
                  </div>
                  <div class="col-bar-wrap">
                    <span v-if="item.newApplications > 0" class="bar-tip app-num">{{ item.newApplications }}</span>
                    <div
                      class="col-bar bar-app"
                      :style="{ height: barPx(item.newApplications) + 'px' }"
                    ></div>
                  </div>
                </div>
                <span class="col-label">{{ fmtDate(item.date) }}</span>
              </div>
            </div>
          </div>
          <div v-else class="chart-empty">
            <el-icon :size="48" color="#d4d4d8"><TrendCharts /></el-icon>
            <p>暂无趋势数据</p>
          </div>
          <div class="chart-legend">
            <span class="legend-item"><i class="leg-dot" style="background:#059669"></i> 新用户</span>
            <span class="legend-item"><i class="leg-dot" style="background:#1677ff"></i> 投递</span>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="chart-card" v-loading="loading.overview">
          <template #header><span>用户角色分布</span></template>

          <div v-if="overview.roleDistribution && overview.roleDistribution.length" class="pie-section">
            <div class="pie-ring" :style="pieStyle"></div>
            <div class="pie-legend">
              <div v-for="item in overview.roleDistribution" :key="item.role" class="pleg-item">
                <i class="pleg-dot" :style="{ background: roleColor(item.role) }"></i>
                <span class="pleg-name">{{ roleLabel(item.role) }}</span>
                <span class="pleg-count">{{ item.count }}</span>
                <span class="pleg-pct">{{ item.percent }}%</span>
              </div>
            </div>
          </div>
          <div v-else class="chart-empty">
            <el-icon :size="48" color="#d4d4d8"><PieChart /></el-icon>
            <p>暂无分布数据</p>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="chart-row">
      <el-col :span="24">
        <el-card class="table-card" v-loading="loading.overview">
          <template #header><span>职位投递排行 TOP10</span></template>
          <el-table :data="overview.jobAppRanking || []" stripe empty-text="暂无投递数据" size="small">
            <el-table-column type="index" label="#" width="50" />
            <el-table-column prop="jobTitle" label="职位名称" min-width="180" />
            <el-table-column prop="companyName" label="所属公司" min-width="160" />
            <el-table-column prop="applyCount" label="投递量" width="100" align="center" sortable />
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <el-col :span="24">
        <el-card class="table-card" v-loading="loading.recentUsers">
          <template #header><span>最新注册用户</span></template>
          <el-table :data="recentUsers" stripe empty-text="暂无注册用户">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="username" label="用户名" width="120" />
            <el-table-column prop="nickname" label="昵称" min-width="160" />
            <el-table-column prop="phone" label="手机号" width="140" />
            <el-table-column prop="role" label="角色" width="100">
              <template #default="{ row }">
                <el-tag
                  :type="row.role === 0 ? 'success' : row.role === 1 ? 'warning' : 'danger'"
                  size="small"
                >{{ row.roleName || roleMap[row.role] }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" label="注册时间" />
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { User, OfficeBuilding, Briefcase, Ticket, TrendCharts, PieChart } from '@element-plus/icons-vue'
import { getOverview, getTrends, getRecentUsers } from '@/api/admin/dashboard'

const roleMap = { 0: '求职者', 1: 'HR', 2: '管理员' }

const loading = ref({ overview: false, trends: false, recentUsers: false })
const overview = ref({})
const trends = ref([])
const recentUsers = ref([])
const trendDays = ref(7)

const BAR_MAX_H = 180

const maxTrendVal = computed(() => {
  if (!trends.value.length) return 5
  let m = 1
  trends.value.forEach(t => {
    m = Math.max(m, t.newUsers || 0, t.newApplications || 0)
  })
  return m || 1
})

const barPx = (val) => val ? Math.max(6, (val / maxTrendVal.value) * BAR_MAX_H) : 0

const roleColor = (role) => ({ 0: '#059669', 1: '#1677ff', 2: '#f5222d' }[role] || '#94a3b8')
const roleLabel = (r) => roleMap[r] || '未知'

const pieStyle = computed(() => {
  const list = overview.value.roleDistribution
  if (!list || !list.length) return {}
  const total = list.reduce((s, i) => s + (i.count || 0), 0)
  if (!total) return {}

  let acc = 0
  const stops = []
  list.forEach(item => {
    const pct = (item.count / total) * 100
    stops.push(`${roleColor(item.role)} ${acc}% ${acc + pct}%`)
    acc += pct
  })

  return { background: `conic-gradient(${stops.join(',')})` }
})

const fmtDate = (d) => {
  if (!d) return ''
  const p = d.split('-')
  return p.length === 3 ? `${p[1]}/${p[2]}` : d
}

const fetchOverview = async () => {
  loading.value.overview = true
  try {
    const res = await getOverview()
    if (res.code === 1) overview.value = res.data
  } catch { /* 静默 */ }
  finally { loading.value.overview = false }
}

const fetchTrends = async () => {
  loading.value.trends = true
  try {
    const res = await getTrends(trendDays.value)
    if (res.code === 1) trends.value = res.data.trends || []
  } catch { trends.value = [] }
  finally { loading.value.trends = false }
}

const fetchRecentUsers = async () => {
  loading.value.recentUsers = true
  try {
    const res = await getRecentUsers(10)
    if (res.code === 1) recentUsers.value = Array.isArray(res.data) ? res.data : (res.data.list || [])
  } catch { recentUsers.value = [] }
  finally { loading.value.recentUsers = false }
}

onMounted(() => {
  fetchOverview()
  fetchTrends()
  fetchRecentUsers()
})
</script>

<style scoped>
.stats-row { margin-bottom: 20px; }
.stat-card { border-radius: 12px; border: 1px solid #edf2f7; }
.stat-content { display: flex; justify-content: space-between; align-items: center; min-height: 80px; }
.stat-label { font-size: 13px; color: #94a3b8; margin-bottom: 6px; font-weight: 500; letter-spacing: 0.5px; }
.stat-value {
  font-size: 30px; font-weight: 700; color: #0f172a;
  font-variant-numeric: tabular-nums;
}
.stat-icon {
  width: 60px; height: 60px; border-radius: 14px;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}
.stat-icon.blue   { background: #eff6ff; color: #3b82f6; }
.stat-icon.green  { background: #ecfdf5; color: #10b981; }
.stat-icon.orange { background: #fff7ed; color: #f97316; }
.stat-icon.purple { background: #f5f3ff; color: #8b5cf6; }
.stat-icon.cyan   { background: #ecfeff; color: #06b6d4; }

.chart-row { margin-bottom: 20px; }
.chart-card { border-radius: 12px; border: 1px solid #edf2f7; }

.chart-header {
  display: flex; justify-content: space-between; align-items: center;
}

/* ====== 柱状图：固定高度 + 绝对定位 ====== */
.bar-chart-area {
  display: flex; gap: 0; padding: 20px 0 0;
}
.y-axis {
  display: flex; flex-direction: column; justify-content: space-between;
  padding-right: 8px; height: 220px; flex-shrink: 0;
}
.y-tick {
  font-size: 11px; color: #a1a1aa; line-height: 1;
  transform: translateY(50%);
}
.bars-container {
  display: flex; align-items: flex-end; gap: 4px;
  flex: 1; height: 220px; border-bottom: 1px solid #e4e4e7;
  padding-bottom: 24px;
  position: relative;
}
.bars-container::before {
  content: '';
  position: absolute; left: 0; top: 0; bottom: 26px;
  width: 1px; background: #e4e4e7;
}

.bar-col {
  display: flex; flex-direction: column; align-items: center;
  flex: 1; min-width: 0; position: relative;
}

.col-bars {
  display: flex; gap: 3px; align-items: flex-end;
  height: 220px; width: 100%; justify-content: center;
}
.col-bar-wrap {
  position: relative; display: flex; flex-direction: column; align-items: center;
}
.bar-tip {
  position: absolute; top: -18px; left: 50%; transform: translateX(-50%);
  font-size: 11px; font-weight: 700; line-height: 1; white-space: nowrap; z-index: 1;
}
.user-num { color: #059669; }
.app-num  { color: #1677ff; }

.col-bar {
  width: 14px; border-radius: 3px 3px 0 0; transition: height .35s ease;
  min-height: 0;
}
.bar-user { background: linear-gradient(180deg, #a7f3d0, #6ee7b7); }
.bar-app  { background: linear-gradient(180deg, #93c5fd, #60a5fa); }

.col-label {
  position: absolute; bottom: -22px; left: 50%;
  transform: translateX(-50%);
  font-size: 11px; color: #a1a1aa; white-space: nowrap;
}

.chart-legend {
  display: flex; justify-content: center; gap: 20px; margin-top: 28px;
}
.legend-item { display: flex; align-items: center; gap: 6px; font-size: 12px; color: #64748b; }
.leg-dot { display: inline-block; width: 10px; height: 10px; border-radius: 3px; }

/* ====== 环形饼图 ====== */
.pie-section {
  display: flex; flex-direction: column; align-items: center; gap: 20px; padding: 16px 0;
}
.pie-ring {
  width: 160px; height: 160px; border-radius: 50%;
  position: relative;
}
.pie-ring::after {
  content: '';
  position: absolute; inset: 45px;
  background: #fff; border-radius: 50%;
}

.pie-legend {
  width: 100%;
}
.pleg-item {
  display: flex; align-items: center; gap: 10px;
  padding: 8px 12px; border-radius: 10px; transition: background .15s;
}
.pleg-item:hover { background: #f8fafc; }
.pleg-dot {
  width: 12px; height: 12px; border-radius: 4px; flex-shrink: 0;
}
.pleg-name { flex: 1; font-size: 14px; color: #475569; }
.pleg-count {
  font-size: 15px; font-weight: 700; color: #0f172a;
  min-width: 32px; text-align: right;
}
.pleg-pct {
  font-size: 12px; color: #94a3b8;
  min-width: 40px; text-align: right;
}

/* ====== 空态 ====== */
.chart-empty {
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  padding: 44px 0; color: #94a3b8; gap: 8px;
}
.chart-empty p { margin: 0; font-size: 13px; }

.table-card { border-radius: 12px; border: 1px solid #edf2f7; }
</style>