//登录状态：继续用 localStorage
// 业务数据：全部从后端 API 获取
(function (window) {
    "use strict";

    const LOGIN_PAGE = "/login/login.html";//login address

    //Retrieve the saved JWT token from the browser's localStorage after login, and use it for subsequent backend API requests.
    function getToken() {
        return localStorage.getItem("token") || "";
    }

    //get users information
    function getCurrentUser() {
        return {
            token: getToken(),
            userId: Number(localStorage.getItem("userId") || "0"),
            username: localStorage.getItem("username") || "",
            fullName: localStorage.getItem("fullName") || "",
            role: (localStorage.getItem("role") || "").toUpperCase(),
            accountStatus: localStorage.getItem("accountStatus") || ""
        };
    }

    //clear login status
    function clearAuth() {
        localStorage.removeItem("token");
        localStorage.removeItem("role");
        localStorage.removeItem("userId");
        localStorage.removeItem("username");
        localStorage.removeItem("fullName");
        localStorage.removeItem("accountStatus");
    }

    //页面权限检查
    function requireAuth(expectedRole) {
        const user = getCurrentUser();

        //return to login page
        if (!user.token) {
            window.location.href = LOGIN_PAGE;
            return null;
        }

        if (expectedRole && user.role !== expectedRole.toUpperCase()) {
            throw new Error("Permission denied for current role.");
        }

        return user;
    }

    //解析 JSON
    async function parseResponse(response) {
        return await response.json();
    }
    //get() post() put() delete()
    async function request(url, options) {
        const config = options || {};
        const token = getToken();
        const headers = new Headers(config.headers || {});

        //fill Content-Type
        if (!headers.has("Content-Type") && config.body !== undefined) {
            headers.set("Content-Type", "application/json");
        }

        //自动添加token
        if (token && !headers.has("Authorization")) {
            headers.set("Authorization", `Bearer ${token}`);
        }

        //******core******
        const response = await fetch(url, {
            ...config,
            headers
        });

        //401 situation
        if (response.status === 401) {
            clearAuth();
            window.location.href = LOGIN_PAGE;
            throw new Error("Login expired. Please log in again.");
        }

        //json response
        const result = await parseResponse(response);

        if (!response.ok) {
            throw new Error(result?.msg || `Request failed with status ${response.status}.`);
        }

        if (result && result.code !== undefined) {
            if (result.code !== 1) {
                throw new Error(result.msg || "Request failed.");
            }

            return result.data;
        }

        return result;
    }

    //turn json
    function sendJsonRequest(method, url, body, options) {
        return request(url, {
            ...(options || {}),
            method: method,
            body: JSON.stringify(body)
        });
    }

    const teacherApi = {
        listProjects(status) {
            const params = new URLSearchParams();
            if (status) {
                params.set("status", status);
            }
            const query = params.toString();
            return request(`/api/teacher/projects${query ? `?${query}` : ""}`, {
                method: "GET"
            });
        },
        getProject(projectId) {
            return request(`/api/teacher/projects/${encodeURIComponent(projectId)}`, {
                method: "GET"
            });
        },
        createProject(payload) {
            return sendJsonRequest("POST", "/api/teacher/projects", payload);
        },
        updateProject(projectId, payload) {
            return sendJsonRequest("PUT", `/api/teacher/projects/${encodeURIComponent(projectId)}`, payload);
        },
        changeProjectStatus(projectId, projectStatus, remark) {
            return sendJsonRequest("PUT", `/api/teacher/projects/${encodeURIComponent(projectId)}/status`, {
                projectStatus,
                remark: remark || ""
            });
        },
        listCategories() {
            return request("/api/teacher/categories", {
                method: "GET"
            });
        },
        listTags() {
            return request("/api/teacher/tags", {
                method: "GET"
            });
        },
        listProjectTags(projectId) {
            return request(`/api/teacher/project-tags/${encodeURIComponent(projectId)}`, {
                method: "GET"
            });
        },
        bindProjectTags(projectId, tagIds) {
            return sendJsonRequest("PUT", `/api/teacher/project-tags/${encodeURIComponent(projectId)}`, {
                tagIds
            });
        },
        listRequests(status) {
            const params = new URLSearchParams();
            if (status) {
                params.set("status", status);
            }
            const query = params.toString();
            return request(`/api/teacher/requests${query ? `?${query}` : ""}`, {
                method: "GET"
            });
        },
        listHistory() {
            return request("/api/teacher/requests", {
                method: "GET"
            });
        },
        listNotifications() {
            return request("/api/teacher/requests", {
                method: "GET"
            });
        },
        getProfile() {
            return request("/api/teacher/profile/me", {
                method: "GET"
            });
        },
        updateProfile(payload) {
            return sendJsonRequest("PUT", "/api/teacher/profile/me", payload);
        },
        changePassword(oldPassword, newPassword) {
            return sendJsonRequest("PUT", "/api/teacher/profile/me/password", {
                oldPassword,
                newPassword
            });
        },
        reviewRequest(requestId, requestStatus, decisionComment) {
            return sendJsonRequest("PUT", `/api/teacher/requests/${encodeURIComponent(requestId)}/review`, {
                requestStatus,
                decisionComment: decisionComment || ""
            });
        }
    };

    const studentApi = {
        withdrawRequest(requestId) {
            return request(`/api/student/requests/${encodeURIComponent(requestId)}/withdraw`, {
                method: "PUT"
            });
        }
    };

    const adminApi = {
        listUsers(role, accountStatus) {
            const params = new URLSearchParams();
            if (role) params.set("role", role);
            if (accountStatus) params.set("accountStatus", accountStatus);
            const query = params.toString();
            return request(`/api/admin/users${query ? `?${query}` : ""}`, {
                method: "GET"
            });
        },
        updateUserStatus(userId, accountStatus) {
            const params = new URLSearchParams({ accountStatus });
            return request(`/api/admin/users/${encodeURIComponent(userId)}/status?${params.toString()}`, {
                method: "PUT"
            });
        },
        listRequestRecords(status) {
            const params = new URLSearchParams();
            if (status) params.set("status", status);
            const query = params.toString();
            return request(`/api/admin/records/requests${query ? `?${query}` : ""}`, {
                method: "GET"
            });
        },
        listRequestHistoryRecords() {
            return request("/api/admin/records/request-history", {
                method: "GET"
            });
        },
        getProfile() {
            return request("/api/admin/profile/me", {
                method: "GET"
            });
        },
        updateProfile(payload) {
            return sendJsonRequest("PUT", "/api/admin/profile/me", payload);
        },
        changePassword(payload) {
            return sendJsonRequest("PUT", "/api/admin/profile/me/password", payload);
        }
    };

    window.ApiClient = {
        getToken,
        getCurrentUser,
        clearAuth,
        requireAuth,
        request,
        teacher: teacherApi,
        student: studentApi,
        admin: adminApi,
        // logout function: notify backend, then clear token and redirect to login page
        async logout() {
            try {
                await request("/api/common/auth/logout", {
                    method: "POST"
                });
            } catch (error) {
                console.warn("Logout request failed, clearing local session anyway.", error);
            } finally {
                clearAuth();
                window.location.href = LOGIN_PAGE;
            }
        },
        get(url, options) {
            return request(url, {
                ...(options || {}),
                method: "GET"
            });
        },
        post(url, body, options) {
            return sendJsonRequest("POST", url, body, options);
        },
        put(url, body, options) {
            return sendJsonRequest("PUT", url, body, options);
        },
        delete(url, options) {
            return request(url, {
                ...(options || {}),
                method: "DELETE"
            });
        }
    };
})(window);//package this part
