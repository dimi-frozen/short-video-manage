import request from '@/utils/request'

export interface PendingVideoItem {
  id: number
  title: string
  description: string
  tags: string
  fileUrl: string
  coverUrl: string
  fileSize: number
  transcodeStatus: string
  auditStatus: string
  createTime: string
}

export function adminPendingVideos(limit = 100) {
  return request({
    url: '/admin/video/pending',
    method: 'get',
    params: { limit }
  })
}

export function adminApproveVideo(id: number) {
  return request({
    url: '/admin/video/approve',
    method: 'post',
    params: { id }
  })
}

export function adminRejectVideo(id: number) {
  return request({
    url: '/admin/video/reject',
    method: 'post',
    params: { id }
  })
}

