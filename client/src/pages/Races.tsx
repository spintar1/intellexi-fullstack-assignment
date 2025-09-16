import { useState, useEffect } from 'react';
import { useRaces } from '../context/RaceContext';

type Race = { id: string; name: string; distance: string };
type FormData = { firstName: string; lastName: string; club: string };

export default function Races({ 
  apiQuery, 
  apiCommand, 
  token 
}: { 
  apiQuery: string; 
  apiCommand: string; 
  token: string | null; 
}) {
  const { races, loading, error } = useRaces();
  const [selectedRaceId, setSelectedRaceId] = useState<string | null>(null);
  const [formData, setFormData] = useState<FormData>({ firstName: '', lastName: '', club: '' });
  const [submitting, setSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState<string | null>(null);

  const selectedRace = races.find(r => r.id === selectedRaceId);

  const resetForm = () => {
    setFormData({ firstName: '', lastName: '', club: '' });
    setSubmitError(null);
  };

  const selectRace = (raceId: string) => {
    setSelectedRaceId(raceId);
    resetForm();
  };

  const goBack = () => {
    setSelectedRaceId(null);
    resetForm();
  };

  const submitApplication = async () => {
    if (!selectedRaceId || !formData.firstName.trim() || !formData.lastName.trim()) return;

    setSubmitting(true);
    setSubmitError(null);

    try {
      const response = await fetch(`${apiCommand}/api/v1/applications`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify({
          firstName: formData.firstName.trim(),
          lastName: formData.lastName.trim(),
          club: formData.club.trim() || null,
          raceId: selectedRaceId
        })
      });

      if (!response.ok) {
        const errorText = await response.text();
        throw new Error(errorText || 'Failed to submit application');
      }

      // Success! Go back to race list
      goBack();
      
    } catch (error) {
      console.error('Application submission failed:', error);
      setSubmitError(error instanceof Error ? error.message : 'Failed to submit application');
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) {
    return (
      <div className="fade-in">
        <div className="page-header">
          <h1 className="page-title">Race Registration</h1>
          <p className="page-subtitle">Find your next challenge and register today</p>
        </div>
        <div className="loading">Loading available races...</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="fade-in">
        <div className="page-header">
          <h1 className="page-title">Race Registration</h1>
          <p className="page-subtitle">Find your next challenge and register today</p>
        </div>
        <div className="error">{error}</div>
      </div>
    );
  }

  if (!token) {
    return (
      <div className="fade-in">
        <div className="page-header">
          <h1 className="page-title">Race Registration</h1>
          <p className="page-subtitle">Find your next challenge and register today</p>
        </div>
        <div className="auth-required">
          <div className="auth-required-icon">🔐</div>
          <div className="auth-required-title">Authentication Required</div>
          <p className="auth-required-text">
            Please login above to view and register for races
          </p>
        </div>
      </div>
    );
  }

  if (races.length === 0) {
    return (
      <div className="fade-in">
        <div className="page-header">
          <h1 className="page-title">Race Registration</h1>
          <p className="page-subtitle">Find your next challenge and register today</p>
        </div>
        <div className="text-center" style={{ 
          padding: 'var(--space-10)', 
          color: 'var(--gray-500)',
          fontSize: '1.125rem'
        }}>
          🏃‍♂️ No races available yet. Check back soon for exciting events!
        </div>
      </div>
    );
  }

  // Show registration form for selected race
  if (selectedRace) {
    return (
      <div className="fade-in">
        <div className="page-header">
          <button
            onClick={goBack}
            className="btn btn-secondary"
            style={{ 
              marginBottom: 'var(--space-4)', 
              alignSelf: 'flex-start',
              margin: '0 auto var(--space-4) auto'
            }}
          >
            ← Back to Races
          </button>
          <h1 className="page-title">Register for {selectedRace.name}</h1>
          <p className="page-subtitle">
            {selectedRace.distance} • Complete the form below to secure your spot
          </p>
        </div>

        <div style={{ maxWidth: '500px', margin: '0 auto' }}>
          <div className="race-card">
            <div className="registration-section">
              <div style={{ marginBottom: 'var(--space-6)' }}>
                <h3 style={{ 
                  margin: '0 0 var(--space-4) 0',
                  color: 'var(--gray-800)',
                  fontSize: '1.25rem',
                  fontWeight: '700'
                }}>
                  Registration Details
                </h3>
              </div>

              <div className="form-grid">
                <div className="form-group">
                  <label className="form-label">First Name *</label>
                  <input
                    type="text"
                    className="form-input"
                    placeholder="Enter first name"
                    autoComplete="off"
                    data-lpignore="true"
                    value={formData.firstName}
                    onChange={e => setFormData(prev => ({ ...prev, firstName: e.target.value }))}
                  />
                </div>
                
                <div className="form-group">
                  <label className="form-label">Last Name *</label>
                  <input
                    type="text"
                    className="form-input"
                    placeholder="Enter last name"
                    autoComplete="off"
                    data-lpignore="true"
                    value={formData.lastName}
                    onChange={e => setFormData(prev => ({ ...prev, lastName: e.target.value }))}
                  />
                </div>
                
                <div className="form-group full-width">
                  <label className="form-label">Club (Optional)</label>
                  <input
                    type="text"
                    className="form-input"
                    placeholder="Enter your running club"
                    autoComplete="off"
                    data-lpignore="true"
                    value={formData.club}
                    onChange={e => setFormData(prev => ({ ...prev, club: e.target.value }))}
                  />
                </div>
              </div>

              <button
                className="btn register-button"
                disabled={
                  submitting || 
                  !formData.firstName.trim() || 
                  !formData.lastName.trim()
                }
                onClick={submitApplication}
              >
                {submitting ? (
                  <>⏳ Registering...</>
                ) : (
                  <>🏅 Register for Race</>
                )}
              </button>

              {submitError && (
                <div className="error" style={{ marginTop: 'var(--space-4)' }}>
                  {submitError}
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
    );
  }

  // Show race list
  return (
    <div className="fade-in">
      <div className="page-header">
        <h1 className="page-title">Available Races</h1>
        <p className="page-subtitle">
          {races.length} race{races.length !== 1 ? 's' : ''} available • Click on a race to register
        </p>
      </div>

      <div style={{ maxWidth: '700px', margin: '0 auto' }}>
        <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-4)' }}>
          {races.map(race => (
            <div
              key={race.id}
              className="race-card slide-in"
              style={{
                cursor: 'pointer',
                transition: 'all 0.2s ease',
                border: '2px solid transparent'
              }}
              onClick={() => selectRace(race.id)}
              onMouseEnter={e => {
                e.currentTarget.style.borderColor = 'rgba(99, 102, 241, 0.3)';
                e.currentTarget.style.transform = 'translateY(-2px)';
              }}
              onMouseLeave={e => {
                e.currentTarget.style.borderColor = 'transparent';
                e.currentTarget.style.transform = 'translateY(0)';
              }}
            >
              <div style={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                gap: 'var(--space-4)'
              }}>
                <div className="race-info">
                  <h2 className="race-name">{race.name}</h2>
                  <span className="race-distance">{race.distance}</span>
                </div>
                <div style={{
                  color: 'var(--primary)',
                  fontSize: '1.5rem',
                  display: 'flex',
                  alignItems: 'center',
                  gap: 'var(--space-2)'
                }}>
                  <span style={{ fontSize: '0.875rem', fontWeight: '600' }}>
                    Click to Register
                  </span>
                  →
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}