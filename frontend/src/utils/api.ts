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
  if (data.accountStatus) localStorage.setItem('accountStatus', data.accountStatus)
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

async function request<T = any>(url: string, options: RequestInit = {}): Promise<ApiResponse<T>> {
  const token = getToken()
  const headers = new Headers(options.headers || {})

  if (!headers.has('Content-Type') && options.body !== undefined) {
    headers.set('Content-Type', 'application/json')
  }
  if (token && !headers.has('Authorization')) {
    headers.set('Authorization', `Bearer ${token}`)
  }

  const response = await fetch(`${BASE_URL}${url}`, { ...options, headers })
  const result: ApiResponse<T> = await response.json()

  if (response.status === 401) {
    clearAuth()
    window.location.href = '/login'
    throw new Error('Login expired')
  }

  return result
}

// Auth APIs
export const authApi = {
  login: (payload: { username: string; password: string }) =>
    request('/common/auth/login', { method: 'POST', body: JSON.stringify(payload) }),

  register: (payload: Record<string, string>) =>
    request('/common/auth/register', { method: 'POST', body: JSON.stringify(payload) }),

  sendEmailOtp: (email: string) =>
    request('/common/auth/email-otp/send', { method: 'POST', body: JSON.stringify({ email }) }),

  emailOtpLogin: (email: string, otp: string) =>
    request('/common/auth/email-otp/login', { method: 'POST', body: JSON.stringify({ email, otp }) }),

  verifyTwoFactor: (challengeToken: string, code: string) =>
    request('/common/auth/2fa/verify-login', { method: 'POST', body: JSON.stringify({ challengeToken, code }) }),

  forgotPassword: (email: string) =>
    request('/common/auth/forgot-password', { method: 'POST', body: JSON.stringify({ email }) }),

  resetPassword: (token: string, newPassword: string) =>
    request('/common/auth/reset-password', { method: 'POST', body: JSON.stringify({ token, newPassword }) }),
}

// Student APIs
export const studentApi = {
  getRequests: () => request('/student/requests'),
  getProjects: (pageNum = 1, pageSize = 12) =>
    request(`/student/projects?pageNum=${pageNum}&pageSize=${pageSize}`),
  getProfile: () => request('/student/profile/me'),
  updateProfile: (payload: Record<string, any>) =>
    request('/student/profile/me', { method: 'PUT', body: JSON.stringify(payload) }),
  changePassword: (oldPassword: string, newPassword: string) =>
    request('/student/profile/me/password', { method: 'PUT', body: JSON.stringify({ oldPassword, newPassword }) }),
  withdrawRequest: (requestId: number | string) =>
    request(`/student/requests/${requestId}/withdraw`, { method: 'PUT' }),
}

// Teacher APIs
export const teacherApi = {
  listProjects: (status?: string) =>
    request(`/teacher/projects${status ? `?status=${status}` : ''}`),
  getProject: (projectId: number | string) =>
    request(`/teacher/projects/${projectId}`),
  createProject: (payload: Record<string, any>) =>
    request('/teacher/projects', { method: 'POST', body: JSON.stringify(payload) }),
  updateProject: (projectId: number | string, payload: Record<string, any>) =>
    request(`/teacher/projects/${projectId}`, { method: 'PUT', body: JSON.stringify(payload) }),
  changeProjectStatus: (projectId: number | string, projectStatus: string, remark?: string) =>
    request(`/teacher/projects/${projectId}/status`, {
      method: 'PUT',
      body: JSON.stringify({ projectStatus, remark: remark || '' }),
    }),
  listCategories: () => request('/teacher/categories'),
  listTags: () => request('/teacher/tags'),
  listProjectTags: (projectId: number | string) =>
    request(`/teacher/project-tags/${projectId}`),
  bindProjectTags: (projectId: number | string, tagIds: number[]) =>
    request(`/teacher/project-tags/${projectId}`, { method: 'PUT', body: JSON.stringify({ tagIds }) }),
  listRequests: (status?: string) =>
    request(`/teacher/requests${status ? `?status=${status}` : ''}`),
  listHistory: () => request('/teacher/requests'),
  listNotifications: () => request('/teacher/requests'),
  getProfile: () => request('/teacher/profile/me'),
  updateProfile: (payload: Record<string, any>) =>
    request('/teacher/profile/me', { method: 'PUT', body: JSON.stringify(payload) }),
  changePassword: (oldPassword: string, newPassword: string) =>
    request('/teacher/profile/me/password', { method: 'PUT', body: JSON.stringify({ oldPassword, newPassword }) }),
  initializeTwoFactorSetup: () =>
    request('/teacher/profile/me/2fa/setup', { method: 'POST', body: JSON.stringify({}) }),
  enableTwoFactor: (code: string) =>
    request('/teacher/profile/me/2fa/enable', { method: 'POST', body: JSON.stringify({ code }) }),
  disableTwoFactor: (currentPassword: string) =>
    request('/teacher/profile/me/2fa/disable', { method: 'POST', body: JSON.stringify({ currentPassword }) }),
  reviewRequest: (requestId: number | string, requestStatus: string, decisionComment?: string) =>
    request(`/teacher/requests/${requestId}/review`, {
      method: 'PUT',
      body: JSON.stringify({ requestStatus, decisionComment: decisionComment || '' }),
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
  updateUserStatus: (userId: number | string, accountStatus: string) =>
    request(`/admin/users/${userId}/status?accountStatus=${accountStatus}`, { method: 'PUT' }),

  // Projects
  listProjects: () => request('/admin/records/projects'),
  listProjectTags: (projectId: number | string) =>
    request(`/admin/projects/${projectId}/tags`),

  // Requests
  listRequestRecords: (status?: string) => {
    const params = new URLSearchParams()
    if (status) params.set('status', status)
    const query = params.toString()
    return request(`/admin/records/requests${query ? `?${query}` : ''}`)
  },
  listRequestHistoryRecords: () => request('/admin/records/request-history'),

  // Categories
  listCategories: () => request('/admin/categories'),
  createCategory: (payload: { categoryName: string; description?: string }) =>
    request('/admin/categories', { method: 'POST', body: JSON.stringify(payload) }),
  updateCategory: (categoryId: number | string, payload: { categoryName: string; description?: string }) =>
    request(`/admin/categories/${categoryId}`, { method: 'PUT', body: JSON.stringify(payload) }),
  deleteCategory: (categoryId: number | string) =>
    request(`/admin/categories/${categoryId}`, { method: 'DELETE' }),

  // Tags
  listTags: () => request('/admin/tags'),
  createTag: (payload: { tagName: string; description?: string }) =>
    request('/admin/tags', { method: 'POST', body: JSON.stringify(payload) }),
  updateTag: (tagId: number | string, payload: { tagName: string; description?: string }) =>
    request(`/admin/tags/${tagId}`, { method: 'PUT', body: JSON.stringify(payload) }),
  deleteTag: (tagId: number | string) =>
    request(`/admin/tags/${tagId}`, { method: 'DELETE' }),

  // Profile
  getProfile: () => request('/admin/profile/me'),
  updateProfile: (payload: Record<string, any>) =>
    request('/admin/profile/me', { method: 'PUT', body: JSON.stringify(payload) }),
  changePassword: (payload: { oldPassword: string; newPassword: string }) =>
    request('/admin/profile/me/password', { method: 'PUT', body: JSON.stringify(payload) }),

  // 2FA
  initializeTwoFactorSetup: () =>
    request('/admin/profile/me/2fa/setup', { method: 'POST', body: JSON.stringify({}) }),
  enableTwoFactor: (code: string) =>
    request('/admin/profile/me/2fa/enable', { method: 'POST', body: JSON.stringify({ code }) }),
  disableTwoFactor: (currentPassword: string) =>
    request('/admin/profile/me/2fa/disable', { method: 'POST', body: JSON.stringify({ currentPassword }) }),
}

// Generic request helpers
export const api = {
  get: <T = any>(url: string) => request<T>(url),
  post: <T = any>(url: string, body?: any) =>
    request<T>(url, { method: 'POST', body: body ? JSON.stringify(body) : undefined }),
  put: <T = any>(url: string, body?: any) =>
    request<T>(url, { method: 'PUT', body: body ? JSON.stringify(body) : undefined }),
  delete: <T = any>(url: string) => request<T>(url, { method: 'DELETE' }),
}

export { setAuth, clearAuth, getCurrentUser }
