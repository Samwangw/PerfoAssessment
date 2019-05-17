package core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import core.coreParameters;
import core.GlobalValues.CATEGORY;
import core.GlobalValues.LEVEL;
import core.GlobalValues.PUBLICATION_TYPE;
import helper.Helper;

public class Staff {
	public String str_fullName;
	public String fullName;
	public String firstName;
	public String middleName;
	public String lastName;
	public String anotherName;
	public String ID;
	public String employNumber;
	public String assNumber;
	public String email;
	public String status;
	public String position;
	public String organization;
	public String str_category;
	public CATEGORY category;
	public String str_level;
	public LEVEL level;
	public Map<String, ArrayList<Publication>> publications_by_years;
	public Map<String, double[]> incomes_by_years;
	public Map<String, double[]> students_by_years;
	public ArrayList<Course> courses;
	public double[] performance;
	public String str_researchonly;
	public String str_startDate;
	public Boolean research_only;
	public int startYear;
	public int startMonth;
	public Boolean excluded;
	public Boolean isNew;
	public double score;
	public String AcaPerfLevel;
	public String TeaPerfLevel;
	public String str_FTE;
	public double FTE;

	public Staff() {
		this.ID = "";
		this.fullName = "";
		this.firstName = "";
		this.lastName = "";
		this.anotherName = "";
		this.category = CATEGORY.NONE;
		this.level = LEVEL.NONE;
		this.research_only = false;
		this.excluded = false;
		this.str_startDate = "";
		this.isNew = false;
		this.publications_by_years = new HashMap<String, ArrayList<Publication>>();
		this.incomes_by_years = new HashMap<String, double[]>();
		this.students_by_years = new HashMap<String, double[]>();
		this.courses = new ArrayList<Course>();
		this.score = 0;
		this.AcaPerfLevel = "";
		this.TeaPerfLevel = "";
		this.str_FTE = "";
	}

	public String toString() {
		String str = "";
		str += this.employNumber + "\r";
		str += "\t" + this.fullName + "\r";
		str += "\t" + this.position + "\r";
		str += "\t" + this.category + "\r";
		str += "\t" + this.organization + "\r";
		str += "\t" + this.level + "\r";
		str += "\t" + this.research_only;
		return str;
	}

	public double[] getTeachingInfo() {
		if (this.courses.size() == 0) {
			return new double[] { 0, 0, 0, 0 };
		}
		double[] infos = new double[] { 0, 0, 0, 0 };
		ArrayList<String> course_id_list = new ArrayList<String>();
		for (Course c : this.courses) {
			String course_id = c.course_id + c.str_period;

			if (c.coordinator_id.equals(this.employNumber) && !course_id_list.contains(course_id)) {
				infos[2] += 1.0;
				course_id_list.add(course_id);
			}
			if (c.teachers_email_list != null) {
				for (String email : c.teachers_email_list) {
					if (email.equalsIgnoreCase(this.email))
						if (c.course_type.equals("Lecture"))
							infos[0] += 1.0 / (double) c.teachers_email_list.size();
						else
							infos[1] += 1.0 / (double) c.teachers_email_list.size();

				}
			}
		}
		return new double[] { infos[0], infos[1], infos[2], (infos[0] + infos[1] + infos[2]) };

	}

	public void standarise() {
		String[] names = Helper.parseStaffName(this.str_fullName);
		this.firstName = names[0];
		this.lastName = names[1];
		this.anotherName = names[2];
		this.fullName = this.getStaffFullName();
		this.employNumber = Helper.formatEmployNumber(this.employNumber);
		this.email = this.email.trim().toLowerCase();
		this.category = Helper.convertString2Category(this.organization);
		this.research_only = Helper.convertString2Bool(this.str_researchonly);
		this.FTE = Double.parseDouble(this.str_FTE);
		int startYear = Integer.parseInt(this.str_startDate.split("-")[2]);
		int startMonth = Helper.convertMonth2int(this.str_startDate.split("-")[1]);
		this.startYear = startYear;
		this.startMonth = startMonth;
		for (String year : coreParameters.years) {
			if (Integer.parseInt(year) == this.startYear) {
				this.isNew = true;
			}
		}
		this.level = Helper.convertString2Level(this.str_level);
		// exclude staff
		// exclude();
		saveInfo();
	}

	public void exclude() {
		if (startYear >= Integer.parseInt(coreParameters.years[coreParameters.years.length - 1]) && startMonth >= 3)
			this.excluded = true;
		if (this.position.contains("Industry"))
			this.excluded = true;
		if (this.level == LEVEL.A)
			this.excluded = true;
	}

	public void saveInfo() {
		if (coreParameters.email_staff_dic.get(this.email) == null)
			coreParameters.email_staff_dic.put(this.email, this.employNumber);
	}

	public void adjustScore() {
		if (this.research_only) {
			if (this.position.contains("Chancellor's Postdoctoral"))
				this.score /= 1.2;
			else
				this.score /= 1.8;
		}
		if (this.isNew) {
			int working_months = 0;
			if (this.startYear == Integer.parseInt(coreParameters.years[0]))
				working_months = 24 - this.startMonth;
			else
				working_months = 12 - this.startMonth;
			this.score = this.score / (double) (working_months) * 12.0;
		}
		if (this.str_level.equals("SSG"))
			this.score *= 2;
		this.score /= this.FTE;
	}

	/**
	 * to get an performance data array for the specific year
	 * 
	 * @param year
	 * @return x1-x9 of the year
	 */
	public double[] getPerformanceData(String year) {
		double[] data = new double[] { 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		double[] pub_by_types = new double[] { 0, 0, 0, 0 };
		if (this.incomes_by_years.get(year) != null) {
			data[0] += this.incomes_by_years.get(year)[0];// x1
			data[1] += this.incomes_by_years.get(year)[1];// x2
			data[2] += this.incomes_by_years.get(year)[2];// x3
		}
		double temp4 = 0;
		double temp6 = 0;
		if (this.publications_by_years.get(year) != null) {
			ArrayList<Publication> pubs = this.publications_by_years.get(year);
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
		}

		if (this.students_by_years.get(year) != null) {
			data[7] += this.students_by_years.get(year)[0];// primary
			data[8] += this.students_by_years.get(year)[1];// co
		}

		if (data[6] > 150)
			data[6] = 150;
		return data;
	}

	public int publicatinCount() {
		int count = 0;
		for (String year : this.publications_by_years.keySet())
			count += this.publications_by_years.get(year).size();
		return count;
	}

	public int publicatinCount(String year) {
		if (this.publications_by_years.get(year) != null)
			return this.publications_by_years.get(year).size();
		else
			return 0;
	}

	public void calculateScoreA(double[] data) {
		double score = 0;
		try {
			if (data.length != 9 || this.category == CATEGORY.NONE || this.level == LEVEL.NONE) {
				this.score = -1;
			}
			double temp1 = 0;
			for (int i = 0; i < 3; i++) {
				temp1 += GlobalValues.BENCHMARK_WEIGHT.get(this.category).get(this.level)[i] * data[i]
						/ GlobalValues.BENCHMARK_VALUE.get(this.category).get(this.level)[i];
			}
			double temp2 = 0;
			for (int i = 3; i < 7; i++) {
				temp2 += GlobalValues.BENCHMARK_WEIGHT.get(this.category).get(this.level)[i] * data[i]
						/ GlobalValues.BENCHMARK_VALUE.get(this.category).get(this.level)[i];
			}
			double temp3 = 0;
			for (int i = 7; i < 9; i++) {
				temp3 += GlobalValues.BENCHMARK_WEIGHT.get(this.category).get(this.level)[i] * data[i]
						/ GlobalValues.BENCHMARK_VALUE.get(this.category).get(this.level)[i];
			}
			if (temp1 > 0.9)
				temp1 = 0.9;
			if (temp2 > 0.9)
				temp2 = 0.9;
			if (temp3 > 0.9)
				temp3 = 0.9;
			score = temp1 + temp2 + temp3;
			this.score = score * 100;
			this.adjustScore();
		} catch (Exception e) {
			e.printStackTrace();
			this.score = 0;
		}
	}

	public void merge(Staff source) {

	}

	public double getIncomeX1(String year) {
		if (this.incomes_by_years.get(year) != null)
			return this.incomes_by_years.get(year)[0];
		else
			return 0;
	}

	public double getIncomeX2(String year) {
		if (this.incomes_by_years.get(year) != null)
			return this.incomes_by_years.get(year)[1];
		else
			return 0;
	}

	public double getIncomeX3(String year) {
		if (this.incomes_by_years.get(year) != null)
			return this.incomes_by_years.get(year)[2];
		else
			return 0;
	}

	public double getJournalCount(String year) {
		if (this.publications_by_years.get(year) != null) {
			int count = 0;
			for (Publication pub : this.publications_by_years.get(year)) {
				if (pub.type == PUBLICATION_TYPE.JOURNAL)
					count++;
			}
			return count;
		} else
			return 0;
	}

	public double getChapterCount(String year) {
		if (this.publications_by_years.get(year) != null) {
			int count = 0;
			for (Publication pub : this.publications_by_years.get(year)) {
				if (pub.type == PUBLICATION_TYPE.CHAPTER)
					count++;
			}
			return count;
		} else
			return 0;
	}

	public double getConferenceCount(String year) {
		if (this.publications_by_years.get(year) != null) {
			int count = 0;
			for (Publication pub : this.publications_by_years.get(year)) {
				if (pub.type == PUBLICATION_TYPE.CONFERENCE)
					count++;
			}
			return count;
		} else
			return 0;
	}

	public double getBookCount(String year) {
		if (this.publications_by_years.get(year) != null) {
			int count = 0;
			for (Publication pub : this.publications_by_years.get(year)) {
				if (pub.type == PUBLICATION_TYPE.BOOK)
					count++;
			}
			return count;
		} else
			return 0;
	}

	public double getPrimaryStudent(String year) {
		if (this.students_by_years.get(year) != null)
			return this.students_by_years.get(year)[0];
		else
			return 0;
	}

	public double getCoStudent(String year) {
		if (this.students_by_years.get(year) != null)
			return this.students_by_years.get(year)[1];
		else
			return 0;
	}

	private String getStaffFullName() {
		String fullName = this.firstName + ", " + this.lastName;
		if (this.anotherName != "")
			fullName += " (" + this.anotherName + ")";
		return fullName;

	}
}
