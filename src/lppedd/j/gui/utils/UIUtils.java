package lppedd.j.gui.utils;

import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Shell;

/**
 * @author Edoardo Luppi
 */
public class UIUtils
{
   public static final void centerInDisplay(final Shell shell) {
      final Rectangle bounds = shell.getDisplay().getPrimaryMonitor().getBounds();
      final Rectangle shellBounds = shell.getBounds();
      final int x = bounds.x + (bounds.width - shellBounds.width) / 2;
      final int y = bounds.y + (bounds.height - shellBounds.height) / 2;
      shell.setLocation(x, y);
   }
}
