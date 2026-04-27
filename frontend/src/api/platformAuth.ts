import request from '@/utils/request'

export type PlatformKey = 'douyin' | 'kuaishou' | 'bilibili'

export interface PlatformAuthStatusResp {
  platform: PlatformKey
  status: 'AUTHORIZED' | 'UNAUTHORIZED'
  expiresInSeconds: number
  expireAt: string | null
}

export function getPlatformAuthStatus(platform: PlatformKey) {
  return request({
    url: `/platforms/${encodeURIComponent(platform)}/auth-status`,
    method: 'get'
  })
}

