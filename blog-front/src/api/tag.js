import request from '@/request'

export function getAllTags() {
  return request({
    url: '/tags',
    method: 'get',
  })
}

export function getAllTagsDetail() {
  return request({
    url: '/tags/detail',
    method: 'get',
  })
}

export function getHotTags() {
  return request({
    url: '/tags/hot',
    method: 'get',
  })
}

export function getTag(id) {
  return request({
    url: `/tags/${id}`,
    method: 'get',
  })
}

export function getTagDetail(id) {
  return request({
    url: `/tags/detail/${id}`,
    method: 'get',
  })
}

export function addTag(tagName) {
  return request({
    url: `/tags/add/${tagName}`,
    method: 'get',
  })
}

export function deteleTag(tagId) {
  return request({
    url: `/tags/delete/${tagId}`,
    method: 'get',
  })
}
