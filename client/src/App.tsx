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
      localStorage.removeItem('userEmail');
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
            <Route path="/applications" element={<Applications apiQuery={API_QUERY} apiCommand={API_COMMAND} token={token} />} />
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

  const submitWithRetry = async (retryCount = 3, delay = 1000) => {
    for (let i = 0; i < retryCount; i++) {
      try {
        const response = await fetch(`${API_QUERY}/auth/token`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ email, role })
        });
        
        // Get the raw response text once
        const responseText = await response.text();
        
        if (!response.ok) {
          // Try to parse as JSON to get structured error
          try {
            const errorData = JSON.parse(responseText);
            if (errorData.error) {
              throw new Error(errorData.error);
            }
          } catch (parseError) {
            // If JSON parsing fails, use the raw response text
            if (responseText && responseText.trim()) {
              throw new Error(responseText);
            }
            
            // If no response text, use status-based fallbacks
            if (response.status === 401) {
              throw new Error('Invalid credentials - user not found');
            } else if (response.status === 403) {
              throw new Error('Access forbidden - please check your role');
            } else if (response.status >= 500) {
              throw new Error('Server error - please try again later');
            }
          }
          throw new Error('Login failed - please check your credentials');
        }
        
        // Parse the success response JSON
        const tokenResponse = JSON.parse(responseText);
        console.log('Token response from server:', tokenResponse);
        
        // Extract the token from the JSON response
        const actualToken = tokenResponse.token || tokenResponse;
        console.log('Extracted token:', actualToken);
        
        if (!actualToken || typeof actualToken !== 'string' || actualToken.length < 10) {
          throw new Error('Invalid token received from server');
        }
        
        return actualToken; // Success!
        
      } catch (err) {
        console.log(`Authentication attempt ${i + 1} failed:`, err);
        
        // If it's a network/connection error and we have retries left, wait and retry
        if ((err instanceof Error && 
            (err.message.includes('fetch') || 
             err.message.includes('NetworkError') || 
             err.message.includes('ERR_CONNECTION_REFUSED'))) && 
            i < retryCount - 1) {
          console.log(`Retrying in ${delay}ms... (${i + 1}/${retryCount})`);
          await new Promise(resolve => setTimeout(resolve, delay));
          delay *= 1.5; // Exponential backoff
          continue;
        }
        
        // If it's not a network error, or we're out of retries, throw the error
        throw err;
      }
    }
    throw new Error('Authentication failed after all retries');
  };

  const submit = async () => {
    setLoading(true);
    setError(null);
    
        try {
          const token = await submitWithRetry();
          if (token) {
            // Store the user's email for later use
            localStorage.setItem('userEmail', email);
            onToken(token);
          }
        } catch (err) {
      // Convert technical errors to user-friendly messages
      let userMessage = 'Login failed';
      
      if (err instanceof Error) {
        if (err.message.includes('Invalid role for user')) {
          userMessage = 'Incorrect role selected. Please choose the correct role for your account.';
        } else if (err.message.includes('user not found')) {
          userMessage = 'Account not found. Please contact an administrator to create your account.';
        } else if (err.message.includes('fetch') || err.message.includes('NetworkError') || err.message.includes('ERR_CONNECTION_REFUSED')) {
          userMessage = 'Unable to connect to the server. Please wait a moment and try again.';
        } else if (err.message.includes('Server error')) {
          userMessage = 'Server is temporarily unavailable. Please try again in a moment.';
        } else {
          userMessage = err.message;
        }
      }
      
      setError(userMessage);
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
      {error && (
        <div className="login-error-container">
          <div className="login-error">
            <span className="error-icon">⚠️</span>
            <div className="error-content">
              <div className="error-title">Sign In Failed</div>
              <div className="error-message">{error}</div>
            </div>
            <button 
              className="error-close-button" 
              onClick={() => setError(null)}
              aria-label="Close error message"
            >
              ✕
            </button>
          </div>
        </div>
      )}
    </div>
  );
}