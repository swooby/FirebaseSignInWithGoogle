package ro.alexmamo.firebasesigninwithgoogle.data.repository

import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue.serverTimestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import ro.alexmamo.firebasesigninwithgoogle.core.Constants.CREATED_AT
import ro.alexmamo.firebasesigninwithgoogle.core.Constants.DISPLAY_NAME
import ro.alexmamo.firebasesigninwithgoogle.core.Constants.EMAIL
import ro.alexmamo.firebasesigninwithgoogle.core.Constants.PHOTO_URL
import ro.alexmamo.firebasesigninwithgoogle.core.Constants.SIGN_IN_REQUEST
import ro.alexmamo.firebasesigninwithgoogle.core.Constants.SIGN_UP_REQUEST
import ro.alexmamo.firebasesigninwithgoogle.core.Constants.USERS
import ro.alexmamo.firebasesigninwithgoogle.domain.model.Response.*
import ro.alexmamo.firebasesigninwithgoogle.domain.repository.AuthRepository
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl  @Inject constructor(
    private val auth: FirebaseAuth,
    private var oneTapClient: SignInClient,
    @Named(SIGN_IN_REQUEST)
    private var signInRequest: BeginSignInRequest,
    @Named(SIGN_UP_REQUEST)
    private var signUpRequest: BeginSignInRequest,
    private val db: FirebaseFirestore
) : AuthRepository {
    override val isUserAuthenticatedInFirebase = auth.currentUser != null

    override fun oneTapSignInWithGoogle() = flow {
        try {
            emit(Loading)
            val signInResult = oneTapClient.beginSignIn(signInRequest).await()
            emit(Success(signInResult))
        } catch (e: Exception) {
            try {
                val signUpResult = oneTapClient.beginSignIn(signUpRequest).await()
                emit(Success(signUpResult))
            } catch (e: Exception) {
                emit(Failure(e))
            }
        }
    }

    override fun firebaseSignInWithGoogle(googleCredential: AuthCredential) = flow {
        try {
            emit(Loading)
            val authResult = auth.signInWithCredential(googleCredential).await()
            val isNewUser = authResult.additionalUserInfo?.isNewUser ?: false
            if (isNewUser) {
                addUserToFirestore()
            }
            emit(Success(true))
        } catch (e: Exception) {
            emit(Failure(e))
        }
    }

    private suspend fun addUserToFirestore() {
        auth.currentUser?.apply {
            val user = toUser()
            db.collection(USERS).document(uid).set(user).await()
        }
    }
}

fun FirebaseUser.toUser() = mapOf(
    DISPLAY_NAME to displayName,
    EMAIL to email,
    PHOTO_URL to photoUrl?.toString(),
    CREATED_AT to serverTimestamp()
)