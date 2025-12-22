package io.wahid.publication.blockchain;

public class Web3Config {
    public static final String RPC_URL = Secrets.getSecret("SEPOLIA_RPC_URL");
    public static final String PRIVATE_KEY = Secrets.getSecret("WEB3_PRIVATE_KEY");
    private Web3Config() {
    }
}
