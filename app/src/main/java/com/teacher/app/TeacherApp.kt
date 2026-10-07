package com.teacher.app

import android.app.Application
import com.teacher.app.data.db.TeacherDatabase
import com.teacher.app.data.repo.Settings
import com.teacher.app.data.repo.TeacherRepository
import com.teacher.app.data.seed.Seeder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TeacherApp : Application() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    lateinit var database: TeacherDatabase
        private set
    lateinit var repository: TeacherRepository
        private set
    lateinit var settings: Settings
        private set

    private val _contentReady = MutableStateFlow(false)
    val contentReady: StateFlow<Boolean> = _contentReady

    override fun onCreate() {
        super.onCreate()
        database = TeacherDatabase.get(this)
        repository = TeacherRepository(database)
        settings = Settings(this)

        scope.launch {
            Seeder.seedIfNeeded(this@TeacherApp, database)
            _contentReady.value = true
        }
    }
}
