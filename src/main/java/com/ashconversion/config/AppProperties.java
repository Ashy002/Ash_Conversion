package com.ashconversion.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private Upload upload = new Upload();
    private Token token = new Token();

    public Upload getUpload() {
        return upload;
    }

    public Token getToken() {
        return token;
    }

  
    public static class Upload {
        private String directory;
        private String originalsDirectory;

        public String getDirectory() {
            return directory;
        }

        public void setDirectory(String directory) {
            this.directory = directory;
        }

        public String getOriginalsDirectory() {
            return originalsDirectory;
        }

        public void setOriginalsDirectory(String originalsDirectory) {
            this.originalsDirectory = originalsDirectory;
        }
    }

    public static class Token {
        private long expiration;

        public long getExpiration() {
            return expiration;
        }

        public void setExpiration(long expiration) {
            this.expiration = expiration;
        }
    }
}

