<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { teacherApi } from '../../utils/api'
import { toast } from '../../utils/ui-feedback'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { PieChart, BarChart } from 'echarts/charts'
import { TitleComponent, TooltipComponent, LegendComponent, GridComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'

use([PieChart, BarChart, TitleComponent, TooltipComponent, LegendComponent, GridComponent, CanvasRenderer])

const loading = ref(true)
const requests = ref<any[]>([])
const projects = ref<any[]>([])

function normalizeStatus(status: string | null | undefined): string {
  return String(status || 'UNKNOWN').toUpperCase()
}

// --- KPIs ---
const pendingCount = computed(() => requests.value.filter(r => normalizeStatus(r.requestStatus) === 'PENDING').length)
const acceptedCount = computed(() => requests.value.filter(r => normalizeStatus(r.requestStatus) === 'ACCEPTED').length)
const rejectedCount = computed(() => requests.value.filter(r => normalizeStatus(r.requestStatus) === 'REJECTED').length)
const withdrawnCount = computed(() => requests.value.filter(r => normalizeStatus(r.requestStatus) === 'WITHDRAWN').length)
const totalProjects = computed(() => projects.value.length)
const totalCapacity = computed(() => projects.value.reduce((s, p) => s + (p.maxStudents || 0), 0))
const filledSlots = computed(() => projects.value.reduce((s, p) => s + (p.currentAgreedCount || 0), 0))

// --- Charts ---

const requestStatusChart = computed(() => ({
  tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
  legend: { bottom: 0, textStyle: { color: '#666' } },
  series: [{
    type: 'pie',
    radius: ['40%', '70%'],
    avoidLabelOverlap: false,
    itemStyle: { borderRadius: 8, borderColor: '#fff', borderWidth: 2 },
    label: { show: false },
    emphasis: { label: { show: true, fontSize: 14, fontWeight: 'bold' } },
    data: [
      { value: pendingCount.value, name: 'Pending', itemStyle: { color: '#f6a63d' } },
      { value: acceptedCount.value, name: 'Accepted', itemStyle: { color: '#2fc5a8' } },
      { value: rejectedCount.value, name: 'Rejected', itemStyle: { color: '#c54545' } },
      { value: withdrawnCount.value, name: 'Withdrawn', itemStyle: { color: '#9c9cb2' } },
    ].filter(d => d.value > 0),
  }],
}))

const projectStatusChart = computed(() => {
  const counts: Record<string, number> = {}
  projects.value.forEach(p => {
    const s = normalizeStatus(p.projectStatus)
    counts[s] = (counts[s] || 0) + 1
  })
  const colorMap: Record<string, string> = { AVAILABLE: '#2fc5a8', REQUESTED: '#f6a63d', AGREED: '#24b3ff', CLOSED: '#9c9cb2', ARCHIVED: '#9c9cb2' }
  return {
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { bottom: 0, textStyle: { color: '#666' } },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      avoidLabelOverlap: false,
      itemStyle: { borderRadius: 8, borderColor: '#fff', borderWidth: 2 },
      label: { show: false },
      emphasis: { label: { show: true, fontSize: 14, fontWeight: 'bold' } },
      data: Object.entries(counts).map(([name, value]) => ({
        value, name, itemStyle: { color: colorMap[name] || '#7c5cfc' },
      })),
    }],
  }
})

const requestsPerProjectChart = computed(() => {
  const counts: Record<string, number> = {}
  requests.value.forEach(r => {
    const title = r.projectTitle || 'Unknown'
    counts[title] = (counts[title] || 0) + 1
  })
  const sorted = Object.entries(counts).sort((a, b) => b[1] - a[1]).slice(0, 10)
  const names = sorted.map(e => e[0])
  const values = sorted.map(e => e[1])
  return {
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'value', minInterval: 1, axisLabel: { color: '#666' } },
    yAxis: { type: 'category', data: names, axisLabel: { color: '#666', width: 120, overflow: 'truncate' } },
    series: [{
      type: 'bar',
      data: values,
      barMaxWidth: 28,
      itemStyle: { color: '#24b3ff', borderRadius: [0, 6, 6, 0] },
    }],
  }
})

const fillRateChart = computed(() => {
  const data = projects.value.map(p => {
    const max = p.maxStudents || 1
    const current = p.currentAgreedCount || 0
    return { name: p.title || 'Untitled', rate: Math.round((current / max) * 100), current, max }
  }).sort((a, b) => b.rate - a.rate).slice(0, 10)
  return {
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' }, formatter: (params: any) => { const d = params[0]; const item = data[d.dataIndex]; return `${item.name}<br/>Fill Rate: ${item.rate}% (${item.current}/${item.max})` } },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'value', max: 100, axisLabel: { formatter: '{value}%', color: '#666' } },
    yAxis: { type: 'category', data: data.map(d => d.name), axisLabel: { color: '#666', width: 120, overflow: 'truncate' } },
    series: [{
      type: 'bar',
      data: data.map(d => d.rate),
      barMaxWidth: 28,
      itemStyle: { color: '#7c5cfc', borderRadius: [0, 6, 6, 0] },
    }],
  }
})

const programmeChart = computed(() => {
  const counts: Record<string, number> = {}
  requests.value.forEach(r => {
    const prog = r.studentProgramme || 'Unknown'
    counts[prog] = (counts[prog] || 0) + 1
  })
  const sorted = Object.entries(counts).sort((a, b) => b[1] - a[1])
  const names = sorted.map(e => e[0])
  const values = sorted.map(e => e[1])
  return {
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: names, axisLabel: { rotate: names.length > 3 ? 30 : 0, color: '#666' } },
    yAxis: { type: 'value', minInterval: 1, axisLabel: { color: '#666' } },
    series: [{
      type: 'bar',
      data: values,
      barMaxWidth: 40,
      itemStyle: { color: '#f6a63d', borderRadius: [6, 6, 0, 0] },
    }],
  }
})

const preferenceRankChart = computed(() => {
  const counts: Record<number, number> = {}
  requests.value.forEach(r => {
    const rank = r.preferenceRank || 0
    counts[rank] = (counts[rank] || 0) + 1
  })
  const ranks = Object.keys(counts).map(Number).sort((a, b) => a - b)
  return {
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: ranks.map(r => `Rank ${r}`), axisLabel: { color: '#666' } },
    yAxis: { type: 'value', minInterval: 1, axisLabel: { color: '#666' } },
    series: [{
      type: 'bar',
      data: ranks.map(r => counts[r]),
      barMaxWidth: 40,
      itemStyle: { color: '#2fc5a8', borderRadius: [6, 6, 0, 0] },
    }],
  }
})

async function loadData() {
  loading.value = true
  try {
    const [reqRes, projRes] = await Promise.all([
      teacherApi.listRequests(),
      teacherApi.listProjects(),
    ])
    requests.value = Array.isArray(reqRes.data) ? reqRes.data : []
    projects.value = Array.isArray(projRes.data) ? projRes.data : []
  } catch (e: any) {
    toast.error(e.message || 'Failed to load data')
  } finally {
    loading.value = false
  }
}

onMounted(loadData)
</script>

<template>
  <div class="analytics-page">
    <header class="page-header">
      <h1>Analytics</h1>
    </header>

    <!-- KPI cards -->
    <section class="kpi-grid">
      <div class="kpi-card">
        <span class="kpi-label">Total Projects</span>
        <strong class="kpi-value">{{ loading ? '...' : totalProjects }}</strong>
      </div>
      <div class="kpi-card">
        <span class="kpi-label">Total Requests</span>
        <strong class="kpi-value">{{ loading ? '...' : requests.length }}</strong>
      </div>
      <div class="kpi-card">
        <span class="kpi-label">Pending Review</span>
        <strong class="kpi-value kpi-orange">{{ loading ? '...' : pendingCount }}</strong>
      </div>
      <div class="kpi-card">
        <span class="kpi-label">Slots Filled</span>
        <strong class="kpi-value kpi-green">{{ loading ? '...' : `${filledSlots}/${totalCapacity}` }}</strong>
      </div>
    </section>

    <!-- Charts -->
    <section class="charts">
      <h2>Data Overview</h2>
      <div class="chart-grid">
        <div class="chart-panel">
          <h3>Request Status</h3>
          <div v-if="loading" class="chart-placeholder">Loading...</div>
          <VChart v-else class="chart" :option="requestStatusChart" autoresize />
        </div>
        <div class="chart-panel">
          <h3>Project Status</h3>
          <div v-if="loading" class="chart-placeholder">Loading...</div>
          <VChart v-else class="chart" :option="projectStatusChart" autoresize />
        </div>
        <div class="chart-panel chart-wide">
          <h3>Requests per Project (Top 10)</h3>
          <div v-if="loading" class="chart-placeholder">Loading...</div>
          <VChart v-else class="chart" :option="requestsPerProjectChart" autoresize />
        </div>
        <div class="chart-panel chart-wide">
          <h3>Project Fill Rate (Top 10)</h3>
          <div v-if="loading" class="chart-placeholder">Loading...</div>
          <VChart v-else class="chart" :option="fillRateChart" autoresize />
        </div>
        <div class="chart-panel">
          <h3>Requests by Programme</h3>
          <div v-if="loading" class="chart-placeholder">Loading...</div>
          <VChart v-else class="chart" :option="programmeChart" autoresize />
        </div>
        <div class="chart-panel">
          <h3>Preference Rank Distribution</h3>
          <div v-if="loading" class="chart-placeholder">Loading...</div>
          <VChart v-else class="chart" :option="preferenceRankChart" autoresize />
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
.analytics-page {
  display: flex;
  flex-direction: column;
  gap: 28px;
}

.page-header h1 {
  margin: 0;
  font-size: 1.8rem;
  font-weight: 600;
  color: var(--text);
}

h2 { margin: 0 0 16px; font-size: 1.4rem; color: var(--text); }

/* KPI cards */
.kpi-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
  gap: 16px;
}

.kpi-card {
  background: #fff;
  border-radius: 18px;
  padding: 20px 24px;
  border: 1px solid rgba(90, 43, 152, 0.16);
  box-shadow: 0 8px 24px rgba(90, 43, 152, 0.05);
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.kpi-label {
  font-size: 0.85rem;
  color: var(--muted);
  text-transform: uppercase;
  letter-spacing: 0.06em;
  font-weight: 600;
}

.kpi-value {
  font-size: 2rem;
  color: var(--text);
}

.kpi-orange { color: var(--orange); }
.kpi-green { color: var(--green); }

/* Charts */
.charts h2 { margin: 0 0 16px; }

.chart-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.chart-panel {
  background: #fff;
  border-radius: 28px;
  padding: 24px 28px;
  border: 1px solid rgba(90, 43, 152, 0.16);
  box-shadow: 0 12px 30px rgba(90, 43, 152, 0.05);
  min-height: 320px;
  display: flex;
  flex-direction: column;
}

.chart-panel.chart-wide {
  grid-column: span 2;
  min-height: 380px;
}

.chart-panel h3 {
  margin: 0 0 12px;
  font-size: 1rem;
  color: var(--muted);
  font-weight: 600;
}

.chart {
  flex: 1;
  min-height: 260px;
}

.chart-placeholder {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--muted);
  font-size: 0.95rem;
}

@media (max-width: 960px) {
  .chart-grid {
    grid-template-columns: 1fr;
  }
  .chart-panel.chart-wide {
    grid-column: span 1;
  }
}
</style>
