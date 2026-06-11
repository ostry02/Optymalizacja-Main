#!/usr/bin/env python3
"""Generator datasetow atrakcji Wroclawia dla PSO/ACO.

Dla kazdego rozmiaru (100, 200, 500) powstaja DWA pliki o tych samych
punktach (wspolrzedne + atrakcyjnosc), roznice tylko w GESTOSCI SIECI
AUTOBUSOWEJ:
  - *_dense  : kazdy wezel polaczony z K_DENSE najblizszymi  -> duzo polaczen
  - *_sparse : kazdy wezel polaczony z K_SPARSE najblizszymi -> rzadka siec

Format pliku jest zgodny z model.Instance.loadFromCSV:
  # Attractions: id,name,x,y,attractiveness
  id,name,x,y,attractiveness
  ...
  # Bus Edges: i,j
  i,j
  ...
"""
import math
import random

SIZES = [100, 200, 500]
K_DENSE = 10          # gesta siec autobusowa
K_SPARSE = 3          # rzadka siec autobusowa
ATTR_MIN, ATTR_MAX = 0.1, 1.0
X_RANGE = (20.0, 65.0)   # zakres wspolrzednych jak w istniejacych instancjach
Y_RANGE = (45.0, 90.0)
SEED = 2026

# realne wroclawskie atrakcje (maja pierwszenstwo w nazewnictwie)
LANDMARKS = [
    "Rynek Glowny", "Hala Targowa", "Ostrow Tumski", "Zoo Wroclaw", "Sky Tower",
    "Hala Stulecia", "Panorama Raclawicka", "Ossolineum", "Hydropolis", "Afrykarium",
    "Aquapark", "Katedra Wroclawska", "Ratusz", "Nowy Targ", "Plac Solny",
    "Wyspa Slodowa", "Opera Wroclawska", "Filharmonia", "Teatr Polski", "Dworzec Glowny",
    "Uniwersytet Wroclawski", "Stadion Miejski", "Park Szczytnicki", "Ogrod Botaniczny",
    "Most Grunwaldzki", "Galeria Dominikanska", "Wzgorze Partyzantow", "Cmentarz Osobowicki",
    "Synagoga Pod Bialym Bocianem", "Zamek Krolewski", "Muzeum Narodowe", "Muzeum Miejskie",
    "Muzeum Architektury", "Muzeum Etnograficzne", "Centrum Historii", "Park Poludniowy",
    "Park Grabiszynski", "Fontanna Multimedialna", "Kosciol sw Elzbiety",
    "Kosciol sw Doroty", "Kosciol sw Krzysztofa", "Biblioteka Uniwersytecka",
    "Brama Tumska", "Bulwar Xawerego Dunikowskiego", "Galeria Foto-Medium-Art",
]

# generator nazw kombinatorycznych: TYP DZIELNICA (jak w istniejacych plikach)
TYPES = [
    "Kosciol", "Park", "Muzeum", "Plac", "Most", "Wieza", "Galeria", "Skwer",
    "Bulwar", "Ogrod", "Brama", "Hala", "Pomnik", "Rynek", "Teatr", "Kino",
    "Palac", "Dworek", "Kamienica", "Kaplica", "Fontanna", "Zaulek", "Centrum",
    "Arena", "Aleja", "Zamek", "Wyspa", "Skwer", "Aquapark", "Stadion",
]
DISTRICTS = [
    "Krzyki", "Psie Pole", "Sloneczny", "Zielony", "Wschodni", "Zachodni",
    "Gorny", "Dolny", "Maly", "Wielki", "Nowy", "Stary", "Bialy", "Czerwony",
    "Biskupin", "Sepolno", "Swojczyce", "Zacisze", "Brochow", "Oltaszyn",
    "Karlowice", "Klecina", "Borek", "Nadodrzanski",
]


def build_names(n, rng):
    """Zwraca n unikalnych nazw: najpierw landmarki, potem TYP DZIELNICA."""
    names = list(LANDMARKS)
    combos = [f"{t} {d}" for t in TYPES for d in DISTRICTS]
    rng.shuffle(combos)
    for c in combos:
        if len(names) >= n:
            break
        if c not in names:
            names.append(c)
    # awaryjnie (gdyby kombinacji bylo za malo) - sufiks numeryczny
    i = 1
    while len(names) < n:
        names.append(f"Punkt {i}")
        i += 1
    return names[:n]


def knn_edges(points, k):
    """Nieskierowane krawedzie: kazdy wezel laczony z k najblizszymi sasiadami."""
    n = len(points)
    edges = set()
    for i in range(n):
        xi, yi = points[i]
        dists = []
        for j in range(n):
            if i == j:
                continue
            dx, dy = xi - points[j][0], yi - points[j][1]
            dists.append((dx * dx + dy * dy, j))
        dists.sort()
        for _, j in dists[:k]:
            edges.add((min(i, j), max(i, j)))
    return sorted(edges)


def write_csv(path, names, points, attrs, edges):
    with open(path, "w") as f:
        f.write("# Attractions: id,name,x,y,attractiveness\n")
        for idx, name in enumerate(names):
            x, y = points[idx]
            f.write(f"{idx},{name},{x:.1f},{y:.1f},{attrs[idx]:.2f}\n")
        f.write("# Bus Edges: i,j\n")
        for i, j in edges:
            f.write(f"{i},{j}\n")


def main():
    for n in SIZES:
        # te same punkty dla obu wariantow gestosci -> czyste porownanie
        rng = random.Random(SEED + n)
        names = build_names(n, rng)
        points = [(rng.uniform(*X_RANGE), rng.uniform(*Y_RANGE)) for _ in range(n)]
        attrs = [rng.uniform(ATTR_MIN, ATTR_MAX) for _ in range(n)]

        for tag, k in (("dense", K_DENSE), ("sparse", K_SPARSE)):
            edges = knn_edges(points, k)
            path = f"data/instance_{n}_{tag}.csv"
            write_csv(path, names, points, attrs, edges)
            avg_deg = 2 * len(edges) / n
            print(f"{path:32s} n={n:4d}  edges={len(edges):5d}  avg_degree={avg_deg:5.2f}")


if __name__ == "__main__":
    main()
