# Optymalizacja-Main

Projekt z Metod Optymalizacji. Wariant TSP na atrakcjach Wroclawia — sciezka Hamiltona.
Funkcja celu:

```
f = total_distance + penalty * liczba_przejsc_bez_autobusu - alpha * suma_atrakcyjnosci
```

## Konfiguracja

Parametry sa wczytywane z `config.properties`:

- `seed` — ziarno generatora,
- `penalty`, `alpha` — wagi skladnikow funkcji celu,
- `datasets` — lista  datasetow do uruchomienia,
- `taguchi.replications` — liczba powtorzen w strojeniu Taguchi.

## Wyniki

Wyniki ladowane do plikow CSV w folderze `results/`