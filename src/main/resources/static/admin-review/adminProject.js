 // ==================== 配置 ====================
    const BASE_URL = '';      // 后端地址，请根据实际情况修改
    const TOKEN_KEY = 'token';
    const LOGIN_PAGE = '../login.html';

    // 获取存储的 token
    const token = localStorage.getItem(TOKEN_KEY);
    if (!token) {
    alert('请先登录');
    window.location.href = LOGIN_PAGE;
}

    // 通用请求函数（自动携带 token）
    async function request(url, options = {}) {
    const token = localStorage.getItem("token") || "";

    const fullUrl = url.startsWith('http') ? url : BASE_URL + url;
    const headers = {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${token}`,
    ...options.headers
};
    const response = await fetch(fullUrl, { ...options, headers });
    if (response.status === 401) {
    // token 无效或过期，清除本地存储并跳转登录页
    localStorage.removeItem(TOKEN_KEY);
    alert('登录已过期，请重新登录');
    window.location.href = LOGIN_PAGE;
    throw new Error('Unauthorized');
}
    const json = await response.json();
    if (json.code !== 1) {
    // 后端返回业务错误，但如果是“未实现”异常，也抛出供上层捕获
    throw new Error(json.msg || '请求失败');
}
    return json.data;
}

    // ==================== 数据加载 ====================
    async function loadCategories() {
    return request('/api/admin/categories');
}

    async function loadProjectTags(projectId) {
    try {
    return await request(`/api/admin/projects/${projectId}/tags`);
} catch (e) {
    console.warn(`获取项目 ${projectId} 标签失败:`, e.message);
    return null;   // 表示标签接口不可用或无数据
}
}

    // 核心渲染函数
    async function renderProjects() {
    const tbody = document.getElementById('projectTableBody');
    const countSpan = document.getElementById('projectCountBadge');
    if (!tbody) return;

    // 显示加载中
    tbody.innerHTML = '<td><td colspan="4">⏳ 加载项目列表中...</td></tr>';
    countSpan.innerText = '加载中...';

    try {
    // 1. 获取项目列表（可能尚未实现）
    let projects;
    try {
    projects = await request('/api/admin/records/projects');
} catch (e) {
    // 如果接口抛出异常（如 UnsupportedOperationException），后端可能返回 500 或 json 中的 code!=1
    // 此时显示友好提示
    const errorMsg = e.message || '后端接口未实现或异常';
    tbody.innerHTML = `<tr><td colspan="4"><div class="error-message">⚠️ 无法加载项目列表：${errorMsg}<br>请确认后端实现了 /api/admin/records/projects 接口。</div></td></tr>`;
    countSpan.innerText = '加载失败';
    return;
}

    if (!projects || projects.length === 0) {
    tbody.innerHTML = '<tr><td colspan="4">暂无项目数据</td></tr>';
    countSpan.innerText = '0 个项目';
    return;
}

    // 2. 加载类别映射
    let categories = [];
    let catMap = new Map();
    try {
    categories = await loadCategories();
    catMap = new Map(categories.map(c => [c.categoryId, c.categoryName]));
} catch (e) {
    console.warn('加载类别失败，将不显示类别名称', e);
}

    // 3. 逐个项目渲染 (为了显示标签，需要串行请求，但可显示加载占位)
    let html = '';
    for (const proj of projects) {
    const categoryName = catMap.get(proj.categoryId) || proj.categoryName || '未分类';
    let tagsHtml = '<span class="loading-text">加载标签中...</span>';
    const tagData = await loadProjectTags(proj.projectId);
    if (tagData && Array.isArray(tagData) && tagData.length > 0) {
    tagsHtml = tagData.map(t => `<span class="tag-pill">${escapeHtml(t.tagName || t.name)}</span>`).join('');
} else if (tagData === null) {
    tagsHtml = '<span class="loading-text">暂未提供标签接口</span>';
} else {
    tagsHtml = '<span class="loading-text">无标签</span>';
}
    html += `<tr>
                            <td>${escapeHtml(proj.projectId)}</td>
                            <td><strong>${escapeHtml(proj.title)}</strong></td>
                            <td><span class="tag-pill">${escapeHtml(categoryName)}</span></td>
                            <td><div class="tag-group">${tagsHtml}</div></td>
                         </tr>`;
}
    tbody.innerHTML = html;
    countSpan.innerText = `${projects.length} 个项目`;
} catch (err) {
    console.error(err);
    tbody.innerHTML = `<tr><td colspan="4"><div class="error-message">❌ 加载失败：${err.message || '网络错误'}</div></td></tr>`;
    countSpan.innerText = '加载失败';
}
}

    // 辅助函数：防止 XSS
    function escapeHtml(str) {
    if (str === undefined || str === null) return '';
    return String(str).replace(/[&<>]/g, m => {
    if (m === '&') return '&amp;';
    if (m === '<') return '&lt;';
    if (m === '>') return '&gt;';
    return m;
});
}

    // 执行渲染
    renderProjects();

    // 侧边栏折叠逻辑
    const layout = document.getElementById('layout');
    const toggle = document.getElementById('toggleSidebar');
    let collapsed = false;
    toggle.addEventListener('click', () => {
    collapsed = !collapsed;
    layout.classList.toggle('collapsed', collapsed);
});
