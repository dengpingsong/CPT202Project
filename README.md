# CPT202Program

基于 **Spring Boot 3** 构建的毕业设计选题系统后端，支持学生、教师与管理员三种角色，涵盖项目发布、申请审核、标签分类等完整业务流程。

---

## 技术栈

| 层次 | 技术 |
|------|------|
| 语言 | Java 17 |
| 框架 | Spring Boot 3.2（Spring MVC、Spring Data JPA） |
| 数据库 | H2 in-memory（开发/CI）；可切换 MySQL（生产） |
| ORM | Hibernate / Spring Data JPA |
| API 文档 | Knife4j 4.4（OpenAPI 3） |
| 工具库 | Lombok、Spring Validation |
| 测试 | JUnit 5 · Mockito · Spring MockMvc |
| 构建 | Maven 3 |
| CI/CD | GitHub Actions |

---

## 目录结构

```
CPT202Program/
├── .github/workflows/ci.yml           # CI/CD 流水线
├── src/
│   ├── main/java/com/cpt202/
│   │   ├── CPT202Application.java     # 启动入口
│   │   ├── controller/
│   │   │   ├── common/                # 公共接口（注册、登录、登出）
│   │   │   ├── admin/                 # 管理员接口（用户管理、分类、标签、记录查询）
│   │   │   ├── student/               # 学生接口（资料、浏览项目、提交/撤回申请）
│   │   │   └── teacher/               # 教师接口（资料、项目 CRUD、审核申请、标签绑定）
│   │   ├── service/                   # 业务接口层
│   │   │   └── impl/                  # 业务实现层
│   │   ├── model/entity/              # JPA 实体（11 个）
│   │   ├── repository/                # Spring Data 仓库（9 个）
│   │   ├── dto/                       # 请求 DTO（17 个）
│   │   ├── vo/                        # 响应 VO（10 个）
│   │   ├── result/Result.java         # 统一响应包装
│   │   ├── exception/                 # 业务异常、规则异常
│   │   └── handler/                   # 全局异常处理器
│   └── resources/
│       ├── application.properties
│       └── firstPage_front/           # 静态前端页面（登录、注册、忘记密码）
└── pom.xml
```

---

## 核心业务模块

### 角色与权限

| 角色 | 主要职责 |
|------|----------|
| `STUDENT` | 浏览/搜索项目，提交选题申请，查看申请历史 |
| `TEACHER` | 发布/管理项目，审核学生申请，绑定项目标签 |
| `ADMIN` | 管理用户账号状态，维护分类与标签，查看全量记录 |

### API 路由概览

| 前缀 | 说明 |
|------|------|
| `POST /api/common/auth/register` | 注册（支持学生/教师角色） |
| `POST /api/common/auth/login` | 登录 |
| `GET/PUT /api/student/profile/{id}` | 学生资料查询与更新 |
| `GET /api/student/projects` | 浏览项目（关键字/分类/状态过滤） |
| `POST /api/student/requests` | 提交选题申请 |
| `PUT /api/student/requests/{id}/withdraw` | 撤回申请 |
| `GET/PUT /api/teacher/profile/{id}` | 教师资料查询与更新 |
| `GET/POST/PUT /api/teacher/projects` | 项目 CRUD 及状态变更 |
| `PUT /api/teacher/requests/{id}/review` | 审核学生申请 |
| `PUT /api/teacher/project-tags/{projectId}` | 重绑定项目标签 |
| `GET/POST/PUT/DELETE /api/admin/categories` | 分类管理 |
| `GET/POST/PUT/DELETE /api/admin/tags` | 标签管理 |
| `GET /api/admin/users` | 用户列表（角色/状态过滤） |
| `PUT /api/admin/users/{id}/status` | 修改账号状态 |

### 选题业务规则（Module8）

- 申请截止日期校验（硬编码 `2026-05-29 23:59`）
- 一名学生只可持有一个 `PENDING` 或 `ACCEPTED` 申请
- 审核通过后，该学生其余 `PENDING` 申请自动置为 `REJECTED`
- 项目录取人数达到上限后自动置为 `CLOSED`

---

## 数据模型

| 实体 | 表名 | 说明 |
|------|------|------|
| `User` | `users` | 通用账号，含角色与账号状态 |
| `StudentProfile` | `student_profile` | 学生扩展信息，入学日期动态计算学年 |
| `TeacherProfile` | `teacher_profile` | 教师扩展信息（院系、职称、研究方向） |
| `Project` | `project` | 选题项目，状态流转：AVAILABLE → REQUESTED → AGREED → CLOSED/ARCHIVED |
| `ProjectRequest` | `project_request` | 选题申请，状态：PENDING / ACCEPTED / REJECTED / WITHDRAWN |
| `Category` | `category` | 项目分类 |
| `Tag` / `ProjectTag` | `tag` / `project_tag` | 标签与项目多对多关联 |
| `ProjectStatusHistory` | `project_status_history` | 项目状态变更历史 |
| `RequestStatusHistory` | `request_status_history` | 申请状态变更历史 |

---

## 快速启动

### 前置条件

- Java 17+
- Maven 3.9+

### 本地运行

```bash
mvn spring-boot:run
```

服务启动于 **http://localhost:8080**，H2 控制台可通过 `/h2-console` 访问。

### API 文档

启动后访问 **http://localhost:8080/doc.html**（Knife4j UI）。

### 运行测试

```bash
mvn test
```

---

## CI/CD 流水线

GitHub Actions（`.github/workflows/ci.yml`）在每次推送或 PR 至 `main` / `master` / `develop` 时触发：

1. **Build** — Maven 编译项目
2. **Test** — 执行全部 JUnit 5 测试，上传 Surefire 报告
3. **Package** *(仅 main/master)* — 打包可执行 JAR 并上传为构建产物

