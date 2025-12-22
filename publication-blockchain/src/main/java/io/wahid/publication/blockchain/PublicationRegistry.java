package io.wahid.publication.blockchain;

import io.reactivex.Flowable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.BaseEventResponse;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tuples.generated.Tuple2;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;

/**
 * <p>Auto generated code.
 * <p><strong>Do not modify!</strong>
 * <p>Please use the <a href="https://docs.web3j.io/command_line.html">web3j command line tools</a>,
 * or the org.web3j.codegen.SolidityFunctionWrapperGenerator in the 
 * <a href="https://github.com/LFDT-web3j/web3j/tree/main/codegen">codegen module</a> to update.
 *
 * <p>Generated with web3j version 1.7.0.
 */
@SuppressWarnings("rawtypes")
public class PublicationRegistry extends Contract {
    public static final String BINARY = "{\n"
            + "  \"_format\": \"hh-sol-artifact-1\",\n"
            + "  \"contractName\": \"PublicationRegistryV2\",\n"
            + "  \"sourceName\": \"contracts/PublicationRegistry.sol\",\n"
            + "  \"abi\": [\n"
            + "    {\n"
            + "      \"anonymous\": false,\n"
            + "      \"inputs\": [\n"
            + "        {\n"
            + "          \"indexed\": false,\n"
            + "          \"internalType\": \"string\",\n"
            + "          \"name\": \"publicationId\",\n"
            + "          \"type\": \"string\"\n"
            + "        },\n"
            + "        {\n"
            + "          \"indexed\": false,\n"
            + "          \"internalType\": \"bytes32\",\n"
            + "          \"name\": \"hash\",\n"
            + "          \"type\": \"bytes32\"\n"
            + "        },\n"
            + "        {\n"
            + "          \"indexed\": false,\n"
            + "          \"internalType\": \"uint256\",\n"
            + "          \"name\": \"timestamp\",\n"
            + "          \"type\": \"uint256\"\n"
            + "        }\n"
            + "      ],\n"
            + "      \"name\": \"PublicationStored\",\n"
            + "      \"type\": \"event\"\n"
            + "    },\n"
            + "    {\n"
            + "      \"inputs\": [\n"
            + "        {\n"
            + "          \"internalType\": \"string\",\n"
            + "          \"name\": \"publicationId\",\n"
            + "          \"type\": \"string\"\n"
            + "        }\n"
            + "      ],\n"
            + "      \"name\": \"getRecord\",\n"
            + "      \"outputs\": [\n"
            + "        {\n"
            + "          \"internalType\": \"bytes32\",\n"
            + "          \"name\": \"hash\",\n"
            + "          \"type\": \"bytes32\"\n"
            + "        },\n"
            + "        {\n"
            + "          \"internalType\": \"uint256\",\n"
            + "          \"name\": \"timestamp\",\n"
            + "          \"type\": \"uint256\"\n"
            + "        }\n"
            + "      ],\n"
            + "      \"stateMutability\": \"view\",\n"
            + "      \"type\": \"function\"\n"
            + "    },\n"
            + "    {\n"
            + "      \"inputs\": [\n"
            + "        {\n"
            + "          \"internalType\": \"bytes32\",\n"
            + "          \"name\": \"hash\",\n"
            + "          \"type\": \"bytes32\"\n"
            + "        }\n"
            + "      ],\n"
            + "      \"name\": \"isHashStored\",\n"
            + "      \"outputs\": [\n"
            + "        {\n"
            + "          \"internalType\": \"bool\",\n"
            + "          \"name\": \"\",\n"
            + "          \"type\": \"bool\"\n"
            + "        }\n"
            + "      ],\n"
            + "      \"stateMutability\": \"view\",\n"
            + "      \"type\": \"function\"\n"
            + "    },\n"
            + "    {\n"
            + "      \"inputs\": [\n"
            + "        {\n"
            + "          \"internalType\": \"string\",\n"
            + "          \"name\": \"publicationId\",\n"
            + "          \"type\": \"string\"\n"
            + "        },\n"
            + "        {\n"
            + "          \"internalType\": \"bytes32\",\n"
            + "          \"name\": \"hash\",\n"
            + "          \"type\": \"bytes32\"\n"
            + "        }\n"
            + "      ],\n"
            + "      \"name\": \"storeHash\",\n"
            + "      \"outputs\": [],\n"
            + "      \"stateMutability\": \"nonpayable\",\n"
            + "      \"type\": \"function\"\n"
            + "    }\n"
            + "  ],\n"
            + "  \"bytecode\": \"0x608060405234801561001057600080fd5b506106a2806100206000396000f3fe608060405234801561001057600080fd5b50600436106100415760003560e01c806311dd88451461004657806312a6f2301461007757806330c6caa4146100a7575b600080fd5b610060600480360381019061005b9190610316565b6100c3565b60405161006e929190610395565b60405180910390f35b610091600480360381019061008c91906103ea565b61011e565b60405161009e9190610432565b60405180910390f35b6100c160048036038101906100bc919061044d565b610148565b005b60008060008085856040516100d99291906104ec565b90815260200160405180910390206040518060400160405290816000820154815260200160018201548152505090508060000151816020015192509250509250929050565b60006001600083815260200190815260200160002060009054906101000a900460ff169050919050565b6000801b810361018d576040517f08c379a000000000000000000000000000000000000000000000000000000000815260040161018490610562565b60405180910390fd5b6001600082815260200190815260200160002060009054906101000a900460ff16156101ee576040517f08c379a00000000000000000000000000000000000000000000000000000000081526004016101e5906105ce565b60405180910390fd5b604051806040016040528082815260200142815250600084846040516102159291906104ec565b90815260200160405180910390206000820151816000015560208201518160010155905050600180600083815260200190815260200160002060006101000a81548160ff0219169083151502179055507fecfbfe8f27308a9dc537fb13d731dbbb58303324edb1efdc8381cfd4331623638383834260405161029a949392919061062c565b60405180910390a1505050565b600080fd5b600080fd5b600080fd5b600080fd5b600080fd5b60008083601f8401126102d6576102d56102b1565b5b8235905067ffffffffffffffff8111156102f3576102f26102b6565b5b60208301915083600182028301111561030f5761030e6102bb565b5b9250929050565b6000806020838503121561032d5761032c6102a7565b5b600083013567ffffffffffffffff81111561034b5761034a6102ac565b5b610357858286016102c0565b92509250509250929050565b6000819050919050565b61037681610363565b82525050565b6000819050919050565b61038f8161037c565b82525050565b60006040820190506103aa600083018561036d565b6103b76020830184610386565b9392505050565b6103c781610363565b81146103d257600080fd5b50565b6000813590506103e4816103be565b92915050565b600060208284031215610400576103ff6102a7565b5b600061040e848285016103d5565b91505092915050565b60008115159050919050565b61042c81610417565b82525050565b60006020820190506104476000830184610423565b92915050565b600080600060408486031215610466576104656102a7565b5b600084013567ffffffffffffffff811115610484576104836102ac565b5b610490868287016102c0565b935093505060206104a3868287016103d5565b9150509250925092565b600081905092915050565b82818337600083830152505050565b60006104d383856104ad565b93506104e08385846104b8565b82840190509392505050565b60006104f98284866104c7565b91508190509392505050565b600082825260208201905092915050565b7f496e76616c696420686173680000000000000000000000000000000000000000600082015250565b600061054c600c83610505565b915061055782610516565b602082019050919050565b6000602082019050818103600083015261057b8161053f565b9050919050565b7f4475706c69636174652068617368000000000000000000000000000000000000600082015250565b60006105b8600e83610505565b91506105c382610582565b602082019050919050565b600060208201905081810360008301526105e7816105ab565b9050919050565b6000601f19601f8301169050919050565b600061060b8385610505565b93506106188385846104b8565b610621836105ee565b840190509392505050565b600060608201905081810360008301526106478186886105ff565b9050610656602083018561036d565b6106636040830184610386565b9594505050505056fea2646970667358221220224c1dc9cc95490ab85859012eca8b0dc8c085e5e447066c8d0b96fe2d86310464736f6c63430008140033\",\n"
            + "  \"deployedBytecode\": \"0x608060405234801561001057600080fd5b50600436106100415760003560e01c806311dd88451461004657806312a6f2301461007757806330c6caa4146100a7575b600080fd5b610060600480360381019061005b9190610316565b6100c3565b60405161006e929190610395565b60405180910390f35b610091600480360381019061008c91906103ea565b61011e565b60405161009e9190610432565b60405180910390f35b6100c160048036038101906100bc919061044d565b610148565b005b60008060008085856040516100d99291906104ec565b90815260200160405180910390206040518060400160405290816000820154815260200160018201548152505090508060000151816020015192509250509250929050565b60006001600083815260200190815260200160002060009054906101000a900460ff169050919050565b6000801b810361018d576040517f08c379a000000000000000000000000000000000000000000000000000000000815260040161018490610562565b60405180910390fd5b6001600082815260200190815260200160002060009054906101000a900460ff16156101ee576040517f08c379a00000000000000000000000000000000000000000000000000000000081526004016101e5906105ce565b60405180910390fd5b604051806040016040528082815260200142815250600084846040516102159291906104ec565b90815260200160405180910390206000820151816000015560208201518160010155905050600180600083815260200190815260200160002060006101000a81548160ff0219169083151502179055507fecfbfe8f27308a9dc537fb13d731dbbb58303324edb1efdc8381cfd4331623638383834260405161029a949392919061062c565b60405180910390a1505050565b600080fd5b600080fd5b600080fd5b600080fd5b600080fd5b60008083601f8401126102d6576102d56102b1565b5b8235905067ffffffffffffffff8111156102f3576102f26102b6565b5b60208301915083600182028301111561030f5761030e6102bb565b5b9250929050565b6000806020838503121561032d5761032c6102a7565b5b600083013567ffffffffffffffff81111561034b5761034a6102ac565b5b610357858286016102c0565b92509250509250929050565b6000819050919050565b61037681610363565b82525050565b6000819050919050565b61038f8161037c565b82525050565b60006040820190506103aa600083018561036d565b6103b76020830184610386565b9392505050565b6103c781610363565b81146103d257600080fd5b50565b6000813590506103e4816103be565b92915050565b600060208284031215610400576103ff6102a7565b5b600061040e848285016103d5565b91505092915050565b60008115159050919050565b61042c81610417565b82525050565b60006020820190506104476000830184610423565b92915050565b600080600060408486031215610466576104656102a7565b5b600084013567ffffffffffffffff811115610484576104836102ac565b5b610490868287016102c0565b935093505060206104a3868287016103d5565b9150509250925092565b600081905092915050565b82818337600083830152505050565b60006104d383856104ad565b93506104e08385846104b8565b82840190509392505050565b60006104f98284866104c7565b91508190509392505050565b600082825260208201905092915050565b7f496e76616c696420686173680000000000000000000000000000000000000000600082015250565b600061054c600c83610505565b915061055782610516565b602082019050919050565b6000602082019050818103600083015261057b8161053f565b9050919050565b7f4475706c69636174652068617368000000000000000000000000000000000000600082015250565b60006105b8600e83610505565b91506105c382610582565b602082019050919050565b600060208201905081810360008301526105e7816105ab565b9050919050565b6000601f19601f8301169050919050565b600061060b8385610505565b93506106188385846104b8565b610621836105ee565b840190509392505050565b600060608201905081810360008301526106478186886105ff565b9050610656602083018561036d565b6106636040830184610386565b9594505050505056fea2646970667358221220224c1dc9cc95490ab85859012eca8b0dc8c085e5e447066c8d0b96fe2d86310464736f6c63430008140033\",\n"
            + "  \"linkReferences\": {},\n"
            + "  \"deployedLinkReferences\": {}\n"
            + "}\n";

    private static String librariesLinkedBinary;

    public static final String FUNC_GETRECORD = "getRecord";

    public static final String FUNC_ISHASHSTORED = "isHashStored";

    public static final String FUNC_STOREHASH = "storeHash";

    public static final Event PUBLICATIONSTORED_EVENT = new Event("PublicationStored", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}, new TypeReference<Bytes32>() {}, new TypeReference<Uint256>() {}));
    ;

    @Deprecated
    protected PublicationRegistry(String contractAddress, Web3j web3j, Credentials credentials,
            BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected PublicationRegistry(String contractAddress, Web3j web3j, Credentials credentials,
            ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    @Deprecated
    protected PublicationRegistry(String contractAddress, Web3j web3j,
            TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    protected PublicationRegistry(String contractAddress, Web3j web3j,
            TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static List<PublicationStoredEventResponse> getPublicationStoredEvents(
            TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(PUBLICATIONSTORED_EVENT, transactionReceipt);
        ArrayList<PublicationStoredEventResponse> responses = new ArrayList<PublicationStoredEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            PublicationStoredEventResponse typedResponse = new PublicationStoredEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.publicationId = (String) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.hash = (byte[]) eventValues.getNonIndexedValues().get(1).getValue();
            typedResponse.timestamp = (BigInteger) eventValues.getNonIndexedValues().get(2).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static PublicationStoredEventResponse getPublicationStoredEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(PUBLICATIONSTORED_EVENT, log);
        PublicationStoredEventResponse typedResponse = new PublicationStoredEventResponse();
        typedResponse.log = log;
        typedResponse.publicationId = (String) eventValues.getNonIndexedValues().get(0).getValue();
        typedResponse.hash = (byte[]) eventValues.getNonIndexedValues().get(1).getValue();
        typedResponse.timestamp = (BigInteger) eventValues.getNonIndexedValues().get(2).getValue();
        return typedResponse;
    }

    public Flowable<PublicationStoredEventResponse> publicationStoredEventFlowable(
            EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getPublicationStoredEventFromLog(log));
    }

    public Flowable<PublicationStoredEventResponse> publicationStoredEventFlowable(
            DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(PUBLICATIONSTORED_EVENT));
        return publicationStoredEventFlowable(filter);
    }

    public RemoteFunctionCall<Tuple2<byte[], BigInteger>> getRecord(String publicationId) {
        final Function function = new Function(FUNC_GETRECORD, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Utf8String(publicationId)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bytes32>() {}, new TypeReference<Uint256>() {}));
        return new RemoteFunctionCall<Tuple2<byte[], BigInteger>>(function,
                new Callable<Tuple2<byte[], BigInteger>>() {
                    @Override
                    public Tuple2<byte[], BigInteger> call() throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple2<byte[], BigInteger>(
                                (byte[]) results.get(0).getValue(), 
                                (BigInteger) results.get(1).getValue());
                    }
                });
    }

    public RemoteFunctionCall<Boolean> isHashStored(byte[] hash) {
        final Function function = new Function(FUNC_ISHASHSTORED, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes32(hash)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}));
        return executeRemoteCallSingleValueReturn(function, Boolean.class);
    }

    public RemoteFunctionCall<TransactionReceipt> storeHash(String publicationId, byte[] hash) {
        final Function function = new Function(
                FUNC_STOREHASH, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Utf8String(publicationId), 
                new org.web3j.abi.datatypes.generated.Bytes32(hash)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    @Deprecated
    public static PublicationRegistry load(String contractAddress, Web3j web3j,
            Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new PublicationRegistry(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    @Deprecated
    public static PublicationRegistry load(String contractAddress, Web3j web3j,
            TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new PublicationRegistry(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static PublicationRegistry load(String contractAddress, Web3j web3j,
            Credentials credentials, ContractGasProvider contractGasProvider) {
        return new PublicationRegistry(contractAddress, web3j, credentials, contractGasProvider);
    }

    public static PublicationRegistry load(String contractAddress, Web3j web3j,
            TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return new PublicationRegistry(contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static RemoteCall<PublicationRegistry> deploy(Web3j web3j, Credentials credentials,
            ContractGasProvider contractGasProvider) {
        return deployRemoteCall(PublicationRegistry.class, web3j, credentials, contractGasProvider, getDeploymentBinary(), "");
    }

    @Deprecated
    public static RemoteCall<PublicationRegistry> deploy(Web3j web3j, Credentials credentials,
            BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(PublicationRegistry.class, web3j, credentials, gasPrice, gasLimit, getDeploymentBinary(), "");
    }

    public static RemoteCall<PublicationRegistry> deploy(Web3j web3j,
            TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return deployRemoteCall(PublicationRegistry.class, web3j, transactionManager, contractGasProvider, getDeploymentBinary(), "");
    }

    @Deprecated
    public static RemoteCall<PublicationRegistry> deploy(Web3j web3j,
            TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(PublicationRegistry.class, web3j, transactionManager, gasPrice, gasLimit, getDeploymentBinary(), "");
    }

    public static void linkLibraries(List<Contract.LinkReference> references) {
        librariesLinkedBinary = linkBinaryWithReferences(BINARY, references);
    }

    private static String getDeploymentBinary() {
        if (librariesLinkedBinary != null) {
            return librariesLinkedBinary;
        } else {
            return BINARY;
        }
    }

    public static class PublicationStoredEventResponse extends BaseEventResponse {
        public String publicationId;

        public byte[] hash;

        public BigInteger timestamp;
    }
}
