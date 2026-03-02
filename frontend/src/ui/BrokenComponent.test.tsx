import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react'; // OLD IMPORT - WILL FAIL

const TestComponent = () => <button>Test</button>;

describe('BrokenComponent', () => {
  test('will fail due to import', () => {
    render(<TestComponent />);
    fireEvent.click(screen.getByText('Test')); // WILL FAIL - fireEvent not from right package
  });
});
