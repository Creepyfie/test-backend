package mobi.sevenwinds.app.budget

import io.ktor.auth.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mobi.sevenwinds.app.author.AuthorTable
import mobi.sevenwinds.app.author.AuthorTable.created
import mobi.sevenwinds.app.author.AuthorTable.full_name
import mobi.sevenwinds.app.budget.BudgetTable
import mobi.sevenwinds.app.budget.BudgetTable.amount
import mobi.sevenwinds.app.budget.BudgetTable.author_id
import mobi.sevenwinds.app.budget.BudgetTable.month
import mobi.sevenwinds.app.budget.BudgetTable.type
import mobi.sevenwinds.app.budget.BudgetTable.year
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

object BudgetService {
    suspend fun addRecord(body: BudgetRecord): BudgetRecord = withContext(Dispatchers.IO) {
        transaction {
            val entity = BudgetEntity.new {
                this.year = body.year
                this.month = body.month
                this.amount = body.amount
                this.type = body.type
                this.authorId = body.authorId
            }
            return@transaction entity.toResponse()
        }
    }

    suspend fun getYearStats(param: BudgetYearParam): BudgetYearStatsResponse = withContext(Dispatchers.IO) {
        transaction {
            val querySelect = BudgetTable
                .join(AuthorTable, JoinType.LEFT, null, null) { BudgetTable.author_id eq AuthorTable.id }
                .select { BudgetTable.year eq param.year }

            if (param.searchQuery != null) {
                    var lowerSearchQuery = param.searchQuery.toLowerCase()
                //добавлен фильтр по ФИО
                querySelect.andWhere { AuthorTable.full_name.lowerCase().like(lowerSearchQuery)}
            }
            //добавляем сортировку по месяцу и кол-ву
            val query = querySelect
                .orderBy(Pair(BudgetTable.month,SortOrder.ASC),Pair(BudgetTable.amount, SortOrder.DESC))

            // total относится ко всем записям из запроса, поэтому перенесли его выше выборки limit
            val total = query.count()
            //аналогично, для подсчется totalByType необходи весь результат запроса
            val data = query.map{v -> BudgetDto(
                year = v[year],
                month = v[month],
                amount = v[amount],
                type = v[type],
                authorName =  v[full_name],
                authorCreated = v[created])}
            val sumByType = data.groupBy { it.type.name }.mapValues { it.value.sumOf { v -> v.amount } }
            //отсеиваем параметрами пагинации нужное кол-во строк
            query.limit(param.limit, param.offset)
            val dataLimited = query.map{v -> BudgetDto(
                year = v[year],
                month = v[month],
                amount = v[amount],
                type = v[type],
                authorName =  v[full_name],
                authorCreated = v[created])}

            return@transaction BudgetYearStatsResponse(
                total = total,
                totalByType = sumByType,
                items = dataLimited
            )
        }
    }
}