import request from '@/utils/request'

export function login(data: any) {
  return request({
    url: '/auth/login',
    method: 'post',
    data
  })
}

export function register(data: any) {
  return request({
    url: '/auth/register',
    method: 'post',
    data
  })
}

export function getProfile() {
  return request({
    url: '/users/profile',
    method: 'get'
  })
}

export function updateProfile(data: any) {
  return request({
    url: '/users/profile',
    method: 'put',
    data
  })
}

export function changePassword(data: any) {
  return request({
    url: '/users/password',
    method: 'put',
    data
  })
}
