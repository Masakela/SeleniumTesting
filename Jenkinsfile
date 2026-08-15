// Minimal declarative pipeline — the CI half of the story.
// The key idea: `mvn test` runs the suite on every build, and a failing test
// fails the build, so broken code can't merge. This is what makes the automation
// a QUALITY GATE rather than a script someone runs manually.
pipeline {
    agent any

    tools {
        maven 'Maven3'   // must match a Maven install in Manage Jenkins > Global Tool Config
        jdk 'JDK17'      // same for the JDK
    }

    parameters {
        choice(name: 'BROWSER', choices: ['chrome', 'firefox'], description: 'Browser to run against')
    }

    triggers {
        // Run nightly at 2am in addition to on-demand / on-PR triggers.
        cron('H 2 * * *')
    }

    stages {
        stage('Checkout') {
            steps { checkout scm }
        }
        stage('Run Login Tests') {
            steps {
                // login.xml scopes the run to LoginTest only;
                // -Dbrowser overrides the suite parameter (see BaseTest).
                sh "mvn -B clean test -DsuiteXmlFile=login.xml -Dbrowser=${params.BROWSER}"
            }
        }
    }

    post {
        always {
            // TestNG results in the Jenkins UI
            junit 'target/surefire-reports/*.xml'

            // Extent HTML report
            publishHTML(target: [
                reportDir: 'test-output/extent',
                reportFiles: 'ExtentReport.html',
                reportName: 'Extent Report',
                keepAll: true,
                alwaysLinkToLastBuild: true,
                allowMissing: false
            ])

            // Screenshots-on-failure archived as build artifacts
            archiveArtifacts artifacts: 'test-output/screenshots/*.png', allowEmptyArchive: true
        }
    }
}