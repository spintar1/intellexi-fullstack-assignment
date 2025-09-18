import React from 'react';
/// <reference types="jest" />
/// <reference types="@testing-library/jest-dom" />

import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';
import Races from '../../pages/Races';
import { RaceProvider } from '../../context/RaceContext';

// Mock the race context
const mockRaceContext = {
  races: [
    { id: '1', name: 'Boston Marathon', distance: 'Marathon' },
    { id: '2', name: 'Central Park 5K', distance: '5k' },
  ],
  loading: false,
  error: null,
  refreshRaces: jest.fn(),
  addRaceOptimistically: jest.fn(),
  updateRaceOptimistically: jest.fn(),
};

const mockFetch = jest.fn();
(globalThis as any).fetch = mockFetch;

// Mock the RaceContext
jest.mock('../../context/RaceContext', () => ({
  RaceProvider: ({ children }: { children: React.ReactNode }) => children,
  useRaces: () => mockRaceContext,
}));

describe('Races Component', () => {
  const defaultProps = {
    apiQuery: 'http://localhost:8082',
    apiCommand: 'http://localhost:8081',
    token: 'mock-token',
  };

  beforeEach(() => {
    jest.clearAllMocks();
    Object.assign(mockRaceContext, {
      loading: false,
      error: null,
      races: [
        { id: '1', name: 'Boston Marathon', distance: 'Marathon' },
        { id: '2', name: 'Central Park 5K', distance: '5k' },
      ],
    });
  });

  test('renders races list when data is loaded', () => {
    render(<Races {...defaultProps} />);
    
    expect(screen.getByText('Available Races')).toBeInTheDocument();
    expect(screen.getByText('Boston Marathon')).toBeInTheDocument();
    expect(screen.getByText('Central Park 5K')).toBeInTheDocument();
  });

  test('displays loading state', () => {
    Object.assign(mockRaceContext, {
      loading: true,
      races: [],
    });
    
    render(<Races {...defaultProps} />);
    
    expect(screen.getByText('Loading available races...')).toBeInTheDocument();
  });

  test('displays error state', () => {
    Object.assign(mockRaceContext, {
      loading: false,
      error: 'Failed to load races',
    });
    
    render(<Races {...defaultProps} />);
    
    expect(screen.getByText('Failed to load races')).toBeInTheDocument();
  });

  test('displays login message when no token', () => {
    render(<Races {...{ ...defaultProps, token: null }} />);
    
    expect(screen.getByText('Authentication Required')).toBeInTheDocument();
    expect(screen.getByText('Please login above to view and register for races')).toBeInTheDocument();
  });

  test('can select a race for registration', () => {
    render(<Races {...defaultProps} />);
    
    const raceCard = screen.getByText('Boston Marathon').closest('.race-card');
    fireEvent.click(raceCard!);
    
    expect(screen.getByText('Register for Boston Marathon')).toBeInTheDocument();
    expect(screen.getByText('🏅 Register for Race')).toBeInTheDocument();
  });

  test('can go back from registration form', () => {
    render(<Races {...defaultProps} />);
    
    // Select a race
    const raceCard = screen.getByText('Boston Marathon').closest('.race-card');
    fireEvent.click(raceCard!);
    
    // Go back
    const backButton = screen.getByText('← Back to Races');
    fireEvent.click(backButton);
    
    expect(screen.getByText('Available Races')).toBeInTheDocument();
    expect(screen.queryByText('Register for Boston Marathon')).not.toBeInTheDocument();
  });

  test('submits registration successfully', async () => {
    // Mock alert before rendering
    const alertMock = jest.fn();
    window.alert = alertMock;
    
    // Mock localStorage for user email
    (globalThis as any).localStorage = {
      getItem: jest.fn(() => 'test@example.com'),
      setItem: jest.fn(),
    };

    // Mock successful application creation
    mockFetch.mockResolvedValueOnce({
      ok: true,
      json: () => Promise.resolve({ id: 'app-1', message: 'Success' }),
      text: () => Promise.resolve(''),
    });

    // Mock successful verification API call
    mockFetch.mockResolvedValueOnce({
      ok: true,
      json: () => Promise.resolve([{ 
        id: 'app-1', 
        raceId: '1', 
        applicantEmail: 'test@example.com' 
      }]),
      text: () => Promise.resolve(''),
    });

    render(<Races {...defaultProps} />);
    
    // Select a race
    const raceCard = screen.getByText('Boston Marathon').closest('.race-card');
    fireEvent.click(raceCard!);
    
    // Submit registration
    const submitButton = screen.getByText('🏅 Register for Race');
    fireEvent.click(submitButton);
    
    await waitFor(() => {
      expect(mockFetch).toHaveBeenCalledWith(
        'http://localhost:8081/api/v1/applications',
        expect.objectContaining({
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': 'Bearer mock-token',
          },
          body: JSON.stringify({ raceId: '1' }),
        })
      );
    });

    await waitFor(() => {
      expect(alertMock).toHaveBeenCalledWith('✅ Successfully registered for the race!');
    }, { timeout: 3000 });
  });

  test('displays error message on registration failure', async () => {
    mockFetch.mockResolvedValueOnce({
      ok: false,
      status: 400,
      json: () => Promise.resolve({ error: 'Already registered' }),
      text: () => Promise.resolve('Already registered'),
    });

    render(<Races {...defaultProps} />);
    
    // Select a race
    const raceCard = screen.getByText('Boston Marathon').closest('.race-card');
    fireEvent.click(raceCard!);
    
    // Submit registration
    const submitButton = screen.getByText('🏅 Register for Race');
    fireEvent.click(submitButton);
    
    await waitFor(() => {
      expect(screen.getByText(/Already registered/i)).toBeInTheDocument();
    });
  });

  test('displays network error message', async () => {
    mockFetch.mockRejectedValueOnce(new Error('Network error'));

    render(<Races {...defaultProps} />);
    
    // Select a race
    const raceCard = screen.getByText('Boston Marathon').closest('.race-card');
    fireEvent.click(raceCard!);
    
    // Submit registration
    const submitButton = screen.getByText('🏅 Register for Race');
    fireEvent.click(submitButton);
    
    await waitFor(() => {
      expect(screen.getByText(/Network error/i)).toBeInTheDocument();
    });
  });

  test('shows loading state during registration', async () => {
    mockFetch.mockImplementation(() => new Promise(() => {})); // Never resolves

    render(<Races {...defaultProps} />);
    
    // Select a race
    const raceCard = screen.getByText('Boston Marathon').closest('.race-card');
    fireEvent.click(raceCard!);
    
    // Submit registration
    const submitButton = screen.getByText('🏅 Register for Race');
    fireEvent.click(submitButton);
    
    expect(screen.getByText('⏳ Registering...')).toBeInTheDocument();
  });
});
