import { useEffect, useState } from 'react';
import { useRaces } from '../context/RaceContext';

type Application = { id: string; firstName: string; lastName: string; raceId: string };

export default function Applications({ apiQuery, token }: { apiQuery: string; token: string | null }) {
  const [apps, setApps] = useState<Application[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const { races, loading: racesLoading } = useRaces();

  const getRaceName = (raceId: string) => {
    const race = races.find(r => r.id === raceId);
    // Updated with UUID display - timestamp: 2025-12-09
    return race ? `${race.name} - ${raceId}` : raceId;
  };

  useEffect(() => {
    if (!token) { setApps([]); setLoading(false); return; }
    (async () => {
      setLoading(true); setError(null);
      try {
        const res = await fetch(`${apiQuery}/api/v1/applications`, { headers: { Authorization: `Bearer ${token}` } });
        if (!res.ok) throw new Error(`${res.status}`);
        setApps(await res.json());
      } catch (e) { setError('Failed to load applications'); }
      finally { setLoading(false); }
    })();
  }, [apiQuery, token]);

  async function remove(id: string) {
    if (!token) return;
    try {
      const base = apiQuery.replace(':8082', ':8081');
      const res = await fetch(`${base}/api/v1/applications/${id}`, { method: 'DELETE', headers: { Authorization: `Bearer ${token}` } });
      if (!res.ok && res.status !== 202) throw new Error(`${res.status}`);
      setApps(prev => prev.filter(a => a.id !== id));
    } catch { setError('Delete failed'); }
  }

  return (
    <div>
      <h2>My Applications</h2>
      {!token ? 'Login to view.' : (loading || racesLoading) ? 'Loading...' : error ? error : (
        <ul>
          {apps.map(a => (
            <li key={a.id} style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
              <span style={{ minWidth: 260 }}>{a.firstName} {a.lastName} — {getRaceName(a.raceId)}</span>
              <button onClick={() => remove(a.id)}>Delete</button>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}


