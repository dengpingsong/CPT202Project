(function (window, document) {
    "use strict";

    if (window.UiFeedback) {
        return;
    }

    const STYLE_ID = "ui-feedback-style";
    const ROOT_ID = "ui-feedback-root";

    function ensureStyles() {
        if (document.getElementById(STYLE_ID)) {
            return;
        }
        const style = document.createElement("style");
        style.id = STYLE_ID;
        style.textContent = `
            #${ROOT_ID} {
                position: fixed;
                inset: 0;
                pointer-events: none;
                z-index: 9999;
                font-family: "Segoe UI", "Microsoft YaHei", Arial, sans-serif;
            }
            #${ROOT_ID} .ui-toast-stack {
                position: fixed;
                top: 24px;
                right: 24px;
                display: flex;
                flex-direction: column;
                gap: 12px;
                align-items: flex-end;
            }
            #${ROOT_ID} .ui-toast {
                min-width: 260px;
                max-width: min(420px, calc(100vw - 32px));
                background: #ffffff;
                border: 1px solid rgba(148, 163, 184, 0.28);
                box-shadow: 0 18px 45px rgba(15, 23, 42, 0.14);
                border-radius: 16px;
                padding: 14px 16px;
                color: #1e293b;
                display: flex;
                gap: 12px;
                align-items: flex-start;
                pointer-events: auto;
            }
            #${ROOT_ID} .ui-toast-accent {
                width: 4px;
                border-radius: 999px;
                align-self: stretch;
                background: #5b8def;
            }
            #${ROOT_ID} .ui-toast-success .ui-toast-accent { background: #22c55e; }
            #${ROOT_ID} .ui-toast-error .ui-toast-accent { background: #ef4444; }
            #${ROOT_ID} .ui-toast-warning .ui-toast-accent { background: #f59e0b; }
            #${ROOT_ID} .ui-toast-message {
                font-size: 14px;
                line-height: 1.6;
            }
            #${ROOT_ID} .ui-dialog-overlay {
                position: fixed;
                inset: 0;
                background: rgba(15, 23, 42, 0.38);
                display: flex;
                align-items: center;
                justify-content: center;
                padding: 20px;
                pointer-events: auto;
            }
            #${ROOT_ID} .ui-dialog {
                width: min(440px, 100%);
                background: #ffffff;
                border-radius: 22px;
                box-shadow: 0 24px 60px rgba(15, 23, 42, 0.2);
                border: 1px solid rgba(226, 232, 240, 0.9);
                overflow: hidden;
            }
            #${ROOT_ID} .ui-dialog-header {
                padding: 20px 22px 10px;
                display: flex;
                align-items: center;
                gap: 12px;
            }
            #${ROOT_ID} .ui-dialog-title {
                margin: 0;
                font-size: 18px;
                color: #0f172a;
                font-weight: 700;
                flex: 1;
            }
            #${ROOT_ID} .ui-dialog-body {
                padding: 0 22px 20px;
                color: #475569;
                font-size: 14px;
                line-height: 1.7;
            }
            #${ROOT_ID} .ui-dialog-input {
                width: 100%;
                margin-top: 14px;
                padding: 11px 14px;
                border-radius: 14px;
                border: 1px solid rgba(148, 163, 184, 0.45);
                outline: none;
                font: inherit;
                color: #0f172a;
                transition: border-color .2s ease, box-shadow .2s ease;
            }
            #${ROOT_ID} .ui-dialog-input:focus {
                border-color: #5b8def;
                box-shadow: 0 0 0 4px rgba(91, 141, 239, 0.14);
            }
            #${ROOT_ID} .ui-dialog-actions {
                display: flex;
                justify-content: flex-end;
                gap: 10px;
                padding: 0 22px 22px;
            }
            #${ROOT_ID} .ui-btn {
                border: none;
                border-radius: 999px;
                padding: 10px 18px;
                font: inherit;
                font-weight: 600;
                cursor: pointer;
            }
            #${ROOT_ID} .ui-btn-secondary {
                background: #eef2f7;
                color: #334155;
            }
            #${ROOT_ID} .ui-btn-primary {
                background: #5b8def;
                color: #ffffff;
            }
        `;
        document.head.appendChild(style);
    }

    function ensureRoot() {
        ensureStyles();
        let root = document.getElementById(ROOT_ID);
        if (!root) {
            root = document.createElement("div");
            root.id = ROOT_ID;
            root.innerHTML = '<div class="ui-toast-stack"></div>';
            document.body.appendChild(root);
        }
        return root;
    }

    function esc(value) {
        return String(value == null ? "" : value)
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#39;");
    }

    function toast(message, options) {
        const root = ensureRoot();
        const stack = root.querySelector(".ui-toast-stack");
        const type = options?.type || "info";
        const duration = options?.duration ?? 2600;
        const item = document.createElement("div");
        item.className = `ui-toast ui-toast-${type}`;
        item.innerHTML = `
            <div class="ui-toast-accent"></div>
            <div class="ui-toast-message">${esc(message)}</div>
        `;
        stack.appendChild(item);
        window.setTimeout(() => {
            item.remove();
        }, duration);
    }

    function openDialog(config) {
        const root = ensureRoot();
        return new Promise(resolve => {
            const overlay = document.createElement("div");
            overlay.className = "ui-dialog-overlay";
            overlay.innerHTML = `
                <div class="ui-dialog" role="dialog" aria-modal="true" aria-labelledby="uiDialogTitle">
                    <div class="ui-dialog-header">
                        <h3 class="ui-dialog-title" id="uiDialogTitle">${esc(config.title || "提示")}</h3>
                    </div>
                    <div class="ui-dialog-body">
                        <div>${esc(config.message || "")}</div>
                        ${config.mode === "prompt" ? `<input class="ui-dialog-input" value="${esc(config.defaultValue || "")}" placeholder="${esc(config.placeholder || "")}">` : ""}
                    </div>
                    <div class="ui-dialog-actions">
                        <button type="button" class="ui-btn ui-btn-secondary" data-action="cancel">${esc(config.cancelText || "取消")}</button>
                        <button type="button" class="ui-btn ui-btn-primary" data-action="confirm">${esc(config.confirmText || "确定")}</button>
                    </div>
                </div>
            `;

            function close(result) {
                overlay.remove();
                resolve(result);
            }

            overlay.addEventListener("click", event => {
                if (event.target === overlay) {
                    close(config.mode === "alert" ? undefined : config.mode === "confirm" ? false : null);
                }
            });

            overlay.querySelector('[data-action="cancel"]').addEventListener("click", () => {
                close(config.mode === "alert" ? undefined : config.mode === "confirm" ? false : null);
            });
            overlay.querySelector('[data-action="confirm"]').addEventListener("click", () => {
                if (config.mode === "prompt") {
                    close(overlay.querySelector(".ui-dialog-input").value);
                    return;
                }
                close(config.mode === "confirm" ? true : undefined);
            });

            root.appendChild(overlay);
            const input = overlay.querySelector(".ui-dialog-input");
            if (input) {
                input.focus();
                input.select();
                input.addEventListener("keydown", event => {
                    if (event.key === "Enter") {
                        overlay.querySelector('[data-action="confirm"]').click();
                    }
                });
            } else {
                overlay.querySelector('[data-action="confirm"]').focus();
            }
        });
    }

    window.UiFeedback = {
        toast,
        success(message, duration) {
            toast(message, { type: "success", duration });
        },
        error(message, duration) {
            toast(message, { type: "error", duration });
        },
        warning(message, duration) {
            toast(message, { type: "warning", duration });
        },
        alert(message, title) {
            return openDialog({
                mode: "alert",
                title: title || "提示",
                message,
                cancelText: "关闭",
                confirmText: "确定"
            });
        },
        confirm(message, title, confirmText, cancelText) {
            return openDialog({
                mode: "confirm",
                title: title || "确认操作",
                message,
                confirmText: confirmText || "确定",
                cancelText: cancelText || "取消"
            });
        },
        prompt(message, title, defaultValue, placeholder) {
            return openDialog({
                mode: "prompt",
                title: title || "请输入",
                message,
                defaultValue: defaultValue || "",
                placeholder: placeholder || "",
                confirmText: "确定",
                cancelText: "取消"
            });
        }
    };
})(window, document);
