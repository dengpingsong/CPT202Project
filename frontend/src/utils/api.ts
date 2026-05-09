import axios from 'axios'

const BASE_URL = '/api'

function getToken(): string {
  return localStorage.getItem('token') || ''
}

function setAuth(data: {
  token?: string
  role?: string
  userId?: number
  username?: string
  fullName?: string
  accountStatus?: string
}) {
  if (data.token) localStorage.setItem('token', data.token)
  if (data.role) localStorage.setItem('role', data.role)
  if (data.userId) localStorage.setItem('userId', String(data.userId))
  if (data.username) localStorage.setItem('username', data.username)
  if (data.fullName) localStorage.setItem('fullName', data.fullName)
  if (data.accountStatus)
    localStorage.setItem('accountStatus', data.accountStatus)
}

function clearAuth() {
  localStorage.removeItem('token')
  localStorage.removeItem('role')
  localStorage.removeItem('userId')
  localStorage.removeItem('username')
  localStorage.removeItem('fullName')
  localStorage.removeItem('accountStatus')
}

function getCurrentUser() {
  return {
    token: getToken(),
    userId: Number(localStorage.getItem('userId') || '0'),
    username: localStorage.getItem('username') || '',
    fullName: localStorage.getItem('fullName') || '',
    role: (localStorage.getItem('role') || '').toUpperCase(),
    accountStatus: localStorage.getItem('accountStatus') || '',
  }
}

interface ApiResponse<T = any> {
  code: number
  msg?: string
  data?: T
}

type ApiSuccessResponse<T> = ApiResponse<T> & { data: T }

export interface PageQuery {
  pageNum: number
  pageSize: number
}

export interface PageResult<T = any> {
  total: number
  records: T[]
  pageNum: number
  pageSize: number
  totalPages: number
}

function toPositiveInt(value: unknown, fallback: number): number {
  const normalized = Number(value)
  if (!Number.isFinite(normalized) || normalized < 1) return fallback
  return Math.floor(normalized)
}

function toPageNumber(value: unknown, totalPages: number, fallback: number): number {
  return Math.min(totalPages, toPositiveInt(value, fallback))
}

function isPageResultPayload<T>(
  payload: PageResult<T> | T[] | null | undefined,
): payload is PageResult<T> {
  return Boolean(payload && !Array.isArray(payload) && 'records' in payload)
}

export function normalizePageResult<T = any>(
  payload: PageResult<T> | T[] | null | undefined,
  fallback: Partial<PageQuery> = {},
): PageResult<T> {
  const fallbackPageSize = toPositiveInt(fallback.pageSize, 10)
  const fallbackPageNum = toPositiveInt(fallback.pageNum, 1)

  if (isPageResultPayload(payload)) {
    const records = Array.isArray(payload.records) ? payload.records : []
    const pageSize = toPositiveInt(payload.pageSize, fallbackPageSize)
    const total = Math.max(records.length, Number(payload.total) || records.length)
    const totalPages = Math.max(
      1,
      toPositiveInt(payload.totalPages, Math.ceil(total / pageSize) || 1),
    )
    const pageNum = toPageNumber(payload.pageNum, totalPages, fallbackPageNum)

    return {
      total,
      records,
      pageNum,
      pageSize,
      totalPages,
    }
  }

  const records = Array.isArray(payload) ? payload : []
  const pageSize = fallbackPageSize
  const total = records.length
  const totalPages = Math.max(1, Math.ceil(total / pageSize) || 1)
  const pageNum = toPageNumber(fallbackPageNum, totalPages, 1)
  const start = (pageNum - 1) * pageSize

  return {
    total,
    records: records.slice(start, start + pageSize),
    pageNum,
    pageSize,
    totalPages,
  }
}

const client = axios.create({
  baseURL: BASE_URL,
  timeout: 15000,
})

client.interceptors.request.use((config) => {
  const token = getToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

client.interceptors.response.use(
  (response) => {
    return response
  },
  (error) => {
    if (error.response?.status === 401) {
      clearAuth()
      window.location.href = '/login'
    }
    return Promise.reject(error)
  },
)

async function request<T = any>(
  url: string,
  options: { method?: string; body?: any } = {},
): Promise<ApiResponse<T>> {
  const { method = 'GET', body } = options
  try {
    const response = await client.request<ApiResponse<T>>({
      url,
      method: method.toLowerCase(),
      data: body,
    })
    const result = response.data
    if (result && result.code !== undefined && result.code !== 1) {
      throw new Error(result.msg || 'Request failed.')
    }
    return result
  } catch (error: any) {
    const message =
      error.response?.data?.msg || error.message || 'Request failed.'
    throw new Error(message)
  }
}

async function requestPageResult<T = any>(
  url: string,
  page: Partial<PageQuery> = {},
  options: { method?: string; body?: any } = {},
) : Promise<ApiSuccessResponse<PageResult<T>>> {
  const result = await request<PageResult<T> | T[]>(url, options)
  return {
    ...result,
    data: normalizePageResult(result.data, page),
  }
}

// Auth APIs
export const authApi = {
  login: (payload: { username: string; password: string }) =>
    request('/common/auth/login', { method: 'POST', body: payload }),

  register: (payload: Record<string, string>) =>
    request('/common/auth/register', { method: 'POST', body: payload }),

  sendRegisterEmailOtp: (email: string) =>
    request('/common/auth/register/email-otp/send', {
      method: 'POST',
      body: { email },
    }),

  sendEmailOtp: (email: string) =>
    request('/common/auth/email-otp/send', { method: 'POST', body: { email } }),

  emailOtpLogin: (email: string, otp: string) =>
    request('/common/auth/email-otp/login', {
      method: 'POST',
      body: { email, otp },
    }),

  verifyTwoFactor: (challengeToken: string, code: string) =>
    request('/common/auth/2fa/verify-login', {
      method: 'POST',
      body: { challengeToken, code },
    }),

  forgotPassword: (email: string) =>
    request('/common/auth/forgot-password', {
      method: 'POST',
      body: { email },
    }),

  resetPassword: (token: string, newPassword: string) =>
    request('/common/auth/reset-password', {
      method: 'POST',
      body: { token, newPassword },
    }),
}

// Student APIs
export const studentApi = {
  getRequests: () => request('/student/requests'),
  getProjects: (
    pageNum = 1,
    pageSize = 12,
    filters: Record<string, any> = {},
  ) => {
    const params = new URLSearchParams({
      pageNum: String(pageNum),
      pageSize: String(pageSize),
    })
    if (filters.keyword) params.set('keyword', String(filters.keyword))
    if (filters.categoryId) params.set('categoryId', String(filters.categoryId))
    if (filters.status) params.set('status', String(filters.status))
    const tagIds = Array.isArray(filters.tagIds) ? filters.tagIds : []
    tagIds.forEach((tagId: number | string) =>
      params.append('tagIds', String(tagId)),
    )
    return requestPageResult(`/student/projects?${params.toString()}`, {
      pageNum,
      pageSize,
    })
  },
  getProfile: () => request('/student/profile/me'),
  updateProfile: (payload: Record<string, any>) =>
    request('/student/profile/me', { method: 'PUT', body: payload }),
  changePassword: (oldPassword: string, newPassword: string) =>
    request('/student/profile/me/password', {
      method: 'PUT',
      body: { oldPassword, newPassword },
    }),
  withdrawRequest: (requestId: number | string) =>
    request(`/student/requests/${requestId}/withdraw`, { method: 'PUT' }),
  listProjectTags: () => request('/student/projects/tags'),
  listProjectCategories: () => request('/student/projects/categories'),
  initializeTwoFactorSetup: () =>
    request('/student/profile/me/2fa/setup', { method: 'POST', body: {} }),
  enableTwoFactor: (code: string) =>
    request('/student/profile/me/2fa/enable', {
      method: 'POST',
      body: { code },
    }),
  disableTwoFactor: (currentPassword: string) =>
    request('/student/profile/me/2fa/disable', {
      method: 'POST',
      body: { currentPassword },
    }),
}

// Teacher APIs
export const teacherApi = {
  listProjects: (status?: string) =>
    request(`/teacher/projects${status ? `?status=${status}` : ''}`),
  getProject: (projectId: number | string) =>
    request(`/teacher/projects/${projectId}`),
  createProject: (payload: Record<string, any>) =>
    request('/teacher/projects', { method: 'POST', body: payload }),
  updateProject: (projectId: number | string, payload: Record<string, any>) =>
    request(`/teacher/projects/${projectId}`, { method: 'PUT', body: payload }),
  changeProjectStatus: (
    projectId: number | string,
    projectStatus: string,
    remark?: string,
  ) =>
    request(`/teacher/projects/${projectId}/status`, {
      method: 'PUT',
      body: { projectStatus, remark: remark || '' },
    }),
  listCategories: () => request('/teacher/categories'),
  listTags: () => request('/teacher/tags'),
  listProjectTags: (projectId: number | string) =>
    request(`/teacher/project-tags/${projectId}`),
  bindProjectTags: (projectId: number | string, tagIds: number[]) =>
    request(`/teacher/project-tags/${projectId}`, {
      method: 'PUT',
      body: { tagIds },
    }),
  listRequests: (status?: string) =>
    request(`/teacher/requests${status ? `?status=${status}` : ''}`),
  listHistory: () => request('/teacher/requests'),
  listNotifications: () => request('/teacher/requests'),
  getProfile: () => request('/teacher/profile/me'),
  updateProfile: (payload: Record<string, any>) =>
    request('/teacher/profile/me', { method: 'PUT', body: payload }),
  changePassword: (oldPassword: string, newPassword: string) =>
    request('/teacher/profile/me/password', {
      method: 'PUT',
      body: { oldPassword, newPassword },
    }),
  initializeTwoFactorSetup: () =>
    request('/teacher/profile/me/2fa/setup', { method: 'POST', body: {} }),
  enableTwoFactor: (code: string) =>
    request('/teacher/profile/me/2fa/enable', {
      method: 'POST',
      body: { code },
    }),
  disableTwoFactor: (currentPassword: string) =>
    request('/teacher/profile/me/2fa/disable', {
      method: 'POST',
      body: { currentPassword },
    }),
  reviewRequest: (
    requestId: number | string,
    requestStatus: string,
    decisionComment?: string,
  ) =>
    request(`/teacher/requests/${requestId}/review`, {
      method: 'PUT',
      body: { requestStatus, decisionComment: decisionComment || '' },
    }),
}

// Admin APIs
export const adminApi = {
  // Users
  listUsers: (role?: string, accountStatus?: string) => {
    const params = new URLSearchParams()
    if (role) params.set('role', role)
    if (accountStatus) params.set('accountStatus', accountStatus)
    const query = params.toString()
    return request(`/admin/users${query ? `?${query}` : ''}`)
  },
  listUsersPage: (
    role?: string,
    accountStatus?: string,
    page: Partial<PageQuery> = {},
  ) => {
    const params = new URLSearchParams()
    if (role) params.set('role', role)
    if (accountStatus) params.set('accountStatus', accountStatus)
    const query = params.toString()
    return requestPageResult(
      `/admin/users${query ? `?${query}` : ''}`,
      page,
    )
  },
  updateUserStatus: (userId: number | string, accountStatus: string) =>
    request(`/admin/users/${userId}/status?accountStatus=${accountStatus}`, {
      method: 'PUT',
    }),
  updateUser: (
    userId: number | string,
    payload: { username: string; fullName: string; email: string },
  ) => request(`/admin/users/${userId}`, { method: 'PUT', body: payload }),

  // Projects
  listProjects: () => request('/admin/records/projects'),
  listProjectsPage: (page: Partial<PageQuery> = {}) =>
    requestPageResult('/admin/records/projects', page),
  listProjectTags: (projectId: number | string) =>
    request(`/admin/projects/${projectId}/tags`),

  // Requests
  listRequestRecords: (status?: string) => {
    const params = new URLSearchParams()
    if (status) params.set('status', status)
    const query = params.toString()
    return request(`/admin/records/requests${query ? `?${query}` : ''}`)
  },
  listRequestRecordsPage: (
    status?: string,
    page: Partial<PageQuery> = {},
  ) => {
    const params = new URLSearchParams()
    if (status) params.set('status', status)
    const query = params.toString()
    return requestPageResult(
      `/admin/records/requests${query ? `?${query}` : ''}`,
      page,
    )
  },
  listRequestHistoryRecords: () => request('/admin/records/request-history'),
  listRequestHistoryRecordsPage: (page: Partial<PageQuery> = {}) =>
    requestPageResult('/admin/records/request-history', page),

  // Categories
  listCategories: () => request('/admin/categories'),
  listCategoriesPage: (page: Partial<PageQuery> = {}) =>
    requestPageResult('/admin/categories', page),
  createCategory: (payload: { categoryName: string; description?: string }) =>
    request('/admin/categories', { method: 'POST', body: payload }),
  updateCategory: (
    categoryId: number | string,
    payload: { categoryName: string; description?: string },
  ) =>
    request(`/admin/categories/${categoryId}`, {
      method: 'PUT',
      body: payload,
    }),
  deleteCategory: (categoryId: number | string) =>
    request(`/admin/categories/${categoryId}`, { method: 'DELETE' }),

  // Tags
  listTags: () => request('/admin/tags'),
  listTagsPage: (page: Partial<PageQuery> = {}) =>
    requestPageResult('/admin/tags', page),
  createTag: (payload: { tagName: string; description?: string }) =>
    request('/admin/tags', { method: 'POST', body: payload }),
  updateTag: (
    tagId: number | string,
    payload: { tagName: string; description?: string },
  ) => request(`/admin/tags/${tagId}`, { method: 'PUT', body: payload }),
  deleteTag: (tagId: number | string) =>
    request(`/admin/tags/${tagId}`, { method: 'DELETE' }),

  // Profile
  getProfile: () => request('/admin/profile/me'),
  updateProfile: (payload: Record<string, any>) =>
    request('/admin/profile/me', { method: 'PUT', body: payload }),
  changePassword: (payload: { oldPassword: string; newPassword: string }) =>
    request('/admin/profile/me/password', { method: 'PUT', body: payload }),

  // 2FA
  initializeTwoFactorSetup: () =>
    request('/admin/profile/me/2fa/setup', { method: 'POST', body: {} }),
  enableTwoFactor: (code: string) =>
    request('/admin/profile/me/2fa/enable', { method: 'POST', body: { code } }),
  disableTwoFactor: (currentPassword: string) =>
    request('/admin/profile/me/2fa/disable', {
      method: 'POST',
      body: { currentPassword },
    }),
}

// Generic request helpers
export const api = {
  get: <T = any>(url: string) => request<T>(url),
  post: <T = any>(url: string, body?: any) =>
    request<T>(url, { method: 'POST', body }),
  put: <T = any>(url: string, body?: any) =>
    request<T>(url, { method: 'PUT', body }),
  delete: <T = any>(url: string) => request<T>(url, { method: 'DELETE' }),
}

export { setAuth, clearAuth, getCurrentUser }
