package com.Ash_Conversion.service;

import com.Ash_Conversion.exception.ConversionException;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Service pour la conversion Word (.docx) → PDF.
 * Utilise Apache POI pour lire le DOCX et iText 7 pour créer le PDF.
 */
public class WordToPdfService {
    
    private static final Logger logger = LoggerFactory.getLogger(WordToPdfService.class);
    
    /**
     * Convertit un fichier Word (.docx) en PDF.
     * 
     * @param wordFile Le fichier DOCX source
     * @param outputFile Le fichier PDF de destination
     * @throws ConversionException si la conversion échoue
     */
    public void convert(File wordFile, File outputFile) throws ConversionException {
        if (wordFile == null || !wordFile.exists()) {
            throw new ConversionException("Le fichier Word source n'existe pas");
        }
        
        try (FileInputStream fis = new FileInputStream(wordFile);
             XWPFDocument document = new XWPFDocument(fis);
             PdfWriter writer = new PdfWriter(outputFile);
             PdfDocument pdfDoc = new PdfDocument(writer);
             Document pdfDocument = new Document(pdfDoc)) {
            
            logger.debug("Début de la conversion Word → PDF: {}", wordFile.getName());
            
            // Extraire le texte du document Word
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                String text = paragraph.getText();
                if (text != null && !text.trim().isEmpty()) {
                    pdfDocument.add(new Paragraph(text));
                }
            }
            
            logger.info("Conversion Word → PDF réussie: {} → {}", 
                       wordFile.getName(), outputFile.getName());
            
        } catch (IOException e) {
            logger.error("Erreur lors de la conversion Word → PDF", e);
            throw new ConversionException("Erreur lors de la conversion Word → PDF: " + e.getMessage(), e);
        }
    }
}
