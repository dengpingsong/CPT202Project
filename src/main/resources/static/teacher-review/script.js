// 侧边栏收起/展开
function toggleSidebar() {
    const sidebar = document.getElementById('sidebar');
    if(sidebar) {
        sidebar.classList.toggle('collapsed');
    }
}

// 当老师点击 Accept 按钮时触发 (对应 PBI 3 & 5)
function acceptProject() {
    alert("Success! The request status has been updated to 'Accepted'. The project status is now 'Agreed'.");
    // 假装处理完毕，跳回首页
    window.location.href = "index.html";
}

// 当老师在弹窗里点击 Confirm Reject 时触发 (对应 PBI 4)
function confirmReject() {
    const feedback = document.getElementById("feedback").value;
    
    if(feedback.trim() === "") {
        alert("Request Rejected without feedback.");
    } else {
        alert("Request Rejected. Feedback saved: " + feedback);
    }
    // 假装处理完毕，跳回首页
    window.location.href = "index.html";
}

// ----------------- 发布项目页面相关功能 -----------------
function addTag() {
    const input = document.getElementById('tagInput');
    if(!input) return;
    
    const val = input.value.trim();
    if (val) {
        const container = document.getElementById('tagsContainer');
        const inputGroup = document.getElementById('tagInputGroup');
        
        const tagDiv = document.createElement('span');
        tagDiv.className = 'badge me-2 mb-2 p-2 tag-wrapper';
        tagDiv.style.backgroundColor = '#5f3391'; // 与侧边栏一致的紫色
        tagDiv.style.fontSize = '14px';
        tagDiv.style.fontWeight = 'normal';
        tagDiv.innerHTML = `${val} <i class="bi bi-x-circle ms-1" style="cursor:pointer;" onclick="this.parentElement.remove()"></i>`;
        
        // 插入在输入框组之前
        container.insertBefore(tagDiv, inputGroup);
        input.value = '';
    }
}

function submitProject() {
    const title = document.getElementById('projectTitle');
    if (!title || !title.value.trim()) {
        alert('请输入项目名称！');
        return;
    }
    
    alert(`发布成功！新项目 "${title.value.trim()}" 已成功发布到系统中。`);
    // 假装发布完毕，跳回我的项目页面
    window.location.href = "projects.html";
}