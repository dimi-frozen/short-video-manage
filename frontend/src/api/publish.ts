import request from '@/utils/request'

export type PublishPlatform = 'douyin' | 'kuaishou' | 'bilibili'

export interface PublishCreateReq {
  videoId: number
  platform: PublishPlatform
}

export interface PublishCreateResp {
  recordId: number
  redirectUrl: string
}

export interface PublishConfirmReq {
  recordId: number
}

export interface PublishConfirmResp {
  recordId: number
  status: string
  platformVideoUrl: string
  platformVideoId: string
}

export interface PublishRecordResp {
  recordId: number
  videoId: number
  platform: string
  status: string
  platformVideoUrl?: string
  createTime: string
  updateTime: string
}

export function createPublishTask(data: PublishCreateReq) {
  return request({
    url: '/publish',
    method: 'post',
    data
  })
}

export function confirmPublishTask(data: PublishConfirmReq) {
  return request({
    url: '/publish/confirm',
    method: 'post',
    data
  })
}

export function getPublishRecord(recordId: number) {
  return request({
    url: `/publish/records/${recordId}`,
    method: 'get'
  })
}

export function listPublishRecords(params: { videoId: number; platform?: PublishPlatform }) {
  return request({
    url: '/publish/records',
    method: 'get',
    params
  })
}
