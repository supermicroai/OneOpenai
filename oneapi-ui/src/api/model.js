import axios from 'axios'

// 创建 axios 实例
const request = axios.create({
    baseURL: import.meta.env.VITE_API_URL,
    timeout: 6000
})

export function getModels () {
    return request({
        url: '/service/com.supersoft.oneapi.proxy.service.OneapiConfigFacade/getModels',
        method: 'post',
        data: []
    })
}

export function addModel (model) {
    return request({
        url: '/service/com.supersoft.oneapi.proxy.service.OneapiConfigFacade/addModel',
        method: 'post',
        data: [model]
    })
}

export function updateModel (model) {
    return request({
        url: '/service/com.supersoft.oneapi.proxy.service.OneapiConfigFacade/updateModel',
        method: 'post',
        data: [model]
    })
}

export function deleteModel (modelId) {
    return request({
        url: '/service/com.supersoft.oneapi.proxy.service.OneapiConfigFacade/deleteModel',
        method: 'post',
        data: [modelId]
    })
}

export function toggleModel (modelId, enabled) {
    return request({
        url: '/service/com.supersoft.oneapi.proxy.service.OneapiConfigFacade/toggleModel',
        method: 'post',
        data: [modelId, enabled]
    })
}

export function getEnabledModels (type) {
    return request({
        url: '/service/com.supersoft.oneapi.proxy.service.OneapiConfigFacade/getEnabledModels',
        method: 'post',
        data: [type]
    })
}
