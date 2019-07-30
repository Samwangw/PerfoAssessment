package helper;

import java.text.SimpleDateFormat;
import java.util.Date;

import core.GlobalValues.CATEGORY;
import core.GlobalValues.LEVEL;
import core.coreParameters;

public class Helper {
	public static String formatEmployNumber(String oldNumber) {
		String number = oldNumber;
		if (number != null && number.length() > 0) {

			int index = number.indexOf(".");
			if (index > 0)
				number = number.substring(0, index);
			index = 0;
			while (number.startsWith("0"))
				number = number.substring(1, number.length());
		} else
			number = "0";
		return number;
	}

	public static String formatJournalTitle(String title) {
		String str = title.toLowerCase();
		if (title == null || title == "")
			return "";
		title.replaceAll(" and ", " & ");
		return str;
	}

	public static Double convertString2Double(String str) throws Exception {
		if (str == null)
			return 0.0;
		else {
			try {
				if (str.equals(""))
					return 0.0;
				else
					return Double.parseDouble(str);
			} catch (Exception e) {
				throw new Exception("\"" + str + "\" can't be converted into double");
			}
		}
	}

	public static int convertString2Int(String str) throws Exception {
		if (str == "")
			return 0;
		try {
			return Integer.parseInt(str);
		} catch (Exception e) {
			throw new Exception("\"" + str + "\" can't be converted into int");
		}

	}

	public static Boolean convertString2Bool(String str) {
		if (str == null || str == "")
			return null;
		try {
			str = str.toLowerCase();
			if (str.contains("teaching") && str.contains("research"))
				return false;
			else
				return true;
		} catch (Exception e) {
			System.out.print("\"" + str + "\" can't be converted to research only");
			return null;
		}
	}

	public static LEVEL convertString2Level(String str) throws Exception {
		try {
			if (str.toLowerCase().contains("ssg"))
				return LEVEL.E;
			else
				switch (str) {
				case "ACA.Level A":
					return LEVEL.A;
				case "ACA.Level B":
					return LEVEL.B;
				case "ACA.Level C":
					return LEVEL.C;
				case "ACA.Level D":
					return LEVEL.D;
				case "ACA.Level E":
					return LEVEL.E;
				default:
					throw new Exception("\"" + str + "\" cant be converted into Level.");
				}
		} catch (Exception e) {
			throw new Exception("\"" + str + "\" cant be converted into Level.");
		}
	}

	public static CATEGORY convertString2Category(String str) throws Exception {
		try {
			if (str == null || str == "")
				throw new Exception("\"" + str + "\" can't be converted into Category");
			str = str.toLowerCase();
			if (str.equals("FEIT.School of Mechanical and Mechatronic Engineering".toLowerCase())
					|| str.equals("FEIT.School of Civil and Environmental Engineering".toLowerCase())) {
				return CATEGORY.ENGINEERING;

			} else
				return CATEGORY.IT;
		} catch (Exception e) {
			throw new Exception("\"" + str + "\" can't be converted into Category");
		}
	}

	public static String[] parseStaffName(String str_raw_name) {
		String[] names = new String[] { "", "", "" };
		String name = str_raw_name.trim().toLowerCase();
		if (name.contains("(") && name.contains(")")) {
			names[2] = name.substring(name.indexOf("(") + 1, name.indexOf(")")).trim();
			name = name.substring(0, name.indexOf("("));
		}
		if (name.contains(",")) {
			int index_comma = name.indexOf(",");
			names[0] = name.substring(0, index_comma).trim();
			names[1] = name.substring(index_comma + 1, name.length()).trim();
		}
		for (int i = 0; i < names.length; i++) {
			names[i] = toUpperFristChar(names[i]);
		}
		return names;
	}

	public static String toUpperFristChar(String string) {
		if (string != "") {
			char[] charArray = string.toCharArray();
			charArray[0] -= 32;
			return String.valueOf(charArray);
		}
		return "";
	}

	public static int convertMonth2int(String month) {
		switch (month) {
		case "Jan":
			return 1;
		case "Feb":
			return 2;
		case "Mar":
			return 3;
		case "Apr":
			return 4;
		case "May":
			return 5;
		case "Jun":
			return 6;
		case "Jul":
			return 7;
		case "Aug":
			return 8;
		case "Sep":
			return 9;
		case "Oct":
			return 10;
		case "Nov":
			return 11;
		case "Dec":
			return 12;
		default:
			return 0;
		}
	}

	public static void mode_print(Object o) {
		if (coreParameters.DEBUG)
			System.out.println(o);
	}

	public static boolean checkYear(String year) {
		for (String y : coreParameters.years) {
			if (y.equalsIgnoreCase(y))
				return true;
		}
		return false;
	}

	public static String formatDateTime(Date d) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
		return format.format(d);
	}
}
