/*
 * SPDX-FileCopyrightText: 2009-2025 AssoTLD <reg@assotld.it>
 * SPDX-FileCopyrightText: 2026 Riccardo Bertelli
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * This file is part of EPPClient.
 *
 * EPPClient is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * EPPClient is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with EPPClient.
 * If not, see <https://www.gnu.org/licenses/>.
 */

package com.codetotime.eppclient.db;

import com.codetotime.eppclient.config.EPPparams;
import com.codetotime.eppclient.messages.Message;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** DAO for persisting and retrieving EPP poll messages from the local database. */
public class messagesDao {
  private static final Logger log = LoggerFactory.getLogger(messagesDao.class);

  private static final String BUNDLE_NAME = "com.codetotime.eppclient.db.messages";

  private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

  /**
   * Returns the string value for the given resource bundle key.
   *
   * @param key the property key
   * @return the value, or {@code !key!} if not found
   */
  public static String getString(String key) {
    try {
      return RESOURCE_BUNDLE.getString(key);
    } catch (MissingResourceException e) {
      return '!' + key + '!';
    }
  }

  /** Creates a new instance of AddressDao. */
  public messagesDao() {
    this("messages");
  }

  /**
   * Creates a DAO bound to the given database name.
   *
   * @param addressBookName the Derby database name to use
   */
  public messagesDao(String addressBookName) {
    this.dbName = addressBookName;

    setDBSystemDir();
    dbProperties = loadDBProperties();

    if (dbProperties.getProperty("derby.url").contains("postgresql")) {
      dbProperties.put("db.schema", "public");
    }

    strDropMessageTable =
        "drop table "
            + dbProperties.getProperty("db.schema")
            + "."
            + dbProperties.getProperty("db.table");

    strCreateAddressTable =
        "create table "
            + dbProperties.getProperty("db.schema")
            + "."
            + dbProperties.getProperty("db.table")
            + " ("
            + "    MSGID                 VARCHAR(30 ) NOT NULL PRIMARY KEY,"
            + "    DATETIME              TIMESTAMP, "
            + "    MESSAGE               VARCHAR(255), "
            + "    MSGXML                CLOB, "
            + "    READFLAG              SMALLINT, "
            + "    ACKFLAG               SMALLINT, "
            + "    ACTIONEDFLAG          SMALLINT"
            + ")";

    strCreateAddressTableMYSQL =
        "create table IF NOT EXISTS "
            + dbProperties.getProperty("db.schema")
            + "."
            + dbProperties.getProperty("db.table")
            + " ("
            + "    MSGID                 VARCHAR(30 ) NOT NULL PRIMARY KEY,"
            + "    DATETIME              TIMESTAMP DEFAULT '1970-01-01 01:00:01', "
            + "    MESSAGE               VARCHAR(255), "
            + "    MSGXML                TEXT, "
            + "    READFLAG              SMALLINT, "
            + "    ACKFLAG               SMALLINT, "
            + "    ACTIONEDFLAG          SMALLINT"
            + ")";

    strGetAddress =
        "SELECT * FROM "
            + dbProperties.getProperty("db.schema")
            + "."
            + dbProperties.getProperty("db.table")
            + " "
            + "WHERE MSGID = ?";

    strSaveAddress =
        "INSERT INTO "
            + dbProperties.getProperty("db.schema")
            + "."
            + dbProperties.getProperty("db.table")
            + " "
            + "   (MSGID, DATETIME, MESSAGE, MSGXML, READFLAG, ACKFLAG, ACTIONEDFLAG) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?)";

    strGetListEntries =
        "SELECT MSGID, DATETIME, MESSAGE, MSGXML, READFLAG, ACKFLAG, ACTIONEDFLAG FROM "
            + dbProperties.getProperty("db.schema")
            + "."
            + dbProperties.getProperty("db.table")
            + " "
            + "ORDER BY DATETIME ASC, MSGID ASC";

    strUpdateAddress =
        "UPDATE "
            + dbProperties.getProperty("db.schema")
            + "."
            + dbProperties.getProperty("db.table")
            + " "
            + "SET READFLAG = ?, "
            + "    ACKFLAG = ?, "
            + "    ACTIONEDFLAG = ? "
            + "WHERE MSGID = ?";

    strDeleteAddress =
        "DELETE FROM "
            + dbProperties.getProperty("db.schema")
            + "."
            + dbProperties.getProperty("db.table")
            + " "
            + "WHERE MSGID = ?";

    String driverName = dbProperties.getProperty("derby.driver");
    loadDatabaseDriver(driverName);
    if (!dbExists()
        && !dbProperties.getProperty("derby.url").contains("mariadb")
        && !dbProperties.getProperty("derby.url").contains("postgresql")) {
      createDatabase();
    } else {
      connect();
      if (!tableExists(dbConnection, dbProperties.getProperty("db.table"))) {
        createTables(dbConnection);
      }
    }
  }

  private boolean tableExists(Connection dbConnection, String tableName) {
    boolean tableExists = false;

    Statement statement = null;
    try {
      ResultSet rs =
          dbConnection
              .getMetaData()
              .getTables(dbProperties.getProperty("db.schema"), null, "%", null);
      while (rs.next()) {
        if (rs.getString(3).toLowerCase().equals(tableName.toLowerCase())) {
          tableExists = true;
        }
      }
    } catch (SQLException ex) {
      log.error("Error checking table existence", ex);
    } catch (Exception ex) {
      log.error("Error checking table existence", ex);
    }

    return tableExists;
  }

  private boolean dbExists() {
    boolean bExists = false;
    String dbLocation = getDatabaseLocation();
    File dbFileDir = new File(dbLocation);
    if (dbFileDir.exists()) {
      bExists = true;
    }
    return bExists;
  }

  private void setDBSystemDir() {
    // decide on the db system directory
    String userHomeDir = System.getProperty("user.home", ".");
    String systemDir = userHomeDir + "/.eppclient";
    System.setProperty("derby.system.home", systemDir);

    // create the db system directory
    File fileSystemDir = new File(systemDir);
    fileSystemDir.mkdir();
  }

  private void loadDatabaseDriver(String driverName) {
    try {
      Class.forName(driverName);
    } catch (ClassNotFoundException ex) {
      if ("org.mariadb.jdbc.Driver".equals(driverName)
          || "org.postgresql.Driver".equals(driverName)) {
        try {
          Class.forName(driverName);
        } catch (ClassNotFoundException e) {
          log.error("Database driver not found: {}", e.getMessage(), e);
        }
      } else {
        log.error("Database driver not found", ex);
      }
    }
  }

  private Properties loadDBProperties() {
    InputStream dbPropInputStream = null;
    dbPropInputStream = messagesDao.class.getResourceAsStream("messages.properties");
    dbProperties = new Properties();
    try {
      dbProperties.load(dbPropInputStream);
      dbProperties.put("derby.locks.monitor", "true");
      dbProperties.put("derby.locks.deadlockTrace", "true");
      dbProperties.put("derby.language.logStatementText", "true");
      String dbUrl = EPPparams.getParameter("EppClient.dburl");
      log.debug("Loaded dbUrl from EPPparams: [{}]", dbUrl);
      if (dbUrl.contains("mariadb")) {
        dbProperties.put("derby.driver", "org.mariadb.jdbc.Driver");
      } else if (dbUrl.contains("postgresql")) {
        dbProperties.put("derby.driver", "org.postgresql.Driver");
      } else {
        dbProperties.put("derby.driver", "org.apache.derby.jdbc.EmbeddedDriver");
      }
      dbProperties.put("derby.url", dbUrl);
      String dbSchema = EPPparams.getParameter("EppClient.dbname");
      String dbUser = EPPparams.getParameter("EppClient.dbuid");
      String dbPwd = EPPparams.getParameter("EppClient.dbpwd");
      log.debug("Loaded dbSchema: [{}]", dbSchema);
      log.debug("Loaded dbUser: [{}]", dbUser);
      log.debug("Loaded dbPwd: [{}]", dbPwd);
      dbProperties.put("db.schema", dbSchema);
      dbProperties.put("user", dbUser);
      dbProperties.put("password", dbPwd);

    } catch (IOException ex) {
      log.error("Error loading database properties", ex);
    }
    return dbProperties;
  }

  private boolean createTables(Connection dbConnection) {
    log.info("Creazione tabelle in corso...");
    boolean bCreatedTables = false;
    Statement statement = null;
    try {
      statement = dbConnection.createStatement();
      if (dbProperties.getProperty("derby.url").contains("mariadb")
          || dbProperties.getProperty("derby.url").contains("postgresql")) {
        statement.execute(strCreateAddressTableMYSQL);
      } else {
        statement.execute(strCreateAddressTable);
      }
      bCreatedTables = true;
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        log.error("Thread interrupted during table creation", e);
      }
    } catch (SQLException ex) {
      log.error("Error creating tables", ex);
    }
    return bCreatedTables;
  }

  /**
   * Drops all message tables from the database.
   *
   * @return {@code true} if the tables were dropped successfully
   */
  public boolean dropTables() {
    boolean bDroppedTables = false;
    try {
      stmtDropMessageTable.clearParameters();
      stmtDropMessageTable.execute();
      bDroppedTables = true;
    } catch (SQLException ex) {
      log.error("Error dropping tables", ex);
    }
    return bDroppedTables;
  }

  private boolean createDatabase() {
    boolean bCreated = false;
    Connection dbConnection = null;

    String dbUrl = getDatabaseUrl();
    dbProperties.put("create", "true");
    try {
      dbConnection = DriverManager.getConnection(dbUrl, dbProperties);
      bCreated = createTables(dbConnection);
    } catch (SQLException ex) {
      log.error("Error creating database", ex);
    }
    dbProperties.remove("create");
    return bCreated;
  }

  /**
   * Opens a connection to the database, creating it if it does not exist.
   *
   * @return {@code true} if the connection was established successfully
   */
  public boolean connect() {
    String dbUrl = getDatabaseUrl();

    try {
      dbProperties.put("shutdown", "false");
      dbConnection = DriverManager.getConnection(dbUrl, dbProperties);
      stmtSaveNewRecord =
          dbConnection.prepareStatement(strSaveAddress, Statement.RETURN_GENERATED_KEYS);
      stmtUpdateExistingRecord = dbConnection.prepareStatement(strUpdateAddress);
      stmtGetAddress = dbConnection.prepareStatement(strGetAddress);
      stmtDeleteAddress = dbConnection.prepareStatement(strDeleteAddress);
      stmtDropMessageTable = dbConnection.prepareStatement(strDropMessageTable);

      isConnected = dbConnection != null;
    } catch (SQLException ex) {
      isConnected = false;
      log.error("Failed to connect to database: {}", ex.getMessage(), ex);
      log.error("DB URL: [{}]", dbUrl);
      log.error("DB Schema: [{}]", dbProperties.getProperty("db.schema"));
      log.error("DB User: [{}]", dbProperties.getProperty("user"));
    }
    return isConnected;
  }

  private String getHomeDir() {
    return System.getProperty("user.home");
  }

  /** Closes the database connection. */
  public void disconnect() {
    if (isConnected) {
      String dbUrl = getDatabaseUrl();
      dbProperties.put("shutdown", "true");
      try {
        DriverManager.getConnection(dbUrl, dbProperties);
      } catch (SQLException ex) {
      }
      isConnected = false;
    }
  }

  /**
   * Returns the filesystem path of the Derby database directory.
   *
   * @return the absolute database path
   */
  public String getDatabaseLocation() {
    String dbLocation = System.getProperty("derby.system.home") + "/" + dbName;
    return dbLocation;
  }

  /**
   * Returns the JDBC URL for the Derby database.
   *
   * @return the JDBC connection URL
   */
  public String getDatabaseUrl() {
    String dbUrl = dbProperties.getProperty("derby.url");
    if (!dbUrl.contains("mariadb") && !dbUrl.contains("postgresql")) {
      dbUrl += dbName;
    }
    return dbUrl;
  }

  /**
   * Inserts a new message record into the database.
   *
   * @param record the message to save
   * @return the generated message ID, or {@code null} on failure
   */
  public String saveRecord(Message record) {
    try {
      stmtSaveNewRecord.clearParameters();

      stmtSaveNewRecord.setString(1, record.getMsgId());
      if (dbProperties.getProperty("derby.url").contains("postgresql")) {
        stmtSaveNewRecord.setTimestamp(2, new java.sql.Timestamp(record.getDateTime().getTime()));
      } else {
        stmtSaveNewRecord.setString(2, dateFormatter.format(record.getDateTime()));
      }
      stmtSaveNewRecord.setString(3, record.getTitle());
      stmtSaveNewRecord.setString(4, record.getXml());
      if (dbProperties.getProperty("derby.url").contains("postgresql")) {
        stmtSaveNewRecord.setShort(5, (short) (record.getRead() ? 1 : 0));
        stmtSaveNewRecord.setShort(6, (short) (record.getAck() ? 1 : 0));
        stmtSaveNewRecord.setShort(7, (short) (record.getActioned() ? 1 : 0));
      } else {
        stmtSaveNewRecord.setBoolean(5, record.getRead());
        stmtSaveNewRecord.setBoolean(6, record.getAck());
        stmtSaveNewRecord.setBoolean(7, record.getActioned());
      }
      int rowCount = stmtSaveNewRecord.executeUpdate();

    } catch (SQLException sqle) {
      log.error("Error saving message record", sqle);
    }
    return record.getMsgId();
  }

  /**
   * Updates an existing message record in the database.
   *
   * @param record the message with updated fields
   * @return {@code true} if the record was updated successfully
   */
  public boolean editRecord(Message record) {
    boolean bEdited = false;
    try {
      stmtUpdateExistingRecord.clearParameters();

      if (dbProperties.getProperty("derby.url").contains("postgresql")) {
        stmtUpdateExistingRecord.setShort(1, (short) (record.getRead() ? 1 : 0));
        stmtUpdateExistingRecord.setShort(2, (short) (record.getAck() ? 1 : 0));
        stmtUpdateExistingRecord.setShort(3, (short) (record.getActioned() ? 1 : 0));
      } else {
        stmtUpdateExistingRecord.setBoolean(1, record.getRead());
        stmtUpdateExistingRecord.setBoolean(2, record.getAck());
        stmtUpdateExistingRecord.setBoolean(3, record.getActioned());
      }
      stmtUpdateExistingRecord.setString(4, record.getMsgId());

      stmtUpdateExistingRecord.executeUpdate();
      bEdited = true;
    } catch (SQLException sqle) {
      log.error("Error editing message record", sqle);
    }
    return bEdited;
  }

  /**
   * Deletes the message with the given ID from the database.
   *
   * @param msgId the message ID to delete
   * @return {@code true} if the record was deleted successfully
   */
  public boolean deleteRecord(String msgId) {
    boolean bDeleted = false;
    try {
      stmtDeleteAddress.clearParameters();
      stmtDeleteAddress.setString(1, msgId);
      stmtDeleteAddress.executeUpdate();
      bDeleted = true;
    } catch (SQLException sqle) {
      log.error("Error deleting message record", sqle);
    }

    return bDeleted;
  }

  /**
   * Deletes the given message record from the database.
   *
   * @param record the message to delete
   * @return {@code true} if the record was deleted successfully
   */
  public boolean deleteRecord(Message record) {
    String contactId = record.getMsgId();
    return deleteRecord(contactId);
  }

  public Vector getListEntries() {
    return getSelectiveEntries(strGetListEntries);
  }

  /**
   * Executes the given prepared statement and returns matching message entries.
   *
   * @param preparedStatement the SQL query to execute
   * @return a vector of matching {@link com.codetotime.eppclient.messages.Message} objects
   */
  public Vector getSelectiveEntries(String preparedStatement) {
    Vector listEntries = new Vector();

    Statement queryStatement = null;
    ResultSet results = null;

    try {
      queryStatement = dbConnection.createStatement();
      results = queryStatement.executeQuery(preparedStatement);
      while (results.next()) {
        Message message =
            new Message(
                results.getString(1),
                results.getTimestamp(2),
                results.getString(3),
                results.getString(4),
                results.getBoolean(5),
                results.getBoolean(6),
                results.getBoolean(7));
        listEntries.add(message);
      }
      results.close();

    } catch (SQLException sqle) {
      log.error("Error getting message list entries", sqle);
    }

    return listEntries;
  }

  /**
   * Retrieves the message with the given ID from the database.
   *
   * @param msgId the message ID to look up
   * @return the matching {@link com.codetotime.eppclient.messages.Message}, or {@code null} if not
   *     found
   */
  public Message getMessage(String msgId) {
    Message message = null;
    try {
      stmtGetAddress.clearParameters();
      stmtGetAddress.setString(1, msgId);
      ResultSet result = stmtGetAddress.executeQuery();
      if (result.next()) {
        java.util.Date datetime = result.getTimestamp("DATETIME");
        String title = result.getString("MESSAGE");
        String xml = result.getString("MSGXML");
        boolean read = result.getBoolean("READFLAG");
        boolean ack = result.getBoolean("ACKFLAG");
        boolean actioned = result.getBoolean("ACTIONEDFLAG");
        message = new Message(msgId, datetime, title, xml, read, ack, actioned);
      }
      result.close();
    } catch (SQLException sqle) {
      log.error("Error getting message", sqle);
    }

    return message;
  }

  private Connection dbConnection;
  private Properties dbProperties;
  private boolean isConnected;
  private String dbName;
  private PreparedStatement stmtSaveNewRecord;
  private PreparedStatement stmtUpdateExistingRecord;
  private PreparedStatement stmtGetListEntries;
  private PreparedStatement stmtGetRegistrantEntries;
  private PreparedStatement stmtGetAddress;
  private PreparedStatement stmtDeleteAddress;
  private PreparedStatement stmtDropMessageTable;

  private final String strDropMessageTable;
  private final String strCreateAddressTable;
  private final String strCreateAddressTableMYSQL;
  private final String strGetAddress;
  private final String strSaveAddress;
  private final String strGetListEntries;
  private final String strUpdateAddress;
  private final String strDeleteAddress;

  java.text.SimpleDateFormat dateFormatter = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
}
