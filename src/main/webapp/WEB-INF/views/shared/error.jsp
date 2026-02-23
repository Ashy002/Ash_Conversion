<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page isErrorPage="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%
    // Récupérer le code d'erreur
    Integer statusCode = (Integer) request.getAttribute("jakarta.servlet.error.status_code");
    if (statusCode == null && pageContext.getErrorData() != null) {
        statusCode = pageContext.getErrorData().getStatusCode();
    }
    if (statusCode == null) {
        statusCode = 500;
    }
    
    // Récupérer le message d'erreur (sécurisé)
    String errorMessage = (String) request.getAttribute("errorMessage");
    if (errorMessage == null) {
        switch (statusCode) {
            case 404:
                errorMessage = "La page demandée est introuvable.";
                break;
            case 403:
                errorMessage = "Accès refusé. Vous n'avez pas les permissions nécessaires.";
                break;
            case 500:
            default:
                errorMessage = "Une erreur interne s'est produite. Veuillez réessayer plus tard.";
                break;
        }
    }
%>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Erreur - Ash_Conversion</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.1/font/bootstrap-icons.css">
</head>
<body>
    <div class="container mt-5">
        <div class="row justify-content-center">
            <div class="col-md-6">
                <div class="card shadow">
                    <div class="card-body text-center py-5">
                        <i class="bi bi-exclamation-triangle-fill text-danger" style="font-size: 4rem;"></i>
                        <h1 class="display-4 mt-3"><c:out value="${statusCode}" /></h1>
                        <h2 class="mb-4">Erreur</h2>
                        <p class="text-muted mb-4"><c:out value="${errorMessage}" /></p>
                        <div class="d-grid gap-2">
                            <a href="${pageContext.request.contextPath}/dashboard" class="btn btn-primary">
                                <i class="bi bi-house"></i> Retour au Dashboard
                            </a>
                            <a href="${pageContext.request.contextPath}/" class="btn btn-outline-secondary">
                                <i class="bi bi-arrow-left"></i> Retour à l'accueil
                            </a>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</body>
</html>

