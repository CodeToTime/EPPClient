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
import EPPClient.domains.Domain;
import EPPClient.domains.ListEntry;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.*;

public class domainsDao
{

  private static final String BUNDLE_NAME = "EPPClient.db.domains";

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
  public domainsDao()
  {
    this("domains");
  }

  public domainsDao(String addressBookName)
  {
    this.dbName = addressBookName;

    setDBSystemDir();
    dbProperties = loadDBProperties();

    strDropDomainTable = "drop table " + dbProperties.getProperty("db.schema") + "." + dbProperties.getProperty("db.table");

    strCreateAddressTable =
            "create table " + dbProperties.getProperty("db.schema") + "." + dbProperties.getProperty("db.table") + " (" +
                    "    DOMAINNAME     VARCHAR(255) NOT NULL PRIMARY KEY," +
                    "    REGISTRANT     VARCHAR(255), " +
                    "    ADMIN          VARCHAR(255), " +
                    "    TECH           VARCHAR(255), " +
                    "    NAMESERVER     VARCHAR(255), " +
                    "    AUTHINFO       VARCHAR(255), " +
                    "    STATUS         VARCHAR(255), " +
                    "    EXPIRE         DATE, " +
                    "    VALIDATIONCODE VARCHAR(255), " +
                    "    ISDNSSEC       SMALLINT, " +
                    "    DNSSECKEYTAG   VARCHAR(255), " +
                    "    DNSSECALG      INTEGER, " +
                    "    DNSSECDIGESTTYPE INTEGER, " +
                    "    DNSSECDIGEST   VARCHAR(255) " +
                    ")";

    strGetAddress =
            "SELECT * FROM " + dbProperties.getProperty("db.schema") + "." + dbProperties.getProperty("db.table") + " " +
                    "WHERE DOMAINNAME = ?";

    strSaveAddress =
            "INSERT INTO " + dbProperties.getProperty("db.schema") + "." + dbProperties.getProperty("db.table") + " " +
                    "   (DOMAINNAME, REGISTRANT, ADMIN, TECH, NAMESERVER, AUTHINFO, STATUS, EXPIRE, VALIDATIONCODE, ISDNSSEC, DNSSECKEYTAG, DNSSECALG, DNSSECDIGESTTYPE, DNSSECDIGEST) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    strGetListEntries =
            "SELECT DOMAINNAME FROM " + dbProperties.getProperty("db.schema") + "." + dbProperties.getProperty("db.table") + " " +
                    "ORDER BY DOMAINNAME ASC";

    strUpdateAddress =
            "UPDATE " + dbProperties.getProperty("db.schema") + "." + dbProperties.getProperty("db.table") + " " +
                    "SET REGISTRANT = ?, " +
                    "    ADMIN = ?, " +
                    "    TECH = ?, " +
                    "    NAMESERVER = ?, " +
                    "    AUTHINFO = ?, " +
                    "    STATUS = ?, " +
                    "    EXPIRE = ?, " +
                    "    VALIDATIONCODE = ?, " +
                    "    ISDNSSEC = ?, " +
                    "    DNSSECKEYTAG = ?, " +
                    "    DNSSECALG = ?, " +
                    "    DNSSECDIGESTTYPE = ?, " +
                    "    DNSSECDIGEST = ? " +
                    "WHERE DOMAINNAME = ?";

    strDeleteAddress =
            "DELETE FROM " + dbProperties.getProperty("db.schema") + "." + dbProperties.getProperty("db.table") + " " +
                    "WHERE DOMAINNAME = ?";

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
        // Aggiorna lo schema: aggiunge le colonne mancanti se non esistono
        String tableName = dbProperties.getProperty("db.table");

        addColumnIfNotExists(dbConnection, tableName, "EXPIRE", "DATE");
        addColumnIfNotExists(dbConnection, tableName, "VALIDATIONCODE", "VARCHAR(255)");
        addColumnIfNotExists(dbConnection, tableName, "ISDNSSEC", "SMALLINT");
        addColumnIfNotExists(dbConnection, tableName, "DNSSECKEYTAG", "VARCHAR(255)");
        addColumnIfNotExists(dbConnection, tableName, "DNSSECALG", "INTEGER");
        addColumnIfNotExists(dbConnection, tableName, "DNSSECDIGESTTYPE", "INTEGER");
        addColumnIfNotExists(dbConnection, tableName, "DNSSECDIGEST", "VARCHAR(255)");
      }
    }
  }

  /**
   * Verifica se una colonna esiste nella tabella specificata.
   * Gestisce le differenze di case-sensitivity tra Derby (maiuscolo) e MySQL (minuscolo).
   */
  private boolean columnExists(Connection conn, String tableName, String columnName)
  {
    try
    {
      // Prova prima con il nome tabella originale (funziona per MySQL con nomi minuscoli)
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
      if ("com.mysql.cj.jdbc.Driver".equals(driverName))
      {
        try
        {
          Class.forName("com.mysql.jdbc.Driver");
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
    dbPropInputStream = domainsDao.class.getResourceAsStream("domains.properties");
    dbProperties = new Properties();
    try
    {
      dbProperties.load(dbPropInputStream);

      String dbUrl = EPPparams.getParameter("EppClient.dburl");
      if (dbUrl.contains("mysql"))
      {
        dbProperties.put("derby.driver", "com.mysql.cj.jdbc.Driver");
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
      statement.execute(strCreateAddressTable);
      bCreatedTables = true;
      EPPparams.setParameter("EppClient.dblevel", "1");
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
      stmtDropDomainTable.clearParameters();
      stmtDropDomainTable.execute();
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
      stmtDropDomainTable = dbConnection.prepareStatement(strDropDomainTable);

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


  public String saveRecord(Domain record)
  {
    try
    {
      stmtSaveNewRecord.clearParameters();

      stmtSaveNewRecord.setString(1, record.getDomainName());
      stmtSaveNewRecord.setString(2, record.getRegistrant());

      String newAdmin = "";
      String[] newAdmins = record.getAdmin();
      if (newAdmins != null)
      {
        for (int adminIndex = 0; adminIndex < newAdmins.length; adminIndex++)
        {
          if (newAdmin.length() > 0) newAdmin = newAdmin + ";";
          newAdmin = newAdmin + newAdmins[adminIndex];
        }
      }
      stmtSaveNewRecord.setString(3, newAdmin);

      String newTech = "";
      String[] newTechs = record.getTech();
      if (newTechs != null)
      {
        for (int techIndex = 0; techIndex < newTechs.length; techIndex++)
        {
          if (newTech.length() > 0) newTech = newTech + ";";
          newTech = newTech + newTechs[techIndex];
        }
      }
      stmtSaveNewRecord.setString(4, newTech);


      String newNameServer = "";
      String[] newNameServers = record.getNameServer();
      if (newNameServers != null)
      {
        for (int nameServerIndex = 0; nameServerIndex < newNameServers.length; nameServerIndex++)
        {
          if (newNameServer.length() > 0) newNameServer = newNameServer + ";";
          newNameServer = newNameServer + newNameServers[nameServerIndex];
        }
      }


      stmtSaveNewRecord.setString(5, newNameServer);
      stmtSaveNewRecord.setString(6, record.getAuthInfo());

      String newStatus = "";
      if (record.getNewStatusV() != null)
        for (int statusIndex = 0; statusIndex < record.getNewStatusV().size(); statusIndex++)
        {
          if (newStatus.length() > 0) newStatus = newStatus + ",";
          newStatus += record.getNewStatusV().get(statusIndex);
        }

/*            int[] newStatuses = record.getNewStatus();
            if (newStatuses != null) {
                for (int statusIndex = 0; statusIndex < newStatuses.length; statusIndex++) {
                    if (newStatus.length() > 0) newStatus = newStatus + ",";
                    newStatus = newStatus + String.valueOf(newStatuses[statusIndex]);
                }
            }*/
      stmtSaveNewRecord.setString(7, newStatus);

      if (record.getExpire() instanceof java.util.Date)
      {
        stmtSaveNewRecord.setDate(8, new java.sql.Date(record.getExpire().getTime()));
      }
      else
      {
        stmtSaveNewRecord.setDate(8, null);
      }
      stmtSaveNewRecord.setString(9, record.getValidationCode());
      stmtSaveNewRecord.setInt(10, record.isDNSSec() ? 1 : 0);
      stmtSaveNewRecord.setString(11, record.getKeyTag());
      stmtSaveNewRecord.setInt(12, record.getAlg());
      stmtSaveNewRecord.setInt(13, record.getDigestType());
      stmtSaveNewRecord.setString(14, record.getDigest());
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
    return record.getDomainName();
  }

  public boolean editRecord(Domain record)
  {
    boolean bEdited = false;
    try
    {
      stmtUpdateExistingRecord.clearParameters();


      stmtUpdateExistingRecord.setString(1, record.getRegistrant());

      String newAdmin = "";
      String[] newAdmins = record.getAdmin();
      if (newAdmins != null)
      {
        for (int adminIndex = 0; adminIndex < newAdmins.length; adminIndex++)
        {
          if (newAdmin.length() > 0) newAdmin = newAdmin + ";";
          newAdmin = newAdmin + newAdmins[adminIndex];
        }
      }
      stmtUpdateExistingRecord.setString(2, newAdmin);

      String newTech = "";
      String[] newTechs = record.getTech();
      if (newTechs != null)
      {
        for (int techIndex = 0; techIndex < newTechs.length; techIndex++)
        {
          if (newTech.length() > 0) newTech = newTech + ";";
          newTech = newTech + newTechs[techIndex];
        }
      }
      stmtUpdateExistingRecord.setString(3, newTech);

      String newNameServer = "";
      String[] newNameServers = record.getNameServer();
      if (newNameServers != null)
      {
        for (int nameServerIndex = 0; nameServerIndex < newNameServers.length; nameServerIndex++)
        {
          if (newNameServer.length() > 0) newNameServer = newNameServer + ";";
          newNameServer = newNameServer + newNameServers[nameServerIndex];
        }
      }

      stmtUpdateExistingRecord.setString(4, newNameServer);
      stmtUpdateExistingRecord.setString(5, record.getAuthInfo());

      String newStatus = "";
      for (int statusIndex = 0; statusIndex < record.getNewStatusV().size(); statusIndex++)
      {
        if (newStatus.length() > 0) newStatus = newStatus + ",";
        newStatus += record.getNewStatusV().get(statusIndex);
      }


/*            int[] newStatuses = record.getNewStatus();
            if (newStatuses != null) {
                for (int statusIndex = 0; statusIndex < newStatuses.length; statusIndex++) {
                    if (newStatus.length() > 0) newStatus = newStatus + ",";
                    newStatus = newStatus + String.valueOf(newStatuses[statusIndex]);
                }
            }*/

      stmtUpdateExistingRecord.setString(6, newStatus);
      if (record.getExpire() instanceof java.util.Date)
      {
        stmtUpdateExistingRecord.setDate(7, new java.sql.Date(record.getExpire().getTime()));
      }
      else
      {
        stmtUpdateExistingRecord.setDate(7, null);
      }

      stmtUpdateExistingRecord.setString(8, record.getValidationCode());

      stmtUpdateExistingRecord.setInt(9, record.isDNSSec() ? 1 : 0);
      stmtUpdateExistingRecord.setString(10, record.getKeyTag());
      stmtUpdateExistingRecord.setInt(11, record.getAlg());
      stmtUpdateExistingRecord.setInt(12, record.getDigestType());
      stmtUpdateExistingRecord.setString(13, record.getDigest());
      stmtUpdateExistingRecord.setString(14, record.getDomainName());
      stmtUpdateExistingRecord.executeUpdate();
      bEdited = true;
    }
    catch (SQLException sqle)
    {
      sqle.printStackTrace();
    }
    return bEdited;

  }

  public boolean deleteRecord(String domainName)
  {
    boolean bDeleted = false;
    try
    {
      stmtDeleteAddress.clearParameters();
      stmtDeleteAddress.setString(1, domainName);
      stmtDeleteAddress.executeUpdate();
      bDeleted = true;
    }
    catch (SQLException sqle)
    {
      sqle.printStackTrace();
    }

    return bDeleted;
  }

  public boolean deleteRecord(Domain record)
  {
    String domainName = record.getDomainName();
    return deleteRecord(domainName);
  }

  public List<ListEntry> getListEntries()
  {
    List<ListEntry> listEntries = new ArrayList<ListEntry>();
    Statement queryStatement = null;
    ResultSet results = null;

    try
    {
      queryStatement = dbConnection.createStatement();
      results = queryStatement.executeQuery(strGetListEntries);
      while (results.next())
      {
        String domainName = results.getString(1);

        ListEntry entry = new ListEntry(domainName);
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

  public Domain getDomain(String domainName)
  {
    Domain address = null;
    try
    {
      stmtGetAddress.clearParameters();
      stmtGetAddress.setString(1, domainName);
      ResultSet result = stmtGetAddress.executeQuery();
      if (result.next())
      {
        String registrant = result.getString("REGISTRANT");

        String[] oldAdmin;
        if (result.getString("ADMIN") != null)
        {
          if (result.getString("ADMIN").length() > 0)
          {
            String[] oldAdmins = result.getString("ADMIN").split(";");
            oldAdmin = new String[oldAdmins.length];
            for (int adminIndex = 0; adminIndex < oldAdmins.length; adminIndex++)
            {
              oldAdmin[adminIndex] = oldAdmins[adminIndex];
            }
          }
          else
          {
            oldAdmin = null;
          }
        }
        else
        {
          oldAdmin = null;
        }

        String[] oldTech;
        if (result.getString("TECH") != null)
        {
          if (result.getString("TECH").length() > 0)
          {
            String[] oldTechs = result.getString("TECH").split(";");
            oldTech = new String[oldTechs.length];
            for (int techIndex = 0; techIndex < oldTechs.length; techIndex++)
            {
              oldTech[techIndex] = oldTechs[techIndex];
            }
          }
          else
          {
            oldTech = null;
          }
        }
        else
        {
          oldTech = null;
        }

        String[] oldNameServer;
        if (result.getString("NAMESERVER") != null)
        {
          if (result.getString("NAMESERVER").length() > 0)
          {
            String[] oldNameServers = result.getString("NAMESERVER").split(";");
            oldNameServer = new String[oldNameServers.length];
            for (int nameServerIndex = 0; nameServerIndex < oldNameServers.length; nameServerIndex++)
            {
              oldNameServer[nameServerIndex] = oldNameServers[nameServerIndex];
            }
          }
          else
          {
            oldNameServer = null;
          }
        }
        else
        {
          oldNameServer = null;
        }


        String authInfo = result.getString("AUTHINFO");
        int[] oldStatus;
        Vector oldStatusV = new Vector();
        if (result.getString("STATUS") != null)
        {
          if (result.getString("STATUS").length() > 0)
          {
            for (String oldStatusTA : result.getString("STATUS").split(","))
            {
              oldStatusV.add(Integer.parseInt(oldStatusTA));
            }
/*                        String[] oldStatuses = result.getString("STATUS").split(",");
                        oldStatus = new int[oldStatuses.length];
                        for (int statusIndex = 0; statusIndex < oldStatuses.length; statusIndex++) {
                            oldStatus[statusIndex] = Integer.valueOf(oldStatuses[statusIndex]);
                        }*/
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

        java.util.Date expireDate = null;
        if (result.getDate("EXPIRE") instanceof java.sql.Date)
        {
          expireDate = new java.util.Date(result.getDate("EXPIRE").getTime());
        }
        String validationCode = result.getString("VALIDATIONCODE");

        boolean isDnsSec = result.getInt("ISDNSSEC") == 1;
        String dnsSecKeyTag = result.getString("DNSSECKEYTAG");
        int dnsSecAlg = result.getInt("DNSSECALG");
        int dnsSecDigestType = result.getInt("DNSSECDIGESTTYPE");
        String dnsSecDigest = result.getString("DNSSECDIGEST");
        address = new Domain(domainName, registrant, oldAdmin, oldTech, oldNameServer, authInfo, oldStatusV, expireDate, validationCode, isDnsSec, dnsSecKeyTag, dnsSecAlg, dnsSecDigestType, dnsSecDigest);
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
  private PreparedStatement stmtGetAddress;
  private PreparedStatement stmtDeleteAddress;
  private PreparedStatement stmtDropDomainTable;

  private final String strDropDomainTable;
  private final String strCreateAddressTable;
  private final String strGetAddress;
  private final String strSaveAddress;
  private final String strGetListEntries;
  private final String strUpdateAddress;
  private final String strDeleteAddress;


}
