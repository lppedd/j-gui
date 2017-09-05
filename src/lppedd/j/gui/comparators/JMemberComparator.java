package lppedd.j.gui.comparators;

import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;

import lppedd.j.api.members.JMember;

/**
 * @author Edoardo Luppi
 */
public class JMemberComparator extends ViewerComparator
{
   private int columnIndex;
   private int sortDirection;
   
   public JMemberComparator() {
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
            if (!(object1 instanceof JMember && object2 instanceof JMember)) {
               return 0;
            }

            final JMember first = (JMember) object1;
            final JMember second = (JMember) object2;
            int i = 0;

            switch (columnIndex) {
               case 0:
                  i = first.getName().compareTo(second.getName());
                  break;
               case 1:
                  i = first.getLibrary().compareTo(second.getLibrary());
                  break;
               case 2:
                  i = first.getAttribute().compareTo(second.getAttribute());
                  break;
               default:
                  break;
            }

            return sortDirection == SWT.UP ? i : -i;
         }
      });
   }
}
