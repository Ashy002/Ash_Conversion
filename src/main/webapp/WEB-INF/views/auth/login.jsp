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
    <title>Connexion - Ash_Conversion</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.1/font/bootstrap-icons.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
    <style>
        :root {
            --bg-page: #fdf5e6;
            --bg-card: #faf0e6;
            --text-main: #3e2723;
            --accent-coffee: #8b4513;
            --accent-paper: #d7ccc8;
            --soft-green: #e8ede4;
        }

        body {
            background-color: var(--bg-page);
            background-image: radial-gradient(var(--accent-paper) 0.5px, transparent 0.5px);
            background-size: 30px 30px;
            min-height: 100vh;
            display: flex;
            align-items: center;
            font-family: 'Georgia', serif;
            color: var(--text-main);
            padding-top: 50px;
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
            padding: 2.5rem 2rem;
            text-align: center;
            border-bottom: 1px solid var(--accent-paper);
        }

        .auth-header h2 {
            margin: 0;
            font-weight: bold;
            letter-spacing: -1px;
        }

        .auth-body {
            padding: 2.5rem;
        }

        .form-label {
            font-weight: 600;
            color: var(--text-main);
            font-size: 0.9rem;
        }

        .form-control {
            background-color: #ffffff;
            border: 1px solid var(--accent-paper);
            color: var(--text-main);
        }

        .form-control:focus {
            border-color: var(--accent-coffee);
            box-shadow: 0 0 0 0.25rem rgba(139, 69, 19, 0.1);
            background-color: #fff;
        }

        /* Le bouton de connexion style "Café" */
        .btn-primary {
            background-color: var(--accent-coffee);
            border: none;
            padding: 0.8rem;
            font-weight: bold;
            transition: all 0.3s ease;
        }

        .btn-primary:hover {
            background-color: var(--text-main);
            transform: translateY(-2px);
            box-shadow: 0 5px 15px rgba(62, 39, 35, 0.2);
        }

        .text-decoration-none {
            color: var(--accent-coffee);
        }

        .text-decoration-none:hover {
            color: var(--text-main);
            text-decoration: underline !important;
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="row justify-content-center">
            <div class="col-md-5 col-lg-4">
                <div class="card auth-card">
                    <div class="auth-header">
                        <h2><i class="bi bi-shield-lock"></i> Connexion</h2>
                        <p class="mb-0 mt-2 opacity-75">Connectez vous à votre compte</p>
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
                        
                        <form action="${pageContext.request.contextPath}/login" method="POST" id="loginForm">
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
                                       placeholder="Entrer votre nom d'utilisateur" 
                                       required autofocus>
                            </div>
                            <div class="mb-4">
                                <label for="password" class="form-label">
                                    <i class="bi bi-lock"></i> Mot de passe
                                </label>
                                <input type="password" class="form-control form-control-lg" 
                                       id="password" name="password" 
                                       placeholder="Entrer votre mot de passe" 
                                       required>
                            </div>
                            <button type="submit" class="btn btn-primary w-100 btn-lg mb-3">
                                <i class="bi bi-box-arrow-in-right"></i> Se connecter
                            </button>
                        </form>
                        <div class="text-center">
                            <p class="mb-0 text-muted">
                                Pas de compte ? 
                                <a href="${pageContext.request.contextPath}/register" class="text-decoration-none fw-bold">
                                    S'inscrire
                                </a>
                            </p>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>

