import request from '@/utils/request'

export interface AnalysisOverview {
  totalVideos: number
  totalViews: number
  totalLikes: number
  totalComments: number
  totalShares: number
}

export interface AnalysisPlatformAgg {
  platformType: string
  videoCount: number
  views: number
  likes: number
  comments: number
  shares: number
}

export interface AnalysisTopVideo {
  videoId: string
  title: string
  platformType: string
  publishTime: string
  views: number
  likes: number
  comments: number
  shares: number
}

export interface AnalysisTrendPoint {
  day: string
  views: number
  likes: number
  comments: number
  shares: number
}

export interface AnalysisDashboard {
  overview: AnalysisOverview
  platformAgg: AnalysisPlatformAgg[]
  topVideos: AnalysisTopVideo[]
  trend: AnalysisTrendPoint[]
}

export interface LocalTrendPoint {
  day: string
  uploadCount: number
  views: number
  likes: number
  comments: number
}

export interface LocalDashboard {
  totalVideos: number
  totalViews: number
  totalLikes: number
  totalComments: number
  trend: LocalTrendPoint[]
  yesterdaySnapshot?: {
    totalVideos: number
    totalViews: number
    totalLikes: number
    totalComments: number
  }
}

export function getAnalysisDashboard(params: any) {
  return request({
    url: '/analysis/dashboard',
    method: 'get',
    params
  })
}

export function getLocalDashboard(params: any) {
  return request({
    url: '/local-dashboard/dashboard',
    method: 'get',
    params
  })
}

