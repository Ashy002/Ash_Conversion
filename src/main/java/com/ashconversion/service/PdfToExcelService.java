package com.ashconversion.service;

import com.ashconversion.exception.ConversionException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.util.regex.Pattern;

/**
 * Service optimisé pour la conversion PDF vers Excel (.xlsx)
 */
@Service
public class PdfToExcelService {

    private static final Logger logger = LoggerFactory.getLogger(PdfToExcelService.class);

    // Détection des colonnes par tabulations ou par blocs d'au moins 2 espaces
    private static final Pattern TAB_PATTERN = Pattern.compile("\\t+");
    private static final Pattern SPACE_PATTERN = Pattern.compile("\\s{2,}");

    public void convert(File pdfFile, File outputFile) throws ConversionException {

        // 1. Vérification de sécurité
        if (pdfFile == null || !pdfFile.exists()) {
            throw new ConversionException("Le fichier PDF source est introuvable.");
        }

        // 2. Utilisation du try-with-resources pour fermer automatiquement les flux
        try (PDDocument document = Loader.loadPDF(pdfFile);
             Workbook workbook = new XSSFWorkbook()) {

            logger.info("Début de l'extraction des données du PDF : {}", pdfFile.getName());

            // 3. Extraction du texte avec tri par position (indispensable pour l'alignement)
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true); 
            String text = stripper.getText(document);

            // 4. Création de la feuille de calcul
            Sheet sheet = workbook.createSheet("Données Extraites");
            String[] lines = text.split("\\r?\\n");
            int rowNum = 0;
            int maxCellIndex = 0;

            for (String line : lines) {
                if (line.trim().isEmpty()) continue;

                Row row = sheet.createRow(rowNum++);
                String[] cells;

                // Logique de découpage en colonnes
                if (line.contains("\t")) {
                    cells = TAB_PATTERN.split(line);
                } else if (SPACE_PATTERN.matcher(line).find()) {
                    cells = SPACE_PATTERN.split(line);
                } else {
                    cells = new String[]{line};
                }

                // Remplissage des cellules
                for (int i = 0; i < cells.length && i < 20; i++) {
                    Cell cell = row.createCell(i);
                    cell.setCellValue(cells[i].trim());
                    if (i > maxCellIndex) maxCellIndex = i;
                }
            }

            // 5. Ajustement de la largeur des colonnes (Largeur fixe pour la stabilité)
            for (int i = 0; i <= maxCellIndex; i++) {
                sheet.setColumnWidth(i, 6000); 
            }

            // 6. Écriture physique du fichier
            try (FileOutputStream out = new FileOutputStream(outputFile)) {
                workbook.write(out);
            }

            logger.info("Fichier Excel généré avec succès : {}", outputFile.getAbsolutePath());

        } catch (Exception e) {
            // Log détaillé pour identifier la cause exacte de l'échec
            logger.error("ERREUR CRITIQUE pendant la conversion Excel : ", e);
            throw new ConversionException("Échec de la conversion : " + e.getMessage(), e);
        }
    }
}