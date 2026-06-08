import { render, screen, fireEvent } from '@testing-library/react';
import { describe, test } from '@jest/globals';

const TestComponent = () => <button>Test</button>;

describe('BrokenComponent', () => {
  test('will fail due to import', () => {
    render(<TestComponent />);
    fireEvent.click(screen.getByText('Test'));
  });
});
