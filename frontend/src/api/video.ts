import request from '@/utils/request'

export interface VideoListItem {
  id: number
  title: string
  description: string
  tags: string[]
  coverUrl?: string
  fileSize: number
  duration: number
  status: string
  auditStatus?: string
  views?: number
  likes?: number
  comments?: number
  createTime: string
  updateTime: string
  userName?: string
}
// 公开视频列表（首页使用）
export function getPublicVideoList(params: any) {
  return request({
    url: '/videos/public',
    method: 'get',
    params
  })
}
// 公开视频详情（播放页使用）
export function getPublicVideo(id: number) {
  return request({
    url: `/videos/public/${id}`,
    method: 'get'
  })
}

export interface PageResp<T> {
  total: number
  list: T[]
}

export interface VideoDetail {
  id: number
  title: string
  description: string
  tags: string[]
  filePath: string
  coverUrl?: string
  fileSize: number
  duration: number
  status: string
  auditStatus?: string
  views?: number
  likes?: number
  createTime: string
  updateTime: string
  deleted: boolean
  version: number
  userName?: string
}

export interface UploadResp {
  videoId: number
  title: string
  uploadTime: string
}

export function uploadVideo(form: FormData) {
  return request({
    url: '/videos/upload',
    method: 'post',
    data: form,
    timeout: 300000
  })
}

export function uploadVideoWithProgress(form: FormData, onProgress?: (percent: number) => void) {
  return request({
    url: '/videos/upload',
    method: 'post',
    data: form,
    timeout: 300000,
    onUploadProgress: (evt: any) => {
      const total = evt?.total || 0
      const loaded = evt?.loaded || 0
      if (total > 0) {
        const p = Math.max(0, Math.min(100, Math.round((loaded * 100) / total)))
        onProgress?.(p)
      }
    }
  })
}

export function pageVideos(params: any) {
  return request({
    url: '/videos',
    method: 'get',
    params
  })
}

// 获取视频列表（首页专用）
export function getVideoList(params: any) {
  return request({
    url: '/videos',
    method: 'get',
    params
  })
}

export function getVideo(id: number, params?: any) {
  return request({
    url: `/videos/detail`,
    method: 'get',
    params: { id, ...params }
  })
}

export function updateVideo(id: number, data: any) {
  return request({
    url: `/videos/${id}`,
    method: 'put',
    data
  })
}

export function deleteVideo(id: number) {
  return request({
    url: `/videos/${id}`,
    method: 'delete'
  })
}

export function incrementVideoViews(id: number) {
  return request({
    url: `/videos/${id}/view`,
    method: 'post'
  })
}

export function incrementVideoLikes(id: number) {
  return request({
    url: `/videos/like`,
    method: 'post',
    params: { id }
  })
}

// 获取相关推荐视频
export function getRelatedVideos(id: number, limit: number = 10) {
  return request({
    url: `/videos/${id}/related`,
    method: 'get',
    params: { limit }
  })
}
