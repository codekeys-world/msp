package com.codekeys.manuel;

import com.lowagie.text.*;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.*;
import com.vladsch.flexmark.ast.*;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;
import org.springframework.core.io.ClassPathResource;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/** Mise en page commune aux manuels : identité MSP, signets, tableaux et captures locales. */
public class ManuelPdfGenerator {
    private static final Color GREEN = new Color(15, 125, 75);
    private static final Color INK = new Color(31, 52, 43);
    private static final Color PALE = new Color(237, 247, 241);
    private final BaseFont base;

    public ManuelPdfGenerator() throws Exception {
        base = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, false);
    }

    private Font font(float size, int style, Color color) { return new Font(base, size, style, color); }

    public byte[] generate(String markdown, String title) throws Exception {
        var options = new MutableDataSet().set(Parser.EXTENSIONS, List.of(TablesExtension.create()));
        Node tree = Parser.builder(options).build().parse(markdown);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 42, 42, 65, 52);
        PdfWriter writer = PdfWriter.getInstance(doc, output);
        writer.setPageEvent(new Branding(title));
        doc.addTitle(title);
        doc.addAuthor("MSP — Mutuelle des Surveillants de Prison du Mali");
        doc.addSubject("Guide utilisateur illustré — procédures par rôle");
        doc.open();

        Image logo = logo();
        logo.scaleToFit(135, 135);
        logo.setAlignment(Element.ALIGN_CENTER);
        logo.setSpacingBefore(45);
        logo.setSpacingAfter(25);
        doc.add(logo);
        Paragraph brand = new Paragraph("MSP", font(36, Font.BOLD, GREEN));
        brand.setAlignment(Element.ALIGN_CENTER); doc.add(brand);
        Paragraph name = new Paragraph("Mutuelle des Surveillants de Prison du Mali", font(13, Font.NORMAL, INK));
        name.setAlignment(Element.ALIGN_CENTER); name.setSpacingAfter(38); doc.add(name);
        Paragraph heading = new Paragraph(title, font(27, Font.BOLD, GREEN));
        heading.setAlignment(Element.ALIGN_CENTER); heading.setSpacingAfter(20); doc.add(heading);
        Paragraph subtitle = new Paragraph("GUIDE PRATIQUE ILLUSTRÉ\nProcédures pas à pas • Repères à l’écran • Vérifications", font(13, Font.NORMAL, INK));
        subtitle.setAlignment(Element.ALIGN_CENTER); subtitle.setLeading(22); subtitle.setSpacingAfter(35); doc.add(subtitle);
        callout(doc, "Édition novembre 2026\nCaptures de l’application avec données fictives. Les menus et actions visibles dépendent des autorisations de votre compte.");
        doc.newPage();
        Paragraph tocTitle = new Paragraph("Sommaire", font(24, Font.BOLD, GREEN));
        tocTitle.setSpacingAfter(20); doc.add(tocTitle);
        int chapter = 0;
        for (Node n = tree.getFirstChild(); n != null; n = n.getNext()) {
            if (n instanceof Heading h && h.getLevel() == 2) {
                Chunk link = new Chunk(h.getText().toString(), font(11, Font.NORMAL, GREEN));
                link.setLocalGoto("chapter-" + (++chapter));
                Paragraph p = new Paragraph(link); p.setSpacingAfter(9); doc.add(p);
            }
        }
        callout(doc, "Utilisation du guide\nCliquez sur une rubrique du sommaire ou utilisez les signets du lecteur PDF. Pour chaque procédure : préparez les informations, suivez les étapes, puis vérifiez le résultat avant de poursuivre.");
        doc.newPage();
        chapter = 0;
        for (Node n = tree.getFirstChild(); n != null; n = n.getNext()) {
            if (n instanceof Heading h) {
                if (h.getLevel() == 1) continue;
                if (h.getLevel() == 2) {
                    if (chapter > 0) doc.newPage();
                    String destination = "chapter-" + (++chapter);
                    Chunk text = new Chunk(h.getText().toString(), font(20, Font.BOLD, GREEN));
                    text.setLocalDestination(destination);
                    Paragraph p = new Paragraph(text); p.setSpacingAfter(16); doc.add(p);
                    new PdfOutline(writer.getRootOutline(), PdfAction.gotoLocalPage(destination, false), h.getText().toString());
                } else {
                    Paragraph p = new Paragraph(h.getText().toString(), font(13, Font.BOLD, GREEN));
                    p.setSpacingBefore(14); p.setSpacingAfter(8); p.setKeepTogether(true); doc.add(p);
                }
            } else renderBlock(doc, n, 0);
        }
        doc.close();
        return output.toByteArray();
    }

    private void renderBlock(Document doc, Node node, int indent) throws Exception {
        if (node instanceof com.vladsch.flexmark.ast.Paragraph) {
            // Les figures sont des paragraphes dédiés dans les sources Markdown.
            if (node.getFirstChild() instanceof com.vladsch.flexmark.ast.Image image) {
                addFigure(doc, image);
            } else {
                Paragraph p = new Paragraph(); p.setLeading(16); p.setSpacingAfter(9); p.setIndentationLeft(indent);
                inline(p, node, Font.NORMAL); doc.add(p);
            }
        } else if (node instanceof BulletList || node instanceof OrderedList) {
            int number = 1;
            for (Node item = node.getFirstChild(); item != null; item = item.getNext()) {
                Paragraph p = new Paragraph();
                p.setFont(font(10.5f, Font.NORMAL, INK));
                p.add(new Chunk(node instanceof OrderedList ? (number++) + ".  " : "•  ", font(10.5f, Font.BOLD, GREEN)));
                p.setLeading(16); p.setSpacingAfter(7); p.setIndentationLeft(indent + 10);
                for (Node child = item.getFirstChild(); child != null; child = child.getNext()) {
                    if (child instanceof com.vladsch.flexmark.ast.Paragraph) inline(p, child, Font.NORMAL);
                }
                doc.add(p);
                for (Node child = item.getFirstChild(); child != null; child = child.getNext()) {
                    if (child instanceof BulletList || child instanceof OrderedList) renderBlock(doc, child, indent + 15);
                }
            }
        } else if (node instanceof BlockQuote) {
            Paragraph p = new Paragraph(); p.setLeading(16); inline(p, node, Font.NORMAL);
            PdfPTable box = new PdfPTable(1); box.setWidthPercentage(100); box.setSpacingBefore(8); box.setSpacingAfter(14);
            PdfPCell cell = new PdfPCell(p); cell.setPadding(12); cell.setBackgroundColor(PALE); cell.setBorderColor(GREEN); box.addCell(cell); doc.add(box);
        } else if (node.getNodeName().equals("TableBlock")) {
            List<Node> rows = new ArrayList<>(); collectRows(node, rows);
            rows.removeIf(row -> row.getChars().toString().matches("[\\s|:\\-]+"));
            if (rows.isEmpty()) return;
            int columns = 0; for (Node c = rows.get(0).getFirstChild(); c != null; c = c.getNext()) columns++;
            PdfPTable table = new PdfPTable(columns); table.setWidthPercentage(100); table.setSpacingBefore(8); table.setSpacingAfter(15); table.setHeaderRows(1);
            for (int r = 0; r < rows.size(); r++) {
                for (Node c = rows.get(r).getFirstChild(); c != null; c = c.getNext()) {
                    Paragraph p = new Paragraph(); p.setLeading(14); inline(p, c, r == 0 ? Font.BOLD : Font.NORMAL);
                    PdfPCell cell = new PdfPCell(p); cell.setPadding(8); cell.setBorderColor(new Color(215, 229, 220));
                    if (r % 2 == 0) cell.setBackgroundColor(PALE); table.addCell(cell);
                }
            }
            doc.add(table);
        } else if (node instanceof FencedCodeBlock block) {
            callout(doc, block.getContentChars().toString());
        }
    }

    private void collectRows(Node n, List<Node> rows) {
        for (Node c = n.getFirstChild(); c != null; c = c.getNext()) {
            if (c.getNodeName().equals("TableRow")) rows.add(c); else collectRows(c, rows);
        }
    }

    private void inline(Paragraph p, Node parent, int style) {
        for (Node n = parent.getFirstChild(); n != null; n = n.getNext()) {
            if (n instanceof StrongEmphasis) inline(p, n, style | Font.BOLD);
            else if (n instanceof Emphasis) inline(p, n, style | Font.ITALIC);
            else if (n instanceof SoftLineBreak || n instanceof HardLineBreak) p.add(" ");
            else if (n instanceof Code code) p.add(new Chunk(code.getText().toString(), font(10, style, GREEN)));
            else if (n instanceof Text) p.add(new Chunk(n.getChars().toString(), font(10.5f, style, INK)));
            else if (n.hasChildren()) inline(p, n, style);
            else p.add(new Chunk(n.getChars().toString(), font(10.5f, style, INK)));
        }
    }

    private void addFigure(Document doc, com.vladsch.flexmark.ast.Image source) throws Exception {
        String file = source.getUrl().toString();
        if (!file.matches("captures/[a-z0-9_-]+\\.png")) throw new IllegalArgumentException("Capture locale non autorisée : " + file);
        try (InputStream input = new ClassPathResource("documentation/" + file).getInputStream()) {
            Image image = Image.getInstance(input.readAllBytes());
            image.scaleToFit(505, 350); image.setAlignment(Element.ALIGN_CENTER);
            PdfPTable figure = new PdfPTable(1); figure.setWidthPercentage(100); figure.setKeepTogether(true); figure.setSpacingBefore(8); figure.setSpacingAfter(16);
            PdfPCell picture = new PdfPCell(); picture.addElement(image); picture.setPadding(5); picture.setBorderColor(new Color(215,229,220)); figure.addCell(picture);
            PdfPCell caption = new PdfPCell(new Phrase(source.getText().toString(), font(9, Font.ITALIC, INK)));
            caption.setPadding(8); caption.setBackgroundColor(PALE); caption.setBorder(Rectangle.NO_BORDER); figure.addCell(caption); doc.add(figure);
        }
    }

    private void callout(Document doc, String text) throws DocumentException {
        PdfPTable table = new PdfPTable(1); table.setWidthPercentage(100); table.setSpacingBefore(20); table.setSpacingAfter(14);
        PdfPCell cell = new PdfPCell(new Phrase(text, font(11, Font.NORMAL, INK)));
        cell.setPadding(16); cell.setBackgroundColor(PALE); cell.setBorderColor(GREEN); table.addCell(cell); doc.add(table);
    }

    private Image logo() throws Exception {
        try (InputStream input = new ClassPathResource("static/logo_msp.png").getInputStream()) { return Image.getInstance(input.readAllBytes()); }
    }

    private class Branding extends PdfPageEventHelper {
        private final String title;
        Branding(String title) { this.title = title; }
        @Override public void onEndPage(PdfWriter writer, Document doc) {
            PdfContentByte canvas = writer.getDirectContent();
            canvas.setColorStroke(GREEN); canvas.setLineWidth(1.5f);
            canvas.moveTo(42, 795); canvas.lineTo(553, 795); canvas.stroke();
            ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT, new Phrase("MSP  /  " + title, font(9, Font.BOLD, GREEN)), 42, 807, 0);
            ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT, new Phrase("Guide utilisateur • Données d’illustration fictives", font(8, Font.NORMAL, INK)), 42, 28, 0);
            ColumnText.showTextAligned(canvas, Element.ALIGN_RIGHT, new Phrase("Page " + writer.getPageNumber(), font(9, Font.BOLD, GREEN)), 553, 28, 0);
        }
    }
}
