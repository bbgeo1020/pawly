# 🐾 Pawly - Solution Android Multirôle de Garde d'Animaux

Pawly est une application Android native développée en **Kotlin** qui structure et sécurise l'écosystème du pet-sitting. Elle interconnecte intelligemment les propriétaires d'animaux, les prestataires de services (gardiens) et les administrateurs autour d'un backend robuste propulsé par **Supabase**.

---

## 🚀 Fonctionnalités Majeures Implémentées

* **🔐 Authentification & Routage Multi-rôles (`LoginActivity`)** : Système de session sécurisé via Supabase Auth qui redirige automatiquement l'utilisateur vers son tableau de bord spécifique selon son rôle (`admin`, `prestataire`, ou `proprietaire`).
* **📅 Gestion des Réservations (`ReservationsActivity`)** : Suivi en temps réel des demandes de garde, affichage dynamique des statuts, calcul des frais et système de demande de remboursement intégré lié à la table `Réserve`.
* **🐕 Gestion du Profil Animal (`AddPetActivity`)** : Formulaire complet avec Spinners dynamiques permettant d'enregistrer de nouveaux animaux (Chien, Chat, Lapin, etc.) liés à l'utilisateur via la table `animaux`.
* **📖 Journal de Garde Collaboratif (`JournalGardeRepository`)** : Fil d'actualité permettant aux prestataires de publier des comptes-rendus quotidiens interactifs (avec compteurs de likes, de vues et gestion de médias) via la table `e_journal`.
* **🛡️ Panel d'Administration Avancé (`AdminActivity`)** : Dashboard complet affichant des statistiques clés (revenus sur commission de 10%, taux de complétion, volume d'utilisateurs) et un module de modération des signalements.

---

## 🛠️ Stack Technique & Architecture

L'application respecte rigoureusement les principes de la **Clean Architecture** :
* **Asynchronisme** : Utilisation intensive de `Kotlin Coroutines` couplées au `lifecycleScope` et dispatchées sur `Dispatchers.IO` pour isoler les appels réseau de l'interface graphique.
* **Sérialisation Type-Safe** : Intégration de `Kotlinx.serialization` pour mapper de manière native et sécurisée les structures JSON complexes de PostgreSQL en Data Classes Kotlin.
* **Persistance & Données Distantes** : Utilisation du SDK Kotlin officiel de **Supabase** (Modules `Postgrest` pour le requêtage de données et `Auth` pour la sécurité).

---

## 💾 Schéma d'Architecture de la Base de Données (PostgreSQL / Supabase)

L'application exploite et synchronise les tables suivantes :
1. **`utilisateur`** : Contient le profil utilisateur, ses coordonnées et son rôle applicatif.
2. **`animaux`** : Stocke le cheptel des animaux (`id`, `idUser`, `nom`, `typeAnimal`, `race`/âge).
3. **`Réserve`** : Gère les contrats de garde (`id_reservation`, `id_proprietaire`, `id_prestataire`, `date_debut`, `date_fin`, `statut`, `prix_total_frais_plateforme`, `statut_remboursement`).
4. **`e_journal`** : Carnet de suivi interactif (`id_article`, `id_prestataire`, `titre`, `contenu`, `type_contenu`, `media_url`, `likes_count`, `vues_count`, `publie`, `date_publication`).
5. **`avis_offres`** : Table d'évaluation de la réputation des pet-sitters (`id`, `offre_id`, `proprietaire_id`, `prestataire_id`, `note`, `commentaire`).

---

## 📁 Inventaire du Code Source

### 📄 Fichiers de Logique Métier (Kotlin)
* `📄 AnimalRepository.kt`
* `📄 AnimalSupabase.kt`
* `📄 AvisOffreSupabase.kt`
* `📄 JournalGardeRepository.kt`
* `📄 JournalSupabase.kt`
* `📄 ReservationRepository.kt`
* `📄 ReservationSupabase.kt`
* `📄 AddPetActivity.kt`
* `📄 AdminActivity.kt`
* `📄 LoginActivity.kt`
* `📄 ReservationsActivity.kt`
* `📄 UserStore.kt`
* `📄 SupabaseManager.kt`
* `📄 Models.kt`

---

## ⚙️ Déploiement et Configuration Rapide

1. **Cloner le dépôt** Git sur votre environnement de développement.
2. **Configurer Supabase** : Créez les 5 tables PostgreSQL mentionnées ci-dessus.
3. **Liaison API** : Renseignez vos identifiants uniques (`supabaseUrl` et `supabaseKey`) au sein de votre singleton au niveau de `SupabaseManager`.
4. **Compilation** : Ouvrez le projet dans Android Studio (Target SDK: 34), synchronisez vos fichiers Gradle et déployez.