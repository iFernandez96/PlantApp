# Slice 3 — Deterministic watering reminders (implementation plan)

## Goal

Local, deterministic watering **reminders**: surface a device notification at a pending
`CareTask`'s `dueAt`. The care decision (`dueAt`) is already produced by the backend care engine
(**D-09**: Android computes no care logic). Slice 3 only decides *reminder scheduling* and
*delivers* the local notification on-device.

## Scope posture (relaxes Slice-1/2 exclusion)

Slices 1–2 deliberately excluded notifications (**D-11/D-12**: no notification permission yet).
That exclusion was **slice-scoped** — Slice 3 is where local reminders land. Push/FCM remains out
of scope here.

## In scope

- **(a)** Deterministic `computeReminders` policy — pure `:domain` Kotlin. **[this handoff]**
- **(b)** A WorkManager **local** notification path: a `Worker` that posts a reminder notification
  + a scheduler that enqueues work from `computeReminders`. Adds the **WorkManager** dependency and
  the **`POST_NOTIFICATIONS`** permission (Android 13+) and a notification channel.
- **(c)** Wiring to (re)schedule reminders on app open / after task changes.

## Out of scope / STOP gates

- **Firebase/FCM push is deferred.** When reminders need server-triggered push, **STOP and ask the
  owner** for a Firebase project + `google-services.json` before any push work.
- No weather or feeding reminders (later slices).
- No background location.

## Red-first sequence

1. **`computeReminders`** — pure, deterministic, `:domain`, dep-free, JVM-tested. **[this handoff]**
2. **WorkManager Worker + scheduler** — `:data`/`:feature`; new WorkManager dep + `POST_NOTIFICATIONS`
   permission + notification channel. The planner will surface the dependency + permission as an
   explicit decision and ground it against the manifest first.
3. **App-open scheduling** — (re)enqueue from `computeReminders` on app open / after task changes.

Each step is red-first and standalone-verified.

## Ratified decision — D-13 (reminder scheduling vs. care computation)

Local reminder **scheduling/delivery is an on-device concern** (WorkManager + a local
notification), while care **computation** (the `dueAt` schedule itself) **stays in the backend**
(**D-09 preserved**). **Server-triggered FCM push remains a later, owner-gated path.** This split
is the owner-approved "WorkManager local path first, then STOP for Firebase/FCM" directive, recorded
here so the boundary is durable rather than implicit.

## Past-trigger note

With a non-zero `leadTime` (or a due-soon / just-past task inside the stale window),
`triggerAtUtc` may be **before** `now`. That is **intentional**: the WorkManager step treats a past
trigger as "fire immediately" (a legitimate case), **not** a bug.

## This handoff — `computeReminders`

`android/domain/src/main/kotlin/dev/plantapp/domain/reminder/ReminderPolicy.kt`:

```kotlin
data class ReminderSpec(
    val taskId: String,
    val kind: String,
    val dueAt: String,        // ISO-8601 UTC, echoed from the task
    val triggerAtUtc: String, // when the local reminder should fire (ISO-8601 UTC)
)

fun computeReminders(
    tasks: List<CareTask>,
    now: java.time.Instant,
    leadTime: java.time.Duration = java.time.Duration.ZERO,
    staleAfter: java.time.Duration = java.time.Duration.ofDays(7),
): List<ReminderSpec>
```

Pure and deterministic (no `Instant.now()` inside; `now` is injected). Emits one `ReminderSpec`
per task that is `status == "pending"` and not more than `staleAfter` past due;
`triggerAtUtc = dueAt - leadTime`. Output order follows input order. Tasks with an unparseable
`dueAt` are skipped (defensive). Not care computation — only local reminder timing (D-13).
