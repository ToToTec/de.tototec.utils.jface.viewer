package de.tototec.utils.jface.viewer;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DecoratedLabelProvider<T> extends ColumnLabelProvider implements IColorProvider {
	
	private final Logger log = LoggerFactory.getLogger(DecoratedLabelProvider.class);
	
	private final ColumnLabelProvider labelProvider;
	private final Function<RGB, Color> colorProvider;
	private final Optional<BiFunction<T, RGB, RGB>> background;
	private final Optional<BiFunction<T, RGB, RGB>> foreground;
	private final Optional<BiFunction<T, String, String>> toolTip;

	public DecoratedLabelProvider(
			final ColumnLabelProvider labelProvider,
			final Function<RGB, Color> colorProvider,
			final Optional<BiFunction<T, RGB, RGB>> background,
			final Optional<BiFunction<T, RGB, RGB>> foreground,
			final Optional<BiFunction<T, String, String>> toolTip) {
		this.labelProvider = labelProvider;
		this.colorProvider = colorProvider;
		this.background = background;
		this.foreground = foreground;
		this.toolTip = toolTip;
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
		final String baseToolTipText = labelProvider.getToolTipText(element);
		return toolTip.map(d -> d.apply((T) element, baseToolTipText)).orElse(baseToolTipText);
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
		try {
			if (background.isPresent()) {
				final RGB baseRgb = baseColor == null ? null : baseColor.getRGB();
				final RGB rgb = background.get().apply((T) element, baseRgb);
				return rgb == null ? null : colorProvider.apply(rgb);
			}
		} catch (final Exception e) {
			log.error("Could not apply backgroudColorDecorator on element: {}", element, e);
			return baseColor;
		}
		return baseColor;
	}

	@Override
	public Color getForeground(final Object element) {
		final Color baseColor = labelProvider.getForeground(element);
		try {
			if (foreground.isPresent()) {
				final RGB baseRgb = baseColor == null ? null : baseColor.getRGB();
				final RGB rgb = foreground.get().apply((T) element, baseRgb);
				return rgb == null ? null : colorProvider.apply(rgb);
			}
		} catch (final Exception e) {
			log.error("Could not apply foregroudColorDecorator on element: {}", element, e);
			return baseColor;
		}
		return baseColor;
	}
}