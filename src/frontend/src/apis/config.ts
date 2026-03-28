const BACKEND_HOST = process.env.BACKEND_HOST || 'localhost'
const BACKEND_PORT = process.env.BACKEND_PORT || '8080'
const SSL_ENABLED = process.env.SSL_ENABLED === 'true'
const API_PREFIX = process.env.API_PREFIX || '/api/v1'

const protocol = SSL_ENABLED ? 'https' : 'http'

export const API_BASE_URL = `${protocol}://${BACKEND_HOST}:${BACKEND_PORT}${API_PREFIX}`
