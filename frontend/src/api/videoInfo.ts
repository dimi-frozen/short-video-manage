import request from '@/utils/request'

export interface VideoInfoItem {
  videoId: string
  title: string
  publishTime: string
  platformType: string
  views: number
  likes: number
  comments: number
  shares: number
  status: string
  createdAt: string
  updatedAt: string
}

export interface IPage<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

export function pageVideoInfo(params: any) {
  return request({
    url: '/video-info/page',
    method: 'get',
    params
  })
}

export function refreshVideoInfoStats(videoId: string) {
  return request({
    url: `/video-info/${encodeURIComponent(videoId)}/refresh-stats`,
    method: 'post'
  })
}

export function updateVideoInfoTitle(videoId: string, title: string) {
  return request({
    url: `/video-info/${encodeURIComponent(videoId)}/title`,
    method: 'put',
    params: { title }
  })
}

export function deleteVideoInfo(videoId: string) {
  return request({
    url: `/video-info/${encodeURIComponent(videoId)}`,
    method: 'delete'
  })
}

