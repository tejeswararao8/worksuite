import { useState } from 'react';
import { View, Text, StyleSheet, KeyboardAvoidingView, Platform, ScrollView } from 'react-native';
import { useDispatch } from 'react-redux';
import { authApi } from '../../../../shared/api/services';
import { setUser } from '../../../../shared/store/authSlice';
import { storageService, getErrorMessage } from '../../../../shared/utils';
import type { AppDispatch } from '../../../../shared/store';
import type { TokenResponse } from '../../../../shared/types';
import { MButton, MInput, COLORS } from '../../components';

export default function LoginScreen() {
  const dispatch = useDispatch<AppDispatch>();
  const [email, setEmail]       = useState('');
  const [password, setPassword] = useState('');
  const [error, setError]       = useState('');
  const [loading, setLoading]   = useState(false);

  const handleLogin = async () => {
    if (!email || !password) { setError('Email and password are required'); return; }
    setLoading(true);
    setError('');
    try {
      const { data } = await authApi.login({ email, password });
      const token = data.data as TokenResponse;

      storageService.setAccessToken(token.accessToken);
      storageService.setRefreshToken(token.refreshToken);

      const userData = { userId: token.userId, companyId: token.companyId, email: token.email, role: token.role };
      storageService.setUser(userData);
      dispatch(setUser(userData));
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  return (
    <KeyboardAvoidingView style={styles.container} behavior={Platform.OS === 'ios' ? 'padding' : 'height'}>
      <ScrollView contentContainerStyle={styles.scroll} keyboardShouldPersistTaps="handled">
        {/* Logo */}
        <View style={styles.logoContainer}>
          <View style={styles.logo}>
            <Text style={styles.logoText}>W</Text>
          </View>
          <Text style={styles.title}>WorkSuite</Text>
          <Text style={styles.subtitle}>Sign in to your account</Text>
        </View>

        {/* Form */}
        <View style={styles.form}>
          <MInput
            label="Email"
            value={email}
            onChangeText={setEmail}
            placeholder="admin@company.com"
            keyboardType="email-address"
          />
          <MInput
            label="Password"
            value={password}
            onChangeText={setPassword}
            placeholder="••••••••"
            secureTextEntry
          />

          {error ? <Text style={styles.errorText}>{error}</Text> : null}

          <MButton onPress={handleLogin} loading={loading} style={styles.loginBtn}>
            Sign In
          </MButton>
        </View>
      </ScrollView>
    </KeyboardAvoidingView>
  );
}

const styles = StyleSheet.create({
  container:       { flex: 1, backgroundColor: COLORS.slate900 },
  scroll:          { flexGrow: 1, justifyContent: 'center', padding: 24 },
  logoContainer:   { alignItems: 'center', marginBottom: 40 },
  logo:            { width: 72, height: 72, borderRadius: 20, backgroundColor: COLORS.primary, alignItems: 'center', justifyContent: 'center', marginBottom: 16 },
  logoText:        { fontSize: 32, fontWeight: '800', color: COLORS.white },
  title:           { fontSize: 28, fontWeight: '800', color: COLORS.white, marginBottom: 4 },
  subtitle:        { fontSize: 14, color: COLORS.slate400 },
  form:            { backgroundColor: COLORS.white, borderRadius: 20, padding: 24, gap: 16 },
  errorText:       { fontSize: 13, color: COLORS.danger, textAlign: 'center' },
  loginBtn:        { marginTop: 8 },
});
