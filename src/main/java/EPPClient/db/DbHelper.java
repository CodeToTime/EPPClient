/*
 * SPDX-FileCopyrightText: 2009-2025 AssoTLD <reg@assotld.it>
 * SPDX-FileCopyrightText: 2026 Riccardo Bertelli
 * SPDX-FileCopyrightText: 2026 Matteo Trubini @ CUBIC S.R.L. <https://cubicsrl.it/>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * This file is part of EPPClient.
 *
 * EPPClient is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * EPPClient is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with EPPClient. If not, see <https://www.gnu.org/licenses/>.
 */

package EPPClient.db;

import EPPClient.config.EPPparams;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.sql.DataSource;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.SqlStatements;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;

/**
 * Database helper with JDBI and HikariCP connection pooling.
 * Supports multiple databases: Derby, MariaDB/MySQL, PostgreSQL, SQLite.
 */
public final class DbHelper
{

  private static volatile DbHelper instance;
  private static final AtomicBoolean initialized = new AtomicBoolean(false);

  private final HikariDataSource dataSource;
  private final Jdbi jdbi;
  private DatabaseType dbType;

  /**
   * Database type enum for dialect detection.
   */
  public enum DatabaseType
  {
    DERBY, MYSQL, MARIADB, POSTGRESQL, SQLITE, UNKNOWN
  }

  private DbHelper()
  {
    HikariConfig hikari = new HikariConfig();
    hikari.setJdbcUrl(EPPparams.getParameter("EppClient.dburl"));
    hikari.setUsername(EPPparams.getParameter("EppClient.dbuid"));
    hikari.setPassword(EPPparams.getParameter("EppClient.dbpwd"));
    // hikari.setDriverClassName(config.getProperty("db.driver"));

    // Pool configuration
    hikari.setMaximumPoolSize(10);
    hikari.setMinimumIdle(2);
    hikari.setConnectionTimeout(30000);
    hikari.setIdleTimeout(600000);
    hikari.setMaxLifetime(1800000);

    this.dataSource = new HikariDataSource(hikari);
    this.jdbi = Jdbi.create(dataSource);

    // Install SQL Object plugin
    this.jdbi.installPlugin(new SqlObjectPlugin());

    // Configure SQL dialect
    configureDialect();
  }

  private void configureDialect()
  {
    // JDBI automatically detects dialect from JDBC URL
    // Additional configuration if needed
    jdbi.useHandle(handle -> {
      handle.getConfig(SqlStatements.class).setUnusedBindingAllowed(true);
    });
  }

  /**
   * Initialize the DbHelper with database configuration.
   */
  public static synchronized void initialize()
  {
    if (instance == null)
    {
      String dbUrl = EPPparams.getParameter("EppClient.dburl");
      instance = new DbHelper();
      initialized.set(true);

      // Create database if it doesn't exist
      instance.createDatabase();
    }
    else
    {
      // Check if re-initializing with different config
      String newUrl = EPPparams.getParameter("EppClient.dburl");
      String existingUrl = instance.dataSource.getJdbcUrl();
      if (!newUrl.equals(existingUrl))
      {
        System.out.println("WARNING: DbHelper already initialized with different database URL. " + "Existing configuration used: " + existingUrl + ". " + "New configuration ignored: " + newUrl);
      }
    }
  }

  /**
   * Get the singleton instance.
   */
  public static DbHelper getInstance()
  {
    if (!initialized.get())
    {
      throw new IllegalStateException("DbHelper not initialized. Call initialize() first.");
    }
    // Use synchronized access to prevent race condition with shutdown()
    synchronized (initialized)
    {
      if (instance == null)
      {
        throw new IllegalStateException("DbHelper not initialized. Call initialize() first.");
      }
      return instance;
    }
  }

  /**
   * Check if DbHelper has been initialized.
   */
  public static boolean isInitialized()
  {
    return initialized.get();
  }

  /**
   * Detect database type from connection URL or type string.
   */
  public static DatabaseType detectDatabaseType(String dbTypeOrUrl)
  {
    String lower = dbTypeOrUrl.toLowerCase();

    // Check by type string first
    if (lower.contains("derby") || lower.equals("derby"))
    {
      return DatabaseType.DERBY;
    }
    if (lower.contains("mysql") || lower.equals("mysql"))
    {
      return DatabaseType.MYSQL;
    }
    if (lower.contains("mariadb") || lower.equals("mariadb"))
    {
      return DatabaseType.MARIADB;
    }
    if (lower.contains("postgres") || lower.equals("postgresql"))
    {
      return DatabaseType.POSTGRESQL;
    }
    if (lower.contains("sqlite") || lower.equals("sqlite"))
    {
      return DatabaseType.SQLITE;
    }

    // Check by JDBC URL
    if (lower.contains("jdbc:derby"))
    {
      return DatabaseType.DERBY;
    }
    if (lower.contains("jdbc:mysql"))
    {
      return DatabaseType.MYSQL;
    }
    if (lower.contains("jdbc:mariadb"))
    {
      return DatabaseType.MARIADB;
    }
    if (lower.contains("jdbc:postgresql"))
    {
      return DatabaseType.POSTGRESQL;
    }
    if (lower.contains("jdbc:sqlite"))
    {
      return DatabaseType.SQLITE;
    }

    return DatabaseType.UNKNOWN;
  }

  /**
   * Get the Jdbi instance for database operations.
   */
  public Jdbi getJdbi()
  {
    return jdbi;
  }

  /**
   * Get the DataSource for legacy compatibility.
   */
  public DataSource getDataSource()
  {
    return dataSource;
  }

  /**
   * Get a connection from DriverManager using the configured database.
   */
  public Connection getConnection() throws SQLException
  {
    String baseUrl = EPPparams.getParameter("EppClient.dburl");
    String dbName = EPPparams.getParameter("EppClient.dbname");
    String dbHost = EPPparams.getParameter("EppClient.dbhost");
    String dbUser = EPPparams.getParameter("EppClient.dbuid");
    String dbPwd = EPPparams.getParameter("EppClient.dbpwd");

    DatabaseType dbType = detectDatabaseType(baseUrl);

    // For Derby, check if database file exists before adding create=true
    boolean createIfNotExists = false;
    if (dbType == DatabaseType.DERBY)
    {
      String dbPath = System.getProperty("user.home") + "/.eppclient/" + dbName;
      File dbFile = new File(dbPath);
      if (!dbFile.exists())
      {
        createIfNotExists = true;
      }
    }

    String dbUrl = buildDatabaseUrl(dbType, baseUrl, dbName, dbHost, createIfNotExists);

    return DriverManager.getConnection(dbUrl, dbUser, dbPwd);
  }

  /**
   * Get the detected database type.
   */
  public DatabaseType getDbType()
  {
    return dbType;
  }

  /**
   * Check if current database is MySQL/MariaDB.
   */
  public boolean isMysqlOrMariaDb()
  {
    return dbType == DatabaseType.MYSQL || dbType == DatabaseType.MARIADB;
  }

  /**
   * Check if current database is Derby.
   */
  public boolean isDerby()
  {
    return dbType == DatabaseType.DERBY;
  }

  /**
   * Check if current database is PostgreSQL.
   */
  public boolean isPostgreSql()
  {
    return dbType == DatabaseType.POSTGRESQL;
  }

  /**
   * Check if current database is SQLite.
   */
  public boolean isSqlite()
  {
    return dbType == DatabaseType.SQLITE;
  }

  /**
   * Builds the appropriate JDBC URL for database creation.
   * @param dbType The database type
   * @param baseUrl The base JDBC URL
   * @param dbName The database name
   * @param dbHost The database host (for server-based databases)
   * @return The complete JDBC URL for connecting to the database
   */
  public static String buildDatabaseUrl(DatabaseType dbType, String baseUrl, String dbName, String dbHost)
  {
    return buildDatabaseUrl(dbType, baseUrl, dbName, dbHost, false);
  }

  /**
   * Builds the appropriate JDBC URL for database creation.
   * @param dbType The database type
   * @param baseUrl The base JDBC URL
   * @param dbName The database name
   * @param dbHost The database host (for server-based databases)
   * @param createIfNotExists If true, adds create=true for Derby
   * @return The complete JDBC URL for connecting to the database
   */
  public static String buildDatabaseUrl(DatabaseType dbType, String baseUrl, String dbName, String dbHost, boolean createIfNotExists)
  {
    switch (dbType)
    {
      case DERBY:
        if (createIfNotExists)
        {
          return baseUrl + dbName + ";create=true";
        }
        return baseUrl + dbName;

      case SQLITE:
        return baseUrl + dbName + ".db";

      case MYSQL:
      case MARIADB:
        if (baseUrl.contains("://"))
        {
          return baseUrl + dbName;
        }
        return "jdbc:mysql://" + dbHost + "/" + dbName;

      case POSTGRESQL:
        if (baseUrl.contains("://"))
        {
          return baseUrl + dbName;
        }
        return "jdbc:postgresql://" + dbHost + "/" + dbName;

      default:
        throw new IllegalArgumentException("Unsupported database type: " + dbType);
    }
  }

  /**
   * Creates the database if it doesn't exist. For file-based databases (Derby, SQLite),
   * this creates the database file. For server-based databases (MySQL, PostgreSQL),
   * this attempts to create the database if it doesn't exist.
   */
  public void createDatabase()
  {
    try
    {
      // Set the database system directory for Derby
      setDBSystemDir();

      String baseUrl = EPPparams.getParameter("EppClient.dburl");
      String dbName = EPPparams.getParameter("EppClient.dbname");
      String dbUser = EPPparams.getParameter("EppClient.dbuid");
      String dbPwd = EPPparams.getParameter("EppClient.dbpwd");
      String dbHost = EPPparams.getParameter("EppClient.dbhost");

      // Detect database type from URL
      DatabaseType dbType = detectDatabaseType(baseUrl);

      // For server-based databases, check if database exists and create if needed
      if (dbType == DatabaseType.MYSQL || dbType == DatabaseType.MARIADB || dbType == DatabaseType.POSTGRESQL)
      {
        if (!serverDatabaseExists(dbType, baseUrl, dbHost, dbName, dbUser, dbPwd))
        {
          createServerDatabase(dbType, baseUrl, dbHost, dbName, dbUser, dbPwd);
        }
      }
    }
    catch (SQLException ex)
    {
      ex.printStackTrace();
    }
  }

  /**
   * Checks if a server-based database exists.
   */
  private boolean serverDatabaseExists(DatabaseType dbType, String baseUrl, String dbHost, String dbName, String dbUser, String dbPwd)
  {
    String adminUrl = getAdminUrl(dbType, baseUrl, dbHost);
    if (adminUrl == null)
    {
      return false;
    }

    try (Connection conn = DriverManager.getConnection(adminUrl, dbUser, dbPwd))
    {
      try (Statement stmt = conn.createStatement())
      {
        ResultSet rs = stmt.executeQuery("SELECT 1 FROM " + dbName + " LIMIT 1");
        return true; // If query succeeds, database exists
      }
      catch (SQLException ex)
      {
        return false; // Database doesn't exist
      }
    }
    catch (SQLException ex)
    {
      return false; // Can't connect, assume doesn't exist
    }
  }

  /**
   * Creates a server-based database (MySQL/MariaDB or PostgreSQL).
   */
  private void createServerDatabase(DatabaseType dbType, String baseUrl, String dbHost, String dbName, String dbUser, String dbPwd) throws SQLException
  {
    String adminUrl = getAdminUrl(dbType, baseUrl, dbHost);
    if (adminUrl == null)
    {
      return;
    }

    try (Connection conn = getConnection())
    {
      try (Statement stmt = conn.createStatement())
      {
        stmt.execute("CREATE DATABASE " + dbName);
      }
    }
  }

  /**
   * Gets the admin URL for creating databases.
   */
  private String getAdminUrl(DatabaseType dbType, String baseUrl, String dbHost)
  {
    if (dbType == DatabaseType.MYSQL || dbType == DatabaseType.MARIADB)
    {
      if (baseUrl.contains("://"))
      {
        return baseUrl.substring(0, baseUrl.lastIndexOf("/")) + "/";
      }
      return "jdbc:mysql://" + dbHost + "/";
    }
    else if (dbType == DatabaseType.POSTGRESQL)
    {
      if (baseUrl.contains("://"))
      {
        return baseUrl.substring(0, baseUrl.lastIndexOf("/")) + "/postgres";
      }
      return "jdbc:postgresql://" + dbHost + "/postgres";
    }
    return null;
  }

  /**
   * Check if a table exists in the database.
   */
  public boolean tableExists(String tableName)
  {
    boolean exists = false;
    try (Connection conn = getConnection())
    {
      ResultSet rs = conn.getMetaData().getTables(null, null, "%", null);
      while (rs.next())
      {
        if (rs.getString(3).toLowerCase().equals(tableName.toLowerCase()))
        {
          exists = true;
          break;
        }
      }
    }
    catch (SQLException ex)
    {
      ex.printStackTrace();
    }
    return exists;
  }

  /**
   * Execute any SQL statement (CREATE TABLE, DROP TABLE, etc.).
   */
  public void executeSql(String sql)
  {
    try (Handle h = jdbi.open())
    {
      h.execute(sql);
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }

  /**
   * Set the database system directory for Derby.
   */
  public static void setDBSystemDir()
  {
    String userHomeDir = System.getProperty("user.home", ".");
    String systemDir = userHomeDir + "/.eppclient";
    System.setProperty("derby.system.home", systemDir);

    File fileSystemDir = new File(systemDir);
    fileSystemDir.mkdir();
  }

  /**
   * Open a handle to the database.
   */
  public Handle openHandle()
  {
    return jdbi.open();
  }

  /**
   * Close a handle.
   */
  public void closeHandle(Handle handle)
  {
    if (handle != null)
    {
      handle.close();
    }
  }

  /**
   * Connect to the database.
   */
  public Handle connect()
  {
    return jdbi.open();
  }

  /**
   * Disconnect from the database.
   */
  public void disconnect(Handle handle)
  {
    if (handle != null)
    {
      handle.close();
    }
  }

  /**
   * Close the connection pool.
   */
  public void shutdown()
  {
    if (dataSource != null && !dataSource.isClosed())
    {
      dataSource.close();
    }
    instance = null;
    initialized.set(false);
  }
}
