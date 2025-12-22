import { ethers } from "hardhat";

async function main() {
  console.log("Deploying PublicationRegistry...");

  const Factory = await ethers.getContractFactory("PublicationRegistryV2");
  const contract = await Factory.deploy();

  console.log("Tx sent, waiting for confirmation...");
  // ✅ ethers v5
  await contract.deployed();

  console.log("PublicationRegistry deployed to:", contract.address);
}

main().catch((error) => {
  console.error(error);
  process.exitCode = 1;
});
