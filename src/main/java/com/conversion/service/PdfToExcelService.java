package com.Ash_Conversion.service;

import com.Ash_Conversion.exception.ConversionException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.regex.Pattern;

/**
 * Service pour la conversion PDF → Excel (.xlsx).
 * Utilise PDFBox pour extraire le texte et Apache POI pour créer le XLSX.
 */
public class PdfToExcelService {
    
    private static final Logger logger = LoggerFactory.getLogger(PdfToExcelService.class);
    private static final Pattern TAB_PATTERN = Pattern.compile("\\t+");
    private static final Pattern SPACE_PATTERN = Pattern.compile("\\s{2,}");
    
    /**
     * Convertit un fichier PDF en Excel (.xlsx).
     * 
     * @param pdfFile Le fichier PDF source
     * @param outputFile Le fichier XLSX de destination
     * @throws ConversionException si la conversion échoue
     */
    public void convert(File pdfFile, File outputFile) throws ConversionException {
        if (pdfFile == null || !pdfFile.exists()) {
            throw new ConversionException("Le fichier PDF source n'existe pas");
        }
        
        try (PDDocument document = Loader.loadPDF(pdfFile);
             Workbook workbook = new XSSFWorkbook();
             FileOutputStream out = new FileOutputStream(outputFile)) {
            
            logger.debug("Début de la conversion PDF → Excel: {}", pdfFile.getName());
            
            // Extraire le texte du PDF
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            
            // Créer une feuille Excel
            Sheet sheet = workbook.createSheet("Données");
            
            // Diviser le texte en lignes
            String[] lines = text.split("\\r?\\n");
            
            int rowNum = 0;
            for (String line : lines) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                
                Row row = sheet.createRow(rowNum++);
                
                // Essayer de détecter des colonnes (tabs ou espaces multiples)
                String[] cells;
                if (line.contains("\t")) {
                    cells = TAB_PATTERN.split(line);
                } else if (SPACE_PATTERN.matcher(line).find()) {
                    cells = SPACE_PATTERN.split(line);
                } else {
                    cells = new String[]{line};
                }
                
                int cellNum = 0;
                for (String cellValue : cells) {
                    if (cellNum >= 20) break; // Limiter à 20 colonnes
                    Cell cell = row.createCell(cellNum++);
                    cell.setCellValue(cellValue.trim());
                }
            }
            
            // Auto-size les colonnes
            for (int i = 0; i < 20; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // Sauvegarder le fichier Excel
            workbook.write(out);
            
            logger.info("Conversion PDF → Excel réussie: {} → {}", 
                       pdfFile.getName(), outputFile.getName());
            
        } catch (IOException e) {
            logger.error("Erreur lors de la conversion PDF → Excel", e);
            throw new ConversionException("Erreur lors de la conversion PDF → Excel: " + e.getMessage(), e);
        }
    }
}
