package org.ethereum.facade;

import org.ethereum.config.SystemProperties;
import org.ethereum.core.Transaction;
import org.ethereum.datasource.KeyValueDataSource;
import org.ethereum.datasource.LevelDbDataSource;
import org.ethereum.datasource.redis.RedisConnection;
import org.ethereum.db.RepositoryImpl;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import static org.ethereum.config.SystemProperties.CONFIG;

public class CommonConfig {

    private static final Logger logger = LoggerFactory.getLogger("general");

    private RedisConnection redisConnection;

    Repository repository() {
        return new RepositoryImpl(keyValueDataSource(), keyValueDataSource());
    }

    public KeyValueDataSource keyValueDataSource() {
        String dataSource = CONFIG.getKeyValueDataSource();
        try {
            if ("redis".equals(dataSource) && redisConnection.isAvailable()) {
                // Name will be defined before initialization
                return redisConnection.createDataSource("");
            }

            dataSource = "leveldb";
            return new LevelDbDataSource();
        } finally {
            logger.info(dataSource + " key-value data source created.");
        }
    }

    public Set<Transaction> pendingTransactions() {
        String storage = "Redis";
        try {
            if (redisConnection.isAvailable()) {
                return redisConnection.createTransactionSet("pendingTransactions");
            }

            storage = "In memory";
            return Collections.synchronizedSet(new HashSet<Transaction>());
        } finally {
            logger.info(storage + " 'pendingTransactions' storage created.");
        }
    }

    /*
    public SessionFactory sessionFactory() {
        LocalSessionFactoryBuilder builder =
                new LocalSessionFactoryBuilder(dataSource());
        builder.scanPackages("org.ethereum.db")
                .addProperties(getHibernateProperties());

        return builder.buildSessionFactory();
    }
    */

    private Properties getHibernateProperties() {

        Properties prop = new Properties();

        prop.put("hibernate.hbm2ddl.auto", "update");
        prop.put("hibernate.format_sql", "true");

// todo: useful but annoying consider define by system.properties
//        prop.put("hibernate.show_sql", "true");
        prop.put("hibernate.dialect",
                "org.hibernate.dialect.HSQLDialect");
        return prop;
    }
    /*
    public HibernateTransactionManager txManager() {
        return new HibernateTransactionManager(sessionFactory());
    }

    public DriverManagerDataSource dataSource() {

        logger.info("Connecting to the block store");

        System.setProperty("hsqldb.reconfig_logging", "false");

        String url =
                String.format("jdbc:hsqldb:file:./%s/blockchain/blockchain.db;" +
                                "create=true;hsqldb.default_table_type=cached",

                        SystemProperties.CONFIG.databaseDir());

        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setDriverClassName("org.hsqldb.jdbcDriver");
        ds.setUrl(url);
        ds.setUsername("sa");


        return ds;
    }
    */
}
