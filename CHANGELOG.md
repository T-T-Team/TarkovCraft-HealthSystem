# Release 1.21.10-1.8.0
- Implemented rescue system
  - Instead of immediately dying, you will be unconscious and waiting for rescue
  - Default rescue wait time is 2.5 minutes
  - Other players can rescue you by using First Aid kits on you
  - Hostile mobs should ignore players who are unconscious
  - Added 3 new related config options:
    - `unconsciousOnDeathChance` - chance of entering rescue wait mode instead of dying, default 100%
    - `rescueWaitDuration` - how long you will wait for rescue, default 2.5 minutes
    - `allowUnconsciousOnHeadDeath` - if losing head body part also allows rescue, default True
- Updated Core library, now requires 1.6.3+ version
- **Internal health container structure has been updated and is not compatible with save data - your health will be 
reset with this update**