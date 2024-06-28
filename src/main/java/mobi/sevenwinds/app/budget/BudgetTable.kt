package mobi.sevenwinds.app.budget

import mobi.sevenwinds.app.author.AuthorTable
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable

object BudgetTable : IntIdTable("budget") {
    val year = integer("year")
    val month = integer("month")
    val amount = integer("amount")
    val type = enumerationByName("type", 100, BudgetType::class)
    val author_id = integer("author_id").nullable()
}

class BudgetEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<BudgetEntity>(BudgetTable)

    var year by BudgetTable.year
    var month by BudgetTable.month
    var amount by BudgetTable.amount
    var type by BudgetTable.type
    var authorId by BudgetTable.author_id
    //эти столбцы по€вл€ютс€ после LEFT JOIN
 //   var authorName by AuthorTable.full_name
 //   var authorCreated by AuthorTable.created

    fun toResponse(): BudgetRecord {
        return BudgetRecord(year, month, amount, type, authorId)
    }
}