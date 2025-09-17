import { useState, useEffect } from 'react';
import { useRaces } from '../context/RaceContext';

type Race = { id: string; name: string; distance: string };
type FormData = {};

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
  const [formData, setFormData] = useState<FormData>({});
  const [submitting, setSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState<string | null>(null);

  const selectedRace = races.find(r => r.id === selectedRaceId);

  const resetForm = () => {
    setFormData({});
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
    if (!selectedRaceId) return;

    setSubmitting(true);
    setSubmitError(null);

    try {
      // Submit the application
      const response = await fetch(`${apiCommand}/api/v1/applications`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify({
          raceId: selectedRaceId
        })
      });

      if (!response.ok) {
        let errorMessage = 'Failed to submit application';
        try {
          const errorData = await response.json();
          errorMessage = errorData.error || errorData.message || errorMessage;
        } catch {
          errorMessage = await response.text() || errorMessage;
        }
        throw new Error(errorMessage);
      }

      const result = await response.json();
      const applicationId = result.id;
      
      // Wait a moment for the event to be processed, then verify the application was created
      await new Promise(resolve => setTimeout(resolve, 1500));
      
      // Check if the application was actually created
      const verifyResponse = await fetch(`${apiQuery}/api/v1/applications`, {
        headers: { 'Authorization': `Bearer ${token}` }
      });
      
      if (verifyResponse.ok) {
        const applications = await verifyResponse.json();
        const applicationExists = applications.some((app: any) => 
          app.id === applicationId || 
          (app.raceId === selectedRaceId && app.applicantEmail === localStorage.getItem('userEmail'))
        );
        
        if (!applicationExists) {
          throw new Error('Registration failed. You may already be registered for this race, or there was a system error.');
        }
      }

      // Success! Show success message and go back to race list
      alert('✅ Successfully registered for the race!');
      goBack();
      
    } catch (error) {
      console.error('Application submission failed:', error);
      let userFriendlyMessage = 'Failed to submit application';
      
      if (error instanceof Error) {
        if (error.message.includes('already registered') || error.message.includes('already be registered')) {
          userFriendlyMessage = '⚠️ You are already registered for this race. Each participant can only register once per race.';
        } else if (error.message.includes('constraint') || error.message.includes('duplicate')) {
          userFriendlyMessage = '⚠️ Registration failed - you are already registered for this race.';
        } else if (error.message.includes('network') || error.message.includes('fetch')) {
          userFriendlyMessage = '🌐 Network error. Please check your connection and try again.';
        } else {
          userFriendlyMessage = error.message;
        }
      }
      
      setSubmitError(userFriendlyMessage);
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
                <div className="form-group full-width" style={{
                  padding: 'var(--space-4)',
                  background: 'rgba(99, 102, 241, 0.08)',
                  border: '1px solid rgba(99, 102, 241, 0.15)',
                  borderRadius: 'var(--radius-lg)',
                  textAlign: 'center'
                }}>
                  <p style={{
                    margin: 0,
                    color: 'var(--gray-700)',
                    fontSize: '0.95rem'
                  }}>
                    Your profile information will be used for race registration.
                  </p>
                </div>
              </div>

              <button
                className="btn register-button"
                disabled={submitting}
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