import request from '@/utils/request'

export interface Comment {
  id: number
  content: string
  userId: number
  userName: string  // 评论用户名
  videoId: number
  parentId?: number  // 父评讻ID
  replyToUserName?: string  // 回复的目标用户名
  isDeleted?: boolean  // 是否已删除
  createTime: string
  updateTime: string
  children?: Comment[]  // 子评论列表
}

export interface AddCommentReq {
  videoId: number
  content: string
}

export interface ReplyCommentReq {
  videoId: number
  parentId: number
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

/**
 * 回复评论
 * @param data 回复数据
 * @returns 回复结果
 */
export function replyComment(data: ReplyCommentReq) {
  return request({
    url: '/comment/reply',
    method: 'POST',
    params: data
  })
}

/**
 * 获取子评论列表
 * @param parentId 父评论ID
 * @returns 子评论列表
 */
export function getChildComments(parentId: number) {
  return request({
    url: '/comment/children',
    method: 'GET',
    params: { parentId }
  })
}

/**
 * 删除评论
 * @param commentId 评论ID
 * @param videoId 视频ID
 * @returns 删除结果
 */
export function deleteComment(commentId: number, videoId: number) {
  return request({
    url: `/comment/${commentId}`,
    method: 'DELETE',
    params: { videoId }
  })
}