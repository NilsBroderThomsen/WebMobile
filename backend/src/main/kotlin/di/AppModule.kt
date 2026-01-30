package di

import database.DatabaseFactory
import database.MoodTrackerDatabaseRepository
import org.kodein.di.*

val appModule = DI.Module("app") {
    bind<DatabaseFactory>() with singleton {
        DatabaseFactory.also { it.init() }
    }

    bind<MoodTrackerDatabaseRepository>() with singleton {
        instance<DatabaseFactory>()
        MoodTrackerDatabaseRepository()
    }
}