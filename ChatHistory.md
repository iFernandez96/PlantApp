I have a garden with a passion fruit tree in a small 5 gallon barrel. I also have tomatoes and tomatillos. I also have strawberries and a few other plants. I want to build an android app that will help me 1: take good care of my garden in terms of watering and feeding and notify me when I need to water each and every plant as well as understand the seasons for them. It also needs to understand my local state and area. 2. Tell me what my plants need to be healthy (ex: my passionfruit plant needs a larger container to grow a lot more. My passion fruit plant is very small right now as I live in the upstairs of a condo with no access to the ground soils.). 3. I want the features to be scalable to be able to add a bunch of other plants. This app needs to be a full replacement for the other apps that do the same. Please do research on this. 4. I want to integrate AI agents to help fix my plant situation by taking photos and having an OpenAI ChatGPT agent look at it and give proper pointers to get it better. 5. I also want recommendations on how to organize my space for the best and more optimal (efficient) way to structure my plants for best sunlight, use of space. This needs to be a feature in the app where I take a photo and it gets uploaded to the app and sent to the AI agent. Also keep in mind the ability to scale vertically and not just horizontally. 

Do not do anything yet. Make recommendations for the app about tech stack, structure, and how I can use claude code to plan out the infrastructure eventually implement everything starting with BDD and the software development life cycle. What software engineering disciplines should I use? Make recommendations on everything as well as more features.

I’ll ground the recommendations in product strategy, architecture, AI-agent design, plant-care data modeling, BDD/SDLC, current docs, and comparable garden apps.

A key distinction is emerging: build reliable plant-care rules and observations as the core, then use AI for diagnosis, explanations, photo triage, and space-layout guidance.
Recommended direction

Build this as a container-garden intelligence app, not just a watering-reminder app. The main differentiator should be: local climate + plant-specific care + container/space constraints + AI photo guidance + vertical-space planning.

The existing app baseline is already strong: Planta advertises per-plant care reminders for watering, fertilizer, seasonal care, pruning, repotting, misting, and AI diagnosis; Blossom offers photo ID, care reminders, repot/fertilizer reminders, and disease identification; PictureThis focuses heavily on plant identification and plant-health guidance from photos. Your app needs to beat them by being more specific to real small-space gardening: balconies, patios, condos, containers, trellises, vertical racks, local weather, hardiness zone, and plant-by-plant feedback loops.
Product concept

The app should be organized around your actual garden space, not just a list of plants.

Each user should have one or more Garden Spaces: balcony, patio, window ledge, upstairs condo area, railing area, vertical shelf, hanging basket zone, grow-light shelf, etc. Each space should store location, sunlight exposure, direction, wind exposure, shade, photos, measurements, and whether rain reaches the plants.

Each plant should be a Plant Instance, not just a species. For example, “Passion fruit in 5-gallon barrel on west balcony” is different from “Passion fruit in 25-gallon container on south patio.” The care engine needs to know species, container size, soil mix, age, trellis/support, current size, last watered, last fertilized, recent photos, and microclimate.

Your passion fruit example is exactly the kind of reasoning the app should support. Passion fruit is a vigorous vine normally trained on strong supports, with significant water needs and frequent balanced fertilization during the growing season; in a small 5-gallon container, the app should flag container restriction, support needs, drying risk, and limited fruiting potential rather than just saying “water every X days.”
Core features to build
1. Plant inventory and garden map

The first feature should be a structured inventory:
Entity	What it tracks
Garden space	Balcony/patio/window, direction, ZIP/location, photos, light, wind, shade, dimensions
Plant instance	Species, cultivar, nickname, age, container, soil, support, growth stage, photos
Container	Size, material, drainage, saucer, self-watering, soil volume
Placement	Shelf/railing/floor/hanging, height, sun exposure, neighboring plants
Care plan	Watering, feeding, pruning, repotting, trellising, seasonal tasks
Observation	Photos, notes, leaf color, pests, flowers, fruit, harvests
Diagnosis	AI observations, confidence, suspected causes, recommendations, follow-up tasks

The app should support many plants by being data-driven. Do not hard-code “tomato,” “tomatillo,” “strawberry,” and “passion fruit.” Instead, create a plant-profile system with species-level defaults and instance-level overrides.
2. Watering and feeding engine

This should be a deterministic rules engine with AI explanations layered on top. Do not let an LLM independently decide watering schedules.

The care engine should calculate reminders from:

    Plant species and cultivar.

    Container size and material.

    Soil/potting mix.

    Recent weather.

    Sun exposure.

    Growth stage.

    Last watered/fertilized.

    Whether the plant is flowering or fruiting.

    User feedback: “soil was still wet,” “plant wilted,” “skipped,” “watered early.”

    Optional sensor data.

This matters because container-grown plants behave differently than in-ground plants. Container tomatoes, for example, need more frequent fertilization because they have less soil volume; Iowa State Extension recommends soluble fertilizer every week or two for container tomatoes. UC guidance for container gardening also notes that fertilizer is lost as containers are watered, so light, more frequent fertilization is often better.

The app should also support care dependencies. Tomatillos are a good example: Utah State Extension notes that tomatillos are not self-fruitful and recommends at least two plants. That means the app should not only say “water tomatillo”; it should say “you may need another compatible tomatillo nearby for reliable fruiting.”

For strawberries, the app should support container-specific disease scouting, feeding after harvest flushes, and regular photo/leaf checks; Iowa State Extension recommends fertilizing container strawberries two to three times during the growing season and removing diseased leaves or fruit promptly.
3. Local climate and season intelligence

The app should ask for either a ZIP code or approximate location. Avoid requiring background location unless truly necessary.

Use:

    USDA Plant Hardiness Zone Map for cold-hardiness context. The USDA tool lets users find a zone by ZIP code or map click.

    National Weather Service API for U.S. forecasts, alerts, and observations.

    Open-Meteo for weather forecasts, historical weather, and global support; it provides forecast and historical APIs with no API key for many use cases.

    Local university extension content for planting dates, pests, fertilizing, and seasonal advice.

Important: USDA zone alone is not enough. It tells you winter low-temperature survival, not whether your balcony gets 7 hours of reflected afternoon heat, wind, or shade. Your app should combine hardiness zone + forecast + microclimate + container size.
4. AI plant-health photo agent

The AI flow should work like this:

    User takes photos: whole plant, top leaves, underside of leaves, stem/base, soil surface, container, surrounding space.

    Android app uploads photos to backend storage.

    Backend sends the images plus plant context to the OpenAI API.

    The AI returns structured JSON: likely issues, confidence, observations, suggested actions, urgency, source citations, and follow-up questions.

    The app converts that into user-facing advice and care tasks.

Use OpenAI’s Responses API with image inputs for multimodal analysis and Structured Outputs so model responses follow a strict schema instead of free-form text.

The AI should never be presented as infallible. It should say things like:

    “Likely underwatering or heat stress; confidence: medium.”

    “Could also be root restriction because container is small.”

    “Take a photo of the underside of the leaves to check for mites.”

    “Do not fertilize until the plant is well-watered and not heat-stressed.”

5. AI space-optimization feature

This is one of your strongest differentiators.

The space planner should let the user take photos of the balcony or condo garden area, then ask for a few measurements:

    Width, depth, height.

    Railing height.

    Direction the space faces.

    Which areas get morning/noon/afternoon sun.

    Whether wind is strong.

    Weight concerns for shelves or large pots.

    Whether dripping water is allowed.

The AI should recommend:

    Tall/trellised plants at the back or side.

    Strawberries in railing planters, hanging baskets, or vertical towers if sun allows.

    Passion fruit on a trellis, wall, or railing-safe support.

    Tomatoes/tomatillos where they receive the most sun and have airflow.

    Vertical racks for smaller herbs and strawberries.

    Drip trays/saucers to protect condo surfaces.

    Access paths so the user can water, prune, and harvest.

The app should not rely on AI alone. It should use a constraint solver or rule engine underneath:
Constraint	Example
Sunlight	Tomatoes, tomatillos, strawberries, and passion fruit generally need strong sun
Height	Passion fruit needs a vertical support path
Container size	Large fruiting plants need larger containers
Weight	Balcony/condo floors and shelves may have limits
Access	User must reach plants for watering and pruning
Pollination	Tomatillos may need multiple plants nearby
Water runoff	Condo users need trays and drainage control
Wind	Tall plants and trellises need stability
Recommended tech stack
Android app

Use native Android with Kotlin + Jetpack Compose. Jetpack Compose is Google’s recommended modern Android UI toolkit, and Android’s architecture guidance supports a layered app with state flowing down and events flowing up.

Recommended Android stack:
Area	Recommendation
Language	Kotlin
UI	Jetpack Compose + Material 3
Architecture	Clean Architecture or MVVM/MVI with unidirectional data flow
Local database	Room
Local preferences	DataStore
Camera	CameraX
Background work	WorkManager for sync/recompute jobs
Notifications	Android notifications + FCM for server-triggered reminders
Dependency injection	Hilt
Networking	Ktor or Retrofit
Image upload	Backend-signed upload URLs
Offline support	Room cache + queued sync
Testing	JUnit/Kotest, MockK, Compose UI tests, instrumented tests

Room is a good fit because this app will handle non-trivial structured data and should remain useful offline. CameraX is the right starting point for the photo features, and WorkManager is appropriate for reliable background tasks that can survive app restarts.

Use Firebase Cloud Messaging for push notifications. FCM is Google’s cross-platform messaging solution for reliably sending notifications and data messages to client apps. For exact user-facing reminders, be careful with Android alarm APIs; Android’s docs note that exact alarms are meant for user-intentioned actions that must happen at a precise time. Most garden reminders can tolerate inexact scheduling.
Backend

My recommendation: Supabase/Postgres + serverless functions + object storage + FCM, with the option to move heavier AI jobs to Cloud Run or another container service later.
Backend area	Recommendation
Database	Postgres
Managed platform	Supabase
Auth	Supabase Auth or Firebase Auth
Photos	Supabase Storage or cloud object storage
Vector search	Supabase pgvector or OpenAI vector stores
API layer	Supabase Edge Functions for simple APIs; Cloud Run/FastAPI for heavier AI orchestration
Notifications	FCM
Scheduling	Cron/queue worker that recalculates care tasks
AI gateway	Backend-only service; never call OpenAI directly from Android
Admin tooling	Web dashboard for plant profiles, rules, sources, and prompt versions

Supabase is attractive here because it gives you Postgres, auth, storage, Edge Functions, realtime, and vector embeddings in one stack. Its pgvector integration supports embeddings and vector similarity search, which is useful for plant-care retrieval.

Firebase is also viable, especially for fast mobile development, but the domain model is relational: plants, containers, care tasks, photos, diagnoses, sources, spaces, and weather snapshots. Postgres is a better long-term fit for that.
AI architecture

Use OpenAI for the plant-health and space-planning features, but keep the app deterministic where it matters.

Recommended AI components:
Component	Purpose
Garden Triage Agent	Main coordinator
Vision Health Agent	Interprets plant photos
Care Schedule Agent	Explains the rule-engine output, not replacing it
Space Planner Agent	Recommends layout and vertical structure
Source/RAG Agent	Pulls extension articles and plant guides
Safety/Guardrail Layer	Blocks overconfident, unsafe, or unsupported advice
Structured Output Parser	Converts AI response into app-safe JSON
Evaluation Harness	Tests AI recommendations before release

The OpenAI Agents SDK is designed for applications that plan, call tools, collaborate across specialists, and maintain enough state for multi-step work. It also supports orchestration patterns like handoffs and agents-as-tools, which fits your “specialist agents” concept. The SDK’s tracing support is important because you will need to debug model outputs, tool calls, guardrails, and handoffs in production.

Use OpenAI file search/vector stores or Supabase pgvector for retrieval. OpenAI’s file search works with vector stores, while Supabase pgvector keeps embeddings inside Postgres.
Plant data sources

Use multiple sources, not one API.
Source type	Use
University extension articles	Most trustworthy care guidance
USDA hardiness zone	Location-based perennial hardiness
NWS/Open-Meteo	Weather and alerts
Perenual	Seed data for plant profiles, watering/sunlight/pest info
GBIF	Taxonomy and species validation
Your own curated database	Final source of truth for app behavior

Perenual offers plant species, care, sunlight, watering, hardiness, pest, and disease data, but you should treat it as seed data, not the sole authority. GBIF is better for taxonomy and biodiversity data than practical care scheduling.
Suggested system structure

Use a modular architecture from the beginning.
Android modules
Module	Responsibility
app	App shell, navigation
core-ui	Design system, reusable components
core-domain	Entities, use cases, domain rules
core-data	Repositories, local DB, API clients
feature-inventory	Plant list, add/edit plant
feature-care	Tasks, reminders, watering/feed logs
feature-camera	CameraX capture and uploads
feature-diagnosis	AI health results
feature-space-planner	Balcony/space optimization
feature-settings	Location, privacy, notifications
sync-worker	WorkManager jobs
Backend services
Service	Responsibility
Garden API	Users, spaces, plants, containers
Care Engine	Rules, reminders, task generation
Weather Service	NWS/Open-Meteo integration
AI Gateway	OpenAI calls, prompt templates, structured outputs
Knowledge Service	RAG, source retrieval, plant guide lookup
Notification Service	FCM push creation
Photo Service	Image storage, compression, metadata
Admin Console	Plant profiles, rules, prompts, source curation
BDD and SDLC approach

Use BDD first, but do not stop at BDD. BDD is useful because it turns behavior into shared, testable documentation; Cucumber describes BDD as a way to close the gap between business and technical people through shared understanding, small iterations, and automatically checked system behavior.

Start with user stories and scenarios before implementation.

Example BDD scenarios to create early:

Feature: Watering reminders for container plants

Scenario: Hot weather increases watering urgency
  Given I have a tomato in a 5 gallon container
  And the plant is on a west-facing balcony
  And the forecast high is above 90°F
  And I last watered it 2 days ago
  When the care engine recalculates tasks
  Then I should receive a "check soil moisture" reminder today
  And the app should explain that small containers dry faster in heat

Feature: Container restriction advice

Scenario: Passion fruit is in a small barrel
  Given I have a passion fruit vine in a 5 gallon container
  And the plant is tagged as fruiting-vine
  When I ask why it is growing slowly
  Then the app should flag possible root restriction
  And recommend a larger container or controlled pruning/trellising plan
  And include confidence and source-backed explanation

Feature: Tomatillo fruiting support

Scenario: User has only one tomatillo plant
  Given I have one tomatillo plant
  When the app creates the seasonal fruiting checklist
  Then it should recommend adding a second tomatillo plant for pollination

Feature: AI plant diagnosis

Scenario: User uploads photos of yellowing leaves
  Given I upload whole-plant and leaf-closeup photos
  And the app knows the plant species, container size, and watering history
  When the AI health agent analyzes the plant
  Then the app should return likely causes with confidence
  And create follow-up tasks
  And ask for missing photos if the diagnosis is uncertain

How to use Claude Code effectively

Use Claude Code as an engineering accelerator, not as an uncontrolled app generator. Claude Code can understand a codebase, work across files, run commands, make changes, and help automate development tasks.

Set up the repo so Claude Code has strong structure:

garden-app/
  docs/
    prd.md
    architecture.md
    domain-model.md
    privacy-threat-model.md
    ai-evaluation-plan.md
    adr/
  features/
    watering.feature
    feeding.feature
    photo-diagnosis.feature
    space-planner.feature
  android/
  backend/
  shared-schemas/
  prompts/
  evals/
  .claude/
    CLAUDE.md
    settings.json
    commands/
    agents/

Put durable project instructions in CLAUDE.md. Claude Code’s memory docs describe CLAUDE.md and auto memory as context loaded at the start of conversations; the more specific and concise the instructions, the more consistently Claude follows them.

Your CLAUDE.md should include:

    Use BDD before implementation.

    Write failing tests before production code.

    Never put OpenAI keys in Android.

    AI responses must use structured schemas.

    The care engine must be deterministic.

    LLMs may explain or triage, but not silently overwrite schedule rules.

    Every recommendation must expose confidence and source references where available.

    Use small vertical-slice PRs.

    Run tests before marking work complete.

    No database schema changes without migrations.

    No new feature without acceptance criteria.

Use Claude Code hooks to automate quality gates. Claude Code hooks can run shell commands, HTTP endpoints, or LLM prompts at lifecycle points; common uses include formatting files after edits, blocking unsafe commands, sending notifications, or injecting context.

Recommended hooks:
Hook	Purpose
After file edit	Run formatter
Before commit	Run unit tests
Before backend change	Check migrations
Before secrets file edit	Block accidental key exposure
Before AI prompt change	Run prompt/eval tests
After PR creation	Generate summary and test evidence

Use Claude Code GitHub integration for PR review, issue handling, and implementation assistance, but keep approvals human-controlled. Anthropic’s Claude Code Action can respond to PRs and issues and implement code changes in GitHub workflows.

For large tasks, use specialized Claude Code agents or sessions:
Claude Code role	Responsibility
Product analyst	PRD, user stories, feature scope
Android engineer	Compose screens, ViewModels, Room
Backend engineer	APIs, Postgres schema, Supabase functions
QA engineer	BDD scenarios, tests, edge cases
AI engineer	prompts, schemas, evals, RAG
Security reviewer	permissions, privacy, secrets, threat model
Horticulture reviewer	source quality, care-rule validation

Claude Code agent teams are especially useful when parallel exploration adds value, such as comparing architecture options, researching APIs, or reviewing different parts of a codebase.
Recommended SDLC roadmap
Phase 0: Discovery and scope

Deliverables:

    Competitor notes.

    User personas.

    Problem statement.

    PRD.

    Feature matrix.

    Risk list.

    First plant set: passion fruit, tomatoes, tomatillos, strawberries.

    Data-source plan.

    Privacy model.

Goal: decide what makes the app meaningfully better than existing apps.
Phase 1: Architecture and domain model

Deliverables:

    Domain model.

    Database schema.

    Android module structure.

    Backend architecture.

    AI architecture.

    Source-of-truth rules.

    BDD feature files.

    Architecture Decision Records.

Goal: make the app scalable before writing lots of screens.
Phase 2: MVP vertical slice

Build one complete flow:

    Add plant.

    Add container and location.

    Generate watering task.

    Notify user.

    User logs watering.

    Schedule adjusts.

    User sees explanation.

This proves the core product.
Phase 3: Local weather and seasons

Add:

    ZIP/location.

    USDA zone.

    Weather forecast.

    Heat/frost/wind alerts.

    Seasonal care calendar.

    Localized recommendations.

Phase 4: AI photo diagnosis

Add:

    CameraX capture.

    Photo upload.

    AI diagnosis.

    Structured JSON output.

    Confidence.

    Follow-up tasks.

    Source-backed explanation.

    AI evaluation set.

Phase 5: Space optimizer

Add:

    Garden-space photo upload.

    Measurements.

    Sun-direction inputs.

    Vertical layout suggestions.

    Trellis/shelf/container recommendations.

    Before/after plan view.

Phase 6: Scale and polish

Add:

    Admin console.

    Plant profile expansion.

    Sensor integrations.

    Community/expert review.

    Analytics.

    App Store/Play Store polish.

    Paid tier, if desired.

Software engineering disciplines to apply

Use these from the beginning:
Discipline	Why it matters
Requirements engineering	Prevents vague “AI garden app” scope creep
BDD	Turns care behavior into executable expectations
Domain-driven design	Keeps plants, containers, spaces, tasks, and diagnoses cleanly modeled
Clean architecture	Keeps Android UI, business rules, data, and AI separate
Test-driven development	Protects the care engine from regressions
Data engineering	Plant data, weather, observations, and sources must be structured
AI engineering	Prompts, schemas, evals, and guardrails need version control
MLOps/LLMOps	AI recommendations must be monitored, evaluated, and improved
Security engineering	Photos, location, and API keys need protection
Privacy engineering	Users are uploading home/balcony photos and location data
UX research	Gardeners need simple advice, not raw horticulture data
Mobile performance engineering	Battery, offline behavior, image upload, and notifications matter
DevOps/SRE	You need CI/CD, migrations, logs, alerts, and cost tracking
Accessibility	The app should support clear reminders, readable UI, and non-visual cues
Privacy and safety recommendations

Photos of a balcony or condo can reveal home layout, location, and personal property. Location and photos should be treated as sensitive data.

Use:

    Explicit consent for camera, photos, location, and notifications.

    ZIP code as default instead of continuous location.

    Backend-only OpenAI calls.

    Signed upload URLs.

    Encrypted storage where possible.

    Delete-photo option.

    Minimal data retention.

    Clear privacy policy.

    Google Play Data Safety disclosure.

    No background location unless absolutely necessary.

Android requires runtime permissions for sensitive capabilities such as location, camera, and notifications, and Google Play requires developers to disclose data collection and sharing in the Data Safety section.

OpenAI’s platform data controls state that API abuse-monitoring logs may be retained for up to 30 days by default, with zero data retention available for qualifying use cases. That should be reflected in your privacy policy if photos and plant data are sent to OpenAI.
Extra feature recommendations

High-value additions:

    Plant onboarding wizard: “What plant is this? Where is it? What size container?”

    Container upgrade planner: recommends next container size, soil volume, trellis needs.

    Care confidence score: “High confidence: water today” vs. “Low confidence: check soil first.”

    Photo timeline: compare plant growth week by week.

    Harvest tracker: tomatoes/tomatillos/strawberries yield logs.

    Problem history: pests, yellowing, wilting, leaf curl, blossom drop.

    Pollination assistant: especially useful for tomatillos and fruiting plants.

    Balcony weight warning: large containers and water are heavy.

    Runoff/drainage planner: important for condos.

    Trellis planner: passion fruit, tomatoes, cucumbers, beans.

    Heat wave mode: shade cloth, morning watering, anti-wilt guidance.

    Vacation mode: watering plan while away.

    Shopping/materials checklist: not retailer-first, just supplies.

    Sensor support: soil moisture, light meter, temperature, humidity.

    Expert mode: shows source citations and raw care logic.

    Beginner mode: simple “Do this today” experience.

    Plant-care experiments: track what worked.

    Calendar sync: Google Calendar export for major tasks.

    Seed packet scanner: extract planting dates, spacing, depth, harvest time.

    Companion and conflict planning: useful once garden size grows.

    AI follow-up questions: “Is the soil wet 2 inches down?” before giving strong advice.

Best first build

Start with a personal MVP for your own garden:

    Add passion fruit, tomato, tomatillo, strawberry.

    Add container size and balcony location.

    Generate watering and feeding tasks.

    Add local weather.

    Notify you.

    Let you log what happened.

    Let the app adjust based on feedback.

Do not start with the space optimizer or full AI diagnosis first. The care engine, data model, and reminders are the foundation. Once that is reliable, AI photos and layout optimization become powerful instead of gimmicky.
