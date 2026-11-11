import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import javax.imageio.ImageIO;
import java.nio.file.*;

/** Prévisualisation facultative avec PDFBox 2 sur le classpath. */
class PreviewPdf {
    public static void main(String[] args) throws Exception {
        Path output = Path.of(args[1]);
        Files.createDirectories(output);
        try (var paths = Files.list(Path.of(args[0]))) {
            for (Path path : paths.filter(p -> p.toString().endsWith(".pdf")).toList()) {
                try (PDDocument pdf = PDDocument.load(path.toFile())) {
                    System.out.println(path.getFileName() + " : " + pdf.getNumberOfPages() + " pages");
                    var stripper = new PDFTextStripper();
                    for (int i = 1; i <= pdf.getNumberOfPages(); i++) {
                        stripper.setStartPage(i); stripper.setEndPage(i);
                        String text = stripper.getText(pdf);
                        if (text.trim().length() < 100) throw new IllegalStateException("Page presque vide : " + path + " / " + i);
                    }
                    if (path.getFileName().toString().equals("manuel_tresorier.pdf")) {
                        PDFRenderer renderer = new PDFRenderer(pdf);
                        for (int i : new int[]{0,1,2,3,5,7,9,12,pdf.getNumberOfPages()-1}) {
                            if (i < pdf.getNumberOfPages()) ImageIO.write(renderer.renderImageWithDPI(i, 110), "png", output.resolve("tresorier-"+(i+1)+".png").toFile());
                        }
                    }
                }
            }
        }
    }
}
