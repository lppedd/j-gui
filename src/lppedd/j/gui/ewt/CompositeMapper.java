package lppedd.j.gui.ewt;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;

/**
 * @author Edoardo Luppi
 */
public final class CompositeMapper
{
   /**
    * Identificato per mappare un componente.
    */
   public static final String CID = "CID";
   
   /**
    * Mappa dei Composite e dei loro figli.
    */
   private static final Map<String, Map<String, Control>> COMPOSITES = new ConcurrentHashMap<>(16);

   public static Map<String, Control> get(final String compositeId) {
      return COMPOSITES.get(compositeId);
   }
   
   public static void map(final Composite composite) {
      final String compositeId = (String) composite.getData(CID);

      if (compositeId == null || compositeId.isEmpty()) {
         return;
      }

      final Control[] children = composite.getChildren();
      final int childrenLength = children.length;
      
      Map<String, Control> controlsMap = COMPOSITES.get(compositeId);

      if (controlsMap != null || childrenLength < 1) {
         return;
      }
      
      controlsMap = new ConcurrentHashMap<String, Control>(childrenLength * 3);
      COMPOSITES.put(compositeId, controlsMap);
      
      recursiveScan(controlsMap, children);
   }
   
   private static void recursiveScan(final Map<String, Control> controlsMap, final Control[] controls) {
      for (final Control control : controls) {
         final Class<? extends Control> className = control.getClass();
         
         if (Composite.class == className) {
            map((Composite) control);
         } else if (Group.class == className) {
            recursiveScan(controlsMap, ((Group) control).getChildren());
         } else {
            final String controlId = (String) control.getData(SWT.CID);

            if (controlId != null && !controlId.isEmpty()) {
               controlsMap.put(controlId, control);
            }
         }
      }
   }
}
