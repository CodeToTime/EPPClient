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
import EPPClient.contacts.Address;
import EPPClient.contacts.ListEntry;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.*;

public class contactsDao
{

  private static final String BUNDLE_NAME = "EPPClient.db.contacts";

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
  public contactsDao()
  {
    this("contacts");
  }

  public contactsDao(String addressBookName)
  {
    this.dbName = addressBookName;

    setDBSystemDir();
    dbProperties = loadDBProperties();

    if (dbProperties.getProperty("derby.url").contains("postgresql"))
    {
      dbProperties.put("db.schema", "public");
    }

    strDropContactTable = "drop table " + dbProperties.getProperty("db.schema") + "." + dbProperties.getProperty("db.table");

    strCreateAddressTable =
            "create table " + dbProperties.getProperty("db.schema") + "." + dbProperties.getProperty("db.table") + " (" +
                    "    CONTACTID             VARCHAR(30 ) NOT NULL PRIMARY KEY," +
                    "    NAME                  VARCHAR(255), " +
                    "    ORG                   VARCHAR(255), " +
                    "    STREET                VARCHAR(255), " +
                    "    CITY                  VARCHAR(255), " +
                    "    STATEORPROVINCE       VARCHAR(255), " +
                    "    POSTALCODE            VARCHAR(10 ), " +
                    "    COUNTRYCODE           VARCHAR(2  ), " +
                    "    VOICE                 VARCHAR(255), " +
                    "    FAX                   VARCHAR(255), " +
                    "    EMAIL                 VARCHAR(255), " +
                    "    CONSENTFORPUBLISHING  SMALLINT    , " +
                    "    ISREGISTRANT          SMALLINT    , " +
                    "    NATIONALITYCODE       VARCHAR(2  ), " +
                    "    ENTITYTYPE            INT         , " +
                    "    REGCODE               VARCHAR(16 ), " +
                    "    STATUS                VARCHAR(255), " +
                    "    SCHOOLCODE            VARCHAR(64 ), " +
                    "    UOCODE                VARCHAR(64 ), " +
                    "    IPACODE               VARCHAR(64 )  " +
                    ")";

    strGetAddress =
            "SELECT * FROM " + dbProperties.getProperty("db.schema") + "." + dbProperties.getProperty("db.table") + " " +
                    "WHERE CONTACTID = ?";

    strSaveAddress =
            "INSERT INTO " + dbProperties.getProperty("db.schema") + "." + dbProperties.getProperty("db.table") + " " +
                    "   (CONTACTID, NAME, ORG, STREET, CITY, STATEORPROVINCE, POSTALCODE, " +
                    "    COUNTRYCODE, VOICE, FAX, EMAIL, CONSENTFORPUBLISHING, ISREGISTRANT, " +
                    "    NATIONALITYCODE, ENTITYTYPE, REGCODE, STATUS, SCHOOLCODE, UOCODE, IPACODE) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";


    strGetListEntries =
            "SELECT CONTACTID, NAME FROM " + dbProperties.getProperty("db.schema") + "." + dbProperties.getProperty("db.table") + " " +
                    "ORDER BY NAME ASC";

    strGetRegistrantEntries =
            "SELECT CONTACTID, NAME FROM " + dbProperties.getProperty("db.schema") + "." + dbProperties.getProperty("db.table") + " " +
                    "WHERE ISREGISTRANT = 1 ORDER BY NAME ASC";

    strUpdateAddress =
            "UPDATE " + dbProperties.getProperty("db.schema") + "." + dbProperties.getProperty("db.table") + " " +
                    "SET NAME = ?, " +
                    "    ORG = ?, " +
                    "    STREET = ?, " +
                    "    CITY = ?, " +
                    "    STATEORPROVINCE = ?, " +
                    "    POSTALCODE = ?, " +
                    "    COUNTRYCODE = ?, " +
                    "    VOICE = ?, " +
                    "    FAX = ?, " +
                    "    EMAIL = ?, " +
                    "    CONSENTFORPUBLISHING = ? ," +
                    "    ISREGISTRANT = ? ," +
                    "    NATIONALITYCODE = ? ," +
                    "    ENTITYTYPE = ? ," +
                    "    REGCODE = ? ," +
                    "    STATUS = ? ," +
                    "    SCHOOLCODE = ?, " +
                    "    UOCODE = ?, " +
                    "    IPACODE = ? " +
                    "WHERE CONTACTID = ?";

    strDeleteAddress =
            "DELETE FROM " + dbProperties.getProperty("db.schema") + "." + dbProperties.getProperty("db.table") + " " +
                    "WHERE CONTACTID = ?";

    String driverName = dbProperties.getProperty("derby.driver");
    loadDatabaseDriver(driverName);
    if (!dbExists() && !dbProperties.getProperty("derby.url").contains("mariadb") && !dbProperties.getProperty("derby.url").contains("postgresql"))
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
        // Aggiorna lo schema: aggiunge le colonne mancanti se non esistono
        String tableName = dbProperties.getProperty("db.table");
        String fullTableName = dbProperties.getProperty("db.schema") + "." + tableName;

        addColumnIfNotExists(dbConnection, tableName, "SCHOOLCODE", "VARCHAR(64)");
        addColumnIfNotExists(dbConnection, tableName, "UOCODE", "VARCHAR(64)");
        addColumnIfNotExists(dbConnection, tableName, "IPACODE", "VARCHAR(64)");
      }
    }
  }

  /**
   * Verifica se una colonna esiste nella tabella specificata.
   * Gestisce le differenze di case-sensitivity tra Derby (maiuscolo) e MySQL/MariaDB/PostgreSQL (minuscolo).
   */
  private boolean columnExists(Connection conn, String tableName, String columnName)
  {
    try
    {
      // Prova prima con il nome tabella originale (funziona per MySQL/MariaDB/PostgreSQL con nomi minuscoli)
      ResultSet columns = conn.getMetaData().getColumns(null, null, tableName, null);
      while (columns.next())
      {
        if (columns.getString("COLUMN_NAME").equalsIgnoreCase(columnName))
        {
          columns.close();
          return true;
        }
      }
      columns.close();

      // Prova con il nome tabella in maiuscolo (funziona per Derby)
      columns = conn.getMetaData().getColumns(null, null, tableName.toUpperCase(), null);
      while (columns.next())
      {
        if (columns.getString("COLUMN_NAME").equalsIgnoreCase(columnName))
        {
          columns.close();
          return true;
        }
      }
      columns.close();
    }
    catch (SQLException ex)
    {
      ex.printStackTrace();
    }
    return false;
  }

  /**
   * Aggiunge una colonna alla tabella se non esiste già.
   */
  private void addColumnIfNotExists(Connection conn, String tableName, String columnName, String columnType)
  {
    if (columnExists(conn, tableName, columnName))
    {
      return; // La colonna esiste già, niente da fare
    }

    String fullTableName = dbProperties.getProperty("db.schema") + "." + tableName;
    Statement statement = null;
    try
    {
      statement = conn.createStatement();
      statement.execute("ALTER TABLE " + fullTableName + " ADD COLUMN " + columnName + " " + columnType);
      System.out.println("Aggiunta colonna " + columnName + " alla tabella " + fullTableName);
    }
    catch (SQLException ex)
    {
      // La colonna potrebbe già esistere (race condition) o altro errore
      ex.printStackTrace();
    }
    finally
    {
      if (statement != null)
      {
        try { statement.close(); } catch (SQLException e) { /* ignore */ }
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
    try
    {
      Class.forName(driverName);
    }
    catch (ClassNotFoundException ex)
    {
      if ("org.mariadb.jdbc.Driver".equals(driverName) || "org.postgresql.Driver".equals(driverName))
      {
        try
        {
          Class.forName(driverName);
        }
        catch (ClassNotFoundException e)
        {
          ex.printStackTrace();
        }
      }
      else
      {
        ex.printStackTrace();
      }
    }

  }

  private Properties loadDBProperties()
  {
    InputStream dbPropInputStream = null;
    dbPropInputStream = contactsDao.class.getResourceAsStream("contacts.properties");
    dbProperties = new Properties();
    try
    {
      dbProperties.load(dbPropInputStream);

      String dbUrl = EPPparams.getParameter("EppClient.dburl");
      if (dbUrl.contains("mariadb"))
      {
        dbProperties.put("derby.driver", "org.mariadb.jdbc.Driver");
      }
      else if (dbUrl.contains("postgresql"))
      {
        dbProperties.put("derby.driver", "org.postgresql.Driver");
      }
      else
      {
        dbProperties.put("derby.driver", "org.apache.derby.jdbc.EmbeddedDriver");
      }
      dbProperties.put("derby.url", dbUrl);
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
    boolean bCreatedTables = false;
    Statement statement = null;
    try
    {
      statement = dbConnection.createStatement();
      String createTableSql = strCreateAddressTable;
      if (dbProperties.getProperty("derby.url").contains("mariadb") || dbProperties.getProperty("derby.url").contains("postgresql"))
      {
        createTableSql = strCreateAddressTable.replace("create table ", "create table IF NOT EXISTS ");
      }
      statement.execute(createTableSql);
      bCreatedTables = true;
    }
    catch (SQLException ex)
    {
      if (ex.getSQLState().equals("X0Y32") || ex.getMessage().contains("already exists")) {
        bCreatedTables = true;
      } else {
        ex.printStackTrace();
      }
    }
    return bCreatedTables;
  }

  public boolean dropTables()
  {
    boolean bDroppedTables = false;
    try
    {
      stmtDropContactTable.clearParameters();
      stmtDropContactTable.execute();
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
      stmtDropContactTable = dbConnection.prepareStatement(strDropContactTable);

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
    if (!dbUrl.contains("mariadb") && !dbUrl.contains("postgresql"))
      dbUrl += dbName;
    return dbUrl;
  }


  public String saveRecord(Address record)
  {
    try
    {
      stmtSaveNewRecord.clearParameters();

      stmtSaveNewRecord.setString(1, record.getContactId());
      stmtSaveNewRecord.setString(2, record.getContactName());
      stmtSaveNewRecord.setString(3, record.getOrg());
      stmtSaveNewRecord.setString(4, record.getStreet());
      stmtSaveNewRecord.setString(5, record.getCity());
      stmtSaveNewRecord.setString(6, record.getStateOrProvince());
      stmtSaveNewRecord.setString(7, record.getPostalCode());
      stmtSaveNewRecord.setString(8, record.getCountryCode());
      stmtSaveNewRecord.setString(9, record.getVoice());
      stmtSaveNewRecord.setString(10, record.getFax());
      stmtSaveNewRecord.setString(11, record.getEmail());
      if (dbProperties.getProperty("derby.url").contains("postgresql"))
      {
        stmtSaveNewRecord.setShort(12, (short) (record.getConsentForPublishing() ? 1 : 0));
        stmtSaveNewRecord.setShort(13, (short) (record.getIsRegistrant() ? 1 : 0));
      }
      else
      {
        stmtSaveNewRecord.setBoolean(12, record.getConsentForPublishing());
        stmtSaveNewRecord.setBoolean(13, record.getIsRegistrant());
      }
      stmtSaveNewRecord.setString(14, record.getNationalityCode());
      stmtSaveNewRecord.setInt(15, record.getEntityType());
      stmtSaveNewRecord.setString(16, record.getRegCode());
      stmtSaveNewRecord.setString(17, "");
      stmtSaveNewRecord.setString(18, record.getSchoolCode());
      stmtSaveNewRecord.setString(19, record.getUoCode());
      stmtSaveNewRecord.setString(20, record.getIpaCode());
      int rowCount = stmtSaveNewRecord.executeUpdate();
//            ResultSet results = stmtSaveNewRecord.getGeneratedKeys();
//            if (results.next()) {
//                id = results.getInt(1);
//            }

    }
    catch (SQLException sqle)
    {
      sqle.printStackTrace();
    }
    return record.getContactId();
  }

  public boolean editRecord(Address record)
  {
    boolean bEdited = false;
    try
    {
      stmtUpdateExistingRecord.clearParameters();


      stmtUpdateExistingRecord.setString(1, record.getContactName());
      stmtUpdateExistingRecord.setString(2, record.getOrg());
      stmtUpdateExistingRecord.setString(3, record.getStreet());
      stmtUpdateExistingRecord.setString(4, record.getCity());
      stmtUpdateExistingRecord.setString(5, record.getStateOrProvince());
      stmtUpdateExistingRecord.setString(6, record.getPostalCode());
      stmtUpdateExistingRecord.setString(7, record.getCountryCode());
      stmtUpdateExistingRecord.setString(8, record.getVoice());
      stmtUpdateExistingRecord.setString(9, record.getFax());
      stmtUpdateExistingRecord.setString(10, record.getEmail());
      if (dbProperties.getProperty("derby.url").contains("postgresql"))
      {
        stmtUpdateExistingRecord.setShort(11, (short) (record.getConsentForPublishing() ? 1 : 0));
        stmtUpdateExistingRecord.setShort(12, (short) (record.getIsRegistrant() ? 1 : 0));
      }
      else
      {
        stmtUpdateExistingRecord.setBoolean(11, record.getConsentForPublishing());
        stmtUpdateExistingRecord.setBoolean(12, record.getIsRegistrant());
      }
      stmtUpdateExistingRecord.setString(13, record.getNationalityCode());
      stmtUpdateExistingRecord.setInt(14, record.getEntityType());
      stmtUpdateExistingRecord.setString(15, record.getRegCode());

      String newStatus = "";
      int[] newStatuses = record.getNewStatus();

      if (newStatuses != null)
      {
        for (int statusIndex = 0; statusIndex < newStatuses.length; statusIndex++)
        {
          if (newStatus.length() > 0) newStatus = newStatus + ",";
          newStatus = newStatus + newStatuses[statusIndex];
        }
      }

      stmtUpdateExistingRecord.setString(16, newStatus);
      stmtUpdateExistingRecord.setString(17, record.getSchoolCode());
      stmtUpdateExistingRecord.setString(18, record.getUoCode());
      stmtUpdateExistingRecord.setString(19, record.getIpaCode());
      stmtUpdateExistingRecord.setString(20, record.getContactId());  // shifted from 17 to 20

      stmtUpdateExistingRecord.executeUpdate();
      bEdited = true;
    }
    catch (SQLException sqle)
    {
      sqle.printStackTrace();
    }
    return bEdited;

  }

  public boolean deleteRecord(String contactID)
  {
    boolean bDeleted = false;
    try
    {
      stmtDeleteAddress.clearParameters();
      stmtDeleteAddress.setString(1, contactID);
      stmtDeleteAddress.executeUpdate();
      bDeleted = true;
    }
    catch (SQLException sqle)
    {
      sqle.printStackTrace();
    }

    return bDeleted;
  }

  public boolean deleteRecord(Address record)
  {
    String contactId = record.getContactId();
    return deleteRecord(contactId);
  }

  public List<ListEntry> getRegistrantEntries()
  {
    return getSelectiveEntries(strGetRegistrantEntries);
  }

  public List<ListEntry> getListEntries()
  {
    return getSelectiveEntries(strGetListEntries);
  }

  public List<ListEntry> getSelectiveEntries(String preparedStatement)
  {
    List<ListEntry> listEntries = new ArrayList<ListEntry>();
    Statement queryStatement = null;
    ResultSet results = null;

    try
    {
      queryStatement = dbConnection.createStatement();
      results = queryStatement.executeQuery(preparedStatement);
      while (results.next())
      {
        String contactId = results.getString(1);
        String name = results.getString(2);

        ListEntry entry = new ListEntry(name, contactId);
        listEntries.add(entry);
      }
      results.close();
    }
    catch (SQLException sqle)
    {
      sqle.printStackTrace();

    }

    return listEntries;
  }

  public Address getAddress(String contactId)
  {
    Address address = null;
    try
    {
      stmtGetAddress.clearParameters();
      stmtGetAddress.setString(1, contactId);
      ResultSet result = stmtGetAddress.executeQuery();
      if (result.next())
      {
        String name = result.getString("NAME");
        String org = result.getString("ORG");
        String street = result.getString("STREET");
        String city = result.getString("CITY");
        String stateOrProvince = result.getString("STATEORPROVINCE");
        String postalCode = result.getString("POSTALCODE");
        String countryCode = result.getString("COUNTRYCODE");
        String voice = result.getString("VOICE");
        String fax = result.getString("FAX");
        String email = result.getString("EMAIL");
        boolean consentForPublishing = result.getBoolean("CONSENTFORPUBLISHING");
        boolean isRegistrant = result.getBoolean("ISREGISTRANT");
        String nationalityCode = result.getString("NATIONALITYCODE");
        int entityType = result.getInt("ENTITYTYPE");
        String regCode = result.getString("REGCODE");
        String schoolCode = result.getString("SCHOOLCODE");
        String uoCode = result.getString("UOCODE");
        String ipaCode = result.getString("IPACODE");


        int[] oldStatus;
        if (result.getString("STATUS") != null)
        {
          if (result.getString("STATUS").length() > 0)
          {
            String[] oldStatuses = result.getString("STATUS").split(",");
            oldStatus = new int[oldStatuses.length];
            for (int statusIndex = 0; statusIndex < oldStatuses.length; statusIndex++)
            {
              oldStatus[statusIndex] = Integer.valueOf(oldStatuses[statusIndex]);
            }
          }
          else
          {
            oldStatus = null;
          }
        }
        else
        {
          oldStatus = null;
        }

        address = new Address(name, org, street, city, stateOrProvince, postalCode, countryCode,
                voice, fax, email, consentForPublishing, isRegistrant,
                nationalityCode, entityType, regCode, oldStatus, contactId,
                schoolCode, ipaCode, uoCode);
      }
      result.close();
    }
    catch (SQLException sqle)
    {
      sqle.printStackTrace();
    }

    return address;
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
  private PreparedStatement stmtDropContactTable;

  private final String strDropContactTable;
  private final String strCreateAddressTable;
  private final String strGetAddress;
  private final String strSaveAddress;
  private final String strGetListEntries;
  private final String strGetRegistrantEntries;
  private final String strUpdateAddress;
  private final String strDeleteAddress;

}
