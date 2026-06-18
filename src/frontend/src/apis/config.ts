const SERVER_BACKEND_HOST = process.env.BACKEND_HOST || 'localhost'
const SERVER_BACKEND_PORT = process.env.BACKEND_PORT || '8080'
const SERVER_SSL_ENABLED = process.env.SSL_ENABLED === 'true'
const API_PREFIX = process.env.API_PREFIX || '/api/v1'

const CLIENT_BACKEND_HOST = process.env.NEXT_PUBLIC_BACKEND_HOST || 'localhost'
const CLIENT_BACKEND_PORT = process.env.NEXT_PUBLIC_BACKEND_PORT || '8080'
const CLIENT_SSL_ENABLED = process.env.NEXT_PUBLIC_SSL_ENABLED === 'true'

const isServer = typeof window === 'undefined'

const getApiBaseUrl = () => {
  const host = isServer ? SERVER_BACKEND_HOST : CLIENT_BACKEND_HOST
  const port = isServer ? SERVER_BACKEND_PORT : CLIENT_BACKEND_PORT
  const ssl = isServer ? SERVER_SSL_ENABLED : CLIENT_SSL_ENABLED
  const protocol = ssl ? 'https' : 'http'

  return `${protocol}://${host}:${port}${API_PREFIX}`
}

const AI_SERVICE_HOST = process.env.NEXT_PUBLIC_AI_SERVICE_HOST || 'localhost'
const AI_SERVICE_PORT = process.env.NEXT_PUBLIC_AI_SERVICE_PORT || '8000'
const AI_SERVICE_SSL_ENABLED = process.env.NEXT_PUBLIC_AI_SERVICE_SSL_ENABLED === 'true'
const AI_SERVICE_PREFIX = process.env.NEXT_PUBLIC_AI_SERVICE_PREFIX || '/ai/v1'

const getAiChatBaseUrl = () => {
  const host = AI_SERVICE_HOST
  const port = AI_SERVICE_PORT
  const ssl = AI_SERVICE_SSL_ENABLED
  const protocol = ssl ? 'https' : 'http'
  return `${protocol}://${host}:${port}${AI_SERVICE_PREFIX}`
}

export const API_BASE_URL = getApiBaseUrl()
export const AI_CHAT_BASE_URL = getAiChatBaseUrl()
