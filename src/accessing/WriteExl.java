package accessing;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import core.GlobalValues.PUBLICATION_TYPE;
import core.Publication;
import core.Staff;
import core.coreParameters;

public class WriteExl {
	public Map<String, XSSFSheet> school_sheet_map = new HashMap<String, XSSFSheet>();
	public String[] _titles = new String[] { "Employee Number", "Name", "x1", "x2", "x3", "journal", "conference",
			"book", "chapter", "JCR20", "weight", "citation", "Principal", "Joint", "Performance", "Level", "Position",
			"Organization", "Grade", "research_only", "start date" };
	public String[] _titles2 = new String[] { "Employee Number", "Name", "LEC", "NON-LEC", "Coordinated", "Total",
			"LEVEL" };

	public void write(String path, Map<String, Staff> staff_Dic_byID, Map<String, Staff> staff_Dic_byName) {

		File file = new File(path);
		if (file.exists()) {
			if (file.isFile()) {
				file.delete();
			}
		}

		Map<String, ArrayList<Staff>> OrgaDic = new HashMap<String, ArrayList<Staff>>();
		for (Staff s : staff_Dic_byID.values()) {
			String orga_name = s.organization;
			if (OrgaDic.get(orga_name) == null) {
				ArrayList<Staff> stafflist = new ArrayList<Staff>();
				OrgaDic.put(orga_name, stafflist);
				double[] data = this.getPerforData(s);
				s.calculateScoreA(data);
				OrgaDic.get(orga_name).add(s);
			} else {
				double[] data = this.getPerforData(s);
				s.calculateScoreA(data);
				for (int i = 0; i < OrgaDic.get(orga_name).size(); i++) {
					if (OrgaDic.get(orga_name).get(i).score < s.score) {
						OrgaDic.get(orga_name).add(i, s);
						break;
					}
					if (i == OrgaDic.get(orga_name).size() - 1) {
						OrgaDic.get(orga_name).add(s);
						break;
					}
				}
			}
		}

		for (String organame : OrgaDic.keySet()) {
			ArrayList<Staff> staffList = OrgaDic.get(organame);
			int orga_size = staffList.size();
			for (int i = 0; i < staffList.size(); i++) {
				Staff s = staffList.get(i);
				if (i <= orga_size * 0.05)
					s.AcaPerfLevel = "Above";
				else if (i <= orga_size * 0.5)
					s.AcaPerfLevel = "Middle";
				else if (i <= orga_size * 0.85)
					s.AcaPerfLevel = "Below";
				else
					s.AcaPerfLevel = "Low";
			}
		}

		XSSFWorkbook workbook = createNewWorkbook();
		XSSFSheet overall_sheet = addNewSheet(workbook, "Performance report");
		System.out.print("Begin creat the excel...");
		// create head
		addColumnTitles(overall_sheet, _titles);
		// insert count numbers
		for (Staff s : staff_Dic_byID.values()) {
			// System.out.println(s.employNumber);
			if (s.excluded)
				continue;
			addStaffRecord(overall_sheet, s);
			String orga_name = s.organization.substring(s.organization.indexOf(" ") + 1).replace("/", "-");
			int poi = 0;
			for (int i = 0; i < orga_name.length(); i++) {
				char ch = orga_name.charAt(i);
				if (Character.isUpperCase(ch)) {
					poi = i;
					break;
				}
			}
			orga_name = orga_name.substring(poi);
			if (school_sheet_map.get(orga_name) == null) {
				XSSFSheet subSheet = addNewSheet(workbook, orga_name);
				school_sheet_map.put(orga_name, subSheet);
				addColumnTitles(subSheet, _titles);
			}
			XSSFSheet sheet = school_sheet_map.get(orga_name);
			addStaffRecord(sheet, s);
		}

		try {
			FileOutputStream outputStream = new FileOutputStream(path);
			workbook.write(outputStream);
			workbook.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Done!");
	}

	private double[] getPerforData(Staff s) {
		double[] data = new double[] { 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		String[] years = coreParameters.years;
		double[] pub_by_types = new double[] { 0, 0, 0, 0 };
		for (String year : years) {
			if (s.incomes_by_years.get(year) != null) {
				data[0] += s.incomes_by_years.get(year)[0];// x1
				data[1] += s.incomes_by_years.get(year)[1];// x2
				data[2] += s.incomes_by_years.get(year)[2];// x3
			}
			if (s.publications_by_years.get(year) != null) {
				ArrayList<Publication> pubs = s.publications_by_years.get(year);
				data[3] += pubs.size();// total pub
				Iterator<Publication> iterator = pubs.iterator();
				double temp4 = 0;
				double temp6 = 0;
				while (iterator.hasNext()) {
					Publication pub = iterator.next();

					temp4 += 1.0 / pub.numberOfAuthors;// weight
					if (pub.isJCR20)
						data[5] += 1.0;// JCR20
					temp6 += pub.citation;// citation
					if (pub.type == PUBLICATION_TYPE.JOURNAL)
						pub_by_types[0] += 1;
					else if (pub.type == PUBLICATION_TYPE.CONFERENCE)
						pub_by_types[1] += 1;
					else if (pub.type == PUBLICATION_TYPE.BOOK)
						pub_by_types[2] += 1;
					else if (pub.type == PUBLICATION_TYPE.CHAPTER)
						pub_by_types[3] += 1;
				}
				data[4] += temp4 / (double) (pub_by_types[0] + pub_by_types[1] + pub_by_types[2] + pub_by_types[3]);
				data[4] = temp4;
				data[6] = temp6;
				// data[6] += temp6 / (double)(pub_by_types[0] + pub_by_types[1]
				// + pub_by_types[2] + pub_by_types[3]);
			}

			if (s.students_by_years.get(year) != null) {
				data[7] += s.students_by_years.get(year)[0];// primary
				data[8] += s.students_by_years.get(year)[1];// co
			}
		}

		for (int i = 0; i < data.length; i++) {
			data[i] /= (double) years.length;
		}

		if (data[6] > 150)
			data[6] = 150;

		for (int i = 0; i < pub_by_types.length; i++) {
			pub_by_types[i] /= (double) years.length;
		}
		return data;
	}

	private XSSFWorkbook createNewWorkbook() {
		return new XSSFWorkbook();
	}

	private XSSFSheet addNewSheet(XSSFWorkbook workbook, String sheetName) {
		return workbook.createSheet(sheetName);
	}

	public void addColumnTitles(XSSFSheet sheet, String[] titles) {
		Row row = sheet.createRow(0);
		XSSFCellStyle style = createCellStyle(sheet);
		for (int i = 0; i < titles.length; i++) {
			Cell cell = row.createCell(i);
			cell.setCellValue(titles[i]);
			cell.setCellStyle(style);
		}
		sheet.setAutoFilter(new CellRangeAddress(0, 0, 0, 20));
		// sheet.setAutoFilter(new CellRangeAddress(0, 0, 0, numColumns));
		sheet.createFreezePane(2, 1);
	}

	public void addStaffRecord2(XSSFSheet sheet, Staff s) {
		double[] data = new double[] { 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		String[] years = coreParameters.years;
		double[] pub_by_types = new double[] { 0, 0, 0, 0 };
		for (String year : years) {
			if (s.incomes_by_years.get(year) != null) {
				data[0] += s.incomes_by_years.get(year)[0];// x1
				data[1] += s.incomes_by_years.get(year)[1];// x2
				data[2] += s.incomes_by_years.get(year)[2];// x3
			}
			double temp4 = 0;
			double temp6 = 0;
			if (s.publications_by_years.get(year) != null) {
				ArrayList<Publication> pubs = s.publications_by_years.get(year);
				data[3] += pubs.size();// total pub
				Iterator<Publication> iterator = pubs.iterator();
				while (iterator.hasNext()) {
					Publication pub = iterator.next();
					temp4 += 1.0 / pub.numberOfAuthors;// weight
					if (pub.isJCR20)
						data[5] += 1.0;// JCR20
					temp6 += pub.citation;// citation
					if (pub.type == PUBLICATION_TYPE.JOURNAL)
						pub_by_types[0] += 1;
					else if (pub.type == PUBLICATION_TYPE.CONFERENCE)
						pub_by_types[1] += 1;
					else if (pub.type == PUBLICATION_TYPE.BOOK)
						pub_by_types[2] += 1;
					else if (pub.type == PUBLICATION_TYPE.CHAPTER)
						pub_by_types[3] += 1;
				}
				data[4] += temp4 / (double) (pub_by_types[0] + pub_by_types[1] + pub_by_types[2] + pub_by_types[3]);
				data[4] = temp4;
				data[6] = temp6;
				// data[6] += temp6 / (double)(pub_by_types[0] + pub_by_types[1]
				// + pub_by_types[2] + pub_by_types[3]);
			}

			if (s.students_by_years.get(year) != null) {
				data[7] += s.students_by_years.get(year)[0];// primary
				data[8] += s.students_by_years.get(year)[1];// co
			}
		}

		for (int i = 0; i < data.length; i++) {
			data[i] /= (double) years.length;
		}

		if (data[6] > 150)
			data[6] = 150;

		for (int i = 0; i < pub_by_types.length; i++) {
			pub_by_types[i] /= (double) years.length;
		}
		int rowIndex = sheet.getLastRowNum() + 1;
		Row row = sheet.createRow(rowIndex);
		int colNum = 0;
		Cell cell = row.createCell(colNum++);
		cell.setCellValue(s.employNumber);
		cell = row.createCell(colNum++);
		cell.setCellValue(s.str_fullName);
		cell = row.createCell(colNum++);
		cell.setCellValue(Math.round(data[0] * 100) / 100.0);
		cell = row.createCell(colNum++);
		cell.setCellValue(Math.round(data[1] * 100) / 100.0);
		cell = row.createCell(colNum++);
		cell.setCellValue(Math.round(data[2] * 100) / 100.0);
		cell = row.createCell(colNum++);
		cell.setCellValue(pub_by_types[0]);
		cell = row.createCell(colNum++);
		cell.setCellValue(pub_by_types[1]);
		cell = row.createCell(colNum++);
		cell.setCellValue(pub_by_types[2]);
		cell = row.createCell(colNum++);
		cell.setCellValue(pub_by_types[3]);
		cell = row.createCell(colNum++);
		cell.setCellValue(data[5]);
		cell = row.createCell(colNum++);
		cell.setCellValue(Math.round(data[4] * 100) / 100.0);
		cell = row.createCell(colNum++);
		cell.setCellValue(data[6]);
		cell = row.createCell(colNum++);
		cell.setCellValue(data[7]);
		cell = row.createCell(colNum++);
		cell.setCellValue(data[8]);
		s.calculateScoreA(data);
		cell = row.createCell(colNum++);
		cell.setCellValue(Math.round(s.score * 100) / 100.0);
		cell = row.createCell(colNum++);

		// cell.setCellValue(s.str_level);
		cell.setCellValue(s.AcaPerfLevel);
		cell = row.createCell(colNum++);
		cell.setCellValue(s.position);
		cell = row.createCell(colNum++);
		cell.setCellValue(s.organization);
		cell = row.createCell(colNum++);
		cell.setCellValue(s.str_level);
		cell = row.createCell(colNum++);
		if (s.research_only)
			cell.setCellValue("Yes");
		else
			cell.setCellValue("No");
		cell = row.createCell(colNum++);
		cell.setCellValue(s.str_startDate);
	}

	public void addStaffRecord(XSSFSheet sheet, Staff s) {
		double[] data = new double[] { 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		String[] years = coreParameters.years;
		double[] pub_by_types = new double[] { 0, 0, 0, 0 };
		for (String year : years) {
			if (s.incomes_by_years.get(year) != null) {
				data[0] += s.incomes_by_years.get(year)[0];// x1
				data[1] += s.incomes_by_years.get(year)[1];// x2
				data[2] += s.incomes_by_years.get(year)[2];// x3
			}
			double temp4 = 0;
			double temp6 = 0;
			if (s.publications_by_years.get(year) != null) {
				ArrayList<Publication> pubs = s.publications_by_years.get(year);
				data[3] += pubs.size();// total pub
				Iterator<Publication> iterator = pubs.iterator();
				while (iterator.hasNext()) {
					Publication pub = iterator.next();
					temp4 += 1.0 / pub.numberOfAuthors;// weight
					if (pub.isJCR20)
						data[5] += 1.0;// JCR20
					temp6 += pub.citation;// citation
					if (pub.type == PUBLICATION_TYPE.JOURNAL)
						pub_by_types[0] += 1;
					else if (pub.type == PUBLICATION_TYPE.CONFERENCE)
						pub_by_types[1] += 1;
					else if (pub.type == PUBLICATION_TYPE.BOOK)
						pub_by_types[2] += 1;
					else if (pub.type == PUBLICATION_TYPE.CHAPTER)
						pub_by_types[3] += 1;
				}
				data[4] += temp4 / (double) (pub_by_types[0] + pub_by_types[1] + pub_by_types[2] + pub_by_types[3]);
				data[4] = temp4;
				data[6] = temp6;
				// data[6] += temp6 / (double)(pub_by_types[0] + pub_by_types[1]
				// + pub_by_types[2] + pub_by_types[3]);
			}

			if (s.students_by_years.get(year) != null) {
				data[7] += s.students_by_years.get(year)[0];// primary
				data[8] += s.students_by_years.get(year)[1];// co
			}
		}

		for (int i = 0; i < data.length; i++) {
			data[i] /= (double) years.length;
		}

		if (data[6] > 150)
			data[6] = 150;

		for (int i = 0; i < pub_by_types.length; i++) {
			pub_by_types[i] /= (double) years.length;
		}
		int rowIndex = sheet.getLastRowNum() + 1;
		Row row = sheet.createRow(rowIndex);
		int colNum = 0;
		Cell cell = row.createCell(colNum++);
		cell.setCellValue(s.employNumber);
		cell = row.createCell(colNum++);
		cell.setCellValue(s.str_fullName);
		cell = row.createCell(colNum++);
		cell.setCellValue(Math.round(data[0] * 100) / 100.0);
		cell = row.createCell(colNum++);
		cell.setCellValue(Math.round(data[1] * 100) / 100.0);
		cell = row.createCell(colNum++);
		cell.setCellValue(Math.round(data[2] * 100) / 100.0);
		cell = row.createCell(colNum++);
		cell.setCellValue(pub_by_types[0]);
		cell = row.createCell(colNum++);
		cell.setCellValue(pub_by_types[1]);
		cell = row.createCell(colNum++);
		cell.setCellValue(pub_by_types[2]);
		cell = row.createCell(colNum++);
		cell.setCellValue(pub_by_types[3]);
		cell = row.createCell(colNum++);
		cell.setCellValue(data[5]);
		cell = row.createCell(colNum++);
		cell.setCellValue(Math.round(data[4] * 100) / 100.0);
		cell = row.createCell(colNum++);
		cell.setCellValue(data[6]);
		cell = row.createCell(colNum++);
		cell.setCellValue(data[7]);
		cell = row.createCell(colNum++);
		cell.setCellValue(data[8]);
		s.calculateScoreA(data);
		cell = row.createCell(colNum++);
		cell.setCellValue(Math.round(s.score * 100) / 100.0);
		cell = row.createCell(colNum++);

		// cell.setCellValue(s.str_level);
		cell.setCellValue(s.AcaPerfLevel);
		cell = row.createCell(colNum++);
		cell.setCellValue(s.position);
		cell = row.createCell(colNum++);
		cell.setCellValue(s.organization);
		cell = row.createCell(colNum++);
		cell.setCellValue(s.str_level);
		cell = row.createCell(colNum++);
		if (s.research_only)
			cell.setCellValue("Yes");
		else
			cell.setCellValue("No");
		cell = row.createCell(colNum++);
		cell.setCellValue(s.str_startDate);
	}

	public XSSFCellStyle createCellStyle(XSSFSheet sheet) {
		XSSFCellStyle style = sheet.getWorkbook().createCellStyle();
		XSSFColor color = new XSSFColor(new Color(237, 125, 49));
		style.setFillBackgroundColor(color);
		style.setFillForegroundColor(color);
		style.setFillPattern(CellStyle.SOLID_FOREGROUND);
		return style;
	}

	public void writeTeaching(String path, Map<String, Staff> staff_Dic_byID) {

		File file = new File(path);
		if (file.exists()) {
			if (file.isFile()) {
				file.delete();
			}
		}

		Map<String, ArrayList<Staff>> OrgaDic = new HashMap<String, ArrayList<Staff>>();

		for (Staff s : staff_Dic_byID.values()) {
			String orga_name = s.organization;
			if (OrgaDic.get(orga_name) == null) {
				ArrayList<Staff> stafflist = new ArrayList<Staff>();
				OrgaDic.put(orga_name, stafflist);
				OrgaDic.get(orga_name).add(s);
			} else {
				for (int i = 0; i < OrgaDic.get(orga_name).size(); i++) {
					if (OrgaDic.get(orga_name).get(i).getTeachingInfo()[3] < s.getTeachingInfo()[3]) {
						OrgaDic.get(orga_name).add(i, s);
						break;
					}
					if (i == OrgaDic.get(orga_name).size() - 1) {
						OrgaDic.get(orga_name).add(s);
						break;
					}
				}
			}
		}

		for (String organame : OrgaDic.keySet()) {
			ArrayList<Staff> staffList = OrgaDic.get(organame);
			int orga_size = staffList.size();
			for (int i = 0; i < staffList.size(); i++) {
				Staff s = staffList.get(i);
				if (i <= orga_size * 0.05)
					s.TeaPerfLevel = "Above";
				else if (i <= orga_size * 0.5)
					s.TeaPerfLevel = "Middle";
				else if (i <= orga_size * 0.85)
					s.TeaPerfLevel = "Below";
				else
					s.TeaPerfLevel = "Low";
			}
		}

		XSSFWorkbook workbook = createNewWorkbook();
		XSSFSheet overall_sheet = addNewSheet(workbook, "Teaching Performance report");
		System.out.print("Begin creat the excel...");
		// create head
		addColumnTitles(overall_sheet, _titles2);
		// insert count numbers
		for (Staff s : staff_Dic_byID.values()) {
			// System.out.println(s.employNumber);
			if (s.excluded)
				continue;
			addStaffTeachingRecord(overall_sheet, s);
			String orga_name = s.organization.substring(s.organization.indexOf(" ") + 1).replace("/", "-");
			int poi = 0;
			for (int i = 0; i < orga_name.length(); i++) {
				char ch = orga_name.charAt(i);
				if (Character.isUpperCase(ch)) {
					poi = i;
					break;
				}
			}
			orga_name = orga_name.substring(poi);
			if (school_sheet_map.get(orga_name) == null) {
				XSSFSheet subSheet = addNewSheet(workbook, orga_name);
				school_sheet_map.put(orga_name, subSheet);
				addColumnTitles(subSheet, _titles2);
			}
			XSSFSheet sheet = school_sheet_map.get(orga_name);
			addStaffTeachingRecord(sheet, s);
		}

		try {
			FileOutputStream outputStream = new FileOutputStream(path);
			workbook.write(outputStream);
			workbook.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Done!");
	}

	public void addStaffTeachingRecord(XSSFSheet sheet, Staff s) {
		double[] data = s.getTeachingInfo();
		int rowIndex = sheet.getLastRowNum() + 1;
		Row row = sheet.createRow(rowIndex);
		int colNum = 0;
		Cell cell = row.createCell(colNum++);
		cell.setCellValue(s.employNumber);
		cell = row.createCell(colNum++);
		cell.setCellValue(s.str_fullName);
		cell = row.createCell(colNum++);
		cell.setCellValue(Math.round(data[0] * 100) / 100.0);
		cell = row.createCell(colNum++);
		cell.setCellValue(Math.round(data[1] * 100) / 100.0);
		cell = row.createCell(colNum++);
		cell.setCellValue(Math.round(data[2] * 100) / 100.0);
		cell = row.createCell(colNum++);
		cell.setCellValue(Math.round(data[3] * 100) / 100.0);
		cell = row.createCell(colNum++);
		cell.setCellValue(s.TeaPerfLevel);
	}
}
