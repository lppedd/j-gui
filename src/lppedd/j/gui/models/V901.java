package lppedd.j.gui.models;

/**
 * @author Edoardo Luppi
 */
public class V901
{
   public final String file;
   public final String workItem;
   public final String user;

   public V901(final String file, final String workItem, final String user) {
      this.file = file;
      this.workItem = workItem;
      this.user = user;
   }

   @Override
   public String toString() {
      return file + " " + workItem + " " + user;
   }
}
