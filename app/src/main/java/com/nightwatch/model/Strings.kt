package com.nightwatch.model

object Strings {

    private val translations = mapOf(
        // Weekdays
        "monday" to mapOf(
            Language.DE to "Montag", Language.EN to "Monday",
            Language.FR to "Lundi", Language.IT to "Lunedì"
        ),
        "tuesday" to mapOf(
            Language.DE to "Dienstag", Language.EN to "Tuesday",
            Language.FR to "Mardi", Language.IT to "Martedì"
        ),
        "wednesday" to mapOf(
            Language.DE to "Mittwoch", Language.EN to "Wednesday",
            Language.FR to "Mercredi", Language.IT to "Mercoledì"
        ),
        "thursday" to mapOf(
            Language.DE to "Donnerstag", Language.EN to "Thursday",
            Language.FR to "Jeudi", Language.IT to "Giovedì"
        ),
        "friday" to mapOf(
            Language.DE to "Freitag", Language.EN to "Friday",
            Language.FR to "Vendredi", Language.IT to "Venerdì"
        ),
        "saturday" to mapOf(
            Language.DE to "Samstag", Language.EN to "Saturday",
            Language.FR to "Samedi", Language.IT to "Sabato"
        ),
        "sunday" to mapOf(
            Language.DE to "Sonntag", Language.EN to "Sunday",
            Language.FR to "Dimanche", Language.IT to "Domenica"
        ),

        // Countdown
        "now" to mapOf(
            Language.DE to "jetzt", Language.EN to "now",
            Language.FR to "maintenant", Language.IT to "adesso"
        ),
        "in_minutes" to mapOf(
            Language.DE to "in %d Minuten", Language.EN to "in %d minutes",
            Language.FR to "dans %d minutes", Language.IT to "tra %d minuti"
        ),
        "in_hours_minutes" to mapOf(
            Language.DE to "in %d Stunden %d Minuten", Language.EN to "in %dh %dm",
            Language.FR to "dans %dh %dm", Language.IT to "tra %d ore %d minuti"
        ),
        "in_days_hours" to mapOf(
            Language.DE to "in %d Tagen %d Stunden", Language.EN to "in %d days %dh",
            Language.FR to "dans %d jours %dh", Language.IT to "tra %d giorni %d ore"
        ),

        // Next event
        "next_event" to mapOf(
            Language.DE to "N\u00e4chster Termin", Language.EN to "Next Event",
            Language.FR to "Prochain \u00e9v\u00e9nement", Language.IT to "Prossimo evento"
        ),
        "unnamed_event" to mapOf(
            Language.DE to "Unbenannter Termin", Language.EN to "Unnamed Event",
            Language.FR to "\u00c9v\u00e9nement sans nom", Language.IT to "Evento senza nome"
        ),

        // Emergency
        "emergency" to mapOf(
            Language.DE to "NOTRUF", Language.EN to "EMERGENCY",
            Language.FR to "URGENCE", Language.IT to "EMERGENZA"
        ),
        "emergency_sending" to mapOf(
            Language.DE to "NOTRUF WIRD ABGESETZT", Language.EN to "SENDING EMERGENCY CALL",
            Language.FR to "APPEL D'URGENCE EN COURS", Language.IT to "CHIAMATA DI EMERGENZA IN CORSO"
        ),
        "tap_to_cancel" to mapOf(
            Language.DE to "Tippen zum Abbrechen", Language.EN to "Tap to cancel",
            Language.FR to "Appuyez pour annuler", Language.IT to "Tocca per annullare"
        ),
        "voice_command_detected" to mapOf(
            Language.DE to "Sprachbefehl erkannt", Language.EN to "Voice command detected",
            Language.FR to "Commande vocale d\u00e9tect\u00e9e", Language.IT to "Comando vocale rilevato"
        ),

        // Voice service
        "voice_recognition" to mapOf(
            Language.DE to "NightWatch Spracherkennung", Language.EN to "NightWatch Voice Recognition",
            Language.FR to "NightWatch Reconnaissance Vocale", Language.IT to "NightWatch Riconoscimento Vocale"
        ),
        "voice_listening" to mapOf(
            Language.DE to "Dauerhaftes Zuh\u00f6ren f\u00fcr Notruf-Erkennung",
            Language.EN to "Continuous listening for emergency detection",
            Language.FR to "\u00c9coute continue pour d\u00e9tection d'urgence",
            Language.IT to "Ascolto continuo per rilevamento emergenze"
        ),
        "voice_active" to mapOf(
            Language.DE to "Spracherkennung aktiv", Language.EN to "Voice recognition active",
            Language.FR to "Reconnaissance vocale active", Language.IT to "Riconoscimento vocale attivo"
        ),

        // Settings
        "settings" to mapOf(
            Language.DE to "Einstellungen", Language.EN to "Settings",
            Language.FR to "Param\u00e8tres", Language.IT to "Impostazioni"
        ),
        "language" to mapOf(
            Language.DE to "Sprache", Language.EN to "Language",
            Language.FR to "Langue", Language.IT to "Lingua"
        ),
        "time_settings" to mapOf(
            Language.DE to "Zeiteinstellungen", Language.EN to "Time Settings",
            Language.FR to "Param\u00e8tres de temps", Language.IT to "Impostazioni ora"
        ),
        "use_real_sun_times" to mapOf(
            Language.DE to "Echte Sonnenzeiten", Language.EN to "Real Sun Times",
            Language.FR to "Heures solaires r\u00e9elles", Language.IT to "Orari solari reali"
        ),
        "sunrise" to mapOf(
            Language.DE to "Sonnenaufgang", Language.EN to "Sunrise",
            Language.FR to "Lever du soleil", Language.IT to "Alba"
        ),
        "sunset" to mapOf(
            Language.DE to "Sonnenuntergang", Language.EN to "Sunset",
            Language.FR to "Coucher du soleil", Language.IT to "Tramonto"
        ),
        "emergency_settings" to mapOf(
            Language.DE to "Notruf-Einstellungen", Language.EN to "Emergency Settings",
            Language.FR to "Param\u00e8tres d'urgence", Language.IT to "Impostazioni emergenza"
        ),
        "trigger_word" to mapOf(
            Language.DE to "Ausl\u00f6sewort", Language.EN to "Trigger Word",
            Language.FR to "Mot d\u00e9clencheur", Language.IT to "Parola chiave"
        ),
        "repetitions" to mapOf(
            Language.DE to "Wiederholungen", Language.EN to "Repetitions",
            Language.FR to "R\u00e9p\u00e9titions", Language.IT to "Ripetizioni"
        ),
        "api_endpoint" to mapOf(
            Language.DE to "API-Endpunkt", Language.EN to "API Endpoint",
            Language.FR to "Point d'acc\u00e8s API", Language.IT to "Endpoint API"
        ),
        "voice_detection" to mapOf(
            Language.DE to "Spracherkennung", Language.EN to "Voice Detection",
            Language.FR to "D\u00e9tection vocale", Language.IT to "Rilevamento vocale"
        ),
        "enabled" to mapOf(
            Language.DE to "Aktiviert", Language.EN to "Enabled",
            Language.FR to "Activ\u00e9", Language.IT to "Attivato"
        ),
        "close" to mapOf(
            Language.DE to "Schlie\u00dfen", Language.EN to "Close",
            Language.FR to "Fermer", Language.IT to "Chiudi"
        ),
        "latitude" to mapOf(
            Language.DE to "Breitengrad", Language.EN to "Latitude",
            Language.FR to "Latitude", Language.IT to "Latitudine"
        ),
        "longitude" to mapOf(
            Language.DE to "L\u00e4ngengrad", Language.EN to "Longitude",
            Language.FR to "Longitude", Language.IT to "Longitudine"
        ),

        // Email
        "email_settings" to mapOf(
            Language.DE to "E-Mail-Benachrichtigung", Language.EN to "Email Notification",
            Language.FR to "Notification par e-mail", Language.IT to "Notifica e-mail"
        ),
        "email_enabled" to mapOf(
            Language.DE to "E-Mail senden", Language.EN to "Send Email",
            Language.FR to "Envoyer un e-mail", Language.IT to "Invia e-mail"
        ),
        "email_recipient" to mapOf(
            Language.DE to "Empf\u00e4nger", Language.EN to "Recipient",
            Language.FR to "Destinataire", Language.IT to "Destinatario"
        ),
        "email_sender" to mapOf(
            Language.DE to "Absender-E-Mail", Language.EN to "Sender Email",
            Language.FR to "E-mail exp\u00e9diteur", Language.IT to "E-mail mittente"
        ),
        "email_password" to mapOf(
            Language.DE to "App-Passwort", Language.EN to "App Password",
            Language.FR to "Mot de passe app", Language.IT to "Password app"
        ),
        "smtp_host" to mapOf(
            Language.DE to "SMTP-Server", Language.EN to "SMTP Server",
            Language.FR to "Serveur SMTP", Language.IT to "Server SMTP"
        ),
        "smtp_port" to mapOf(
            Language.DE to "SMTP-Port", Language.EN to "SMTP Port",
            Language.FR to "Port SMTP", Language.IT to "Porta SMTP"
        ),
        "emergency_email_body" to mapOf(
            Language.DE to "ACHTUNG: Ein Notruf wurde vom NightWatch-Ger\u00e4t ausgel\u00f6st!",
            Language.EN to "ALERT: An emergency call was triggered from the NightWatch device!",
            Language.FR to "ALERTE: Un appel d'urgence a \u00e9t\u00e9 d\u00e9clench\u00e9 depuis l'appareil NightWatch!",
            Language.IT to "ATTENZIONE: Una chiamata di emergenza \u00e8 stata attivata dal dispositivo NightWatch!"
        ),
        "emergency_email_time" to mapOf(
            Language.DE to "Zeitpunkt", Language.EN to "Time",
            Language.FR to "Heure", Language.IT to "Ora"
        ),
        "emergency_email_device" to mapOf(
            Language.DE to "Ger\u00e4t", Language.EN to "Device",
            Language.FR to "Appareil", Language.IT to "Dispositivo"
        ),
        // Watchdog
        "watchdog_settings" to mapOf(
            Language.DE to "Watchdog (t\u00e4gliche Statusmeldung)", Language.EN to "Watchdog (daily status)",
            Language.FR to "Watchdog (statut quotidien)", Language.IT to "Watchdog (stato giornaliero)"
        ),
        "watchdog_enabled" to mapOf(
            Language.DE to "Watchdog aktiv", Language.EN to "Watchdog active",
            Language.FR to "Watchdog actif", Language.IT to "Watchdog attivo"
        ),
        "watchdog_time" to mapOf(
            Language.DE to "Sendezeit", Language.EN to "Send time",
            Language.FR to "Heure d'envoi", Language.IT to "Ora di invio"
        ),
        "watchdog_code" to mapOf(
            Language.DE to "Watchdog-Code", Language.EN to "Watchdog Code",
            Language.FR to "Code watchdog", Language.IT to "Codice watchdog"
        ),
        "watchdog_email_subject" to mapOf(
            Language.DE to "NightWatch - Status OK", Language.EN to "NightWatch - Status OK",
            Language.FR to "NightWatch - Statut OK", Language.IT to "NightWatch - Stato OK"
        ),
        "watchdog_email_body" to mapOf(
            Language.DE to "NightWatch ist aktiv und funktioniert einwandfrei.",
            Language.EN to "NightWatch is active and running normally.",
            Language.FR to "NightWatch est actif et fonctionne normalement.",
            Language.IT to "NightWatch \u00e8 attivo e funziona normalmente."
        ),

        "emergency_code_label" to mapOf(
            Language.DE to "Code", Language.EN to "Code",
            Language.FR to "Code", Language.IT to "Codice"
        ),
        "emergency_code" to mapOf(
            Language.DE to "Notruf-Code", Language.EN to "Emergency Code",
            Language.FR to "Code d'urgence", Language.IT to "Codice emergenza"
        ),
        "emergency_email_footer" to mapOf(
            Language.DE to "Bitte pr\u00fcfen Sie sofort, ob Hilfe ben\u00f6tigt wird.",
            Language.EN to "Please check immediately if help is needed.",
            Language.FR to "Veuillez v\u00e9rifier imm\u00e9diatement si de l'aide est n\u00e9cessaire.",
            Language.IT to "Si prega di verificare immediatamente se \u00e8 necessario aiuto."
        ),

        // Mock server
        "emergency_received" to mapOf(
            Language.DE to "Notruf empfangen. Hilfe ist unterwegs.",
            Language.EN to "Emergency received. Help is on the way.",
            Language.FR to "Urgence re\u00e7ue. L'aide est en chemin.",
            Language.IT to "Emergenza ricevuta. I soccorsi sono in arrivo."
        )
    )

    private var currentLanguage: Language = Language.DE

    fun setLanguage(language: Language) {
        currentLanguage = language
    }

    fun getLanguage(): Language = currentLanguage

    fun get(key: String): String =
        translations[key]?.get(currentLanguage)
            ?: translations[key]?.get(Language.DE)
            ?: key

    fun get(key: String, vararg args: Any): String =
        String.format(get(key), *args)

    fun weekday(dayOfWeek: Int): String = when (dayOfWeek) {
        java.util.Calendar.MONDAY -> get("monday")
        java.util.Calendar.TUESDAY -> get("tuesday")
        java.util.Calendar.WEDNESDAY -> get("wednesday")
        java.util.Calendar.THURSDAY -> get("thursday")
        java.util.Calendar.FRIDAY -> get("friday")
        java.util.Calendar.SATURDAY -> get("saturday")
        java.util.Calendar.SUNDAY -> get("sunday")
        else -> ""
    }

    // Default trigger words per language
    fun defaultTriggerWord(language: Language): String = when (language) {
        Language.DE -> "hilfe"
        Language.EN -> "help"
        Language.FR -> "aide"
        Language.IT -> "aiuto"
    }
}
