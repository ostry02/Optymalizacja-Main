# Optymalizacja-Main

Projekt z Metod Optymalizacji. Wariant TSP na atrakcjach Wroclawia — sciezka Hamiltona.
Funkcja celu:

```
f = total_distance + penalty * liczba_przejsc_bez_autobusu - alpha * suma_atrakcyjnosci
```

Porownanie dwoch metaheurystyk: **PSO** (permutacyjne, swap-velocity) i **ACO** (klasyczny, eta = (1+attr)/dist).
Tylko standardowa biblioteka Javy, zero zewnetrznych zaleznosci.

---

## Jak odpalic

```
javac *.java
java Main
```

Parametry sa wczytywane z `config.properties` (seed, penalty, alpha, listy instancji, parametry PSO/ACO, liczba powtorzen).
Instancje sa wczytywane z `data/instance_*.csv` (statyczne dane, nie generowane losowo).

Wyniki ladowane do plikow CSV w katalogu projektu:
- `fitness_n<N>.csv` — historia best fitness PSO i ACO per iteracja
- `stats_n<N>.csv` — porownanie statystyczne (mean/std/min/max)
- `taguchi_pso_n<N>.csv`, `taguchi_aco_n<N>.csv` — wyniki strojenia L9

---

## Pliki

| Plik | Opis |
|------|------|
| `Attraction.java` | klasa atrakcji turystycznej (id, name, x, y, attractiveness) |
| `Instance.java` | instancja problemu — lista atrakcji + zbior krawedzi autobusowych, distance, hasBus, loadFromCSV |
| `Evaluation.java` | funkcja celu + breakdown skladowych |
| `Particle.java` | pojedyncza czastka w roju (position, velocity, personalBest) |
| `PSO.java` | Particle Swarm Optimization (permutacja + swap velocity) |
| `ACO.java` | Ant Colony Optimization (feromony + heurystyka eta) |
| `Config.java` | wczytywanie konfiguracji z config.properties |
| `Stats.java` | porownanie statystyczne PSO vs ACO (mean/std/min/max) |
| `Taguchi.java` | strojenie parametrow metoda Taguchi L9 |
| `Main.java` | pipeline: wczytanie instancji -> PSO/ACO -> Stats -> Taguchi |
| `data/instance_*.csv` | 7 statycznych instancji (n=5,8,12,20,30,40,50) |
| `config.properties` | konfiguracja (seed, parametry, lista instancji) |
