package de.tototec.utils.jface.viewer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

public enum Util {
	INSTANCE;

	public static <T, R> List<R> map(final Iterable<T> source, final Function<? super T, ? extends R> convert) {
		final List<R> result = (source instanceof Collection<?>) ? new ArrayList<R>(((Collection<?>) source).size())
				: new LinkedList<R>();
		for (final T t : source) {
			result.add(convert.apply(t));
		}
		return result;
	}

	public static <T, R> List<R> map(final T[] source, final Function<? super T, ? extends R> convert) {
		return map(Arrays.asList(source), convert);
	}

}
