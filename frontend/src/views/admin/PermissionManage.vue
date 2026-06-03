<template>
  <div class="permission-manage">
    <el-row :gutter="20">
      <el-col :span="10">
        <el-card class="role-card">
          <template #header>
            <div class="card-header">
              <span>角色列表</span>
              <el-button type="primary" size="small" :icon="Plus">新增角色</el-button>
            </div>
          </template>
          <el-table :data="roleList" highlight-current-row @current-change="handleRoleChange" stripe>
            <el-table-column prop="name" label="角色名称" />
            <el-table-column prop="description" label="描述" />
            <el-table-column prop="userCount" label="用户数" width="80" />
            <el-table-column label="操作" width="120">
              <template #default="{ row }">
                <el-button link type="primary" :icon="Edit" @click="handleEditRole(row)">编辑</el-button>
                <el-button link type="danger" :icon="Delete" @click="handleDeleteRole(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="14">
        <el-card class="perm-card">
          <template #header>
            <div class="card-header">
              <span>权限分配 - {{ currentRole ? currentRole.name : '请选择角色' }}</span>
              <el-button type="primary" size="small" @click="savePermissions" :disabled="!currentRole">保存权限</el-button>
            </div>
          </template>
          <el-tree
            :data="permTree"
            show-checkbox
            node-key="id"
            default-expand-all
            :default-checked-keys="checkedKeys"
            ref="treeRef"
          />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, nextTick } from "vue";
import { Plus, Edit, Delete } from "@element-plus/icons-vue";
import { ElMessage } from "element-plus";

const treeRef = ref(null);
const currentRole = ref(null);
const checkedKeys = ref([]);

const roleList = ref([
  { id: 1, name: "超级管理员", description: "拥有系统全部权限", userCount: 2 },
  { id: 2, name: "普通管理员", description: "拥有部分管理权限", userCount: 5 },
  { id: 3, name: "HR经理", description: "HR管理权限", userCount: 12 },
  { id: 4, name: "HR专员", description: "基础HR操作权限", userCount: 28 },
]);

const permTree = ref([
  {
    id: 1,
    label: "数据概览",
    children: [
      { id: 11, label: "查看数据面板" },
      { id: 12, label: "导出数据报表" },
    ],
  },
  {
    id: 2,
    label: "用户管理",
    children: [
      { id: 21, label: "查看用户列表" },
      { id: 22, label: "新增用户" },
      { id: 23, label: "编辑用户" },
      { id: 24, label: "删除用户" },
    ],
  },
  {
    id: 3,
    label: "HR管理",
    children: [
      { id: 31, label: "查看HR列表" },
      { id: 32, label: "新增HR" },
      { id: 33, label: "编辑HR" },
      { id: 34, label: "禁用/启用HR" },
    ],
  },
  {
    id: 4,
    label: "企业/公司管理",
    children: [
      { id: 41, label: "查看企业列表" },
      { id: 42, label: "新增企业" },
      { id: 43, label: "编辑企业" },
      { id: 44, label: "企业认证" },
    ],
  },
  {
    id: 5,
    label: "职位管理",
    children: [
      { id: 51, label: "查看职位列表" },
      { id: 52, label: "审核职位" },
      { id: 53, label: "删除职位" },
    ],
  },
  {
    id: 6,
    label: "系统设置",
    children: [
      { id: 61, label: "基本设置" },
      { id: 62, label: "安全设置" },
      { id: 63, label: "邮件设置" },
    ],
  },
]);

const rolePermMap = {
  1: [11, 12, 21, 22, 23, 24, 31, 32, 33, 34, 41, 42, 43, 44, 51, 52, 53, 61, 62, 63],
  2: [11, 21, 31, 41, 51],
  3: [11, 21, 31, 32, 33, 41, 43, 51, 52],
  4: [11, 21, 31, 41, 51],
};

const handleRoleChange = (row) => {
  currentRole.value = row;
  checkedKeys.value = rolePermMap[row.id] || [];
};

const handleEditRole = (row) => console.log("编辑角色:", row);
const handleDeleteRole = (row) => console.log("删除角色:", row);

const savePermissions = () => {
  const keys = treeRef.value.getCheckedKeys();
  const halfKeys = treeRef.value.getHalfCheckedKeys();
  console.log("保存权限:", [...keys, ...halfKeys]);
  ElMessage.success("权限保存成功");
};
</script>

<style scoped>
.role-card, .perm-card { border-radius: 8px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>