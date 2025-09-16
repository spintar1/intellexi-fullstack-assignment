import { useState } from 'react';
import { useRaces } from '../context/RaceContext';

const distances = ['5k','10k','HalfMarathon','Marathon'];

export default function AdminRaces({ apiQuery, apiCommand, token }: { apiQuery: string; apiCommand: string; token: string | null }) {
  const { races, loading, error, refreshRaces, addRaceOptimistically, removeRaceOptimistically, updateRaceOptimistically } = useRaces();
  const [name, setName] = useState('');
  const [distance, setDistance] = useState(distances[0]);
  const [submitError, setSubmitError] = useState<string | null>(null);
  const [isCreating, setIsCreating] = useState(false);
  const [editingId, setEditingId] = useState<string | null>(null);
  const [editingName, setEditingName] = useState('');
  const [isSaving, setIsSaving] = useState(false);

  async function createRace() {
    if (!token || !name.trim()) return;
    
    setSubmitError(null);
    setIsCreating(true);
    
    // Generate temporary ID for optimistic update
    const tempId = `temp-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`;
    const newRace = { id: tempId, name: name.trim(), distance };
    
    try {
      // 1. Immediately show the race in UI (optimistic update)
      addRaceOptimistically(newRace);
      
      // 2. Clear form immediately
      setName(''); 
      setDistance(distances[0]);
      
      // 3. Send request to server
      const res = await fetch(`${apiCommand}/api/v1/races`, { 
        method: 'POST', 
        headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` }, 
        body: JSON.stringify({ name: newRace.name, distance: newRace.distance }) 
      });
      
      if (!res.ok) throw new Error(`${res.status}`);
      
      // 4. Get the real race ID from response
      const realRace = await res.json();
      console.log('✅ Race created successfully:', realRace);
      
      // Smart sync: Single background refresh to ensure consistency
      setTimeout(() => refreshRaces(), 1000);
      
    } catch (error) {
      console.error('❌ Failed to create race:', error);
      // Remove the optimistic race if creation failed
      removeRaceOptimistically(tempId);
      setSubmitError('Create failed - please try again');
    } finally {
      setIsCreating(false);
    }
  }

  async function updateRace(id: string, patch: { name?: string; distance?: string }) {
    if (!token) return;
    setSubmitError(null);
    
    // Store original race data in case we need to restore it
    const originalRace = races.find(r => r.id === id);
    
    try {
      // 1. Immediately update in UI (optimistic update)
      updateRaceOptimistically(id, patch);
      
      // 2. Send request to server
      const res = await fetch(`${apiCommand}/api/v1/races/${id}`, { 
        method: 'PATCH', 
        headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` }, 
        body: JSON.stringify(patch) 
      });
      
      if (!res.ok) throw new Error(`${res.status}`);
      
      console.log('✅ Race updated successfully:', id, patch);
      
      // Smart sync: Refresh only after successful operations to ensure data consistency
      setTimeout(() => refreshRaces(), 1000);
      
    } catch (error) {
      console.error('❌ Failed to update race:', error);
      // Restore original race if update failed
      if (originalRace) {
        updateRaceOptimistically(id, { name: originalRace.name, distance: originalRace.distance });
      }
      setSubmitError('Update failed - please try again');
    }
  }

  function startEditing(raceId: string, raceName: string) {
    console.log('Starting to edit race:', raceId, raceName);
    setEditingId(raceId);
    setEditingName(raceName);
  }

  function cancelEditing() {
    setEditingId(null);
    setEditingName('');
  }

  async function saveEdit(id: string) {
    if (!editingName.trim() || isSaving) return;
    
    setIsSaving(true);
    console.log('Saving edit for race ID:', id, 'New name:', editingName.trim());
    
    try {
      await updateRace(id, { name: editingName.trim() });
      // Success - exit editing mode
      setEditingId(null);
      setEditingName('');
    } catch (error) {
      // Error handling is already done in updateRace
      console.error('Failed to save edit:', error);
    } finally {
      setIsSaving(false);
    }
  }

  async function deleteRace(id: string) {
    if (!token) return;
    
    setSubmitError(null);
    
    // Store original race data in case we need to restore it
    const raceToDelete = races.find(r => r.id === id);
    
    try {
      // 1. Immediately remove from UI (optimistic update)
      removeRaceOptimistically(id);
      
      // 2. Send delete request to server
      const res = await fetch(`${apiCommand}/api/v1/races/${id}`, { 
        method: 'DELETE', 
        headers: { Authorization: `Bearer ${token}` } 
      });
      
      if (!res.ok && res.status !== 204) throw new Error(`${res.status}`);
      
      console.log('✅ Race deleted successfully:', id);
      
      // Smart sync: Single background refresh to ensure consistency  
      setTimeout(() => refreshRaces(), 1000);
      
    } catch (error) {
      console.error('❌ Failed to delete race:', error);
      // Restore the race if deletion failed
      if (raceToDelete) {
        addRaceOptimistically(raceToDelete);
      }
      setSubmitError('Delete failed - please try again');
    }
  }

  return (
    <div className="fade-in">
      {!token ? (
        <div className="card text-center">
          <h2 className="card-title">🔒 Authentication Required</h2>
          <p>Please login as Administrator to manage races.</p>
        </div>
      ) : (
        <>
          <div className="card">
            <div className="card-header">
              <h2 className="card-title">Race Management</h2>
            </div>
            
            <div style={{ display: 'flex', flexWrap: 'wrap', gap: 'var(--space-4)', marginBottom: 'var(--space-6)', alignItems: 'end' }}>
              <div className="form-group" style={{ flex: '2', minWidth: '200px' }}>
                <label className="form-label">Race Name</label>
                <input 
                  className="form-input" 
                  placeholder="Enter race name" 
                  value={name} 
                  onChange={e => setName(e.target.value)}
                  disabled={isCreating}
                />
              </div>
              <div className="form-group" style={{ flex: '1', minWidth: '150px' }}>
                <label className="form-label">Distance</label>
                <select 
                  className="form-select" 
                  value={distance} 
                  onChange={e => setDistance(e.target.value)}
                  disabled={isCreating}
                >
                  {distances.map(d => <option key={d} value={d}>{d}</option>)}
                </select>
              </div>
              <div style={{ display: 'flex', alignItems: 'center', minWidth: 'fit-content' }}>
                <button 
                  className={`btn btn-primary btn-large ${isCreating ? '' : ''}`}
                  onClick={createRace} 
                  disabled={isCreating || !name.trim()}
                >
                  {isCreating ? '⏳ Creating...' : '🏁 Create Race'}
                </button>
              </div>
            </div>
            
            {submitError && <div className="error mb-4">{submitError}</div>}
          </div>
          <div className="card">
            <div className="card-header">
              <h3 className="card-title">🏁 Active Races</h3>
              <div className="text-sm" style={{ color: 'var(--gray-500)' }}>({races.length} races)</div>
            </div>
            
            {loading ? (
              <div className="loading">Loading races...</div>
            ) : error ? (
              <div className="error">{error}</div>
            ) : races.length === 0 ? (
              <div className="text-center" style={{ padding: 'var(--space-8)', color: 'var(--gray-500)' }}>
                🏃‍♂️ No races yet. Create your first race above!
              </div>
            ) : (
              <div className="race-list">
                {races.map(r => {
                  const isTemporary = r.id.startsWith('temp-');
                  const isEditing = editingId === r.id;
                  
                  return (
                    <div 
                      key={r.id} 
                      className={`race-item ${isEditing ? 'editing' : ''} ${isTemporary ? 'slide-in' : ''}`}
                      style={{ 
                        opacity: isTemporary ? 0.7 : 1,
                        fontStyle: isTemporary ? 'italic' : 'normal'
                      }}
                    >
                      {isEditing ? (
                        <div className="race-edit-form">
                          <input 
                            className="race-edit-input"
                            value={editingName}
                            onChange={e => setEditingName(e.target.value)}
                            onKeyDown={e => {
                              if (e.key === 'Enter') saveEdit(r.id);
                              if (e.key === 'Escape') cancelEditing();
                            }}
                            autoFocus
                            placeholder="Enter race name"
                          />
                          <div className="race-distance">{r.distance}</div>
                        </div>
                      ) : (
                        <div className="race-info">
                          <div className="race-name">
                            {r.name}
                            {(isTemporary || (editingId === r.id && isSaving)) && (
                              <span className="status-badge status-saving">Saving...</span>
                            )}
                          </div>
                          <div className="race-distance">{r.distance}</div>
                        </div>
                      )}
                      
                      <div className="race-actions">
                        {isEditing ? (
                          <>
                            <button 
                              className="btn btn-success btn-small" 
                              onClick={() => saveEdit(r.id)} 
                              disabled={!editingName.trim() || isSaving}
                            >
                              {isSaving ? '⏳ Saving...' : '✓ Save'}
                            </button>
                            <button 
                              className="btn btn-ghost btn-small" 
                              onClick={cancelEditing}
                            >
                              × Cancel
                            </button>
                          </>
                        ) : (
                          <>
                            <button 
                              className="btn btn-secondary btn-small" 
                              onClick={() => startEditing(r.id, r.name)}
                              disabled={isTemporary}
                              title="Rename race"
                            >
                              ✏️ Rename
                            </button>
                            <button 
                              className="btn btn-danger btn-small" 
                              onClick={() => deleteRace(r.id)}
                              disabled={isTemporary}
                              title="Delete race"
                            >
                              🗑️ Delete
                            </button>
                          </>
                        )}
                      </div>
                    </div>
                  );
                })}
              </div>
            )}
          </div>
        </>
      )}
    </div>
  );
}


