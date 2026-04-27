import request from '@/utils/request'

export type PlatformKey = 'douyin' | 'kuaishou' | 'bilibili'

export interface PlatformPublishResp {
  videoId: string
  title: string
  publishTime: string
  status: string
  platformVideoUrl: string
}

export function publishLocalVideoToPlatform(platform: PlatformKey, videoId: number, accessToken?: string) {
  return request({
    url: `/platforms/${encodeURIComponent(platform)}/videos/publish-local`,
    method: 'post',
    params: {
      videoId,
      access_token: accessToken
    }
  })
}

