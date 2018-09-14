package de.tototec.utils.jface.viewer;

import java.util.LinkedList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.FocusCellOwnerDrawHighlighter;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerEditor;
import org.eclipse.jface.viewers.TableViewerFocusCellManager;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.TableColumn;

/**
 * Mutable builder for {@link TableViewer}s.
 */
public class TableViewerBuilder<T> {

	private BiFunction<TableViewer, ColumnViewerEditorActivationEvent, Boolean> activationStrategy;
	private int features = ColumnViewerEditor.DEFAULT;
	private boolean ownerDrawHighlighter;
	private List<ViewerFilter> viewerFilters;
	private List<ViewerColumnBuilder<T>> columnBuilders;
	private Boolean resizable;
	private Boolean moveable;
	private boolean withTableLayout;
	private Function<RGB, Color> colorProvider;

	public TableViewerBuilder() {
	}

	public TableViewerBuilder<T> withTableLayout() {
		this.withTableLayout = true;
		return this;
	}

	public TableViewerBuilder<T> setColumnsResizable(final boolean resizable) {
		this.resizable = resizable;
		return this;
	}

	public TableViewerBuilder<T> setColumnsMoveable(final boolean moveable) {
		this.moveable = moveable;
		return this;
	}

	public TableViewerBuilder<T> setEditorActivationStrategy(
			final BiFunction<TableViewer, ColumnViewerEditorActivationEvent, Boolean> activationStrategy) {
		this.activationStrategy = activationStrategy;
		return this;
	}

	/**
	 * Features flags are:
	 * <ul>
	 * <li>{@link ColumnViewerEditor#DEFAULT} - Tabbing from cell to cell is
	 * turned off</li>
	 * <li>{@link ColumnViewerEditor#KEEP_EDITOR_ON_DOUBLE_CLICK} - Style mask
	 * used to turn <strong>off</strong> the
	 * feature that an editor activation is canceled on double click.</li>
	 * <li>{@link ColumnViewerEditor#KEYBOARD_ACTIVATION} - Style mask used to
	 * enable keyboard activation.</li>
	 * <li>{@link ColumnViewerEditor#TABBING_CYCLE_IN_ROW} - Should if the end
	 * of the row is reach started from the
	 * beginning in the same row.</li>
	 * <li>{@link ColumnViewerEditor#TABBING_HORIZONTAL} - Should tabbing from
	 * column to column with in one row be
	 * supported.</li>
	 * <li>{@link ColumnViewerEditor#TABBING_MOVE_TO_ROW_NEIGHBOR} - Should if
	 * the end of the row is reach started from
	 * the start/end of the row below/above.</li>
	 * <li>{@link ColumnViewerEditor#TABBING_VERTICAL} - Support tabbing to Cell
	 * above/below the current cell.</li>
	 * </ul>
	 *
	 *
	 * @param featuresFlags
	 * @return
	 */
	public TableViewerBuilder<T> setEditorFeatures(final int featuresFlags) {
		this.features = featuresFlags;
		return this;
	}

	public TableViewerBuilder<T> setCellOwnerDrawHighlighter(final boolean ownerDrawHighlighter) {
		this.ownerDrawHighlighter = ownerDrawHighlighter;
		return this;
	}

	public TableViewerBuilder<T> addFilter(final ViewerFilter viewerFilter) {
		if (viewerFilters == null) {
			viewerFilters = new LinkedList<ViewerFilter>();
		}
		this.viewerFilters.add(viewerFilter);
		return this;
	}

	public TableViewerBuilder<T> addColumn(final ViewerColumnBuilder<T> tableViewerColumnBuilder) {
		if (columnBuilders == null) {
			columnBuilders = new LinkedList<ViewerColumnBuilder<T>>();
		}
		columnBuilders.add(tableViewerColumnBuilder);
		return this;
	}

	public ViewerColumnBuilder<T> addColumn() {
		final ViewerColumnBuilder<T> colBuilder = new ViewerColumnBuilder<>();
		addColumn(colBuilder);
		return colBuilder;
	}

	public TableViewerBuilder<T> setColorProvider(final Function<RGB, Color> colorProvider) {
		this.colorProvider = colorProvider;
		return this;
	}

	public TableViewer apply(final TableViewer tableViewer) {
		if (colorProvider == null) {
			colorProvider = new ColorProvider(tableViewer.getControl());
		}

		if (withTableLayout) {
			tableViewer.getTable().setLayout(new TableLayout());
		}

		if (columnBuilders != null) {
			for (final ViewerColumnBuilder<T> columnBuilder : columnBuilders) {
				columnBuilder.setColorProvider(colorProvider);
				columnBuilder.build(tableViewer);
			}
		}

		if (viewerFilters != null) {
			tableViewer.setFilters(viewerFilters.toArray(new ViewerFilter[viewerFilters.size()]));
		}

		final TableViewerFocusCellManager focusCellMgr;
		if (ownerDrawHighlighter) {
			focusCellMgr = new TableViewerFocusCellManager(tableViewer, new FocusCellOwnerDrawHighlighter(tableViewer));
		} else {
			focusCellMgr = null;
		}

		final ColumnViewerEditorActivationStrategy actStrategy;
		if (activationStrategy != null) {
			actStrategy = new ColumnViewerEditorActivationStrategy(tableViewer) {
				@Override
				protected boolean isEditorActivationEvent(final ColumnViewerEditorActivationEvent event) {
					return activationStrategy.apply(tableViewer, event);
				}

			};
		} else {
			actStrategy = new ColumnViewerEditorActivationStrategy(tableViewer);
		}

		if (actStrategy != null && focusCellMgr == null) {
			TableViewerEditor.create(tableViewer, actStrategy, features);
		} else if (actStrategy != null && focusCellMgr != null) {
			TableViewerEditor.create(tableViewer, focusCellMgr, actStrategy, features);
		}

		if (resizable != null) {
			for (final TableColumn column : tableViewer.getTable().getColumns()) {
				column.setResizable(resizable.booleanValue());
			}
		}
		if (moveable != null) {
			for (final TableColumn column : tableViewer.getTable().getColumns()) {
				column.setMoveable(moveable.booleanValue());
			}
		}

		return tableViewer;
	}
}
