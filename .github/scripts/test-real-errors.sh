#!/bin/bash
# Create real errors and test the AI fixing workflow end-to-end

echo "🧪 Testing AI Code Fixes with Real Errors"
echo ""

# 1. Create Spring Boot error
echo "1. Creating Spring Boot TestRestTemplate error..."
mkdir -p backend/src/test/java/be/ap/student/tickets
cat > backend/src/test/java/be/ap/student/tickets/BrokenTest.java << 'EOF'
package be.ap.student.tickets;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate; // OLD IMPORT - WILL FAIL
import org.springframework.beans.factory.annotation.Autowired;
import org.junit.jupiter.api.Test;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BrokenTest {
    @Autowired
    private TestRestTemplate restTemplate; // WILL CAUSE COMPILATION ERROR
    
    @Test
    public void testWillFail() {
        // This test will fail due to import error
    }
}
EOF

# 2. Create React Testing Library error  
echo "2. Creating React Testing Library error..."
mkdir -p frontend/src/ui
cat > frontend/src/ui/BrokenComponent.test.tsx << 'EOF'
import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react'; // OLD IMPORT - WILL FAIL

const TestComponent = () => <button>Test</button>;

describe('BrokenComponent', () => {
  test('will fail due to import', () => {
    render(<TestComponent />);
    fireEvent.click(screen.getByText('Test')); // WILL FAIL - fireEvent not from right package
  });
});
EOF

# 3. Remove required dependency
echo "3. Temporarily removing @testing-library/dom dependency..."
cd frontend
npm uninstall @testing-library/dom --save-dev 2>/dev/null || true
cd ..

echo ""
echo "✅ Errors created! Now commit and push:"
echo ""
echo "git add ."
echo "git commit -m 'Test AI fixes: introduce compilation errors'"  
echo "git push"
echo ""
echo "The AI Code Fixes workflow will:"
echo "1. Detect SDLC flow failures"
echo "2. Analyze the errors"
echo "3. Apply automated fixes"
echo "4. Create a PR with the fixes"
echo ""
echo "Check GitHub Actions tab to see the workflow in action!"