// API configuration with fallback for test environments
const getEnv = () => {
  try {
    // In test environment, import.meta might not be available
    return (import.meta as any)?.env || {};
  } catch {
    // Fallback for test environments
    return {};
  }
};

export const API_QUERY = getEnv().VITE_API_QUERY_URL ?? 'http://localhost:8082';
export const API_COMMAND = getEnv().VITE_API_COMMAND_URL ?? 'http://localhost:8081';
