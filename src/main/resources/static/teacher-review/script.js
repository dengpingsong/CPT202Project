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


// ----------------- 发布项目页逻辑 -----------------
const BASE_URL = "/api/teacher/projects";

function getAuthContext() {
    const token = localStorage.getItem("token") || "";
    const role = (localStorage.getItem("role") || "").toUpperCase();
    const userId = Number(localStorage.getItem("userId") || "0");
    return { token, role, userId };
}


// Admin创建后要激活该功能
function getTagList() {
    const wrappers = document.querySelectorAll("#tagsContainer .tag-wrapper");
    const tags = [];

    wrappers.forEach((tagEl) => {
        // 优先读 data-tag，避免读到 x 图标
        const fromData = (tagEl.dataset.tag || "").trim();
        if (fromData) {
            tags.push(fromData);
            return;
        }

        const text = (tagEl.textContent || "")
            .replace(/\s*[×xX]\s*$/, "")
            .trim();

        if (text) tags.push(text);
    });

    return tags;
}

function addTag() {
    const input = document.getElementById("tagInput");
    const container = document.getElementById("tagsContainer");
    const inputGroup = document.getElementById("tagInputGroup");

    if (!input || !container || !inputGroup) return;

    const value = input.value.trim();
    if (!value) return;

    // 防重复（大小写不敏感）
    const exists = Array.from(container.querySelectorAll(".tag-wrapper"))
        .some((el) => (el.dataset.tag || "").toLowerCase() === value.toLowerCase());
    if (exists) {
        input.value = "";
        return;
    }

    const tag = document.createElement("span");
    tag.className = "badge me-2 mb-2 p-2 tag-wrapper";
    tag.dataset.tag = value;
    tag.style.backgroundColor = "#5f3391";
    tag.style.fontSize = "14px";
    tag.style.fontWeight = "normal";
    tag.innerHTML = `${value} <i class="bi bi-x-circle ms-1" style="cursor:pointer;"></i>`;

    const removeIcon = tag.querySelector("i");
    if (removeIcon) {
        removeIcon.addEventListener("click", () => tag.remove());
    }

    container.insertBefore(tag, inputGroup);
    input.value = "";
}

async function submitProject() {
    const token = localStorage.getItem("token") || "";
    const role = (localStorage.getItem("role") || "").toUpperCase();
    const userId = Number(localStorage.getItem("userId") || "0");
    // 教师身份校验
    // if (!token || role !== "TEACHER" || !userId) {
    //     alert("请先以教师身份登录");
    //     window.location.href = "/login.html";
    //     return;
    // }

    // ProjectDTO
    const payload = {
        teacherId: userId,
        // categoryId应当由管理员创建，暂时这里先拿数字来替代。
        categoryId: Number(document.getElementById("projectCategoryId").value),
        title: document.getElementById("projectTitle").value.trim(),
        description: document.getElementById("projectDescription").value.trim(),
        requiredSkills: document.getElementById("projectRequirements").value.trim(),
        topicArea: document.getElementById("projectTopicArea").value.trim(),
        maxStudents: Number(document.getElementById("projectQuota").value)
    };

    // 基本校验（对应 DTO 注解）
    if (!payload.teacherId) return alert("teacherId 不能为空");
    if (!payload.categoryId) return alert("categoryId 不能为空");
    if (!payload.title) return alert("title 不能为空");
    if (!payload.description) return alert("description 不能为空");
    if (!payload.maxStudents || payload.maxStudents < 1) return alert("maxStudents 必须 >= 1");

    try {
        const res = await fetch(BASE_URL, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${token}`
            },
            body: JSON.stringify(payload)
        });

        const data = await res.json();

        if (res.ok && data.code === 1) {
            alert("发布成功");
            window.location.href = "projects.html";
        } else {
            alert(data.msg || "发布失败");
        }
    } catch (e) {
        console.error(e);
        alert("网络异常");
    }
}


// ----------------- 页面初始化 -----------------
// 后续再加上
// document.addEventListener("DOMContentLoaded", function () {
//     const tagInput = document.getElementById("tagInput");
//     if (tagInput) {
//         tagInput.addEventListener("keypress", function (e) {
//             if (e.key === "Enter") {
//                 e.preventDefault();
//                 addTag();
//             }
//         });
//     }
//
//     if (document.getElementById("createProjectForm")) {
//         ensureTeacherAuth();
//     }
// });
