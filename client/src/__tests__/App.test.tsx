import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import '@testing-library/jest-dom';

// Mock the API config module
jest.mock('../config/api', () => ({
  API_QUERY: 'http://localhost:8082',
  API_COMMAND: 'http://localhost:8081'
}));

// Mock CSS imports
jest.mock('../styles/modern-race.css', () => ({}));

import App from '../App';

// Mock the API endpoints
const mockFetch = jest.fn();
global.fetch = mockFetch;

// Mock localStorage
const mockLocalStorage = {
  getItem: jest.fn(),
  setItem: jest.fn(),
  removeItem: jest.fn(),
};
Object.defineProperty(window, 'localStorage', {
  value: mockLocalStorage,
});

// Helper function to render App with Router context
const renderApp = () => {
  return render(
    <BrowserRouter>
      <App />
    </BrowserRouter>
  );
};

describe('App Component', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    mockLocalStorage.getItem.mockReturnValue(null);
    // Default mock for race loading
    mockFetch.mockResolvedValue({
      ok: true,
      json: () => Promise.resolve([]),
    });
  });

  test('renders login form when no token is present', () => {
    renderApp();
    
    expect(screen.getByText('RaceRunner')).toBeInTheDocument();
    expect(screen.getByPlaceholderText('Email address')).toBeInTheDocument();
    expect(screen.getByText('Sign In')).toBeInTheDocument();
  });

  test('login form handles successful authentication', async () => {
    mockFetch.mockResolvedValueOnce({
      ok: true,
      text: () => Promise.resolve(JSON.stringify({ token: 'mock-jwt-token' })),
    });

    renderApp();
    
    const emailInput = screen.getByPlaceholderText('Email address');
    const signInButton = screen.getByText('Sign In');
    
    fireEvent.change(emailInput, { target: { value: 'test@example.com' } });
    fireEvent.click(signInButton);
    
    await waitFor(() => {
      expect(mockFetch).toHaveBeenCalledWith(
        expect.stringContaining('/auth/token'),
        expect.objectContaining({
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ email: 'test@example.com', role: 'Applicant' }),
        })
      );
    });
  });

  test('login form displays error on authentication failure', async () => {
    mockFetch.mockResolvedValueOnce({
      ok: false,
      status: 401,
      text: () => Promise.resolve('{"error": "Invalid role for user"}'),
    });

    renderApp();
    
    const signInButton = screen.getByText('Sign In');
    fireEvent.click(signInButton);
    
    await waitFor(() => {
      expect(screen.getByText('Sign In Failed')).toBeInTheDocument();
      expect(screen.getByText('Incorrect role selected. Please choose the correct role for your account.')).toBeInTheDocument();
    });
  });

  test('login form displays user not found error', async () => {
    mockFetch.mockResolvedValueOnce({
      ok: false,
      status: 401,
      text: () => Promise.resolve('{"error": "Invalid credentials - user not found"}'),
    });

    renderApp();
    
    const signInButton = screen.getByText('Sign In');
    fireEvent.click(signInButton);
    
    await waitFor(() => {
      expect(screen.getByText('Account not found. Please contact an administrator to create your account.')).toBeInTheDocument();
    });
  });

  test('error message can be dismissed', async () => {
    mockFetch.mockResolvedValueOnce({
      ok: false,
      status: 401,
      text: () => Promise.resolve('{"error": "Invalid credentials - user not found"}'),
    });

    renderApp();
    
    const signInButton = screen.getByText('Sign In');
    fireEvent.click(signInButton);
    
    await waitFor(() => {
      expect(screen.getByText('Account not found. Please contact an administrator to create your account.')).toBeInTheDocument();
    });

    const closeButton = screen.getByLabelText('Close error message');
    fireEvent.click(closeButton);
    
    expect(screen.queryByText('Account not found. Please contact an administrator to create your account.')).not.toBeInTheDocument();
  });

  test('role selector works correctly', () => {
    renderApp();
    
    const roleSelect = screen.getByDisplayValue('Applicant');
    fireEvent.change(roleSelect, { target: { value: 'Administrator' } });
    
    expect(roleSelect).toHaveValue('Administrator');
  });
});