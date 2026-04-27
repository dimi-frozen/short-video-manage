import request from '@/utils/request'

export interface Comment {
  id: number
  content: string
  userId: number
  userNickname: string
  videoId: number
  createTime: string
  updateTime: string
}

export interface AddCommentReq {
  videoId: number
  content: string
}

export interface CommentListResp {
  total: number
  list: Comment[]
}

/**
 * 获取视频评论列表
 * @param videoId 视频ID
 * @returns 评论列表
 */
export function getCommentList(videoId: number) {
  return request({
    url: '/comment/list',
    method: 'GET',
    params: { videoId }
  })
}

/**
 * 发表评论
 * @param data 评论数据
 * @returns 发表结果
 */
export function addComment(data: AddCommentReq) {
  return request({
    url: '/comment/add',
    method: 'POST',
    params: data
  })
}