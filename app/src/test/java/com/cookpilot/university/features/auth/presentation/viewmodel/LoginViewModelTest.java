package com.cookpilot.university.features.auth.presentation.viewmodel;

import static org.junit.Assert.assertEquals;

import com.cookpilot.university.R;

import org.junit.Test;

public class LoginViewModelTest {
    @Test
    public void passwordValidationRequiresMinimumLengthOnlyForRegistration() {
        assertEquals(R.string.login_error_password_required,
                LoginViewModel.passwordErrorFor("", false));
        assertEquals(R.string.login_error_password_required,
                LoginViewModel.passwordErrorFor("", true));
        assertEquals(0, LoginViewModel.passwordErrorFor("12345", false));
        assertEquals(R.string.login_error_password_short,
                LoginViewModel.passwordErrorFor("12345", true));
        assertEquals(0, LoginViewModel.passwordErrorFor("123456", true));
    }
}
