package de.tototec.utils.jface.viewer;

import java.util.Optional;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SelectionHelper {

	private final Logger log = LoggerFactory.getLogger(SelectionHelper.class);

	public <T> Optional<T> getSelected(final ISelectionProvider selectionProvider, final Class<T> type) {
		return getSelected(selectionProvider.getSelection(), type);
	}

	public <T> Optional<T> getSelected(final ISelection selection, final Class<T> type) {
		if (selection.isEmpty()) {
			return Optional.empty();
		}

		if (!(selection instanceof IStructuredSelection)) {
			log.error("Selection of type {} required, but got: {}.", IStructuredSelection.class.getName(),
					selection.getClass().getName());
			return Optional.empty();
		}

		final IStructuredSelection structSelect = (IStructuredSelection) selection;
		final Object firstElement = structSelect.getFirstElement();
		if (!type.isInstance(firstElement)) {
			log.error("Selection must contain a {}, but has a {}.",
					type.getName(), firstElement.getClass().getName());
			return Optional.empty();
		} else {
			return Optional.of(type.cast(firstElement));
		}
	}

}
