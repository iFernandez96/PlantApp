// Shared helpers for the Slice 1 schema-validation tests.
// Loads JSON Schemas from ../../../shared-schemas/ and produces an Ajv 2020-12
// validator. No production behavior; test-only.
import { readFileSync } from 'node:fs';
import { dirname, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';
import Ajv2020 from 'ajv/dist/2020.js';
import addFormats from 'ajv-formats';
import type { ValidateFunction } from 'ajv';

const __dirname = dirname(fileURLToPath(import.meta.url));
export const SCHEMAS_DIR = resolve(__dirname, '../../../shared-schemas');

export function createAjv(): InstanceType<typeof Ajv2020> {
  const ajv = new Ajv2020({ allErrors: true, strict: true });
  addFormats(ajv);
  return ajv;
}

export function loadSchema(name: string): object {
  const raw = readFileSync(resolve(SCHEMAS_DIR, `${name}.schema.json`), 'utf8');
  return JSON.parse(raw) as object;
}

export function compileSchema(name: string): ValidateFunction {
  const ajv = createAjv();
  return ajv.compile(loadSchema(name));
}

/** Deep-clones a plain JSON-safe object so tests can mutate fixtures without polluting siblings. */
export function clone<T>(value: T): T {
  return JSON.parse(JSON.stringify(value)) as T;
}
