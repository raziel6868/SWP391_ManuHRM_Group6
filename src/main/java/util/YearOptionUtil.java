package util;

import java.time.Year;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class YearOptionUtil {

	private YearOptionUtil() {
	}

	public static List<Integer> dataYearsWithCurrent(Collection<Integer> dataYears) {
		Set<Integer> years = new LinkedHashSet<>();
		if (dataYears != null) {
			years.addAll(dataYears);
		}
		years.add(currentYear());
		return descending(years);
	}

	public static List<Integer> dataYearsWithCurrentAndNext(Collection<Integer> dataYears) {
		Set<Integer> years = new LinkedHashSet<>();
		if (dataYears != null) {
			years.addAll(dataYears);
		}
		int currentYear = currentYear();
		years.add(currentYear);
		years.add(currentYear + 1);
		return descending(years);
	}

	public static List<Integer> configYears(Collection<Integer> dataYears) {
		int currentYear = currentYear();
		Set<Integer> years = new LinkedHashSet<>();
		for (int year = currentYear - 2; year <= currentYear + 2; year++) {
			years.add(year);
		}
		if (dataYears != null) {
			years.addAll(dataYears);
		}
		return ascending(years);
	}

	private static int currentYear() {
		return Year.now().getValue();
	}

	private static List<Integer> descending(Collection<Integer> years) {
		List<Integer> sorted = new ArrayList<>(years);
		sorted.sort(Collections.reverseOrder());
		return sorted;
	}

	private static List<Integer> ascending(Collection<Integer> years) {
		List<Integer> sorted = new ArrayList<>(years);
		Collections.sort(sorted);
		return sorted;
	}
}
