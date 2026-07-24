package util;

import java.sql.Date;
import java.time.LocalDate;
import model.ContractType;

public final class ContractRuleUtil {

	public static final String FIXED_TERM_CODE = "FIXED_TERM";
	public static final String INDEFINITE_CODE = "INDEFINITE";

	private ContractRuleUtil() {
	}

	public static boolean isAllowedContractTypeCode(String code) {
		if (code == null) {
			return false;
		}
		String normalized = code.trim().toUpperCase();
		return INDEFINITE_CODE.equals(normalized) || FIXED_TERM_CODE.equals(normalized);
	}

	public static String validateTerm(ContractType contractType, Date startDate, Date endDate) {
		if (contractType == null || !isAllowedContractTypeCode(contractType.getCode())) {
			return "Loại hợp đồng lao động chỉ được là không xác định thời hạn hoặc xác định thời hạn.";
		}
		if (startDate == null) {
			return "Ngày bắt đầu không hợp lệ.";
		}
		String code = contractType.getCode().trim().toUpperCase();
		if (INDEFINITE_CODE.equals(code)) {
			if (endDate != null) {
				return "Hợp đồng không xác định thời hạn không được nhập ngày kết thúc.";
			}
			return null;
		}
		if (endDate == null) {
			return "Hợp đồng xác định thời hạn phải có ngày kết thúc.";
		}
		LocalDate start = startDate.toLocalDate();
		LocalDate end = endDate.toLocalDate();
		if (!end.isAfter(start)) {
			return "Ngày kết thúc phải sau ngày bắt đầu.";
		}
		LocalDate minEndDate = start.plusMonths(12);
		LocalDate maxEndDate = start.plusMonths(36);
		if (end.isBefore(minEndDate) || end.isAfter(maxEndDate)) {
			return "Hợp đồng xác định thời hạn phải kéo dài từ đủ 12 tháng đến 36 tháng.";
		}
		return null;
	}
}
