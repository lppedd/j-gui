package lppedd.j.gui.comparators;

import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;

import lppedd.j.gui.models.V901;

/**
 * @author Edoardo Luppi
 */
public class V901Comparator extends ViewerComparator
{
   private int columnIndex;
   private int sortDirection;

   public V901Comparator() {
      sortDirection = SWT.DOWN;
   }
   
   public int getDirection() {
      return sortDirection;
   }
   
   public void setColumn(final int columnIndex) {
      if (this.columnIndex == columnIndex) {
         sortDirection = sortDirection == SWT.DOWN ? SWT.UP : SWT.DOWN;
      } else {
         this.columnIndex = columnIndex;
         sortDirection = SWT.UP;
      }
   }
   
   @Override
   public void sort(final Viewer viewer, final Object[] elements) {
      Arrays.sort(elements, new Comparator<Object>() {
         @Override
         public int compare(final Object object1, final Object object2) {
            if (!(object1 instanceof V901 && object2 instanceof V901)) {
               return 0;
            }
            
            final V901 first = (V901) object1;
            final V901 second = (V901) object2;
            int i = 0;
            
            switch (columnIndex) {
               case 0:
                  i = first.file.compareTo(second.file);
                  break;
               case 1:
                  i = first.workItem.compareTo(second.workItem);
                  break;
               case 2:
                  i = first.user.compareTo(second.user);
                  break;
               default:
                  break;
            }
            
            return sortDirection == SWT.UP ? i : -i;
         }
      });
   }
}
