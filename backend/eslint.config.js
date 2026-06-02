// Flat config (ESLint 9). See https://eslint.org/docs/latest/use/configure/configuration-files for the format.
import tseslint from 'typescript-eslint';
import prettier from 'eslint-config-prettier';

export default tseslint.config(
  {
    ignores: ['dist/**', 'node_modules/**', 'coverage/**'],
  },
  ...tseslint.configs.recommended,
  {
    languageOptions: {
      parserOptions: {
        // Lint against tsconfig.eslint.json (extends the build tsconfig, widened to
        // cover tests + *.config.ts + this config) so every linted file resolves to a
        // TS program for typed linting. The build tsconfig's emitted scope is unchanged.
        project: ['./tsconfig.eslint.json'],
        tsconfigRootDir: import.meta.dirname,
      },
    },
    rules: {
      '@typescript-eslint/no-unused-vars': ['warn', { argsIgnorePattern: '^_', varsIgnorePattern: '^_' }],
    },
  },
  prettier,
);
