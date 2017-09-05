package lppedd.j.gui.actions;

/**
 * @author Edoardo Luppi
 */
public interface ProgressListener
{
   public void progressUpdated(final int progress);
   
   public void totalUpdated(final int total);

   public void textUpdated(final String text);
   
   public void finished();
}
