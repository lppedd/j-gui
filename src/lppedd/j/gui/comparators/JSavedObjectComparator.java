package lppedd.j.gui.comparators;

import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;

import lppedd.j.api.files.device.JSaveFile.JSavedObject;

/**
 * @author Edoardo Luppi
 */
public class JSavedObjectComparator extends ViewerComparator
{
   private int columnIndex = -1;
   private int sortDirection = SWT.UP;
   
   public int getSortDirection() {
      return sortDirection;
   }
   
   public void setSortedColumn(final int columnIndex) {
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
            if (!(object1 instanceof JSavedObject && object2 instanceof JSavedObject)) {
               return 0;
            }
            
            final JSavedObject first = (JSavedObject) object1;
            final JSavedObject second = (JSavedObject) object2;
            int i = 0;
            
            switch (columnIndex) {
               case 0:
                  i = first.getName().compareTo(second.getName());
                  break;
               case 1:
                  i = first.getType().compareTo(second.getType());
                  break;
               case 2:
                  i = first.getText().compareTo(second.getText());
                  break;
               default:
                  break;
            }

            return sortDirection == SWT.UP ? i : -i;
         }
      });
   }
}
