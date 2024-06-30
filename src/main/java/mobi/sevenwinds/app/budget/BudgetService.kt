package mobi.sevenwinds.app.budget

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mobi.sevenwinds.app.author.AuthorTable
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

object BudgetService {
    suspend fun addRecord(body: BudgetRecord): BudgetRecord = withContext(Dispatchers.IO) {
        transaction {
            val entity = BudgetEntity.new {
                this.year = body.year
                this.month = body.month
                this.amount = body.amount
                this.type = body.type
                this.authorId = body.authorId!!
            }
            return@transaction entity.toResponse()
        }
    }

    suspend fun getYearStats(param: BudgetYearParam): BudgetYearStatsResponse = withContext(Dispatchers.IO) {
        transaction {
            val query = BudgetTable
                    //���
                .join(AuthorTable, JoinType.LEFT,AuthorTable.id.eq(BudgetTable.id))
                .select { BudgetTable.year eq param.year}
                    //�������� ������ �� ���
                .andWhere { AuthorTable.full_name regexp  (".*(?i)"+param.filter.toString()+"(?-i).*") }
                    //�� ����� ����� ������ ���� where
           //     .andWhere { BudgetTable.author_id.isNotNull()}
                .orderBy(Pair(BudgetTable.month,SortOrder.ASC),Pair(BudgetTable.amount, SortOrder.DESC))
            // total ��������� �� ���� ������� �� �������, ������� ��������� ��� ���� ������� limit
            val total = query.count()
            //����������, ��� ��������� totalByType �������� ���� ��������� �������
            val data = BudgetEntity.wrapRows(query).map { it.toResponse() }
            val sumByType = data.groupBy { it.type.name }.mapValues { it.value.sumOf { v -> v.amount } }
            //��������� ����������� ��������� ������ ���-�� �����
            query.limit(param.limit, param.offset)
            val dataLimited = BudgetEntity.wrapRows(query).map { it.toResponse() }

            return@transaction BudgetYearStatsResponse(
                total = total,
                totalByType = sumByType,
                items = dataLimited
            )
        }
    }
}