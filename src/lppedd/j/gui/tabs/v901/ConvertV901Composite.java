package lppedd.j.gui.tabs.v901;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;

import lppedd.j.gui.actions.ConvertV901Action;
import lppedd.j.gui.popups.progress.ProgressDialog;

/**
 * @author Edoardo Luppi
 */
public class ConvertV901Composite extends Composite
{
   private final Group groupSource;
   private final Text textSourceMember;
   private final Text textSourceObject;
   private final Text textSourceLibrary;
   private final Button buttonConfirm;

   /**
    * Creates the composite.
    *
    * @param parent
    *        The parent composite
    */
   public ConvertV901Composite(final Composite parent) {
      super(parent, SWT.NONE);
      setLayout(new GridLayout(1, false));
      
      groupSource = new Group(this, SWT.NONE);
      groupSource.setLayout(new GridLayout(1, false));
      groupSource.setText("Sorgente V901");

      final GridData gdGroupSource = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
      gdGroupSource.widthHint = 160;

      groupSource.setLayoutData(gdGroupSource);
      
      textSourceMember = new Text(groupSource, SWT.BORDER);
      textSourceMember.setToolTipText("Membro");
      textSourceMember.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
      textSourceMember.setMessage("Membro");
      textSourceMember.setTextLimit(10);
      textSourceMember.setTextCase(SWT.UPPERCASE);
      textSourceMember.setTextType(SWT.ALPHANUMERIC);
      
      textSourceObject = new Text(groupSource, SWT.BORDER);
      textSourceObject.setToolTipText("Oggetto");
      textSourceObject.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
      textSourceObject.setMessage("Oggetto");
      textSourceObject.setTextLimit(10);
      textSourceObject.setTextCase(SWT.UPPERCASE);
      textSourceObject.setTextType(SWT.ALPHANUMERIC);
      
      textSourceLibrary = new Text(groupSource, SWT.BORDER);
      textSourceLibrary.setToolTipText("Libreria");
      textSourceLibrary.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
      textSourceLibrary.setMessage("Libreria");
      textSourceLibrary.setTextLimit(10);
      textSourceLibrary.setTextCase(SWT.UPPERCASE);
      textSourceLibrary.setTextType(SWT.ALPHANUMERIC);
      
      buttonConfirm = new Button(this, SWT.NONE);
      buttonConfirm.setText("Conferma");
      buttonConfirm.addSelectionListener(new SelectionAdapter() {
         @Override
         public void widgetSelected(final SelectionEvent e) {
            final String sourceMember = textSourceMember.getText().trim();
            final String sourceObject = textSourceObject.getText().trim();
            final String sourceLibrary = textSourceLibrary.getText().trim();
            
            if (sourceMember.isEmpty() || sourceObject.isEmpty() || sourceLibrary.isEmpty()) {
               return;
            }
            
            new ProgressDialog(getShell()).open(
                  new ConvertV901Action(sourceMember, sourceObject, sourceLibrary),
                  SWT.INDETERMINATE);
         }
      });
      
      final GridData gdButtonConfirm = new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1);
      gdButtonConfirm.widthHint = 100;
      gdButtonConfirm.heightHint = 23;
      
      buttonConfirm.setLayoutData(gdButtonConfirm);
   }
   
   @Override
   protected void checkSubclass() {
      //
   }
}
