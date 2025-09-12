import { Link, Route, Routes, useNavigate } from 'react-router-dom';
import { useEffect, useMemo, useState } from 'react';
import { jwtDecode } from 'jwt-decode';
import { RaceProvider } from './context/RaceContext';
import Races from './pages/Races';
import Applications from './pages/Applications';
import AdminRaces from './pages/AdminRaces';

type Decoded = { sub: string; role: 'Applicant'|'Administrator'; exp: number };

const API_QUERY = (import.meta as any).env?.VITE_API_QUERY_URL ?? 'http://localhost:8082';
const API_COMMAND = (import.meta as any).env?.VITE_API_COMMAND_URL ?? 'http://localhost:8081';

export default function App() {
  const navigate = useNavigate();
  const [token, setToken] = useState<string | null>(() => localStorage.getItem('token'));
  const user = useMemo<Decoded | null>(() => {
    if (!token) return null;
    try { return jwtDecode(token); } catch { return null; }
  }, [token]);

  useEffect(() => {
    if (token) localStorage.setItem('token', token); else localStorage.removeItem('token');
  }, [token]);

  function logout() { setToken(null); navigate('/'); }

  return (
    <RaceProvider apiQuery={API_QUERY} token={token}>
      <div style={{ maxWidth: 900, margin: '0 auto', padding: 16 }}>
        <header style={{ display: 'flex', gap: 12, alignItems: 'center', justifyContent: 'space-between' }}>
          <nav style={{ display: 'flex', gap: 12 }}>
            <Link to="/">Races</Link>
            {user && <Link to="/applications">My Applications</Link>}
            {user?.role === 'Administrator' && <Link to="/admin/races">Admin Races</Link>}
          </nav>
          <div>
            {user ? (
              <>
                <span style={{ marginRight: 8 }}>{user.sub} ({user.role})</span>
                <button onClick={logout}>Logout</button>
              </>
            ) : (
              <Login onToken={setToken} />
            )}
          </div>
        </header>
        <Routes>
          <Route path="/" element={<Races apiQuery={API_QUERY} apiCommand={API_COMMAND} token={token} />} />
          <Route path="/applications" element={<Applications apiQuery={API_QUERY} token={token} />} />
          <Route path="/admin/races" element={<AdminRaces apiQuery={API_QUERY} apiCommand={API_COMMAND} token={token} />} />
        </Routes>
      </div>
    </RaceProvider>
  );
}

function Login({ onToken }: { onToken: (t: string) => void }) {
  const [email, setEmail] = useState('applicant@example.com');
  const [role, setRole] = useState<'Applicant'|'Administrator'>('Applicant');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function submit() {
    setLoading(true); setError(null);
    try {
      const res = await fetch(`${API_COMMAND}/auth/token`, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ email, role }) });
      if (!res.ok) throw new Error(`${res.status}`);
      const data = await res.json();
      onToken(data.token);
    } catch (e) { setError('Login failed'); }
    finally { setLoading(false); }
  }

  return (
    <div style={{ display: 'inline-flex', gap: 8 }}>
      <input value={email} onChange={e => setEmail(e.target.value)} placeholder="email" />
      <select value={role} onChange={e => setRole(e.target.value as any)}>
        <option>Applicant</option>
        <option>Administrator</option>
      </select>
      <button disabled={loading} onClick={submit}>Get Token</button>
      {error && <span style={{ color: 'red', marginLeft: 8 }}>{error}</span>}
    </div>
  );
}


