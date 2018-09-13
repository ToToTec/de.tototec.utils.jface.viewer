package de.tototec.utils.jface.viewer;

import java.util.function.BiFunction;
import java.util.function.Function;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.LoggerFactory;

public class EditingSupportBuilder<O, E> {

	private Function<O, Boolean> editable;
	private BiFunction<Composite, O, CellEditor> cellEditor;
	private Function<O, E> getter;
	private Procedure2<O, E> setter;

	public EditingSupportBuilder<O, E> setEditable(final Function<O, Boolean> editable) {
		this.editable = editable;
		return this;
	}

	public EditingSupportBuilder<O, E> setCellEditor(final BiFunction<Composite, O, CellEditor> cellEditor) {
		this.cellEditor = cellEditor;
		return this;
	}

	public EditingSupportBuilder<O, E> setGetter(final Function<O, E> getter) {
		this.getter = getter;
		return this;
	}

	public EditingSupportBuilder<O, E> setSetter(final Procedure2<O, E> setter) {
		this.setter = setter;
		return this;
	}

	public EditingSupport build(final ColumnViewer tableViewer) {
		final BiFunction<Composite, O, CellEditor> localCellEditor = cellEditor;
		final Function<O, Boolean> localEditable = editable;
		final Function<O, E> localGetter = getter;
		final Procedure2<O, E> localSetter = setter;

		if (localCellEditor == null || localGetter == null || localSetter == null) {
			throw new IllegalStateException(
					"Not enough information to build an EditingSupport: CellEditor, Setter and Getter are needed.");
		}

		return new EditingSupport(tableViewer) {

			@Override
			protected boolean canEdit(final Object arg0) {
				if (localEditable != null) {
					try {
						@SuppressWarnings("unchecked")
						final O o = (O) arg0;
						return localEditable.apply(o);
					} catch (final ClassCastException e) {
						LoggerFactory.getLogger(EditingSupportBuilder.class).error(
								"Could not cast object (O) \"{}\" to type of editable function {}",
								new Object[] { arg0, localEditable, e });
						return false;
					}
				}
				return true;
			}

			@Override
			protected CellEditor getCellEditor(final Object arg0) {
				try {
					@SuppressWarnings("unchecked")
					final O o = (O) arg0;
					if (tableViewer instanceof TableViewer) {
						return localCellEditor.apply(((TableViewer) tableViewer).getTable(), o);
					} else if (tableViewer instanceof TreeViewer) {
						return localCellEditor.apply(((TreeViewer) tableViewer).getTree(), o);
					} else {
						return null;
					}
				} catch (final ClassCastException e) {
					LoggerFactory.getLogger(EditingSupportBuilder.class).error(
							"Could not cast element (O) \"{}\" to type of cellEditor function {}",
							new Object[] { arg0, localCellEditor, e });
					return null;
				}
			}

			@Override
			protected Object getValue(final Object arg0) {
				try {
					@SuppressWarnings("unchecked")
					final O o = (O) arg0;
					return localGetter.apply(o);
				} catch (final ClassCastException e) {
					LoggerFactory.getLogger(EditingSupportBuilder.class).error(
							"Could not cast element (O) \"{}\" to type of editor function {}",
							new Object[] { arg0, localGetter, e });
					return null;
				}
			}

			@Override
			protected void setValue(final Object arg0, final Object arg1) {
				try {
					@SuppressWarnings("unchecked")
					final O o = (O) arg0;
					@SuppressWarnings("unchecked")
					final E e = (E) arg1;
					localSetter.apply(o, e);
					tableViewer.update(arg0, null);
				} catch (final ClassCastException ex) {
					LoggerFactory.getLogger(EditingSupportBuilder.class).error(
							"Could not cast element (O) \"{}\" and/or (E) \"{}\" to type of editor function {}",
							new Object[] { arg0, arg1, localSetter, ex });
				}
			}
		};
	}
}
