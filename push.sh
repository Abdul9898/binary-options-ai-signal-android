#!/bin/bash
set -e

echo "🚀 Committing and pushing changes to GitHub..."
git add .
git commit -m "Update Binary Options AI Signal Android App" || echo "No changes to commit"
git push -u origin main || git push -u origin master
echo "✅ Pushed to GitHub! Check the Actions tab on GitHub to download your new APK."
