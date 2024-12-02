package com.app.householdtracing.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.app.householdtracing.R
import com.app.householdtracing.ui.theme.primaryDarkColor
import com.app.householdtracing.ui.theme.primaryTextColorDark
import com.app.householdtracing.ui.viewmodels.LoginScreenViewModel
import com.app.householdtracing.ui.viewmodels.LoginUiState
import com.app.householdtracing.ui.views.CustomTextField
import com.app.householdtracing.ui.views.SmallButton
import org.koin.androidx.compose.koinViewModel

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun LoginScreen(
    onLoginClick: () -> Unit
) {

    val loginScreenVM: LoginScreenViewModel = koinViewModel()
    val loginState = loginScreenVM.authState

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = primaryTextColorDark
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Image(
                modifier = Modifier
                    .fillMaxWidth()
                    .zIndex(1f),
                alignment = Alignment.TopCenter,
                contentScale = ContentScale.Crop,
                painter = painterResource(id = R.drawable.logincurvedbg),
                contentDescription = null
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.TopCenter)
                    .padding(bottom = 24.dp)
                    .zIndex(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center

            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        modifier = Modifier
                            .fillMaxWidth()
                            .zIndex(1f),
                        alignment = Alignment.TopCenter,
                        contentScale = ContentScale.Crop,
                        painter = painterResource(id = R.drawable.ic_login_icon),
                        contentDescription = null
                    )//image
                    Text(
                        text = stringResource(R.string.tv_track_shopping_mission),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 30.sp,
                        color = primaryDarkColor,
                        maxLines = 1,
                        lineHeight = 36.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 24.dp)
                    )//text
                    Spacer(modifier = Modifier
                        .fillMaxWidth()
                        .height(76.dp))

                    CustomTextField(
                        text = stringResource(R.string.tv_username),
                        value = loginScreenVM.emailTextField.value,
                        placeholder = stringResource(R.string.tv_username_placeholder),
                        onValueChange = {
                            loginScreenVM.emailTextField.value = it
                        }
                    )
                    Spacer(modifier = Modifier
                        .fillMaxWidth()
                        .height(16.dp))
                    CustomTextField(
                        text = stringResource(R.string.tv_password),
                        value = loginScreenVM.passwordTextField.value,
                        placeholder = stringResource(R.string.tv_password_placeholder),
                        onValueChange = {
                            loginScreenVM.passwordTextField.value = it
                        }
                    )

                    Spacer(modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp))
                    SmallButton(
                        text = stringResource(R.string.btn_login),
                        onClick = {
                            loginScreenVM.authenticate(loginScreenVM.emailTextField.value, loginScreenVM.passwordTextField.value)
                        }
                    )//login button
                    Spacer(modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp))

                    when (loginState) {
                        is LoginUiState.Idle -> Text("Please Enter Login credentials...")
                        is LoginUiState.Loading -> CircularProgressIndicator()
                        is LoginUiState.Success -> Text("Login successful: ${loginState.data.token}")
                        is LoginUiState.Error -> Text("Error: ${loginState.error}", color = Color.Red)
                    }
                }

                Image(
                    modifier = Modifier
                        .fillMaxWidth()
                        .zIndex(1f),
                    alignment = Alignment.BottomCenter,
                    contentScale = ContentScale.None,
                    painter = painterResource(id = R.drawable.ic_company_logo),
                    contentDescription = null
                )
            }

        }

    }
}