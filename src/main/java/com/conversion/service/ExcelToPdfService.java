package com.Ash_Conversion.service;

import com.Ash_Conversion.exception.ConversionException;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Service pour la conversion Excel (.xlsx, .xls) → PDF.
 * Utilise Apache POI pour lire l'Excel et iText 7 pour créer le PDF.
 */
public class ExcelToPdfService {
    
    private static final Logger logger = LoggerFactory.getLogger(ExcelToPdfService.class);
    
    /**
     * Convertit un fichier Excel (.xlsx ou .xls) en PDF.
     * 
     * @param excelFile Le fichier Excel source
     * @param outputFile Le fichier PDF de destination
     * @throws ConversionException si la conversion échoue
     */
    public void convert(File excelFile, File outputFile) throws ConversionException {
        if (excelFile == null || !excelFile.exists()) {
            throw new ConversionException("Le fichier Excel source n'existe pas");
        }
        
        try (FileInputStream fis = new FileInputStream(excelFile);
             Workbook workbook = createWorkbook(excelFile, fis);
             PdfWriter writer = new PdfWriter(outputFile);
             PdfDocument pdfDoc = new PdfDocument(writer);
             Document pdfDocument = new Document(pdfDoc)) {
            
            logger.debug("Début de la conversion Excel → PDF: {}", excelFile.getName());
            
            // Parcourir toutes les feuilles du classeur
            int sheetCount = workbook.getNumberOfSheets();
            for (int i = 0; i < sheetCount; i++) {
                Sheet sheet = workbook.getSheetAt(i);
                String sheetName = sheet.getSheetName();
                
                // Ajouter un titre pour chaque feuille
                if (sheetCount > 1) {
                    pdfDocument.add(new Paragraph(sheetName).setBold().setFontSize(14));
                    pdfDocument.add(new Paragraph(" ")); // Espacement
                }
                
                // Créer un tableau PDF pour cette feuille
                int maxColumns = getMaxColumns(sheet);
                if (maxColumns > 0) {
                    Table table = new Table(maxColumns);
                    
                    // Parcourir les lignes de la feuille
                    for (Row row : sheet) {
                        for (int col = 0; col < maxColumns; col++) {
                            org.apache.poi.ss.usermodel.Cell excelCell = row.getCell(col);
                            String cellValue = getCellValueAsString(excelCell);
                            table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(cellValue != null ? cellValue : "")));
                        }
                    }
                    
                    pdfDocument.add(table);
                    pdfDocument.add(new Paragraph(" ")); // Espacement entre les feuilles
                }
            }
            
            logger.info("Conversion Excel → PDF réussie: {} → {}", 
                       excelFile.getName(), outputFile.getName());
            
        } catch (IOException e) {
            logger.error("Erreur lors de la conversion Excel → PDF", e);
            throw new ConversionException("Erreur lors de la conversion Excel → PDF: " + e.getMessage(), e);
        }
    }
    
    /**
     * Crée un Workbook selon le type de fichier Excel.
     */
    private Workbook createWorkbook(File excelFile, FileInputStream fis) throws IOException {
        String filename = excelFile.getName().toLowerCase();
        if (filename.endsWith(".xlsx")) {
            return new XSSFWorkbook(fis);
        } else if (filename.endsWith(".xls")) {
            return new HSSFWorkbook(fis);
        } else {
            throw new IOException("Format Excel non supporté: " + filename);
        }
    }
    
    /**
     * Détermine le nombre maximum de colonnes dans une feuille.
     */
    private int getMaxColumns(Sheet sheet) {
        int maxColumns = 0;
        for (Row row : sheet) {
            if (row != null) {
                int lastCellNum = row.getLastCellNum();
                if (lastCellNum > maxColumns) {
                    maxColumns = lastCellNum;
                }
            }
        }
        return maxColumns;
    }
    
    /**
     * Extrait la valeur d'une cellule Excel sous forme de chaîne.
     */
    private String getCellValueAsString(org.apache.poi.ss.usermodel.Cell excelCell) {
        if (excelCell == null) {
            return "";
        }
        
        switch (excelCell.getCellType()) {
            case STRING:
                return excelCell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(excelCell)) {
                    return excelCell.getDateCellValue().toString();
                } else {
                    // Éviter la notation scientifique pour les grands nombres
                    double numValue = excelCell.getNumericCellValue();
                    if (numValue == (long) numValue) {
                        return String.valueOf((long) numValue);
                    } else {
                        return String.valueOf(numValue);
                    }
                }
            case BOOLEAN:
                return String.valueOf(excelCell.getBooleanCellValue());
            case FORMULA:
                try {
                    // Récupérer la valeur calculée
                    DataFormatter formatter = new DataFormatter();
                    return formatter.formatCellValue(excelCell);
                } catch (Exception e) {
                    return excelCell.getCellFormula();
                }
            case BLANK:
                return "";
            default:
                return "";
        }
    }
}
