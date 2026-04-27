package com.ashconversion.service;

import com.ashconversion.exception.ConversionException;
import com.ashconversion.modele.entity.FileJob;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import org.apache.poi.xwpf.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Service de conversion Word (.docx) → PDF
 */
@Service
public class WordToPdfService {

    private static final Logger logger = LoggerFactory.getLogger(WordToPdfService.class);

    /**
     * LOGIQUE DE CONVERSION (Fichier à Fichier)
     */
public void convert(File wordFile, File outputFile) throws ConversionException {
    if (wordFile == null || !wordFile.exists()) {
        throw new ConversionException("Le fichier Word source n'existe pas");
    }

    try (
        FileInputStream fis = new FileInputStream(wordFile);
        XWPFDocument document = new XWPFDocument(fis);
        PdfWriter writer = new PdfWriter(outputFile);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document pdfDocument = new Document(pdfDoc)
    ) {
        for (XWPFParagraph paragraph : document.getParagraphs()) {
            String text = paragraph.getText();
            if (text != null && !text.isBlank()) {
                pdfDocument.add(new Paragraph(text));
            }
        }
    } catch (Exception e) {
        // NE SURTOUT PAS UTILISER fileJob ICI
        throw new ConversionException("Erreur technique WordToPdf : " + e.getMessage());
    }
}

    /**
     * PONT AVEC L'ENTITÉ (Appelé par ConversionService)
     */
    public void convert(FileJob fileJob) throws ConversionException {
        try {
            if (fileJob.getFilePath() == null || fileJob.getOutputPath() == null) {
                throw new ConversionException("Les chemins source ou destination sont nuls.");
            }

            File wordFile = new File(fileJob.getFilePath());
            File pdfFile = new File(fileJob.getOutputPath());

            if (pdfFile.getParentFile() != null) {
                pdfFile.getParentFile().mkdirs();
            }

            // On appelle la méthode au-dessus
            this.convert(wordFile, pdfFile);

        } catch (Exception e) {
            logger.error("Erreur pour le job ID: " + fileJob.getId(), e);
            throw new ConversionException("Échec Word vers PDF : " + e.getMessage());
        }
    }
}