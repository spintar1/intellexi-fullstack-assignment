import { Link, Route, Routes, useNavigate, useLocation, NavLink } from 'react-router-dom';
import { useEffect, useMemo, useState } from 'react';
import { jwtDecode } from 'jwt-decode';
import { RaceProvider } from './context/RaceContext';
import Races from './pages/Races';
import Applications from './pages/Applications';
import AdminRaces from './pages/AdminRaces';
import './styles/modern-race.css';

type Decoded = { sub: string; role: 'Applicant'|'Administrator'; exp: number };

const API_QUERY = (import.meta as any).env?.VITE_API_QUERY_URL ?? 'http://localhost:8082';
const API_COMMAND = (import.meta as any).env?.VITE_API_COMMAND_URL ?? 'http://localhost:8081';

export default function App() {
  const navigate = useNavigate();
  const location = useLocation();
  const [token, setToken] = useState<string | null>(() => {
    const storedToken = localStorage.getItem('token');
    console.log('Loading stored token:', storedToken);
    return storedToken;
  });
  const user = useMemo<Decoded | null>(() => {
    if (!token) return null;
    try { return jwtDecode(token); } catch { return null; }
  }, [token]);

  useEffect(() => {
    if (token) {
      console.log('Storing token:', token);
      localStorage.setItem('token', token);
    } else {
      localStorage.removeItem('token');
    }
  }, [token]);

  const setCleanToken = (newToken: string | null) => {
    console.log('setCleanToken - Received token:', newToken);
    setToken(newToken);
  };

  function logout() { setToken(null); navigate('/'); }

  return (
    <RaceProvider apiQuery={API_QUERY} token={token}>
      <div className="app">
        <header className="app-header">
          <div className="header-container">
            <div className="brand">
              <Link to="/" className="brand-link">
                <div className="brand-icon">R</div>
                <span className="brand-text">RaceRunner</span>
              </Link>
            </div>
            
            <nav className="main-navigation">
              <NavLink 
                to="/" 
                className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}
                end
              >
                Races
              </NavLink>
              
              {user && (
                <NavLink 
                  to="/applications" 
                  className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}
                >
                  My Applications
                </NavLink>
              )}
              
              {user?.role === 'Administrator' && (
                <NavLink 
                  to="/admin/races" 
                  className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}
                >
                  Admin Panel
                </NavLink>
              )}
            </nav>

            <div className="header-actions">
              {user ? (
                <div className="user-section">
                  <div className="user-info">
                    <div className="user-name">{user.sub}</div>
                    <div className="user-role">{user.role}</div>
                  </div>
                  <button onClick={logout} className="logout-button">
                    Sign Out
                  </button>
                </div>
              ) : (
                <Login onToken={setCleanToken} />
              )}
            </div>
          </div>
        </header>

        <main className="app-main">
          <Routes>
            <Route path="/" element={<Races apiQuery={API_QUERY} apiCommand={API_COMMAND} token={token} />} />
            <Route path="/applications" element={<Applications apiQuery={API_QUERY} token={token} />} />
            <Route path="/admin/races" element={<AdminRaces apiQuery={API_QUERY} apiCommand={API_COMMAND} token={token} />} />
          </Routes>
        </main>
      </div>
    </RaceProvider>
  );
}

function Login({ onToken }: { onToken: (t: string) => void }) {
  const [email, setEmail] = useState('applicant@example.com');
  const [role, setRole] = useState<'Applicant'|'Administrator'>('Applicant');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const submit = async () => {
    setLoading(true);
    setError(null);
    
    try {
      const response = await fetch(`${API_COMMAND}/auth/token`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, role })
      });
      
      if (!response.ok) throw new Error('Login failed');
      
      // Parse JSON response to get the actual token
      const tokenResponse = await response.json();
      console.log('Token response from server:', tokenResponse);
      
      // Extract the token from the JSON response
      const actualToken = tokenResponse.token || tokenResponse;
      console.log('Extracted token:', actualToken);
      
      if (!actualToken || typeof actualToken !== 'string' || actualToken.length < 10) {
        throw new Error('Invalid token received from server');
      }
      
      onToken(actualToken);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Login failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-section">
      <div className="login-form">
        <input 
          className="login-input" 
          value={email} 
          onChange={e => setEmail(e.target.value)} 
          placeholder="Email address"
          type="email"
        />
        <select 
          className="login-select" 
          value={role} 
          onChange={e => setRole(e.target.value as any)}
        >
          <option value="Applicant">Applicant</option>
          <option value="Administrator">Administrator</option>
        </select>
        <button 
          disabled={loading} 
          onClick={submit} 
          className="login-button"
        >
          {loading ? 'Signing in...' : 'Sign In'}
        </button>
      </div>
      {error && <div className="login-error">{error}</div>}
    </div>
  );
}