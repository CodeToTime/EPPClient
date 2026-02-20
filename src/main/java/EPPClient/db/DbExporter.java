/*
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

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Utility to export database tables (contacts, domains, messages) to CSV files inside a ZIP archive.
 * It uses raw SELECT * and connects using the same configuration used by DAOs,
 * so it works with both Derby and MySQL.
 */
public class DbExporter {

    /**
     * Export contacts, domains and messages tables to a ZIP file at the given destination.
     * The ZIP will contain contacts.csv, domains.csv and messages.csv.
     */
    public void exportAllToZip(File destinationZip) throws Exception {
        // Ensure parent directory exists
        File parent = destinationZip.getAbsoluteFile().getParentFile();
        if (parent != null && !parent.exists()) {
            if (!parent.mkdirs()) {
                throw new IOException("Impossibile creare la cartella di destinazione: " + parent);
            }
        }

        try (FileOutputStream fos = new FileOutputStream(destinationZip);
             BufferedOutputStream bos = new BufferedOutputStream(fos);
             ZipOutputStream zos = new ZipOutputStream(bos, StandardCharsets.UTF_8)) {

            // Contacts
            exportSingleTableToZipEntry(zos,
                    "contacts.properties",
                    "contacts.csv");

            // Domains
            exportSingleTableToZipEntry(zos,
                    "domains.properties",
                    "domains.csv");

            // Messages
            exportSingleTableToZipEntry(zos,
                    "messages.properties",
                    "messages.csv");
        }
    }

    /**
     * Export a single table (determined by the provided properties resource name) into a CSV entry in the given zip.
     * Properties files must contain: db.table, db.schema and default derby.url/derby.driver (will be overridden by EPPparams).
     */
    private void exportSingleTableToZipEntry(ZipOutputStream zos, String propertiesResourceName, String entryName) throws Exception {
        // Load base properties the same way DAOs do (resource relative to DAO packages)
        Properties props = new Properties();
        try (InputStream in = DbExporter.class.getResourceAsStream(propertiesResourceName)) {
            if (in != null) {
                props.load(in);
            }
        }

        // Override with runtime configuration from EPPparams
        props.put("derby.driver", "org.apache.derby.jdbc.EmbeddedDriver");
        props.put("derby.url", EPPparams.getParameter("EppClient.dburl"));
        props.put("db.schema", EPPparams.getParameter("EppClient.dbname"));
        props.put("user", EPPparams.getParameter("EppClient.dbuid"));
        props.put("password", EPPparams.getParameter("EppClient.dbpwd"));

        // Ensure Derby system directory is configured, like DAOs do
        setDerbySystemDir();

        String schema = props.getProperty("db.schema");
        String table = props.getProperty("db.table");
        if (schema == null || table == null) {
            throw new IllegalStateException("Proprietà mancanti per l'esportazione: db.schema o db.table non definite in " + propertiesResourceName);
        }

        String baseUrl = props.getProperty("derby.url");
        String driver = resolveDriver(baseUrl);
        loadDriver(driver);

        // For Derby, DAOs use separate DB names: contacts/domains/messages. Infer from properties file name.
        String dbNameForDerby = null;
        if (baseUrl != null && !baseUrl.toLowerCase().contains("mysql") && !baseUrl.toLowerCase().contains("mariadb")) {
            dbNameForDerby = propertiesResourceName;
            if (dbNameForDerby != null && dbNameForDerby.endsWith(".properties")) {
                dbNameForDerby = dbNameForDerby.substring(0, dbNameForDerby.length() - ".properties".length());
            }
        }

        String jdbcUrl = buildJdbcUrl(baseUrl, dbNameForDerby);

        try (Connection conn = DriverManager.getConnection(jdbcUrl, props)) {
            String sql = "SELECT * FROM " + schema + "." + table;
            try (Statement st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
                try (ResultSet rs = st.executeQuery(sql)) {
                    zos.putNextEntry(new ZipEntry(entryName));
                    OutputStreamWriter osw = new OutputStreamWriter(zos, StandardCharsets.UTF_8);
                    BufferedWriter writer = new BufferedWriter(osw);
                    // Write CSV from ResultSet
                    writeResultSetAsCsv(rs, writer);
                    writer.flush();
                    // Do not close osw/writer; closing them would close the underlying ZipOutputStream
                    // Do not close zos here; just close the entry
                    zos.closeEntry();
                }
            }
        }
    }

    private static void writeResultSetAsCsv(ResultSet rs, BufferedWriter writer) throws Exception {
        char delimiter = resolveDelimiter();
        ResultSetMetaData meta = rs.getMetaData();
        int cols = meta.getColumnCount();
        // Header
        for (int i = 1; i <= cols; i++) {
            if (i > 1) writer.write(delimiter);
            writer.write(alwaysQuote(meta.getColumnName(i)));
        }
        writer.write("\r\n");
        // Rows
        while (rs.next()) {
            for (int i = 1; i <= cols; i++) {
                if (i > 1) writer.write(delimiter);
                Object val = rs.getObject(i);
                writer.write(alwaysQuote(val));
            }
            writer.write("\r\n");
        }
    }

    private static String alwaysQuote(Object value) {
        String s = (value == null) ? "" : String.valueOf(value);
        // Escape double quotes by doubling them (RFC 4180)
        s = s.replace("\"", "\"\"");
        return '"' + s + '"';
    }

    private static char resolveDelimiter() {
        try {
            String cfg = EPPClient.config.EPPparams.getParameter("EppClient.csv.delimiter");
            if (cfg != null) {
                cfg = cfg.trim().toLowerCase();
                switch (cfg) {
                    case ";":
                    case "semicolon":
                        return ';';
                    case ",":
                    case "comma":
                        return ',';
                    case "\t":
                    case "tab":
                    case "tsv":
                        return '\t';
                    default:
                        if (cfg.length() == 1) return cfg.charAt(0);
                }
            }
        } catch (Exception ignored) {}
        // Default: semicolon (better for many EU Excel/LibreOffice setups)
        return ';';
    }

    private static void loadDriver(String driverClass) throws ClassNotFoundException {
        Class.forName(driverClass);
    }

    private static String resolveDriver(String url) {
        if (url != null && url.toLowerCase().contains("mysql")) {
            // Prefer modern MySQL driver if present
            return "com.mysql.cj.jdbc.Driver";
        }
        if (url != null && url.toLowerCase().contains("mariadb")) {
            return "org.mariadb.jdbc.Driver";
        }
        return "org.apache.derby.jdbc.EmbeddedDriver";
    }

    private static String buildJdbcUrl(String baseUrl, String dbName) {
        if (baseUrl == null) return null;
        if (baseUrl.toLowerCase().contains("mysql") || baseUrl.toLowerCase().contains("mariadb")) {
            return baseUrl; // full URL already configured for MySQL
        }
        // Derby: append database name (contacts/domains/messages). If dbName is null, return baseUrl as-is.
        if (dbName == null || dbName.isEmpty()) {
            return baseUrl;
        }
        if (!baseUrl.endsWith(dbName)) {
            return baseUrl + dbName;
        }
        return baseUrl;
    }

    private static void setDerbySystemDir() {
        try {
            String userHomeDir = System.getProperty("user.home", ".");
            String systemDir = userHomeDir + "/.eppclient";
            System.setProperty("derby.system.home", systemDir);
            File fileSystemDir = new File(systemDir);
            // no-op if already exists
            if (!fileSystemDir.exists()) {
                fileSystemDir.mkdirs();
            }
        } catch (Exception ignored) {
        }
    }
}
