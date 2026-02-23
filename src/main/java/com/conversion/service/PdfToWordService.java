package com.Ash_Conversion.service;

import com.Ash_Conversion.exception.ConversionException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Service pour la conversion PDF → Word (.docx).
 * Utilise PDFBox pour extraire le texte et Apache POI pour créer le DOCX.
 */
public class PdfToWordService {
    
    private static final Logger logger = LoggerFactory.getLogger(PdfToWordService.class);
    
    /**
     * Convertit un fichier PDF en Word (.docx).
     * 
     * @param pdfFile Le fichier PDF source
     * @param outputFile Le fichier DOCX de destination
     * @throws ConversionException si la conversion échoue
     */
    public void convert(File pdfFile, File outputFile) throws ConversionException {
        if (pdfFile == null || !pdfFile.exists()) {
            throw new ConversionException("Le fichier PDF source n'existe pas");
        }
        
        try (PDDocument document = Loader.loadPDF(pdfFile);
             XWPFDocument wordDocument = new XWPFDocument();
             FileOutputStream out = new FileOutputStream(outputFile)) {
            
            logger.debug("Début de la conversion PDF → Word: {}", pdfFile.getName());
            
            // Extraire le texte du PDF
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            
            // Créer le document Word avec le texte extrait
            XWPFParagraph paragraph = wordDocument.createParagraph();
            XWPFRun run = paragraph.createRun();
            run.setText(text);
            run.setFontSize(12);
            
            // Sauvegarder le document Word
            wordDocument.write(out);
            
            logger.info("Conversion PDF → Word réussie: {} → {}", 
                       pdfFile.getName(), outputFile.getName());
            
        } catch (IOException e) {
            logger.error("Erreur lors de la conversion PDF → Word", e);
            throw new ConversionException("Erreur lors de la conversion PDF → Word: " + e.getMessage(), e);
        }
    }
}
