import request from '@/utils/request'

export interface DouyinAuthResp {
  authUrl: string
  accessToken: string
  expiresInSeconds: number
  expireAt: string
}

export interface DouyinPublishResp {
  videoId: string
  title: string
  publishTime: string
  status: string
}

export interface DouyinVideoStatsResp {
  videoId: string
  views: number
  likes: number
  comments: number
  shares: number
  statTime: string
}

export interface DouyinVideoItem {
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

export function douyinAuthUrl() {
  return request({
    url: '/platforms/douyin/auth-url',
    method: 'get'
  })
}

export function douyinPublishVideo(form: FormData, token?: string) {
  return request({
    url: '/platforms/douyin/videos/publish',
    method: 'post',
    data: form,
    params: token ? { access_token: token } : undefined,
    timeout: 300000
  })
}

export function douyinVideoStats(videoId: string) {
  return request({
    url: `/platforms/douyin/videos/${encodeURIComponent(videoId)}/stats`,
    method: 'get'
  })
}

export function douyinVideoPage(params: any) {
  return request({
    url: '/platforms/douyin/videos',
    method: 'get',
    params
  })
}

export function douyinUpdateTitle(videoId: string, title: string) {
  return request({
    url: `/platforms/douyin/videos/${encodeURIComponent(videoId)}`,
    method: 'put',
    data: { title }
  })
}

export function douyinDeleteVideo(videoId: string) {
  return request({
    url: `/platforms/douyin/videos/${encodeURIComponent(videoId)}`,
    method: 'delete'
  })
}
