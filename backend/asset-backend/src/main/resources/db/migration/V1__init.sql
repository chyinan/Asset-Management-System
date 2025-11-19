CREATE TABLE departments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    parent_id BIGINT NULL,
    remark VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    remark VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE permissions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(200) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(200) NOT NULL,
    full_name VARCHAR(200),
    email VARCHAR(200),
    dept_id BIGINT,
    enabled BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_users_department FOREIGN KEY (dept_id) REFERENCES departments(id)
);

CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles(id)
);

CREATE TABLE role_permissions (
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    CONSTRAINT fk_role_permissions_role FOREIGN KEY (role_id) REFERENCES roles(id),
    CONSTRAINT fk_role_permissions_permission FOREIGN KEY (permission_id) REFERENCES permissions(id)
);

CREATE TABLE asset_types (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    code VARCHAR(100) UNIQUE,
    remark VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE vendors (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    contact VARCHAR(200),
    phone VARCHAR(100),
    remark VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE assets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    asset_no VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    asset_type_id BIGINT,
    model VARCHAR(200),
    vendor_id BIGINT,
    purchase_date DATE,
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    location VARCHAR(200),
    price DECIMAL(12, 2),
    created_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,
    CONSTRAINT fk_assets_asset_type FOREIGN KEY (asset_type_id) REFERENCES asset_types(id),
    CONSTRAINT fk_assets_vendor FOREIGN KEY (vendor_id) REFERENCES vendors(id),
    CONSTRAINT fk_assets_creator FOREIGN KEY (created_by) REFERENCES users(id)
);

CREATE TABLE asset_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    request_no VARCHAR(100) NOT NULL UNIQUE,
    requester_id BIGINT NOT NULL,
    department_id BIGINT,
    status VARCHAR(50) NOT NULL,
    remark VARCHAR(1000),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_asset_requests_requester FOREIGN KEY (requester_id) REFERENCES users(id),
    CONSTRAINT fk_asset_requests_department FOREIGN KEY (department_id) REFERENCES departments(id)
);

CREATE TABLE request_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    request_id BIGINT NOT NULL,
    asset_type_id BIGINT,
    quantity INT NOT NULL,
    purpose VARCHAR(500),
    CONSTRAINT fk_request_items_request FOREIGN KEY (request_id) REFERENCES asset_requests(id),
    CONSTRAINT fk_request_items_asset_type FOREIGN KEY (asset_type_id) REFERENCES asset_types(id)
);

CREATE TABLE approvals (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    entity_type VARCHAR(50) NOT NULL,
    entity_id BIGINT NOT NULL,
    approver_id BIGINT NOT NULL,
    comment VARCHAR(1000),
    result VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_approvals_approver FOREIGN KEY (approver_id) REFERENCES users(id)
);

CREATE TABLE purchases (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    purchase_no VARCHAR(100) NOT NULL UNIQUE,
    request_id BIGINT,
    vendor_id BIGINT,
    status VARCHAR(50),
    total_amount DECIMAL(12, 2),
    ordered_at TIMESTAMP,
    received_at TIMESTAMP,
    remark VARCHAR(1000),
    CONSTRAINT fk_purchases_request FOREIGN KEY (request_id) REFERENCES asset_requests(id),
    CONSTRAINT fk_purchases_vendor FOREIGN KEY (vendor_id) REFERENCES vendors(id)
);

CREATE TABLE inventory (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    asset_id BIGINT NOT NULL,
    serial_no VARCHAR(200) UNIQUE,
    status VARCHAR(50) NOT NULL,
    location VARCHAR(200),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_inventory_asset FOREIGN KEY (asset_id) REFERENCES assets(id)
);

CREATE TABLE checkout_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    inventory_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    type VARCHAR(20) NOT NULL,
    remark VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_checkout_inventory FOREIGN KEY (inventory_id) REFERENCES inventory(id),
    CONSTRAINT fk_checkout_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    action VARCHAR(200) NOT NULL,
    entity VARCHAR(200),
    entity_id BIGINT,
    detail TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_audit_logs_user FOREIGN KEY (user_id) REFERENCES users(id)
);

INSERT INTO departments (id, name, remark)
VALUES (1, 'IT', '信息技术部'),
       (2, 'HR', '人力资源部'),
       (3, 'Finance', '财务部');

INSERT INTO roles (id, code, name, remark)
VALUES (1, 'ROLE_ADMIN', '系统管理员', '系统全局管理'),
       (2, 'ROLE_DEPT_ADMIN', '部门管理员', '部门审批'),
       (3, 'ROLE_USER', '普通用户', '资产申请/查看');

INSERT INTO permissions (id, code, name)
VALUES (1, 'asset:view', '资产查看'),
       (2, 'asset:apply', '资产申请'),
       (3, 'asset:approve', '资产审批'),
       (4, 'asset:purchase', '资产采购'),
       (5, 'asset:stockin', '资产入库'),
       (6, 'asset:checkout', '资产领用'),
       (7, 'asset:return', '资产归还'),
       (8, 'asset:admin', '资产管理'),
       (9, 'user:manage', '用户管理'),
       (10, 'role:manage', '角色管理'),
       (11, 'permission:view', '权限查看'),
       (12, 'audit:view', '审计日志查看');

INSERT INTO role_permissions (role_id, permission_id)
VALUES (1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7), (1, 8), (1, 9), (1, 10), (1, 11), (1, 12),
       (2, 1), (2, 3), (2, 6), (2, 7), (2, 12),
       (3, 1), (3, 2);

INSERT INTO users (id, username, password, full_name, email, dept_id)
VALUES (1, 'admin', '$2a$10$Dow1z9ofuUBev5nYeD1yf.A7PK5EKeosFVJeFt3PcTJS3BM4tiT6i', '系统管理员', 'admin@example.com', 1),
       (2, 'dept_admin', '$2a$10$7EqJtq98hPqEX7fNZaFWoO5X5dUvNbK/C.juAju0imm2Z3EhoG8i.', '部门管理员', 'dept_admin@example.com', 2),
       (3, 'user1', '$2a$10$7EqJtq98hPqEX7fNZaFWoO5X5dUvNbK/C.juAju0imm2Z3EhoG8i.', '张三', 'user1@example.com', 1);

INSERT INTO user_roles (user_id, role_id)
VALUES (1, 1),
       (2, 2),
       (3, 3);

INSERT INTO asset_types (id, name, code, remark)
VALUES (1, 'Laptop', 'LAPTOP', '笔记本电脑'),
       (2, 'Monitor', 'MONITOR', '显示器'),
       (3, 'Mobile', 'MOBILE', '移动设备');

INSERT INTO vendors (id, name, contact, phone, remark)
VALUES (1, 'Lenovo', '李四', '18800000001', 'ThinkPad 供应商'),
       (2, 'Dell', '王五', '18800000002', '显示器供应商');

INSERT INTO assets (id, asset_no, name, asset_type_id, model, vendor_id, purchase_date, status, location, price, created_by)
VALUES (1, 'AST-2025001', 'ThinkPad X1', 1, 'X1 2025', 1, '2025-01-15', 'IN_STOCK', '仓库A', 15000.00, 1);

INSERT INTO inventory (id, asset_id, serial_no, status, location)
VALUES (1, 1, 'SN-2025001', 'IN_STOCK', '仓库A');

INSERT INTO asset_requests (id, request_no, requester_id, department_id, status, remark)
VALUES (1, 'REQ-2025001', 3, 1, 'PENDING', '开发测试使用');

INSERT INTO request_items (request_id, asset_type_id, quantity, purpose)
VALUES (1, 1, 1, '新项目开发');

INSERT INTO audit_logs (user_id, action, entity, entity_id, detail)
VALUES (1, 'SYSTEM_INIT', 'SYSTEM', 0, '初始化系统基础数据');


