package ro.alexmamo.firebasesigninwithgoogle.presentation.profile.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import ro.alexmamo.firebasesigninwithgoogle.components.ProgressBar
import ro.alexmamo.firebasesigninwithgoogle.core.Utils.Companion.print
import ro.alexmamo.firebasesigninwithgoogle.domain.model.Response.*
import ro.alexmamo.firebasesigninwithgoogle.presentation.auth.AuthViewModel

@Composable
fun RevokeAccess(
    viewModel: AuthViewModel = hiltViewModel(),
    navigateToAuthScreen: @Composable (accessRevoked: Boolean) -> Unit,
    showSnackBar: () -> Unit
) {
    when(val revokeAccessResponse = viewModel.revokeAccessResponse) {
        is Loading -> ProgressBar()
        is Success -> revokeAccessResponse.data?.let { accessRevoked ->
            navigateToAuthScreen(accessRevoked)
        }
        is Failure -> LaunchedEffect(Unit) {
            print(revokeAccessResponse.e)
            showSnackBar()
        }
    }
}