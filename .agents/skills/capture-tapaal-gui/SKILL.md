---
name: capture-tapaal-gui
description: Reproduce TAPAAL GUI behavior and capture visual evidence. Use when interacting with the Swing app or creating screenshots and videos for bug reports, issues, or pull requests.
---

# Capture the TAPAAL GUI

Use a dedicated Xvfb display so the app, interaction commands, and capture process share one stable desktop.

## Prerequisites

Check the required tools first:

```bash
for tool in Xvfb xdpyinfo xdotool ffmpeg; do
  command -v "$tool" >/dev/null || echo "Missing: $tool"
done
```

If any are missing, stop and tell the user which tools are unavailable. On Debian/Ubuntu, suggest the following command and ask for permission before running it:

```bash
sudo apt-get update && sudo apt-get install -y xvfb x11-utils xdotool ffmpeg
```

Continue when every `command -v` check succeeds. `xdotool` drives the UI and `ffmpeg` captures it.

## Start and interact

```bash
mkdir -p build/captures
Xvfb :91 -screen 0 1440x900x24 -ac >build/captures/xvfb.log 2>&1 &
xvfb_pid=$!
export DISPLAY=:91
for attempt in {1..50}; do
  xdpyinfo >/dev/null 2>&1 && break
  sleep 0.1
done
xdpyinfo >/dev/null 2>&1 || { echo "Xvfb did not start"; kill "$xvfb_pid"; exit 1; }
./gradlew run >build/captures/app.log 2>&1 &
app_pid=$!
window_id=$(xdotool search --sync --onlyvisible --name 'TAPAAL' | head -n 1)
xdotool windowfocus --sync "$window_id"
```

To open a fixture directly, replace the `./gradlew run` launch above with:

```bash
./gradlew run --args='"src/main/resources/Example nets/intro-example.tapn"'
```

The main window appears before a requested model finishes loading. Wait for the target state before interacting or capturing; for the example above:

```bash
for attempt in {1..100}; do
  window_title=$(xdotool getwindowname "$window_id")
  [[ "$window_title" == *intro-example.tapn* ]] && break
  sleep 0.1
done
[[ "$window_title" == *intro-example.tapn* ]] || { echo "Model did not load"; exit 1; }
```

Prefer stable keyboard shortcuts and text entry. Use `xdotool getwindowgeometry "$window_id"` before coordinate clicks, then record the exact interactions needed to reproduce the behavior. Keep issue/PR evidence in the ignored `build/captures/` directory unless the user requests another destination.

## Screenshot

Capture the whole virtual display after arranging the relevant window and dialogs:

```bash
ffmpeg -y -f x11grab -video_size 1440x900 -i "$DISPLAY" \
  -frames:v 1 build/captures/tapaal.png
```

Inspect the PNG and retake it if the target state, cursor, menus, or modal dialogs are unclear.

## Video

Start recording before the shortest reliable reproduction:

```bash
ffmpeg -nostdin -y -f x11grab -framerate 30 -video_size 1440x900 -i "$DISPLAY" \
  -c:v libx264 -preset veryfast -crf 23 -pix_fmt yuv420p \
  build/captures/tapaal.mp4 >build/captures/ffmpeg.log 2>&1 &
recording_pid=$!
```

After reproducing the behavior, stop recording cleanly so the MP4 trailer is written:

```bash
kill -INT "$recording_pid"
wait "$recording_pid" || true
ffprobe -v error build/captures/tapaal.mp4 >/dev/null
```

Review the resulting video. Trim the interaction plan and re-record when it includes unrelated setup, idle time, notifications, or sensitive data.

## Finish

Stop the app and X server, then report the evidence paths and a concise reproduction sequence:

```bash
kill "$app_pid" 2>/dev/null || true
kill "$xvfb_pid" 2>/dev/null || true
```
