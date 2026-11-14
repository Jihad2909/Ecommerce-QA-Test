# Gestion Commerciale — Qualité Logicielle & Automatisation des Tests

**Référentiel Qualité :** ISO/IEC 25010

**Stack Technique :** Spring Boot 4 (Java), PostgreSQL, H2, React (Vite), Tailwind CSS, JUnit 5, Mockito, JaCoCo, Playwright.

## 1. Présentation du Projet

Application full-stack de **gestion commerciale** permettant l'administration sécurisée d'un catalogue de produits, la gestion automatique des stocks avec alertes de seuil critique, le suivi complet du cycle de vie des commandes (`SUBMITTED` $\rightarrow$ `CONFIRMED` $\rightarrow$ `DELIVERED` / `CANCELLED`) ainsi que la traçabilité des opérations sensibles via un journal d'audit (`audit_logs`).

Ce projet met en œuvre une stratégie complète d'**assurance qualité logicielle (QA)** structurée autour de la **pyramide des tests** et des exigences de la norme **ISO/IEC 25010**.

## 2. Architecture du Projet

```
gestion-commerciale/
├── backend/                        # API REST Spring Boot
│   ├── src/main/java/com/commercial/project/
│   │   ├── controller/             # Contrôleurs REST (Auth, Product, Order)
│   │   ├── dto/                    # Objets de transfert et validations
│   │   ├── exceptions/             # Gestion centralisée des erreurs
│   │   ├── models/                 # Entités JPA (User, Product, Order, OrderItem, AuditLog)
│   │   ├── repositories/           # Accès aux données Spring Data JPA
│   │   ├── security/               # Authentification JWT, filtre & Spring Security
│   │   └── services/               # Logique métier transactionnelle & audit
│   └── src/test/java/com/commercial/project/
│       ├── services/               # Tests unitaires (JUnit 5 + Mockito)
│       └── integration/            # Tests d'intégration API & Sécurité (MockMvc + H2)
│
└── frontend/                       # Interface Web React + Tailwind CSS
    ├── src/
    │   ├── components/             # Composants modulaires (Navbar, LoginForm, ProductSection, OrderSection)
    │   ├── services/               # Client Axios avec intercepteur JWT
    │   └── App.jsx                 # Orchestration du tableau de bord
    └── tests/
        └── e2e-commercial.spec.js  # Tests End-to-End automatisés (Playwright)

```

## 3. Alignement avec la Norme ISO/IEC 25010

| Caractéristique ISO/IEC 25010 | Mesures Techniques Implémentées | Preuves & Outils de Validation | 
| ----- | ----- | ----- | 
| **Aptitude fonctionnelle** | Gestion du catalogue produits, détection automatique de stock bas, calcul des totaux et workflow des commandes. | Scénarios End-to-End automatisés avec **Playwright**. | 
| **Sécurité** | Authentification stateless par **JWT**, hachage des mots de passe avec **BCrypt**, contrôle d'accès par rôles (`ADMIN`, `GESTIONNAIRE`) via `@PreAuthorize`, journalisation d'audit (`audit_logs`). | Tests d'intégration `MockMvc` vérifiant le rejet (`403 Forbidden`) des requêtes non authentifiées ou avec token invalide + **97 %** de couverture JaCoCo sur le package `security`. | 
| **Fiabilité** | Validation stricte des données entrantes, transactions atomiques (`@Transactional`) garantissant la cohérence des stocks, gestion globale des exceptions métier. | Tests unitaires **JUnit 5 + Mockito** sur les cas nominaux et les cas d'erreur (stock insuffisant, transitions invalides). | 
| **Maintenabilité** | Architecture en couches côté backend (Controller / Service / Repository / DTO) et découpage en composants réutilisables côté React. | Analyse de couverture de code via **JaCoCo** et séparation claire des responsabilités. | 

## 4. Installation et Exécution

### Prérequis

* **Java 25** & **Maven**

* **PostgreSQL** (base de données `commercial_db` sur le port `5432`)

* **Node.js 22** & **npm**

### 4.1. Démarrer le Backend (Spring Boot)

```
cd backend
./mvnw spring-boot:run

```

L'API est accessible sur `http://localhost:8080`.

### 4.2. Démarrer le Frontend (React + Tailwind CSS)

```
cd frontend
npm install
npm run dev

```

L'interface web est accessible sur `http://localhost:5173`.

## 5. Exécution de la Pyramide de Tests

### 5.1. Tests Unitaires, Intégration & Couverture JaCoCo (Backend)

Les tests d'intégration s'exécutent en isolation sur une base en mémoire **H2** (`@ActiveProfiles("test")`) sans impacter la base PostgreSQL de développement :

```
cd backend
./mvnw clean test jacoco:report

```

* **Consulter le rapport de couverture JaCoCo :**

  Ouvrir `backend/target/site/jacoco/index.html` dans un navigateur.

### 5.2. Tests End-to-End (Frontend avec Playwright)

Assurez-vous que le backend (`:8080`) et le frontend (`:5173`) sont démarrés, puis lancez :

```
cd frontend
npx playwright test --ui

```

Pour générer et afficher le rapport HTML détaillé :

```
npx playwright test --reporter=html
npx playwright show-report

```

## 6. Scénarios Automatisés Couverts

1. **Tests Unitaires (`OrderServiceTest`, `ProductServiceTest`) :**

   * Création d'un produit et vérification de l'audit.

   * Création d'une commande valide et décrémentation du stock.

   * Rejet d'une commande en cas de stock insuffisant (`BusinessRuleException`).

   * Validation des transitions de statut (`confirmOrder`, `cancelOrder`, `deliverOrder`).

2. **Tests d'Intégration (`ApiIntegrationTest`) :**

   * Accès public en lecture (`GET /api/products` $\rightarrow$ `200 OK`).

   * Blocage des écritures sans token ou avec JWT falsifié (`POST /api/products` $\rightarrow$ `403 Forbidden`).

   * Parcours authentifié complet (`Login` $\rightarrow$ `POST /api/products` $\rightarrow$ `POST /api/orders` $\rightarrow$ `PATCH /confirm` $\rightarrow$ `PATCH /deliver`).

3. **Tests End-to-End (`e2e-commercial.spec.js`) :**

   * Affichage d'un message d'erreur lors d'une tentative de connexion avec de mauvais identifiants.

   * Parcours utilisateur complet dans Chromium : connexion, ajout d'un produit, passation d'une commande, vérification visuelle de la baisse du stock dans le tableau et confirmation de la commande.