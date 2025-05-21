<template>
  <div>
    <div class="card" style="margin-bottom: 5px;">
      <el-input v-model="data.operatorId" placeholder="操作员ID查询" style="width: 300px; margin-right: 10px" />
      <el-button type="primary" @click="load">查询</el-button>
      <el-button type="info" @click="reset">重置</el-button>
      <el-button type="success" @click="openBackupDialog">开始备份</el-button>
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
        <el-button type="danger" @click="deleteSelectFiles">删除文件</el-button>
        <el-button type="primary" @click="restore">恢复</el-button>
      </h3>
      <ul>
        <li v-for="file in selectedFiles" :key="file.backupId">{{ file.filePath }}</li>
      </ul>
    </div>
  </div>
    <el-dialog v-model="dialogVisible" title="选择备份类型">
    <el-radio-group v-model="backupType">
      <el-radio label="full">全量备份</el-radio>
      <el-radio label="diff">差异备份</el-radio>
    </el-radio-group>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmBackup">确认</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import { reactive, ref } from 'vue'
import request from '@/utils/request'
import {ElMessage, ElMessageBox} from "element-plus";

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

const user = JSON.parse(localStorage.getItem('system-user'))
const userId = user.userId



const dialogVisible = ref(false)
const backupType = ref('full') // 默认选中全量备份

const openBackupDialog = () => {
  dialogVisible.value = true
}

const confirmBackup = () => {
  const type = backupType.value
  dialogVisible.value = false

  request.post('/backup/start', { type, userId }).then(res => {
    if (res.code === '200') {
      // 添加 duration: 2000 表示 2 秒后自动关闭
      ElMessage({
        message: `${type === 'full' ? '全量' : '差异'}备份启动成功`,
        type: 'success',
        duration: 2000 // 设置自动关闭时间（单位：毫秒）
      })

      load()
    } else {
      ElMessage.error(res.msg)
    }
  })
}



const handleSelectionChange = (selection) => {
  selectedFiles.value = selection
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
  if (selectedFiles.value.length<=2&&selectedFiles.value.length) {
    const backupIds = selectedFiles.value.map(file => file.backupId).join(',')
    request.post('/backup/restore', backupIds).then(res => {
      if (res.code === '200') {
        ElMessage.success('恢复成功')
        load()
      } else {
        ElMessage.error(res.msg)
      }
    })
  } else if(selectedFiles.value.length>2)
  {
    ElMessage.warning('恢复数据库至多要两个文件')
  }
  else {
    ElMessage.warning('请选择要恢复的文件')
  }
}


const deleteSelectFiles = () => {
  if (selectedFiles.value.length) {
    ElMessageBox.confirm(
      '确定要删除所选的文件吗？此操作不可恢复。',
      '警告',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning'
      }
    ).then(() => {
      const backupIds = selectedFiles.value.map(file => file.backupId).join(',')
      request.delete('/backup/delete', { params: { backupIds } }).then(res => {
        if (res.code === '200') {
          ElMessage.success('删除成功')
          load()
        } else {
          ElMessage.error(res.msg)
        }
      })
    }).catch(() => {
      ElMessage.info('已取消删除')
      // 用户点击取消时不做任何操作
    })
  } else {
    ElMessage.warning('请选择要删除的文件')
  }
}


load()
</script>
