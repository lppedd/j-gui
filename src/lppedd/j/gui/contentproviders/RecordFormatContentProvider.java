package lppedd.j.gui.contentproviders;

import org.eclipse.jface.viewers.ITreeContentProvider;

import lppedd.j.gui.models.RecordFormat;

/**
 * @author Edoardo Luppi
 */
public class RecordFormatContentProvider implements ITreeContentProvider
{
   @Override
   public Object[] getElements(final Object inputElement) {
      return (Object[]) inputElement;
   }

   @Override
   public Object[] getChildren(final Object parentElement) {
      return ((RecordFormat) parentElement).children;
   }

   @Override
   public Object getParent(final Object element) {
      return ((RecordFormat) element).parent;
   }

   @Override
   public boolean hasChildren(final Object element) {
      return ((RecordFormat) element).children.length != 0;
   }
}
