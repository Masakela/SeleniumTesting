pipeline {
    agent any

    tools {
        maven 'Maven3'   // must match a Maven install in Manage Jenkins > Tools
        jdk 'JDK17'      // same for the JDK
    }

    parameters {
        choice(name: 'BROWSER', choices: ['chrome', 'firefox'], description: 'Browser to run against')
    }

    triggers {
        pollSCM('H/5 * * * *')   // build on new commits (checks every ~5 min)
        cron('H 2 * * *')        // nightly run
    }

    stages {
        stage('Checkout') {
            steps { checkout scm }
        }
        stage('Run Login Tests') {
            steps {
                // bat for Windows agents; login.xml scopes to LoginTest only
                bat "mvn -B clean test -DsuiteXmlFile=login.xml -Dbrowser=%BROWSER%"
            }
        }
    }

    post {
        always {
            junit 'target/surefire-reports/*.xml'

            publishHTML(target: [
                reportDir: 'test-output/extent',
                reportFiles: 'ExtentReport.html',
                reportName: 'Extent Report',
                keepAll: true,
                alwaysLinkToLastBuild: true,
                allowMissing: false
            ])

            archiveArtifacts artifacts: 'test-output/screenshots/*.png', allowEmptyArchive: true
        }
    }
}