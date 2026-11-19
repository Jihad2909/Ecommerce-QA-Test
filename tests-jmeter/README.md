# Tests de performance — Apache JMeter

Caractéristique ISO/IEC 25010 couverte : **Efficacité de performance** (temps de réponse, débit, capacité).

## Prérequis

- Apache JMeter **5.6.3** (Java 8+) : https://jmeter.apache.org/download_jmeter.cgi
- Backend démarré sur `http://localhost:8080` (PostgreSQL inclus, par ex. `docker compose up -d`)

## Scénarios du plan `ecommerce-load-test.jmx`

| Groupe | Utilisateurs (défaut) | Requêtes |
| --- | --- | --- |
| 00 - Setup | 1 (une fois) | Création du compte `jmeter.admin@tekup.tn` (`ROLE_ADMIN`), login, création d'un produit de test (stock 10 000 000) |
| 01 - Consultation catalogue | 20 | `GET /api/products`, `GET /api/products?search=`, `GET /api/products/{id}` |
| 02 - Parcours commande | 10 | Login (1×/utilisateur) → `POST /api/orders` → `PATCH /confirm` → `PATCH /deliver` → `GET /api/dashboard` |
| 03 - Sécurité sous charge | 3 | Écriture sans token et JWT falsifié → `401/403` attendu |

Chaque requête porte une assertion (code HTTP et statut de commande `SUBMITTED` → `CONFIRMED` → `DELIVERED`).

## Paramètres (`-Jnom=valeur`)

| Propriété | Défaut | Rôle |
| --- | --- | --- |
| `host` / `port` | `localhost` / `8080` | Cible de l'API |
| `duration` | `60` | Durée du test (s) |
| `rampup` | `10` | Montée en charge (s) |
| `catalogUsers` | `20` | Utilisateurs du groupe 01 |
| `orderUsers` | `10` | Utilisateurs du groupe 02 |
| `securityUsers` | `3` | Utilisateurs du groupe 03 |
| `adminEmail` / `adminPassword` | `jmeter.admin@tekup.tn` / `jmeter123` | Compte utilisé par le test |

## Exécution

Mode graphique (conception / débogage) :

```
jmeter -t tests-jmeter/ecommerce-load-test.jmx
```

Mode ligne de commande avec rapport HTML (recommandé pour les mesures) :

```
jmeter -n -t tests-jmeter/ecommerce-load-test.jmx -l tests-jmeter/results/results.jtl -e -o tests-jmeter/results/report -Jduration=120 -JcatalogUsers=50
```

Ou avec le script fourni sous Windows : `tests-jmeter\run-jmeter.bat`

Sans installer Java/JMeter, via Docker (backend lancé avec `docker compose up -d`) :

```
docker run --rm --network ecommerce-qa-test_default -v "${PWD}/tests-jmeter:/tests" justb4/jmeter:5.5 -n -t /tests/ecommerce-load-test.jmx -l /tests/results/results.jtl -e -o /tests/results/report -Jhost=backend
```

Ouvrir ensuite `tests-jmeter/results/report/index.html`.

## Résultat de référence

Exécution du 19/11/2026 (Docker local, 33 utilisateurs, 30 s) : **817 requêtes, 0 % d'erreur**, temps moyen 14 ms, p95 28 ms. Le login (BCrypt) est l'opération la plus lente (~143 ms en moyenne) ; le cycle complet d'une commande (création → confirmation → livraison → dashboard) prend ~78 ms en moyenne.

## Remarques

- Le test crée de vraies données (produit, commandes, journaux d'audit) : à lancer sur une base de dev/test, pas en production.
- Le setup s'appuie sur le fait que `/api/auth/register` accepte le champ `role` envoyé par le client. C'est pratique pour le test, mais c'est aussi une **faille de sécurité** (n'importe qui peut s'inscrire en `ROLE_ADMIN`) à corriger côté backend ; une fois corrigée, créer le compte admin manuellement et passer ses identifiants via `-JadminEmail` / `-JadminPassword`.
