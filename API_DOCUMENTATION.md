# CPT202 项目接口文档

版本：`0.1.0`

说明：
- 版本号采用 GNU 版本规则：`主版本号.次版本号.修订号`
- `0.1.0` 表示接口骨架已基本成型，但业务实现仍处于早期阶段

## 1. 基本约定

### 1.1 基础路径

所有接口统一以 `/api` 作为前缀。

### 1.2 统一响应结构

所有接口统一使用 `Result<T>` 作为响应包装对象：

```json
{
  "code": 1,
  "msg": null,
  "data": {}
}
```

字段说明：
- `code = 1`：请求成功
- `code = 0`：业务失败
- `msg`：错误消息，成功时通常为 `null`
- `data`：实际返回数据

### 1.3 接口设计规范

- 查询类接口返回业务数据，如 `Result<VO>`、`Result<List<VO>>`
- 新增、修改、删除、审核、绑定等命令类接口统一返回 `Result<Void>`
- 当前部分接口仍通过参数显式传递 `studentId`、`teacherId`
后续若接入 JWT / 登录态，应从认证上下文中获取操作者身份

## 2. 公共认证接口

接口前缀：`/api/common/auth`

### 2.1 用户注册

- 请求方式：`POST`
- 请求路径：`/api/common/auth/register`
- 接口说明：注册新用户

请求体：

```json
{
  "username": "alice",
  "password": "123456",
  "email": "alice@example.com",
  "fullName": "Alice",
  "role": "STUDENT",
  "studentNo": "20240001",
  "programme": "Software Engineering",
  "academicYear": 3,
  "phone": "13800000000",
  "interests": "AI"
}
```

成功响应：

```json
{
  "code": 1,
  "msg": null,
  "data": {
    "userId": 1,
    "username": "alice",
    "fullName": "Alice",
    "role": "STUDENT",
    "accountStatus": "ACTIVE"
  }
}
```

### 2.2 用户登录

- 请求方式：`POST`
- 请求路径：`/api/common/auth/login`
- 接口说明：用户登录

请求体：

```json
{
  "username": "alice",
  "password": "123456"
}
```

成功响应：

```json
{
  "code": 1,
  "msg": null,
  "data": {
    "userId": 1,
    "username": "alice",
    "fullName": "Alice",
    "role": "STUDENT",
    "accountStatus": "ACTIVE"
  }
}
```

### 2.3 用户退出

- 请求方式：`POST`
- 请求路径：`/api/common/auth/logout`
- 接口说明：退出登录

成功响应：

```json
{
  "code": 1,
  "msg": null,
  "data": null
}
```

## 3. 管理端用户接口

接口前缀：`/api/admin/users`

### 3.1 查询用户列表

- 请求方式：`GET`
- 请求路径：`/api/admin/users`
- 接口说明：按角色和账号状态筛选用户列表

查询参数：
- `role`：用户角色，可选，取值为 `ADMIN` / `TEACHER` / `STUDENT`
- `accountStatus`：账号状态，可选

成功响应：

```json
{
  "code": 1,
  "msg": null,
  "data": [
    {
      "userId": 1,
      "username": "alice",
      "email": "alice@example.com",
      "fullName": "Alice",
      "role": "STUDENT",
      "accountStatus": "ACTIVE"
    }
  ]
}
```

### 3.2 修改用户状态

- 请求方式：`PUT`
- 请求路径：`/api/admin/users/{userId}/status`
- 接口说明：修改指定用户账号状态

查询参数：
- `accountStatus`：目标账号状态

成功响应：

```json
{
  "code": 1,
  "msg": null,
  "data": null
}
```

## 4. 管理端分类接口

接口前缀：`/api/admin/categories`

### 4.1 查询分类列表

- 请求方式：`GET`
- 请求路径：`/api/admin/categories`
- 接口说明：查询全部分类列表

成功响应：

```json
{
  "code": 1,
  "msg": null,
  "data": [
    {
      "categoryId": 1,
      "categoryName": "AI",
      "description": "Artificial Intelligence",
      "createdAt": "2026-04-05T12:00:00",
      "updatedAt": "2026-04-05T12:00:00"
    }
  ]
}
```

### 4.2 查询分类详情

- 请求方式：`GET`
- 请求路径：`/api/admin/categories/{categoryId}`
- 接口说明：根据分类主键查询详情

### 4.3 新增分类

- 请求方式：`POST`
- 请求路径：`/api/admin/categories`
- 接口说明：新增分类

请求体：

```json
{
  "categoryName": "AI",
  "description": "Artificial Intelligence"
}
```

成功响应：

```json
{
  "code": 1,
  "msg": null,
  "data": null
}
```

### 4.4 修改分类

- 请求方式：`PUT`
- 请求路径：`/api/admin/categories/{categoryId}`
- 接口说明：修改指定分类

请求体：

```json
{
  "categoryName": "AI",
  "description": "Updated description"
}
```

### 4.5 删除分类

- 请求方式：`DELETE`
- 请求路径：`/api/admin/categories/{categoryId}`
- 接口说明：删除指定分类

## 5. 管理端标签接口

接口前缀：`/api/admin/tags`

### 5.1 查询标签列表

- 请求方式：`GET`
- 请求路径：`/api/admin/tags`
- 接口说明：查询全部标签

成功响应：

```json
{
  "code": 1,
  "msg": null,
  "data": [
    {
      "tagId": 1,
      "tagName": "Machine Learning",
      "description": "ML related",
      "createdAt": "2026-04-05T12:00:00",
      "updatedAt": "2026-04-05T12:00:00"
    }
  ]
}
```

### 5.2 查询标签详情

- 请求方式：`GET`
- 请求路径：`/api/admin/tags/{tagId}`
- 接口说明：根据标签主键查询详情

### 5.3 新增标签

- 请求方式：`POST`
- 请求路径：`/api/admin/tags`
- 接口说明：新增标签

请求体：

```json
{
  "tagName": "Machine Learning",
  "description": "ML related"
}
```

### 5.4 修改标签

- 请求方式：`PUT`
- 请求路径：`/api/admin/tags/{tagId}`
- 接口说明：修改标签

### 5.5 删除标签

- 请求方式：`DELETE`
- 请求路径：`/api/admin/tags/{tagId}`
- 接口说明：删除标签

## 6. 管理端记录接口

接口前缀：`/api/admin/records`

### 6.1 查询项目记录

- 请求方式：`GET`
- 请求路径：`/api/admin/records/projects`
- 接口说明：查询项目记录列表

### 6.2 查询申请记录

- 请求方式：`GET`
- 请求路径：`/api/admin/records/requests`
- 接口说明：查询申请记录列表

查询参数：
- `status`：申请状态，可选，取值为 `PENDING` / `ACCEPTED` / `REJECTED` / `WITHDRAWN`

### 6.3 查询申请历史记录

- 请求方式：`GET`
- 请求路径：`/api/admin/records/request-history`
- 接口说明：查询申请历史记录列表

## 7. 学生端项目接口

接口前缀：`/api/student/projects`

### 7.1 查询项目列表

- 请求方式：`GET`
- 请求路径：`/api/student/projects`
- 接口说明：查询学生可见项目列表

查询参数：
- `keyword`：关键字，可选
- `categoryId`：分类主键，可选
- `status`：项目状态，可选，取值为 `AVAILABLE` / `REQUESTED` / `AGREED` / `CLOSED` / `ARCHIVED`

成功响应：

```json
{
  "code": 1,
  "msg": null,
  "data": [
    {
      "projectId": 1,
      "teacherId": 10,
      "teacherName": "Dr. Smith",
      "categoryId": 2,
      "categoryName": "AI",
      "title": "AI Research Project",
      "description": "Project description",
      "requiredSkills": "Java, Python",
      "topicArea": "Computer Vision",
      "maxStudents": 3,
      "currentAgreedCount": 1,
      "projectStatus": "AVAILABLE",
      "publishDate": "2026-04-05T12:00:00",
      "closeDate": "2026-05-01T12:00:00"
    }
  ]
}
```

### 7.2 查询项目详情

- 请求方式：`GET`
- 请求路径：`/api/student/projects/{projectId}`
- 接口说明：查询项目详情

## 8. 学生端申请接口

接口前缀：`/api/student/requests`

### 8.1 查询学生申请列表

- 请求方式：`GET`
- 请求路径：`/api/student/requests`
- 接口说明：查询某个学生的申请列表

查询参数：
- `studentId`：学生主键

成功响应：

```json
{
  "code": 1,
  "msg": null,
  "data": [
    {
      "requestId": 1,
      "projectId": 1,
      "projectTitle": "AI Research Project",
      "studentId": 100,
      "studentName": "Alice",
      "reviewedByTeacherId": 10,
      "preferenceRank": 1,
      "notes": "I am very interested in this topic.",
      "requestStatus": "PENDING",
      "decisionComment": null,
      "submittedAt": "2026-04-05T12:00:00",
      "reviewedAt": null,
      "withdrawnAt": null
    }
  ]
}
```

### 8.2 提交项目申请

- 请求方式：`POST`
- 请求路径：`/api/student/requests`
- 接口说明：提交新的项目申请

请求体：

```json
{
  "projectId": 1,
  "studentId": 100,
  "preferenceRank": 1,
  "notes": "I am very interested in this topic."
}
```

成功响应：

```json
{
  "code": 1,
  "msg": null,
  "data": null
}
```

### 8.3 撤回项目申请

- 请求方式：`PUT`
- 请求路径：`/api/student/requests/{requestId}/withdraw`
- 接口说明：撤回指定申请

查询参数：
- `studentId`：学生主键

## 9. 学生端资料接口

接口前缀：`/api/student/profile`

### 9.1 查询学生资料

- 请求方式：`GET`
- 请求路径：`/api/student/profile/{studentId}`
- 接口说明：查询学生资料详情

成功响应：

```json
{
  "code": 1,
  "msg": null,
  "data": {
    "studentId": 100,
    "username": "alice",
    "email": "alice@example.com",
    "fullName": "Alice",
    "studentNo": "20240001",
    "programme": "Software Engineering",
    "academicYear": 3,
    "phone": "13800000000",
    "interests": "AI",
    "updatedAt": "2026-04-05T12:00:00"
  }
}
```

### 9.2 修改学生资料

- 请求方式：`PUT`
- 请求路径：`/api/student/profile/{studentId}`
- 接口说明：修改学生资料

请求体：

```json
{
  "fullName": "Alice",
  "email": "alice@example.com",
  "programme": "Software Engineering",
  "academicYear": 3,
  "phone": "13800000000",
  "interests": "AI"
}
```

成功响应：

```json
{
  "code": 1,
  "msg": null,
  "data": null
}
```

## 10. 学生端申请历史接口

接口前缀：`/api/student/request-history`

### 10.1 查询申请状态历史

- 请求方式：`GET`
- 请求路径：`/api/student/request-history/{requestId}`
- 接口说明：查询指定申请的状态历史

成功响应：

```json
{
  "code": 1,
  "msg": null,
  "data": [
    {
      "historyId": 1,
      "requestId": 1,
      "oldStatus": "PENDING",
      "newStatus": "WITHDRAWN",
      "changedByStudentId": 100,
      "changedByStudentName": "Alice",
      "remark": "Student withdrew the request",
      "changedAt": "2026-04-05T12:00:00"
    }
  ]
}
```

## 11. 教师端项目接口

接口前缀：`/api/teacher/projects`

### 11.1 查询教师项目列表

- 请求方式：`GET`
- 请求路径：`/api/teacher/projects`
- 接口说明：查询教师本人项目列表

查询参数：
- `teacherId`：教师主键
- `status`：项目状态，可选

### 11.2 查询项目详情

- 请求方式：`GET`
- 请求路径：`/api/teacher/projects/{projectId}`
- 接口说明：查询项目详情

### 11.3 新增项目

- 请求方式：`POST`
- 请求路径：`/api/teacher/projects`
- 接口说明：新增项目

请求体：

```json
{
  "teacherId": 10,
  "categoryId": 2,
  "title": "AI Research Project",
  "description": "Project description",
  "requiredSkills": "Java, Python",
  "topicArea": "Computer Vision",
  "maxStudents": 3
}
```

### 11.4 修改项目

- 请求方式：`PUT`
- 请求路径：`/api/teacher/projects/{projectId}`
- 接口说明：修改项目

### 11.5 修改项目状态

- 请求方式：`PUT`
- 请求路径：`/api/teacher/projects/{projectId}/status`
- 接口说明：修改项目状态

请求体：

```json
{
  "teacherId": 10,
  "projectStatus": "CLOSED",
  "remark": "Recruitment completed"
}
```

命令类响应：

```json
{
  "code": 1,
  "msg": null,
  "data": null
}
```

## 12. 教师端申请审核接口

接口前缀：`/api/teacher/requests`

### 12.1 查询教师申请列表

- 请求方式：`GET`
- 请求路径：`/api/teacher/requests`
- 接口说明：查询教师待审核或指定状态的申请列表

查询参数：
- `teacherId`：教师主键
- `status`：申请状态，可选

### 12.2 审核项目申请

- 请求方式：`PUT`
- 请求路径：`/api/teacher/requests/{requestId}/review`
- 接口说明：审核指定申请

请求体：

```json
{
  "teacherId": 10,
  "requestStatus": "ACCEPTED",
  "decisionComment": "Accepted for interview"
}
```

成功响应：

```json
{
  "code": 1,
  "msg": null,
  "data": null
}
```

## 13. 教师端项目标签接口

接口前缀：`/api/teacher/project-tags`

### 13.1 查询项目标签

- 请求方式：`GET`
- 请求路径：`/api/teacher/project-tags/{projectId}`
- 接口说明：查询项目已绑定标签

成功响应：

```json
{
  "code": 1,
  "msg": null,
  "data": [
    {
      "projectId": 1,
      "tagId": 2,
      "tagName": "Machine Learning"
    }
  ]
}
```

### 13.2 绑定项目标签

- 请求方式：`PUT`
- 请求路径：`/api/teacher/project-tags/{projectId}`
- 接口说明：为项目重新绑定标签

请求体：

```json
{
  "teacherId": 10,
  "tagIds": [1, 2, 3]
}
```

成功响应：

```json
{
  "code": 1,
  "msg": null,
  "data": null
}
```

## 14. 教师端资料接口

接口前缀：`/api/teacher/profile`

### 14.1 查询教师资料

- 请求方式：`GET`
- 请求路径：`/api/teacher/profile/{teacherId}`
- 接口说明：查询教师资料详情

成功响应：

```json
{
  "code": 1,
  "msg": null,
  "data": {
    "teacherId": 10,
    "username": "smith",
    "email": "smith@example.com",
    "fullName": "Dr. Smith",
    "staffNo": "T2024001",
    "department": "Computer Science",
    "title": "Associate Professor",
    "researchArea": "Machine Learning",
    "office": "BS401",
    "updatedAt": "2026-04-05T12:00:00"
  }
}
```

### 14.2 修改教师资料

- 请求方式：`PUT`
- 请求路径：`/api/teacher/profile/{teacherId}`
- 接口说明：修改教师资料

请求体：

```json
{
  "fullName": "Dr. Smith",
  "email": "smith@example.com",
  "department": "Computer Science",
  "title": "Associate Professor",
  "researchArea": "Machine Learning",
  "office": "BS401"
}
```

成功响应：

```json
{
  "code": 1,
  "msg": null,
  "data": null
}
```

---

当前文档为接口骨架文档，具体错误码、鉴权规则、字段长度限制等可在业务落地后继续补充
