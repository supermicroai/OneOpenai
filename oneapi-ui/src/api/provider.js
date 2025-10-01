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

export function getProvider(id) {
    return request({
        url: '/service/com.supersoft.oneapi.proxy.service.OneapiConfigFacade/getProvider',
        method: 'post',
        data: [id]
    })
}

export function getProviders () {
    return request({
        url: '/service/com.supersoft.oneapi.proxy.service.OneapiConfigFacade/getProviders',
        method: 'post',
        data: []
    })
}

export function updateProvider (provider) {
    return request({
        url: '/service/com.supersoft.oneapi.proxy.service.OneapiConfigFacade/updateProvider',
        method: 'post',
        data: [provider]
    })
}

export function enableProvider (id, enable) {
    return request({
        url: '/service/com.supersoft.oneapi.proxy.service.OneapiConfigFacade/enableProvider',
        method: 'post',
        data: [id, enable]
    })
}

export function getAccounts (id) {
    return request({
        url: '/service/com.supersoft.oneapi.proxy.service.OneapiConfigFacade/getAccounts',
        method: 'post',
        data: [id]
    })
}

export function updateAccount (account) {
    return request({
        url: '/service/com.supersoft.oneapi.proxy.service.OneapiConfigFacade/updateAccount',
        method: 'post',
        data: [account]
    })
}

export function enableAccount (id, enable) {
    return request({
        url: '/service/com.supersoft.oneapi.proxy.service.OneapiConfigFacade/enableAccount',
        method: 'post',
        data: [id, enable]
    })
}

export function deleteAccount (id) {
    return request({
        url: '/service/com.supersoft.oneapi.proxy.service.OneapiConfigFacade/deleteAccount',
        method: 'post',
        data: [id]
    })
}
