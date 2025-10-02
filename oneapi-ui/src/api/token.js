import axios from 'axios'

// 创建 axios 实例
const request = axios.create({
    baseURL: import.meta.env.VITE_API_URL,
    timeout: 6000
})

export function getAllTokens() {
    return request({
        url: '/service/com.supersoft.oneapi.token.service.OneapiTokenService/getAllTokens',
        method: 'post',
        data: []
    })
}

export function createToken(name, description, expireTime, maxUsage, creator) {
    return request({
        url: '/service/com.supersoft.oneapi.token.service.OneapiTokenService/createToken',
        method: 'post',
        data: [name, description, expireTime, maxUsage, creator]
    })
}

export function updateToken(token) {
    return request({
        url: '/service/com.supersoft.oneapi.token.service.OneapiTokenService/updateToken',
        method: 'post',
        data: [token]
    })
}

export function deleteToken(id) {
    return request({
        url: '/service/com.supersoft.oneapi.token.service.OneapiTokenService/deleteToken',
        method: 'post',
        data: [id]
    })
}

export function getUsageRecords(limit = 100) {
    return request({
        url: '/service/com.supersoft.oneapi.token.service.OneapiTokenService/getUsageRecords',
        method: 'post',
        data: [limit]
    })
}

export function queryUsageRecords(provider, model, status, startTime, endTime, page, pageSize) {
    return request({
        url: '/service/com.supersoft.oneapi.token.service.OneapiTokenService/queryUsageRecords',
        method: 'post',
        data: [provider, model, status, startTime, endTime, page, pageSize]
    })
}
