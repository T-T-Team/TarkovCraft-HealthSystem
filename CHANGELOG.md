# Release 1.21.8-1.5.0
- Added Pain status effect - appears when player is hurt or has any status effect causing pain, such as fractures
- Added Wound status effect - not displayed anywhere, is based on received damage amount and causes pain
- Added group status effects (Positive,Neutral,Negative) - allows to add multiple additional effects into one
- Reworked status effect format and implemented new delayed effect scheduler
- Added command to allow damaging specific body parts - `/tarkovcraft hurt <targets> <limb> <damage_type> <amount> [causing_entity] [direct_entity]`
- Overweight status effects now show correctly when carry weight exceeds weight limit
- Fixed issue where 100% side effects could be ignored if player had high enough skill level