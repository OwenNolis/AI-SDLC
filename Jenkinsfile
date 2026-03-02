pipeline {
  agent any

  options {
    timestamps()
    ansiColor('xterm')
    disableConcurrentBuilds()
  }

  environment {
    FEATURE_ID = "feature-001-support-ticket"
    GEMINI_API_KEY = credentials('gemini-api-key')
    GEMINI_MODEL  = "gemini-2.5-flash-lite"
  }

  stages {
    stage('Checkout') {
      steps {
        checkout scm
      }
    }

    stage('Tooling') {
      steps {
        sh '''
          node -v || true
          mvn -v || true
          chmod +x ai/flow.sh ai/sync-from-fa.sh ai/generate-tests.sh
        '''
      }
    }

    stage('Validate + Generate + Test (CI)') {
      steps {
        // Als je ook in Jenkins de LLM sync wil doen, moet GEMINI_API_KEY in env staan.
        // Anders kan je de sync overslaan en enkel validate/generate/tests doen.
        sh '''
          set -euo pipefail

          if [ -z "${GEMINI_API_KEY:-}" ]; then
            echo "No GEMINI_API_KEY in Jenkins -> skipping sync, running validate/generate/tests only."
            npm --prefix ai/validator ci
            npm --prefix ai/validator run validate
            ./ai/generate-tests.sh "${FEATURE_ID}"
            (cd backend && mvn test)
            (cd frontend && npm ci && npm test)
          else
            echo "GEMINI_API_KEY present -> running full one-command flow."
            ./ai/flow.sh "${FEATURE_ID}"
          fi
        '''
      }
    }

    stage('Package') {
      steps {
        sh '''
          set -euo pipefail
          cd backend
          mvn -DskipTests package
          cd ../frontend
          npm ci
          npm run build
        '''
      }
    }

    stage('Archive Artifacts') {
      steps {
        archiveArtifacts artifacts: '''
          backend/target/*.jar,
          frontend/dist/**,
          backend/src/test/java/**,
          frontend/src/ui/__generated__/**,
          docs/technical-analysis/**,
          docs/test-scenarios/**,
          docs/test-context/**
        ''', fingerprint: true
      }
    }

    stage('Deploy (sketch)') {
      when {
        branch 'main'
      }
      steps {
        echo "Deploy stage (placeholder)"
        // Voorbeeld:
        // sh 'kubectl apply -f k8s/'
        // sh 'docker push ...'
        // sh 'helm upgrade ...'
      }
    }
  }

  post {
    always {
      junit allowEmptyResults: true, testResults: 'backend/target/surefire-reports/*.xml'
    }
    failure {
      echo "Build failed. Check logs above."
    }
  }
}