/**
 * Shared frontend API helper for static pages.
 *
 * The backend wraps every response as:
 * { code: 1 | 0, msg: string | null, data: any }
 */
(function (window) {
    "use strict";

    const LOGIN_PAGE = "/login/login.html";

    function getToken() {
        return localStorage.getItem("token") || "";
    }

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

    function clearAuth() {
        localStorage.removeItem("token");
        localStorage.removeItem("role");
        localStorage.removeItem("userId");
        localStorage.removeItem("username");
        localStorage.removeItem("fullName");
        localStorage.removeItem("accountStatus");
    }

    function requireAuth(expectedRole) {
        const user = getCurrentUser();

        if (!user.token) {
            window.location.href = LOGIN_PAGE;
            return null;
        }

        if (expectedRole && user.role !== expectedRole.toUpperCase()) {
            throw new Error("Permission denied for current role.");
        }

        return user;
    }

    async function parseResponse(response) {
        const text = await response.text();
        if (!text) return null;

        try {
            return JSON.parse(text);
        } catch (error) {
            throw new Error("Invalid server response.");
        }
    }

    async function request(url, options) {
        const config = options || {};
        const token = getToken();
        const headers = new Headers(config.headers || {});

        if (!headers.has("Content-Type") && config.body !== undefined) {
            headers.set("Content-Type", "application/json");
        }

        if (token && !headers.has("Authorization")) {
            headers.set("Authorization", `Bearer ${token}`);
        }

        const response = await fetch(url, {
            ...config,
            headers
        });

        if (response.status === 401) {
            clearAuth();
            window.location.href = LOGIN_PAGE;
            throw new Error("Login expired. Please log in again.");
        }

        const result = await parseResponse(response);

        if (!response.ok) {
            throw new Error(result?.msg || `Request failed with status ${response.status}.`);
        }

        if (result && Object.prototype.hasOwnProperty.call(result, "code")) {
            if (result.code !== 1) {
                throw new Error(result.msg || "Request failed.");
            }

            return result.data;
        }

        return result;
    }

    function withJsonBody(method, url, body, options) {
        return request(url, {
            ...(options || {}),
            method,
            body: JSON.stringify(body || {})
        });
    }

    window.ApiClient = {
        getToken,
        getCurrentUser,
        clearAuth,
        requireAuth,
        request,
        get(url, options) {
            return request(url, {
                ...(options || {}),
                method: "GET"
            });
        },
        post(url, body, options) {
            return withJsonBody("POST", url, body, options);
        },
        put(url, body, options) {
            return withJsonBody("PUT", url, body, options);
        },
        delete(url, options) {
            return request(url, {
                ...(options || {}),
                method: "DELETE"
            });
        }
    };
})(window);
