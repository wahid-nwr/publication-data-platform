package io.wahid.publication.blockchain;

import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

public class BlockchainClient {

    private final Web3j web3j;
    private final Credentials credentials;

    public BlockchainClient() {
        System.out.println("loading web3 configurations!");
        System.out.println("URL ----> " + Web3Config.RPC_URL);
        System.out.println("key->" + Web3Config.PRIVATE_KEY.substring(30));
        this.web3j = Web3j.build(new HttpService(Web3Config.RPC_URL));
        this.credentials = Credentials.create(Web3Config.PRIVATE_KEY);
        System.out.println("web3 config loading complete!");
    }

    public Web3j getWeb3j() {
        return web3j;
    }

    public Credentials getCredentials() {
        return credentials;
    }
}
