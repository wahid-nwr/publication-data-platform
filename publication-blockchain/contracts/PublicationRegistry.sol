// SPDX-License-Identifier: MIT
pragma solidity ^0.8.20;

contract PublicationRegistry {
    struct Record {
        bytes32 hash;
        uint256 timestamp;
    }

    mapping(string => Record) public publicationRecords;

    function storeHash(string memory publicationId, bytes32 hash) public {
        publicationRecords[publicationId] = Record(hash, block.timestamp);
    }

    function getHash(string memory publicationId) public view returns (bytes32, uint256) {
        Record memory rec = publicationRecords[publicationId];
        return (rec.hash, rec.timestamp);
    }
}
