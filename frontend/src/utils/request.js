import axios from 'axios';
import { ElMessage } from 'element-plus';
import router from '@/router/index.js';
import { clearChatCache } from '@/services/chatCache.js';

// 创建 axios 实例
const request = axios.create({
  baseURL: '/api', // API 的 base_url，根据实际情况修改
  timeout: 600000 // 请求超时时间
});

// 请求拦截器
 request.interceptors.request.use(
  config => {
    // 在这里可以统一添加 token 等请求头
    const loginUser = JSON.parse(localStorage.getItem('loginUser'));
    if (loginUser && loginUser.token) {
      config.headers.token = loginUser.token;
    }
    return config;
  },
  error => {
    console.error('Request Error:', error);
    return Promise.reject(error);
  }
); 

// 响应拦截器
request.interceptors.response.use(
  response => {
    // 二进制流（blob）下载不作 JSON 解析，直接透传完整响应
    if (response.config.responseType === 'blob') {
      return response;
    }
    const res = response.data;
    if (res.code && res.code !== 1) {
      ElMessage.error(res.message || 'Error');
      return Promise.reject(new Error(res.message || 'Error'));
    }
    return res;
  },
  error => {
    if (error.response) {
      const { status } = error.response;
      if (status === 401) {
        ElMessage.error('登录已过期，请重新登录');
        localStorage.removeItem('loginUser');
        clearChatCache();
        router.replace('/login');
        return Promise.reject(error);
      }
      if (status === 403) {
        ElMessage.error('没有操作权限');
        return Promise.reject(error);
      }
    }
    ElMessage.error(error.message || '网络错误');
    return Promise.reject(error);
  }
);

export default request;
