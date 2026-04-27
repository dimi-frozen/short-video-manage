import axios from 'axios'
import { ElMessage } from 'element-plus'

const service = axios.create({
  baseURL: '/api',
  timeout: 60000
})

// 请求拦截器
service.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['Authorization'] = 'Bearer ' + token
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    if (response.status === 204) {
      return { code: 0, data: null }
    }
    const res = response.data
    if (res.code !== 0) {
      ElMessage.error(res.message || 'Error')
      return Promise.reject(new Error(res.message || 'Error'))
    } else {
      return res
    }
  },
  error => {
    const data = error?.response?.data
    const msg =
      data?.message ||
      data?.error ||
      error?.message ||
      'Network Error'
    ElMessage.error(msg)
    return Promise.reject(new Error(msg))
  }
)

export default service
