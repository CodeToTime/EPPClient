/*
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

package EPPClient;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

/**
 * Log viewer dialog that displays the contents of the log file with level filtering.
 */
public class LogViewer extends JDialog
{
  private static final long serialVersionUID = 1L;

  private JComboBox<String> cmbLevel;
  private JList<String> logList;
  private DefaultListModel<String> logModel;
  private JButton btnRefresh;
  private JButton btnClose;
  private JButton btnOpenFolder;

  private List<LogEntry> allLogEntries = new ArrayList<>();

  // Filter options: "ALL" + log levels
  private static final String ALL_LEVELS = "ALL";
  private static final String[] LEVELS =
  {
    ALL_LEVELS,
    "ERROR",
    "WARN",
    "DEBUG",
    "INFO"
  };

  /**
   * Creates a new LogViewer dialog.
   *
   * @param parent the parent frame
   */
  public LogViewer(java.awt.Frame parent)
  {
    super(parent, "Visualizzatore Log", true);
    initComponents();
    loadLogFile();
    setLocationRelativeTo(parent);
  }

  private void initComponents()
  {
    setMinimumSize(new Dimension(800, 600));
    setPreferredSize(new Dimension(900, 650));

    JPanel topPanel = new JPanel(new GridBagLayout());
    topPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));

    // Level filter label
    JLabel lblFilter = new JLabel("Filtro per livello:");
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.anchor = GridBagConstraints.WEST;
    gbc.insets = new Insets(0, 0, 0, 10);
    topPanel.add(lblFilter, gbc);

    // Level combo box
    cmbLevel = new JComboBox<>(LEVELS);
    cmbLevel.setPreferredSize(new Dimension(120, 25));
    cmbLevel.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        applyFilter();
      }
    });
    gbc = new GridBagConstraints();
    gbc.gridx = 1;
    gbc.gridy = 0;
    gbc.anchor = GridBagConstraints.WEST;
    gbc.insets = new Insets(0, 0, 0, 20);
    topPanel.add(cmbLevel, gbc);

    // Refresh button
    btnRefresh = new JButton("Aggiorna");
    btnRefresh.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        loadLogFile();
      }
    });
    gbc = new GridBagConstraints();
    gbc.gridx = 2;
    gbc.gridy = 0;
    gbc.anchor = GridBagConstraints.WEST;
    gbc.insets = new Insets(0, 0, 0, 10);
    topPanel.add(btnRefresh, gbc);

    // Open folder button
    btnOpenFolder = new JButton("Apri Cartella");
    btnOpenFolder.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        openLogFolder();
      }
    });
    gbc = new GridBagConstraints();
    gbc.gridx = 3;
    gbc.gridy = 0;
    gbc.anchor = GridBagConstraints.WEST;
    topPanel.add(btnOpenFolder, gbc);

    // Log list
    logModel = new DefaultListModel<>();
    logList = new JList<>(logModel);
    logList.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
    logList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    logList.setVisibleRowCount(20);

    JScrollPane scrollPane = new JScrollPane(logList);
    scrollPane.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 10, 5, 10));

    // Bottom panel with close button
    JPanel bottomPanel = new JPanel(new GridBagLayout());
    bottomPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));

    btnClose = new JButton("Chiudi");
    btnClose.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        dispose();
      }
    });
    gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.anchor = GridBagConstraints.EAST;
    bottomPanel.add(btnClose, gbc);

    // Add panels to dialog
    getContentPane().setLayout(new BorderLayout());
    getContentPane().add(topPanel, BorderLayout.NORTH);
    getContentPane().add(scrollPane, BorderLayout.CENTER);
    getContentPane().add(bottomPanel, BorderLayout.SOUTH);

    // Set default close operation
    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
  }

  /**
   * Loads the log file content.
   */
  private void loadLogFile()
  {
    allLogEntries.clear();

    File logFile = new File(System.getProperty("user.dir"), "logs" + File.separator + "eppclient.log");

    if (!logFile.exists())
    {
      JOptionPane.showMessageDialog(this, "Il file di log non esiste.\n\n" + "Percorso: " + logFile.getAbsolutePath(), "File non trovato", JOptionPane.WARNING_MESSAGE);
      logModel.clear();
      return;
    }

    try (BufferedReader reader = new BufferedReader(new FileReader(logFile)))
    {
      String line;
      while ((line = reader.readLine()) != null)
      {
        LogEntry entry = parseLogLine(line);
        if (entry != null)
        {
          allLogEntries.add(entry);
        }
      }
    }
    catch (IOException ex)
    {
      JOptionPane.showMessageDialog(this, "Errore nella lettura del file di log:\n\n" + ex.getMessage(), "Errore lettura", JOptionPane.ERROR_MESSAGE);
    }

    applyFilter();
  }

  /**
   * Parses a log line and extracts level and message from JSON format.
   *
   * @param line the log line to parse
   * @return LogEntry or null if line cannot be parsed
   */
  private LogEntry parseLogLine(String line)
  {
    if (line == null || line.isEmpty())
    {
      return null;
    }

    try
    {
      JsonObject json = JsonParser.parseString(line).getAsJsonObject();

      String level = json.has("loglevel") ? json.get("loglevel").getAsString() : "INFO";
      String message = json.has("message") ? json.get("message").getAsString() : line;

      return new LogEntry(level, message);
    }
    catch (Exception e)
    {
      // Invalid JSON, skip line
      return null;
    }
  }

  /**
   * Applies the level filter to the log entries.
   */
  private void applyFilter()
  {
    logModel.clear();

    String selectedLevel = (String) cmbLevel.getSelectedItem();

    for (LogEntry entry : allLogEntries)
    {
      if (ALL_LEVELS.equals(selectedLevel) || selectedLevel.equals(entry.level()))
      {
        logModel.addElement(entry.message());
      }
    }

    // Scroll to bottom (most recent entries)
    if (logModel.getSize() > 0)
    {
      logList.scrollRectToVisible(logList.getCellBounds(logModel.getSize() - 1, logModel.getSize() - 1));
    }
  }

  /**
   * Opens the log folder in the system file manager.
   */
  private void openLogFolder()
  {
    try
    {
      File logsDir = new File(System.getProperty("user.dir"), "logs");
      if (!logsDir.exists())
      {
        logsDir.mkdirs();
      }
      java.awt.Desktop.getDesktop().open(logsDir);
    }
    catch (Exception ex)
    {
      JOptionPane.showMessageDialog(this, "Impossibile aprire la cartella dei log.\n\n" + "Errore: " + ex.getMessage(), "Errore", JOptionPane.ERROR_MESSAGE);
    }
  }

  /**
   * Shows the log viewer dialog.
   *
   * @param parent the parent frame
   */
  public static void showLogViewer(java.awt.Frame parent)
  {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run()
      {
        LogViewer viewer = new LogViewer(parent);
        viewer.setVisible(true);
      }
    });
  }

  /**
   * Record class to hold log entry data (Java 21+).
   */
  private record LogEntry(String level, String message)
  {
  }
}
