package com.ashconversion.service;

import com.ashconversion.modele.enums.ConversionStatus;
import com.ashconversion.exception.ConversionException;
import com.ashconversion.modele.entity.FileJob;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Service de conversion PDF → Word (.docx)
 */
@Service
public class PdfToWordService {

    private static final Logger logger = LoggerFactory.getLogger(PdfToWordService.class);

    public void convert(File pdfFile, File outputFile) throws ConversionException {

        if (pdfFile == null || !pdfFile.exists()) {
            throw new ConversionException("Le fichier PDF source n'existe pas");
        }

        try (
                PDDocument document = Loader.loadPDF(pdfFile);
                XWPFDocument wordDocument = new XWPFDocument();
                FileOutputStream out = new FileOutputStream(outputFile)
        ) {

            logger.debug("Début conversion PDF → Word");

            // Extraction du texte du PDF
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);

            // Création du document Word
            XWPFParagraph paragraph = wordDocument.createParagraph();
            XWPFRun run = paragraph.createRun();
            run.setText(text);
            run.setFontSize(12);

            wordDocument.write(out);
            logger.info("Conversion PDF → Word réussie");

        } catch (IOException e) {
            logger.error("Erreur conversion PDF → Word", e);
            throw new ConversionException("Erreur conversion PDF → Word", e);
        }
    }

   public void convert(FileJob fileJob) throws ConversionException {
    try {
        if (fileJob.getFilePath() == null || fileJob.getOutputPath() == null) {
            throw new ConversionException("Les chemins source ou destination sont nuls.");
        }

        File pdfFile = new File(fileJob.getFilePath());
        File wordFile = new File(fileJob.getOutputPath());

        if (wordFile.getParentFile() != null) {
            wordFile.getParentFile().mkdirs();
        }

        // Appel de ta méthode de logique interne
        this.convert(pdfFile, wordFile);

    } catch (Exception e) {
        logger.error("Erreur lors de la conversion pour le job ID: " + fileJob.getId(), e);
        throw new ConversionException("Échec PDF vers Word : " + e.getMessage());
    }
    }
}