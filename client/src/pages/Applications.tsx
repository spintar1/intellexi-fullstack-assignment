import { useState, useEffect } from 'react';
import { useRaces } from '../context/RaceContext';

type Application = { id: string; firstName: string; lastName: string; club?: string; raceId: string };

export default function Applications({ apiQuery, apiCommand, token }: { apiQuery: string; apiCommand: string; token: string | null }) {
  const { races, loading: racesLoading } = useRaces();
  const [apps, setApps] = useState<Application[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const getRaceName = (raceId: string) => {
    const race = races.find(r => r.id === raceId);
    return race?.name || 'Unknown Race';
  };

  const getRaceDistance = (raceId: string) => {
    const race = races.find(r => r.id === raceId);
    return race?.distance || '';
  };

  useEffect(() => {
    if (!token || racesLoading) return;
    
    const fetchApplications = async () => {
      setLoading(true);
      setError(null);
      
      try {
        const response = await fetch(`${apiQuery}/api/v1/applications`, {
          headers: { 'Authorization': `Bearer ${token}` }
        });
        
        if (!response.ok) {
          throw new Error('Failed to fetch applications');
        }
        
        const data = await response.json();
        setApps(data);
      } catch (err) {
        console.error('Failed to fetch applications:', err);
        setError(err instanceof Error ? err.message : 'Failed to load applications');
      } finally {
        setLoading(false);
      }
    };

    fetchApplications();
  }, [apiQuery, token, racesLoading]);

  const remove = async (id: string) => {
    if (!confirm('Are you sure you want to delete this application?')) return;

    try {
      const response = await fetch(`${apiCommand}/api/v1/applications/${id}`, {
        method: 'DELETE',
        headers: { 'Authorization': `Bearer ${token}` }
      });
      
      if (!response.ok) {
        throw new Error('Failed to delete application');
      }
      
      setApps(prev => prev.filter(a => a.id !== id));
    } catch (err) {
      console.error('Failed to delete application:', err);
      alert('Failed to delete application');
    }
  };

  if (!token) {
    return (
      <div className="fade-in">
        <div className="page-header">
          <h1 className="page-title">My Applications</h1>
          <p className="page-subtitle">Track your race registrations</p>
        </div>
        <div className="auth-required">
          <div className="auth-required-icon">🔐</div>
          <div className="auth-required-title">Authentication Required</div>
          <p className="auth-required-text">
            Please login above to view your race applications
          </p>
        </div>
      </div>
    );
  }

  if (loading || racesLoading) {
    return (
      <div className="fade-in">
        <div className="page-header">
          <h1 className="page-title">My Applications</h1>
          <p className="page-subtitle">Track your race registrations</p>
        </div>
        <div className="loading">Loading your applications...</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="fade-in">
        <div className="page-header">
          <h1 className="page-title">My Applications</h1>
          <p className="page-subtitle">Track your race registrations</p>
        </div>
        <div className="error">{error}</div>
      </div>
    );
  }

  if (apps.length === 0) {
    return (
      <div className="fade-in">
        <div className="page-header">
          <h1 className="page-title">My Applications</h1>
          <p className="page-subtitle">Track your race registrations</p>
        </div>
        <div className="text-center" style={{ 
          padding: 'var(--space-10)', 
          color: 'var(--gray-500)',
          fontSize: '1.125rem'
        }}>
          📝 You haven't registered for any races yet.
          <br />
          <span style={{ fontSize: '0.875rem', marginTop: 'var(--space-2)', display: 'block' }}>
            Check out the available races to get started!
          </span>
        </div>
      </div>
    );
  }

  return (
    <div className="fade-in">
      <div className="page-header">
        <h1 className="page-title">My Applications</h1>
        <p className="page-subtitle">
          {apps.length} application{apps.length !== 1 ? 's' : ''} registered • Ready to race!
        </p>
      </div>

      <div className="races-grid">
        {apps.map(app => (
          <div key={app.id} className="race-card slide-in">
            <div className="race-header">
              <div className="race-info">
                <h2 className="race-name">{getRaceName(app.raceId)}</h2>
                <span className="race-distance">{getRaceDistance(app.raceId)}</span>
              </div>
            </div>

            <div className="registration-section">
              <div className="form-grid">
                <div className="form-group">
                  <label className="form-label">Participant</label>
                  <div style={{ 
                    padding: 'var(--space-3) var(--space-4)',
                    background: 'rgba(99, 102, 241, 0.05)',
                    border: '1px solid rgba(99, 102, 241, 0.1)',
                    borderRadius: 'var(--radius-lg)',
                    color: 'var(--gray-700)',
                    fontWeight: '600'
                  }}>
                    {app.firstName} {app.lastName}
                  </div>
                </div>
                
                {app.club && (
                  <div className="form-group">
                    <label className="form-label">Running Club</label>
                    <div style={{ 
                      padding: 'var(--space-3) var(--space-4)',
                      background: 'rgba(99, 102, 241, 0.05)',
                      border: '1px solid rgba(99, 102, 241, 0.1)',
                      borderRadius: 'var(--radius-lg)',
                      color: 'var(--gray-700)',
                      fontWeight: '600'
                    }}>
                      {app.club}
                    </div>
                  </div>
                )}
              </div>

              <div style={{ 
                display: 'flex', 
                alignItems: 'center', 
                gap: 'var(--space-4)',
                marginTop: 'var(--space-4)',
                padding: 'var(--space-4)',
                background: 'rgba(34, 197, 94, 0.05)',
                border: '1px solid rgba(34, 197, 94, 0.2)',
                borderRadius: 'var(--radius-lg)'
              }}>
                <div style={{ 
                  fontSize: '1.5rem' 
                }}>
                  ✅
                </div>
                <div style={{ flex: 1 }}>
                  <div style={{ 
                    color: 'var(--success)', 
                    fontWeight: '600',
                    marginBottom: 'var(--space-1)'
                  }}>
                    Registration Confirmed
                  </div>
                  <div style={{ 
                    fontSize: '0.875rem', 
                    color: 'var(--gray-600)' 
                  }}>
                    You're all set for this race!
                  </div>
                </div>
                <button
                  onClick={() => remove(app.id)}
                  className="btn btn-danger btn-sm"
                  style={{ flexShrink: 0 }}
                >
                  🗑️ Withdraw
                </button>
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}