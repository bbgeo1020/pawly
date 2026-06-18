package fr.pawly.app

object UserStore {

    var currentUserId: String = ""

    var prenom: String    = "Prénom"
    var nom: String       = "Nom"
    var email: String     = "utilisateur@email.fr"
    var telephone: String = "+33 6 12 34 56 78"
    var adresse: String   = "12 rue de la Paix, Paris"


    var role: String      = "proprietaire"

    var statut: String    = "Non vérifié"
    var bio: String       = "Passionné(e) des animaux 🐾"

    // Statistiques
    var nbGardes: Int  = 0
    var nbAnimaux: Int = 0
    var nbAvis: Int    = 0
}