<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ page import="com.ashconversion.util.CsrfTokenUtil" %>
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
    <title>Nouvel espace - Ash Studio</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.1/font/bootstrap-icons.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
    <style>
        body {
            background:
                radial-gradient(circle at 12% 12%, rgba(79,70,229,.22), transparent 28rem),
                radial-gradient(circle at 88% 90%, rgba(6,182,212,.18), transparent 30rem),
                #eef2ff;
            min-height: 100vh;
            display: flex;
            align-items: center;
            font-family: "Inter", "Segoe UI", Arial, sans-serif;
            color: #172033;
            padding-top: 50px;
        }

        .auth-card {
            border: 1px solid rgba(79,70,229,.16);
            border-radius: 28px;
            box-shadow: 0 28px 70px rgba(15,23,42,.16);
            overflow: hidden;
            background: rgba(255,255,255,.94);
            margin-top: 2rem;
        }

        .auth-header {
            background: linear-gradient(135deg, #4f46e5 0%, #0284c7 55%, #06b6d4 100%);
            color: #fff;
            padding: 2.5rem 2rem;
            text-align: center;
        }

        .auth-header h2 { margin: 0; font-weight: 800; letter-spacing: -0.04em; }
        .auth-body { padding: 2.5rem; }
        .form-label { font-weight: 700; color: #172033; font-size: .92rem; }
        .form-control { background-color: rgba(255,255,255,.92); border: 1px solid rgba(79,70,229,.18); color: #172033; border-radius: 16px; }
        .form-control:focus { border-color: #4f46e5; box-shadow: 0 0 0 .25rem rgba(79,70,229,.12); background-color: #fff; }
        .btn-primary { background: linear-gradient(135deg, #4f46e5 0%, #0284c7 55%, #06b6d4 100%); border: none; border-radius: 16px; padding: .85rem; font-weight: 800; transition: all .25s ease; box-shadow: 0 14px 30px rgba(79,70,229,.26); }
        .btn-primary:hover { transform: translateY(-2px); box-shadow: 0 18px 38px rgba(2,132,199,.28); filter: brightness(.98); }
        .text-decoration-none { color: #4f46e5; }
        .text-decoration-none:hover { color: #0284c7; text-decoration: underline !important; }
    </style>
    
</head>
<body>
    <div class="container">
        <div class="row justify-content-center">
            <div class="col-md-6 col-lg-5">
                <div class="card auth-card">
                    <div class="auth-header">
                        <h2><i class="bi bi-rocket-takeoff"></i> Créer votre espace</h2>
                        <p class="mb-0 mt-2 opacity-75">Démarrez avec une interface plus moderne</p>
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
                                String csrfToken = com.ashconversion.util.CsrfTokenUtil.getOrCreateToken(formSession);
                            %>
                            <input type="hidden" name="_csrf" value="<%= csrfToken != null ? csrfToken : "" %>">
                            <div class="mb-3">
                                <label for="username" class="form-label">
                                    <i class="bi bi-person"></i> Nom d'utilisateur
                                </label>
                                <input type="text" class="form-control form-control-lg" 
                                       id="username" name="username" 
                                       placeholder="Choisissez un identifiant clair" 
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
                                       placeholder="votre@email.com" 
                                       required>
                            </div>
                            <div class="mb-3">
                                <label for="password" class="form-label">
                                    <i class="bi bi-lock"></i> Mot de passe
                                </label>
                                <input type="password" class="form-control form-control-lg" 
                                       id="password" name="password" 
                                       placeholder="8 caractères minimum" 
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
                                       placeholder="Répétez votre mot de passe" 
                                       required>
                            </div>
                            <button type="submit" class="btn btn-primary w-100 btn-lg mb-3">
                                <i class="bi bi-person-check"></i> Créer un accès
                            </button>
                        </form>
                        <div class="text-center">
                            <p class="mb-0 text-muted">
                                Avez vous déjà un compte ? 
                                <a href="${pageContext.request.contextPath}/login" class="text-decoration-none fw-bold">
                                    Ouvrir mon espace
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

