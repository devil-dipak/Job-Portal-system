package com.devSoft.Utils;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.web.servlet.View;

import com.devSoft.Model.Certificate;
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

public class CertificatePdfView implements View {

	@Override
	public String getContentType() {
		return "application/pdf";
	}

	@Override
	public void render(Map<String, ?> model, HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		response.addHeader("Content-Disposition", "attachment;filename=certificate.pdf");
		response.setContentType(getContentType());

		@SuppressWarnings("unchecked")
		List<Certificate> list = (List<Certificate>) model.get("cList");

		Document document = new Document();
		PdfWriter.getInstance(document, response.getOutputStream());
		document.open();

		if (list != null && !list.isEmpty()) {
			StringBuilder hashes = new StringBuilder();
			for (Certificate c : list) {
				if (c.getCertificateHash() != null) {
					if (hashes.length() > 0)
						hashes.append(",");
					hashes.append(c.getCertificateHash());
				}
			}
			document.addSubject(hashes.toString());
		}

		HeaderFooter header = new HeaderFooter(new Phrase("CERTIFICATE PDF VIEW"), false);
		header.setAlignment(Element.ALIGN_CENTER);
		document.setHeader(header);

		HeaderFooter footer = new HeaderFooter(new Phrase(new Date() + " (C) Patan Multiple Campus, Page # "), true);
		footer.setAlignment(Element.ALIGN_CENTER);
		document.setFooter(footer);

		Font titleFont = new Font(Font.TIMES_ROMAN, 30, Font.BOLD);
		Paragraph title = new Paragraph("CERTIFICATE DATA", titleFont);
		title.setAlignment(Element.ALIGN_CENTER);
		title.setSpacingBefore(20.0f);
		title.setSpacingAfter(25.0f);
		document.add(title);

		Font tableHead = new Font(Font.TIMES_ROMAN, 12, Font.BOLD);
		PdfPTable table = new PdfPTable(5);
		table.addCell(new Phrase("ID", tableHead));
		table.addCell(new Phrase("COURSE NAME", tableHead));
		table.addCell(new Phrase("DEPARTMENT", tableHead));
		table.addCell(new Phrase("ISSUER NAME", tableHead));
		table.addCell(new Phrase("HASH VALUE", tableHead));

		for (Certificate spec : list) {
			table.addCell(String.valueOf(spec.getId()));
			table.addCell(spec.getCourseName());
			table.addCell(spec.getDepartment());
			table.addCell(spec.getIssuer());
			table.addCell(spec.getCertificateHash());
		}
		document.add(table);

		document.close();
	}

}
