package core;

import core.GlobalValues.PUBLICATION_TYPE;

public class Publication {
	public String ID;
	public String authorName;
	public String authorStaffNumber;
	public String nameOfAuthors;
	public int numberOfAuthors;
	public PUBLICATION_TYPE type;
	public String pubTime;
	public String str_citation;
	public double citation;
	public String jour_title;
	public boolean isJCR20;

	public Publication() {
		this.ID = "";
		this.authorName = "";
		this.authorStaffNumber = "";
		this.nameOfAuthors = "";
		this.numberOfAuthors = 1;
		this.type = null;
		this.pubTime = "";
		this.str_citation = "";
		this.citation = 0;
		this.jour_title = "";
	}
}
