package com.devSoft.Utils;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.web.servlet.View;

import com.devSoft.Model.Department;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.HeaderFooter;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class DepartmentPdfView implements View {

	@Override
	public String getContentType() {
		return "application/pdf";
	}

	@Override
	public void render(Map<String, ?> model, HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		response.addHeader("Content-Disposition", "attachment;filename=department.pdf");
		response.setContentType(getContentType());

		@SuppressWarnings("unchecked")
		List<Department> list = (List<Department>) model.get("dList");

		Document document = new Document();
		PdfWriter.getInstance(document, response.getOutputStream());
		document.open();

		HeaderFooter header = new HeaderFooter(new Phrase("DEPARTMENT PDF VIEW"), false);
		header.setAlignment(Element.ALIGN_CENTER);
		document.setHeader(header);

		HeaderFooter footer = new HeaderFooter(new Phrase(new Date() + " (C) bway, Page # "), true);
		footer.setAlignment(Element.ALIGN_CENTER);
		document.setFooter(footer);

		Font titleFont = new Font(Font.TIMES_ROMAN, 30, Font.BOLD);
		Paragraph title = new Paragraph("DEPARTMENT DATA", titleFont);
		title.setAlignment(Element.ALIGN_CENTER);
		title.setSpacingBefore(20.0f);
		title.setSpacingAfter(25.0f);
		document.add(title);

		Font tableHead = new Font(Font.TIMES_ROMAN, 12, Font.BOLD);
		PdfPTable table = new PdfPTable(4);
		table.addCell(new Phrase("ID", tableHead));
		table.addCell(new Phrase("DEPARTMENT NAME", tableHead));
		table.addCell(new Phrase("DEPARTMENT HOD", tableHead));
		table.addCell(new Phrase("DEPARTMENT PHONE", tableHead));

		for (Department spec : list) {
			table.addCell(String.valueOf(spec.getDeptId()));
			table.addCell(spec.getDeptName());
			table.addCell(spec.getDeptHead());
			table.addCell(spec.getDeptPhone());
		}
		document.add(table);

		document.close();
	}

}
