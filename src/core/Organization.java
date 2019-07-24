package core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class Organization {
	public String name;
	private ArrayList<Staff> staff_list;

	public Organization(String name) {
		this.name = name;
		this.staff_list = new ArrayList<Staff>();
	}

	public void addStaff(Staff s) {
		this.staff_list.add(s);
	}

	public int staff_count() {
		return this.staff_list.size();
	}

	public ArrayList<Staff> getStaffList() {
		return this.staff_list;
	}

	private void sortStaffbyScore() {
		Collections.sort(this.staff_list, new Comparator<Staff>() {
			public int compare(Staff s1, Staff s2) {
				return Double.compare(s2.score, s1.score);
			}
		});
	}

	public void setAcaLevel() {
		int orga_size = this.staff_count();
		this.sortStaffbyScore();
		for (int i = 0; i < orga_size; i++) {
			Staff s = this.staff_list.get(i);
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

	public void displayStaff() {
		System.out.println(this.name);
		for (Staff s : this.staff_list) {
			System.out.println(s.employNumber + ":" + s.score+" "+s.AcaPerfLevel);
		}
	}
}
