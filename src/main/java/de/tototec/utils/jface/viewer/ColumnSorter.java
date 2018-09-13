package de.tototec.utils.jface.viewer;

import static de.tototec.utils.jface.viewer.Util.map;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.core.internal.commands.util.Util;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a utility class that enables sortable column for {@link TableViewer}s
 * and {@link TreeViewer}s.
 * <p>
 *
 * Features:
 *
 * <ul>
 * <li>Automatically sort column based on it's label provider
 * <li>Automatically toggle between ascending and descending when clicking on
 * the column header
 * <li>Support for alternative column sorters (if the label provider isn't
 * enough)
 * <li>Support for fallback sorter, which is used initially and also when the
 * user clicks multiple times on the column
 * header. If a fallback sorter is present, the click on the column header
 * cycles between ascending, descending and
 * fallback sorter.
 * <li>Only sort selected columns.
 * </ul>
 *
 */
public class ColumnSorter extends ViewerComparator {
	public static final int ASC = 1;
	public static final int NONE = 0;
	public static final int DESC = -1;

	private final Logger log = LoggerFactory.getLogger(ColumnSorter.class);

	private final ColumnViewer viewer;

	private int direction = 0;
	private TableColumn tableColumn = null;
	private TreeColumn treeColumn = null;
	private int columnIndex = 0;

	private Map<Object, ViewerComparator> customSorters = new LinkedHashMap<Object, ViewerComparator>();
	private ViewerComparator customSorter = null;
	private ViewerComparator fallbackComparator;

	/**
	 * Enabled column sorting for the given {@link TableViewer} and the given
	 * columns. If no columns are given, all current columns of the table will
	 * be used.
	 *
	 */
	public ColumnSorter(final TableViewer viewer, final TableViewerColumn... columns) {
		this(null, viewer, columns);
	}

	public ColumnSorter(final ViewerComparator fallbackComparator, final TableViewer viewer,
			final TableViewerColumn... columns) {
		this.fallbackComparator = fallbackComparator;
		this.viewer = viewer;
		if (viewer.getComparator() != null) {
			log.warn(
					"Viewer had a sorter before. If you need some initial or fallback comparator, add it as fallbackComparator instead.");
		}

		final Table table = viewer.getTable();

		final SelectionListener selectionHandler = new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				if (ColumnSorter.this != viewer.getComparator()) {
					log.warn("Wrong sorter");
				}
				final TableColumn selectedColumn = (TableColumn) e.widget;
				Assert.isTrue(table == selectedColumn.getParent());
				ColumnSorter.this.setTableColumn(table, selectedColumn);
				viewer.refresh();
			}
		};

		final TableColumn[] addToColumns = (columns == null || columns.length == 0)
				? table.getColumns()
				: map(columns, (final TableViewerColumn col) -> col.getColumn())
						.toArray(new TableColumn[0]);

		for (

		final TableColumn tableColumn : addToColumns) {
			tableColumn.addSelectionListener(selectionHandler);
		}

		viewer.setComparator(this);
	}

	public ColumnSorter(final TreeViewer viewer, final TreeViewerColumn... columns) {
		this(null, viewer, columns);
	}

	public ColumnSorter(final ViewerComparator fallbackComparator, final TreeViewer viewer,
			final TreeViewerColumn... columns) {
		this.fallbackComparator = fallbackComparator;
		this.viewer = viewer;
		if (viewer.getComparator() != null) {
			log.warn(
					"Viewer had a sorter before. If you need some initial or fallback comparator, add it as fallbackComparator instead.");
		}

		final Tree tree = viewer.getTree();

		final SelectionListener selectionHandler = new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				if (ColumnSorter.this != viewer.getComparator()) {
					log.warn("Wrong sorter");
				}
				final TreeColumn selectedColumn = (TreeColumn) e.widget;
				Assert.isTrue(tree == selectedColumn.getParent());
				ColumnSorter.this.setTreeColumn(tree, selectedColumn);
				viewer.refresh();
			}
		};

		final TreeColumn[] addToColumns = (columns == null || columns.length == 0)
				? tree.getColumns()
				: map(columns, (final TreeViewerColumn col) -> col.getColumn())
						.toArray(new TreeColumn[0]);

		for (final TreeColumn tableColumn : addToColumns) {
			tableColumn.addSelectionListener(selectionHandler);
		}

		viewer.setComparator(this);
	}

	public void setCustomSorter(final TreeColumn treeColumn, final ViewerComparator customSorter) {
		this.customSorters.put(treeColumn, customSorter);
	}

	public void setCustomSorter(final TableColumn tableColumn, final ViewerComparator customSorter) {
		this.customSorters.put(tableColumn, customSorter);
	}

	public void setFallbackSorter(final ViewerComparator fallbackComparator) {
		this.fallbackComparator = fallbackComparator;
	}

	protected void setTableColumn(final Table table, final TableColumn selectedColumn) {
		if (tableColumn == selectedColumn) {
			if (fallbackComparator != null) {
				direction = direction == ASC ? DESC : direction == DESC ? NONE : ASC;
			} else {
				direction = direction == ASC ? DESC : ASC;
			}
		} else {
			this.tableColumn = selectedColumn;
			this.direction = ASC;
		}

		customSorter = null;
		if (customSorters.containsKey(selectedColumn)) {
			customSorter = customSorters.get(selectedColumn);
		}

		final TableColumn[] columns = table.getColumns();
		boolean found = false;
		for (int i = 0; i < columns.length; i++) {
			final TableColumn theColumn = columns[i];
			if (theColumn == this.tableColumn) {
				columnIndex = i;
				found = true;
				break;
			}
		}

		if (!found) {
			direction = NONE;
			customSorter = fallbackComparator;
		}

		switch (direction) {
		case ASC:
			table.setSortColumn(selectedColumn);
			table.setSortDirection(SWT.UP);
			break;
		case DESC:
			table.setSortColumn(selectedColumn);
			table.setSortDirection(SWT.DOWN);
			break;
		default:
			table.setSortColumn(null);
			table.setSortDirection(SWT.NONE);
			break;
		}

	}

	protected void setTreeColumn(final Tree tree, final TreeColumn selectedColumn) {
		if (treeColumn == selectedColumn) {
			if (fallbackComparator != null) {
				direction = direction == ASC ? DESC : direction == DESC ? NONE : ASC;
			} else {
				direction = direction == ASC ? DESC : ASC;
			}
		} else {
			this.treeColumn = selectedColumn;
			this.direction = ASC;
		}

		customSorter = null;
		if (customSorters.containsKey(selectedColumn)) {
			customSorter = customSorters.get(selectedColumn);
		}

		final TreeColumn[] columns = tree.getColumns();
		boolean found = false;
		for (int i = 0; i < columns.length; i++) {
			final TreeColumn theColumn = columns[i];
			if (theColumn == this.treeColumn) {
				columnIndex = i;
				found = true;
				break;
			}
		}

		if (!found) {
			direction = NONE;
			customSorter = fallbackComparator;
		}

		switch (direction) {
		case ASC:
			tree.setSortColumn(selectedColumn);
			tree.setSortDirection(SWT.UP);
			break;
		case DESC:
			tree.setSortColumn(selectedColumn);
			tree.setSortDirection(SWT.DOWN);
			break;
		default:
			tree.setSortColumn(null);
			tree.setSortDirection(SWT.NONE);
			break;
		}

	}

	@Override
	public int compare(final Viewer viewer, final Object e1, final Object e2) {
		if (direction == 0) {
			return fallbackComparator == null ? 0 : fallbackComparator.compare(viewer, e1, e2);
		} else {
			return direction * doCompare(viewer, e1, e2);
		}
	}

	protected int doCompare(final Viewer v, final Object e1, final Object e2) {
		if (customSorter != null) {
			return customSorter.compare(v, e1, e2);
		}
		if (v != this.viewer) {
			log.warn("compare invoked on the wrong table. Ignoring");
			return 0;
		}
		final ILabelProvider labelProvider = (ILabelProvider) viewer.getLabelProvider(columnIndex);
		return Util.compare(labelProvider.getText(e1), labelProvider.getText(e2));
	}

}