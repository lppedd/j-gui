package lppedd.j.gui.contentproviders;

import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;

import lppedd.j.api.files.device.JSaveFile.JSavedObject;

/**
 * @author Edoardo Luppi
 */
public class SavedObjectContentProvider implements ITreeContentProvider
{
   @Override
   public Object[] getElements(final Object inputElement) {
      return ((List<JSavedObject>) inputElement).toArray();
   }

   @Override
   public Object[] getChildren(final Object parentElement) {
      return ((JSavedObject) parentElement).getMembers().toArray();
   }

   @Override
   public Object getParent(final Object element) {
      return null;
   }

   @Override
   public boolean hasChildren(final Object element) {
      if (element instanceof JSavedObject) {
         return ((JSavedObject) element).getMembers().size() != 0;
      }

      return false;
   }
}
