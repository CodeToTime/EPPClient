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

import java.awt.Component;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.slf4j.Logger;

/**
 * Centralized error handling utility class.
 * Provides dialog display for errors - logging is done directly via SLF4J.
 *
 * The user receives a generic error message and is invited to view the log file for detailed information.
 *
 * File logging is controlled by system property -Deppclient.logLevel
 * Console output for errors can be enabled with -Deppclient.console=true
 */
public class ErrorHandler
{

  /**
   * Default message shown to users when an error occurs.
   */
  public static final String DEFAULT_USER_MESSAGE = "Si è verificato un errore durante l'operazione.\n\n" + "Clicca \"Visualizza log\" per aprire il visualizzatore dei log.\n\n" + "Consultare i file di log per maggiori dettagli.";

  /**
   * Logs an error and shows a dialog to the user.
   * The exception is logged at ERROR level with full stack trace.
   *
   * @param logger The SLF4J logger instance
   * @param exception The exception that occurred
   * @param userTitle The title for the error dialog
   * @param parent The parent frame for the dialog
   */
  public static void error(Logger logger, Exception exception, String userTitle, JFrame parent)
  {
    logger.error("Error occurred: {}", exception.getMessage(), exception);
    showErrorDialog(parent, userTitle);
  }

  /**
   * Logs an error without showing a dialog.
   *
   * @param logger The SLF4J logger instance
   * @param exception The exception that occurred
   */
  public static void logError(Logger logger, Exception exception)
  {
    logger.error("Error occurred: {}", exception.getMessage(), exception);
  }

  /**
   * Shows an error dialog to the user with the default message.
   *
   * @param parent The parent component for the dialog
   * @param title The title of the dialog
   */
  public static void showErrorDialog(java.awt.Component parent, String title)
  {
    showErrorDialogWithLogViewer(parent, title, DEFAULT_USER_MESSAGE);
  }

  /**
   * Shows an error dialog with a "Show logs" button that opens the LogViewer.
   *
   * @param parent The parent component for the dialog
   * @param title The title of the dialog
   * @param message The message to display
   */
  private static void showErrorDialogWithLogViewer(Component parent, String title, String message)
  {
    // Create buttons: OK and View Log
    JButton viewLogButton = new JButton("Visualizza log");
    viewLogButton.setFocusPainted(false);

    // Create option pane with custom buttons
    JOptionPane optionPane = new JOptionPane(message, JOptionPane.ERROR_MESSAGE, JOptionPane.OK_CANCEL_OPTION, null, new Object[]
    {viewLogButton, "OK"}, "OK");

    // Create and configure the dialog
    JDialog dialog = optionPane.createDialog(parent, title);
    dialog.setResizable(false);

    // Handle button click
    viewLogButton.addActionListener(e -> {
      dialog.dispose();
      openLogViewer(parent);
    });

    // Show dialog
    dialog.setVisible(true);
  }

  /**
   * Opens the log viewer dialog.
   *
   * @param parent The parent component for the log viewer
   */
  private static void openLogViewer(Component parent)
  {
    // Get the parent frame for the log viewer
    JFrame frame = null;
    if (parent instanceof JFrame)
    {
      frame = (JFrame) parent;
    }
    else if (parent != null)
    {
      frame = (JFrame) SwingUtilities.getWindowAncestor(parent);
    }

    LogViewer.showLogViewer(frame);
  }
}
