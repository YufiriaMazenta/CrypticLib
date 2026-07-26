package crypticlib.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.SQLException;

public class DataSourceWrapper {

    private HikariConfig hikariConfig;
    private volatile HikariDataSource dataSource;

    public DataSourceWrapper() {}

    public DataSourceWrapper(HikariConfig hikariConfig) {
        this.hikariConfig = hikariConfig;
    }

    public synchronized void loadDataSource() {
        if (hikariConfig == null) {
            throw new NullPointerException("hikariConfig is null");
        }
        if (dataSource != null) {
            dataSource.close();
        }
        dataSource = new HikariDataSource(hikariConfig);
    }

    public @Nullable Connection getConn() {
        return getConn(false);
    }

    public @Nullable Connection getConn(boolean throwException) {
        HikariDataSource ds = dataSource;
        if (ds == null) {
            synchronized (this) {
                ds = dataSource;
                if (ds == null) {
                    loadDataSource();
                    ds = dataSource;
                }
            }
        }
        try {
            return ds.getConnection();
        } catch (SQLException e) {
            if (throwException) {
                throw new RuntimeException(e);
            } else {
                e.printStackTrace();
                return null;
            }
        }
    }

    public HikariConfig hikariConfig() {
        return hikariConfig;
    }

    public synchronized DataSourceWrapper setHikariConfig(HikariConfig hikariConfig, boolean reloadDataSource) {
        this.hikariConfig = hikariConfig;
        if (reloadDataSource) {
            loadDataSource();
        }
        return this;
    }

    public synchronized void close() {
        if (dataSource != null) {
            dataSource.close();
        }
    }

    public HikariDataSource dataSource() {
        return dataSource;
    }

}
