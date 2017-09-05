package lppedd.j.gui.popups.progress;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import lppedd.j.gui.actions.ProgressAction;
import lppedd.j.gui.actions.ProgressListener;
import lppedd.j.gui.utils.UIUtils;

/**
 * @author Edoardo Luppi
 */
public class ProgressDialog extends Dialog implements ProgressListener
{
   private Shell dialogProgress;
   private ProgressComposite compositeProgress;
   private int progressBarStyle;
   
   /**
    * Listener which handles the Close event.
    */
   private final Listener closeListener = new Listener() {
      @Override
      public void handleEvent(final Event event) {
         event.doit = false;
      }
   };

   /**
    * The action that is running
    */
   private ProgressAction progressAction;
   
   /**
    * Creates the dialog.
    *
    * @param parent
    *        The parent shell
    */
   public ProgressDialog(final Shell parent) {
      super(parent, SWT.NONE);
   }

   /**
    * Open the dialog.
    *
    * @return the result
    */
   public void open(final ProgressAction progressAction, final int progressBarStyle) {
      this.progressAction = progressAction;
      this.progressBarStyle = progressBarStyle;
      
      createContents();
      
      dialogProgress.addListener(SWT.Close, closeListener);
      dialogProgress.open();
      dialogProgress.layout();
      dialogProgress.pack();
      UIUtils.centerInDisplay(dialogProgress);
      
      final Display display = getParent().getDisplay();
      
      // Execute the assigned action
      this.progressAction.addListener(this);
      this.progressAction.run();
      
      while (!dialogProgress.isDisposed()) {
         if (!display.readAndDispatch()) {
            display.sleep();
         }
      }
   }

   /**
    * Create contents of the dialog.
    */
   private void createContents() {
      dialogProgress = new Shell(getParent(), SWT.BORDER | SWT.TITLE | SWT.APPLICATION_MODAL);
      dialogProgress.setText("Avanzamento");
      dialogProgress.setSize(426, 197);
      dialogProgress.setMinimumSize(350, 125);
      dialogProgress.setLayout(new GridLayout(1, false));

      compositeProgress = new ProgressComposite(dialogProgress, progressBarStyle);
      compositeProgress.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
      compositeProgress.getButtonInterrupt().addSelectionListener(new SelectionAdapter() {
         @Override
         public void widgetSelected(final SelectionEvent e) {
            progressAction.interrupt();
         }
      });
   }

   @Override
   public void progressUpdated(final int progress) {
      compositeProgress.getDisplay().asyncExec(new Runnable() {
         @Override
         public void run() {
            compositeProgress.setProgress(progress);
         }
      });
   }
   
   @Override
   public void totalUpdated(final int total) {
      compositeProgress.getDisplay().asyncExec(new Runnable() {
         @Override
         public void run() {
            compositeProgress.setTotal(total);
         }
      });
   }

   @Override
   public void textUpdated(final String text) {
      compositeProgress.getDisplay().asyncExec(new Runnable() {
         @Override
         public void run() {
            compositeProgress.setMessage(text);
         }
      });
   }
   
   @Override
   public void finished() {
      progressAction.removeListener(this);
      dialogProgress.getDisplay().asyncExec(new Runnable() {
         @Override
         public void run() {
            dialogProgress.removeListener(SWT.Close, closeListener);
            dialogProgress.close();
         }
      });
   }
}
