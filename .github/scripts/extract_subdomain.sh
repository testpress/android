#!/bin/bash

INPUT_URLS=$1
if [[ -z "$INPUT_URLS" ]]; then
  echo "Error: No URL(s) provided"
  exit 1
fi

# Split by comma and handle each URL
IFS=',' read -ra ADDR <<< "$INPUT_URLS"
SUBDOMAINS=()

for URL in "${ADDR[@]}"; do
  # Trim whitespace
  URL=$(echo "$URL" | xargs)
  if [[ -z "$URL" ]]; then continue; fi

  # Strip protocol and trailing paths
  CLEAN_URL=$(echo "$URL" | sed -e 's|^[^/]*//||' -e 's|/.*$||')
  echo "Processing URL: $CLEAN_URL"

  if [[ "$CLEAN_URL" == *".testpress.in" ]]; then
    FINAL_URL="$CLEAN_URL"
  else
    # Get CNAME lookup
    DIG_RESULT=$(dig "$CLEAN_URL" CNAME +short 2>/dev/null | sed 's/\.$//')
    FINAL_URL=$(echo "$DIG_RESULT" | grep "testpress.in" | head -n 1)
  fi

  if [[ -z "$FINAL_URL" ]]; then
    echo "::error::Could not determine testpress subdomain for $URL"
    exit 1
  fi

  SUBDOMAIN=$(echo "$FINAL_URL" | cut -d'.' -f1)
  echo "Extracted Subdomain: $SUBDOMAIN"
  SUBDOMAINS+=("$SUBDOMAIN")
done

# Join with comma
RESULT=$(IFS=, ; echo "${SUBDOMAINS[*]}")
echo "result=$RESULT" >> $GITHUB_OUTPUT
echo "SUBDOMAIN=$RESULT" >> $GITHUB_ENV
# For backward compatibility if single subdomain is expected in SUBDOMAINS env
echo "SUBDOMAINS=$RESULT" >> $GITHUB_ENV
