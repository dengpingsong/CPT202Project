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