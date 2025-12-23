# 📚 Publication Management System

A **cloud-native Java publication processing platform** built around CSV ingestion, secure persistence, event streaming, and blockchain anchoring.

> This project started as a Java CSV coding challenge and evolved into a **production-style system** demonstrating modern backend architecture, cloud deployment, and secure configuration practices.

---

## 🚀 Overview

The system ingests **large CSV datasets** (authors, books, magazines), processes them in batches, persists them to a database, publishes events, and optionally anchors content hashes on blockchain.

The same Java codebase runs **locally, in Docker, and on Google Cloud Run** without modification.

Take a look at the adr documents [here](./docs/adr/ADR-index.md)

---

## 🧱 Architecture

[`Architecture`](./docs/architecture.md)
```text
                ┌────────────────────┐
                │   CSV Files        │
                │ (Authors, Books,   │
                │  Magazines)        │
                └─────────┬──────────┘
                          │
                          ▼
                ┌────────────────────┐
                │ CSV Parser (DTO)   │
                │ OpenCSV            │
                └─────────┬──────────┘
                          │
                          ▼
                ┌────────────────────┐
                │ Batch Processor    │
                │ (Streaming / MT)   │
                └─────────┬──────────┘
                          │
        ┌─────────────────┼─────────────────┐
        ▼                 ▼                 ▼
┌──────────────┐  ┌────────────────┐  ┌──────────────────┐
│ Database     │  │ Kafka / Events │  │ Blockchain Hash  │
│ (JPA)        │  │ (Confluent)    │  │ (Sepolia / EVM)  │
└──────────────┘  └────────────────┘  └──────────────────┘
                          │
                          ▼
                ┌────────────────────┐
                │ Cloud Run Service  │
                │ (Secure, Scalable) │
                └────────────────────┘
