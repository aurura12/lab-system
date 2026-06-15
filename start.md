  启动步骤

  1. 启动数据库：
  cd lab-system
  docker-compose up -d

  2. 启动后端：
  cd backend
  mvn spring-boot:run
  后端启动后自动执行 Flyway 迁移，创建表结构并插入示例数据。

  3. 启动前端：
  cd frontend
  npm install
  npm run dev

  4. 访问系统：

- 前端：<http://localhost:5173>
- 后端 API：<http://localhost:8080>
- Swagger UI：<http://localhost:8080/swagger-ui.html>
- 默认管理员账号：admin / admin123

## 演示账号

| 用户名 | 密码 | 姓名 | 角色 |
|--------|------|------|------|
| admin | admin123 | 系统管理员 | 系统管理员 |
| labmanager1 | admin123 | 王芳 | 实验室管理员 |
| labmanager2 | admin123 | 陈静 | 实验室管理员 |
| researcher1 | admin123 | 张伟 | 研究员 |
| researcher2 | admin123 | 李明 | 研究员 |
| researcher3 | admin123 | 张伟 | 研究员 |
| researcher4 | admin123 | 刘洋 | 研究员 |
| researcher5 | admin123 | 赵敏 | 研究员 |
