// HTTP entry point for the PlantApp Fastify API. The app itself is built by buildApp() in app.ts
// (also used in-process by tests via app.inject()); this file is the only place that .listen()s.
// Binds 0.0.0.0 by default so a LAN device can reach it; HOST/PORT are env-overridable.
import { buildApp } from './app.js';

async function main(): Promise<void> {
  const host = process.env.HOST ?? '0.0.0.0';
  const port = Number(process.env.PORT ?? 3000);
  const app = await buildApp();
  await app.listen({ host, port });
  console.log(`PlantApp API listening on http://${host}:${port}`);
}

main().catch((err) => {
  console.error('Failed to start PlantApp API:', err);
  process.exit(1);
});
