import axios from 'axios'

// 创建 axios 实例
const request = axios.create({
    baseURL: import.meta.env.VITE_API_URL,
    timeout: 6000
})

export function getConfigs () {
    return request({
        url: '/service/com.supersoft.oneapi.proxy.service.OneapiConfigFacade/getConfigs',
        method: 'post',
        data: []
    })
}

export function updateConfig (config) {
    return request({
        url: '/service/com.supersoft.oneapi.proxy.service.OneapiConfigFacade/updateConfig',
        method: 'post',
        data: [config]
    })
}