package com.conversion;

import org.apache.catalina.startup.Tomcat;
import java.io.File;

public class Main {
    public static void main(String[] args) throws Exception {
        Tomcat tomcat = new Tomcat();
        tomcat.setPort(8080);

        // Déploie ton dossier webapp comme racine
        File webAppDir = new File("src/main/webapp");
        tomcat.addWebapp("/", webAppDir.getAbsolutePath());

        tomcat.start();
        tomcat.getServer().await();
    }
}
