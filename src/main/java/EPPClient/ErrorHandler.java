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

import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import org.slf4j.Logger;

/**
 * Centralized error handling utility class.
 * Provides dialog display for errors - logging is done directly via SLF4J.
 *
 * The user receives a generic error message and is invited to enable logging for detailed information.
 *
 * File logging is controlled by system property -Deppclient.logLevel
 * Console output for errors can be enabled with -Deppclient.console=true
 */
public class ErrorHandler
{

  /**
   * System property to set the logging level.
   */
  private static final String LOG_LEVEL_PROPERTY = "eppclient.logLevel";

  /**
   * Original command-line arguments passed to the application.
   * Used when restarting with debug enabled.
   */
  private static String[] originalArgs = new String[0];

  /**
   * Sets the original command-line arguments for use during debug restart.
   *
   * @param args The command-line arguments passed to main()
   */
  public static void setOriginalArgs(String[] args)
  {
    if (args != null)
    {
      originalArgs = args.clone();
    }
  }

  /**
   * Default message shown to users when an error occurs.
   */
  public static final String DEFAULT_USER_MESSAGE = "Si è verificato un errore durante l'operazione.\n\n" + "Clicca \"Riavvia con debug attivo\" per riavviare l'applicazione con il logging di debug abilitato.\n\n" + "Consultare i file di log per maggiori dettagli.";

  /**
   * Logs an error and shows a dialog to the user.
   * The exception is logged at ERROR level with full stack trace.
   *
   * @param logger The SLF4J logger instance
   * @param exception The exception that occurred
   * @param userTitle The title for the error dialog
   */
  public static void error(Logger logger, Exception exception, String userTitle)
  {
    logger.error("Error occurred: {}", exception.getMessage(), exception);
    showErrorDialog(null, userTitle);
  }

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
   * Logs an error and shows a dialog to the user.
   * The exception is logged at ERROR level with full stack trace.
   *
   * @param logger The SLF4J logger instance
   * @param exception The exception that occurred
   * @param userTitle The title for the error dialog
   * @param parent The parent component for the dialog
   */
  public static void error(Logger logger, Exception exception, String userTitle, java.awt.Component parent)
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
   * Logs an error without showing a dialog.
   *
   * @param logger The SLF4J logger instance
   * @param message The error message to log
   */
  public static void logError(Logger logger, String message)
  {
    logger.error(message);
  }

  /**
   * Logs an error with exception without showing a dialog.
   *
   * @param logger The SLF4J logger instance
   * @param message The error message to log
   * @param exception The exception to log
   */
  public static void logError(Logger logger, String message, Exception exception)
  {
    logger.error(message, exception);
  }

  /**
   * Shows an error dialog to the user with the default message.
   *
   * @param parent The parent component for the dialog
   * @param title The title of the dialog
   */
  public static void showErrorDialog(java.awt.Component parent, String title)
  {
    showErrorDialogWithRestart(parent, title, DEFAULT_USER_MESSAGE);
  }

  /**
   * Shows an error dialog to the user with a custom message.
   *
   * @param parent The parent component for the dialog
   * @param title The title of the dialog
   * @param customMessage Custom message to display
   */
  public static void showErrorDialog(java.awt.Component parent, String title, String customMessage)
  {
    String fullMessage = customMessage + "\n\n" + DEFAULT_USER_MESSAGE;
    showErrorDialogWithRestart(parent, title, fullMessage);
  }

  /**
   * Shows an error dialog to the user with a custom message.
   *
   * @param parent The parent frame for the dialog
   * @param title The title of the dialog
   * @param customMessage Custom message to display
   */
  public static void showErrorDialog(JFrame parent, String title, String customMessage)
  {
    String fullMessage = customMessage + "\n\n" + DEFAULT_USER_MESSAGE;
    showErrorDialogWithRestart(parent, title, fullMessage);
  }

  /**
   * Shows an error dialog with a "Restart with debug active" button.
   *
   * @param parent The parent component for the dialog
   * @param title The title of the dialog
   * @param message The message to display
   */
  private static void showErrorDialogWithRestart(java.awt.Component parent, String title, String message)
  {
    // Create buttons: OK and Restart with debug
    JButton restartButton = new JButton("Riavvia con debug attivo");
    restartButton.setFocusPainted(false);

    // Create option pane with custom buttons
    JOptionPane optionPane = new JOptionPane(message, JOptionPane.ERROR_MESSAGE, JOptionPane.OK_CANCEL_OPTION, null, new Object[]
    {restartButton, "OK"}, "OK");

    // Create and configure the dialog
    JDialog dialog = optionPane.createDialog(parent, title);
    dialog.setResizable(false);

    // Handle button click
    restartButton.addActionListener(e -> {
      dialog.dispose();
      restartWithDebug();
    });

    // Show dialog
    dialog.setVisible(true);

    // Check if OK was clicked (no action needed)
    Object selectedValue = optionPane.getValue();
    if (selectedValue == null || "OK".equals(selectedValue))
    {
      // User clicked OK or closed dialog - no action needed
    }
  }

  /**
   * Restarts the application with debug logging enabled.
   * Uses Java 21+ APIs for reliable process management.
   */
  private static void restartWithDebug()
  {
    try
    {
      // Get current JVM input arguments
      List<String> inputArgs = ManagementFactory.getRuntimeMXBean().getInputArguments();

      // Build new arguments list with debug enabled
      List<String> newArgs = new ArrayList<>();
      boolean debugAlreadySet = false;

      for (String arg : inputArgs)
      {
        if (arg.startsWith("-D" + LOG_LEVEL_PROPERTY))
        {
          // Replace existing log level with DEBUG
          newArgs.add("-D" + LOG_LEVEL_PROPERTY + "=DEBUG");
          debugAlreadySet = true;
        }
        else
        {
          newArgs.add(arg);
        }
      }

      // Add debug parameter if not already present
      if (!debugAlreadySet)
      {
        newArgs.add("-D" + LOG_LEVEL_PROPERTY + "=DEBUG");
      }

      // Get the classpath using RuntimeMXBean
      String classPath = ManagementFactory.getRuntimeMXBean().getClassPath();
      // Use hardcoded main class name - the ProcessHandle command returns the Java executable, not the main class
      String mainClass = "EPPClient.main";

      // Build the command using ProcessHandle API (Java 9+)
      List<String> command = buildJavaCommand();

      // Add all JVM arguments (including -cp and -D properties)
      for (String arg : newArgs)
      {
        command.add(arg);
      }

      // Add classpath
      command.add("-cp");
      command.add(classPath);
      command.add(mainClass);

      // Add original program arguments
      for (String arg : originalArgs)
      {
        command.add(arg);
      }

      // Start new process and exit current one
      ProcessBuilder pb = new ProcessBuilder(command);

      // Preserve environment variables from current process
      pb.environment().putAll(System.getenv());

      pb.start();

      // Exit current JVM
      System.exit(0);

    }
    catch (Exception e)
    {
      // If restart fails, show error message
      JOptionPane.showMessageDialog(null, "Impossibile riavviare l'applicazione con debug.\n" + "Avviare manualmente l'applicazione con:\n" + "java -D" + LOG_LEVEL_PROPERTY + "=DEBUG -jar EPPClient.jar", "Errore", JOptionPane.ERROR_MESSAGE);
    }
  }

  /**
   * Builds the Java command using Java 21+ APIs for cross-platform compatibility.
   * @return List containing the Java executable path
   */
  private static List<String> buildJavaCommand()
  {
    List<String> command = new ArrayList<>();

    // Try to get Java executable from ProcessHandle (Java 9+)
    try
    {
      ProcessHandle currentProcess = ProcessHandle.current();
      String javaExec = currentProcess.info().command().orElse(null);

      if (javaExec != null && !javaExec.isEmpty())
      {
        File javaFile = new File(javaExec);
        // Verify it exists
        if (javaFile.exists())
        {
          command.add(javaExec);
          return command;
        }
      }
    }
    catch (Exception e)
    {
      // Fallback to other methods
    }

    // Fallback 1: Use java.home property with proper path construction
    String javaHome = System.getProperty("java.home");
    String os = System.getProperty("os.name").toLowerCase();
    String javaExec;

    if (os.contains("windows"))
    {
      javaExec = javaHome + File.separator + "bin" + File.separator + "java.exe";
    }
    else
    {
      javaExec = javaHome + File.separator + "bin" + File.separator + "java";
    }

    // Verify executable exists
    File javaFile = new File(javaExec);
    if (javaFile.exists())
    {
      command.add(javaExec);
    }
    else
    {
      // Last resort: just use "java" and rely on PATH
      command.add("java");
    }

    return command;
  }
}
