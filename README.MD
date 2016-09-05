This Xposed module mutes MTCRadio as soon as any app tries to open an audio session or obtain
Audio Focus.
It also unmutes the radio as soon as the user returns to the radio Activity.

This way we can change between e.g. Spotify and MTC Radio without having to exit MTCRadio to mute it.

This was tested in a Pumpkin KD-0250 running AutoMate.