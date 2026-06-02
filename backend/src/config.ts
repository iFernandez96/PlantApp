// Runtime config for the backend API, read from the environment at app-build time.
// Local-dev values come from `supabase status -o env`; no secrets are committed.

export interface ApiConfig {
  supabaseUrl: string;
  supabaseAnonKey: string;
}

export function loadConfig(): ApiConfig {
  const supabaseUrl = process.env.SUPABASE_URL ?? process.env.API_URL;
  const supabaseAnonKey = process.env.SUPABASE_ANON_KEY ?? process.env.ANON_KEY;
  if (!supabaseUrl || !supabaseAnonKey) {
    throw new Error(
      'Missing SUPABASE_URL / SUPABASE_ANON_KEY (or API_URL / ANON_KEY) in the environment.',
    );
  }
  return { supabaseUrl, supabaseAnonKey };
}
