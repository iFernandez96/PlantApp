// Slice 1 backend API (Fastify). Endpoints:
//   POST /garden-spaces, POST /containers, POST /plants, GET /plants/:id/tasks
// The add-plant flow validates input, persists the plant_instance, calls the real
// care-engine, and persists the resulting CareTask. All DB access runs through the
// request-scoped Supabase client so RLS enforces ownership (ADR-0006).
import Fastify, { type FastifyInstance } from 'fastify';
import { randomUUID } from 'node:crypto';
import { computeInitialWaterTask } from '../care-engine/index.js';
import { loadConfig } from './config.js';
import { makeAuthHook } from './auth.js';
import { toGardenSpace, toContainer, toPlantInstance, toCareTask } from './mappers.js';

export async function buildApp(): Promise<FastifyInstance> {
  const config = loadConfig();
  const app = Fastify({ logger: false });
  const requireAuth = makeAuthHook(config);

  // POST /garden-spaces
  app.post(
    '/garden-spaces',
    {
      onRequest: requireAuth,
      schema: {
        body: {
          type: 'object',
          required: ['name', 'kind'],
          properties: {
            name: { type: 'string', minLength: 1, maxLength: 80 },
            kind: { type: 'string' },
            indoor: { type: 'boolean' },
            postalCode: { type: 'string' },
            countryCode: { type: 'string' },
          },
        },
      },
    },
    async (request, reply) => {
      const body = request.body as {
        name: string;
        kind: string;
        indoor?: boolean;
        postalCode?: string;
        countryCode?: string;
      };
      const row: Record<string, unknown> = {
        user_id: request.userId,
        name: body.name,
        kind: body.kind,
      };
      if (body.indoor !== undefined) row.indoor = body.indoor;
      if (body.postalCode !== undefined) row.postal_code = body.postalCode;
      if (body.countryCode !== undefined) row.country_code = body.countryCode;

      const { data, error } = await request.supabase
        .from('garden_spaces')
        .insert(row)
        .select()
        .single();
      if (error) return reply.code(400).send({ error: error.message });
      return reply.code(201).send(toGardenSpace(data as Record<string, unknown>));
    },
  );

  // POST /containers
  app.post(
    '/containers',
    {
      onRequest: requireAuth,
      schema: {
        body: {
          type: 'object',
          required: ['volumeLiters', 'material', 'drainage'],
          properties: {
            name: { type: 'string', maxLength: 80 },
            volumeLiters: { type: 'number', exclusiveMinimum: 0, maximum: 10000 },
            material: { type: 'string' },
            drainage: { type: 'string' },
            selfWatering: { type: 'boolean' },
            saucer: { type: 'boolean' },
            soilMix: { type: 'string', maxLength: 200 },
          },
        },
      },
    },
    async (request, reply) => {
      const body = request.body as {
        name?: string;
        volumeLiters: number;
        material: string;
        drainage: string;
        selfWatering?: boolean;
        saucer?: boolean;
        soilMix?: string;
      };
      const row: Record<string, unknown> = {
        user_id: request.userId,
        volume_liters: body.volumeLiters,
        material: body.material,
        drainage: body.drainage,
      };
      if (body.name !== undefined) row.name = body.name;
      if (body.selfWatering !== undefined) row.self_watering = body.selfWatering;
      if (body.saucer !== undefined) row.saucer = body.saucer;
      if (body.soilMix !== undefined) row.soil_mix = body.soilMix;

      const { data, error } = await request.supabase
        .from('containers')
        .insert(row)
        .select()
        .single();
      if (error) return reply.code(400).send({ error: error.message });
      return reply.code(201).send(toContainer(data as Record<string, unknown>));
    },
  );

  // POST /plants — add-plant → CareTask flow
  app.post(
    '/plants',
    {
      onRequest: requireAuth,
      schema: {
        body: {
          type: 'object',
          required: ['profileId', 'containerId', 'gardenSpaceId', 'growthStage'],
          properties: {
            profileId: { type: 'string', minLength: 1 },
            containerId: { type: 'string', format: 'uuid' },
            gardenSpaceId: { type: 'string', format: 'uuid' },
            growthStage: { type: 'string' },
            lastWateredAt: { type: 'string' },
            nickname: { type: 'string', maxLength: 80 },
            cultivar: { type: 'string', maxLength: 80 },
            placement: { type: 'string' },
          },
        },
      },
    },
    async (request, reply) => {
      const body = request.body as {
        profileId: string;
        containerId: string;
        gardenSpaceId: string;
        growthStage: string;
        lastWateredAt?: string;
        nickname?: string;
        cultivar?: string;
        placement?: string;
      };
      const supabase = request.supabase;

      // Validate referenced rows exist and are visible to the caller (RLS).
      const profileRes = await supabase
        .from('plant_profiles')
        .select('id, version, common_names, watering_profile, container_profile')
        .eq('id', body.profileId)
        .maybeSingle();
      if (profileRes.error) return reply.code(400).send({ error: profileRes.error.message });
      if (!profileRes.data) {
        return reply.code(400).send({ error: 'unknown profileId', field: 'profileId' });
      }

      const containerRes = await supabase
        .from('containers')
        .select('id, volume_liters')
        .eq('id', body.containerId)
        .maybeSingle();
      if (containerRes.error) return reply.code(400).send({ error: containerRes.error.message });
      if (!containerRes.data) {
        return reply.code(400).send({ error: 'unknown containerId', field: 'containerId' });
      }

      const spaceRes = await supabase
        .from('garden_spaces')
        .select('id')
        .eq('id', body.gardenSpaceId)
        .maybeSingle();
      if (spaceRes.error) return reply.code(400).send({ error: spaceRes.error.message });
      if (!spaceRes.data) {
        return reply.code(400).send({ error: 'unknown gardenSpaceId', field: 'gardenSpaceId' });
      }

      const profile = profileRes.data as {
        id: string;
        version: number;
        common_names: string[];
        watering_profile: { baseIntervalDays: number };
        container_profile: { recommendedMinLiters: number };
      };
      const container = containerRes.data as { id: string; volume_liters: number };

      const now = new Date().toISOString();
      const plantId = randomUUID();

      const plantRow: Record<string, unknown> = {
        id: plantId,
        user_id: request.userId,
        profile_id: body.profileId,
        container_id: body.containerId,
        garden_space_id: body.gardenSpaceId,
        growth_stage: body.growthStage,
        created_at: now,
      };
      if (body.lastWateredAt !== undefined) plantRow.last_watered_at = body.lastWateredAt;
      if (body.nickname !== undefined) plantRow.nickname = body.nickname;
      if (body.cultivar !== undefined) plantRow.cultivar = body.cultivar;
      if (body.placement !== undefined) plantRow.placement = body.placement;

      const plantInsert = await supabase.from('plant_instances').insert(plantRow).select().single();
      if (plantInsert.error) return reply.code(400).send({ error: plantInsert.error.message });
      const plant = plantInsert.data;

      const task = computeInitialWaterTask({
        id: randomUUID(),
        clockUtc: now,
        plant: {
          id: plantId,
          profileId: body.profileId,
          containerId: body.containerId,
          gardenSpaceId: body.gardenSpaceId,
          createdAt: now,
          ...(body.lastWateredAt !== undefined ? { lastWateredAt: body.lastWateredAt } : {}),
        },
        profile: {
          id: profile.id,
          version: profile.version,
          commonNames: profile.common_names,
          wateringProfile: { baseIntervalDays: profile.watering_profile.baseIntervalDays },
          containerProfile: {
            recommendedMinLiters: profile.container_profile.recommendedMinLiters,
          },
        },
        container: { id: container.id, volumeLiters: Number(container.volume_liters) },
        gardenSpace: { id: body.gardenSpaceId },
      });

      const taskInsert = await supabase
        .from('care_tasks')
        .insert({
          id: task.id,
          plant_instance_id: task.plantInstanceId,
          user_id: request.userId,
          kind: task.kind,
          due_at: task.dueAt,
          priority: task.priority,
          rationale: task.rationale,
          engine_version: task.engineVersion,
          inputs_hash: task.inputsHash,
          source_inputs: task.sourceInputs,
          status: task.status,
        })
        .select()
        .single();
      if (taskInsert.error) return reply.code(400).send({ error: taskInsert.error.message });

      return reply.code(201).send({ plant: toPlantInstance(plant as Record<string, unknown>), task });
    },
  );

  // GET /plants — list the caller's plants (RLS-scoped to own rows).
  app.get('/plants', { onRequest: requireAuth }, async (request, reply) => {
    const { data, error } = await request.supabase
      .from('plant_instances')
      .select('*')
      .order('created_at', { ascending: true });
    if (error) return reply.code(400).send({ error: error.message });
    return reply.code(200).send((data ?? []).map((r) => toPlantInstance(r as Record<string, unknown>)));
  });

  // GET /plants/:id — the caller's single plant; 404 if not visible/owned.
  app.get('/plants/:id', { onRequest: requireAuth }, async (request, reply) => {
    const { id } = request.params as { id: string };
    const { data, error } = await request.supabase
      .from('plant_instances')
      .select('*')
      .eq('id', id)
      .maybeSingle();
    if (error) return reply.code(400).send({ error: error.message });
    if (!data) return reply.code(404).send({ error: 'not_found' });
    return reply.code(200).send(toPlantInstance(data as Record<string, unknown>));
  });

  // GET /plants/:id/tasks
  app.get(
    '/plants/:id/tasks',
    { onRequest: requireAuth },
    async (request, reply) => {
      const { id } = request.params as { id: string };
      const { data, error } = await request.supabase
        .from('care_tasks')
        .select('*')
        .eq('plant_instance_id', id)
        .order('created_at', { ascending: true });
      if (error) return reply.code(400).send({ error: error.message });
      return reply.code(200).send((data ?? []).map((r) => toCareTask(r as Record<string, unknown>)));
    },
  );

  // DELETE /plants/:id — delete the caller's plant; 204 on success, 404 if not owned.
  // care_tasks rows cascade via the plant_instances → care_tasks ON DELETE CASCADE FK.
  app.delete('/plants/:id', { onRequest: requireAuth }, async (request, reply) => {
    const { id } = request.params as { id: string };
    const { data, error } = await request.supabase
      .from('plant_instances')
      .delete()
      .eq('id', id)
      .select('id');
    if (error) return reply.code(400).send({ error: error.message });
    if (!data || data.length === 0) return reply.code(404).send({ error: 'not_found' });
    return reply.code(204).send();
  });

  return app;
}
