package de.tototec.utils.jface.viewer;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.ViewerColumn;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
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
	private Integer width;

	private ColumnLabelProvider columnLabelProvider;

	private Function<T, String> labelFunction;

	private EditingSupportBuilder<T, ?> editingSupportBuilder;

	private Boolean resizable;

	private Boolean moveable;

	private Integer layoutWeight;

	private Integer layoutWidth;

	private BiFunction<Object, Color, Color> backgroudColorDecorator;

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

	public ViewerColumnBuilder<T> setEditingSupportBuilder(final EditingSupportBuilder<T, ?> editingSupportBuilder) {
		this.editingSupportBuilder = editingSupportBuilder;
		return this;
	}

	public ViewerColumnBuilder<T> backgroundColorDecorator(
			final BiFunction<Object, Color, Color> backgroudColorDecorator) {
		this.backgroudColorDecorator = backgroudColorDecorator;
		return this;
	}

	protected CellLabelProvider createLabelProvider() {
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

		if (backgroudColorDecorator != null) {
			class Deco extends ColumnLabelProvider implements IColorProvider {
				private final ColumnLabelProvider labelProvider;
				private final Optional<BiFunction<Object, Color, Color>> background;

				public Deco(
						final ColumnLabelProvider labelProvider,
						final Optional<BiFunction<Object, Color, Color>> background) {
					this.labelProvider = labelProvider;
					this.background = background;
				}

				@Override
				public String getText(final Object element) {
					return labelProvider.getText(element);
				}

				@Override
				public Image getImage(final Object element) {
					return labelProvider.getImage(element);
				}

				@Override
				public Color getToolTipBackgroundColor(final Object object) {
					return labelProvider.getToolTipBackgroundColor(object);
				}

				@Override
				public int getToolTipDisplayDelayTime(final Object object) {
					return labelProvider.getToolTipDisplayDelayTime(object);
				}

				@Override
				public Font getToolTipFont(final Object object) {
					return labelProvider.getToolTipFont(object);
				}

				@Override
				public Color getToolTipForegroundColor(final Object object) {
					return labelProvider.getToolTipForegroundColor(object);
				}

				@Override
				public Image getToolTipImage(final Object object) {
					return labelProvider.getToolTipImage(object);
				}

				@Override
				public Point getToolTipShift(final Object object) {
					return labelProvider.getToolTipShift(object);
				}

				@Override
				public int getToolTipStyle(final Object object) {
					return labelProvider.getToolTipStyle(object);
				}

				@Override
				public String getToolTipText(final Object element) {
					return labelProvider.getToolTipText(element);
				}

				@Override
				public int getToolTipTimeDisplayed(final Object object) {
					return labelProvider.getToolTipTimeDisplayed(object);
				}

				@Override
				public Font getFont(final Object element) {
					return labelProvider.getFont(element);
				}

				@Override
				public Color getBackground(final Object element) {
					final Color baseColor = labelProvider.getBackground(element);
					if (background.isPresent()) {
						return background.get().apply(element, baseColor);
					}
					return baseColor;
				}

				@Override
				public Color getForeground(final Object element) {
					return labelProvider.getForeground(element);
				}
			}
			return new Deco(labelProvider, Optional.of(backgroudColorDecorator));
		}

		return labelProvider;
	}

	public TableViewerColumn build(final TableViewer tableViewer) {
		final boolean hasTableLayout = tableViewer.getTable().getLayout() instanceof TableLayout;

		final TableViewerColumn tableViewerColumn = new TableViewerColumn(tableViewer, style);
		if (header != null) {
			tableViewerColumn.getColumn().setText(header);
		}
		if (headerTooltip != null) {
			tableViewerColumn.getColumn().setToolTipText(headerTooltip);
		}
		if (width != null && width >= 0) {
			tableViewerColumn.getColumn().setWidth(width.intValue());
		} else if (!hasTableLayout && layoutWidth != null && layoutWidth >= 0) {
			tableViewerColumn.getColumn().setWidth(layoutWidth.intValue());
		}

		if (hasTableLayout) {
			final TableLayout tableLayout = (TableLayout) tableViewer.getTable().getLayout();
			if (layoutWeight != null) {
				if (layoutWidth != null) {
					tableLayout.addColumnData(new ColumnWeightData(layoutWeight, layoutWidth));
				} else {
					tableLayout.addColumnData(new ColumnWeightData(layoutWeight));
				}
			} else if (layoutWidth != null) {
				tableLayout.addColumnData(new ColumnPixelData(layoutWidth));
			} else {
				log.warn("Missing layout data but table has a layout.");
			}
		}

		if (resizable != null) {
			tableViewerColumn.getColumn().setResizable(resizable.booleanValue());
		}
		if (moveable != null) {
			tableViewerColumn.getColumn().setMoveable(moveable.booleanValue());
		}

		tableViewerColumn.setLabelProvider(createLabelProvider());

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
		final boolean hasTableLayout = treeViewer.getTree().getLayout() instanceof TableLayout;

		final TreeViewerColumn tableViewerColumn = new TreeViewerColumn(treeViewer, style);
		if (header != null) {
			tableViewerColumn.getColumn().setText(header);
		}
		if (headerTooltip != null) {
			tableViewerColumn.getColumn().setToolTipText(headerTooltip);
		}
		if (width != null && width >= 0) {
			tableViewerColumn.getColumn().setWidth(width.intValue());
		} else if (!hasTableLayout && layoutWidth != null && layoutWidth >= 0) {
			tableViewerColumn.getColumn().setWidth(layoutWidth.intValue());
		}

		if (hasTableLayout) {
			final TableLayout tableLayout = (TableLayout) treeViewer.getTree().getLayout();
			if (layoutWeight != null) {
				if (layoutWidth != null) {
					tableLayout.addColumnData(new ColumnWeightData(layoutWeight, layoutWidth));
				} else {
					tableLayout.addColumnData(new ColumnWeightData(layoutWeight));
				}
			} else if (layoutWidth != null) {
				tableLayout.addColumnData(new ColumnPixelData(layoutWidth));
			} else {
				log.warn("Missing layout data but tree has a table layout");
			}
		} else {
			if (layoutWeight != null || layoutWidth != null) {
				log.warn("Missing table layout but column has some layout info");
			}
		}
		if (resizable != null) {
			tableViewerColumn.getColumn().setResizable(resizable.booleanValue());
		}
		if (moveable != null) {
			tableViewerColumn.getColumn().setMoveable(moveable.booleanValue());
		}

		tableViewerColumn.setLabelProvider(createLabelProvider());

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

}
