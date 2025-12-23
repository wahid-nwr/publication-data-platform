# Architecture Decision Record (ADR) Index

This file lists all ADRs used in the **Publication Data Platform**.

Each ADR captures an important architectural decision, its context, rationale, alternatives, and consequences.

---

## 📑 ADR Index

| ADR ID     | Title                                          | Module / Scope                        | Status      |
|------------|------------------------------------------------|----------------------------------------|-------------|
| ADR-0001   | Overall System Architecture                    | Cross-cutting                          | Accepted    |
| ADR-0002   | Publication Domain Model Design                | publication-model                     | Accepted    |
| ADR-0003   | CSV Ingestion & Streaming Strategy             | csv-producer                          | Accepted    |
| ADR-0004   | Persistence & Batch Processing Strategy        | publication-persister                 | Accepted    |
| ADR-0005   | Event Streaming with Kafka                     | kafka-services                        | Accepted    |
| ADR-0006   | Authentication & Security Architecture         | publication-security                  | Accepted    |
| ADR-0007   | Blockchain Anchoring for Data Integrity        | publication-blockchain                | Accepted    |

---

## 📁 ADR Locations

Module | ADR Path
------ | --------
Cross-cutting | [`docs/adr/ADR-0001.md`](./ADR-001.md)
publication-model | [`publication-model/docs/adr/ADR-0002.md`](../../publication-model/docs/adr/ADR-0002.md)
csv-producer | [`csv-producer/docs/adr/ADR-0003.md`](../../csv-producer/docs/adr/ADR-0003.md)
publication-persister | [`publication-persister/docs/adr/ADR-0004.md`](../../publication-persister/docs/adr/ADR-0004.md)
kafka-services | [`kafka-services/docs/adr/ADR-0005.md`](../../kafka-services/docs/adr/ADR-0005.md)
publication-security | [`publication-security/docs/adr/ADR-0006.md`](../../publication-security/docs/adr/ADR-0006.md)
publication-blockchain | [`publication-blockchain/docs/adr/ADR-0007.md`](../../publication-blockchain/docs/adr/ADR-0007.md)

---

## 🧠 How to use this index

1. Start with **ADR-0001** to understand the overall architectural direction.
2. Refer to module-specific ADRs for implementation rationale.
3. Link ADRs from code or docs when referencing decisions in commits or PRs.

---

## 📝 ADR Conventions

- File names are `ADR-XXXX.md`.
- Each ADR contains:
  - Status (Proposed / Accepted / Superseded)
  - Date
  - Context
  - Decision
  - Rationale
  - Alternatives
  - Consequences

---

*Maintained by the Architecture Team — publication-data-platform*

