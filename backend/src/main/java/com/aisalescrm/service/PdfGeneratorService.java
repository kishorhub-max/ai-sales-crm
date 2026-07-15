package com.aisalescrm.service;

import com.aisalescrm.entity.Invoice;
import com.aisalescrm.entity.Order;
import com.aisalescrm.entity.OrderItem;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Slf4j
public class PdfGeneratorService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private static final Font TITLE_FONT   = new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD,   new BaseColor(30, 58, 138));
    private static final Font HEADER_FONT  = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD,   BaseColor.WHITE);
    private static final Font NORMAL_FONT  = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, BaseColor.DARK_GRAY);
    private static final Font BOLD_FONT    = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD,   BaseColor.DARK_GRAY);
    private static final Font SMALL_FONT   = new Font(Font.FontFamily.HELVETICA,  9, Font.NORMAL, BaseColor.GRAY);
    private static final BaseColor HEADER_BG   = new BaseColor(30, 58, 138);
    private static final BaseColor ROW_ALT_BG  = new BaseColor(241, 245, 249);

    public byte[] generateInvoicePdf(Invoice invoice) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Document doc = new Document(PageSize.A4, 40, 40, 50, 50);
            PdfWriter.getInstance(doc, out);
            doc.open();

            // ── Header bar ───────────────────────────────────────────────────
            addHeader(doc, invoice);

            doc.add(Chunk.NEWLINE);

            // ── Bill To / Invoice Info ───────────────────────────────────────
            addBillToSection(doc, invoice);

            doc.add(Chunk.NEWLINE);

            // ── Line items table ─────────────────────────────────────────────
            if (invoice.getOrder() != null && !invoice.getOrder().getItems().isEmpty()) {
                addItemsTable(doc, invoice.getOrder().getItems());
            }

            doc.add(Chunk.NEWLINE);

            // ── Totals ───────────────────────────────────────────────────────
            addTotalsSection(doc, invoice);

            doc.add(Chunk.NEWLINE);

            // ── Footer ───────────────────────────────────────────────────────
            addFooter(doc, invoice);

            doc.close();
            return out.toByteArray();

        } catch (Exception e) {
            log.error("PDF generation failed for invoice {}", invoice.getInvoiceNumber(), e);
            throw new RuntimeException("Failed to generate invoice PDF: " + e.getMessage());
        }
    }

    // ── Sections ─────────────────────────────────────────────────────────────

    private void addHeader(Document doc, Invoice invoice) throws DocumentException {
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[]{2f, 1f});

        // Company name
        PdfPCell companyCell = new PdfPCell();
        companyCell.setBorder(Rectangle.NO_BORDER);
        companyCell.setBackgroundColor(HEADER_BG);
        companyCell.setPadding(15);
        Paragraph companyPara = new Paragraph("AI Sales CRM", TITLE_FONT);
        companyPara.add(new Chunk("\nEnterprise Solutions",
                new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL, new BaseColor(147, 197, 253))));
        companyCell.addElement(companyPara);
        headerTable.addCell(companyCell);

        // Invoice label
        PdfPCell invoiceCell = new PdfPCell();
        invoiceCell.setBorder(Rectangle.NO_BORDER);
        invoiceCell.setBackgroundColor(HEADER_BG);
        invoiceCell.setPadding(15);
        invoiceCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        Font invLabelFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, BaseColor.WHITE);
        Paragraph invLabel = new Paragraph("INVOICE", invLabelFont);
        invLabel.setAlignment(Element.ALIGN_RIGHT);
        Font invNumFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, new BaseColor(147, 197, 253));
        invLabel.add(new Chunk("\n" + invoice.getInvoiceNumber(), invNumFont));
        invoiceCell.addElement(invLabel);
        headerTable.addCell(invoiceCell);

        doc.add(headerTable);
    }

    private void addBillToSection(Document doc, Invoice invoice) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1f, 1f});

        // Bill To
        PdfPCell billToCell = new PdfPCell();
        billToCell.setBorder(Rectangle.NO_BORDER);
        billToCell.setPadding(5);
        Paragraph billTo = new Paragraph();
        billTo.add(new Chunk("BILL TO\n", BOLD_FONT));
        if (invoice.getCustomer() != null) {
            billTo.add(new Chunk(invoice.getCustomer().getFullName() + "\n", NORMAL_FONT));
            if (invoice.getCustomer().getCompany() != null)
                billTo.add(new Chunk(invoice.getCustomer().getCompany() + "\n", NORMAL_FONT));
            billTo.add(new Chunk(invoice.getCustomer().getEmail(), SMALL_FONT));
        }
        billToCell.addElement(billTo);
        table.addCell(billToCell);

        // Invoice details
        PdfPCell detailsCell = new PdfPCell();
        detailsCell.setBorder(Rectangle.NO_BORDER);
        detailsCell.setPadding(5);
        detailsCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        Paragraph details = new Paragraph();
        details.setAlignment(Element.ALIGN_RIGHT);
        details.add(new Chunk("Issue Date: ", BOLD_FONT));
        details.add(new Chunk(invoice.getIssueDate() != null
                ? invoice.getIssueDate().format(DATE_FMT) : "-", NORMAL_FONT));
        details.add(new Chunk("\nDue Date:   ", BOLD_FONT));
        details.add(new Chunk(invoice.getDueDate() != null
                ? invoice.getDueDate().format(DATE_FMT) : "-", NORMAL_FONT));
        details.add(new Chunk("\nStatus:     ", BOLD_FONT));
        details.add(new Chunk(invoice.getStatus().name(), NORMAL_FONT));
        detailsCell.addElement(details);
        table.addCell(detailsCell);

        doc.add(table);
    }

    private void addItemsTable(Document doc, List<OrderItem> items) throws DocumentException {
        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{3f, 1f, 1.5f, 1f, 1.5f});

        // Header row
        String[] headers = {"Product", "Qty", "Unit Price", "Discount", "Total"};
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, HEADER_FONT));
            cell.setBackgroundColor(HEADER_BG);
            cell.setPadding(8);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setBorder(Rectangle.NO_BORDER);
            table.addCell(cell);
        }

        // Data rows
        boolean alt = false;
        for (OrderItem item : items) {
            BaseColor bg = alt ? ROW_ALT_BG : BaseColor.WHITE;
            addItemRow(table, item, bg);
            alt = !alt;
        }

        doc.add(table);
    }

    private void addItemRow(PdfPTable table, OrderItem item, BaseColor bg) {
        String name     = item.getProduct() != null ? item.getProduct().getName() : "—";
        String qty      = String.valueOf(item.getQuantity());
        String price    = String.format("$%.2f", item.getUnitPrice());
        String discount = item.getDiscountPercent() != null && item.getDiscountPercent().doubleValue() > 0
                ? String.format("%.1f%%", item.getDiscountPercent()) : "—";
        String total    = String.format("$%.2f", item.getTotalPrice());

        String[] values = {name, qty, price, discount, total};
        int[] aligns    = {Element.ALIGN_LEFT, Element.ALIGN_CENTER,
                Element.ALIGN_RIGHT, Element.ALIGN_CENTER, Element.ALIGN_RIGHT};

        for (int i = 0; i < values.length; i++) {
            PdfPCell cell = new PdfPCell(new Phrase(values[i], NORMAL_FONT));
            cell.setBackgroundColor(bg);
            cell.setPadding(7);
            cell.setHorizontalAlignment(aligns[i]);
            cell.setBorderColor(new BaseColor(226, 232, 240));
            cell.setBorderWidth(0.5f);
            table.addCell(cell);
        }
    }

    private void addTotalsSection(Document doc, Invoice invoice) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(45);
        table.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.setWidths(new float[]{1.5f, 1f});

        addTotalRow(table, "Subtotal",  String.format("$%.2f", invoice.getSubtotal()), false);
        if (invoice.getDiscountAmount() != null && invoice.getDiscountAmount().doubleValue() > 0)
            addTotalRow(table, "Discount (" + invoice.getDiscountPercent() + "%)",
                    String.format("-$%.2f", invoice.getDiscountAmount()), false);
        if (invoice.getTaxAmount() != null && invoice.getTaxAmount().doubleValue() > 0)
            addTotalRow(table, "Tax (" + invoice.getTaxPercent() + "%)",
                    String.format("$%.2f", invoice.getTaxAmount()), false);
        addTotalRow(table, "TOTAL", String.format("$%.2f", invoice.getTotal()), true);

        doc.add(table);
    }

    private void addTotalRow(PdfPTable table, String label, String value, boolean highlight) {
        BaseColor bg   = highlight ? HEADER_BG : BaseColor.WHITE;
        Font lblFont   = highlight ? new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD, BaseColor.WHITE) : BOLD_FONT;
        Font valFont   = highlight ? new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD, BaseColor.WHITE) : NORMAL_FONT;

        PdfPCell labelCell = new PdfPCell(new Phrase(label, lblFont));
        labelCell.setBackgroundColor(bg);
        labelCell.setPadding(8);
        labelCell.setBorder(Rectangle.NO_BORDER);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, valFont));
        valueCell.setBackgroundColor(bg);
        valueCell.setPadding(8);
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        valueCell.setBorder(Rectangle.NO_BORDER);

        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    private void addFooter(Document doc, Invoice invoice) throws DocumentException {
        Paragraph footer = new Paragraph();
        footer.setAlignment(Element.ALIGN_CENTER);
        footer.add(new Chunk("Thank you for your business!\n",
                new Font(Font.FontFamily.HELVETICA, 10, Font.ITALIC, new BaseColor(100, 116, 139))));
        if (invoice.getNotes() != null && !invoice.getNotes().isBlank()) {
            footer.add(new Chunk("Notes: " + invoice.getNotes(), SMALL_FONT));
        }
        doc.add(footer);
    }
}