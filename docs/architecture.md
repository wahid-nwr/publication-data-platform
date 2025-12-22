flowchart LR
%% External Inputs
CSV[CSV Files<br/>Books / Magazines / Authors]

    %% CSV Producer
    subgraph CSV_Producer[csv-producer]
        CSV --> Parser[Streaming CSV Parser]
        Parser --> DTOs[Publication DTOs]
    end

    %% Kafka
    subgraph Kafka[kafka-services]
        DTOs --> Producer[Kafka Producer]
        Producer --> Topic[(Kafka Topics)]
    end

    %% Persister
    subgraph Persister[publication-persister]
        Topic --> Consumer[Kafka Consumer]
        Consumer --> Batch[Batch Processor]
        Batch --> JPA[JPA / Hibernate]
        JPA --> DB[(Database)]
    end

    %% Blockchain
    subgraph Blockchain[publication-blockchain]
        Batch --> Canon[Canonicalizer]
        Canon --> Hash[SHA-256 Hash]
        Hash --> Contract[(Ethereum / Sepolia)]
    end

    %% Security
    subgraph Security[publication-security]
        ADC[Application Default Credentials]
        Secrets[Secret Manager]
    end

    %% Cloud Runtime
    subgraph Cloud[Google Cloud]
        CloudRun[Cloud Run Service]
        Artifact[Artifact Registry]
        IAM[IAM Service Account]
    end

    %% Connections
    CloudRun --> Persister
    CloudRun --> Kafka
    CloudRun --> Security
    Artifact --> CloudRun
    IAM --> CloudRun
    Secrets --> CloudRun
    ADC --> CloudRun
