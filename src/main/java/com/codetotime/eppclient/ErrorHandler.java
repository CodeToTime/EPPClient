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

package com.codetotime.eppclient;

import java.awt.Component;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import org.slf4j.Logger;

/**
 * Centralized error handling utility class. Provides dialog display for errors - logging is done
 * directly via SLF4J.
 */
public class ErrorHandler {

  /** Default message shown to users when an error occurs. */
  public static final String DEFAULT_USER_MESSAGE =
      "Si è verificato un errore durante l'operazione.\n\n"
          + "Consultare i log per maggiori dettagli.";

  /**
   * Logs an error and shows a dialog to the user. The exception is logged at ERROR level with full
   * stack trace.
   *
   * @param logger The SLF4J logger instance
   * @param exception The exception that occurred
   * @param userTitle The title for the error dialog
   * @param parent The parent frame for the dialog
   */
  public static void error(Logger logger, Exception exception, String userTitle, JFrame parent) {
    logger.error("Error occurred: {}", exception.getMessage(), exception);
    showErrorDialog(parent, userTitle);
  }

  /**
   * Logs an error without showing a dialog.
   *
   * @param logger The SLF4J logger instance
   * @param exception The exception that occurred
   */
  public static void logError(Logger logger, Exception exception) {
    logger.error("Error occurred: {}", exception.getMessage(), exception);
  }

  /**
   * Shows an error dialog to the user
   *
   * @param parent The parent component for the dialog
   * @param title The title of the dialog
   */
  public static void showErrorDialog(Component parent, String title, String message) {
    JOptionPane optionPane =
        new JOptionPane(
            message,
            JOptionPane.ERROR_MESSAGE,
            JOptionPane.DEFAULT_OPTION,
            null,
            new Object[] {"OK"},
            "OK");

    JDialog dialog = optionPane.createDialog(parent, title);
    dialog.setResizable(false);
    dialog.setVisible(true);
    dialog.dispose();
  }

  /**
   * Shows an error dialog to the user with the default message. Delegates to {@link
   * #showErrorDialog(Component, String, String)}.
   *
   * @param parent The parent component for the dialog
   * @param title The title of the dialog
   */
  public static void showErrorDialog(Component parent, String title) {
    showErrorDialog(parent, title, DEFAULT_USER_MESSAGE);
  }
}
