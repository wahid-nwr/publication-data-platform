import { ethers } from "hardhat";

async function main() {
  const address = "0xa149fa10c76a7b0CFfB41A185a03aEDeCA36bc61";

  const registry = await ethers.getContractAt(
    "PublicationRegistry",
    address
  );

  const publicationId = "pub-001";
  const hash =
    "0x" + "11".repeat(32); // dummy bytes32

  console.log("Storing hash...");
  const tx = await registry.storeHash(publicationId, hash);
  await tx.wait();

  console.log("Reading back...");
  const result = await registry.getHash(publicationId);

  console.log("Hash:", result[0]);
  console.log("Timestamp:", result[1].toString());
}

main().catch(console.error);
