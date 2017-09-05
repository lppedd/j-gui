package lppedd.j.gui.popups.signin;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import lppedd.j.api.exceptions.JConnectionException;
import lppedd.j.gui.Application;
import lppedd.j.gui.utils.UIUtils;

/**
 * @author Edoardo Luppi
 */
public class SignInDialog extends Dialog
{
   private Shell dialogLogin;
   private SignInComposite compositeLogin;

   /**
    * The listener which handle the Enter key.
    */
   private final Listener enterListener = new Listener() {
      @Override
      public void handleEvent(final Event event) {
         if (event.character != SWT.CR) {
            return;
         }

         final SignInCredentials credentials = compositeLogin.getSignInCredentials();
         
         if (!credentials.isValid()) {
            return;
         }

         try {
            Application.establishConnection(credentials.getHostName(), credentials.getUser(), credentials.getPassword());
            dialogLogin.getDisplay().removeFilter(SWT.KeyUp, this);
            dialogLogin.close();
            return;
         } catch (final JConnectionException e) {
            e.printStackTrace();
         }

         compositeLogin.doShowError(true);
      }
   };
   
   /**
    * Creates the dialog.
    *
    * @param parent
    *        The parent shell
    */
   public SignInDialog(final Shell parent) {
      super(parent, SWT.ON_TOP);
      createContents();

      dialogLogin.layout();
      dialogLogin.pack();
      UIUtils.centerInDisplay(dialogLogin);
      dialogLogin.open();

      final Display display = getParent().getDisplay();
      
      while (!dialogLogin.isDisposed()) {
         if (!display.readAndDispatch()) {
            display.sleep();
         }
      }
   }
   
   /**
    * Create contents of the dialog.
    */
   private void createContents() {
      dialogLogin = new Shell(getParent(), SWT.DIALOG_TRIM | SWT.RESIZE | SWT.APPLICATION_MODAL);
      dialogLogin.setText("JGUI Sign-in");
      dialogLogin.setSize(375, 140);
      dialogLogin.setMinimumSize(350, 125);
      dialogLogin.setLayout(new FillLayout(SWT.HORIZONTAL));
      dialogLogin.getDisplay().addFilter(SWT.KeyUp, enterListener);

      compositeLogin = new SignInComposite(dialogLogin);
   }
}
