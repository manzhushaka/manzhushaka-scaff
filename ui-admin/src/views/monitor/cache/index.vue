<template>
  <div class="app-container">
    <el-row :gutter="12">
      <el-col :span="24" class="card-box">
        <section class="ui-panel-card cache-panel">
          <div class="cache-panel__header">
            <Monitor class="cache-panel__icon" />
            <span>基本信息</span>
          </div>
          <div class="cache-info-table">
            <table cellspacing="0">
              <tbody>
                <tr>
                  <td class="el-table__cell is-leaf"><div class="cell">Redis版本</div></td>
                  <td class="el-table__cell is-leaf"><div class="cell" v-if="cache.info">{{ cache.info.redis_version }}</div></td>
                  <td class="el-table__cell is-leaf"><div class="cell">运行模式</div></td>
                  <td class="el-table__cell is-leaf"><div class="cell" v-if="cache.info">{{ cache.info.redis_mode == "standalone" ? "单机" : "集群" }}</div></td>
                  <td class="el-table__cell is-leaf"><div class="cell">端口</div></td>
                  <td class="el-table__cell is-leaf"><div class="cell" v-if="cache.info">{{ cache.info.tcp_port }}</div></td>
                  <td class="el-table__cell is-leaf"><div class="cell">客户端数</div></td>
                  <td class="el-table__cell is-leaf"><div class="cell" v-if="cache.info">{{ cache.info.connected_clients }}</div></td>
                </tr>
                <tr>
                  <td class="el-table__cell is-leaf"><div class="cell">运行时间(天)</div></td>
                  <td class="el-table__cell is-leaf"><div class="cell" v-if="cache.info">{{ cache.info.uptime_in_days }}</div></td>
                  <td class="el-table__cell is-leaf"><div class="cell">使用内存</div></td>
                  <td class="el-table__cell is-leaf"><div class="cell" v-if="cache.info">{{ cache.info.used_memory_human }}</div></td>
                  <td class="el-table__cell is-leaf"><div class="cell">使用CPU</div></td>
                  <td class="el-table__cell is-leaf"><div class="cell" v-if="cache.info">{{ parseFloat(cache.info.used_cpu_user_children).toFixed(2) }}</div></td>
                  <td class="el-table__cell is-leaf"><div class="cell">内存配置</div></td>
                  <td class="el-table__cell is-leaf"><div class="cell" v-if="cache.info">{{ cache.info.maxmemory_human }}</div></td>
                </tr>
                <tr>
                  <td class="el-table__cell is-leaf"><div class="cell">AOF是否开启</div></td>
                  <td class="el-table__cell is-leaf"><div class="cell" v-if="cache.info">{{ cache.info.aof_enabled == "0" ? "否" : "是" }}</div></td>
                  <td class="el-table__cell is-leaf"><div class="cell">RDB是否成功</div></td>
                  <td class="el-table__cell is-leaf"><div class="cell" v-if="cache.info">{{ cache.info.rdb_last_bgsave_status }}</div></td>
                  <td class="el-table__cell is-leaf"><div class="cell">Key数量</div></td>
                  <td class="el-table__cell is-leaf"><div class="cell" v-if="cache.dbSize">{{ cache.dbSize }} </div></td>
                  <td class="el-table__cell is-leaf"><div class="cell">网络入口/出口</div></td>
                  <td class="el-table__cell is-leaf"><div class="cell" v-if="cache.info">{{ cache.info.instantaneous_input_kbps }}kps/{{cache.info.instantaneous_output_kbps}}kps</div></td>
                </tr>
              </tbody>
            </table>
          </div>
        </section>
      </el-col>

      <el-col :xs="24" :sm="24" :md="12" class="card-box">
        <section class="ui-panel-card cache-panel">
          <div class="cache-panel__header">
            <PieChart class="cache-panel__icon" />
            <span>命令统计</span>
          </div>
          <div ref="commandstats" class="cache-chart" />
        </section>
      </el-col>

      <el-col :xs="24" :sm="24" :md="12" class="card-box">
        <section class="ui-panel-card cache-panel">
          <div class="cache-panel__header">
            <Odometer class="cache-panel__icon" />
            <span>内存信息</span>
          </div>
          <div ref="usedmemory" class="cache-chart" />
        </section>
      </el-col>
    </el-row>
  </div>
</template>

<script setup name="Cache">
import { getCache } from '@/api/monitor/cache'
import * as echarts from 'echarts'

const cache = ref([])
const commandstats = ref(null)
const usedmemory = ref(null)
const { proxy } = getCurrentInstance()

function getList() {
  proxy.$modal.loading("正在加载缓存监控数据，请稍候！")
  getCache().then(response => {
    proxy.$modal.closeLoading()
    cache.value = response.data

    const commandstatsIntance = echarts.init(commandstats.value, "macarons")
    commandstatsIntance.setOption({
      tooltip: {
        trigger: "item",
        formatter: "{a} <br/>{b} : {c} ({d}%)"
      },
      series: [
        {
          name: "命令",
          type: "pie",
          roseType: "radius",
          radius: [15, 95],
          center: ["50%", "38%"],
          data: response.data.commandStats,
          animationEasing: "cubicInOut",
          animationDuration: 1000
        }
      ]
    })
    const usedmemoryInstance = echarts.init(usedmemory.value, "macarons")
    usedmemoryInstance.setOption({
      tooltip: {
        formatter: "{b} <br/>{a} : " + cache.value.info.used_memory_human
      },
      series: [
        {
          name: "峰值",
          type: "gauge",
          min: 0,
          max: 1000,
          detail: {
            formatter: cache.value.info.used_memory_human
          },
          data: [
            {
              value: parseFloat(cache.value.info.used_memory_human),
              name: "内存消耗"
            }
          ]
        }
      ]
    })
    window.addEventListener("resize", () => {
      commandstatsIntance.resize()
      usedmemoryInstance.resize()
    })
  })
}

getList()
</script>

<style lang="scss" scoped>
.cache-panel {
  height: 100%;
  padding: 0;
  overflow: hidden;
}

.cache-panel__header {
  display: flex;
  align-items: center;
  gap: 8px;
  min-height: 44px;
  padding: 0 16px;
  color: var(--ui-text-primary);
  font-size: 14px;
  font-weight: 700;
  border-bottom: 1px solid var(--ui-border);
  background: var(--ui-bg-panel-muted);
}

.cache-panel__icon {
  width: 16px;
  height: 16px;
  color: var(--ui-primary);
}

.cache-info-table {
  overflow-x: auto;

  table {
    width: 100%;
    min-width: 960px;
    border-collapse: collapse;
  }

  td {
    height: 42px;
    border-bottom: 1px solid var(--ui-table-border);
    color: var(--ui-text-regular);
  }

  tr:last-child td {
    border-bottom: 0;
  }

  td:nth-child(odd) {
    width: 112px;
    background: var(--ui-bg-panel-muted);
    color: var(--ui-text-secondary);
    font-weight: 600;
  }
}

.cache-chart {
  height: 420px;
  min-height: 320px;
}
</style>
