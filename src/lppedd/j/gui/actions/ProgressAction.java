package lppedd.j.gui.actions;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

/**
 * @author Edoardo Luppi
 */
public abstract class ProgressAction implements Action, ProgressDispatcher
{
   private static final List<ProgressListener> LISTENERS = new CopyOnWriteArrayList<>();

   private int progress;
   private int total;
   private String text;

   private Control[] disabledControls;
   private boolean resetText;
   private boolean resetProgress;
   private boolean interrupted;

   @Override
   public final void run() {
      new Thread(new Runnable() {
         @Override
         public void run() {
            updateTotal(0);
            updateProgress(0);
            disableControls();

            try {
               main();
            } catch (final Exception e) {
               e.printStackTrace();
            }
            
            reset();
            enableControls();
            finished();
         }
      }).start();
   }

   protected abstract void main();

   @Override
   public void interrupt() {
      interrupted = true;
   }
   
   @Override
   public boolean isInterrupted() {
      return interrupted;
   }
   
   public void addListener(final ProgressListener listener) {
      LISTENERS.add(listener);
   }

   public void removeListener(final ProgressListener listener) {
      LISTENERS.remove(listener);
   }
   
   public void setDisabled(final Control... disabledControls) {
      this.disabledControls = disabledControls;
   }
   
   public void resetTextWhenFinished(final boolean resetText) {
      this.resetText = resetText;
   }

   public void resetProgressWhenFinished(final boolean resetProgress) {
      this.resetProgress = resetProgress;
   }

   public int getProgress() {
      return progress;
   }
   
   public int getTotal() {
      return total;
   }
   
   public String getText() {
      return text;
   }
   
   public float getPercentage() {
      return (float) progress / total * 100;
   }
   
   public void setTotal(final int total) {
      this.total = total;
   }

   public void setProgress(final int progress) {
      this.progress = progress;
   }
   
   public void setText(final String text) {
      this.text = text;
   }

   private void disableControls() {
      if (disabledControls == null) {
         return;
      }
      
      Display.getDefault().asyncExec(new Runnable() {
         @Override
         public void run() {
            for (final Control control : disabledControls) {
               control.setEnabled(false);
            }
         }
      });
   }

   private void enableControls() {
      if (disabledControls == null) {
         return;
      }

      Display.getDefault().asyncExec(new Runnable() {
         @Override
         public void run() {
            for (final Control control : disabledControls) {
               control.setEnabled(true);
            }
         }
      });
   }
   
   private void reset() {
      if (resetText) {
         updateText("In attesa...");
      }
      
      if (resetProgress) {
         updateTotal(0);
         updateProgress(0);
      }
   }
   
   @Override
   public void updateProgress(final int progress) {
      this.progress = progress;

      for (final ProgressListener listener : LISTENERS) {
         listener.progressUpdated(progress);
      }
   }

   @Override
   public void updateTotal(final int total) {
      this.total = total;

      for (final ProgressListener listener : LISTENERS) {
         listener.totalUpdated(total);
      }
   }

   @Override
   public void updateText(final String text) {
      this.text = text;

      for (final ProgressListener listener : LISTENERS) {
         listener.textUpdated(text);
      }
   }
   
   private void finished() {
      for (final ProgressListener listener : LISTENERS) {
         listener.finished();
      }
   }
}
