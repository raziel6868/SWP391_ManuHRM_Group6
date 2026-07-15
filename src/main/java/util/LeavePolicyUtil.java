package util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Period;
import model.LeaveType;

public final class LeavePolicyUtil {

	private static final BigDecimal MONTHS_IN_YEAR = new BigDecimal("12");

	private LeavePolicyUtil() {
	}

	public static BigDecimal calculateRequestDays(LocalDate startDate, LocalDate endDate, String dayCountMethod) {
		if (startDate == null || endDate == null || endDate.isBefore(startDate)) {
			return BigDecimal.ZERO;
		}
		if ("CALENDAR_DAY".equalsIgnoreCase(dayCountMethod)) {
			return BigDecimal.valueOf(java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1);
		}
		long workingDays = 0;
		LocalDate current = startDate;
		while (!current.isAfter(endDate)) {
			DayOfWeek dayOfWeek = current.getDayOfWeek();
			if (dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY) {
				workingDays++;
			}
			current = current.plusDays(1);
		}
		return BigDecimal.valueOf(workingDays);
	}

	public static BigDecimal calculateAnnualEntitlement(LeaveType annualType, LocalDate firstContractStart, int year) {
		if (annualType == null || firstContractStart == null) {
			return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
		}

		LocalDate yearEnd = LocalDate.of(year, 12, 31);
		if (firstContractStart.isAfter(yearEnd)) {
			return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
		}

		int workingMonths = 12;
		if (firstContractStart.getYear() == year) {
			workingMonths = 12 - firstContractStart.getMonthValue() + 1;
		} else if (firstContractStart.getYear() > year) {
			workingMonths = 0;
		}

		BigDecimal baseDays = valueOrDefault(annualType.getBaseDays(), new BigDecimal("12"));
		BigDecimal proratedBase = baseDays.multiply(BigDecimal.valueOf(workingMonths)).divide(MONTHS_IN_YEAR, 2,
				RoundingMode.HALF_UP);

		BigDecimal seniorityBonus = BigDecimal.ZERO;
		if (Boolean.TRUE.equals(annualType.getHasSeniorityBonus())) {
			int intervalYears = annualType.getSeniorityIntervalYears() == null
					? 5
					: annualType.getSeniorityIntervalYears();
			BigDecimal bonusDays = valueOrDefault(annualType.getSeniorityBonusDays(), BigDecimal.ONE);
			if (intervalYears > 0) {
				int fullYears = Math.max(0, Period.between(firstContractStart, yearEnd).getYears());
				seniorityBonus = bonusDays.multiply(BigDecimal.valueOf(fullYears / intervalYears));
			}
		}

		return proratedBase.add(seniorityBonus).setScale(2, RoundingMode.HALF_UP);
	}

	private static BigDecimal valueOrDefault(BigDecimal value, BigDecimal fallback) {
		return value == null ? fallback : value;
	}
}
