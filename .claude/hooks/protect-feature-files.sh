#!/bin/bash
# protect-feature-files.sh
# Blocks direct edits to feature/ files unless a skill is active.
# Used as a PreToolUse hook on Edit|Write tool calls.
#
# Skills must create the marker file before editing feature files:
#   touch /tmp/.claude-kmpilot-skill-active
# And clean up when done:
#   rm -f /tmp/.claude-kmpilot-skill-active

SKILL_MARKER="/tmp/.claude-kmpilot-skill-active"

INPUT=$(cat)
FILE_PATH=$(echo "$INPUT" | jq -r '.tool_input.file_path // empty')

# Only check files under feature/ directories
if [[ "$FILE_PATH" == *"/feature/"* ]] || [[ "$FILE_PATH" == feature/* ]]; then
  # Allow test files (commonTest, desktopTest, androidTest) - test agents write these directly
  if [[ "$FILE_PATH" == *"/commonTest/"* ]] || [[ "$FILE_PATH" == *"/desktopTest/"* ]] || [[ "$FILE_PATH" == *"/androidTest/"* ]] || [[ "$FILE_PATH" == *"/test/"* ]]; then
    exit 0
  fi

  # Allow build.gradle.kts edits (test dependency setup, integration agent)
  if [[ "$FILE_PATH" == *"build.gradle.kts"* ]]; then
    exit 0
  fi

  # Allow edits when a skill is active (marker file exists and is recent)
  if [[ -f "$SKILL_MARKER" ]]; then
    # Staleness check: marker must be less than 2 hours old
    if [[ "$(uname)" == "Darwin" ]]; then
      marker_age=$(( $(date +%s) - $(stat -f %m "$SKILL_MARKER") ))
    else
      marker_age=$(( $(date +%s) - $(stat -c %Y "$SKILL_MARKER") ))
    fi
    if [[ "$marker_age" -lt 7200 ]]; then
      exit 0
    else
      rm -f "$SKILL_MARKER"
    fi
  fi

  # Block direct feature source edits - must use skills
  echo "Blocked: Cannot edit feature source files directly. Use /creating-kmp-feature or /modifying-kmp-feature skill first." >&2
  exit 2
fi

exit 0
