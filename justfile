# PlantApp root task runner. Requires `just` (https://github.com/casey/just).
#
# Each target delegates to the backend or Android module that owns the work.
# Targets are intentionally thin: the canonical commands live in
# backend/package.json scripts and Android Gradle tasks.

# Default: list available targets.
default:
    @just --list

# ---------------------------------------------------------------------------
# Lint
# ---------------------------------------------------------------------------

lint: lint-backend lint-android

lint-backend:
    cd backend && npm run lint

lint-android:
    cd android && ./gradlew lint

# ---------------------------------------------------------------------------
# Tests
# ---------------------------------------------------------------------------

test: test-backend test-android

test-backend:
    cd backend && npm test

test-backend-int:
    cd backend && npm run test:int

test-android:
    cd android && ./gradlew test

# ---------------------------------------------------------------------------
# Typecheck (compile-only checks)
# ---------------------------------------------------------------------------

typecheck: typecheck-backend typecheck-android

typecheck-backend:
    cd backend && npm run typecheck

typecheck-android:
    cd android && ./gradlew compileDebugKotlin

# ---------------------------------------------------------------------------
# Build
# ---------------------------------------------------------------------------

build: build-backend build-android

build-backend:
    cd backend && npm run build

build-android:
    cd android && ./gradlew assembleDebug

# ---------------------------------------------------------------------------
# Schema validation
# ---------------------------------------------------------------------------
# Compiles every shared JSON Schema with Ajv (2020-12 mode). Fails on the
# first schema that does not compile. Driven from the backend tree because
# Ajv is installed there.
validate-schemas:
    cd backend && npm run validate-schemas
