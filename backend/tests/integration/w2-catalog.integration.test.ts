import { describe, it, expect, beforeAll, afterAll } from 'vitest';
import { Client } from 'pg';

const DB_URL = process.env.SUPABASE_DB_URL ?? 'postgresql://postgres:postgres@127.0.0.1:54322/postgres';
let client: Client;

beforeAll(async () => {
  client = new Client({ connectionString: DB_URL });
  await client.connect();
});
afterAll(async () => {
  await client?.end();
});

// Grows with each W2 seed batch (batch 2..4 update this constant).
const EXPECTED_PROFILE_COUNT = 75;

const BATCH1_IDS = [
  'allium-fistulosum', 'allium-sativum', 'beta-vulgaris', 'beta-vulgaris-cicla',
  'brassica-oleracea-acephala', 'brassica-oleracea-italica', 'capsicum-annuum',
  'cucumis-sativus', 'cucurbita-pepo', 'daucus-carota', 'lactuca-sativa',
  'phaseolus-vulgaris', 'pisum-sativum', 'raphanus-sativus',
  'solanum-lycopersicum', 'spinacia-oleracea',
];

const BATCH2_IDS = [
  'allium-schoenoprasum', 'anethum-graveolens', 'artemisia-dracunculus',
  'coriandrum-sativum', 'foeniculum-vulgare', 'fragaria-x-ananassa',
  'lavandula-angustifolia', 'melissa-officinalis', 'mentha-spicata',
  'ocimum-basilicum', 'origanum-vulgare', 'petroselinum-crispum',
  'ribes-rubrum', 'ribes-uva-crispa', 'rubus-fruticosus', 'rubus-idaeus',
  'salvia-officinalis', 'salvia-rosmarinus', 'thymus-vulgaris',
  'vaccinium-corymbosum',
];

const BATCH3_IDS = [
  'actinidia-deliciosa', 'aloe-vera', 'citrus-limon', 'crassula-ovata',
  'echeveria-elegans', 'ficus-carica', 'haworthia-attenuata', 'malus-domestica',
  'passiflora-edulis', 'physalis-philadelphica', 'prunus-avium',
  'prunus-domestica', 'prunus-persica', 'pyrus-communis', 'vitis-vinifera',
];

const BATCH4_IDS = [
  'aglaonema-commutatum', 'antirrhinum-majus', 'begonia-semperflorens',
  'chlorophytum-comosum', 'cosmos-bipinnatus', 'dahlia-pinnata',
  'dracaena-trifasciata', 'epipremnum-aureum', 'ficus-elastica',
  'helianthus-annuus', 'impatiens-walleriana', 'lobularia-maritima',
  'monstera-deliciosa', 'pelargonium-x-hortorum', 'petunia-x-hybrida',
  'philodendron-hederaceum', 'rosa-x-hybrida', 'rudbeckia-hirta',
  'salvia-splendens', 'spathiphyllum-wallisii', 'tagetes-patula',
  'viola-x-wittrockiana', 'zamioculcas-zamiifolia', 'zinnia-elegans',
];

describe('W2 catalog batch 1 — vegetables + roots', () => {
  it('all 16 batch-1 profiles are present', async () => {
    const { rows } = await client.query(
      'select id from public.plant_profiles where id = any($1) order by id',
      [BATCH1_IDS],
    );
    expect(rows.map((r) => r.id)).toEqual(BATCH1_IDS);
  });

  it('every batch-1 profile carries citations and a version', async () => {
    const { rows } = await client.query(
      "select id from public.plant_profiles where id = any($1) and (source is null or jsonb_array_length(source) = 0 or version < 1)",
      [BATCH1_IDS],
    );
    expect(rows).toEqual([]);
  });

  it('tomato was enriched in place, not duplicated', async () => {
    const { rows } = await client.query(
      "select category, version from public.plant_profiles where id = 'solanum-lycopersicum'",
    );
    expect(rows).toHaveLength(1);
    expect(rows[0].category).toBe('vegetable');
    expect(rows[0].version).toBeGreaterThanOrEqual(2);
  });

  it('catalog total matches the seeded batches', async () => {
    const { rows } = await client.query('select count(*)::int as n from public.plant_profiles');
    expect(rows[0].n).toBe(EXPECTED_PROFILE_COUNT);
  });
});

describe('W2 catalog batch 2 — herbs + berries', () => {
  it('all 20 batch-2 profiles are present', async () => {
    const { rows } = await client.query(
      'select id from public.plant_profiles where id = any($1) order by id',
      [BATCH2_IDS],
    );
    expect(rows.map((r) => r.id)).toEqual(BATCH2_IDS);
  });

  it('every batch-2 profile carries citations and a version', async () => {
    const { rows } = await client.query(
      "select id from public.plant_profiles where id = any($1) and (source is null or jsonb_array_length(source) = 0 or version < 1)",
      [BATCH2_IDS],
    );
    expect(rows).toEqual([]);
  });

  it('strawberry and basil were enriched in place, not duplicated', async () => {
    const { rows } = await client.query(
      "select id, category, version from public.plant_profiles where id in ('fragaria-x-ananassa','ocimum-basilicum') order by id",
    );
    expect(rows).toHaveLength(2);
    expect(rows[0]).toMatchObject({ id: 'fragaria-x-ananassa', category: 'berry' });
    expect(rows[1]).toMatchObject({ id: 'ocimum-basilicum', category: 'herb' });
    expect(rows.every((r) => r.version >= 2)).toBe(true);
  });
});

describe('W2 catalog batch 3 — fruit, vines, succulents', () => {
  it('all 15 batch-3 profiles are present', async () => {
    const { rows } = await client.query(
      'select id from public.plant_profiles where id = any($1) order by id',
      [BATCH3_IDS],
    );
    expect(rows.map((r) => r.id)).toEqual(BATCH3_IDS);
  });

  it('every batch-3 profile carries citations and a version', async () => {
    const { rows } = await client.query(
      "select id from public.plant_profiles where id = any($1) and (source is null or jsonb_array_length(source) = 0 or version < 1)",
      [BATCH3_IDS],
    );
    expect(rows).toEqual([]);
  });

  it('passion fruit and tomatillo were enriched in place, not duplicated', async () => {
    const { rows } = await client.query(
      "select id, category, version, pollination_partners_required from public.plant_profiles where id in ('passiflora-edulis','physalis-philadelphica') order by id",
    );
    expect(rows).toHaveLength(2);
    expect(rows[0]).toMatchObject({ id: 'passiflora-edulis', category: 'fruit' });
    expect(rows[1]).toMatchObject({ id: 'physalis-philadelphica', category: 'fruit', pollination_partners_required: 2 });
    expect(rows.every((r) => r.version >= 2)).toBe(true);
  });
});

describe('W2 catalog batch 4 — ornamentals + houseplants (completes the 75)', () => {
  it('all 24 batch-4 profiles are present', async () => {
    const { rows } = await client.query(
      'select id from public.plant_profiles where id = any($1) order by id',
      [BATCH4_IDS],
    );
    expect(rows.map((r) => r.id)).toEqual(BATCH4_IDS);
  });

  it('every batch-4 profile carries citations and a version', async () => {
    const { rows } = await client.query(
      "select id from public.plant_profiles where id = any($1) and (source is null or jsonb_array_length(source) = 0 or version < 1)",
      [BATCH4_IDS],
    );
    expect(rows).toEqual([]);
  });

  it('the houseplant category (Gate B) is live with 9 species', async () => {
    const { rows } = await client.query(
      "select count(*)::int as n from public.plant_profiles where category = 'houseplant'",
    );
    expect(rows[0].n).toBe(9);
  });

  it('the full 75-plant pilot catalog is seeded and cited', async () => {
    const { rows } = await client.query(
      "select count(*)::int as total, count(*) filter (where source is not null and jsonb_array_length(source) > 0)::int as cited from public.plant_profiles",
    );
    expect(rows[0].total).toBe(75);
    expect(rows[0].cited).toBe(75);
  });
});
