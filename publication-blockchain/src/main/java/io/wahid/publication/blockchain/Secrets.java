package io.wahid.publication.blockchain;

public class Secrets {
    private Secrets() {
    }

    public static String getSecret(String name) {
        // 1. Try environment variable first (Cloud Run)
        String value = System.getenv(name);
        if (value != null && !value.isEmpty()) {
            return value;
        }

        // 2. Fallback to Docker secret file
        String secretFilePath = "/run/secrets/" + name;
        try {
            java.nio.file.Path path = java.nio.file.Paths.get(secretFilePath);
            if (java.nio.file.Files.exists(path)) {
                return java.nio.file.Files.readString(path).trim();
            }
        } catch (Exception e) {
            System.err.println("exception-> " + e.getMessage());
        }

        throw new RuntimeException("Secret not found: " + name);
    }
}
