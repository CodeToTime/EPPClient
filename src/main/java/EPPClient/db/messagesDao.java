/*
 * SPDX-FileCopyrightText: 2009-2025 AssoTLD <reg@assotld.it>
 * SPDX-FileCopyrightText: 2026 Riccardo Bertelli
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
import EPPClient.messages.Message;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Vector;

public class messagesDao
{

  private static final String BUNDLE_NAME = "EPPClient.db.messages";

  private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

  public static String getString(String key)
  {
    try
    {
      return RESOURCE_BUNDLE.getString(key);
    }
    catch (MissingResourceException e)
    {
      return '!' + key + '!';
    }
  }


  /**
   * Creates a new instance of AddressDao
   */
  public messagesDao()
  {
    this("messages");
  }

  public messagesDao(String addressBookName)
  {
    this.dbName = addressBookName;

    setDBSystemDir();
    dbProperties = loadDBProperties();

    strDropMessageTable = "drop table " + dbProperties.getProperty("db.schema") + "." + dbProperties.getProperty("db.table");

    strCreateAddressTable =
            "create table " + dbProperties.getProperty("db.schema") + "." + dbProperties.getProperty("db.table") + " (" +
                    "    MSGID                 VARCHAR(30 ) NOT NULL PRIMARY KEY," +
                    "    DATETIME              TIMESTAMP, " +
                    "    MESSAGE               VARCHAR(255), " +
                    "    MSGXML                CLOB, " +
                    "    READFLAG              SMALLINT, " +
                    "    ACKFLAG               SMALLINT, " +
                    "    ACTIONEDFLAG          SMALLINT" +
                    ")";

    strCreateAddressTableMYSQL =
            "create table " + dbProperties.getProperty("db.schema") + "." + dbProperties.getProperty("db.table") + " (" +
                    "    MSGID                 VARCHAR(30 ) NOT NULL PRIMARY KEY," +
                    "    DATETIME              TIMESTAMP DEFAULT '1970-01-01 01:00:01', " +
                    "    MESSAGE               VARCHAR(255), " +
                    "    MSGXML                TEXT, " +
                    "    READFLAG              SMALLINT, " +
                    "    ACKFLAG               SMALLINT, " +
                    "    ACTIONEDFLAG          SMALLINT" +
                    ")";

    strGetAddress =
            "SELECT * FROM " + dbProperties.getProperty("db.schema") + "." + dbProperties.getProperty("db.table") + " " +
                    "WHERE MSGID = ?";

    strSaveAddress =
            "INSERT INTO " + dbProperties.getProperty("db.schema") + "." + dbProperties.getProperty("db.table") + " " +
                    "   (MSGID, DATETIME, MESSAGE, MSGXML, READFLAG, ACKFLAG, ACTIONEDFLAG) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";

    strGetListEntries =
            "SELECT MSGID, DATETIME, MESSAGE, MSGXML, READFLAG, ACKFLAG, ACTIONEDFLAG FROM " + dbProperties.getProperty("db.schema") + "." + dbProperties.getProperty("db.table") + " " +
                    "ORDER BY DATETIME ASC, MSGID ASC";

    strUpdateAddress =
            "UPDATE " + dbProperties.getProperty("db.schema") + "." + dbProperties.getProperty("db.table") + " " +
                    "SET READFLAG = ?, " +
                    "    ACKFLAG = ?, " +
                    "    ACTIONEDFLAG = ? " +
                    "WHERE MSGID = ?";

    strDeleteAddress =
            "DELETE FROM " + dbProperties.getProperty("db.schema") + "." + dbProperties.getProperty("db.table") + " " +
                    "WHERE MSGID = ?";

    String driverName = dbProperties.getProperty("derby.driver");
    loadDatabaseDriver(driverName);
    if (!dbExists() && !dbProperties.getProperty("derby.url").contains("mysql"))
    {
      createDatabase();
    }
    else
    {
      connect();
      if (!tableExists(dbConnection, dbProperties.getProperty("db.table")))
      {
        createTables(dbConnection);
      }
      else
      {

      }

      if (false)
      {
        // Upgrade database
        connect();
        Statement statement = null;
        try
        {
          statement = dbConnection.createStatement();
          statement.execute("ALTER TABLE " + dbProperties.getProperty("db.schema") + "." + dbProperties.getProperty("db.table") + " ADD COLUMN STATUS VARCHAR(255)");
        }
        catch (SQLException ex)
        {
          ex.printStackTrace();
        }
        disconnect();
      }
    }
  }

  private boolean tableExists(Connection dbConnection, String tableName)
  {
    boolean tableExists = false;

    Statement statement = null;
    try
    {
      ResultSet rs = dbConnection.getMetaData().getTables(dbProperties.getProperty("db.schema"), null, "%", null);
      while (rs.next())
      {
        if (rs.getString(3).toLowerCase().equals(tableName.toLowerCase()))
          tableExists = true;
      }
    }
    catch (SQLException ex)
    {
      ex.printStackTrace();
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }

    return tableExists;
  }

  private boolean dbExists()
  {
    boolean bExists = false;
    String dbLocation = getDatabaseLocation();
    File dbFileDir = new File(dbLocation);
    if (dbFileDir.exists())
    {
      bExists = true;
    }
    return bExists;
  }

  private void setDBSystemDir()
  {
    // decide on the db system directory
    String userHomeDir = System.getProperty("user.home", ".");
    String systemDir = userHomeDir + "/.eppclient";
    System.setProperty("derby.system.home", systemDir);

    // create the db system directory
    File fileSystemDir = new File(systemDir);
    fileSystemDir.mkdir();
  }

  private void loadDatabaseDriver(String driverName)
  {
    // load Derby driver
    try
    {
      Class.forName(driverName);
    }
    catch (ClassNotFoundException ex)
    {
      ex.printStackTrace();
    }

  }

  private Properties loadDBProperties()
  {
    InputStream dbPropInputStream = null;
    dbPropInputStream = messagesDao.class.getResourceAsStream("messages.properties");
    dbProperties = new Properties();
    try
    {
      dbProperties.load(dbPropInputStream);
      dbProperties.put("derby.locks.monitor", "true");
      dbProperties.put("derby.locks.deadlockTrace", "true");
      dbProperties.put("derby.language.logStatementText", "true");
      dbProperties.put("derby.driver", "org.apache.derby.jdbc.EmbeddedDriver");
      dbProperties.put("derby.url", EPPparams.getParameter("EppClient.dburl"));
      dbProperties.put("db.schema", EPPparams.getParameter("EppClient.dbname"));
      dbProperties.put("user", EPPparams.getParameter("EppClient.dbuid"));
      dbProperties.put("password", EPPparams.getParameter("EppClient.dbpwd"));

    }
    catch (IOException ex)
    {
      ex.printStackTrace();
    }
    return dbProperties;
  }


  private boolean createTables(Connection dbConnection)
  {
    System.out.println("Creazione tabelle in corso...");
    boolean bCreatedTables = false;
    Statement statement = null;
    try
    {
      statement = dbConnection.createStatement();
      if (dbProperties.getProperty("derby.url").contains("mysql"))
      {
        statement.execute(strCreateAddressTableMYSQL);
      }
      else
      {
        statement.execute(strCreateAddressTable);
      }
      bCreatedTables = true;
      try
      {
        Thread.sleep(1000);
      }
      catch (InterruptedException e)
      {
        e.printStackTrace();
      }
    }
    catch (SQLException ex)
    {
      ex.printStackTrace();
    }
    return bCreatedTables;
  }

  public boolean dropTables()
  {
    boolean bDroppedTables = false;
    try
    {
      stmtDropMessageTable.clearParameters();
      stmtDropMessageTable.execute();
      bDroppedTables = true;
    }
    catch (SQLException ex)
    {
      ex.printStackTrace();
    }
    return bDroppedTables;
  }

  private boolean createDatabase()
  {
    boolean bCreated = false;
    Connection dbConnection = null;

    String dbUrl = getDatabaseUrl();
    dbProperties.put("create", "true");
    try
    {
      dbConnection = DriverManager.getConnection(dbUrl, dbProperties);
      bCreated = createTables(dbConnection);
    }
    catch (SQLException ex)
    {
      ex.printStackTrace();
    }
    dbProperties.remove("create");
    return bCreated;
  }


  public boolean connect()
  {
    String dbUrl = getDatabaseUrl();

    try
    {
      dbProperties.put("shutdown", "false");
      dbConnection = DriverManager.getConnection(dbUrl, dbProperties);
      stmtSaveNewRecord = dbConnection.prepareStatement(strSaveAddress, Statement.RETURN_GENERATED_KEYS);
      stmtUpdateExistingRecord = dbConnection.prepareStatement(strUpdateAddress);
      stmtGetAddress = dbConnection.prepareStatement(strGetAddress);
      stmtDeleteAddress = dbConnection.prepareStatement(strDeleteAddress);
      stmtDropMessageTable = dbConnection.prepareStatement(strDropMessageTable);

      isConnected = dbConnection != null;
    }
    catch (SQLException ex)
    {
      isConnected = false;
      System.out.println("errore!!");
      ex.printStackTrace();
    }
    return isConnected;
  }

  private String getHomeDir()
  {
    return System.getProperty("user.home");
  }

  public void disconnect()
  {
    if (isConnected)
    {
      String dbUrl = getDatabaseUrl();
      dbProperties.put("shutdown", "true");
      try
      {
        DriverManager.getConnection(dbUrl, dbProperties);
      }
      catch (SQLException ex)
      {
      }
      isConnected = false;
    }
  }

  public String getDatabaseLocation()
  {
    String dbLocation = System.getProperty("derby.system.home") + "/" + dbName;
    return dbLocation;
  }

  public String getDatabaseUrl()
  {
    String dbUrl = dbProperties.getProperty("derby.url");
    if (!dbUrl.contains("mysql"))
      dbUrl += dbName;
    return dbUrl;
  }


  public String saveRecord(Message record)
  {
    try
    {
      stmtSaveNewRecord.clearParameters();

      stmtSaveNewRecord.setString(1, record.getMsgId());
      stmtSaveNewRecord.setString(2, dateFormatter.format(record.getDateTime()));
      stmtSaveNewRecord.setString(3, record.getTitle());
      stmtSaveNewRecord.setString(4, record.getXml());
      stmtSaveNewRecord.setBoolean(5, record.getRead());
      stmtSaveNewRecord.setBoolean(6, record.getAck());
      stmtSaveNewRecord.setBoolean(7, record.getActioned());
      int rowCount = stmtSaveNewRecord.executeUpdate();

    }
    catch (SQLException sqle)
    {
      sqle.printStackTrace();
    }
    return record.getMsgId();
  }

  public boolean editRecord(Message record)
  {
    boolean bEdited = false;
    try
    {
      stmtUpdateExistingRecord.clearParameters();


      stmtUpdateExistingRecord.setBoolean(1, record.getRead());
      stmtUpdateExistingRecord.setBoolean(2, record.getAck());
      stmtUpdateExistingRecord.setBoolean(3, record.getActioned());
      stmtUpdateExistingRecord.setString(4, record.getMsgId());

      stmtUpdateExistingRecord.executeUpdate();
      bEdited = true;
    }
    catch (SQLException sqle)
    {
      sqle.printStackTrace();
    }
    return bEdited;
  }

  public boolean deleteRecord(String msgId)
  {
    boolean bDeleted = false;
    try
    {
      stmtDeleteAddress.clearParameters();
      stmtDeleteAddress.setString(1, msgId);
      stmtDeleteAddress.executeUpdate();
      bDeleted = true;
    }
    catch (SQLException sqle)
    {
      sqle.printStackTrace();
    }

    return bDeleted;
  }

  public boolean deleteRecord(Message record)
  {
    String contactId = record.getMsgId();
    return deleteRecord(contactId);
  }

  public Vector getListEntries()
  {
    return getSelectiveEntries(strGetListEntries);
  }

  public Vector getSelectiveEntries(String preparedStatement)
  {
    Vector listEntries = new Vector();

    Statement queryStatement = null;
    ResultSet results = null;

    try
    {
      queryStatement = dbConnection.createStatement();
      results = queryStatement.executeQuery(preparedStatement);
      while (results.next())
      {
        Message message = new Message(results.getString(1), results.getTimestamp(2), results.getString(3), results.getString(4), results.getBoolean(5), results.getBoolean(6), results.getBoolean(7));
        listEntries.add(message);
      }
      results.close();

    }
    catch (SQLException sqle)
    {
      sqle.printStackTrace();

    }

    return listEntries;
  }

  public Message getMessage(String msgId)
  {
    Message message = null;
    try
    {
      stmtGetAddress.clearParameters();
      stmtGetAddress.setString(1, msgId);
      ResultSet result = stmtGetAddress.executeQuery();
      if (result.next())
      {
        java.util.Date datetime = result.getTimestamp("DATETIME");
        String title = result.getString("MESSAGE");
        String xml = result.getString("MSGXML");
        boolean read = result.getBoolean("READFLAG");
        boolean ack = result.getBoolean("ACKFLAG");
        boolean actioned = result.getBoolean("ACTIONEDFLAG");
        message = new Message(msgId, datetime, title, xml, read, ack, actioned);
      }
      result.close();
    }
    catch (SQLException sqle)
    {
      sqle.printStackTrace();
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
