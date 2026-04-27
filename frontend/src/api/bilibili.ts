import request from '@/utils/request'

export interface BilibiliAuthResp {
  authUrl: string
  accessToken: string
  expiresInSeconds: number
  expireAt: string
}

export interface BilibiliPublishResp {
  videoId: string
  title: string
  publishTime: string
  status: string
}

export interface BilibiliVideoStatsResp {
  videoId: string
  views: number
  likes: number
  comments: number
  shares: number
  statTime: string
}

export interface BilibiliVideoItem {
  videoId: string
  title: string
  publishTime: string
  views: number
  likes: number
  comments: number
  shares: number
}

export interface IPage<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

export function bilibiliAuthUrl() {
  return request({
    url: '/platforms/bilibili/auth-url',
    method: 'get'
  })
}

export function bilibiliPublishVideo(form: FormData, token?: string) {
  return request({
    url: '/platforms/bilibili/videos/publish',
    method: 'post',
    data: form,
    params: token ? { access_token: token } : undefined,
    timeout: 300000
  })
}

export function bilibiliVideoStats(videoId: string) {
  return request({
    url: `/platforms/bilibili/videos/${encodeURIComponent(videoId)}/stats`,
    method: 'get'
  })
}

export function bilibiliVideoPage(params: any) {
  return request({
    url: '/platforms/bilibili/videos',
    method: 'get',
    params
  })
}

export function bilibiliUpdateTitle(videoId: string, title: string) {
  return request({
    url: `/platforms/bilibili/videos/${encodeURIComponent(videoId)}`,
    method: 'put',
    data: { title }
  })
}

export function bilibiliDeleteVideo(videoId: string) {
  return request({
    url: `/platforms/bilibili/videos/${encodeURIComponent(videoId)}`,
    method: 'delete'
  })
}

