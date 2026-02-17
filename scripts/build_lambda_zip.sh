#!/usr/bin/env bash
set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
PACKAGE_DIR="$PROJECT_ROOT/lambda_pkg"
ZIP_FILE="$PROJECT_ROOT/lambda_pkg.zip"
JAR_FILE="$PROJECT_ROOT/target/cab-booking-lambda.jar"

cd "$PROJECT_ROOT"

echo "[1/4] Building shaded Lambda jar..."
if ! command -v mvn >/dev/null 2>&1; then
  echo "Error: Maven is not installed. Install it first: sudo apt update && sudo apt install -y maven"
  exit 1
fi

mvn -q -DskipTests clean package

if [[ ! -f "$JAR_FILE" ]]; then
  echo "Error: Expected jar not found at $JAR_FILE"
  exit 1
fi

echo "[2/4] Preparing lambda_pkg directory..."
rm -rf "$PACKAGE_DIR"
mkdir -p "$PACKAGE_DIR"
(
  cd "$PACKAGE_DIR"
  jar xf "$JAR_FILE"
)

echo "[3/4] Creating deployment manifest..."
cat > "$PACKAGE_DIR/DEPLOYMENT_INFO.txt" << 'EOF'
Handler: api.LambdaHandler::handleRequest
Runtime: java17
Package: lambda_pkg.zip (classes and dependencies at zip root)
EOF

echo "[4/4] Creating lambda_pkg.zip..."
rm -f "$ZIP_FILE"
(
  cd "$PACKAGE_DIR"
  zip -q -r "$ZIP_FILE" .
)

echo "Done. Upload this file to Lambda: $ZIP_FILE"
echo "Set handler to: api.LambdaHandler::handleRequest"
