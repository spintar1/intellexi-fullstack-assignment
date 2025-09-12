import { useState } from 'react';
import { useRaces } from '../context/RaceContext';

type ApplicationForm = {
  firstName: string;
  lastName: string;
  club: string;
};

export default function Races({ apiQuery, apiCommand, token }: { apiQuery: string; apiCommand: string; token: string | null }) {
  const { races, loading, error } = useRaces();
  const [forms, setForms] = useState<Record<string, ApplicationForm>>({});
  const [submittingId, setSubmittingId] = useState<string | null>(null);
  const [submitError, setSubmitError] = useState<string | null>(null);

  const getForm = (raceId: string): ApplicationForm => {
    return forms[raceId] || { firstName: '', lastName: '', club: '' };
  };

  const updateForm = (raceId: string, updates: Partial<ApplicationForm>) => {
    setForms(prev => ({
      ...prev,
      [raceId]: { ...getForm(raceId), ...updates }
    }));
  };

  return (
    <div>
      <h2>Races</h2>
      {loading ? 'Loading...' : error ? error : (
        <ul>
          {races.map(r => (
            <li key={r.id}>
              {r.name} — {r.distance}
              {token && (
                <div style={{ marginTop: 8 }}>
                  <input 
                    placeholder="First name" 
                    value={getForm(r.id).firstName} 
                    onChange={e => updateForm(r.id, { firstName: e.target.value })} 
                  />
                  <input 
                    placeholder="Last name" 
                    value={getForm(r.id).lastName} 
                    onChange={e => updateForm(r.id, { lastName: e.target.value })} 
                  />
                  <input 
                    placeholder="Club (optional)" 
                    value={getForm(r.id).club} 
                    onChange={e => updateForm(r.id, { club: e.target.value })} 
                  />
                  <button disabled={submittingId === r.id}
                    onClick={async () => {
                      const form = getForm(r.id);
                      if (!form.firstName || !form.lastName) { setSubmitError('First/Last name required'); return; }
                      setSubmittingId(r.id); setSubmitError(null);
                      try {
                        const res = await fetch(`${apiCommand}/api/v1/applications`, {
                          method: 'POST',
                          headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` },
                          body: JSON.stringify({ 
                            firstName: form.firstName, 
                            lastName: form.lastName, 
                            club: form.club || undefined, 
                            raceId: r.id 
                          })
                        });
                        if (!res.ok) throw new Error(`${res.status}`);
                        // Clear the form for this specific race
                        updateForm(r.id, { firstName: '', lastName: '', club: '' });
                      } catch { setSubmitError('Application failed'); }
                      finally { setSubmittingId(null); }
                    }}>Apply</button>
                </div>
              )}
            </li>
          ))}
        </ul>
      )}
      {submitError && <div style={{ color: 'red', marginTop: 8 }}>{submitError}</div>}
    </div>
  );
}


