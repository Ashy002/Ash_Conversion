<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<div class="card">
    <div class="card-body">
        <div class="d-flex justify-content-between align-items-center mb-3">
            <h5 class="card-title mb-0">Liste des fichiers</h5>
            <button class="btn btn-primary" data-bs-toggle="modal" data-bs-target="#uploadModal">
                Upload Fichier
            </button>
        </div>
        <div id="fileListContainer">
            <p class="text-muted text-center">Aucun fichier pour le moment.</p>
        </div>
    </div>
</div>

<!-- Upload Modal -->
<div class="modal fade" id="uploadModal" tabindex="-1">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title">Upload Fichier</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <div class="modal-body">
                <form id="uploadForm" enctype="multipart/form-data">
                    <div class="mb-3">
                        <label for="fileInput" class="form-label">Sélectionner un fichier</label>
                        <input type="file" class="form-control" id="fileInput" name="file" required>
                    </div>
                    <div class="mb-3">
                        <label for="conversionType" class="form-label">Type de conversion</label>
                        <select class="form-select" id="conversionType" name="conversionType" required>
                            <option value="">-- Sélectionner --</option>
                            <option value="PDF_TO_WORD">PDF → Word</option>
                            <option value="WORD_TO_PDF">Word → PDF</option>
                            <option value="PDF_TO_EXCEL">PDF → Excel</option>
                        </select>
                    </div>
                </form>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Annuler</button>
                <button type="button" class="btn btn-primary" id="uploadBtn">Upload</button>
            </div>
        </div>
    </div>
</div>

