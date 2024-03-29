package io.cresco.cpms.database.utilities;

import io.cresco.cpms.database.models.*;
import io.cresco.cpms.logging.BasicCPMSLoggerBuilder;
import io.cresco.cpms.logging.CPMSLogger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Environment;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings({"unused"})
public class BaseSessionFactoryManager implements SessionFactoryManager {
    private SessionFactory factory;
    private final CPMSLogger logger;
    private final Map<String, Object> config;

    public BaseSessionFactoryManager(Map<String, Object> config) {
        this.config = config;
        this.logger = new BasicCPMSLoggerBuilder().withClass(BaseSessionFactoryManager.class).build();
    }

    public BaseSessionFactoryManager(Map<String, Object> config, CPMSLogger logger) {
        this.config = config;
        this.logger = logger;
    }

    private boolean buildSession() {
        logger.trace("Attempting to build a new session factory from plugin settings");
        /*
            Required parameters:
            --------------------
            - db_type
            - db_dbname
            - db_user
            - db_password
         */
        String dbType = (String) config.get("db_type");
        String dbName = (String) config.get("db_dbname");
        String dbUser = (String) config.get("db_user");
        String dbPassword = (String) config.get("db_password");
        if (dbType == null || dbType.isEmpty()) {
            logger.error("Required parameter [db_type] not provided");
            return false;
        }
        if (dbName == null || dbName.isEmpty()) {
            logger.error("Required parameter [db_dbname] not provided");
            return false;
        }
        if (dbUser == null || dbUser.isEmpty()) {
            logger.error("Required parameter [db_user] not provided");
            return false;
        }
        if (dbPassword == null || dbPassword.isEmpty()) {
            logger.error("Required parameter [db_password] not provided");
            return false;
        }

        /*
            Optional parameters:

                C3P0 parameters
                ---------------
                - db_c3p0_min_size              (defaults to 10)
                - db_c3p0_max_size              (defaults to 20)
                - db_c3p0_max_statements        (defaults to 50)
                - db_c3p0_acquire_increment     (defaults to 1)
                - db_c3p0_idle_test_period      (defaults to 3000)
                - db_c3p0_timeout               (defaults to 1800)

                General parameters
                ------------------
                - db_hbm2ddl_auto               (defaults to false)
                - db_show_sql                   (defaults to false)
         */
        String c3p0MinSize = (config.containsKey("db_c3p0_min_size")) ? (String) config.get("db_c3p0_min_size") : "10";
        String c3p0MaxSize = (config.containsKey("db_c3p0_max_size")) ? (String) config.get("db_c3p0_max_size") : "20";
        String c3p0MaxStatements = (config.containsKey("db_c3p0_max_statements")) ? (String) config.get("db_c3p0_max_statements") : "50";
        String c3p0AcquireIncrement = (config.containsKey("db_c3p0_acquire_increment")) ? (String) config.get("db_c3p0_acquire_increment") : "1";
        String c3p0IdleTestPeriod = (config.containsKey("db_c3p0_idle_test_period")) ? (String) config.get("db_c3p0_idle_test_period") : "3000";
        String c3p0Timeout = (config.containsKey("db_c3p0_timeout")) ? (String) config.get("db_c3p0_timeout") : "1800";
        String dbHBM2DDLAuto = (config.containsKey("db_hbm2ddl_auto")) ? (String) config.get("db_hbm2ddl_auto") : "update";
        boolean dbShowSQL = config.containsKey("db_hbm2ddl_auto") && (boolean) config.get("db_show_sql");
        boolean dbAutoCommit = config.containsKey("db_auto_commit") && (boolean) config.get("db_auto_commit");

        // Hibernate settings equivalent to properties in hibernate.cfg.xml
        Map<String, Object> settings = new HashMap<>();
        String dbServer;
        String dbPort;
        dbType = dbType.toLowerCase();
        switch (dbType) {
            case "h2": {
                /*
                    ###############
                    # H2 Database #
                    ###############

                    Required parameters:
                    --------------------
                    - db_filepath
                 */
                logger.trace("Database type is H2");
                String dbFilePathStr = (String) config.get("db_filepath");
                if (dbFilePathStr == null || dbFilePathStr.isEmpty()) {
                    logger.error("Required parameter [db_filepath] not provided");
                    return false;
                }
                try {
                    Class.forName("org.h2.Driver");
                } catch (Exception ex) {
                    logger.error("Could not find H2 driver in bundled plugin. Please rebuild.");
                    return false;
                }
                Path h2Path = Paths.get(dbFilePathStr);
                try {
                    if (!Files.exists(h2Path)) {
                        Files.createDirectories(h2Path);
                    }
                } catch (IOException e) {
                    logger.error("Failed to create H2 database path: " + h2Path.toAbsolutePath().normalize());
                    return false;
                }
                settings.put(Environment.JAKARTA_JDBC_DRIVER, "org.h2.Driver");
                settings.put(Environment.DIALECT, "org.hibernate.dialect.H2Dialect");
                settings.put(Environment.JAKARTA_JDBC_URL,
                        String.format("jdbc:h2:%s;DB_CLOSE_DELAY=-1",
                                h2Path.resolve(dbName).toAbsolutePath().normalize()
                        )
                );
            }
                break;
            case "mssql": {
                /*
                    #######################
                    # SQL Server Database #
                    #######################

                    Required parameters:
                    --------------------
                    - db_server

                    Optional parameters:
                    --------------------
                    - db_port                       (defaults to 1433)
                 */
                logger.trace("Database type is Microsoft SQL Server");
                dbServer = (String) config.get("db_server");
                dbPort = (config.containsKey("db_port")) ? (String) config.get("db_port") : "1433";
                if (dbServer == null || dbServer.isEmpty()) {
                    logger.error("Required parameter [db_server] not provided");
                    return false;
                }
                if (dbPort.isEmpty()) {
                    logger.error("Invalid [db_filepath] provided: " + dbPort);
                    return false;
                }
                try {
                    Integer.parseInt(dbPort);
                } catch (NumberFormatException e) {
                    logger.error("Invalid [db_filepath] provided: " + dbPort);
                    return false;
                }
                try {
                    Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
                } catch (Exception e) {
                    logger.error("Could not find Microsoft SQL Server driver in bundled plugin. Please rebuild.");
                    return false;
                }
                settings.put(Environment.JAKARTA_JDBC_DRIVER, "com.microsoft.sqlserver.jdbc.SQLServerDriver");
                settings.put(Environment.DIALECT, "org.hibernate.dialect.SQLServerDialect");
                settings.put(Environment.JAKARTA_JDBC_URL,
                        String.format("jdbc:sqlserver://%s:%s;databaseName=%s",
                                dbServer, dbPort, dbName
                        )
                );
            }
                break;
            case "mysql": {
                /*
                    ##################
                    # MySQL Database #
                    ##################

                    Required parameters:
                    --------------------
                    - db_server

                    Optional parameters:
                    --------------------
                    - db_port               Port for MySQL server (defaults to 3306)
                    - db_char_enc           MySQL character encoding (defaults to UTF-8)
                    - db_timezone           MySQL server timezone (defaults to UTC)
                    - db_auto_rec           Auto reconnect to MySQL server (defaults to true)
                 */
                logger.trace("Database type is MySQL");
                dbServer = (String) config.get("db_server");
                dbPort = (config.containsKey("db_port")) ? (String) config.get("db_port") : "3306";
                String dbCharacterEncoding = (config.containsKey("db_char_enc")) ? (String) config.get("db_char_enc") : "UTF-8";
                String dbTimezone = (config.containsKey("db_timezone")) ? (String) config.get("db_timezone") : "UTC";
                boolean dbAutoReconnect = !config.containsKey("db_auto_rec") || (boolean) config.get("db_auto_rec");
                if (dbServer == null || dbServer.isEmpty()) {
                    logger.error("Required parameter [db_server] not provided");
                    return false;
                }
                if (dbPort.isEmpty()) {
                    logger.error("Invalid [db_port] provided: " + dbPort);
                    return false;
                }
                try {
                    Integer.parseInt(dbPort);
                } catch (NumberFormatException e) {
                    logger.error("Invalid [db_port] provided: " + dbPort);
                    return false;
                }
                try {
                    Class.forName("com.mysql.cj.jdbc.Driver");
                } catch (Exception e) {
                    logger.error("Could not find MySQL driver in bundled plugin. Please rebuild.");
                    return false;
                }
                settings.put(Environment.JAKARTA_JDBC_DRIVER, "com.mysql.cj.jdbc.Driver");
                settings.put(Environment.DIALECT, "org.hibernate.dialect.MySQLDialect");
                settings.put(Environment.JAKARTA_JDBC_URL,
                        String.format("jdbc:mysql://%s:%s/%s?characterEncoding=%s&serverTimezone=%s&autoReconnect=%s",
                                dbServer, dbPort, dbName, dbCharacterEncoding, dbTimezone, dbAutoReconnect
                        )
                );
            }
                break;
            case "postgresql": {
                /*
                    #######################
                    # PostgreSQL Database #
                    #######################

                    Required parameters:
                    --------------------
                    - db_server

                    Optional parameters:
                    --------------------
                    - db_port               Port for MySQL server (defaults to 5432)
                 */
                logger.trace("Database type is PostgreSQL");
                dbServer = (String) config.get("db_server");
                dbPort = (config.containsKey("db_port")) ? (String) config.get("db_port") : "5432";
                if (dbServer == null || dbServer.isEmpty()) {
                    logger.error("Required parameter [db_server] not provided");
                    return false;
                }
                if (dbPort.isEmpty()) {
                    logger.error("Invalid [db_port] provided: " + dbPort);
                    return false;
                }
                try {
                    Integer.parseInt(dbPort);
                } catch (NumberFormatException e) {
                    logger.error("Invalid [db_port] provided: " + dbPort);
                    return false;
                }
                try {
                    Class.forName("org.postresql.Driver");
                } catch (Exception e) {
                    logger.error("Could not find PostgreSQL driver in bundled plugin. Please rebuild.");
                    return false;
                }
                settings.put(Environment.JAKARTA_JDBC_DRIVER, "org.postresql.Driver");
                settings.put(Environment.DIALECT, "org.hibernate.dialect.PostgreSQLDialect");
                settings.put(Environment.JAKARTA_JDBC_URL,
                        String.format("jdbc:postgresql://%s:%s/%s",
                                dbServer, dbPort, dbName
                        )
                );
            }
                break;
        }
        settings.put(Environment.JAKARTA_JDBC_USER, dbUser);
        settings.put(Environment.JAKARTA_JDBC_PASSWORD, dbPassword);

        settings.put(Environment.C3P0_MIN_SIZE, c3p0MinSize);
        settings.put(Environment.C3P0_MAX_SIZE, c3p0MaxSize);
        settings.put(Environment.C3P0_MAX_STATEMENTS, c3p0MaxStatements);
        settings.put(Environment.C3P0_ACQUIRE_INCREMENT, c3p0AcquireIncrement);
        settings.put(Environment.C3P0_IDLE_TEST_PERIOD, c3p0IdleTestPeriod);
        settings.put(Environment.C3P0_TIMEOUT, c3p0Timeout);

        settings.put("hibernate.cache.provider_class", "org.hibernate.cache.internal.NoCachingRegionFactory");

        settings.put(Environment.HBM2DDL_AUTO, dbHBM2DDLAuto);
        settings.put(Environment.SHOW_SQL, Boolean.toString(dbShowSQL));
        settings.put(Environment.AUTOCOMMIT, Boolean.toString(dbAutoCommit));

        StandardServiceRegistryBuilder registryBuilder = new StandardServiceRegistryBuilder();

        // Apply settings
        registryBuilder.applySettings(settings);

        // Create registry
        StandardServiceRegistry registry = registryBuilder.build();

        MetadataSources sources = new MetadataSources(registry);

        sources.addAnnotatedClass(GenericModel.class);
        sources.addAnnotatedClass(GenericLogModel.class);
        sources.addAnnotatedClass(GenericRunModel.class);
        sources.addAnnotatedClass(Pipeline.class);
        sources.addAnnotatedClass(PipelineRun.class);
        sources.addAnnotatedClass(Runner.class);
        sources.addAnnotatedClass(RunnerLog.class);
        sources.addAnnotatedClass(Task.class);
        sources.addAnnotatedClass(TaskRun.class);
        sources.addAnnotatedClass(TaskRunLog.class);
        sources.addAnnotatedClass(TaskRunOutput.class);

        // Create Metadata
        Metadata metadata = sources.getMetadataBuilder().build();

        try {
            factory = metadata.getSessionFactoryBuilder().build();
            logger.info("Successfully built database session factory.");
            return true;
        } catch (Exception e) {
            StandardServiceRegistryBuilder.destroy( registry );
            logger.error("Failed to create database session factory");
            return false;
        }
    }

    public Session getSession() {
        logger.trace("Grabbing database session from factory");
        if ( factory == null )
            if ( !buildSession() )
                return null;
        return factory.openSession();
    }

    public void close() {
        logger.trace("Closing down session factory");
        if ( factory != null )
            factory.close();
    }
}
