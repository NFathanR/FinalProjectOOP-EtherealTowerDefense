# Ethereal Tower Defense

## Controls (Temporary)
- `1`: select cannon placement
- `2`: select mortar placement
- `3`: select enemy placement (debug spawn)
- Left click: confirm placement
- Right click or `Esc`: cancel placement
- `Space`: pause / resume simulation

## Design patterns
- Factory: `TowerFactory`, `EnemyFactory`
- Object Pool: `EnemyPool`, `ProjectilePool`
- Strategy: `AttackStrategy`, `CannonStrategy`, `MortarStrategy`; towers can replace their strategy via `setStrategy`.
- Observer: `GameEvents`, `GameObserver`, `GameEvent`
- Command: `BuyTowerCommand`, `PlaceTowerCommand`, `SpawnEnemyCommand`
- State: `GameState`, `PlayingState`, `PlacementState`, `PausedState`, `GameOverState`
