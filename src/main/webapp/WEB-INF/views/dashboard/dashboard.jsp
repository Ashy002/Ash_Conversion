<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ page import="com.ashconversion.util.CsrfTokenUtil" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.List" %>
<%
    // Récupérer les flash messages
    String flashSuccess = (String) session.getAttribute("flashSuccess");
    String flashError = (String) session.getAttribute("flashError");
    if (flashSuccess != null) session.removeAttribute("flashSuccess");
    if (flashError != null) session.removeAttribute("flashError");
    
    // Récupérer le token CSRF pour JavaScript
    String csrfToken = CsrfTokenUtil.getOrCreateToken(session);
    
    // Récupérer les données du servlet
    Map<String, Long> stats = (Map<String, Long>) request.getAttribute("stats");
    List<com.ashconversion.modele.entity.FileJob> fileJobs = 
        (List<com.ashconversion.modele.entity.FileJob>) request.getAttribute("fileJobs");
    String currentTab = (String) request.getAttribute("currentTab");
    String currentSearch = (String) request.getAttribute("currentSearch");
    Integer currentPage = (Integer) request.getAttribute("currentPage");
    Integer totalPages = (Integer) request.getAttribute("totalPages");
    Long totalCount = (Long) request.getAttribute("totalCount");
    
    // Valeurs par défaut si null
    if (stats == null) {
        stats = new java.util.HashMap<>();
        stats.put("total", 0L);
        stats.put("converted", 0L);
        stats.put("pending", 0L);
        stats.put("processing", 0L);
        stats.put("failed", 0L);
    }
    if (fileJobs == null) fileJobs = new java.util.ArrayList<>();
    if (currentTab == null) currentTab = "all";
    if (currentSearch == null) currentSearch = "";
    if (currentPage == null) currentPage = 1;
    if (totalPages == null) totalPages = 1;
    if (totalCount == null) totalCount = 0L;
%>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Espace fichiers - Ash Studio</title>

<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
   <link rel="stylesheet" href="/assets/css/style.css">
  <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.1/font/bootstrap-icons.css"> 
  
    <style>
        body { font-family: "Inter", "Segoe UI", Arial, sans-serif; }
        .page-hero {
            background: linear-gradient(135deg, rgba(79,70,229,.96), rgba(2,132,199,.92));
            color: #fff;
            border-radius: 28px;
            padding: 1.5rem;
            box-shadow: 0 24px 55px rgba(15,23,42,.16);
        }
        .page-hero .btn { background: rgba(255,255,255,.95) !important; color: #3730a3 !important; box-shadow: none; }
        .stats-card { border: 1px solid rgba(79,70,229,.16) !important; border-radius: 24px !important; transition: transform .22s ease, box-shadow .22s ease; }
        .stats-card:hover { transform: translateY(-5px); box-shadow: 0 24px 55px rgba(15,23,42,.14) !important; }
        .file-item { border-left: 4px solid transparent; transition: all .2s; }
        .file-item:hover { border-left-color: #4f46e5 !important; background-color: rgba(79,70,229,.05); }
        .badge-status { font-size: .75rem; padding: .42em .72em; }
        .search-box { max-width: 520px; }
        .pagination .page-link { border-radius: 14px; margin: 0 2px; border: 1px solid rgba(79,70,229,.16); }
        .pagination .page-item.active .page-link { background: linear-gradient(135deg, #4f46e5 0%, #0284c7 100%); border-color: transparent; }
        .text-success-soft { color: #059669 !important; }
        .text-danger-soft { color: #dc2626 !important; }
        .btn-logout { background-color: #fff1f2 !important; color: #be123c !important; border: 1px solid #fecdd3 !important; }
    </style>
</head>
<body>
    <jsp:include page="../shared/header.jsp"/>
    
<div class="container-fluid py-4">
    <div class="page-hero d-flex flex-column flex-lg-row justify-content-between align-items-lg-center gap-3 mb-4">
        <h1 class="h3 mb-0"> Atelier de conversion </h1>
        <button class="btn btn-custom-green" data-bs-toggle="modal" data-bs-target="#uploadModal">
            <i class="bi bi-cloud-upload"></i> Ajouter un fichier
        </button>
    </div>
    
    <!-- Flash Messages -->
    <c:if test="${not empty flashSuccess}">
        <div class="alert alert-success alert-dismissible fade show" role="alert">
            <i class="bi bi-check-circle"></i> <c:out value="${flashSuccess}" />
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    </c:if>
    <c:if test="${not empty flashError}">
        <div class="alert alert-danger alert-dismissible fade show" role="alert">
            <i class="bi bi-exclamation-triangle"></i> <c:out value="${flashError}" />
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    </c:if>
    
    <!-- Stats Cards -->
     <div class="row g-3 mb-4">
    <div class="col-md-4">
        <div class="card stats-card shadow-sm border-0">
            <div class="card-body">
                <div class="d-flex justify-content-between align-items-center">
                    <div>
                        <h6 class="card-subtitle mb-2 text-muted">Fichiers au total</h6>
                        <h2 class="card-title mb-0" style="color: var(--accent-coffee); font-weight: bold;"><%= stats.get("total") %></h2>
                    </div>
                    <i class="bi bi-files stats-icon" style="font-size: 2rem; color: var(--accent-paper);"></i>
                </div>
            </div>
        </div>
    </div>
    
    <div class="col-md-4">
        <div class="card stats-card shadow-sm border-0">
            <div class="card-body">
                <div class="d-flex justify-content-between align-items-center">
                    <div>
                        <h6 class="card-subtitle mb-2 text-muted">Réussis</h6>
                        <h2 class="card-title mb-0" style="color: var(--success-soft); font-weight: bold;"><%= stats.get("converted") %></h2>
                    </div>
                    <i class="bi bi-check-circle stats-icon" style="font-size: 2rem; color: var(--success-soft); opacity: 0.3;"></i>
                </div>
            </div>
        </div>
    </div>

    <div class="col-md-4">
        <div class="card stats-card shadow-sm border-0">
            <div class="card-body">
                <div class="d-flex justify-content-between align-items-center">
                    <div>
                        <h6 class="card-subtitle mb-2 text-muted">À vérifier</h6>
                        <h2 class="card-title mb-0" style="color: var(--danger-soft); font-weight: bold;"><%= stats.get("failed") != null ? stats.get("failed") : 0L %></h2>
                    </div>
                    <i class="bi bi-x-circle stats-icon" style="font-size: 2rem; color: var(--danger-soft); opacity: 0.3;"></i>
                </div>
            </div>
        </div>
    </div>
</div>

   
    <!-- Search and Tabs -->
    <div class="card shadow-sm mb-4">
        <div class="card-body">
            <!-- Search Box -->
            <div class="row mb-3">
                <div class="col-md-6">
                    <form method="GET" action="${pageContext.request.contextPath}/dashboard" class="d-flex">
                        <input type="hidden" name="tab" value="<c:out value="${currentTab}" />">
                        <div class="input-group search-box">
                            <span class="input-group-text"><i class="bi bi-search"></i></span>
                            <input type="text" class="form-control" name="search" 
                                   placeholder="Filtrer vos fichiers par nom" 
                                   value="<c:out value="${currentSearch}" /> ">
                            <button class="btn btn-outline-secondary" type="submit">Filtrer</button>
                            <c:if test="${!empty currentSearch}">
                                <a href="${pageContext.request.contextPath}/dashboard?tab=<c:out value="${currentTab}" />" 
                                   class="btn btn-outline-danger">
                                    <i class="bi bi-x"></i>
                                </a>
                            </c:if>
                        </div>
                    </form>
                </div>
                <div class="col-md-6 text-end">
                    <span class="text-muted">
                        <i class="bi bi-info-circle"></i> 
                        <c:out value="${totalCount}" /> fichier(s) trouvé(s)
                    </span>
                </div>
            </div>
    
    <!-- Tabs -->
            <ul class="nav nav-tabs" id="fileTabs" role="tablist">
        <li class="nav-item" role="presentation">
                    <a class="nav-link ${currentTab == 'all' ? 'active' : ''}" 
                       href="${pageContext.request.contextPath}/dashboard?tab=all&search=<c:out value="${fn:escapeXml(currentSearch)}" /> ">
                        <i class="bi bi-list-ul"></i> Tous
                    </a>
        </li>
        <li class="nav-item" role="presentation">
                    <a class="nav-link ${currentTab == 'uploaded' ? 'active' : ''}" 
                       href="${pageContext.request.contextPath}/dashboard?tab=uploaded&search=<c:out value="${fn:escapeXml(currentSearch)}" />">
                        <i class="bi bi-cloud-arrow-up"></i> Téléchargés
                    </a>
        </li>
        <li class="nav-item" role="presentation">
                    <a class="nav-link ${currentTab == 'converted' ? 'active' : ''}" 
                       href="${pageContext.request.contextPath}/dashboard?tab=converted&search=<c:out value="${fn:escapeXml(currentSearch)}" />">
                        <i class="bi bi-check-circle"></i> Réussis
                    </a>
        </li>
     <%--   <li class="nav-item" role="presentation">
                    <a class="nav-link ${currentTab == 'pending' ? 'active' : ''}" 
                       href="${pageContext.request.contextPath}/dashboard?tab=pending&search=<c:out value="${fn:escapeXml(currentSearch)}" />">
                        <i class="bi bi-clock-history"></i> File d’attente
                    </a>
        </li> --%>
              
    </ul>
        </div>
    </div>
    
    <!-- File List -->
    <div class="card shadow-sm">
        <div class="card-body p-0">
            <c:choose>
                <c:when test="${empty fileJobs}">
                    <div class="text-center py-5">
                        <i class="bi bi-inbox" style="font-size: 4rem; color: #dee2e6;"></i>
                        <p class="text-muted mt-3">Aucun document dans cet espace pour le moment</p>
                        <button class="btn btn-primary" data-bs-toggle="modal" data-bs-target="#uploadModal">
                            <i class="bi bi-cloud-upload"></i> Ajouter mon premier fichier
                        </button>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="table-responsive">
                        <table class="table table-hover mb-0">
                            <thead class="table-light">
                                <tr>
                                    <th style="width: 30%;">Nom du fichier</th>
                                    <th style="width: 10%;">Taille</th>
                                    <th style="width: 15%;">Date</th>
                                    <th style="width: 15%;">Type</th>
                                    <th style="width: 10%;">Statut</th>
                                    <th style="width: 10%;">Format </th>
                                    <th style="width: 10%;">Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="fileJob" items="${fileJobs}">
                                    <tr class="file-item">
                                        <td>
                                            <i class="bi bi-file-earmark"></i>
                                            <strong>${fileJob.originalFilename}</strong>
                                        </td>
                                        <td>
                                            <fmt:formatNumber value="${fileJob.fileSize / 1024}" 
                                                              maxFractionDigits="2" /> KB
                                        </td>
                                        <td>
                                            <c:set var="createdAt" value="${fileJob.createdAt}" />
                                            <c:choose>
                                                <c:when test="${createdAt != null}">
                                                    <%
                                                        java.time.LocalDateTime ldt = (java.time.LocalDateTime) pageContext.getAttribute("createdAt");
                                                        if (ldt != null) {
                                                            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                                                            out.print(ldt.format(formatter));
                                                        }
                                                    %>
                                                </c:when>
                                                <c:otherwise>N/A</c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${fileJob.mimeType != null && fileJob.mimeType.contains('pdf')}">
                                                    <span class="badge bg-danger">PDF</span>
                                                </c:when>
                                                <c:when test="${fileJob.mimeType != null && fileJob.mimeType.contains('word')}">
                                                    <span class="badge bg-primary">Word</span>
                                                </c:when>
                                                <c:when test="${fileJob.mimeType != null && fileJob.mimeType.contains('excel')}">
                                                    <span class="badge bg-success">Excel</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="badge bg-secondary">${fileJob.mimeType != null ? fileJob.mimeType : 'N/A'}</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                         <c:choose>
    <c:when test="${fileJob.status eq 'COMPLETED'}">
        <span class="badge bg-success badge-status">
            <i class="bi bi-check-circle"></i> Prêt
        </span>
    </c:when>

    <c:when test="${fileJob.status eq 'PROCESSING'}">
        <span class="badge bg-warning badge-status">
            <i class="bi bi-clock"></i> File d’attente
        </span>
    </c:when>

    <c:when test="${fileJob.status eq 'UPLOADED'}">
        <span class="badge bg-info badge-status">
            <i class="bi bi-gear"></i> Traitement
        </span>
    </c:when>

    <c:when test="${fileJob.status eq 'FAILED'}">
        <span class="badge bg-danger badge-status">
            <i class="bi bi-x-circle"></i> À relancer
        </span>
    </c:when>
</c:choose>

                                        </td>
                                        <td>
                                           <c:choose>
    <c:when test="${fileJob.conversionType eq 'PDF_TO_WORD'}">
        <span class="badge bg-primary">DOCX</span>
    </c:when>
    <c:when test="${fileJob.conversionType eq 'WORD_TO_PDF'}">
        <span class="badge bg-danger">PDF</span>
    </c:when>
    <c:when test="${fileJob.conversionType eq 'PDF_TO_EXCEL'}">
        <span class="badge bg-success">XLSX</span>
    </c:when>
    <c:when test="${fileJob.conversionType eq 'EXCEL_TO_PDF'}">
        <span class="badge bg-danger">PDF</span>
    </c:when>
</c:choose>

                                        </td>
                                        <td>
                                            <div class="btn-group btn-group-sm" role="group">
                         <c:if test="${fileJob.status eq 'UPLOADED'
                                                              or fileJob.status eq 'PROCESSING'
                                                             or fileJob.status eq 'FAILED'}">
                                <button class="btn btn-outline-primary btn-action"
                                                    title="Relancer"
                                                   onclick="convertFile(${fileJob.id}, '${fileJob.conversionType}')">
                                                       <i class="bi bi-arrow-repeat"></i>
                                                   </button>
                                          </c:if>

                                               <c:if test="${fileJob.status eq 'COMPLETED'}">
    <button class="btn btn-outline-info btn-action" 
            title="Aperçu" 
            onclick="previewFile(${fileJob.id}, '${fileJob.outputFilename}')">
        <i class="bi bi-eye"></i>
    </button>                                                                                                                                                                          

    <button class="btn btn-outline-success btn-action" 
            title="Récupérer" onclick="downloadFile(${fileJob.id})">
        <i class="bi bi-download"></i>
    </button>

    <button class="btn btn-outline-secondary btn-action" 
            title="Envoyer" onclick="shareFile(${fileJob.id})">
        <i class="bi bi-share"></i>
    </button>
</c:if>
     <button class="btn btn-outline-danger btn-action" 
        title="Retirer" onclick="deleteFile(${fileJob.id})">
        <i class="bi bi-trash"></i>
            </button>
      </div>
     </td>
     </tr>
    </c:forEach>
    </tbody>
   </table>
    </div>                                           
                    
                    <!-- Pagination -->
                    <c:if test="${totalPages > 1}">
                        <div class="card-footer">
                            <nav aria-label="Pagination">
                                <ul class="pagination justify-content-center mb-0">
                                    <c:if test="${currentPage > 1}">
                                        <li class="page-item">
                                            <a class="page-link" 
                                               href="${pageContext.request.contextPath}/dashboard?tab=${currentTab}&search=${fn:escapeXml(currentSearch)}&page=${currentPage - 1}">
                                                <i class="bi bi-chevron-left"></i>
                                            </a>
                                        </li>
                                    </c:if>
                                    
                                    <c:forEach var="i" begin="1" end="${totalPages}">
                                        <c:if test="${i == 1 || i == totalPages || (i >= currentPage - 2 && i <= currentPage + 2)}">
                                            <li class="page-item ${i == currentPage ? 'active' : ''}">
                                                <a class="page-link" 
                                                   href="${pageContext.request.contextPath}/dashboard?tab=${currentTab}&search=${fn:escapeXml(currentSearch)}&page=${i}">
                                                    ${i}
                                                </a>
                                            </li>
                                        </c:if>
                                        <c:if test="${i == currentPage - 3 || i == currentPage + 3}">
                                            <li class="page-item disabled">
                                                <span class="page-link">...</span>
                                            </li>
                                        </c:if>
                                    </c:forEach>
                                    
                                    <c:if test="${currentPage < totalPages}">
                                        <li class="page-item">
                                            <a class="page-link" 
                                               href="${pageContext.request.contextPath}/dashboard?tab=${currentTab}&search=${fn:escapeXml(currentSearch)}&page=${currentPage + 1}">
                                                <i class="bi bi-chevron-right"></i>
                                            </a>
                                        </li>
                                    </c:if>
                                </ul>
                            </nav>
                        </div>
                    </c:if>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</div>

<!-- Upload Modal -->
<div class="modal fade" id="uploadModal" tabindex="-1">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title"><i class="bi bi-cloud-upload"></i> Ajouter un fichier</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <form id="uploadForm" action="${pageContext.request.contextPath}/api/files/upload" 
                  method="POST" enctype="multipart/form-data">
                <input type="hidden" name="_csrf" value="<%= CsrfTokenUtil.getOrCreateToken(session) %>">
                <div class="modal-body">
                    <div class="mb-3">
                        <label for="fileInput" class="form-label">
                            <i class="bi bi-file-earmark"></i> Choisir un document
                        </label>
                        <input type="file" class="form-control" id="fileInput" name="file" 
                               accept=".pdf,.docx,.doc,.xlsx,.xls" required>
                        <div class="form-text">
                            <i class="bi bi-info-circle"></i> Formats acceptés : PDF, Word et Excel — jusqu’à 100 MB
                        </div>
                    </div>
                    <div class="mb-3">
                        <label for="conversionType" class="form-label">
                            <i class="bi bi-arrow-repeat"></i> Conversion souhaitée
                        </label>
                        <select class="form-select" id="conversionType" name="conversionType" required>
                            <option value="">-- Choisir une transformation --</option>
                            <option value="PDF_TO_WORD">PDF → Word (.docx)</option>
                            <option value="WORD_TO_PDF">Word (.docx) → PDF</option>
                            <option value="PDF_TO_EXCEL">PDF → Excel (.xlsx)</option>
                            <option value="EXCEL_TO_PDF">Excel (.xlsx) → PDF</option>
                        </select>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Retour</button>
                    <button type="submit" class="btn btn-primary" id="uploadBtn">
                        <i class="bi bi-magic"></i> Lancer la conversion
                    </button>
                </div>
            </form>
        </div>
    </div>
</div>

<!-- Share Modal -->
<div class="modal fade" id="shareModal" tabindex="-1">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title"><i class="bi bi-share"></i> Envoyer ce document</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <div class="modal-body">
                <div class="mb-3">
                    <label for="shareUrl" class="form-label">Lien sécurisé</label>
                    <div class="input-group">
                        <input type="text" class="form-control" id="shareUrl" readonly>
                        <button class="btn btn-outline-secondary" type="button" onclick="copyShareUrl()">
                            <i class="bi bi-clipboard"></i> Copier le lien
                        </button>
                    </div>
                   <!-- <div class="form-text">Le lien expire dans 24 heures</div> -->
                </div>
                <div class="d-grid gap-2">
                    <a id="whatsappShare" href="#" target="_blank" class="btn btn-success">
                        <i class="bi bi-whatsapp"></i> WhatsApp
                    </a>
                    <a id="telegramShare" href="#" target="_blank" class="btn btn-primary">
                        <i class="bi bi-telegram"></i> Telegram
                    </a>
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Terminer</button>
            </div>
        </div>
    </div>
</div>

<!-- Preview Modal -->
<div class="modal fade" id="previewModal" tabindex="-1">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title"><i class="bi bi-eye"></i> Aperçu du document</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <div class="modal-body p-0">
                <iframe id="previewFrame" src="" style="width: 100%; height: 600px; border: none;"></iframe>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Terminer</button>
            </div>
        </div>
    </div>
</div>

<script src="/assets/js/bootstrap.bundle.min.js"></script>

<script>
    // Token CSRF pour les appels API
    const csrfToken = '<%= csrfToken %>';
    
    // Conversion
    function convertFile(id, conversionType) {
        if (!conversionType) {
            alert('Conversion souhaitée non défini');
            return;
        }
        
        // Déterminer le format cible
        let to = 'docx';
        if (conversionType === 'WORD_TO_PDF') {
            to = 'pdf';
        } else if (conversionType === 'PDF_TO_EXCEL') {
            to = 'xlsx';
        } else if (conversionType === 'EXCEL_TO_PDF') {
            to = 'pdf';
        }
        
        fetch('${pageContext.request.contextPath}/api/convert?id=' + id + '&to=' + to + '&_csrf=' + encodeURIComponent(csrfToken), {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
                'X-CSRF-Token': csrfToken
            }
        })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                alert('Conversion démarrée !');
                setTimeout(() => location.reload(), 2000);
            } else {
                alert('Erreur: ' + (data.message || 'Erreur inconnue'));
            }
        })
        .catch(error => {
            console.error('Erreur:', error);
            alert('Erreur lors du démarrage de la conversion');
        });
    }
    
    // Téléchargement
    function downloadFile(id) {
        window.location.href = '${pageContext.request.contextPath}/api/download?id=' + id;
    }
    
    // Prévisualisation
      function previewFile(id, filename) {
    // On définit l'URL de l'API
    const previewUrl = `${pageContext.request.contextPath}/api/files/preview/` + id;
    const modalBody = document.querySelector('#previewModal .modal-body');
    
    // 1. On vérifie l'extension du fichier
    const isPDF = filename.toLowerCase().endsWith('.pdf');

    if (!isPDF) {
        // --- CAS EXCEL / WORD : Pas de fetch, on affiche direct le message ---
        modalBody.innerHTML = `
            <div class="text-center p-5">
                <i class="bi bi-file-earmark-spreadsheet" style="font-size: 4rem; color: #198754;"></i>
                <h5 class="mt-3">Aperçu direct indisponible</h5>
                <p class="text-muted">Ce format ne s’affiche pas directement ici, mais il reste disponible en téléchargement.</p>
                <div class="mt-4">
                    <button class="btn btn-success" onclick="downloadFile(${id})">
                        <i class="bi bi-download"></i> Récupérer pour ouvrir
                    </button>
                </div>
            </div>
        `;
    } else {
        // --- CAS PDF : On injecte l'iframe pour afficher le document ---
        modalBody.innerHTML = `
            <iframe id="previewFrame" 
                    src="${previewUrl}" 
                    style="width:100%; height:500px; border:none;">
            </iframe>
        `;
    }

    // 2. On affiche le modal Bootstrap
    const previewModalElement = document.getElementById('previewModal');
    const previewModal = bootstrap.Modal.getOrCreateInstance(previewModalElement);
    previewModal.show();
 }
    
    // Partage
    let currentShareUrl = '';
    function shareFile(id) {
        fetch('${pageContext.request.contextPath}/api/share', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
                'X-CSRF-Token': csrfToken
            },
            body: 'fileId=' + id + '&_csrf=' + encodeURIComponent(csrfToken)
        })
        .then(response => response.json())
        .then(data => {
            if (data.success && data.data) {
                currentShareUrl = data.data.url;
                document.getElementById('shareUrl').value = currentShareUrl;
                
                // URLs pour WhatsApp et Telegram avec lien direct de téléchargement
                // Le lien de partage (currentShareUrl) permet de télécharger directement le fichier
                const encodedUrl = encodeURIComponent(currentShareUrl);
                const message = encodeURIComponent('📄 Voici votre fichier ! :\n' + currentShareUrl);
                
                // WhatsApp : message avec lien de téléchargement direct
                document.getElementById('whatsappShare').href = 
                    'https://wa.me/?text=' + message;
                
                // Telegram : partage avec URL et message
                document.getElementById('telegramShare').href = 
                    'https://t.me/share/url?url=' + encodedUrl + '&text=' + encodeURIComponent('📄 Voici votre fichier  !');
                
                const shareModal = new bootstrap.Modal(document.getElementById('shareModal'));
                shareModal.show();
            } else {
                alert('Erreur: ' + (data.message || 'Erreur lors de la génération du lien'));
            }
        })
        .catch(error => {
            console.error('Erreur:', error);
            alert('Erreur lors de la génération du lien de partage');
        });
    }
    
    function copyShareUrl() {
        const shareUrlInput = document.getElementById('shareUrl');
        shareUrlInput.select();
        document.execCommand('copy');
        alert('Lien copié dans le presse-papiers !');
    }
    
    // Suppression
    function deleteFile(id) {
        if (confirm('Êtes-vous sûr de vouloir supprimer ce fichier ?')) {
            fetch('${pageContext.request.contextPath}/api/files/' + id, {
                method: 'DELETE',
                headers: {
                    'X-CSRF-Token': csrfToken
                }
            })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    alert('Fichier supprimé ');
                    location.reload();
                } else {
                    alert('Erreur: ' + (data.message || 'Erreur lors de la suppression'));
                }
            })
            .catch(error => {
                console.error('Erreur:', error);
                alert('Erreur lors de la suppression');
            });
        }
    }
</script>

<jsp:include page="../shared/footer.jsp"/>
