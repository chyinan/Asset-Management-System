## 资产管理系统 (Asset Management System)

一个覆盖资产全生命周期（申请 → 审批 → 采购 → 入库 → 领用 → 归还）的前后端分离项目

### 技术栈

- **后端**：Spring Boot 3.4、Spring Security、JJWT、Spring Data JPA、Flyway、MySQL、Spring Mail (JavaMailSender)
- **前端**：Vue 3 + Vite + TypeScript + Element Plus + Pinia + Vue Router
- **测试**：JUnit 5、MockMvc、Vue TSC
- **其他**：OpenAPI(Swagger UI)、Docker、GitHub Actions

---

### 目录结构

```
backend/asset-backend     # Spring Boot 服务
frontend/asset-frontend   # Vue3 + Element Plus 前端
infra/postman             # Postman/Insomnia 示例
docker-compose.yml        # 一键启动 MySQL + 后端 + 前端
README.md                 # 当前文档
```

---

### 快速开始

#### 1. 启动数据库 + 后端 + 前端（推荐 Docker）

```bash
cp docker/.env.example .env           # 可选，覆盖默认环境
docker compose up -d                  # 启动 mysql + backend + frontend
# 前端将监听 0.0.0.0:80
# 后端将监听 0.0.0.0:8080
```

手动方式：

```bash
cd backend/asset-backend
./mvnw spring-boot:run \
  -Dspring-boot.run.jvmArguments="\
    -DDB_URL=jdbc:mysql://localhost:3306/asset \
    -DDB_USERNAME=root \
    -DDB_PASSWORD=123456 \
    -DJWT_SECRET=popcap"
```

手动方式（IntelliJ IDEA）：

1. **导入项目**：打开 IDEA，选择 `Open`，指向 `backend/asset-backend` 目录（识别为 Maven 项目）。
2. **配置数据库**：确保本地 MySQL 已启动（端口 3306，账号 root/123456）且存在 `asset` 库。
   - 若使用 Docker 启动的 MySQL，会自动初始化，无需额外配置。
   - 若需修改数据库配置，可在 Run Configuration 的 Environment variables 中添加 `DB_PASSWORD=your_password` 等。
3. **运行**：找到 `src/main/java/com/project/asset/AssetBackendApplication.java`，点击运行（Run）。

Flyway 会在首次启动时自动创建并初始化数据库（含种子数据、角色、权限、示例资产等）。

#### 2. 启动前端（手动开发模式）

> **Node.js 要求**：22.12 及以上（或 20.19+）。项目当前在 Node 22.11.0 下会收到 Vite 的版本提示，请按提示升级。

```bash
cd frontend/asset-frontend
npm install
npm run dev         # 默认端口 5173，可在 env 中自定义
```

构建产物：

```bash
npm run build
```

---

### 默认账号

| 角色            | 账号        | 密码      | 权限说明                                         |
|----------------|------------|----------|--------------------------------------------------|
| 系统管理员     | admin      | admin123 | 拥有所有权限                                     |
| 部门管理员     | dept_admin | 123456   | 资产审批、领用/归还、审计查看                   |
| 运营负责人     | ops_lead   | 123456   | 与部门管理员相同，用于演示多账号审批            |
| 仓库主管       | warehouse  | 123456   | 资产入库、采购、审批、领用/归还、库存维护       |
| 审计员         | auditor    | 123456   | 审计日志查看、导出                               |
| 普通员工       | user1      | 123456   | 资产申请、查看                                   |

#### 默认角色 & 权限

| 角色编码            | 说明           | 核心权限                                                       |
|---------------------|---------------|----------------------------------------------------------------|
| `ROLE_ADMIN`        | 系统管理员     | `asset:*`, `user:manage`, `role:manage`, `permission:view`, `audit:view` |
| `ROLE_DEPT_ADMIN`   | 部门管理员     | `asset:view/approve/checkout/return`, `audit:view`             |
| `ROLE_ASSET_ADMIN`  | 资产管理员     | `asset:view/stockin/checkout/return/purchase/apply/approve`, `asset:admin` |
| `ROLE_AUDITOR`      | 审计员         | `audit:view`, `audit:export`                                   |
| `ROLE_VENDOR_MANAGER` | 供应商管理员 | `vendor:manage`, `asset:view`                                  |
| `ROLE_USER`         | 普通员工       | `asset:view`, `asset:apply`                                    |

---

### 核心特性

- JJWT + Spring Security 实现 access/refresh token，支持刷新、权限点注解（`@PreAuthorize`）
- RBAC 权限模型（用户-角色-权限），前后端均支持按钮/路由级别的权限控制
- 资产生命周期 API（申请/审批/入库/领用/归还）+ 审批记录 + 审计日志
- **(New) 资产归还提醒**：可配置 SMTP 邮箱服务、自定义提醒频率（Cron）及提前提醒天数，自动发送邮件提醒即将到期或已逾期的资产。
- 统一响应结构 `{code,message,data}`、全局异常处理、参数校验、分页/排序
- Flyway 数据库迁移 + 种子数据
- Swagger UI (`/swagger-ui.html`) + Postman/Insomnia 示例
- 单元测试（UserService、JwtTokenProvider）+ Integration Test（完整流程）
- Vue3 + Element Plus 前端：登录、仪表盘、申请管理、库存操作、系统管理、审计日志
- Dockerfile（后端）、docker-compose（后端 + MySQL），演示部署

---

### API 文档 & 工具

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- Postman/Insomnia：`infra/postman/asset-management.postman_collection.json`

---

### 运行测试

后端：

```bash
cd backend/asset-backend
./mvnw test
```

前端类型检查 + 构建：

```bash
cd frontend/asset-frontend
npm run build
```

---

### 环境变量

后端（`application.yml` 支持读取）：

| 变量                   | 默认值                                              | 说明 |
|-----------------------|-----------------------------------------------------|---|
| `DB_URL`              | `jdbc:mysql://localhost:3306/asset?...`             | |
| `DB_USERNAME`         | `root`                                              | |
| `DB_PASSWORD`         | `123456`                                            | |
| `JWT_SECRET`          | `popcap`                                            | |
| `JWT_ACCESS_EXPIRATION` | `3600`（秒）                                      | |
| `JWT_REFRESH_EXPIRATION`| `604800`（秒）                                    | |
| `ASSET_LOAN_REMINDER_ENABLED` | `true` | 是否开启归还提醒任务 |
| `ASSET_LOAN_REMINDER_CRON` | `0 0 9 ? * MON` | 默认提醒任务 Cron 表达式 |

前端（`env.example`）：

```
VITE_API_BASE_URL=http://localhost:8080/api
VITE_DEV_SERVER_PORT=5173
```

---

### Docker

#### 后端镜像

```bash
cd backend/asset-backend
docker build -t asset-backend .
```

#### docker-compose

```bash
docker compose up -d
```

`docker-compose.yml` 会启动：

- `mysql`：初始化数据库 `asset`，默认账号/密码 `root/123456`
- `asset-backend`：等待 mysql 就绪后启动，暴露 8080 端口（JWT 默认为 `popcap`）
- `asset-frontend`：构建并启动前端（Nginx），暴露 80 端口，反向代理 API 请求到后端

---

### GitHub Actions（示例）

`.github/workflows/ci.yml` 包含：

1. Checkout + JDK 17
2. 后端 `mvnw -B test`
3. 前端 `npm ci && npm run build`

可根据实际仓库改造后直接启用。

---

### 主要 API（示例）

```http
POST /api/auth/login           # 登录
POST /api/asset-requests       # 创建资产申请
POST /api/asset-requests/{id}/approve
POST /api/inventory/stock-in
POST /api/inventory/{id}/checkout
POST /api/inventory/{id}/return
GET  /api/audit-logs
GET  /api/system/reminder-settings # 获取提醒配置
PUT  /api/system/reminder-settings # 更新提醒配置
```

更多请参阅 Swagger UI。

---

### 开发路线提示

1. `./mvnw spring-boot:run` 启动后端，留意控制台输出的 Flyway 日志。
2. 前端 `.env` 设置 `VITE_API_BASE_URL` 指向真实后端。
3. 若需要进一步扩展，可增加采购模块、报废流程、Redis 刷新 token 黑名单等。

欢迎根据业务需求继续完善。Enjoy! 🎉
