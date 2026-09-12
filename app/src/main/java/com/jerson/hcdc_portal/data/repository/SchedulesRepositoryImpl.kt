package com.jerson.hcdc_portal.data.repository

import com.jerson.hcdc_portal.data.local.PortalDB
import com.jerson.hcdc_portal.data.parser.ScheduleParser
import com.jerson.hcdc_portal.data.parser.UserSessionParser
import com.jerson.hcdc_portal.domain.model.Schedule
import com.jerson.hcdc_portal.domain.repository.SchedulesRepository
import com.jerson.hcdc_portal.util.AppPreference
import com.jerson.hcdc_portal.util.Constants
import com.jerson.hcdc_portal.util.Constants.KEY_CSRF_TOKEN
import com.jerson.hcdc_portal.util.Constants.KEY_ENROLL_ANNOUNCE
import com.jerson.hcdc_portal.util.Constants.KEY_IS_ENROLLED
import com.jerson.hcdc_portal.util.Constants.KEY_IS_SESSION
import com.jerson.hcdc_portal.util.Constants.KEY_STUDENTS_UNITS
import com.jerson.hcdc_portal.util.Constants.KEY_STUDENT_COURSE
import com.jerson.hcdc_portal.util.Constants.KEY_STUDENT_ID
import com.jerson.hcdc_portal.util.Constants.KEY_STUDENT_NAME
import com.jerson.hcdc_portal.util.Constants.KEY_USER_AVATAR
import com.jerson.hcdc_portal.util.Resource
import com.jerson.hcdc_portal.util.await
import com.jerson.hcdc_portal.util.getRequest
import com.jerson.hcdc_portal.util.networkBoundResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import org.jsoup.Jsoup
import java.io.IOException
import javax.inject.Inject

class SchedulesRepositoryImpl @Inject constructor(
    private val db: PortalDB,
    private val client: OkHttpClient,
    private val preference: AppPreference,
    private val scheduleParser: ScheduleParser,
    private val sessionParser: UserSessionParser
) : SchedulesRepository {

    override suspend fun fetchSchedules(): Flow<Resource<List<Schedule>>> {
        return networkBoundResource(
            query = { db.scheduleDao().getSchedules() },
            fetch = {
                withContext(Dispatchers.IO) {
                    val response = client.newCall(getRequest(Constants.baseUrl)).await()
                    response.use { res ->
                        if (!res.isSuccessful) {
                            throw IOException("HTTP error ${res.code}: ${res.message}")
                        }
                        val htmlString = res.body?.string().orEmpty()
                        val doc = Jsoup.parse(htmlString)

                        if (doc.body().text().lowercase().contains("something went wrong")) {
                            throw IOException("Server error: something went wrong")
                        }

                        val sessionStatus = sessionParser.parseSession(doc)
                        preference.setStringPreference(KEY_CSRF_TOKEN, sessionStatus.csrfToken)
                        preference.setBooleanPreference(KEY_IS_SESSION, sessionStatus.isSessionExpired)
                        if (sessionStatus.isSessionExpired) {
                            throw IOException("session end - ${res.code}")
                        }

                        // Parse and cache user profile
                        val profile = scheduleParser.parseUserProfile(doc)
                        preference.setStringPreference(KEY_STUDENTS_UNITS, profile.units)
                        preference.setStringPreference(KEY_STUDENT_NAME, profile.name)
                        preference.setStringPreference(KEY_STUDENT_ID, profile.studentId)
                        preference.setStringPreference(KEY_STUDENT_COURSE, profile.course)
                        preference.setStringPreference(KEY_ENROLL_ANNOUNCE, profile.enrollAnnounce)
                        preference.setStringPreference(KEY_IS_ENROLLED, profile.isEnrolled)
                        profile.avatarUrl?.let { avatar ->
                            preference.setStringPreference(KEY_USER_AVATAR, avatar)
                        }

                        scheduleParser.parseSchedules(doc)
                    }
                }
            },
            saveFetchResult = { schedules ->
                db.scheduleDao().replaceSchedules(schedules)
            }
        )
    }

    override suspend fun getSchedules(): Flow<Resource<List<Schedule>>> = channelFlow {
        send(Resource.Loading())
        db.scheduleDao().getSchedules()
            .catch {
                send(Resource.Error(it.message))
            }
            .collect {
                send(Resource.Success(it))
            }
    }
}