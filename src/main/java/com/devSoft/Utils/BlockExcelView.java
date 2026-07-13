package com.devSoft.Utils;

import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.servlet.View;

import com.devSoft.Model.Block;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class BlockExcelView implements View {

	@Override
	public String getContentType() {
		return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
	}

	@Override
	public void render(Map<String, ?> model, HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		response.addHeader("Content-Disposition", "attachment;filename=block.xlsx");
		response.setContentType(getContentType());

		@SuppressWarnings("unchecked")
		List<Block> list = (List<Block>) model.get("bList");

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
		row.createCell(1).setCellValue("PREVIOUS HASH");
		row.createCell(2).setCellValue("CURRENT HASH");
		row.createCell(3).setCellValue("DATA");
	}

	private void setBody(Sheet sheet, List<Block> list) {
		int rowNum = 1;
		for (Block spec : list) {
			Row row = sheet.createRow(rowNum++);
			row.createCell(0).setCellValue(spec.getId());
			row.createCell(1).setCellValue(spec.getPreviousHash());
			row.createCell(2).setCellValue(spec.getCurrentHash());
			row.createCell(3).setCellValue(spec.getData());
		}
	}

}
