<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ page import="com.Ash_Conversion.util.CsrfTokenUtil" %>
<%@ page import="jakarta.servlet.http.HttpSession" %>
<%
    // Récupérer les flash messages
    String flashSuccess = (String) session.getAttribute("flashSuccess");
    String flashError = (String) session.getAttribute("flashError");
    if (flashSuccess != null) session.removeAttribute("flashSuccess");
    if (flashError != null) session.removeAttribute("flashError");
%>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Inscription - Ash_Conversion</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.1/font/bootstrap-icons.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
    <style>
    :root {
        --bg-page: #fdf5e6;
        --bg-card: #faf0e6;
        --text-main: #3e2723;
        --text-muted: #6d4c41;
        --accent-coffee: #8b4513;
        --accent-paper: #d7ccc8;
        --soft-green: #e8ede4;
    }

    body {
        background-color: var(--bg-page);
        /* Effet subtil de texture papier */
        background-image: radial-gradient(var(--accent-paper) 0.5px, transparent 0.5px);
        background-size: 30px 30px;
        min-height: 100vh;
        display: flex;
        align-items: center;
        padding-top: 50px;
        font-family: 'Georgia', serif;
        color: var(--text-main);
    }

    .auth-card {
        border: 1px solid var(--accent-paper);
        border-radius: 12px;
        box-shadow: 0 15px 35px rgba(62, 39, 35, 0.1);
        overflow: hidden;
        background: var(--bg-card);
        margin-top: 2rem;
    }

    .auth-header {
        background-color: var(--soft-green);
        color: var(--text-main);
        padding: 2rem;
        text-align: center;
        border-bottom: 1px solid var(--accent-paper);
    }

    .auth-header h2 {
        margin: 0;
        font-weight: bold;
    }

    .auth-body {
        padding: 2.5rem;
    }

    .form-label {
        font-weight: 600;
        color: var(--text-main);
    }

    .form-control {
        background-color: #ffffff;
        border: 1px solid var(--accent-paper);
        color: var(--text-main);
    }

    .form-control:focus {
        border-color: var(--accent-coffee);
        box-shadow: 0 0 0 0.25rem rgba(139, 69, 19, 0.1);
    }

    .btn-primary {
        background-color: var(--accent-coffee);
        border: none;
        padding: 0.75rem;
        font-weight: bold;
        transition: all 0.3s ease;
    }

    .btn-primary:hover {
        background-color: var(--text-main);
        transform: translateY(-2px);
    }

    .password-requirements {
        font-size: 0.85rem;
        color: var(--text-muted);
        font-style: italic;
        margin-top: 0.4rem;
    }

    .text-decoration-none {
        color: var(--accent-coffee);
    }

    .text-decoration-none:hover {
        color: var(--text-main);
        text-decoration: underline !important;
    }

    /* Style pour les alertes */
    .alert {
        border-radius: 8px;
        border: 1px solid rgba(0,0,0,0.05);
    }
</style>
    
</head>
<body>
    <div class="container">
        <div class="row justify-content-center">
            <div class="col-md-6 col-lg-5">
                <div class="card auth-card">
                    <div class="auth-header">
                        <h2><i class="bi bi-person-plus"></i> Inscription</h2>
                        <p class="mb-0 mt-2 opacity-75">Créez un compte</p>
                    </div>
                    <div class="auth-body">
                        <!-- Flash Messages -->
                        <c:if test="<%= flashSuccess != null %>">
                            <div class="alert alert-success alert-dismissible fade show" role="alert">
                                <i class="bi bi-check-circle"></i> <c:out value="${flashSuccess}" />
                                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                            </div>
                        </c:if>
                        <c:if test="<%= flashError != null %>">
                            <div class="alert alert-danger alert-dismissible fade show" role="alert">
                                <i class="bi bi-exclamation-triangle"></i> <c:out value="${flashError}" />
                                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                            </div>
                        </c:if>
                        
                        <form action="${pageContext.request.contextPath}/register" method="POST" id="registerForm">
                            <%
                                // S'assurer que la session existe
                                HttpSession formSession = request.getSession(true);
                                String csrfToken = com.Ash_Conversion.util.CsrfTokenUtil.getOrCreateToken(formSession);
                            %>
                            <input type="hidden" name="_csrf" value="<%= csrfToken != null ? csrfToken : "" %>">
                            <div class="mb-3">
                                <label for="username" class="form-label">
                                    <i class="bi bi-person"></i> Nom d'utilisateur
                                </label>
                                <input type="text" class="form-control form-control-lg" 
                                       id="username" name="username" 
                                       placeholder="3-20 caractères (lettres, chiffres, _, -)" 
                                       required autofocus
                                       pattern="[a-zA-Z0-9_-]{3,20}"
                                       title="3-20 caractères (lettres, chiffres, underscore, tiret)">
                                <div class="password-requirements">
                                    <i class="bi bi-info-circle"></i> 3 à 20 caractères (lettres, chiffres, underscore, tiret)
                                </div>
                            </div>
                            <div class="mb-3">
                                <label for="email" class="form-label">
                                    <i class="bi bi-envelope"></i> Email
                                </label>
                                <input type="email" class="form-control form-control-lg" 
                                       id="email" name="email" 
                                       placeholder="mon@email.com" 
                                       required>
                            </div>
                            <div class="mb-3">
                                <label for="password" class="form-label">
                                    <i class="bi bi-lock"></i> Mot de passe
                                </label>
                                <input type="password" class="form-control form-control-lg" 
                                       id="password" name="password" 
                                       placeholder="Minimum 8 caractères" 
                                       required
                                       minlength="8">
                                <div class="password-requirements">
                                    <i class="bi bi-shield-check"></i> 8 caractères minimum avec au moins une lettre et un chiffre
                                </div>
                            </div>
                            <div class="mb-4">
                                <label for="confirmPassword" class="form-label">
                                    <i class="bi bi-lock-fill"></i> Confirmer le mot de passe
                                </label>
                                <input type="password" class="form-control form-control-lg" 
                                       id="confirmPassword" name="confirmPassword" 
                                       placeholder="confirmer votre mot de passe" 
                                       required>
                            </div>
                            <button type="submit" class="btn btn-primary w-100 btn-lg mb-3">
                                <i class="bi bi-person-check"></i> S'inscrire
                            </button>
                        </form>
                        <div class="text-center">
                            <p class="mb-0 text-muted">
                                Avez vous déjà un compte ? 
                                <a href="${pageContext.request.contextPath}/login" class="text-decoration-none fw-bold">
                                    Se connecter
                                </a>
                            </p>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        // Validation côté client
        document.getElementById('registerForm').addEventListener('submit', function(e) {
            const password = document.getElementById('password').value;
            const confirmPassword = document.getElementById('confirmPassword').value;
            
            if (password !== confirmPassword) {
                e.preventDefault();
                alert('Les mots de passe ne correspondent pas !');
                return false;
            }
        });
    </script>
</body>
</html>

