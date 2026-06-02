// Fastify auth: verify the Supabase bearer token and attach a request-scoped Supabase
// client (initialized with that token) so every DB call runs as the user and Postgres
// RLS enforces ownership (ADR-0006).
import { createClient, type SupabaseClient } from '@supabase/supabase-js';
import type { FastifyReply, FastifyRequest } from 'fastify';
import type { ApiConfig } from './config.js';

declare module 'fastify' {
  interface FastifyRequest {
    userId: string;
    supabase: SupabaseClient;
  }
}

export function makeAuthHook(config: ApiConfig) {
  return async function authHook(request: FastifyRequest, reply: FastifyReply): Promise<void> {
    const header = request.headers.authorization;
    if (!header || !header.startsWith('Bearer ')) {
      await reply.code(401).send({ error: 'missing_bearer_token' });
      return;
    }
    const token = header.slice('Bearer '.length).trim();

    const supabase = createClient(config.supabaseUrl, config.supabaseAnonKey, {
      auth: { persistSession: false, autoRefreshToken: false },
      global: { headers: { Authorization: `Bearer ${token}` } },
    });

    const { data, error } = await supabase.auth.getUser(token);
    if (error || !data.user) {
      await reply.code(401).send({ error: 'invalid_token' });
      return;
    }

    request.userId = data.user.id;
    request.supabase = supabase;
  };
}
