/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `role`
--

DROP TABLE IF EXISTS `role`;
CREATE TABLE `role` (
    `id` varchar(36) NOT NULL,
    `type` varchar(20) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
    `id` varchar(36) NOT NULL,
    `username` varchar(20) NOT NULL,
    `email` varchar(255) NOT NULL,
    `first_name` varchar(255) DEFAULT NULL,
    `last_name` varchar(255) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_username` (`username`),
    UNIQUE KEY `uk_user_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Table structure for table `user_audit`
--

DROP TABLE IF EXISTS `user_audit`;
CREATE TABLE `user_audit` (
    `id` varchar(36) NOT NULL,
    `ts` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `type` int NOT NULL,
    `operator_id` varchar(36) DEFAULT NULL,
    `user_id` varchar(36) NOT NULL,
    `message` varchar(255) DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_sample_log_ts` (`ts`),
    KEY `fk_user_audit_operator` (`operator_id`),
    KEY `fk_user_audit_user` (`user_id`),
    CONSTRAINT `fk_user_audit_operator_id` FOREIGN KEY (`operator_id`) REFERENCES `user` (`id`) ON DELETE SET NULL,
    CONSTRAINT `fk_user_audit_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Table structure for table `user_role`
--

DROP TABLE IF EXISTS `user_role`;
CREATE TABLE `user_role` (
    `id` varchar(36) NOT NULL,
    `role_id` varchar(36) NOT NULL,
    `user_id` varchar(36) NOT NULL,
    UNIQUE KEY `uk_user_role_ids`(`role_id`, `user_id`),
    KEY `fk_user_role_role_id` (`role_id`),
    KEY `fk_user_role_user_id` (`user_id`),
    CONSTRAINT `fk_user_role_role_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_user_role_user_id` FOREIGN KEY (`role_id`) REFERENCES `role` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Table structure for table `user_session`
--

DROP TABLE IF EXISTS `user_session`;
CREATE TABLE `user_session` (
    `id` varchar(36) NOT NULL,
    `last_seen` datetime NOT NULL,
    `remember_me` bit(1) DEFAULT 0,
    `user_id` varchar(36) NOT NULL,
    PRIMARY KEY (`id`),
    KEY `fk_user_session_user_id` (`user_id`),
    CONSTRAINT `fk_user_session_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Table structure for table `runner`
--

DROP TABLE IF EXISTS `runner`;
CREATE TABLE `runner` (
    `id` varchar(36) NOT NULL,
    `region` varchar(255) NOT NULL,
    `agent` varchar(255) NOT NULL,
    `plugin` varchar(255) NOT NULL,
    `identifier` varchar(255) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_runner_instance` (`region`, `agent`, `plugin`, `identifier`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Table structure for table `runner_log`
--

DROP TABLE IF EXISTS `runner_log`;
CREATE TABLE `runner_log` (
    `id` varchar(36) NOT NULL,
    `ts` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `state` int NOT NULL,
    `message` longtext NOT NULL,
    `runner_id` varchar(36) NOT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_runner_log_ts` (`ts`),
    KEY `fk_runner_log_runner` (`runner_id`),
    CONSTRAINT `fk_runner_log_runner` FOREIGN KEY (`runner_id`) REFERENCES `runner` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Table structure for table `pipeline`
--

DROP TABLE IF EXISTS `pipeline`;
CREATE TABLE `pipeline` (
    `id` varchar(36) NOT NULL,
    `name` varchar(255) NOT NULL,
    `script` longtext NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_pipeline_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Table structure for table `pipeline_audit`
--

DROP TABLE IF EXISTS `pipeline_audit`;
CREATE TABLE `pipeline_audit` (
    `id` varchar(36) NOT NULL,
    `ts` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `status` int NOT NULL,
    `identifier` varchar(255) NOT NULL,
    `message` varchar(255) NOT NULL,
    `pipeline_id` varchar(36) DEFAULT NULL,
    `operator_id` varchar(36) DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `fk_pipeline_audit_pipeline` (`pipeline_id`),
    KEY `fk_pipeline_audit_operator` (`operator_id`),
    CONSTRAINT `fk_pipeline_audit_pipeline` FOREIGN KEY (`pipeline_id`) REFERENCES `pipeline` (`id`) ON DELETE SET NULL,
        CONSTRAINT `fk_pipeline_audit_operator` FOREIGN KEY (`operator_id`) REFERENCES `user` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Table structure for table `pipeline_run`
--

DROP TABLE IF EXISTS `pipeline_run`;
CREATE TABLE `pipeline_run` (
    `id` varchar(36) NOT NULL,
    `created` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `status` int NOT NULL,
    `pipeline_id` varchar(36) NOT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_pipeline_run_created` (`created`),
    KEY `fk_pipeline_run_pipeline` (`pipeline_id`),
    CONSTRAINT `fk_pipeline_run_pipeline` FOREIGN KEY (`pipeline_id`) REFERENCES `pipeline` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Table structure for table `task`
--

DROP TABLE IF EXISTS `task`;
CREATE TABLE `task` (
    `id` varchar(36) NOT NULL,
    `pipeline_id` varchar(36) NOT NULL,
    `name` varchar(255) NOT NULL,
    `script` longtext NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_task_pipeline_name` (`pipeline_id`, `name`),
    KEY `fk_task_pipeline` (`pipeline_id`),
    CONSTRAINT `fk_task_pipeline` FOREIGN KEY (`pipeline_id`) REFERENCES `pipeline` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Table structure for table `task_audit`
--

DROP TABLE IF EXISTS `task_audit`;
CREATE TABLE `task_audit` (
    `id` varchar(36) NOT NULL,
    `ts` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `status` int NOT NULL,
    `identifier` varchar(255) NOT NULL,
    `message` varchar(255) NOT NULL,
    `task_id` varchar(36) DEFAULT NULL,
    `operator_id` varchar(36) DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `fk_task_audit_task` (`task_id`),
    KEY `fk_task_audit_operator` (`operator_id`),
    CONSTRAINT `fk_task_audit_task` FOREIGN KEY (`task_id`) REFERENCES `task` (`id`) ON DELETE SET NULL,
    CONSTRAINT `fk_task_audit_operator` FOREIGN KEY (`operator_id`) REFERENCES `user` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Table structure for table `task_run`
--

DROP TABLE IF EXISTS `task_run`;
CREATE TABLE `task_run` (
    `id` varchar(36) NOT NULL,
    `created` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `status` int DEFAULT 0,
    `pipeline_run_id` varchar(36) NOT NULL,
    `task_id` varchar(36) NOT NULL,
    `runner_id` varchar(36) DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_task_run_created` (`created`),
    KEY `fk_task_run_pipeline_run` (`pipeline_run_id`),
    KEY `fk_task_run_task` (`task_id`),
    KEY `fk_task_run_runner` (`runner_id`),
    CONSTRAINT `fk_task_run_pipeline_run` FOREIGN KEY (`pipeline_run_id`) REFERENCES `pipeline_run` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_task_run_task` FOREIGN KEY (`task_id`) REFERENCES `task` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_task_run_runner` FOREIGN KEY (`runner_id`) REFERENCES `runner` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Table structure for table `task_run_log`
--

DROP TABLE IF EXISTS `task_run_log`;
CREATE TABLE `task_run_log` (
    `id` varchar(36) NOT NULL,
    `ts` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `state` int NOT NULL,
    `message` longtext NOT NULL,
    `task_run_id` varchar(36) NOT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_run_log_ts` (`ts`),
    KEY `fk_run_log_run` (`task_run_id`),
    CONSTRAINT `fk_run_log_task_run` FOREIGN KEY (`task_run_id`) REFERENCES `task_run` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Table structure for table `task_run_output`
--

DROP TABLE IF EXISTS `task_run_output`;
CREATE TABLE `task_run_output` (
    `id` varchar(36) NOT NULL,
    `ts` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `task_run_id` varchar(36) NOT NULL,
    `output` longtext NOT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_run_output_ts` (`ts`),
    KEY `fk_run_output_task_run` (`task_run_id`),
    CONSTRAINT `fk_run_output_task_run` FOREIGN KEY (`task_run_id`) REFERENCES `task_run` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;
/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;