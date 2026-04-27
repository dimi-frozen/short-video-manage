import request from '@/utils/request'

export interface KuaishouAuthResp {
  authUrl: string
  accessToken: string
  expiresInSeconds: number
  expireAt: string
}

export interface KuaishouPublishResp {
  videoId: string
  title: string
  publishTime: string
  status: string
}

export interface KuaishouVideoStatsResp {
  videoId: string
  views: number
  likes: number
  comments: number
  shares: number
  statTime: string
}

export interface KuaishouVideoItem {
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

export function kuaishouAuthUrl() {
  return request({
    url: '/platforms/kuaishou/auth-url',
    method: 'get'
  })
}

export function kuaishouPublishVideo(form: FormData, token?: string) {
  return request({
    url: '/platforms/kuaishou/videos/publish',
    method: 'post',
    data: form,
    params: token ? { access_token: token } : undefined,
    timeout: 300000
  })
}

export function kuaishouVideoStats(videoId: string) {
  return request({
    url: `/platforms/kuaishou/videos/${encodeURIComponent(videoId)}/stats`,
    method: 'get'
  })
}

export function kuaishouVideoPage(params: any) {
  return request({
    url: '/platforms/kuaishou/videos',
    method: 'get',
    params
  })
}

export function kuaishouUpdateTitle(videoId: string, title: string) {
  return request({
    url: `/platforms/kuaishou/videos/${encodeURIComponent(videoId)}`,
    method: 'put',
    data: { title }
  })
}

export function kuaishouDeleteVideo(videoId: string) {
  return request({
    url: `/platforms/kuaishou/videos/${encodeURIComponent(videoId)}`,
    method: 'delete'
  })
}

