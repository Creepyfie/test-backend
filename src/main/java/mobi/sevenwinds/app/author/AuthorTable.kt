package mobi.sevenwinds.app.author

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable

object AuthorTable : IntIdTable("author") {
    val full_name = text("full_name")
    val created = datetime("created")
}

class AuthorEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<AuthorEntity>(AuthorTable)
    var fullName by AuthorTable.full_name
    var created by AuthorTable.created

    fun toResponse(): AuthorRecord {
        return AuthorRecord(fullName, created)
    }
}