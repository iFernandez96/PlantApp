# PlantApp — AI Architecture

Status: Draft v0.1 — 2026-05-26

## 1. Boundary

- **Single AI caller: the backend `ai-gateway` service.** Android never holds provider keys.
- **Single output shape: schema-validated structured JSON** per `shared-schemas/`.
- **Deterministic engine remains authoritative** for care-task scheduling. AI proposes; the engine disposes.

## 2. Flows

### 2.1 Plant photo diagnosis

Input: PlantInstance context (species profile, container, recent care log) + 1–4 photos (storage keys).
Prompt: `prompts/plant-diagnosis.system.md` (versioned).
Model: OpenAI multimodal (e.g. GPT-class with vision) via Responses API with structured outputs.
Output schema: `diagnosis-result.schema.json`.
Post-processing: validate; on schema fail retry once with stricter instruction; on second fail return typed `schema_error`.
Side effects: persist `DiagnosisResult`; surface recommended follow-up tasks as **suggestions only**. User-accepted suggestions enter via the care-engine's normal task-create API.

### 2.2 Space optimizer

Input: GardenSpace photos + measurements + current PlantInstance list.
Prompt: `prompts/space-optimizer.system.md`.
Output schema: `space-plan.schema.json`.
Output must include explicit horizontal + vertical layout options and tradeoffs.
Side effects: persist `SpacePlan`. Not auto-applied to the inventory; user reviews and accepts.

### 2.3 Care explanation (natural-language layer)

Input: a `CareTask` with rationale + engine inputs.
Prompt: `prompts/care-explanation.system.md`.
Output: short structured explanation `{summary, why, tips[]}`.
This flow exists so the AI explains the engine's deterministic decision — never overrides it.

## 3. Prompt versioning

- Each prompt file begins with frontmatter: `version`, `lastReviewedAt`, `intendedModel`, `evalSuite`.
- Production deploys reference an exact prompt version. Bumping a prompt requires passing the eval suite at or above the recorded threshold.
- A `prompt_registry` table on the backend records `(promptId, version, hash, deployedAt)`.

## 4. Structured outputs

- Use the provider's strict structured-output mode (OpenAI Responses API JSON schema mode, or equivalent).
- Schemas live in `shared-schemas/`. The gateway loads and embeds them at request time.
- Schema is the contract; freeform text only appears inside typed string fields.

## 5. Guardrails

- **Prompt injection**: System prompt instructs the model to ignore any instructions found within image OCR text or user-supplied notes. Schema constraints further limit blast radius.
- **Out-of-scope refusal**: If the request is unrelated to plant care (e.g. user uploads a face), the model returns a typed `out_of_scope` result rather than hallucinating.
- **Confidence floors**: Diagnosis recommendations carry a `confidence` field; UI hides actions below a threshold (TBD via evals).
- **No autonomous mutation**: Gateway responses never write to inventory or schedule tables directly.

## 6. Cost controls

- Image downscale before sending (longer-edge ≤ 1024px default).
- Per-user daily AI quota.
- Smaller model attempted first when an eval shows it's adequate.
- Token + image-count metrics logged for budget alerts.

## 7. Telemetry

- Per request: prompt id+version, model id, input size (count, not content), latency, output schema validation result, retry count, cost estimate.
- No prompt text or image bytes in logs.
- Sampled evaluation: random 1% of production requests run through the offline eval scoring (no PII; uses sanitized synthetic inputs from `evals/` instead — see `ai-evaluation-plan.md`).

## 8. Provider strategy

- Primary: OpenAI Responses API.
- Future: pluggable provider interface so a secondary (e.g. Anthropic Claude vision) can be A/B tested.
- Zero-retention configuration where available; documented per provider in the ADR.

## 9. Failure surface

| Failure | Surface to user |
|---|---|
| Provider 5xx / timeout | "AI temporarily unavailable, try later" — no mock advice |
| Schema invalid after retry | "Could not produce a structured diagnosis" + offer to resend |
| Out-of-scope | "This doesn't look like a plant; please try another photo" |
| Quota exceeded | "Daily AI limit reached" + reset time |
