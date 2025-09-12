import { useState } from 'react';
import { useRaces } from '../context/RaceContext';

const distances = ['5k','10k','HalfMarathon','Marathon'];

export default function AdminRaces({ apiQuery, apiCommand, token }: { apiQuery: string; apiCommand: string; token: string | null }) {
  const { races, loading, error, refreshRaces } = useRaces();
  const [name, setName] = useState('');
  const [distance, setDistance] = useState(distances[0]);
  const [submitError, setSubmitError] = useState<string | null>(null);

  async function createRace() {
    if (!token) return;
    setSubmitError(null);
    try {
      const res = await fetch(`${apiCommand}/api/v1/races`, { method: 'POST', headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` }, body: JSON.stringify({ name, distance }) });
      if (!res.ok) throw new Error(`${res.status}`);
      setName(''); setDistance(distances[0]);
      await refreshRaces(); // This will update all components using the race context
    } catch { setSubmitError('Create failed'); }
  }

  async function updateRace(id: string, patch: { name?: string; distance?: string }) {
    if (!token) return;
    setSubmitError(null);
    try {
      const res = await fetch(`${apiCommand}/api/v1/races/${id}`, { method: 'PATCH', headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` }, body: JSON.stringify(patch) });
      if (!res.ok) throw new Error(`${res.status}`);
      await refreshRaces();
    } catch { setSubmitError('Update failed'); }
  }

  async function deleteRace(id: string) {
    if (!token) return;
    setSubmitError(null);
    try {
      const res = await fetch(`${apiCommand}/api/v1/races/${id}`, { method: 'DELETE', headers: { Authorization: `Bearer ${token}` } });
      if (!res.ok && res.status !== 204) throw new Error(`${res.status}`);
      await refreshRaces();
    } catch { setSubmitError('Delete failed'); }
  }

  return (
    <div>
      <h2>Admin Races</h2>
      {!token ? 'Login as Administrator.' : (
        <>
          <div style={{ display: 'flex', gap: 8, marginBottom: 12 }}>
            <input placeholder="Race name" value={name} onChange={e => setName(e.target.value)} />
            <select value={distance} onChange={e => setDistance(e.target.value)}>{distances.map(d => <option key={d}>{d}</option>)}</select>
            <button onClick={createRace}>Create</button>
            {submitError && <span style={{ color: 'red' }}>{submitError}</span>}
          </div>
          {loading ? 'Loading...' : error ? error : (
            <ul>
              {races.map(r => (
                <li key={r.id} style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
                  <span style={{ minWidth: 260 }}>{r.name} — {r.distance}</span>
                  <button onClick={() => updateRace(r.id, { name: r.name + ' *' })}>Rename</button>
                  <button onClick={() => deleteRace(r.id)}>Delete</button>
                </li>
              ))}
            </ul>
          )}
        </>
      )}
    </div>
  );
}


