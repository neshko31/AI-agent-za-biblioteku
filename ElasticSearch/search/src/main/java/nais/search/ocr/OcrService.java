package nais.search.ocr;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

@Service
public class OcrService {

    private static final byte[] PDF_MAGIC = {'%', 'P', 'D', 'F'};

    private final Tesseract tesseract;
    private final int maxPages;

    public OcrService(@Value("${ocr.tessdata-path:/usr/share/tesseract-ocr/5/tessdata}") String tessDataPath,
                       @Value("${ocr.language:eng}") String language,
                       @Value("${ocr.max-pages:20}") int maxPages) {
        validateTessData(tessDataPath, language);

        this.tesseract = new Tesseract();
        this.tesseract.setDatapath(tessDataPath);
        this.tesseract.setLanguage(language);
        this.maxPages = maxPages;
    }

    private void validateTessData(String tessDataPath, String language) {
        File dataDir = new File(tessDataPath);
        if (!dataDir.isDirectory()) {
            throw new IllegalStateException(
                    "ocr.tessdata-path ('" + tessDataPath + "') ne postoji ili nije direktorijum. "
                            + "Provizorno: pronadji stvarnu putanju u kontejneru sa "
                            + "'find / -name eng.traineddata' i postavi OCR_TESSDATA_PATH.");
        }
        for (String lang : language.split("\\+")) {
            File trainedData = new File(dataDir, lang + ".traineddata");
            if (!trainedData.isFile()) {
                throw new IllegalStateException(
                        "Nedostaje fajl '" + trainedData.getAbsolutePath() + "'. "
                                + "Provjeri da je odgovarajuci tesseract-ocr-<lang> paket instaliran u image-u.");
            }
        }
    }

    public String extractText(byte[] fileBytes) throws IOException, TesseractException {
        if (isPdf(fileBytes)) {
            return extractFromPdf(fileBytes);
        }
        return extractFromImage(fileBytes);
    }

    private boolean isPdf(byte[] bytes) {
        if (bytes.length < PDF_MAGIC.length) return false;
        for (int i = 0; i < PDF_MAGIC.length; i++) {
            if (bytes[i] != PDF_MAGIC[i]) return false;
        }
        return true;
    }

    private String extractFromPdf(byte[] pdfBytes) throws IOException, TesseractException {
        StringBuilder text = new StringBuilder();
        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            PDFRenderer renderer = new PDFRenderer(document);
            int pages = Math.min(document.getNumberOfPages(), maxPages);
            for (int page = 0; page < pages; page++) {
                BufferedImage image = renderer.renderImageWithDPI(page, 150);
                text.append(tesseract.doOCR(image));
                text.append("\n");
            }
        }
        return text.toString().trim();
    }

    private String extractFromImage(byte[] imageBytes) throws IOException, TesseractException {
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));
        if (image == null) {
            throw new IOException("Nepoznat format slike");
        }
        return tesseract.doOCR(image).trim();
    }
}
