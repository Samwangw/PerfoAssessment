package core;

import core.GlobalValues.ERA;

public class JournalPaper {
	public String _Title;
	public String _ID;
	public double _ImpactFator;
	public double _EigenfactorScore;
	public String _Authors;
	public String _JournalTitle;
	public int _VolumeNumber;
	public int _IssueNumber;
	public int _StartPage;
	public int _EndPage;
	public ERA _ERA;
	public boolean _InTop20JCR;
	public double _SNIP_rank;
	public double _SJR_rank;

	public JournalPaper() {
		this._ID = "";
		this._Title = "";
		this._JournalTitle = "";
		this._VolumeNumber = -1;
		this._IssueNumber = -1;
		this._StartPage = -1;
		this._EndPage = -1;
		this._ERA = ERA.NONE;
		this._InTop20JCR = false;
		this._SNIP_rank = 0;
		this._SJR_rank = 0;
	}
}
