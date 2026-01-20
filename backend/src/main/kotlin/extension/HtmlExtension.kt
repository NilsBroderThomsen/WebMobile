package extension

import model.Entry
import kotlinx.html.*

fun FlowContent.entryCard(entry: Entry) {
    val mood = entry.moodRating

    article(classes = "entry-card") {
        h3 { +entry.title }
        p {
            +entry.content.take(100)
            +if (entry.content.length > 100) "..." else ""
        }
        p {
            if (mood != null) {
                +"Stimmung: ${mood}/10 ${mood.toEmoji()}"
            } else {
                +"Keine Stimmung angegeben"
            }
        }
        p { +"Erstellt: ${entry.createdDate}" }
        a(href = "/entries/${entry.id.value}") { +"Details" }
        +" | "
        form(action = "/entries/${entry.id.value}/delete", method = FormMethod.post) {
            style = "display: inline;"
            button(type = ButtonType.submit) { +"Löschen" }
        }
    }
}