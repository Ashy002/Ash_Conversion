package com.Ash_Conversion.config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

public class DatabaseConfig {
    
    public static EntityManager getEntityManager() {
        EntityManagerFactory emf = DatabaseInitializer.getEntityManagerFactory();
        if (emf == null) {
            throw new IllegalStateException("EntityManagerFactory non initialisé");
        }
        return emf.createEntityManager();
    }
}

