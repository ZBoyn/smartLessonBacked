/*
 * =================================================================
 * 数据库: SmartCourseDB
 * 描述: 用于支持“智慧课程”系统，包括知识图谱、智能批改和学情分析。
 * =================================================================
 */

/*
 * =================================================================
 * 1. 用户与课程管理
 * =================================================================
 */
USE smartlesson;

-- 用户表 (教师, 学生)
CREATE TABLE Users (
                       user_id INT AUTO_INCREMENT PRIMARY KEY,
                       username VARCHAR(100) NOT NULL UNIQUE,
                       password_hash VARCHAR(255) NOT NULL, -- 存储哈希后的密码
                       full_name VARCHAR(100) NOT NULL,
                       email VARCHAR(100) NOT NULL UNIQUE,
                       `role` ENUM('teacher', 'student') NOT NULL, -- 支持 US01, US02
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 课程表 (例如: "数据库系统", "软件工程")
CREATE TABLE Courses (
                         course_id INT AUTO_INCREMENT PRIMARY KEY,
                         course_name VARCHAR(255) NOT NULL,
                         description TEXT,
                         creator_id INT NOT NULL, -- 课程创建者 (教师)
                         FOREIGN KEY (creator_id) REFERENCES Users(user_id) ON DELETE RESTRICT
);

-- 班级表 (课程的具体实例, 例如: "2025秋季数据库系统班")
CREATE TABLE Classes (
                         class_id INT AUTO_INCREMENT PRIMARY KEY,
                         course_id INT NOT NULL,
                         class_name VARCHAR(255) NOT NULL,
                         teacher_id INT NOT NULL, -- 该班级的任课教师
                         semester VARCHAR(50),
                         FOREIGN KEY (course_id) REFERENCES Courses(course_id) ON DELETE CASCADE,
                         FOREIGN KEY (teacher_id) REFERENCES Users(user_id) ON DELETE RESTRICT
);

-- 选课表 (学生加入班级)
CREATE TABLE Enrollments (
                             enrollment_id INT AUTO_INCREMENT PRIMARY KEY,
                             student_id INT NOT NULL,
                             class_id INT NOT NULL,
                             enrollment_date DATE DEFAULT NULL, -- <--- CORRECTED LINE
                             UNIQUE KEY (student_id, class_id),
                             FOREIGN KEY (student_id) REFERENCES Users(user_id) ON DELETE CASCADE,
                             FOREIGN KEY (class_id) REFERENCES Classes(class_id) ON DELETE CASCADE
);

/*
 * =================================================================
 * 2. 知识图谱与教学资源 (支持 Source 3 和 US06, US10)
 * =================================================================
 */

-- 知识点表
CREATE TABLE KnowledgePoints (
                                 kp_id INT AUTO_INCREMENT PRIMARY KEY,
                                 course_id INT NOT NULL, -- 知识点归属于某门课程
                                 name VARCHAR(255) NOT NULL,
                                 description TEXT,
                                 FOREIGN KEY (course_id) REFERENCES Courses(course_id) ON DELETE CASCADE
);

-- 知识点关系表 (构建图谱)
CREATE TABLE KnowledgeRelations (
                                    relation_id INT AUTO_INCREMENT PRIMARY KEY,
                                    source_kp_id INT NOT NULL, -- 起始知识点
                                    target_kp_id INT NOT NULL, -- 目标知识点
                                    relation_type ENUM('prerequisite', 'belongs_to', 'explains') NOT NULL, -- '前置', '属于', '详细解释' (来自 Source 3)
                                    FOREIGN KEY (source_kp_id) REFERENCES KnowledgePoints(kp_id) ON DELETE CASCADE,
                                    FOREIGN KEY (target_kp_id) REFERENCES KnowledgePoints(kp_id) ON DELETE CASCADE
);

-- 教学资源表 (视频, PDF等)
CREATE TABLE Resources (
                           resource_id INT AUTO_INCREMENT PRIMARY KEY,
                           course_id INT NOT NULL,
                           title VARCHAR(255) NOT NULL,
                           resource_type ENUM('video', 'pdf', 'ppt', 'text') NOT NULL,
                           file_path VARCHAR(512) NOT NULL, -- URL或文件路径
                           uploaded_by_id INT NOT NULL,
                           created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                           FOREIGN KEY (course_id) REFERENCES Courses(course_id) ON DELETE CASCADE,
                           FOREIGN KEY (uploaded_by_id) REFERENCES Users(user_id) ON DELETE RESTRICT
);

-- 资源-知识点关联表 (Source 3 需求: 资源推荐知识点)
CREATE TABLE ResourceKnowledgePoints (
                                         id INT AUTO_INCREMENT PRIMARY KEY,
                                         resource_id INT NOT NULL,
                                         kp_id INT NOT NULL,
                                         UNIQUE KEY (resource_id, kp_id),
                                         FOREIGN KEY (resource_id) REFERENCES Resources(resource_id) ON DELETE CASCADE,
                                         FOREIGN KEY (kp_id) REFERENCES KnowledgePoints(kp_id) ON DELETE CASCADE
);

-- 视频学习分析表 (支持 US07, US08)
CREATE TABLE VideoAnalytics (
                                analytics_id INT AUTO_INCREMENT PRIMARY KEY,
                                resource_id INT NOT NULL, -- 必须是 'video' 类型的资源
                                student_id INT NOT NULL,
                                completion_rate DECIMAL(5, 2) DEFAULT 0.00, -- 视频完成率 (US08)
                                heatmap_data JSON, -- 存储热力图数据 (US07)
                                last_watched_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                FOREIGN KEY (resource_id) REFERENCES Resources(resource_id) ON DELETE CASCADE,
                                FOREIGN KEY (student_id) REFERENCES Users(user_id) ON DELETE CASCADE
);


/*
 * =================================================================
 * 3. 题库系统 (支持 US11, US12, US13, US14)
 * =================================================================
 */

-- 题目表 (题库)
CREATE TABLE Questions (
                           question_id INT AUTO_INCREMENT PRIMARY KEY,
                           course_id INT NOT NULL,
                           prompt TEXT NOT NULL, -- 题干
                           question_type ENUM('single_choice', 'multi_choice', 'short_answer', 'report') NOT NULL,
                           difficulty ENUM('easy', 'medium', 'hard') DEFAULT 'medium',
                           supports_ai_grading BOOLEAN DEFAULT 0, -- 是否支持AI批改 (US13)
                           creator_id INT NOT NULL,
                           created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                           FOREIGN KEY (course_id) REFERENCES Courses(course_id) ON DELETE CASCADE,
                           FOREIGN KEY (creator_id) REFERENCES Users(user_id) ON DELETE RESTRICT
);

-- 选项表 (用于选择题)
CREATE TABLE QuestionOptions (
                                 option_id INT AUTO_INCREMENT PRIMARY KEY,
                                 question_id INT NOT NULL,
                                 option_text TEXT NOT NULL,
                                 is_correct BOOLEAN NOT NULL,
                                 FOREIGN KEY (question_id) REFERENCES Questions(question_id) ON DELETE CASCADE
);

-- 题目-知识点关联表 (支持 US06, US11, US12, US13, US29)
CREATE TABLE QuestionKnowledgePoints (
                                         id INT AUTO_INCREMENT PRIMARY KEY,
                                         question_id INT NOT NULL,
                                         kp_id INT NOT NULL,
                                         UNIQUE KEY (question_id, kp_id),
                                         FOREIGN KEY (question_id) REFERENCES Questions(question_id) ON DELETE CASCADE,
                                         FOREIGN KEY (kp_id) REFERENCES KnowledgePoints(kp_id) ON DELETE CASCADE
);


/*
 * =================================================================
 * 4. 考试与提交 (支持 US15-US29)
 * =================================================================
 */

-- 测评活动表 (考试, 作业)
CREATE TABLE Assessments (
                             assessment_id INT AUTO_INCREMENT PRIMARY KEY,
                             class_id INT NOT NULL, -- 发布给哪个班级
                             title VARCHAR(255) NOT NULL,
                             assessment_type ENUM('exam', 'homework_report', 'quiz') NOT NULL,
                             `status` ENUM('draft', 'published', 'closed') DEFAULT 'draft', -- US16(draft), US18(published)
                             start_time TIMESTAMP, -- US18
                             end_time TIMESTAMP, -- US18
                             created_by_id INT NOT NULL,
                             FOREIGN KEY (class_id) REFERENCES Classes(class_id) ON DELETE CASCADE,
                             FOREIGN KEY (created_by_id) REFERENCES Users(user_id) ON DELETE RESTRICT
);

-- 试卷题目表 (智能组卷或手动组卷的结果)
CREATE TABLE AssessmentQuestions (
                                     id INT AUTO_INCREMENT PRIMARY KEY,
                                     assessment_id INT NOT NULL,
                                     question_id INT NOT NULL,
                                     points DECIMAL(5, 2) NOT NULL DEFAULT 10, -- 该题分值
                                     order_index INT NOT NULL, -- 题目顺序
                                     FOREIGN KEY (assessment_id) REFERENCES Assessments(assessment_id) ON DELETE CASCADE,
                                     FOREIGN KEY (question_id) REFERENCES Questions(question_id) ON DELETE CASCADE
);

-- 提交总表 (学生的一次测评提交)
CREATE TABLE Submissions (
                             submission_id INT AUTO_INCREMENT PRIMARY KEY,
                             assessment_id INT NOT NULL,
                             student_id INT NOT NULL,
                             `status` ENUM('in_progress', 'submitted', 'graded') NOT NULL DEFAULT 'in_progress', -- US25, US26, US27
                             submitted_at TIMESTAMP,
                             total_score DECIMAL(5, 2), -- 最终总分 (US27)
                             is_published_to_student BOOLEAN DEFAULT 0, -- 成绩是否已发布 (US22)
                             FOREIGN KEY (assessment_id) REFERENCES Assessments(assessment_id) ON DELETE CASCADE,
                             FOREIGN KEY (student_id) REFERENCES Users(user_id) ON DELETE CASCADE
);

-- 答案表 (学生对具体问题的回答)
CREATE TABLE Answers (
                         answer_id INT AUTO_INCREMENT PRIMARY KEY,
                         submission_id INT NOT NULL,
                         question_id INT NOT NULL,
                         answer_text TEXT, -- 用于简答题 (US25)
                         file_path VARCHAR(512), -- 用于报告类作业 (US24)
                         score DECIMAL(5, 2), -- 该题得分
                         FOREIGN KEY (submission_id) REFERENCES Submissions(submission_id) ON DELETE CASCADE,
                         FOREIGN KEY (question_id) REFERENCES Questions(question_id) ON DELETE CASCADE
);

-- 学生选项答案表 (用于选择题)
CREATE TABLE AnswerOptions (
                               id INT AUTO_INCREMENT PRIMARY KEY,
                               answer_id INT NOT NULL,
                               option_id INT NOT NULL,
                               FOREIGN KEY (answer_id) REFERENCES Answers(answer_id) ON DELETE CASCADE,
                               FOREIGN KEY (option_id) REFERENCES QuestionOptions(option_id) ON DELETE CASCADE
);

-- AI批改反馈表 (支持 US19, US20, US21, US22, US28)
CREATE TABLE AIFeedback (
                            feedback_id INT AUTO_INCREMENT PRIMARY KEY,
                            answer_id INT NOT NULL UNIQUE, -- 关联到具体的报告作业答案
                            ai_generated_score DECIMAL(5, 2),
                            ai_generated_summary TEXT, -- 优点、缺点 (US20)
                            ai_generated_suggestions TEXT, -- 改进建议 (US20, US28)
                            dimensions JSON, -- 存储多维度评价 (US19), e.g., '{"内容相关性": 9, "逻辑结构": 8}'
                            teacher_revised_score DECIMAL(5, 2), -- 教师复核后分数 (US21)
                            teacher_revised_feedback TEXT, -- 教师复核后评语 (US21)
                            is_revised_by_teacher BOOLEAN DEFAULT 0,
                            FOREIGN KEY (answer_id) REFERENCES Answers(answer_id) ON DELETE CASCADE
);


/*
 * =================================================================
 * 5. 插入示例数据
 * =================================================================
 */
ALTER TABLE Users CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
-- 1. 用户
INSERT INTO Users (username, password_hash, full_name, email, `role`) VALUES
                                                                          ('teacher_zhang', 'hash_pw_teacher', '张老师', 'zhang@neu.edu.cn', 'teacher'),
                                                                          ('student_li', 'hash_pw_student1', '李明', 'li.ming@stu.neu.edu.cn', 'student'),
                                                                          ('student_wang', 'hash_pw_student2', '王芳', 'wang.fang@stu.neu.edu.cn', 'student');

ALTER TABLE Courses CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
-- 2. 课程和班级
INSERT INTO Courses (course_name, description, creator_id) VALUES
    ('数据库系统', '讲授数据库设计、SQL和事务管理', 1);

ALTER TABLE Classes CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
INSERT INTO Classes (course_id, class_name, teacher_id, semester) VALUES
    (1, '数据库系统-2025秋季班', 1, '2025 Fall');

-- 3. 选课
ALTER TABLE Enrollments CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
INSERT INTO Enrollments (student_id, class_id) VALUES
                                                   (2, 1),
                                                   (3, 1);

-- 4. 知识图谱 (US10)
ALTER TABLE KnowledgePoints CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
INSERT INTO KnowledgePoints (course_id, name, description) VALUES
                                                               (1, '关系规范化 (Normalization)', '关于1NF, 2NF, 3NF, BCNF的设计理论'),
                                                               (1, 'SQL连接 (JOIN)', '包括 INNER JOIN, LEFT JOIN, RIGHT JOIN'),
                                                               (1, '事务 (Transaction)', 'ACID特性');

ALTER TABLE KnowledgeRelations CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
INSERT INTO KnowledgeRelations (source_kp_id, target_kp_id, relation_type) VALUES
    (1, 2, 'prerequisite'); -- 规范化 是 SQL连接 的前置知识 (假设)

-- 5. 教学资源 (US07)
ALTER TABLE Resources CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
INSERT INTO Resources (course_id, title, resource_type, file_path, uploaded_by_id) VALUES
                                                                                       (1, '视频: SQL JOIN 详解', 'video', '/videos/sql_join_intro.mp4', 1),
                                                                                       (1, '讲义: 关系规范化', 'pdf', '/pdfs/normalization.pdf', 1);

ALTER TABLE ResourceKnowledgePoints CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
INSERT INTO ResourceKnowledgePoints (resource_id, kp_id) VALUES
                                                             (1, 2), -- 视频关联 "SQL连接"
                                                             (2, 1); -- PDF关联 "关系规范化"

-- 6. 视频分析 (US08)
ALTER TABLE VideoAnalytics CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
INSERT INTO VideoAnalytics (resource_id, student_id, completion_rate, heatmap_data) VALUES
    (1, 2, 0.85, '{"0-30s": 1.5, "31-60s": 1.1}'); -- 李明看了85%

-- 7. 题库 (US11, US13)
ALTER TABLE Questions CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
INSERT INTO Questions (course_id, prompt, question_type, difficulty, supports_ai_grading, creator_id) VALUES
                                                                                                          (1, '哪种JOIN类型会返回左表的所有记录，以及右表中匹配的记录？', 'single_choice', 'easy', 0, 1),
                                                                                                          (1, '请论述数据库规范化的必要性，并解释1NF到3NF的区别。', 'report', 'medium', 1, 1); -- 支持AI批改

-- 选项 (Q1)
ALTER TABLE QuestionOptions CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
INSERT INTO QuestionOptions (question_id, option_text, is_correct) VALUES
                                                                       (1, 'INNER JOIN', 0),
                                                                       (1, 'LEFT JOIN', 1),
                                                                       (1, 'RIGHT JOIN', 0),
                                                                       (1, 'FULL OUTER JOIN', 0);

-- 题目-知识点 (US11, US13)
ALTER TABLE QuestionKnowledgePoints CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
INSERT INTO QuestionKnowledgePoints (question_id, kp_id) VALUES
                                                             (1, 2), -- Q1 关联 "SQL连接"
                                                             (2, 1); -- Q2 关联 "关系规范化"

-- 8. 测评活动 (US18)
ALTER TABLE Assessments CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
INSERT INTO Assessments (class_id, title, assessment_type, `status`, start_time, end_time, created_by_id) VALUES
                                                                                                              (1, '期中测试', 'exam', 'published', '2025-11-20 09:00:00', '2025-11-20 11:00:00', 1),
                                                                                                              (1, '作业1: 规范化报告', 'homework_report', 'published', '2025-11-15 09:00:00', '2025-11-25 23:59:59', 1);

-- 试卷题目
ALTER TABLE AssessmentQuestions CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
INSERT INTO AssessmentQuestions (assessment_id, question_id, points, order_index) VALUES
                                                                                      (1, 1, 20, 1), -- 期中测试 考 Q1
                                                                                      (2, 2, 100, 1); -- 作业1 考 Q2

-- 9. 学生提交 (US24, US26)
-- 李明 (student 2) 提交
ALTER TABLE Submissions CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
INSERT INTO Submissions (assessment_id, student_id, `status`, submitted_at) VALUES
                                                                                (1, 2, 'submitted', '2025-11-20 10:30:00'), -- 期中
                                                                                (2, 2, 'submitted', '2025-11-22 14:00:00'); -- 作业1

-- 王芳 (student 3) 提交

INSERT INTO Submissions (assessment_id, student_id, `status`, submitted_at) VALUES
                                                                                (1, 3, 'submitted', '2025-11-20 10:45:00'), -- 期中
                                                                                (2, 3, 'submitted', '2025-11-23 18:00:00'); -- 作业1

-- 10. 答案
-- 李明 (SubID: 1, 2)
INSERT INTO Answers (submission_id, question_id, score) VALUES
    (1, 1, 20.00); -- Q1 (期中) - 答对了
INSERT INTO Answers (submission_id, question_id, file_path) VALUES
    (2, 2, '/submissions/student2_hw1.pdf'); -- Q2 (作业1) - 待AI批改

-- 王芳 (SubID: 3, 4)
INSERT INTO Answers (submission_id, question_id, score) VALUES
    (3, 1, 0.00); -- Q1 (期中) - 答错了
INSERT INTO Answers (submission_id, question_id, file_path) VALUES
    (4, 2, '/submissions/student3_hw1.pdf'); -- Q2 (作业1) - 待AI批改

-- 答案选项 (李明答对了Q1)
INSERT INTO AnswerOptions (answer_id, option_id) VALUES
    (1, 2); -- 关联到 "LEFT JOIN"
-- (王芳答错了，假设她选了 INNER JOIN)
INSERT INTO AnswerOptions (answer_id, option_id) VALUES
    (3, 1); -- 关联到 "INNER JOIN"

-- 11. AI批改 (US19, US20, US21)
-- 李明 (AnswerID: 2)
ALTER TABLE AIFeedback CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
INSERT INTO AIFeedback (answer_id, ai_generated_score, ai_generated_summary, ai_generated_suggestions, dimensions) VALUES
    (2, 88.00, '优点: 概念清晰，1NF到3NF的解释基本正确。缺点: BCNF的例子不够准确。', '建议: 补充阅读关于BCNF的资料，并修正例子。', '{"内容相关性": 90, "逻辑结构": 85}');
-- (假设教师确认)
UPDATE Answers SET score = 88.00 WHERE answer_id = 2;
UPDATE Submissions SET `status` = 'graded', total_score = 88.00, is_published_to_student = 1 WHERE submission_id = 2;


-- 王芳 (AnswerID: 4)
INSERT INTO AIFeedback (answer_id, ai_generated_score, ai_generated_summary, ai_generated_suggestions, dimensions) VALUES
    (4, 75.00, '优点: 论述了必要性。缺点: 3NF的理解有偏差。', '建议: 重新复习3NF的定义。', '{"内容相关性": 80, "逻辑结构": 70}');
-- 教师复核 (US21)
UPDATE AIFeedback SET
                      teacher_revised_score = 78.00,
                      teacher_revised_feedback = 'AI评价基本准确。补充：你对1NF的理解很好，但3NF的传递依赖部分需要加强。总体不错，给你加了3分。',
                      is_revised_by_teacher = 1
WHERE answer_id = 4;
UPDATE Answers SET score = 78.00 WHERE answer_id = 4;
UPDATE Submissions SET `status` = 'graded', total_score = 78.00, is_published_to_student = 1 WHERE submission_id = 4;


-- 12. 更新期中测试总分 (US27)
UPDATE Submissions SET `status` = 'graded', total_score = 20.00, is_published_to_student = 1 WHERE submission_id = 1; -- 李明
UPDATE Submissions SET `status` = 'graded', total_score = 0.00, is_published_to_student = 1 WHERE submission_id = 3; -- 王芳


ALTER TABLE Answers ADD COLUMN feedback TEXT NULL COMMENT '教师或AI的评语';