package io.wahid.publication.blockchain;

import org.web3j.crypto.Hash;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tuples.generated.Tuple2;
import org.web3j.tx.gas.StaticGasProvider;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BlockchainService {

    private static final Logger LOGGER = Logger.getLogger(BlockchainService.class.getName());
    private final PublicationRegistry contract;

    public BlockchainService(BlockchainClient client) {
        String contractAddress = loadContractAddress();

        StaticGasProvider gasProvider = new StaticGasProvider(Convert.toWei("4.1",
                Convert.Unit.GWEI).toBigInteger(), BigInteger.valueOf(100_000));
        this.contract = PublicationRegistry.load(
                contractAddress,
                client.getWeb3j(),
                client.getCredentials(),
                gasProvider
        );
    }

    public TransactionReceipt storePublicationHash(String pubId, byte[] hashBytes) throws Exception {
        return contract.storeHash(pubId, Hash.sha3(hashBytes)).send();
    }

    public Boolean isHashStored(byte[] hashBytes) throws Exception {
        return contract.isHashStored(Hash.sha3(hashBytes)).send();
    }

    private String loadContractAddress() {
        String address = Secrets.getSecret("CONTRACT_ADDRESS_V2");
        if (!address.isBlank()) {
            return address;
        }
        LOGGER.log(Level.INFO, "env contact address not found");

        try (InputStream is = getClass().getClassLoader()
                .getResourceAsStream("contract-address.txt")) {
            if (is == null) {
                throw new IllegalStateException("contract-address.txt not found in resources");
            }
            LOGGER.log(Level.INFO, "file contact address found.");

            address = new BufferedReader(new InputStreamReader(is))
                    .lines()
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Contract address file is empty"));

            if (!address.startsWith("0x") || address.length() != 42) {
                throw new IllegalArgumentException("Invalid contract address: " + address);
            }

            return address;

        } catch (IOException e) {
            throw new RuntimeException("Failed to load contract address", e);
        }
    }

    public Tuple2<byte[], BigInteger> getStoredHash(String publicationId) throws Exception {
        LOGGER.log(Level.INFO, "Using contract address: {0}", contract.getContractAddress());
        return contract.getRecord(publicationId).send();
    }

    public Boolean verifyPublication(String id, String canonical) throws Exception {
        byte[] localHash = Hash.sha3(canonical.getBytes(StandardCharsets.UTF_8));

        // 3. Fetch stored hash from blockchain
        byte[] storedHash = getStoredHash(id).component1();
        LOGGER.log(Level.INFO, "Local hash:   0x{0}", Numeric.toHexStringNoPrefix(localHash));
        LOGGER.log(Level.INFO, "Chain hash:   0x{0}", Numeric.toHexStringNoPrefix(storedHash));
        boolean match = Arrays.equals(storedHash, Hash.sha3(localHash));
        LOGGER.log(Level.INFO, "MATCH = {0}", match);
        // 4. Compare
        return MessageDigest.isEqual(Hash.sha3(localHash), storedHash);
    }

    public boolean verifyPublicationHash(String pubId, byte[] hashBytes) throws Exception {
        Tuple2<byte[], BigInteger> stored = getStoredHash(pubId);
        byte[] storedHash = stored.component1();
        return Arrays.equals(storedHash, Hash.sha3(hashBytes));
    }
}
