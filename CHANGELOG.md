# Release 1.21.10-1.8.0
- Implemented rescue system
  - Instead of immediately dying you will be unconscious and waiting for rescue
  - Default rescue wait time is 2.5 minutes
  - Other players can rescue you by using First Aid kits on you
  - Upon entering unconscious mode, you will be invulnerable for short time, but otherwise you can still be finished off
  - Added 4 new related config options:
    - `unconsciousOnDeathChance` - chance of entering rescue wait mode instead of dying, default 100%
    - `rescueWaitDuration` - how long you will wait for rescue, default 2.5 minutes
    - `rescueInvulnerabilityGracePeriod` - how long you will be invulnerable after entering rescue mode, default 5 seconds
    - `allowUnconsciousOnHeadDeath` - if losing head body part also allows rescue, default True
- Updated Core library, now requires 1.6.3+ version
- **Internal health container structure has been updated and is not compatible with save data - your health will be 
reset with this update**