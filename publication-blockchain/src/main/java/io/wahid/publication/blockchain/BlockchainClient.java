package io.wahid.publication.blockchain;

import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

import java.util.logging.Logger;

public class BlockchainClient {
    private static final Logger LOGGER = Logger.getLogger(BlockchainClient.class.getName());
    private final Web3j web3j;
    private final Credentials credentials;

    public BlockchainClient() {
        LOGGER.info("loading web3 configurations!");
        this.web3j = Web3j.build(new HttpService(Web3Config.RPC_URL));
        this.credentials = Credentials.create(Web3Config.PRIVATE_KEY);
        LOGGER.info("web3 config loading complete!");
    }

    public Web3j getWeb3j() {
        return web3j;
    }

    public Credentials getCredentials() {
        return credentials;
    }
}
