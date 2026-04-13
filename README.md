# UHCWorldGenerator

Bienvenue dans **UHCWorldGenerator** !

Ce projet est un generateur de monde UHC concu pour simplifier la pregeneration de mondes pour vos parties UHC. Il inclut des biomes specifiques : **Roofed Forest**, **Taiga** et **Plain**, sans lacs, afin de repondre aux besoins de ce type de jeu. Fini le stress de la pregen, tout est automatise et pret en un rien de temps.


---

## Fonctionnalites

- **Generation ultra rapide** : votre map UHC est generee en moins d'une minute, prete a l'emploi.
- **Biomes en zones concentriques** : Roofed Forest au centre (Dark Oaks denses), Taiga autour (sapins), puis Plains en bordure. Pas de lacs, pas de surprises.
- **Boost des minerais integre** : diamants, or, fer et lapis sont deja boostes avec des multiplicateurs equilibres pour le UHC.
- **Nettoyage automatique** : l'eau, la lave, le sable et le gravier en surface sont supprimes pour un terrain propre et jouable.
- **Aucune structure generee** : pas de donjons, pas de temples, pas d'avantage aleatoire pour un joueur.
- **Protection pendant la generation** : les joueurs sont bloques tant que la map n'est pas prete.
- **Tout est configurable** : chaque parametre est modifiable simplement dans un seul fichier.

---

## Installation

1. Compilez le plugin avec Maven :
   ```bash
   mvn clean package
   ```
2. Placez le `.jar` genere (dans `target/`) dans le dossier `plugins/` de votre serveur **Spigot 1.8.8**.
3. Lancez le serveur. C'est tout.

La generation demarre automatiquement au demarrage. La map `uhc_map` est creee, configuree et prete sans aucune intervention.

---

## Configuration

Tout se configure dans un seul fichier : `GenerationConfig.java`. Pas besoin de toucher au reste du code.

### Zones de biomes

| Parametre | Defaut | Description |
|---|---|---|
| `RADIUS_ROOFED_FOREST` | 400 | Rayon de la zone Roofed Forest (dark oaks) |
| `RADIUS_TAIGA` | 500 | Rayon de la zone Taiga (sapins) |
| `RADIUS_PLAINS` | 700 | Rayon total de la map (plains en bordure) |

### Densite des arbres

| Parametre | Defaut | Description |
|---|---|---|
| `TREE_CHANCE_DARK_OAK` | 0.5 | Chance par bloc de generer un Dark Oak (50%) |
| `TREE_CHANCE_TAIGA` | 0.11 | Chance par bloc de generer un sapin (11%) |

### Minerais

| Minerai | Multiplicateur | Taille | Hauteur |
|---|---|---|---|
| Diamant | x2.3 | 7 | Y 1-16 |
| Or | x2.55 | 9 | Y 1-32 |
| Fer | x2.0 | 9 | Y 1-64 |
| Lapis | x2.22 | 7 | Centre Y16, spread 16 |

### Nettoyage & Performance

| Parametre | Defaut | Description |
|---|---|---|
| `RADIUS_WATER_FIX` | 500 | Rayon de nettoyage eau/lave/sable/gravier |
| `WATER_FIX_MIN_Y` | 50 | Hauteur min du nettoyage (les grottes en dessous sont preservees) |
| `CHUNKS_PER_TICK` | 15 | Chunks traites par tick (plus = plus rapide, plus de lag) |

Modifiez les valeurs, recompilez, et c'est bon. Rien de plus.


---

## Ajustements non inclus

Pour des raisons de simplicite (et un peu de flemme...), le boost du nombre et de la taille des caves n'a pas ete inclus.

---

## Credits

Un immense merci a :
- **Skrii**
- **Androzz**
- **Snowty**

Votre aide et vos conseils ont ete precieux pour la realisation de ce projet. Je vous fais de gros bisous.
