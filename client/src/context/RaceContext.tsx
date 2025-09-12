import { createContext, useContext, useState, useEffect, ReactNode } from 'react';

type Race = { id: string; name: string; distance: string };

interface RaceContextType {
  races: Race[];
  loading: boolean;
  error: string | null;
  refreshRaces: () => Promise<void>;
}

const RaceContext = createContext<RaceContextType | undefined>(undefined);

export function RaceProvider({ children, apiQuery, token }: { children: ReactNode; apiQuery: string; token: string | null }) {
  const [races, setRaces] = useState<Race[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const refreshRaces = async () => {
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
      setRaces(await res.json());
    } catch (e) { 
      setError('Failed to load races'); 
    } finally { 
      setLoading(false); 
    }
  };

  useEffect(() => {
    refreshRaces();
  }, [token, apiQuery]);

  return (
    <RaceContext.Provider value={{ races, loading, error, refreshRaces }}>
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

