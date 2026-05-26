# ADR-0004 — AI Boundaries

Date: 2026-05-26
Status: Proposed

## Context

We want AI to add real value (photo diagnosis, space optimization, plain-language explanations) without compromising:
- Determinism of care scheduling.
- Privacy of photos and location.
- Auditability of recommendations.
- Cost predictability.

## Decision

1. **Single AI caller.** Only the backend `ai-gateway` module calls LLM providers. The Android app never holds provider keys and never calls providers directly.
2. **AI never writes to the schedule.** AI may *suggest* actions; user acceptance flows through the deterministic care-engine API to create or modify tasks.
3. **Structured outputs only.** Every AI response is constrained by a JSON Schema in `shared-schemas/`. Free text is allowed only inside typed fields.
4. **Versioned prompts.** Each system prompt is a file in `prompts/` with frontmatter `version`, `lastReviewedAt`, `intendedModel`, `evalSuite`. Production rollouts require the eval suite to pass at recorded thresholds.
5. **Privacy posture.** Photos are downscaled and EXIF-stripped before reaching the gateway. Provider configured for zero retention where supported. No raw photo bytes or PII in logs.
6. **Confidence and refusal.** Outputs include confidence values; a typed `out_of_scope` result is required when the model cannot answer (no hallucinated fallback). On schema validation failure: one retry with stricter instructions, then a typed `schema_error` to the client.
7. **Cost controls.** Per-user daily quota, image-size caps, smaller-model-first when evals show parity.
8. **Provider agnosticism (eventually).** The gateway exposes a provider-neutral interface internally so a second provider can be A/B tested without API changes.

## Alternatives considered

- **Direct provider calls from Android.** Rejected: leaks keys, can't enforce schema, harder to audit and rate-limit.
- **Letting AI write to the schedule.** Rejected: incompatible with the deterministic-engine rule and explanation requirements.
- **Free-form text outputs.** Rejected: defeats structured downstream handling and evaluation.

## Consequences

- Slight latency added by validation + retry.
- Backend owns secrets, quotas, observability.
- AI features can be disabled at the gateway level without an app update.
