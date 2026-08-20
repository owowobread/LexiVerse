with open(".github/workflows/auto_release.yml", "r") as f:
    content = f.read()

new_step = """      - name: Fix Gradle Wrapper & Make Executable
        if: steps.check_tag.outputs.exists == 'false'
        run: |
          wget -qO gradle/wrapper/gradle-wrapper.jar https://raw.githubusercontent.com/gradle/gradle/master/gradle/wrapper/gradle-wrapper.jar
          chmod +x ./gradlew

      - name: Generate Debug Keystore (If Missing)
        if: steps.check_tag.outputs.exists == 'false'
        run: |
          if [ ! -f "debug.keystore" ]; then
            echo "debug.keystore not found, generating a new one for GitHub Actions..."
            keytool -genkey -v -keystore debug.keystore -storepass android -alias androiddebugkey -keypass android -keyalg RSA -keysize 2048 -validity 10000 -dname "C=US, O=Android, CN=Android Debug"
          fi"""

content = content.replace("      - name: Fix Gradle Wrapper & Make Executable\n        if: steps.check_tag.outputs.exists == 'false'\n        run: |\n          wget -qO gradle/wrapper/gradle-wrapper.jar https://raw.githubusercontent.com/gradle/gradle/master/gradle/wrapper/gradle-wrapper.jar\n          chmod +x ./gradlew", new_step)

with open(".github/workflows/auto_release.yml", "w") as f:
    f.write(content)
