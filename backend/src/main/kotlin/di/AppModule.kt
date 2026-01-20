package di

import database.DatabaseFactory
import database.MoodTrackerDatabaseRepository
import org.kodein.di.*
import service.ExportService
import service.ImportService

val appModule = DI.Module("app") {
    bind<DatabaseFactory>() with singleton {
        DatabaseFactory.also { it.init() }
    }

    bind<MoodTrackerDatabaseRepository>() with singleton { MoodTrackerDatabaseRepository() }

    bind<ExportService>() with singleton { ExportService(instance()) }
    bind<ImportService>() with singleton { ImportService(instance()) }

}