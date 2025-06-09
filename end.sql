-- 创建数据库 manage
CREATE DATABASE IF NOT EXISTS manage
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

-- 使用数据库 manage
USE manage;
CREATE TABLE user (
                      user_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID(主键)',
                      username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
                      password VARCHAR(255) NOT NULL COMMENT '密码（需加密存储）',
                      email VARCHAR(100) NOT NULL UNIQUE COMMENT '邮箱',
                      register_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
                      permission_status ENUM('正常', '受限') NOT NULL DEFAULT '正常' COMMENT '权限状态'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 管理员表（专用于管理员角色）
CREATE TABLE admin (
                       user_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID(主键)',
                       username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
                       password VARCHAR(255) NOT NULL COMMENT '密码（需加密存储）',
                       email VARCHAR(100) NOT NULL UNIQUE COMMENT '邮箱',
                       register_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
                       role ENUM('管理员') NOT NULL DEFAULT '管理员' COMMENT '用户角色（固定）',
                       permission_status ENUM('正常', '受限') NOT NULL DEFAULT '正常' COMMENT '权限状态'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员表';
INSERT INTO admin (username, password, email)
VALUES (
           'admin',
           '123',
           'admin@example.com'
       );
-- 审核员表（专用于审核员角色）
CREATE TABLE reviewer (
                          user_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID(主键)',
                          username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
                          password VARCHAR(255) NOT NULL COMMENT '密码（需加密存储）',
                          email VARCHAR(100) NOT NULL UNIQUE COMMENT '邮箱',
                          register_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
                          role ENUM('审核员') NOT NULL DEFAULT '审核员' COMMENT '用户角色（固定）',
                          permission_status ENUM('正常', '受限') NOT NULL DEFAULT '正常' COMMENT '权限状态'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审核员表';

-- 文物表（存储文物基本信息）
CREATE TABLE artifact (
                          artifact_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '文物ID(主键)',
                          name VARCHAR(100) NOT NULL COMMENT '文物名称',
                          era VARCHAR(50) NOT NULL COMMENT '年代',
                          type VARCHAR(50) NOT NULL COMMENT '文物类型（如瓷器、书画等）',
                          museum VARCHAR(50) NOT NULL COMMENT '博物馆名称',
                          description TEXT COMMENT '详细介绍',
                          image_url VARCHAR(255) COMMENT '图片存储路径',
                          likes INT DEFAULT 0 COMMENT '点赞数',
                          feature TEXT NOT NULL COMMENT '特征向量（字符串存储）'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文物表';

-- 点赞表（记录当前用户是否点赞当前显示文物）
CREATE TABLE likes (
                       like_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '点赞记录ID(主键)',
                       user_id INT NOT NULL COMMENT '用户ID',
                       artifact_id INT NOT NULL COMMENT '文物ID'

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='点赞表';

-- 评论表（用户对文物的评论）
CREATE TABLE comment (
                         comment_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '评论ID(主键)',
                         content TEXT NOT NULL COMMENT '评论内容',
                         publish_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
                         user_id INT NOT NULL COMMENT '用户ID',
                         artifact_id INT NOT NULL COMMENT '文物ID',
                         review_status ENUM('通过', '未通过') NOT NULL DEFAULT '未通过' COMMENT '审核状态'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论表';

-- 收藏表（用户收藏文物记录）
CREATE TABLE collection (
                            collection_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '收藏ID(主键)',
                            user_id INT NOT NULL COMMENT '用户ID',
                            artifact_id INT NOT NULL COMMENT '文物ID',
                            collect_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏表';

-- 新增问答表和主题表
-- 问答记录表
CREATE TABLE QA (
                    qa_id INT PRIMARY KEY AUTO_INCREMENT,
                    history_id INT NOT NULL,
                    content TEXT NOT NULL,
                    ask_time DATETIME NOT NULL,
                    user_id INT

);
-- 主题记录表
CREATE TABLE Topic (
                       id INT AUTO_INCREMENT PRIMARY KEY,
                       user_id INT NOT NULL,
                       history_id INT NOT NULL,
                       topic VARCHAR(255)
);

-- 审核记录表（审核员操作记录）
CREATE TABLE review (
                        review_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '审核ID(主键)',
                        content_type ENUM('评论', '动态', '媒体') NOT NULL COMMENT '审核内容类型',
                        comment_id INT COMMENT '关联评论ID（仅当类型为评论时有效）',
                        post_id INT COMMENT '关联动态ID（仅当类型为动态时有效）',
                        result ENUM('通过', '未通过') NOT NULL COMMENT '审核结果',
                        review_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '审核时间',
                        reviewer_id INT NOT NULL COMMENT '审核员ID'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审核记录表';
INSERT INTO review (content_type, comment_id, post_id, result, reviewer_id) VALUES
                                                                                ('评论', 101, NULL, '通过', 1),
                                                                                ('动态', NULL, 201, '未通过', 2),
                                                                                ('媒体', NULL, NULL, '通过', 3),
                                                                                ('评论', 102, NULL, '未通过', 2),
                                                                                ('动态', NULL, 202, '通过', 1);
-- 日志表（系统操作日志）
CREATE TABLE log (
                     log_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '日志ID(主键)',
                     operation_type ENUM('增', '删', '改', '备份', '恢复') NOT NULL COMMENT '操作类型',
                     operation_detail TEXT COMMENT '操作详情',
                     operation_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
                     operator_id INT NOT NULL COMMENT '操作用户ID'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统日志表';

-- 备份表（系统备份记录）
CREATE TABLE backup (
                        backup_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '备份ID(主键)',
                        backup_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '备份时间',
                        file_path VARCHAR(255) NOT NULL COMMENT '备份文件路径',
                        operator_id INT NOT NULL COMMENT '操作员ID'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='备份记录表';
INSERT INTO backup (backup_time, file_path, operator_id) VALUES
                                                             (NOW(), '/backup/20240509_backup1.sql', 101),
                                                             (NOW(), '/backup/20240509_backup2.sql', 102),
                                                             (NOW(), '/backup/20240508_backup3.sql', 101),
                                                             ('2024-12-01 10:30:00', '/backup/20241201_backup4.sql', 103),
                                                             ('2025-01-15 14:45:00', '/backup/20250115_backup5.sql', 104);

INSERT INTO log (operation_type, operation_detail, operation_time, operator_id) VALUES
                                                                                    ('增', '新增用户：test_user', NOW() - INTERVAL 5 MINUTE, 1001),
                                                                                    ('删', '删除角色：guest_role', NOW() - INTERVAL 4 MINUTE, 1002),
                                                                                    ('改', '修改配置项：max_connections=1000', NOW() - INTERVAL 3 MINUTE, 1003),
                                                                                    ('备份', '执行数据库全量备份', NOW() - INTERVAL 2 MINUTE, 1004),
                                                                                    ('恢复', '从2025-05-08备份中恢复数据', NOW() - INTERVAL 1 MINUTE, 1005);
INSERT INTO reviewer (username, password, email)
VALUES (
           'a',
           '123',  -- 示例 bcrypt 加密密码
           'test_reviewer@example.com'
       );
INSERT INTO artifact (name, era, type, museum, description, image_url, likes, feature) VALUES
    ('青花瓷瓶', '明朝', '瓷器', '故宫博物院', '明代永乐年间的青花瓷器，釉面光润，图案精美。',
     'https://wangri.oss-cn-beijing.aliyuncs.com/collection/1/30.jpg', '1250', 'blue white porcelain, floral pattern, Ming dynasty');
INSERT INTO comment (content, publish_time, user_id, artifact_id, review_status) VALUES
                                                                                     ('这件文物的历史真是令人着迷！', '2025-05-10 14:30:00', 1, 101, '通过'),
                                                                                     ('很荣幸能看到如此珍贵的文物。', DEFAULT, 2, 102, '未通过'),
                                                                                     ('这个展览让我对古代文明有了更深的理解。', '2025-05-09 16:20:45', 3, 103, '通过'),
                                                                                     ('细节之处尽显古人的智慧。', DEFAULT, 4, 101, '通过'),
                                                                                     ('每次观看都有不同的感悟。', '2025-05-08 17:45:12', 5, 104, '未通过'),
                                                                                     ('这真是一件不可多得的艺术品。', DEFAULT, 6, 105, '通过'),
                                                                                     ('希望可以有更多这样的展览。', '2025-05-07 19:00:00', 7, 101, '通过'),
                                                                                     ('感受到了历史的厚重。', DEFAULT, 8, 106, '未通过'),
                                                                                     ('非常值得一看的展览。', '2025-05-06 20:30:00', 9, 107, '通过'),
                                                                                     ('让我们一起珍惜这些文化遗产吧。', DEFAULT, 10, 108, '通过');
INSERT INTO comment (content, publish_time, user_id, artifact_id, review_status) VALUES
                                                                                     ('这件文物的历史非常悠久，令人赞叹！', '2025-05-18 10:30:00', 1001, 2001, '通过'),
                                                                                     ('真希望可以了解更多关于这个展品的信息。', '2025-05-19 14:45:00', 1002, 2002, '通过'),
                                                                                     ('展示效果非常好，值得一看。', NOW(), 1003, 2003, '未通过'),
                                                                                     ('这简直是一件艺术品，太美了！', '2025-05-20 09:22:00', 1004, 2004, '通过'),
                                                                                     ('我对这段历史很感兴趣，希望能有更多类似的展览。', '2025-05-21 16:47:00', 1005, 2005, '未通过'),
                                                                                     ('每次参观都有不同的收获，真的很棒！', NOW(), 1006, 2006, '通过'),
                                                                                     ('希望能够了解更多的背景故事。', '2025-05-22 13:15:00', 1007, 2007, '通过'),
                                                                                     ('展览设计得很独特，让人印象深刻！', NOW(), 1008, 2008, '未通过');