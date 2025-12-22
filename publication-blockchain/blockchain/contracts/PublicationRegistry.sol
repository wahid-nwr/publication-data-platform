// SPDX-License-Identifier: MIT
pragma solidity ^0.8.20;

contract PublicationRegistryV2 {
    struct Record {
        bytes32 hash;
        uint256 timestamp;
    }

    // publicationId → record
    mapping(string => Record) private publicationRecords;

    // hash → already stored?
    mapping(bytes32 => bool) private hashExists;

    event PublicationStored(
        string publicationId,
        bytes32 hash,
        uint256 timestamp
    );

    function storeHash(string calldata publicationId, bytes32 hash) external {
        require(hash != bytes32(0), "Invalid hash");
        require(!hashExists[hash], "Duplicate hash");

        publicationRecords[publicationId] = Record({
            hash: hash,
            timestamp: block.timestamp
        });

        hashExists[hash] = true;

        emit PublicationStored(publicationId, hash, block.timestamp);
    }

    function getRecord(string calldata publicationId)
        external
        view
        returns (bytes32 hash, uint256 timestamp)
    {
        Record memory r = publicationRecords[publicationId];
        return (r.hash, r.timestamp);
    }

    function isHashStored(bytes32 hash) external view returns (bool) {
        return hashExists[hash];
    }
}
