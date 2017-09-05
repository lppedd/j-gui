package lppedd.j.gui.actions;

import java.util.List;

import org.eclipse.jface.viewers.TreeViewer;

import lppedd.j.api.files.device.JSaveFile;
import lppedd.j.api.files.device.JSaveFile.JSavedObject;
import lppedd.j.api.objects.JObject;

/**
 * @author Edoardo Luppi
 */
public final class RemoteSaveFileAnalysisAction extends ProgressAction
{
   private final TreeViewer _treeViewer;
   private final JObject _object;
   
   public RemoteSaveFileAnalysisAction(final TreeViewer treeSavedObjectsViewer, final JObject object) {
      _treeViewer = treeSavedObjectsViewer;
      _object = object;
   }

   @Override
   protected void main() {
      if (!(_object instanceof JSaveFile)) {
         return;
      }

      updateText("Analisi del save file...");
      final List<JSavedObject> savedObjects = ((JSaveFile) _object).getSavedObjects();
      
      _treeViewer.getTree().getDisplay().asyncExec(new Runnable() {
         @Override
         public void run() {
            _treeViewer.setInput(savedObjects);
         }
      });
   }
}
