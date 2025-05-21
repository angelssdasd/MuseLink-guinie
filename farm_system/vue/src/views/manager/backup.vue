<template>
  <div>
    <div class="card" style="margin-bottom: 5px;">
      <el-input v-model="data.operatorId" placeholder="操作员ID查询" style="width: 300px; margin-right: 10px" />
      <el-button type="primary" @click="load">查询</el-button>
      <el-button type="info" @click="reset">重置</el-button>
    </div>

    <div class="card">
      <el-table ref="tableRef" :data="data.tableData" stripe @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="backupId" label="ID" />
        <el-table-column prop="backupTime" label="时间" />
        <el-table-column prop="filePath" label="路径" />
        <el-table-column prop="operatorId" label="操作员ID" />
      </el-table>
    </div>

    <div class="card" v-if="data.total">
      <el-pagination
        @current-change="load"
        background
        layout="prev, pager, next"
        v-model:page-size="data.pageSize"
        v-model:current-page="data.pageNum"
        :total="data.total"
      />
    </div>

    <div class="card" v-if="selectedFiles.length">
      <h3>
        已选择的文件
        <el-button type="danger" @click="clearSelectedFiles">清空</el-button>
        <el-button type="primary" @click="restore">恢复</el-button>
      </h3>
      <ul>
        <li v-for="file in selectedFiles" :key="file.backupId">{{ file.filePath }}</li>
      </ul>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import request from '@/utils/request'

const data = reactive({
  tableData: [],
  total: 0,
  pageNum: 1,
  pageSize: 10,
  operatorId: null,
})
const tableRef = ref(null)

const selectedFiles = ref([])

const load = () => {
  request.get('/backup/selectPage', {
    params: {
      operatorId: data.operatorId,
      pageNum: data.pageNum,
      pageSize: data.pageSize
    }
  }).then(res => {
    data.tableData = res.data?.list || []
    data.total = res.data?.total || 0
  })
}

const handleSelectionChange = (selection) => {
  selectedFiles.value = selection.slice(0,2)// 只选择前两个文件
}

const reset = () => {
  data.operatorId = null
  load()
}

const clearSelectedFiles = () => {
  selectedFiles.value = []
  tableRef.value.clearSelection()
}
const restore = () => {
  if (selectedFiles.value.length) {
    const backupIds = selectedFiles.value.map(file => file.backupId).join(',')
    request.post('/backup/restore', backupIds).then(res => {
      if (res.code === '200') {
        ElMessage.success('恢复成功')
        load()
      } else {
        ElMessage.error(res.msg)
      }
    })
  } else {
    ElMessage.warning('请选择要恢复的文件')
  }
}

load()
</script>
