declare global {
  interface ImportMeta {
    env: Record<string, string | undefined>;
  }
  
  // For Jest global types
  namespace NodeJS {
    interface Global {
      fetch: any;
      localStorage: any;
    }
  }
}

export {};
