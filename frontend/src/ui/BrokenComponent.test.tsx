import { render, screen } from '@testing-library/react';
import { fireEvent } from '@testing-library/dom';

const TestComponent = () => <button>Test</button>;

describe('BrokenComponent', () => {
  test('will fail due to import', () => {
    render(<TestComponent />);
    fireEvent.click(screen.getByText('Test'));
  });
});
