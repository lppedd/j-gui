package lppedd.j.gui.popups.progress;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;

/**
 * @author Edoardo Luppi
 */
public class ProgressComposite extends Composite
{
   private final Label labelMessage;
   private final ProgressBar progressBar;
   private final Button buttonInterrupt;
   
   /**
    * Creates the composite.
    *
    * @param parent
    * @param style
    */
   public ProgressComposite(final Composite parent, final int progressBarStyle) {
      super(parent, SWT.NONE);
      setLayout(new GridLayout(1, false));

      progressBar = new ProgressBar(this, progressBarStyle);
      
      final GridData gdProgressBar = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
      gdProgressBar.verticalIndent = 10;
      gdProgressBar.heightHint = 20;
      
      progressBar.setLayoutData(gdProgressBar);

      labelMessage = new Label(this, SWT.NONE);
      labelMessage.setText("In attesa...");

      final GridData gdLabelMessage = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
      gdLabelMessage.heightHint = 35;

      labelMessage.setLayoutData(gdLabelMessage);

      buttonInterrupt = new Button(this, SWT.NONE);
      buttonInterrupt.setText("Interrompi");
      buttonInterrupt.addSelectionListener(new SelectionAdapter() {
         @Override
         public void widgetSelected(final SelectionEvent e) {
            buttonInterrupt.setEnabled(false);
            buttonInterrupt.setText("Interruzione...");
         }
      });
      
      final GridData gdButtonInterrupt = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
      gdButtonInterrupt.widthHint = 100;
      
      buttonInterrupt.setLayoutData(gdButtonInterrupt);
   }

   public void setProgress(final int progress) {
      progressBar.setSelection(progress);
   }
   
   public void setTotal(final int total) {
      progressBar.setMaximum(total);
   }

   public void setMessage(final String message) {
      labelMessage.setText(message);
   }
   
   public Button getButtonInterrupt() {
      return buttonInterrupt;
   }

   @Override
   protected void checkSubclass() {
      //
   }
}
