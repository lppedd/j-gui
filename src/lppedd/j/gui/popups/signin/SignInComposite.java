package lppedd.j.gui.popups.signin;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * @author Edoardo Luppi
 */
public class SignInComposite extends Composite
{
   private final Combo hostNameCombo;
   private final Text userText;
   private final Text passwordText;
   private final Label errorLabel;

   /**
    * Creates the composite.
    *
    * @param parent
    *        The parent composite
    */
   public SignInComposite(final Composite parent) {
      super(parent, SWT.NONE);
      setLayout(new GridLayout(1, false));

      hostNameCombo = new Combo(this, SWT.READ_ONLY);

      // TODO: load host names from a property file
      hostNameCombo.setItems("pub400.com");
      hostNameCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
      hostNameCombo.select(0);

      userText = new Text(this, SWT.BORDER);
      userText.setMessage("Utente");
      userText.setTextCase(SWT.UPPERCASE);
      userText.setTextType(SWT.ALPHANUMERIC);
      userText.setFurtherAllowedCharacters('_');
      userText.setTextLimit(10);
      userText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

      passwordText = new Text(this, SWT.BORDER | SWT.PASSWORD);
      passwordText.setMessage("Password");
      passwordText.setTextLimit(10);
      passwordText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

      final GridData gdLabelErrorMessage = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
      gdLabelErrorMessage.exclude = true;
      
      errorLabel = new Label(this, SWT.NONE);
      errorLabel.setLayoutData(gdLabelErrorMessage);
      errorLabel.setText("Dati errati o connessione assente");
      errorLabel.setTextStyle(SWT.BOLD);
      errorLabel.setForeground(getDisplay().getSystemColor(SWT.COLOR_RED));
      
      setTabList(new Control[] {
            userText, passwordText, hostNameCombo
      });
   }

   /**
    * Returns the sign-in credentials set on the user interface.
    */
   public SignInCredentials getSignInCredentials() {
      return new SignInCredentials(hostNameCombo.getText(), userText.getText(), passwordText.getText());
   }

   /**
    * You can use this method in case the sign-in process is not successful to show
    * an error message below the password field.
    *
    * @param show
    *        Indicate if the error message has to be shown
    */
   public void doShowError(final boolean show) {
      ((GridData) errorLabel.getLayoutData()).exclude = !show;
      requestLayout();
   }
}
