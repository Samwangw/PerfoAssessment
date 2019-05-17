package core;

import java.util.ArrayList;
import java.util.HashMap;

import helper.Helper;

public class Course {
	public String course_id;
	public String course_name;
	public String str_year;
	public String str_halfyear;
	public String str_Coordinator_id;
	public String str_Coordinator_email;
	public String str_classid;
	public String str_Teacher_email_list;
	public String str_period;
	public String str_course_type;
	public String coordinator_id;
	public String course_type;
	public HashMap<String, ArrayList<String>> classes;
	public ArrayList<String> teachers_email_list;

	public String toString() {
		String str = "";
		str += this.course_id + "\r";
		str += "\t" + this.course_name + "\r";
		str += "\t" + this.coordinator_id + "\r";
		str += "\t" + this.course_type + "\r";
		str += "\t" + this.str_period + "\r";
		if (this.teachers_email_list != null)
			for (String email : this.teachers_email_list)
				str += "\t" + email + "\r";
		return str;
	}

	public void standarise() {

		this.coordinator_id = Helper.formatEmployNumber(this.str_Coordinator_id);
		this.str_course_type = this.str_course_type.trim().toLowerCase();
		this.str_Coordinator_email = this.str_Coordinator_email.trim().toLowerCase();
		if (this.str_course_type.startsWith("lec"))
			this.course_type = "Lecture";
		else
			this.course_type = "non-Lecture";
		// Teacher list
		this.classes = new HashMap<String, ArrayList<String>>();
		this.teachers_email_list = new ArrayList<String>();
		if (!this.str_Teacher_email_list.equalsIgnoreCase("NA")) {
			String[] emails = this.str_Teacher_email_list.toLowerCase().split(",");
			for (String e : emails)
				this.teachers_email_list.add(e);
		} else
			this.teachers_email_list.add(this.str_Coordinator_email);
		//this.classes.put(this.str_course_type + this.str_classid, this.teachers_email_list);
	}

}
