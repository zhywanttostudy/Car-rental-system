# Car-rental-system
汽车租赁系统。前台使用 JavaSE 开发，后台采用 SQL Server 数据库。该系统提供了用户登录、注册、车辆管理、订单管理、租赁统计等功能，方便员工进行车辆和订单的管理，同时也为客户提供了便捷的租车服务。

功能特性
用户管理：支持用户登录和注册功能，包括员工和客户。
车辆管理：员工可以进行车辆信息的添加、修改、删除和查询操作。
订单管理：员工可以管理订单信息，客户可以进行订单支付操作。
租赁统计：统计指定时间段内的租赁费用。
界面友好：采用 Swing 构建图形用户界面，操作方便。

环境要求
Java 开发环境：JDK 8 及以上版本
数据库：SQL Server
开发工具：建议使用 IntelliJ IDEA 或 Eclipse

数据库配置
在 Carmanager/src/basis/Connect.java 文件中配置数据库连接信息：
（注意在employeeframe类中也需要修改）
String url = "jdbc:sqlserver://localhost:1433;DatabaseName=Car;trustServerCertificate=true";
String user = "sa";
String password = "tangyuan999";//对应密码

项目结构
Car-rental-system/
├── Carmanager/
│   ├── src/
│       ├── basis/          # 数据库连接和工具类
│       ├── entity/         # 实体类
│       ├── service/        # 业务逻辑服务类
│       ├── view/           # 视图层，包含界面和操作逻辑
│       └── Main.java       # 项目入口
├── LICENSE                 # 项目许可证
├── .gitignore              # Git 忽略文件配置
└── README.md               # 项目说明文档

运行步骤
数据库准备：在 SQL Server 中创建名为 Car 的数据库。
导入项目：将项目导入到 Java 开发工具中。
配置依赖：确保项目中包含 SQL Server JDBC 驱动。
运行项目：运行 Carmanager/src/Main.java 类的 main 方法。
