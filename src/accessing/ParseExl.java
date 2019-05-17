package accessing;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

//import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ParseExl {
	public <targetClass> ArrayList<targetClass> parse(Class<targetClass> c, String file, int sheetIndex,
			int titleRowIndex, Map<String, String> columnTitle2field)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException {

		final ArrayList<targetClass> targetList = new ArrayList<targetClass>();

		FileInputStream file_reader = null;
		int current_row_index = titleRowIndex;
		try {
			file_reader = new FileInputStream(new File(file));
			System.out.println("Processing..." + file);
			Workbook workbook = new XSSFWorkbook(file_reader);
			Sheet targetSheet = workbook.getSheetAt(sheetIndex);
			Iterator<Row> iterator = targetSheet.iterator();
			Map<String, Integer> field2columnIndex = new HashMap<String, Integer>();
			while (iterator.hasNext()) {
				Row currentRow = iterator.next();
				current_row_index++;
				if (currentRow.getRowNum() < titleRowIndex)
					continue;
				if (currentRow.getRowNum() == titleRowIndex) {
					for (int i = 0; i < currentRow.getLastCellNum(); i++) {
						String column_title = currentRow.getCell(i).toString().trim().toLowerCase();
						if (columnTitle2field.get(column_title) != null) {
							field2columnIndex.put(columnTitle2field.get(column_title), i);
						}
					}
					for (String column_title : columnTitle2field.keySet()) {
						if (field2columnIndex.get(columnTitle2field.get(column_title)) == null)
							System.out.println(c.getName() + "." + columnTitle2field.get(column_title) + " not found.");
					}
					continue;
				}
				targetClass o = c.newInstance();
				for (String field_name : field2columnIndex.keySet()) {
					Field f = null;
					f = c.getField(field_name);
					if (currentRow.getCell(field2columnIndex.get(field_name)) != null) {
						Cell cell = currentRow.getCell(field2columnIndex.get(field_name));
						switch (cell.getCellType()) {
						case XSSFCell.CELL_TYPE_FORMULA:
							f.set(o, String.valueOf(cell.getNumericCellValue()));
							break;
						default:
							f.set(o, currentRow.getCell(field2columnIndex.get(field_name)).toString().trim());
						}
					}
				}
				targetList.add(o);
			}
			workbook.close();
			file_reader.close();
		} catch (Exception e) {
			System.out.println("Row " + current_row_index + " :");
			e.printStackTrace();
		}
		return targetList;
	}
}
