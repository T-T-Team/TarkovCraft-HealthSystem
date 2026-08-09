# Release 2.11.0+1.21.1
- Updated to latest core release - requires version 2.9.0 or newer
- Added skill leveling limits for damage based skills such as resilience or armors
  - This fixes issue where you could immediately level up from excessive damage given by some mods
- Reduced death event priority for better compatibility with mods which can cancel death events
- Improved damage handling to hopefully prevent crashes with various mods
- Improved API for unconscious mode to allow animation overrides