# Optymalizacja-Main

Projekt z Metod Optymalizacji. Wariant TSP na atrakcjach Wroclawia — sciezka Hamiltona.
Funkcja celu:

```
f = total_distance + penalty * liczba_przejsc_bez_autobusu - alpha * suma_atrakcyjnosci
```

Porownanie dwoch metaheurystyk: **PSO** (permutacyjne, swap-velocity) i **ACO** (klasyczny, eta = (1+attr)/dist).

Parametry sa wczytywane z `config.properties` (seed, penalty, alpha, listy instancji, parametry PSO/ACO, liczba powtorzen). Wyniki sa reprodukowalne dzieki staremu seedowi bazowemu.
Instancje sa wczytywane z `data/instance_*.csv`

Wyniki ladowane do plikow CSV w folderze `results/`:
- `results/results_n<N>.csv` — historia best fitness PSO i ACO per iteracja
- `results/stats_n<N>.csv` — porownanie statystyczne (mean/std/min/max)
- `results/taguchi_pso_n<N>.csv`, `results/taguchi_aco_n<N>.csv` — wyniki strojenia L9

## Struktura projektu

```
Optymalizacja-Main/
├── Main.java                # punkt wejscia
├── config.properties        # parametry eksperymentu
├── README.md
├── model/                   # dane problemu
│   ├── Attraction.java      # atrakcja (id, name, x, y, attractiveness)
│   ├── Instance.java        # instancja problemu, loadFromCSV, distance, hasBus
│   └── Evaluation.java      # funkcja celu
├── algorithms/              # metaheurystyki
│   ├── Particle.java        # czastka (position, velocity, personalBest)
│   ├── PSO.java             # Particle Swarm Optimization
│   └── ACO.java             # Ant Colony Optimization
├── experiments/             # uruchamianie i strojenie
│   ├── Config.java          # wczytywanie config.properties
│   ├── Stats.java           # porownanie statystyczne (mean/std/min/max)
│   └── Taguchi.java         # strojenie parametrow metoda Taguchi L9
├── data/                    # statyczne instancje CSV
│   └── instance_*.csv
└── results/                 # wyniki
    └── *.csv
```

---

## Pliki

| Plik | Paczka | Opis |
|------|--------|------|
| `Main.java` | (root) | pipeline: wczytanie instancji -> PSO/ACO -> Stats -> Taguchi |
| `Attraction.java` | model | klasa atrakcji turystycznej (id, name, x, y, attractiveness) |
| `Instance.java` | model | instancja problemu — lista atrakcji + zbior krawedzi autobusowych, distance, hasBus, loadFromCSV |
| `Evaluation.java` | model | funkcja celu + breakdown skladowych |
| `Particle.java` | algorithms | pojedyncza czastka w roju (position, velocity, personalBest) |
| `PSO.java` | algorithms | Particle Swarm Optimization (permutacja + swap velocity) |
| `ACO.java` | algorithms | Ant Colony Optimization (feromony + heurystyka eta) |
| `Config.java` | experiments | wczytywanie konfiguracji z config.properties |
| `Stats.java` | experiments | porownanie statystyczne PSO vs ACO (mean/std/min/max) |
| `Taguchi.java` | experiments | strojenie parametrow metoda Taguchi L9 |
| `data/instance_*.csv` | — | 7 statycznych instancji (n=5,8,12,20,30,40,50) |
| `config.properties` | — | konfiguracja (parametry, lista instancji, liczba przebiegow) |
