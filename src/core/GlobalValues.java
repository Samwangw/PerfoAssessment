package core;

import java.util.HashMap;
import java.util.Map;

public class GlobalValues {
	public static enum CATEGORY {
		IT, ENGINEERING, NONE;
		@Override
		public String toString() {
			switch (this) {
			case IT:
				return "IT";
			case ENGINEERING:
				return "ENGINEERING";
			case NONE:
				return "not known";
			default:
				throw new IllegalArgumentException();
			}
		}
	}

	public static enum PUBLICATION_TYPE {
		JOURNAL, CONFERENCE, BOOK, CHAPTER;
		public String toString() {
			switch (this) {
			case JOURNAL:
				return "Journal";
			case CONFERENCE:
				return "Conference";
			case BOOK:
				return "Book";
			case CHAPTER:
				return "Chapter";
			default:
				throw new IllegalArgumentException();
			}
		}
	}

	public static enum LEVEL {
		A, B, C, D, E, NONE;
		@Override
		public String toString() {
			switch (this) {
			case A:
				return "A";
			case B:
				return "B";
			case C:
				return "C";
			case D:
				return "D";
			case E:
				return "E";
			case NONE:
				return "not known";
			default:
				throw new IllegalArgumentException();
			}
		}
	}

	public static enum ERA {
		AA, A, B, C, NONE;
		@Override
		public String toString() {
			switch (this) {
			case AA:
				return "A*";
			case A:
				return "A";
			case B:
				return "B";
			case C:
				return "C";
			case NONE:
				return "";
			default:
				throw new IllegalArgumentException();
			}
		}
	}

	private static Map<LEVEL, Double[]> BENCHMARK_VALUE_OF_IT_LEVEL = new HashMap<LEVEL, Double[]>();
	private static Map<LEVEL, Double[]> BENCHMARK_VALUE_OF_EG_LEVEL = new HashMap<LEVEL, Double[]>();
	private static Map<LEVEL, Double[]> BENCHMARK_WEIGHT_OF_LEVEL = new HashMap<LEVEL, Double[]>();
	public final static Map<CATEGORY, Map<LEVEL, Double[]>> BENCHMARK_VALUE = new HashMap<CATEGORY, Map<LEVEL, Double[]>>();
	public final static Map<CATEGORY, Map<LEVEL, Double[]>> BENCHMARK_WEIGHT = new HashMap<CATEGORY, Map<LEVEL, Double[]>>();
	static {
		//income1, income2, income3, pubs, weights, JCR, cites, prin, co
		// The value is assigned from x1 to x9 then y1 to y3
		BENCHMARK_VALUE_OF_IT_LEVEL.put(LEVEL.A,
				new Double[] { 30.0, 15.0, 10.0, 4.0, 2.0, 1.0, 2.0, 0.2, 0.2, 1.0, 1.0, 1.0 });
		BENCHMARK_VALUE_OF_IT_LEVEL.put(LEVEL.B, BENCHMARK_VALUE_OF_IT_LEVEL.get(LEVEL.A));
		BENCHMARK_VALUE_OF_IT_LEVEL.put(LEVEL.C,
				new Double[] { 60.0, 30.0, 10.0, 5.0, 2.0, 1.0, 3.0, 0.4, 0.4, 2.0, 2.0, 2.0 });
		BENCHMARK_VALUE_OF_IT_LEVEL.put(LEVEL.D,
				new Double[] { 80.0, 40.0, 30.0, 6.0, 2.0, 2.0, 4.0, 0.8, 0.8, 3.0, 3.0, 3.0 });
		BENCHMARK_VALUE_OF_IT_LEVEL.put(LEVEL.E,
				new Double[] { 100.0, 50.0, 40.0, 9.0, 3.0, 2.0, 5.0, 1.2, 1.2, 4.0, 4.0, 4.0 });
		BENCHMARK_VALUE.put(CATEGORY.IT, BENCHMARK_VALUE_OF_IT_LEVEL);
		BENCHMARK_VALUE_OF_EG_LEVEL.put(LEVEL.A,
				new Double[] { 40.0, 20.0, 10.0, 4.0, 2.0, 1.0, 2.0, 0.2, 0.2, 1.0, 1.0, 1.0 });
		BENCHMARK_VALUE_OF_EG_LEVEL.put(LEVEL.B, BENCHMARK_VALUE_OF_EG_LEVEL.get(LEVEL.A));
		BENCHMARK_VALUE_OF_EG_LEVEL.put(LEVEL.C,
				new Double[] { 90.0, 45.0, 10.0, 5.0, 2.0, 1.0, 3.0, 0.4, 0.4, 2.0, 2.0, 2.0 });
		BENCHMARK_VALUE_OF_EG_LEVEL.put(LEVEL.D,
				new Double[] { 120.0, 60.0, 30.0, 6.0, 2.0, 2.0, 4.0, 0.8, 0.8, 3.0, 3.0, 3.0 });
		BENCHMARK_VALUE_OF_EG_LEVEL.put(LEVEL.E,
				new Double[] { 140.0, 70.0, 40.0, 9.0, 3.0, 2.0, 5.0, 1.2, 1.2, 4.0, 4.0, 4.0 });
		BENCHMARK_VALUE.put(CATEGORY.ENGINEERING, BENCHMARK_VALUE_OF_EG_LEVEL);
		BENCHMARK_WEIGHT_OF_LEVEL.put(LEVEL.A,
				new Double[] { 0.1, 0.05, 0.05, 0.18, 0.15, 0.10, 0.07, 0.15, 0.15, 0.35, 0.35, 0.30 });
		BENCHMARK_WEIGHT_OF_LEVEL.put(LEVEL.B, BENCHMARK_WEIGHT_OF_LEVEL.get(LEVEL.A));
		BENCHMARK_WEIGHT_OF_LEVEL.put(LEVEL.C, BENCHMARK_WEIGHT_OF_LEVEL.get(LEVEL.A));
		BENCHMARK_WEIGHT_OF_LEVEL.put(LEVEL.D,
				new Double[] { 0.1, 0.1, 0.1, 0.12, 0.1, 0.1, 0.08, 0.2, 0.1, 0.35, 0.35, 0.30 });
		BENCHMARK_WEIGHT_OF_LEVEL.put(LEVEL.E, BENCHMARK_WEIGHT_OF_LEVEL.get(LEVEL.D));
		BENCHMARK_WEIGHT.put(CATEGORY.IT, BENCHMARK_WEIGHT_OF_LEVEL);
		BENCHMARK_WEIGHT.put(CATEGORY.ENGINEERING, BENCHMARK_WEIGHT_OF_LEVEL);
	}
}
