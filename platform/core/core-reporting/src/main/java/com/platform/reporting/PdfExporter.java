package com.platform.reporting;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
public class PdfExporter {

    private static final float MARGIN        = 40;
    private static final float ROW_HEIGHT    = 20;
    private static final float HEADER_HEIGHT = 25;
    private static final float FONT_SIZE     = 9;
    private static final float TITLE_SIZE    = 14;

    public byte[] export(ReportDefinition report) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            List<String> columns = report.getColumns();
            float pageWidth   = page.getMediaBox().getWidth();
            float colWidth    = (pageWidth - 2 * MARGIN) / columns.size();
            float startY      = page.getMediaBox().getHeight() - MARGIN;

            PDType1Font boldFont    = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDType1Font regularFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

            try (PDPageContentStream cs = new PDPageContentStream(document, page)) {

                // ── Title ──────────────────────────────────
                cs.beginText();
                cs.setFont(boldFont, TITLE_SIZE);
                cs.newLineAtOffset(MARGIN, startY);
                cs.showText(report.getTitle());
                cs.endText();
                startY -= 30;

                // ── Header row ─────────────────────────────
                cs.setNonStrokingColor(0.2f, 0.4f, 0.7f);
                cs.addRect(MARGIN, startY - HEADER_HEIGHT, pageWidth - 2 * MARGIN, HEADER_HEIGHT);
                cs.fill();

                cs.setNonStrokingColor(1f, 1f, 1f);
                cs.beginText();
                cs.setFont(boldFont, FONT_SIZE);
                cs.newLineAtOffset(MARGIN + 4, startY - HEADER_HEIGHT + 7);
                for (int i = 0; i < columns.size(); i++) {
                    cs.showText(truncate(columns.get(i), 18));
                    if (i < columns.size() - 1) {
                        cs.newLineAtOffset(colWidth, 0);
                    }
                }
                cs.endText();
                startY -= HEADER_HEIGHT;

                // ── Data rows ──────────────────────────────
                cs.setNonStrokingColor(0f, 0f, 0f);
                boolean alternate = false;
                for (Map<String, Object> row : report.getRows()) {
                    if (startY - ROW_HEIGHT < MARGIN) {
                        // Add new page if needed
                        cs.close();
                        break;
                    }

                    if (alternate) {
                        cs.setNonStrokingColor(0.95f, 0.95f, 0.95f);
                        cs.addRect(MARGIN, startY - ROW_HEIGHT, pageWidth - 2 * MARGIN, ROW_HEIGHT);
                        cs.fill();
                        cs.setNonStrokingColor(0f, 0f, 0f);
                    }
                    alternate = !alternate;

                    cs.beginText();
                    cs.setFont(regularFont, FONT_SIZE);
                    cs.newLineAtOffset(MARGIN + 4, startY - ROW_HEIGHT + 6);
                    for (int i = 0; i < columns.size(); i++) {
                        Object val = row.getOrDefault(columns.get(i), "");
                        cs.showText(truncate(val != null ? val.toString() : "", 18));
                        if (i < columns.size() - 1) {
                            cs.newLineAtOffset(colWidth, 0);
                        }
                    }
                    cs.endText();
                    startY -= ROW_HEIGHT;
                }
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.save(out);
            return out.toByteArray();
        }
    }

    private String truncate(String text, int max) {
        if (text == null) return "";
        return text.length() > max ? text.substring(0, max - 1) + "…" : text;
    }
}
