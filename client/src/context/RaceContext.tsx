import { createContext, useContext, useState, useEffect, ReactNode } from 'react';

type Race = { id: string; name: string; distance: string };

interface RaceContextType {
  races: Race[];
  loading: boolean;
  error: string | null;
  refreshRaces: () => Promise<void>;
  addRaceOptimistically: (race: Race) => void;
  removeRaceOptimistically: (raceId: string) => void;
  updateRaceOptimistically: (raceId: string, updates: Partial<Race>) => void;
}

const RaceContext = createContext<RaceContextType | undefined>(undefined);

export function RaceProvider({ children, apiQuery, token }: { children: ReactNode; apiQuery: string; token: string | null }) {
  const [races, setRaces] = useState<Race[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const refreshRaces = async (retryCount = 0) => {
    if (!token) {
      setRaces([]);
      setLoading(false);
      return;
    }
    
    setLoading(true);
    setError(null);
    try {
      const res = await fetch(`${apiQuery}/api/v1/races`, { 
        headers: { Authorization: `Bearer ${token}` } 
      });
      if (!res.ok) throw new Error(`${res.status}`);
      const fetchedRaces = await res.json();
      setRaces(fetchedRaces.sort((a: Race, b: Race) => a.name.localeCompare(b.name)));
    } catch (e) { 
      setError('Failed to load races'); 
    } finally { 
      setLoading(false); 
    }
  };

  // Simple refresh without retries - optimistic updates handle consistency

  const addRaceOptimistically = (race: Race) => {
    setRaces(prevRaces => {
      // Avoid duplicates
      if (prevRaces.find(r => r.id === race.id)) {
        return prevRaces;
      }
      const newRaces = [...prevRaces, race];
      return newRaces.sort((a, b) => a.name.localeCompare(b.name));
    });
  };

  const removeRaceOptimistically = (raceId: string) => {
    setRaces(prevRaces => prevRaces.filter(r => r.id !== raceId));
  };

  const updateRaceOptimistically = (raceId: string, updates: Partial<Race>) => {
    setRaces(prevRaces => {
      const updatedRaces = prevRaces.map(r => r.id === raceId ? { ...r, ...updates } : r);
      return updatedRaces.sort((a, b) => a.name.localeCompare(b.name));
    });
  };

  useEffect(() => {
    refreshRaces();
  }, [token, apiQuery]);

  return (
    <RaceContext.Provider value={{ 
      races, 
      loading, 
      error, 
      refreshRaces,
      addRaceOptimistically,
      removeRaceOptimistically,
      updateRaceOptimistically
    }}>
      {children}
    </RaceContext.Provider>
  );
}

export function useRaces() {
  const context = useContext(RaceContext);
  if (context === undefined) {
    throw new Error('useRaces must be used within a RaceProvider');
  }
  return context;
}

