package de.tototec.utils.jface.viewer;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.ViewerColumn;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TreeColumn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mutable builder for {@link ViewerColumn}s.
 */
public class ViewerColumnBuilder<T> {

	private final Logger log = LoggerFactory.getLogger(ViewerColumnBuilder.class);

	private int style;
	private String header;
	private String headerTooltip;
	private Map<String, Object> dataMap = new LinkedHashMap<>();
	private Integer width;
	private ColumnLabelProvider columnLabelProvider;
	private Function<T, String> labelFunction;
	private EditingSupportBuilder<T, ?> editingSupportBuilder;
	private Boolean resizable;
	private Boolean moveable;
	private Integer layoutWeight;
	private Integer layoutWidth;
	private BiFunction<T, RGB, RGB> backgroudColorDecorator;
	private BiFunction<T, RGB, RGB> foregroudColorDecorator;
	private Function<RGB, Color> colorProvider;
	private BiFunction<T, String, String> toolTipDecorator;

	public ViewerColumnBuilder<T> setStyle(final int style) {
		this.style = style;
		return this;
	}

	public ViewerColumnBuilder<T> setHeader(final String header) {
		this.header = header;
		return this;
	}

	public ViewerColumnBuilder<T> setHeaderTooltip(final String headerTooltip) {
		this.headerTooltip = headerTooltip;
		return this;
	}

	public ViewerColumnBuilder<T> setData(final String dataKey, final Object dataValue) {
		this.dataMap.put(dataKey, dataValue);
		return this;
	}

	public ViewerColumnBuilder<T> setWidth(final int width) {
		this.width = width;
		return this;
	}

	public ViewerColumnBuilder<T> setLayoutWeight(final int weight, final int minWidth) {
		this.layoutWeight = weight;
		this.layoutWidth = minWidth;
		return this;
	}

	public ViewerColumnBuilder<T> setLayoutWidth(final int width) {
		this.layoutWidth = width;
		return this;
	}

	public ViewerColumnBuilder<T> setResizable(final boolean resizable) {
		this.resizable = resizable;
		return this;
	}

	public ViewerColumnBuilder<T> setMoveable(final boolean moveable) {
		this.moveable = moveable;
		return this;
	}

	public ViewerColumnBuilder<T> setLabelProvider(final ColumnLabelProvider columnLabelProvider) {
		this.columnLabelProvider = columnLabelProvider;
		return this;
	}

	public ViewerColumnBuilder<T> setLabelFunction(final Function<T, String> labelFunction) {
		this.labelFunction = labelFunction;
		return this;
	}

	public ViewerColumnBuilder<T> setToolTipDecorator(final BiFunction<T, String, String> toolTipDecorator) {
		this.toolTipDecorator = toolTipDecorator;
		return this;
	}

	public ViewerColumnBuilder<T> setEditingSupportBuilder(final EditingSupportBuilder<T, ?> editingSupportBuilder) {
		this.editingSupportBuilder = editingSupportBuilder;
		return this;
	}

	public ViewerColumnBuilder<T> setForegroundColorDecorator(
			final BiFunction<T, RGB, RGB> foregroudColorDecorator) {
		this.foregroudColorDecorator = foregroudColorDecorator;
		return this;
	}

	public ViewerColumnBuilder<T> setBackgroundColorDecorator(
			final BiFunction<T, RGB, RGB> backgroudColorDecorator) {
		this.backgroudColorDecorator = backgroudColorDecorator;
		return this;
	}

	protected CellLabelProvider createLabelProvider(final Function<RGB, Color> colorProvider) {
		final ColumnLabelProvider labelProvider;
		if (columnLabelProvider != null) {
			labelProvider = columnLabelProvider;
		} else if (labelFunction != null) {
			labelProvider = new ColumnLabelProvider() {
				@Override
				public String getText(final Object element) {
					try {
						@SuppressWarnings("unchecked")
						final T t = (T) element;
						return labelFunction.apply(t);
					} catch (final Exception e) {
						log.error("Could not apply labelFunction on element: {}", element, e);
						return "";
					}
				}
			};
		} else {
			labelProvider = new ColumnLabelProvider() {
				@Override
				public String getText(final Object element) {
					return "";
				}
			};
		}

		return new DecoratedLabelProvider<T>(
				labelProvider,
				colorProvider,
				Optional.ofNullable(backgroudColorDecorator),
				Optional.ofNullable(foregroudColorDecorator),
				Optional.ofNullable(toolTipDecorator));

	}

	public ViewerColumnBuilder<T> setColorProvider(final Function<RGB, Color> colorProvider) {
		this.colorProvider = colorProvider;
		return this;
	}

	protected void buildCommon(final Item column, final Layout layout) {
		final boolean hasTableLayout = layout instanceof TableLayout;
		final Optional<TableColumn> tableCol = column instanceof TableColumn ? Optional.of((TableColumn) column)
				: Optional.empty();
		final Optional<TreeColumn> treeCol = column instanceof TreeColumn ? Optional.of((TreeColumn) column)
				: Optional.empty();

		if (header != null) {
			column.setText(header);
		}
		if (headerTooltip != null) {
			tableCol.ifPresent(c -> c.setToolTipText(headerTooltip));
			treeCol.ifPresent(c -> c.setToolTipText(headerTooltip));
		}
		if (dataMap != null) {
			for (final Entry<String, Object> data : dataMap.entrySet()) {
				column.setData(data.getKey(), data.getValue());
			}
		}
		if (width != null && width >= 0) {
			tableCol.ifPresent(c -> c.setWidth(width.intValue()));
			treeCol.ifPresent(c -> c.setWidth(width.intValue()));
		} else if (!hasTableLayout && layoutWidth != null && layoutWidth >= 0) {
			tableCol.ifPresent(c -> c.setWidth(layoutWidth.intValue()));
			treeCol.ifPresent(c -> c.setWidth(layoutWidth.intValue()));
		}

		if (hasTableLayout) {
			final TableLayout tableLayout = (TableLayout) layout;
			if (layoutWeight != null) {
				if (layoutWidth != null) {
					tableLayout.addColumnData(new ColumnWeightData(layoutWeight, layoutWidth));
				} else {
					tableLayout.addColumnData(new ColumnWeightData(layoutWeight));
				}
			} else if (layoutWidth != null) {
				tableLayout.addColumnData(new ColumnPixelData(layoutWidth));
			} else {
				log.warn("Missing layout data but {} has a layout.", tableCol.isPresent() ? "table" : "tree");
			}
		} else {
			if (layoutWeight != null || layoutWidth != null) {
				log.warn("Missing table layout but column has some layout info.");
			}
		}

		if (resizable != null) {
			tableCol.ifPresent(c -> c.setResizable(resizable.booleanValue()));
			treeCol.ifPresent(c -> c.setResizable(resizable.booleanValue()));
		}
		if (moveable != null) {
			tableCol.ifPresent(c -> c.setMoveable(moveable.booleanValue()));
			treeCol.ifPresent(c -> c.setMoveable(moveable.booleanValue()));
		}

	}

	public TableViewerColumn build(final TableViewer tableViewer) {
		if (colorProvider == null) {
			colorProvider = new ColorProvider(tableViewer.getControl());
		}

		final TableViewerColumn tableViewerColumn = new TableViewerColumn(tableViewer, style);

		buildCommon(tableViewerColumn.getColumn(), tableViewer.getTable().getLayout());

		tableViewerColumn.setLabelProvider(createLabelProvider(colorProvider));

		if (editingSupportBuilder != null) {
			try {
				final EditingSupport editingSupport = editingSupportBuilder.build(tableViewer);
				tableViewerColumn.setEditingSupport(editingSupport);
			} catch (final IllegalStateException e) {
				LoggerFactory.getLogger(ViewerColumnBuilder.class).error("Could not build editing support.", e);
			}
		}

		return tableViewerColumn;
	}

	public TreeViewerColumn build(final TreeViewer treeViewer) {
		if (colorProvider == null) {
			colorProvider = new ColorProvider(treeViewer.getControl());
		}

		final TreeViewerColumn tableViewerColumn = new TreeViewerColumn(treeViewer, style);

		buildCommon(tableViewerColumn.getColumn(), treeViewer.getTree().getLayout());

		tableViewerColumn.setLabelProvider(createLabelProvider(colorProvider));

		if (editingSupportBuilder != null) {
			try {
				final EditingSupport editingSupport = editingSupportBuilder.build(treeViewer);
				tableViewerColumn.setEditingSupport(editingSupport);
			} catch (final IllegalStateException e) {
				LoggerFactory.getLogger(ViewerColumnBuilder.class).error("Could not build editing support.", e);
			}
		}

		return tableViewerColumn;
	}

	public boolean needsToolTipSupport() {
		return toolTipDecorator != null;
	}
}
