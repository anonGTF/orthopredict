package com.fkg.smarttooth.data.firebase

import android.util.Log
import com.fkg.smarttooth.data.model.Jaw
import com.fkg.smarttooth.data.model.Jaw.Companion.toJaw
import com.fkg.smarttooth.data.model.Patient
import com.fkg.smarttooth.data.model.Patient.Companion.toPatient
import com.fkg.smarttooth.data.model.User
import com.fkg.smarttooth.data.model.User.Companion.toUser
import com.fkg.smarttooth.utils.Resource
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

object FirebaseUtil {

    private const val TAG = "FirebaseUtils"
    private const val KEY_ID = "id"
    private const val KEY_EMAIL = "email"
    private const val KEY_NAME = "name"
    private const val KEY_MAXILLA = "maxilla"
    private const val KEY_MANDIBLE = "mandible"

    private const val PATIENT_COLLECTION = "patients"
    private const val USER_COLLECTION = "users"
    private const val DATA_COLLECTION = "data"

    private val db by lazy { FirebaseFirestore.getInstance() }
    private val auth by lazy { FirebaseAuth.getInstance() }

    fun getCurrentUser() = auth.currentUser

    fun getUserId() = auth.currentUser?.uid.orEmpty()

    fun isLoggedIn() = getCurrentUser() != null

    suspend fun login(email: String, password: String) = safeCallFirebase(
        firebaseCall = {
            val user = auth.signInWithEmailAndPassword(email, password).await().user
                ?: return@safeCallFirebase null
            db.collection(USER_COLLECTION).document(user.uid).get().await().toUser()
        },
        customKey = KEY_EMAIL,
        customValue = email,
        customMessage = "Error signing in user"
    )

    suspend fun register(name: String, institution: String, email: String, password: String) = safeCallFirebase(
        firebaseCall = {
            val user = auth.createUserWithEmailAndPassword(email, password).await().user
            db.collection(USER_COLLECTION).document(user?.uid.orEmpty()).set(
                User("", name, institution)
            ).await()
        },
        customKey = KEY_EMAIL,
        customValue = email,
        customMessage = "Error signing up user"
    )

    suspend fun logout() = safeCallFirebase(
        firebaseCall = { auth.signOut() }
    )

    suspend fun getDetailUser() = safeCallFirebase(
        firebaseCall = { db.collection(USER_COLLECTION)
            .document(auth.currentUser?.uid.orEmpty())
            .get().await().toUser()
        }
    )

    fun getAllPatients() = getRealtimeCollection(
        dbRef = db.collection(PATIENT_COLLECTION),
        mapperFunction = { it.toPatient() }
    )

    fun getPatient(id: String) = getRealtimeData(
        dbRef = db.collection(PATIENT_COLLECTION).document(id),
        mapperFunction = { it.toPatient() }
    )

    suspend fun addPatient(patient: Patient) = safeCallFirebase(
        firebaseCall = {
            db.collection(PATIENT_COLLECTION).add(patient).await()
                .get().await().toPatient()
        },
        customValue = patient.name,
        customMessage = "Error add patient",
        customKey = KEY_NAME
    )

    suspend fun setPatient(patient: Patient) = safeCallFirebase(
        firebaseCall = {
            db.collection(PATIENT_COLLECTION).document(patient.id)
                .set(patient, SetOptions.merge()).await()
        },
        customValue = patient.id,
        customMessage = "Error updating patient"
    )

    suspend fun deletePatient(id: String) = safeCallFirebase(
        firebaseCall = {
            val patientRef = db.collection(PATIENT_COLLECTION).document(id)
            val data = patientRef.collection(DATA_COLLECTION).get().await()
            data.documents.forEach {
                it.reference.delete().await()
            }
            patientRef.delete().await()
        }
    )

    suspend fun getJaw(id: String, isUpperJaw: Boolean) = safeCallFirebase(
        firebaseCall = {
            val dataId = if (isUpperJaw) KEY_MAXILLA else KEY_MANDIBLE
            val snapshot = db.collection(PATIENT_COLLECTION).document(id)
                .collection(DATA_COLLECTION).document(dataId).get().await()
            return@safeCallFirebase if (snapshot.exists()) snapshot.toJaw()
                else null
        },
        customValue = id,
        customMessage = "Error getting jaw data"
    )

    suspend fun getJaws(id: String) = safeCallFirebase(
        firebaseCall = {
            val collectionRef = db.collection(PATIENT_COLLECTION).document(id).collection(DATA_COLLECTION)
            val mandibleTask = collectionRef.document(KEY_MANDIBLE).get()
            val maxillaTask = collectionRef.document(KEY_MAXILLA).get()

            val result = Tasks.whenAllSuccess<DocumentSnapshot>(mandibleTask, maxillaTask).await()
            val mandible = if (result[0].exists()) result[0].toJaw() else null
            val maxilla = if (result[1].exists()) result[1].toJaw() else null
            return@safeCallFirebase Pair(mandible, maxilla)
        },
        customValue = id,
        customMessage = "Error getting jaw data"
    )

    suspend fun setJaw(id: String, isUpperJaw: Boolean, data: Jaw) = safeCallFirebase(
        firebaseCall = {
            val dataId = if (isUpperJaw) KEY_MAXILLA else KEY_MANDIBLE
            db.collection(PATIENT_COLLECTION).document(id)
                .collection(DATA_COLLECTION).document(dataId)
                .set(data.toMap(), SetOptions.merge()).await()
        },
        customValue = id,
        customMessage = "Error updating jaw data"
    )

    private fun <T> getRealtimeCollection(
        dbRef: CollectionReference,
        mapperFunction: (DocumentSnapshot) -> T?
    ) = callbackFlow<Resource<List<T>>> {
        trySend(Resource.Loading()).isSuccess
        val listenerRegistration = dbRef.addSnapshotListener { value, error ->
            if (error != null) {
                trySend(Resource.Error(error.localizedMessage.orEmpty())).isSuccess
            } else {
                val data = value?.documents?.mapNotNull { mapperFunction.invoke(it) }
                trySend(Resource.Success(data.orEmpty())).isSuccess
            }
        }

        awaitClose {
            listenerRegistration.remove()
        }
    }

    private fun <T> getRealtimeData(
        dbRef: DocumentReference,
        mapperFunction: (DocumentSnapshot) -> T?
    ) = callbackFlow<Resource<T>> {
        trySend(Resource.Loading()).isSuccess
        val listenerRegistration = dbRef.addSnapshotListener { value, error ->
            if (error != null) {
                trySend(Resource.Error(error.localizedMessage.orEmpty())).isSuccess
                return@addSnapshotListener
            }
            if (value == null || !value.exists()) {
                trySend(Resource.Error("Data tidak ditemukan")).isSuccess
                return@addSnapshotListener
            }

            val data = mapperFunction.invoke(value)
            if (data == null) {
                trySend(Resource.Error("Data gagal dimuat")).isSuccess
                return@addSnapshotListener
            }
            trySend(Resource.Success(data)).isSuccess
        }

        awaitClose {
            listenerRegistration.remove()
        }
    }

    private suspend fun <T> safeCallFirebase(
        firebaseCall: suspend () -> T,
        customKey: String = KEY_ID,
        customValue: String = getUserId(),
        customMessage: String = "Error getting data"
    ): T? = try {
        firebaseCall.invoke()
    } catch (e: Exception) {
        Log.e(TAG, customMessage, e)
        FirebaseCrashlytics.getInstance().log(customMessage)
        FirebaseCrashlytics.getInstance().setCustomKey(customKey, customValue)
        FirebaseCrashlytics.getInstance().recordException(e)
        throw e
    }

}