package de.tototec.utils.jface.viewer;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Control;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ColorProvider implements Function<RGB, Color> {

	private final Logger log = LoggerFactory.getLogger(ColorProvider.class);

	private final Map<RGB, Color> colorCache = new LinkedHashMap<>();

	private final Control owner;

	public ColorProvider(final Control owner) {
		this.owner = owner;
		owner.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(final DisposeEvent arg0) {
				log.debug("Disposing {} created colors", colorCache.size());
				for (final Color color : colorCache.values()) {
					color.dispose();
				}
				colorCache.clear();
			}
		});
	}

	@Override
	public Color apply(final RGB rgb) {
		if (rgb == null) {
			return null;
		} else {
			final Color cached = colorCache.get(rgb);
			if (cached == null) {
				log.debug("Creating color {}", rgb);
				final Color color = new Color(owner.getDisplay(), rgb);
				colorCache.put(rgb, color);
				return color;
			} else {
				return cached;
			}
		}
	}

}
