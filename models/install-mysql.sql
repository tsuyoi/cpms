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
-- Function to generate UUID
--

DROP FUNCTION IF EXISTS uuid_v4;
DELIMITER //
CREATE FUNCTION uuid_v4()
    RETURNS VARCHAR(36)
BEGIN
    SET @h1 = LPAD(HEX(FLOOR(RAND() * 0xffff)), 4, '0');
    SET @h2 = LPAD(HEX(FLOOR(RAND() * 0xffff)), 4, '0');
    SET @h3 = LPAD(HEX(FLOOR(RAND() * 0xffff)), 4, '0');
    SET @h6 = LPAD(HEX(FLOOR(RAND() * 0xffff)), 4, '0');
    SET @h7 = LPAD(HEX(FLOOR(RAND() * 0xffff)), 4, '0');
    SET @h8 = LPAD(HEX(FLOOR(RAND() * 0xffff)), 4, '0');
    SET @h4 = CONCAT('4', LPAD(HEX(FLOOR(RAND() * 0x0fff)), 3, '0'));
    SET @h5 = CONCAT(HEX(FLOOR(RAND() * 4 + 8)),
        LPAD(HEX(FLOOR(RAND() * 0x0fff)), 3, '0'));
    RETURN LOWER(CONCAT(
        @h1, @h2, '-', @h3, '-', @h4, '-', @h5, '-', @h6, @h7, @h8
    ));
END
//
DELIMITER ;

DROP FUNCTION IF EXISTS hibernate_uuid;
DELIMITER //
CREATE FUNCTION hibernate_uuid()
    RETURNS BINARY(16)
BEGIN
    SET @h1 = LPAD(HEX(FLOOR(RAND() * 0xffff)), 4, '0');
    SET @h2 = LPAD(HEX(FLOOR(RAND() * 0xffff)), 4, '0');
    SET @h3 = LPAD(HEX(FLOOR(RAND() * 0xffff)), 4, '0');
    SET @h6 = LPAD(HEX(FLOOR(RAND() * 0xffff)), 4, '0');
    SET @h7 = LPAD(HEX(FLOOR(RAND() * 0xffff)), 4, '0');
    SET @h8 = LPAD(HEX(FLOOR(RAND() * 0xffff)), 4, '0');
    SET @h4 = CONCAT('4', LPAD(HEX(FLOOR(RAND() * 0x0fff)), 3, '0'));
    SET @h5 = CONCAT(HEX(FLOOR(RAND() * 4 + 8)),
        LPAD(HEX(FLOOR(RAND() * 0x0fff)), 3, '0'));
    RETURN UNHEX(CONCAT(
        @h1, @h2, @h3, @h4, @h5, @h6, @h7, @h8
    ));
END
//
DELIMITER ;

--
-- Table structure for table `role`
--

DROP TABLE IF EXISTS `role`;
CREATE TABLE `role` (
    `id` binary(16) NOT NULL,
    `type` varchar(20) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Load initial `role` table data
--

SET @admin_role := hibernate_uuid();

INSERT INTO role (id, type) VALUES (@admin_role, 'Administrator');
INSERT INTO role (id, type) VALUES (hibernate_uuid(), 'Director');
INSERT INTO role (id, type) VALUES (hibernate_uuid(), 'User');
INSERT INTO role (id, type) VALUES (hibernate_uuid(), 'Viewer');

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
    `id` binary(16) NOT NULL,
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
    `id` binary(16) NOT NULL,
    `ts` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `type` int(3) NOT NULL,
    `operator_id` binary(16) DEFAULT NULL,
    `user_id` binary(16) NOT NULL,
    `message` varchar(255) DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_sample_log_ts` (`ts`),
    KEY `fk_user_audit_operator_id` (`operator_id`),
    KEY `fk_user_audit_user_id` (`user_id`),
    CONSTRAINT `fk_user_audit_operator_id` FOREIGN KEY (`operator_id`) REFERENCES `user` (`id`) ON DELETE SET NULL,
    CONSTRAINT `fk_user_audit_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Table structure for table `user_role`
--

DROP TABLE IF EXISTS `user_role`;
CREATE TABLE `user_role` (
    `id` binary(16) NOT NULL,
    `role_id` binary(16) NOT NULL,
    `user_id` binary(16) NOT NULL,
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
    `id` binary(16) NOT NULL,
    `last_seen` datetime NOT NULL,
    `remember_me` bit(1) DEFAULT 0,
    `user_id` binary(16) NOT NULL,
    PRIMARY KEY (`id`),
    KEY `fk_user_session_user_id` (`user_id`),
    CONSTRAINT `fk_user_session_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Table structure for table `runner`
--

DROP TABLE IF EXISTS `runner`;
CREATE TABLE `runner` (
    `id` binary(16) NOT NULL,
    `region` varchar(255) DEFAULT NULL,
    `agent` varchar(255) DEFAULT NULL,
    `plugin` varchar(255) DEFAULT NULL,
    `identifier` varchar(36) DEFAULT NULL,
    `latest_id` binary(16) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_runner_instance` (`identifier`),
    KEY `fk_runner_latest_log` (`latest_id`),
    CONSTRAINT `fk_runner_latest_log` FOREIGN KEY (`latest_id`) REFERENCES `runner_log` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Table structure for table `runner_log`
--

DROP TABLE IF EXISTS `runner_log`;
CREATE TABLE `runner_log` (
    `id` binary(16) NOT NULL,
    `ts` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `state` int(3) NOT NULL,
    `step` int(11) NOT NULL,
    `message` longtext NOT NULL,
    `runner_id` binary(16) NOT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_runner_log_ts` (`ts`),
    KEY `fk_runner_log_runner_id` (`runner_id`),
    CONSTRAINT `fk_runner_log_runner_id` FOREIGN KEY (`runner_id`) REFERENCES `runner` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Table structure for table `task`
--

DROP TABLE IF EXISTS `task`;
CREATE TABLE `task` (
    `id` binary(16) NOT NULL,
    `latest_id` binary(16) DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `fk_task_latest_id` (`latest_id`),
    CONSTRAINT `fk_task_latest_id` FOREIGN KEY (`latest_id`) REFERENCES `task_log` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Table structure for table `task_log`
--

DROP TABLE IF EXISTS `task_log`;
CREATE TABLE `task_log` (
    `id` binary(16) NOT NULL,
    `ts` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `state` int(3) NOT NULL,
    `message` longtext NOT NULL,
    `task_id` binary(16) NOT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_task_log_ts` (`ts`),
    KEY `fk_task_log_task_id` (`task_id`),
    CONSTRAINT `fk_task_log_task_id` FOREIGN KEY (`task_id`) REFERENCES `task` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Table structure for table `run`
--

DROP TABLE IF EXISTS `run`;
CREATE TABLE `run` (
    `id` binary(16) NOT NULL,
    `created` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `status` int(3) DEFAULT 0,
    `runner_id` binary(16) DEFAULT NULL,
    `task_id` binary(16) NOT NULL,
    `latest_id` binary(16) DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_run_created` (`created`),
    KEY `fk_run_runner_id` (`runner_id`),
    KEY `fk_run_task_id` (`task_id`),
    KEY `fk_run_latest_id` (`latest_id`),
    CONSTRAINT `fk_run_runner_id` FOREIGN KEY (`runner_id`) REFERENCES `runner` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_run_task_id` FOREIGN KEY (`task_id`) REFERENCES `task` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_run_latest_id` FOREIGN KEY (`latest_id`) REFERENCES `run_log` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Table structure for table `run_audit`
--

DROP TABLE IF EXISTS `run_audit`;
CREATE TABLE `run_audit` (
    `id` binary(16) NOT NULL,
    `ts` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `status` int(3) NOT NULL,
    `operator_id` binary(16) DEFAULT NULL,
    `message` varchar(255) NOT NULL,
    `run_id` binary(16) DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `fk_run_audit_operator_id` (`operator_id`),
    KEY `fk_run_audit_run_id` (`run_id`),
    CONSTRAINT `fk_run_audit_operator_id` FOREIGN KEY (`operator_id`) REFERENCES `user` (`id`) ON DELETE SET NULL,
    CONSTRAINT `fk_run_audit_run_id` FOREIGN KEY (`run_id`) REFERENCES `run` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Table structure for table `run_log`
--

DROP TABLE IF EXISTS `run_log`;
CREATE TABLE `run_log` (
    `id` binary(16) NOT NULL,
    `ts` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `state` int(3) NOT NULL,
    `step` int(11) NOT NULL,
    `message` longtext NOT NULL,
    `run_id` binary(16) NOT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_run_log_ts` (`ts`),
    KEY `fk_run_log_run_id` (`run_id`),
    CONSTRAINT `fk_run_log_run_id` FOREIGN KEY (`run_id`) REFERENCES `run` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Table structure for table `run_output`
--

DROP TABLE IF EXISTS `run_output`;
CREATE TABLE `run_output` (
    `id` binary(16) NOT NULL,
    `ts` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `output` longtext NOT NULL,
    `run_id` binary(16) NOT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_run_output_ts` (`ts`),
    KEY `fk_run_output_run_id` (`run_id`),
    CONSTRAINT `fk_run_output_run_id` FOREIGN KEY (`run_id`) REFERENCES `run` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;
/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;