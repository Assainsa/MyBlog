import request from '@/request'

export function login(account, password) {
  const data = {
    account,
    password
  }
  return request({
    url: '/login',
    method: 'post',
    data
  })
}

export function logout(token) {
  return request({
    headers: {'Authorization': token},
    url: '/logout',
    method: 'get'
  })
}

export function getUserInfo(token) {
  return request({
    headers: {'Authorization': token},
    url: '/users/currentUser',
    method: 'get'
  })
}

export function register(account, nickname, password, avatar) {
  const data = {
    account,
    nickname,
    password,
    avatar
  }
  return request({
    url: '/register',
    method: 'post',
    data
  })
}

export function updateUser(user, token) {
  return request({
    headers: {'Authorization': token},
    url: '/users/update',
    method: 'post',
    data: user
  })
}

export function getInvitationCode() {
  return request({
    url: '/register/InvitationCode',
    method: 'get'
  })
}
