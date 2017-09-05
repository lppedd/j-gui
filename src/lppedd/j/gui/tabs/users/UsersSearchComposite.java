package lppedd.j.gui.tabs.users;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

import lppedd.j.api.members.JMember;
import lppedd.j.gui.actions.UsersCopyAction;
import lppedd.j.gui.actions.UsersSearchAction;
import lppedd.j.gui.comparators.JMemberComparator;
import lppedd.j.gui.ewt.CompositeMapper;
import lppedd.j.gui.popups.progress.ProgressDialog;
import lppedd.j.gui.popups.workitem.WorkItemDialog;

/**
 * @author Edoardo Luppi
 */
public class UsersSearchComposite extends Composite
{
   private final Text textObject;
   private final Text textFilter;
   private final Table tableUsers;
   private final TableColumn tableColumnObject;
   private final TableColumn tableColumnLibrary;
   private final TableColumn tableColumnType;
   private final TableViewer tableUsersViewer;
   private final TableViewerColumn tableColumnObjectViewer;
   private final TableViewerColumn tableColumnLibraryViewer;
   private final TableViewerColumn tableColumnTypeViewer;
   private final Menu menuTableUsers;
   private final MenuItem menuItemCopyIn;
   private final Label labelItemCount;
   private final JMemberComparator tableUsersComparator = new JMemberComparator();

   /**
    * Contains the actual text of the {@link #textFilter} widget
    */
   private String textSearchValue = "";

   private final TraverseListener _traverseListener = new TraverseListener() {
      @Override
      public void keyTraversed(final TraverseEvent e) {
         if (SWT.TRAVERSE_RETURN == e.detail) {
            final String objectName = textObject.getText().trim();

            if (objectName.isEmpty()) {
               return;
            }

            new ProgressDialog(getShell()).open(
                  new UsersSearchAction(tableUsersViewer, objectName),
                  SWT.INDETERMINATE);
         }
      }
   };

   /**
    * Creates the composite.
    *
    * @param parent
    *        The parent composite
    */
   public UsersSearchComposite(final Composite parent) {
      super(parent, SWT.NONE);
      setData(SWT.CID, "compositeUsersSearch");
      setLayout(new GridLayout(2, true));

      textObject = new Text(this, SWT.BORDER);
      textObject.setTextLimit(10);
      textObject.setTextCase(SWT.UPPERCASE);
      textObject.setTextType(SWT.ALPHANUMERIC);
      textObject.setFurtherAllowedCharacters('_');
      textObject.setMessage("Nome dell'oggetto");
      textObject.setToolTipText("Nome dell'oggetto");
      textObject.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
      textObject.addTraverseListener(_traverseListener);

      textFilter = new Text(this, SWT.BORDER);
      textFilter.setTextLimit(10);
      textFilter.setTextCase(SWT.UPPERCASE);
      textFilter.setTextType(SWT.ALPHANUMERIC);
      textFilter.setFurtherAllowedCharacters('_', '*');
      textFilter.setMessage("Filtro");
      textFilter.setToolTipText("Filtro");
      textFilter.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
      textFilter.addTraverseListener(_traverseListener);
      textFilter.addKeyListener(new KeyAdapter() {
         @Override
         public void keyReleased(final KeyEvent e) {
            textSearchValue = textFilter.getText();
            tableUsersViewer.refresh();
         }
      });

      tableUsersViewer = new TableViewer(this, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
      tableUsersViewer.setContentProvider(ArrayContentProvider.getInstance());
      tableUsersViewer.setComparator(tableUsersComparator);
      tableUsersViewer.addFilter(new ViewerFilter() {
         @Override
         public boolean select(final Viewer viewer, final Object parentElement, final Object element) {
            if (textSearchValue.isEmpty()) {
               return true;
            }

            final JMember object = (JMember) element;
            return object.getName().contains(textSearchValue) ||
                  object.getLibrary().contains(textSearchValue) ||
                  object.getAttribute().contains(textSearchValue);
         }
      });

      tableUsers = tableUsersViewer.getTable();
      tableUsers.setLinesVisible(true);
      tableUsers.setHeaderVisible(true);
      tableUsers.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
      tableUsers.addMenuDetectListener(new MenuDetectListener() {
         @Override
         public void menuDetected(final MenuDetectEvent e) {
            if (tableUsers.getSelectionCount() < 1) {
               e.doit = false;
            }
         }
      });

      tableColumnObjectViewer = new TableViewerColumn(tableUsersViewer, SWT.NONE);
      tableColumnObjectViewer.setLabelProvider(new ColumnLabelProvider() {
         @Override
         public String getText(final Object element) {
            return ((JMember) element).getName();
         }
      });

      tableColumnObject = tableColumnObjectViewer.getColumn();
      tableColumnObject.setResizable(false);
      tableColumnObject.setWidth(120);
      tableColumnObject.setText("Oggetto");
      tableColumnObject.addSelectionListener(new SelectionAdapter() {
         @Override
         public void widgetSelected(final SelectionEvent e) {
            tableUsersComparator.setColumn(0);
            tableUsers.setSortColumn(tableColumnObject);
            tableUsers.setSortDirection(tableUsersComparator.getDirection());
            tableUsersViewer.refresh();
         }
      });

      tableUsersComparator.setColumn(0);
      tableUsers.setSortColumn(tableColumnObject);
      tableUsers.setSortDirection(SWT.UP);

      tableColumnLibraryViewer = new TableViewerColumn(tableUsersViewer, SWT.NONE);
      tableColumnLibraryViewer.setLabelProvider(new ColumnLabelProvider() {
         @Override
         public String getText(final Object element) {
            return ((JMember) element).getLibrary();
         }
      });

      tableColumnLibrary = tableColumnLibraryViewer.getColumn();
      tableColumnLibrary.setResizable(false);
      tableColumnLibrary.setWidth(120);
      tableColumnLibrary.setText("Libreria");
      tableColumnLibrary.addSelectionListener(new SelectionAdapter() {
         @Override
         public void widgetSelected(final SelectionEvent e) {
            tableUsersComparator.setColumn(1);
            tableUsers.setSortColumn(tableColumnLibrary);
            tableUsers.setSortDirection(tableUsersComparator.getDirection());
            tableUsersViewer.refresh();
         }
      });

      tableColumnTypeViewer = new TableViewerColumn(tableUsersViewer, SWT.NONE);
      tableColumnTypeViewer.setLabelProvider(new ColumnLabelProvider() {
         @Override
         public String getText(final Object element) {
            return ((JMember) element).getAttribute();
         }
      });

      tableColumnType = tableColumnTypeViewer.getColumn();
      tableColumnType.setResizable(false);
      tableColumnType.setWidth(120);
      tableColumnType.setText("Tipo");

      menuTableUsers = new Menu(tableUsers);
      tableUsers.setMenu(menuTableUsers);

      menuItemCopyIn = new MenuItem(menuTableUsers, SWT.NONE);
      menuItemCopyIn.setText("Copia selezionati...");
      menuItemCopyIn.addSelectionListener(new SelectionAdapter() {
         @Override
         public void widgetSelected(final SelectionEvent e) {
            new ProgressDialog(getShell()).open(new UsersCopyAction(tableUsersViewer, new WorkItemDialog(getShell()).open()), SWT.NONE);
         }
      });

      tableColumnType.addSelectionListener(new SelectionAdapter() {
         @Override
         public void widgetSelected(final SelectionEvent e) {
            tableUsersComparator.setColumn(2);
            tableUsers.setSortColumn(tableColumnType);
            tableUsers.setSortDirection(tableUsersComparator.getDirection());
            tableUsersViewer.refresh();
         }
      });

      labelItemCount = new Label(this, SWT.NONE);
      labelItemCount.setData(SWT.CID, "labelItemCount");

      final GridData gdLabelItemCount = new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1);
      gdLabelItemCount.exclude = true;

      labelItemCount.setLayoutData(gdLabelItemCount);

      CompositeMapper.map(this);
   }

   @Override
   protected void checkSubclass() {
      //
   }
}
