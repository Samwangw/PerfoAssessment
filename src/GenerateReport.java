import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

import accessing.*;
import core.*;
import core.GlobalValues.CATEGORY;
import core.GlobalValues.LEVEL;
import core.GlobalValues.PUBLICATION_TYPE;
import helper.Helper;

public class GenerateReport {

	public static Map<String, Staff> staff_byID = new HashMap<String, Staff>();
	public static Map<String, Staff> staff_byName = new HashMap<String, Staff>();
	public static Map<String, Organization> organizations = new HashMap<String, Organization>();
	public static Map<String, JournalPaper> journal_byID = new HashMap<String, JournalPaper>();
	public static Map<String, JournalPaper> journal_byName = new HashMap<String, JournalPaper>();
	public static String data_path;
	public static boolean DEBUG = false;

	public static void main(String[] args) {
		try {
			init();
			getStaffInfo();
			academicAssess();
//			teachingAssess();
//			for (Staff s : staff_byID.values()) {
//				if (s.employNumber.equalsIgnoreCase("101395")) {
//					for (Course c : s.courses)
//						System.out.println(c);
//
//				}
//			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void init() {
		String config_path = "config.xml";
		try {
			File config = new File(config_path);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(config);
			System.out.println("Loading config...");
			String len = doc.getElementsByTagName("data_root").item(0).getTextContent();
			System.out.println(len);
			System.out.println("......end");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void academicAssess() {
		parseJCR20();
		getResearchPerformance();
		for (Staff s : staff_byID.values()) {
			s.setFinalAcaScore(s.getAverAcaPerform());
		}
		for (Organization org : organizations.values()) {
			org.setAcaLevel();
			if (DEBUG)
				org.displayStaff();
		}
		writePerformance();
	}

	public static void teachingAssess() {
		getTeachingPerformance();
		writeTeachingPerformance();
	}

	/***
	 * Description: get staff information by filtering out those are not
	 * necessary to assess<br>
	 */
	public static void getStaffInfo() {
		System.out.println("Load staff information start......");
		// get staff information from general staff list
		parseStaff();
		System.out.println("......end");
	}

	public static void parseStaff() {
		ParseExl parser = new ParseExl();
		// Setup the connection between column title and class field
		Map<String, String> map = new HashMap<String, String>();
		map.put("Employee Number".toLowerCase(), "employNumber");
		map.put("Full Name".toLowerCase(), "str_fullName");
		map.put("Status".toLowerCase(), "status");
		map.put("Position Name".toLowerCase(), "position");
		map.put("Organisation".toLowerCase(), "organization");
		map.put("Function".toLowerCase(), "str_researchonly");
		map.put("Grade".toLowerCase(), "str_level");
		map.put("Start Date".toLowerCase(), "str_startDate");
		map.put("FTE".toLowerCase(), "str_FTE");
		map.put("Email Address".toLowerCase(), "email");
		int title_row_index = 1;
		int current_row_index = title_row_index;
		try {
			// get instance list of target class
			ArrayList list = parser.parse(Class.forName("core.Staff"), "Sources\\Staff_information\\staff.xlsx", 0,
					title_row_index, map);
			// process the return list
			Iterator<Staff> iterator = list.iterator();
			while (iterator.hasNext()) {
				try {
					// get the row
					Staff s = iterator.next();
					current_row_index++;
					s.standarise();
					if (s.excluded)
						continue;
					if (!organizations.containsKey(s.organization)) {
						organizations.put(s.organization, new Organization(s.organization));
						organizations.get(s.organization).addStaff(s);
					} else {
						organizations.get(s.organization).addStaff(s);
					}
					if (staff_byID.get(s.employNumber) == null) {
						staff_byID.put(s.employNumber, s);
						if (staff_byName.get(s.fullName) == null)
							staff_byName.put(s.fullName, s);
						else {
							Helper.mode_print(
									"  Row " + current_row_index + " found duplicate staff name: " + s.fullName);
							processDuplicateStaff(staff_byName.get(s.fullName), s);
						}
					} else {
						Helper.mode_print(
								"  Row " + current_row_index + " found duplicate staff id: " + s.employNumber);
						processDuplicateStaff(staff_byID.get(s.employNumber), s);
					}
				} catch (Exception e) {
					System.out.println("  Row " + current_row_index + " :" + e.getMessage());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void processDuplicateStaff(Staff olds, Staff news) {
	}

	public static void parseJCR20() {
		System.out.println("Load JCR information start......");
		File des = new File("Sources\\JCR_journal_infomation");
		if (des.exists()) {
			if (des.isDirectory()) {
				System.out.println("Found JCR dictionary.");
				File[] files = des.listFiles();
				for (File file : files) {
					List<JournalPaper> jlist = new ArrayList<JournalPaper>();
					try {
						System.out.println("Processing file..." + file.getName());
						BufferedReader reader = new BufferedReader(new FileReader(file));
						reader.readLine();
						reader.readLine();
						String line = null;
						while ((line = reader.readLine()) != null) {
							String item[] = line.split(",");
							if (item.length < 2)
								continue;
							JournalPaper j = new JournalPaper();
							String journalTitle = item[1].toLowerCase();
							journalTitle.replace(" and ", " & ");
							j._Title = journalTitle;
							jlist.add(j);
						}
						reader.close();
					} catch (Exception e) {
					}
					double th = jlist.size() * 0.2;
					Iterator<JournalPaper> iterator = jlist.iterator();
					int index_max = 1;
					while (iterator.hasNext() && index_max <= th) {
						JournalPaper j = iterator.next();
						journal_byName.put(j._Title, j);
						index_max++;
					}
				}
				System.out.println("......end");
			} else {
				System.out.println("Not found JCR dictionary.");
			}
		}
	}

	public static void getResearchPerformance() {
		parseIncome("2017", "Sources\\Performance\\2017\\income2.xlsx", 0, 1, false);
		parseIncome("2018", "Sources\\Performance\\2018\\income2.xlsx", 0, 1, false);
		parsePublication("2017", PUBLICATION_TYPE.CONFERENCE, "Sources\\Performance\\2017\\Conferences.xlsx");
		parsePublication("2017", PUBLICATION_TYPE.JOURNAL, "Sources\\Performance\\2017\\Journal.xlsx");
		parsePublication("2017", PUBLICATION_TYPE.BOOK, "Sources\\Performance\\2017\\Books.xlsx");
		parsePublication("2017", PUBLICATION_TYPE.CHAPTER, "Sources\\Performance\\2017\\Chapters.xlsx");
		parsePublication("2018", PUBLICATION_TYPE.CONFERENCE, "Sources\\Performance\\2018\\Conferences.xlsx");
		parsePublication("2018", PUBLICATION_TYPE.JOURNAL, "Sources\\Performance\\2018\\Journal.xlsx");
		parsePublication("2018", PUBLICATION_TYPE.BOOK, "Sources\\Performance\\2018\\Books.xlsx");
		parsePublication("2018", PUBLICATION_TYPE.CHAPTER, "Sources\\Performance\\2018\\Chapters.xlsx");
		parseSupervision("2017", "Sources\\Performance\\2017\\Benchmarks.xlsx", 4, 0, false);
		parseSupervision("2018", "Sources\\Performance\\2018\\supervisor.xlsx", 2, 0, false);
	}

	public static void getTeachingPerformance() {
		System.out.println("Load teaching data start ......");
		ParseTeachingInfo("Sources\\Performance\\2018\\teaching.xlsx", 0, 0, false);
		System.out.println("......end");
	}

	public static void parseSupervision(String year, String file, int sheet_index, int title_index,
			Boolean addNewStaff) {
		System.out.println("Load supervision information start......");
		if (!Helper.checkYear(year)) {
			System.out.println("Specific year " + year + " is not available in core parameter");
			System.exit(1);
		}
		ParseExl parser = new ParseExl();
		// Setup the connection between column title and class field
		Map<String, String> map = new HashMap<String, String>();
		map.put("id".toLowerCase(), "staffNumber");
		map.put("principal".toLowerCase(), "primary");
		map.put("Co".toLowerCase(), "co");
		int current_row_index = title_index;
		try {
			// get instance list of target class
			ArrayList list = parser.parse(Class.forName("core.Student"), file, sheet_index, title_index, map);
			// process the return list
			Iterator<Student> iterator = list.iterator();
			int x = 0;
			while (iterator.hasNext()) {
				// update the row index
				current_row_index++;
				// get the row
				Student student = iterator.next();
				String emNumber = Helper.formatEmployNumber(student.staffNumber);
				if (staff_byID.get(emNumber) != null) {
					Staff s = staff_byID.get(emNumber);
					x++;
					// System.out.println(x+" hit student");
					if (s.students_by_years.get(year) == null) {
						double[] student_value = new double[2];
						student_value[0] = Helper.convertString2Double(student.primary);
						student_value[1] = Helper.convertString2Double(student.co);
						s.students_by_years.put(year, student_value);
					} else
						Helper.mode_print(current_row_index + " get duplicate " + emNumber);

				} else if (addNewStaff) {
					Helper.mode_print(current_row_index + " get new staff " + emNumber);
				}
			}
		} catch (Exception e) {
			System.out.println("Row " + current_row_index + " :");
			e.printStackTrace();
		}
		System.out.println("......end");
	}

	public static void parseIncome(String year, String file, int sheet_index, int title_index, Boolean addNewStaff) {
		System.out.println("Load income information start......");
		if (!Helper.checkYear(year)) {
			System.out.println("Specific year " + year + " is not available in core parameter");
			System.exit(1);
		}
		ParseExl parser = new ParseExl();
		// Setup the connection between column title and class field
		Map<String, String> map = new HashMap<String, String>();
		map.put("id".toLowerCase(), "staffNumber");
		map.put("Staff Name".toLowerCase(), "staffName");
		map.put("x1".toLowerCase(), "x1");
		map.put("x2".toLowerCase(), "x2");
		map.put("x3".toLowerCase(), "x3");
		int current_row_index = title_index;
		try {
			// get instance list of target class
			ArrayList list = parser.parse(Class.forName("core.Income"), file, sheet_index, title_index, map);
			// process the return list
			Iterator<Income> iterator = list.iterator();
			while (iterator.hasNext()) {
				// update the row index
				current_row_index++;
				// get the row
				Income income = iterator.next();
				String emNumber = Helper.formatEmployNumber(income.staffNumber);
				if (staff_byID.get(emNumber) != null) {
					Staff s = staff_byID.get(emNumber);
					if (s.incomes_by_years.get(year) == null) {
						double[] income_value = new double[3];
						income_value[0] = Helper.convertString2Double(income.x1) / 1000.0;
						income_value[1] = Helper.convertString2Double(income.x2) / 1000.0;
						income_value[2] = Helper.convertString2Double(income.x3) / 1000.0;
						s.incomes_by_years.put(year, income_value);
					} else
						System.out.println(current_row_index + " get duplicate income " + emNumber);
				} else if (addNewStaff) {
					System.out.println(current_row_index + " get new staff " + emNumber);
				}
			}
		} catch (Exception e) {
			System.out.println("Row " + current_row_index + " :" + e.getMessage());
			// e.printStackTrace();
		}
		System.out.println("......end");
	}

	/**
	 * 
	 * @param year Publication year
	 * @param type Publication type
	 * @param file Publication file
	 */
	public static void parsePublication(String year, PUBLICATION_TYPE type, String file) {
		System.out.println("Load publication information start......");
		if (!Helper.checkYear(year)) {
			System.out.println("Specific year " + year + " is not available in core parameter");
			System.exit(1);
		}
		ParseExl parser = new ParseExl();
		// Setup the connection between column title and class field
		Map<String, String> map = new HashMap<String, String>();
		map.put("username".toLowerCase(), "authorStaffNumber");
		map.put("name".toLowerCase(), "authorName");
		map.put("Authors".toLowerCase(), "nameOfAuthors");
		map.put("Times cited (Scopus)".toLowerCase(), "str_citation");
		map.put("Canonical journal title".toLowerCase(), "jour_title");
		int title_row_index = 0;
		int current_row_index = title_row_index;
		try {
			// get instance list of target class
			ArrayList list = parser.parse(Class.forName("core.Publication"), file, 0, 0, map);
			Iterator<Publication> iterator = list.iterator();
			// process the return list
			while (iterator.hasNext()) {
				current_row_index++;
				Publication obj = iterator.next();
				String emNumber = Helper.formatEmployNumber(obj.authorStaffNumber);
				obj.authorName = obj.authorName.toLowerCase();
				if (staff_byID.get(emNumber) != null) {
					obj.pubTime = year;
					obj.type = type;
					obj.citation = Helper.convertString2Double(obj.str_citation);
					obj.jour_title = Helper.formatJournalTitle(obj.jour_title);
					if (journal_byName.get(obj.jour_title) != null)
						obj.isJCR20 = true;
					if (obj.nameOfAuthors != null && obj.nameOfAuthors.length() > 0)
						obj.numberOfAuthors = obj.nameOfAuthors.split(",").length;
					if (staff_byID.get(emNumber).publications_by_years.get(year) == null) {
						staff_byID.get(emNumber).publications_by_years.put(year, new ArrayList<Publication>());
						staff_byID.get(emNumber).publications_by_years.get(year).add(obj);
					} else {
						staff_byID.get(emNumber).publications_by_years.get(year).add(obj);
					}
				} else {
					// System.out.println(current_row_index + " get unclaimed
					// pub " + emNumber);
				}
			}
		} catch (Exception e) {
			System.out.println("Row " + current_row_index + " :");
			e.printStackTrace();
		}
		System.out.println("......end");
	}

	public static void writePerformance() {
		WriteExl writer = new WriteExl();
		String path = "Sources\\Report\\result " + new Date().toGMTString().replace(':', '-') + ".xlsx";
		writer.write(path, staff_byID, staff_byName);
	}

	public static void writeTeachingPerformance() {
		WriteExl writer = new WriteExl();
		String path = "Sources\\Report\\Teaching " + new Date().toGMTString().replace(':', '-') + ".xlsx";
		writer.writeTeaching(path, staff_byID);
	}

	public static void ParseTeachingInfo(String file, int sheet_index, int title_index, Boolean addNewStaff) {
		ParseExl parser = new ParseExl();
		// Setup the connection between column title and class field
		Map<String, String> map = new HashMap<String, String>();
		map.put("Subject".toLowerCase(), "course_id");
		map.put("Subject Name".toLowerCase(), "course_name");
		map.put("year".toLowerCase(), "str_year");
		map.put("Halfyear".toLowerCase(), "str_halfyear");
		map.put("Coordinator Id".toLowerCase(), "str_Coordinator_id");
		map.put("Coordinator Email".toLowerCase(), "str_Coordinator_email");
		map.put("Teacher Emails".toLowerCase(), "str_Teacher_email_list");
		map.put("Class 1".toLowerCase(), "str_course_type");
		map.put("Class 2".toLowerCase(), "str_classid");
		map.put("Period".toLowerCase(), "str_period");
		int current_row_index = title_index;
		try {
			// get instance list of target class
			ArrayList list = parser.parse(Class.forName("core.Course"), file, sheet_index, title_index, map);
			// process the return list
			Iterator<Course> iterator = list.iterator();
			ArrayList<String> course_id_list = new ArrayList<String>();
			ArrayList<Course> course_list = new ArrayList<Course>();
			int x = 0;
			while (iterator.hasNext()) {
				// update the row index
				current_row_index++;
				// get the row
				Course course = iterator.next();
				course.standarise();

				String unique_course_id = "";
				unique_course_id = course.course_id + course.str_course_type + course.str_classid + course.str_period;
				if (course_id_list.contains(unique_course_id))
					System.out.println("found duplicate course at row " + current_row_index);
				else {
					course_id_list.add(unique_course_id);
					course_list.add(course);
				}
			}

			for (Course c : course_list) {
				if (staff_byID.get(c.coordinator_id) != null)
					staff_byID.get(c.coordinator_id).courses.add(c);
				else {
					Helper.mode_print("Staff " + c.coordinator_id + " not found.");
				}
				for (String email : c.teachers_email_list) {
					if (coreParameters.email_staff_dic.get(email) == null) {
						// System.out.println(email + " not found.");
					} else {
						String teacherEmNumber = coreParameters.email_staff_dic.get(email);
						if (staff_byID.get(teacherEmNumber) != null
								&& !teacherEmNumber.equalsIgnoreCase(c.coordinator_id)) {
							staff_byID.get(teacherEmNumber).courses.add(c);
							// Helper.mode_print("add course " + c.course_id + "
							// to " + teacherEmNumber);
						}
					}
				}
			}

		} catch (Exception e) {
			System.out.println("Row " + current_row_index + " :");
			e.printStackTrace();
		}
	}

}
