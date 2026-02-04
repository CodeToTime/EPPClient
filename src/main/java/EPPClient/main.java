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

package EPPClient;

import java.awt.event.*;
import EPPClient.db.contactsDao;
import EPPClient.db.domainsDao;
import EPPClient.db.messagesDao;
import EPPClient.config.EPPparams;

import EPPClient.uplink.EPPuplink;
import EPPClient.config.manageParameters;
import EPPClient.contacts.ContactsManagement;
import EPPClient.domains.DomainsManagement;
import EPPClient.importer.TxtImport;
import EPPClient.messages.Message;
import EPPClient.messages.MessageManagement;
import EPPClient.BuildInfo;
import EPPClient.db.DbExporter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

public class main extends JFrame implements WindowListener
{
  public EPPuplink EPPuplink;

  public messagesDao messagesDao;
  public domainsDao domainsDao;
  public contactsDao contactsDao;

  public String paramPrefix;

  /**
   * @param args the command line arguments
   */
  public static void main(String[] args)
  {
    if (args.length > 0)
    {
      EPPparams.setPrefix(args[0]);
    }

    EventQueue.invokeLater(new Runnable()
    {
      public void run()
      {
        new main().setVisible(true);
      }
    });
  }

  /**
   * Creates new form mainFrame
   */
  public main()
  {
    System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
    initComponents();
    this.addWindowListener(this);
    setVersion(BuildInfo.BUILD_VERSION);

    try
    {
      messagesDao = new messagesDao();
      messagesDao.connect();
      domainsDao = new domainsDao();
      domainsDao.connect();
      contactsDao = new contactsDao();
      contactsDao.connect();

      EPPuplink = new EPPuplink(this);

      domainsMenu = new DomainsManagement(this);
      contactsMenu = new ContactsManagement(this);
      messagesMenu = new MessageManagement(this);
      importMenu = new TxtImport(this);
    }
    catch (Exception ex)
    {
      JOptionPane.showMessageDialog(this, "Si è verificato un errore durante l'accesso al database.\nVerificare che i dati immessi nel menu \"Configurazione\" siano corretti\ne riavviare il client.\n\nAl momento è possibile aprire solo la finestra \"Configurazione\"!", "Errore connessione al database", JOptionPane.WARNING_MESSAGE);
    }

    configMenu = new manageParameters(this);
    if(BuildInfo.BUILD_VERSION.contains("SNAPSHOT")) {
        JOptionPane.showMessageDialog(this, "Attenzione: è in esecuzione una versione di sviluppo del software.\n" +
                "Non è consigliata per l’uso in ambienti di produzione.", "Versione di sviluppo", JOptionPane.WARNING_MESSAGE);
    }

    // Controlla aggiornamenti (solo per versioni non-SNAPSHOT)
    UpdateChecker.checkForUpdates(this, BuildInfo.BUILD_VERSION);
  }

  public void windowReset()
  {
    messagesDao = new messagesDao();
    messagesDao.connect();
    domainsDao = new domainsDao();
    domainsDao.connect();
    contactsDao = new contactsDao();
    contactsDao.connect();

    EPPuplink = new EPPuplink(this);

    domainsMenu = new DomainsManagement(this);
    contactsMenu = new ContactsManagement(this);
    messagesMenu = new MessageManagement(this);
    importMenu = new TxtImport(this);
  }

  public void windowOpened(WindowEvent e)
  {
    EPPuplink.start();
  }

  public void windowClosing(WindowEvent e)
  {
    if (EPPuplink == null)
    {
      System.exit(0);
    }
    else
    {
      boolean isClosed = EPPuplink.gracefulStop();
      if (isClosed)
      {
        System.exit(0);
      }
      else
      {
        boolean isCancelled = false;
        while (!isCancelled)
        {
          switch (JOptionPane.showConfirmDialog(this, "The application is still waiting for the EPP Server to logout.\nDo you want to wait 5 seconds for a graceful close?", "Exiting application", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE))
          {
            case JOptionPane.YES_OPTION:
              isClosed = EPPuplink.gracefulStop();
              if (isClosed)
              {
                System.exit(0);
              }
              break;
            case JOptionPane.NO_OPTION:
              //Forzo chiusura
              System.exit(0);
              break;

            default:
              //Annullo chiusura
              isCancelled = true;
              EPPuplink.restart();
              break;
          }
        }
      }
    }
  }

  public void windowActivated(WindowEvent e)
  {
  }

  public void windowClosed(WindowEvent e)
  {
  }

  public void windowDeactivated(WindowEvent e)
  {
  }

  public void windowDeiconified(WindowEvent e)
  {
  }

  private void btnInformazioni(ActionEvent e) {
      InfoDialog.show(null);
  }

  public void windowIconified(WindowEvent e)
  {
  }


  /**
   * This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {
      lblTitle = new JLabel();
      lblCopy = new JLabel();
      resCredit = new JTextField();
      lblResCredit = new JLabel();
      msgQ = new JTextField();
      lblMsgQ = new JLabel();
      lblSoci = new JLabel();
      nextMsg = new JTextField();
      lblNextMsg = new JLabel();
      lblEPPworking = new JLabel();
      EPPonoff = new JButton();
      lblVersion = new JLabel();
      btnContact = new JButton();
      btnDomain = new JButton();
      btnMsg = new JButton();
      btnConfig = new JButton();
      btnInformazioni = new JButton();
      btnDoTest = new JButton();
      btnImportFromTxt = new JButton();

      //======== this ========
      setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
      setTitle("EPP Client");
      setMinimumSize(new Dimension(500, 450));
      setPreferredSize(new Dimension(500, 520));
      Container contentPane = getContentPane();
      contentPane.setLayout(new GridBagLayout());

      //---- lblTitle ----
      lblTitle.setFont(new Font("Tahoma", Font.PLAIN, 14));
      lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
      lblTitle.setText("EPP Client");
      contentPane.add(lblTitle, new GridBagConstraints(0, 0, 3, 1, 0.0, 0.0,
          GridBagConstraints.CENTER, GridBagConstraints.NONE,
          new Insets(0, 0, 0, 0), 0, 0));

      //---- lblCopy ----
      lblCopy.setFont(new Font("Tahoma", Font.PLAIN, 14));
      lblCopy.setText("Compilato in data: " + BuildInfo.BUILD_DATE);
      contentPane.add(lblCopy, new GridBagConstraints(0, 2, 3, 1, 0.0, 0.0,
          GridBagConstraints.CENTER, GridBagConstraints.NONE,
          new Insets(0, 0, 0, 0), 0, 0));

      //---- resCredit ----
      resCredit.setEditable(false);
      resCredit.setPreferredSize(new Dimension(100, 20));
      contentPane.add(resCredit, new GridBagConstraints(2, 6, 1, 1, 0.0, 0.0,
          GridBagConstraints.NORTHEAST, GridBagConstraints.NONE,
          new Insets(0, 5, 5, 5), 0, 0));

      //---- lblResCredit ----
      lblResCredit.setFont(new Font("Tahoma", Font.PLAIN, 10));
      lblResCredit.setText("Credito disponibile (\u20ac):");
      contentPane.add(lblResCredit, new GridBagConstraints(2, 5, 1, 1, 0.0, 0.0,
          GridBagConstraints.SOUTHEAST, GridBagConstraints.NONE,
          new Insets(5, 5, 0, 5), 0, 0));

      //---- msgQ ----
      msgQ.setEditable(false);
      msgQ.setPreferredSize(new Dimension(100, 20));
      contentPane.add(msgQ, new GridBagConstraints(2, 8, 1, 1, 0.0, 0.0,
          GridBagConstraints.NORTHEAST, GridBagConstraints.NONE,
          new Insets(0, 5, 5, 5), 0, 0));

      //---- lblMsgQ ----
      lblMsgQ.setFont(new Font("Tahoma", Font.PLAIN, 10));
      lblMsgQ.setText("Nr. messaggi in coda:");
      contentPane.add(lblMsgQ, new GridBagConstraints(2, 7, 1, 1, 0.0, 0.0,
          GridBagConstraints.SOUTHEAST, GridBagConstraints.NONE,
          new Insets(5, 5, 0, 5), 0, 0));

      //---- lblSoci ----
      lblSoci.setHorizontalAlignment(SwingConstants.CENTER);
      lblSoci.setText(BuildInfo.BUILD_LABEL);
      contentPane.add(lblSoci, new GridBagConstraints(0, 3, 3, 1, 0.0, 0.0,
          GridBagConstraints.CENTER, GridBagConstraints.NONE,
          new Insets(0, 0, 50, 0), 0, 0));

      //---- nextMsg ----
      nextMsg.setEditable(false);
      nextMsg.setFont(new Font("Tahoma", Font.PLAIN, 10));
      nextMsg.setMaximumSize(new Dimension(300, 20));
      nextMsg.setMinimumSize(new Dimension(300, 20));
      nextMsg.setPreferredSize(new Dimension(300, 20));
      contentPane.add(nextMsg, new GridBagConstraints(0, 15, 3, 1, 0.0, 0.0,
          GridBagConstraints.CENTER, GridBagConstraints.NONE,
          new Insets(0, 5, 0, 5), 0, 0));

      //---- lblNextMsg ----
      lblNextMsg.setFont(new Font("Tahoma", Font.PLAIN, 10));
      lblNextMsg.setText("Prossimo messaggio");
      contentPane.add(lblNextMsg, new GridBagConstraints(0, 14, 3, 1, 0.0, 0.0,
          GridBagConstraints.SOUTHEAST, GridBagConstraints.NONE,
          new Insets(5, 5, 0, 5), 0, 0));

      //---- lblEPPworking ----
      lblEPPworking.setFont(new Font("Tahoma", Font.PLAIN, 10));
      lblEPPworking.setHorizontalAlignment(SwingConstants.TRAILING);
      lblEPPworking.setIcon(new ImageIcon(getClass().getResource("/gear.gif")));
      lblEPPworking.setText("sessione EPP non attiva");
      lblEPPworking.setHorizontalTextPosition(SwingConstants.LEADING);
      contentPane.add(lblEPPworking, new GridBagConstraints(1, 16, 2, 1, 0.0, 0.0,
          GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL,
          new Insets(5, 5, 5, 5), 0, 0));

      //---- EPPonoff ----
      EPPonoff.setText("Attiva Sessione");
      EPPonoff.addActionListener(e -> EPPonoffActionPerformed(e));
      contentPane.add(EPPonoff, new GridBagConstraints(0, 16, 1, 1, 0.0, 0.0,
          GridBagConstraints.CENTER, GridBagConstraints.NONE,
          new Insets(0, 0, 12, 0), 0, 0));

      //---- lblVersion ----
      lblVersion.setFont(new Font("Tahoma", Font.PLAIN, 14));
      lblVersion.setText(" ");
      contentPane.add(lblVersion, new GridBagConstraints(0, 1, 3, 1, 0.0, 0.0,
          GridBagConstraints.CENTER, GridBagConstraints.NONE,
          new Insets(0, 0, 0, 0), 0, 0));

      //---- btnContact ----
      btnContact.setText("Contatti");
      btnContact.setMaximumSize(new Dimension(150, 25));
      btnContact.setMinimumSize(new Dimension(150, 25));
      btnContact.setPreferredSize(new Dimension(150, 25));
      btnContact.addActionListener(e -> btnContactActionPerformed(e));
      contentPane.add(btnContact, new GridBagConstraints(0, 6, 1, 1, 0.0, 0.0,
          GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
          new Insets(5, 5, 5, 5), 0, 0));

      //---- btnDomain ----
      btnDomain.setText("Domini");
      btnDomain.setMaximumSize(new Dimension(150, 25));
      btnDomain.setMinimumSize(new Dimension(150, 25));
      btnDomain.setPreferredSize(new Dimension(150, 25));
      btnDomain.addActionListener(e -> btnDomainActionPerformed(e));
      contentPane.add(btnDomain, new GridBagConstraints(0, 7, 1, 1, 0.0, 0.0,
          GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
          new Insets(5, 5, 5, 5), 0, 0));

      //---- btnMsg ----
      btnMsg.setText("Messaggi");
      btnMsg.setMaximumSize(new Dimension(150, 25));
      btnMsg.setMinimumSize(new Dimension(150, 25));
      btnMsg.setPreferredSize(new Dimension(150, 25));
      btnMsg.addActionListener(e -> btnMsgActionPerformed(e));
      contentPane.add(btnMsg, new GridBagConstraints(0, 8, 1, 1, 0.0, 0.0,
          GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
          new Insets(5, 5, 5, 5), 0, 0));

      //---- btnConfig ----
      btnConfig.setText("Configurazione");
      btnConfig.setMaximumSize(new Dimension(150, 25));
      btnConfig.setMinimumSize(new Dimension(150, 25));
      btnConfig.setPreferredSize(new Dimension(150, 25));
      btnConfig.addActionListener(e -> btnConfigActionPerformed(e));
      contentPane.add(btnConfig, new GridBagConstraints(0, 9, 1, 1, 0.0, 0.0,
          GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
          new Insets(5, 5, 5, 5), 0, 0));

      //---- btnInformazioni ----
      btnInformazioni.setMaximumSize(new Dimension(150, 25));
      btnInformazioni.setMinimumSize(new Dimension(150, 25));
      btnInformazioni.setPreferredSize(new Dimension(150, 25));
      btnInformazioni.setAction(null);
      btnInformazioni.setText("Informazioni");
      btnInformazioni.addActionListener(e -> btnInformazioni(e));
      contentPane.add(btnInformazioni, new GridBagConstraints(2, 9, 1, 1, 0.0, 0.0,
          GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
          new Insets(5, 5, 5, 5), 0, 0));
      btnInformazioni.setVisible(!BuildInfo.BUILD_LABEL.isEmpty());

      //---- btnDoTest ----
      btnDoTest.setText("Esporta DB");
      btnDoTest.setMaximumSize(new Dimension(150, 25));
      btnDoTest.setMinimumSize(new Dimension(150, 25));
      btnDoTest.setPreferredSize(new Dimension(150, 25));
      btnDoTest.addActionListener(e -> btnDoTestActionPerformed(e));
      contentPane.add(btnDoTest, new GridBagConstraints(0, 10, 1, 1, 0.0, 0.0,
          GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
          new Insets(5, 5, 5, 5), 0, 0));

      //---- btnImportFromTxt ----
      btnImportFromTxt.setText("Importa da txt");
      btnImportFromTxt.setMaximumSize(new Dimension(150, 25));
      btnImportFromTxt.setMinimumSize(new Dimension(150, 25));
      btnImportFromTxt.setPreferredSize(new Dimension(150, 25));
      btnImportFromTxt.addActionListener(e -> btnImportFromTxtActionPerformed(e));
      contentPane.add(btnImportFromTxt, new GridBagConstraints(2, 10, 1, 1, 0.0, 0.0,
          GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
          new Insets(5, 5, 5, 5), 0, 0));
      pack();
      setLocationRelativeTo(getOwner());
  }// </editor-fold>//GEN-END:initComponents

  private void btnDomainActionPerformed(java.awt.event.ActionEvent evt)
  {//GEN-FIRST:event_btnDomainActionPerformed
    domainsMenu.setVisible(true);
  }//GEN-LAST:event_btnDomainActionPerformed

  private void btnContactActionPerformed(java.awt.event.ActionEvent evt)
  {//GEN-FIRST:event_btnContactActionPerformed
    contactsMenu.setVisible(true);
  }//GEN-LAST:event_btnContactActionPerformed

  private void btnConfigActionPerformed(java.awt.event.ActionEvent evt)
  {//GEN-FIRST:event_btnConfigActionPerformed
    configMenu.setVisible(true);
  }//GEN-LAST:event_btnConfigActionPerformed

  private void btnMsgActionPerformed(java.awt.event.ActionEvent evt)
  {//GEN-FIRST:event_btnMsgActionPerformed
    messagesMenu.setVisible(true);
  }//GEN-LAST:event_btnMsgActionPerformed

  private void EPPonoffActionPerformed(java.awt.event.ActionEvent evt)
  {//GEN-FIRST:event_EPPonoffActionPerformed
    if (isActiveEPP)
    {
      EPPuplink.gracefulStop();
    }
    else
    {
      EPPuplink.start();
    }
  }//GEN-LAST:event_EPPonoffActionPerformed

  private void btnDoTestActionPerformed(java.awt.event.ActionEvent evt)
  {//GEN-FIRST:event_btnDoTestActionPerformed
    // Esporta DB -> scegli destinazione ZIP e avvia esportazione
    JFileChooser chooser = new JFileChooser();
    chooser.setDialogTitle("Esporta DB");
    chooser.setFileFilter(new FileNameExtensionFilter("ZIP file", "zip"));
    try {
      String ts = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
      File suggested = new File(System.getProperty("user.home"), "epp-backup-" + ts + ".zip");
      chooser.setSelectedFile(suggested);
    } catch (Exception ignore) {}

    int result = chooser.showSaveDialog(this);
    if (result != JFileChooser.APPROVE_OPTION) {
      return;
    }

    File file = chooser.getSelectedFile();
    String path = file.getAbsolutePath();
    if (!path.toLowerCase().endsWith(".zip")) {
      file = new File(path + ".zip");
    }

    if (file.exists()) {
      int ow = JOptionPane.showConfirmDialog(this,
              "Il file esiste già. Sovrascrivere?\n" + file.getAbsolutePath(),
              "Conferma sovrascrittura",
              JOptionPane.YES_NO_OPTION,
              JOptionPane.WARNING_MESSAGE);
      if (ow != JOptionPane.YES_OPTION) return;
    }

    final File destFile = file;
    btnDoTest.setEnabled(false);

    SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
      private Exception error;
      @Override
      protected Void doInBackground() {
        try {
          new DbExporter().exportAllToZip(destFile);
        } catch (Exception ex) {
          error = ex;
        }
        return null;
      }

      @Override
      protected void done() {
        btnDoTest.setEnabled(true);
        if (error != null) {
          error.printStackTrace();
          JOptionPane.showMessageDialog(main.this,
                  "Errore durante l'esportazione: " + error.getMessage(),
                  "Errore",
                  JOptionPane.ERROR_MESSAGE);
        } else {
          JOptionPane.showMessageDialog(main.this,
                  "Esportazione completata:\n" + destFile.getAbsolutePath(),
                  "Operazione completata",
                  JOptionPane.INFORMATION_MESSAGE);
        }
      }
    };
    worker.execute();
  }//GEN-LAST:event_btnDoTestActionPerformed

  private void btnImportFromTxtActionPerformed(java.awt.event.ActionEvent evt)
  {//GEN-FIRST:event_btnImportFromTxtActionPerformed
    if (EPPuplink.isActive())
    {
      importMenu.setVisible(true);
    }
    else
    {
      sessionNotActiveWarning();
    }


  }//GEN-LAST:event_btnImportFromTxtActionPerformed

  public void setMSGQ(String txt)
  {
    msgQ.setText(txt);
  }

  public void setResCredit(String txt)
  {
    resCredit.setText(txt);
  }

  public void setNextMsg(String txt)
  {
    nextMsg.setText(txt);
  }

  public void addMsgtoList(Message message)
  {
    messagesMenu.addMessage(message);
  }

  public void setActiveEPP(int enableFlag)
  {
    isActiveEPP = enableFlag == 1;
    if (enableFlag == 1)
    {
      EPPonoff.setText("Termina Sessione");
      lblEPPworking.setText("Sessione EPP attiva");
      lblEPPworking.setEnabled(true);
      EPPonoff.setEnabled(true);
    }
    else if (enableFlag == 0)
    {
      EPPonoff.setText("Attiva Sessione");
      lblEPPworking.setText("Sessione EPP non attiva");
      lblEPPworking.setEnabled(false);
      EPPonoff.setEnabled(true);
    }
    else if (enableFlag == 2)
    {
      EPPonoff.setEnabled(false);
      lblEPPworking.setEnabled(false);
    }

    domainsMenu.setEPPEnablement(isActiveEPP);
    contactsMenu.setEPPEnablement(isActiveEPP);
    messagesMenu.setEPPEnablement(isActiveEPP);
    configMenu.setEPPEnablement(isActiveEPP);
    if (importMenu.isVisible())
      importMenu.setVisible(isActiveEPP);
  }

  private void setVersion(String version)
  {
    lblVersion.setText("Ver. " + version);
  }

  private void sessionNotActiveWarning()
  {
    JOptionPane.showMessageDialog(this, "Attenzione! Non è attiva alcuna sessione EPP con il server del Registro.\nImpossibile procedere!", "Sessione EPP non attiva", JOptionPane.WARNING_MESSAGE);
  }

  // Variables declaration - do not modify//GEN-BEGIN:variables

  private JLabel lblTitle;
  private JLabel lblCopy;
  private JTextField resCredit;
  private JLabel lblResCredit;
  private JTextField msgQ;
  private JLabel lblMsgQ;
  private JLabel lblSoci;
  private JTextField nextMsg;
  private JLabel lblNextMsg;
  private JLabel lblEPPworking;
  private JButton EPPonoff;
  private JLabel lblVersion;
  private JButton btnContact;
  private JButton btnDomain;
  private JButton btnMsg;
  private JButton btnConfig;
  private JButton btnInformazioni;
  private JButton btnDoTest;
  private JButton btnImportFromTxt;
  // End of variables declaration//GEN-END:variables

  private boolean isActiveEPP = false;

  private DomainsManagement domainsMenu;
  private ContactsManagement contactsMenu;
  private manageParameters configMenu;
  private MessageManagement messagesMenu;

  private TxtImport importMenu;

    private class btnInformazioni extends AbstractAction {
        private btnInformazioni() {
            // JFormDesigner - Action initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
          
            putValue(NAME, "Gestione Contatti");
            // JFormDesigner - End of action initialization  //GEN-END:initComponents  @formatter:on
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            // TODO add your code here
        }
    }
}
