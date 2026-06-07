---
description: Generate and open coverage reports for all features
allowed-tools: Bash(./gradlew:*), Bash(open:*), Bash(ls:*), Bash(for:*)
model: haiku
---

Generate coverage reports and open them for all feature modules.

Execute these steps:

1. Detect features: List all feature modules by checking `feature/*/build.gradle.kts`
2. Run: `./gradlew koverHtmlReport`
3. Open all feature reports dynamically:
   ```bash
   for f in feature/*/build.gradle.kts; do
     name=$(dirname "$f" | xargs basename)
     open "feature/$name/build/reports/kover/html/index.html" 2>/dev/null
   done
   ```

Confirm completion with: "✅ Coverage reports generated and opened for all features (ViewModels, Repositories, DataSources, Screens only)"
