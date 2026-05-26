import { defineConfig } from 'vitest/config';

export default defineConfig({
  test: {
    include: ['**/*.test.ts'],
    exclude: ['node_modules/**', 'dist/**', '**/*.integration.test.ts'],
    environment: 'node',
    coverage: {
      reporter: ['text', 'lcov'],
      include: ['care-engine/**/*.ts', 'src/**/*.ts'],
    },
  },
});
