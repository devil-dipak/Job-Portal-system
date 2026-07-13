package com.devSoft.Utils;

import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.servlet.View;

import com.devSoft.Model.Department;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class DepartmentExcelView implements View {

	@Override
	public String getContentType() {
		return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
	}

	@Override
	public void render(Map<String, ?> model, HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		response.addHeader("Content-Disposition", "attachment;filename=department.xlsx");
		response.setContentType(getContentType());

		@SuppressWarnings("unchecked")
		List<Department> list = (List<Department>) model.get("dList");

		try (Workbook workbook = new XSSFWorkbook()) {
			Sheet sheet = workbook.createSheet("DEPARTMENT");
			setHead(sheet);
			setBody(sheet, list);
			workbook.write(response.getOutputStream());
		}
	}

	private void setHead(Sheet sheet) {
		Row row = sheet.createRow(0);
		row.createCell(0).setCellValue("ID");
		row.createCell(1).setCellValue("DEPARTMENT NAME");
		row.createCell(2).setCellValue("DEPARTMENT HOD");
		row.createCell(3).setCellValue("DEPARTMENT PHONE");
	}

	private void setBody(Sheet sheet, List<Department> list) {
		int rowNum = 1;
		for (Department spec : list) {
			Row row = sheet.createRow(rowNum++);
			row.createCell(0).setCellValue(spec.getDeptId());
			row.createCell(1).setCellValue(spec.getDeptName());
			row.createCell(2).setCellValue(spec.getDeptHead());
			row.createCell(3).setCellValue(spec.getDeptPhone());
		}
	}

}
