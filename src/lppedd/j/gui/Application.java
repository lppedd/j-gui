package lppedd.j.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import lppedd.j.api.JConnection;
import lppedd.j.api.exceptions.JConnectionException;
import lppedd.j.gui.popups.signin.SignInDialog;
import lppedd.j.gui.tabs.compilefile.CompileFileComposite;
import lppedd.j.gui.tabs.printerfile.PrinterFileComposite;
import lppedd.j.gui.tabs.savefile.SaveFileComposite;
import lppedd.j.gui.tabs.users.UsersSearchComposite;
import lppedd.j.gui.tabs.v901.ConvertV901Composite;
import lppedd.j.gui.tabs.v901.MissingV901Composite;
import lppedd.j.gui.utils.UIUtils;

/**
 * @author Edoardo Luppi
 */
public class Application extends Shell
{
   /**
    * The only connection to an IBMi machine that will be used by the entire application.
    */
   private static JConnection connection;

   private CTabFolder tabFolder;
   private Composite compositeUpper;
   private CTabItem tabItemUsersSearch;
   private UsersSearchComposite compositeUsersSearch;
   private CTabItem tabItemMissingV901;
   private MissingV901Composite compositeMissingV901;
   private CTabItem tabItemFileCompile;
   private CompileFileComposite compositeCreateFile;
   private CTabItem tabItemPrinterAnalysis;
   private PrinterFileComposite compositePrinterAnalysis;
   private CTabItem tabItemSaveFile;
   private SaveFileComposite compositeSavFile;
   private CTabItem tabItemConvertV901;
   private ConvertV901Composite compositeConvertV901;

   /**
    * Establishes a connection to an IBMi machine.<br>
    * Only a single connection per application session is allowed.
    *
    * @param hostName
    *        The IP address or the name of the remote IBMi machine
    * @param user
    *        The user for sign-in purposes
    * @param password
    *        The password for sign-in purposes
    * @throws JConnectionException
    *         If the connection to the remote IBMi machine fails
    */
   public static void establishConnection(final String hostName, final String user, final String password) throws JConnectionException {
      if (connection != null) {
         connection.disconnect();
      }
      
      connection = new JConnection(hostName, user, password);
   }

   /**
    * Returns the connection used by the entire application.
    */
   public static JConnection getConnection() {
      return connection;
   }
   
   /**
    * Launches the application.
    */
   public static void main(final String... args) {
      final Display display = Display.getDefault();
      final Application shell = new Application(display);

      shell.layout();
      UIUtils.centerInDisplay(shell);
      shell.open();

      new SignInDialog(shell);
      final JConnection connection = getConnection();

      if (connection == null || !connection.isConnected()) {
         shell.close();
         return;
      }

      shell.setText("JGUI - " + connection.getAs400().getUserId());

      while (!shell.isDisposed()) {
         if (!display.readAndDispatch()) {
            display.sleep();
         }
      }
   }
   
   /**
    * Creates the shell.
    *
    * @param display
    */
   public Application(final Display display) {
      super(display, SWT.SHELL_TRIM);
      
      addDisposeListener(new DisposeListener() {
         @Override
         public void widgetDisposed(final DisposeEvent e) {
            final JConnection connection = getConnection();

            if (connection != null) {
               connection.disconnect();
            }
         }
      });
      
      setText("JGUI - Edoardo Luppi");
      setSize(550, 560);
      setMinimumSize(530, 450);
      setLayout(new GridLayout(1, false));
      createContents();
   }

   private void createContents() {
      compositeUpper = new Composite(this, SWT.NONE);
      compositeUpper.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
      compositeUpper.setLayout(new FillLayout(SWT.HORIZONTAL));
      
      tabFolder = new CTabFolder(compositeUpper, SWT.FLAT);
      tabFolder.setBorderVisible(true);
      tabFolder.setUnselectedImageVisible(false);
      tabFolder.setUnselectedCloseVisible(false);
      
      tabItemUsersSearch = new CTabItem(tabFolder, SWT.NONE);
      tabItemUsersSearch.setText("Ricerca utilizzatori");

      compositeUsersSearch = new UsersSearchComposite(tabFolder);
      tabItemUsersSearch.setControl(compositeUsersSearch);
      
      tabItemPrinterAnalysis = new CTabItem(tabFolder, SWT.NONE);
      tabItemPrinterAnalysis.setText("Analisi printer");

      compositePrinterAnalysis = new PrinterFileComposite(tabFolder);
      tabItemPrinterAnalysis.setControl(compositePrinterAnalysis);
      
      tabItemMissingV901 = new CTabItem(tabFolder, SWT.NONE);
      tabItemMissingV901.setText("V901 mancanti");
      
      compositeMissingV901 = new MissingV901Composite(tabFolder);
      tabItemMissingV901.setControl(compositeMissingV901);

      tabItemConvertV901 = new CTabItem(tabFolder, SWT.NONE);
      tabItemConvertV901.setText("Converti V901");

      compositeConvertV901 = new ConvertV901Composite(tabFolder);
      tabItemConvertV901.setControl(compositeConvertV901);

      tabItemFileCompile = new CTabItem(tabFolder, SWT.NONE);
      tabItemFileCompile.setText("Compila file");
      
      compositeCreateFile = new CompileFileComposite(tabFolder);
      tabItemFileCompile.setControl(compositeCreateFile);

      tabItemSaveFile = new CTabItem(tabFolder, SWT.NONE);
      tabItemSaveFile.setText("Save File");

      compositeSavFile = new SaveFileComposite(tabFolder, 0);
      tabItemSaveFile.setControl(compositeSavFile);
   }

   @Override
   protected void checkSubclass() {
      //
   }
}
