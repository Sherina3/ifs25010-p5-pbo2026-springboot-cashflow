// Pipeline CI/CD: Build & Test, Jacoco 100%, Trivy Scan, Package, Publish, Deploy.
// Nilai kredensial/registry/server disesuaikan dengan konfigurasi Jenkins di lingkungan masing-masing.
pipeline {
    agent any

    environment {
        APP_NAME  = 'ifs25010-p5-pbo2026-springboot-cashflow'
        IMAGE_TAG = "${env.BUILD_NUMBER}"
    }

    options {
        timestamps()
        disableConcurrentBuilds()
    }

    stages {
        stage('Build & Test') {
            steps {
                sh 'chmod +x mvnw'
                sh './mvnw -B clean test'
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'
                }
            }
        }

        stage('Jacoco 100%') {
            steps {
                // Gagal bila coverage counter terukur < 100% (view & auth dikecualikan di pom.xml)
                sh './mvnw -B verify'
            }
            post {
                always {
                    archiveArtifacts allowEmptyArchive: true, artifacts: 'target/site/jacoco/**'
                }
            }
        }

        stage('Trivy Scan') {
            steps {
                sh 'trivy fs --exit-code 1 --severity HIGH,CRITICAL --skip-dirs node_modules,target .'
            }
        }

        stage('Package') {
            steps {
                sh './mvnw -B clean package -Pproduction -DskipTests'
            }
        }

        stage('Publish') {
            steps {
                archiveArtifacts artifacts: "target/${APP_NAME}-*.jar", fingerprint: true
            }
        }

        stage('Deploy') {
            when {
                branch 'main'
            }
            steps {
                withCredentials([
                    string(credentialsId: 'cashflow-db-host', variable: 'DB_HOST'),
                    string(credentialsId: 'cashflow-db-password', variable: 'DB_PASSWORD'),
                    string(credentialsId: 'cashflow-session-secret', variable: 'SESSION_SECRET')
                ]) {
                    sh '''
                        mkdir -p /opt/${APP_NAME}
                        cp target/${APP_NAME}-*.jar /opt/${APP_NAME}/app.jar
                        # Jalankan ulang service; DB_*, ROOT_*, UPLOAD_DIR diset lewat environment server.
                        sudo systemctl restart ${APP_NAME} || echo "Atur service ${APP_NAME} di server terlebih dahulu"
                    '''
                }
            }
        }
    }

    post {
        success { echo "Pipeline ${APP_NAME} #${IMAGE_TAG} berhasil." }
        failure { echo "Pipeline ${APP_NAME} #${IMAGE_TAG} gagal." }
    }
}
