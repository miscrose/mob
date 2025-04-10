import React, { useState } from 'react';
import {
  View,
  Text,
  TextInput,
  TouchableOpacity,
  StyleSheet,
  Alert,
  ScrollView,
} from 'react-native';
import axios from 'axios';
import { router } from 'expo-router';

// Configuration de l'URL de l'API
//const API_URL = 'http://192.168.1.101:8080'; // Votre adresse IP locale
const API_URL = 'http://localhost:8080';
export default function SignUpScreen() {
  const [formData, setFormData] = useState({
    name: '',
    email: '',
    password: '',
    address: '',
    phone: '',
  });

  const handleSignUp = async () => {
    try {
      const response = await axios.post(`${API_URL}/api/auth/signup`, formData);
      Alert.alert('Succès', 'Inscription réussie !');
      router.replace('/login');
    } catch (error) {
      if (axios.isAxiosError(error)) {
        Alert.alert('Erreur', error.response?.data?.message || 'Une erreur est survenue');
      } else {
        Alert.alert('Erreur', 'Une erreur inattendue est survenue');
      }
    }
  };

  return (
    <ScrollView style={styles.container}>
      <View style={styles.formContainer}>
        <Text style={styles.title}>Create Pharmacy Account</Text>

        <TextInput
          style={styles.input}
          placeholder="Pharmacy Name"
          value={formData.name}
          onChangeText={(text) => setFormData({ ...formData, name: text })}
        />

        <TextInput
          style={styles.input}
          placeholder="Email"
          keyboardType="email-address"
          autoCapitalize="none"
          value={formData.email}
          onChangeText={(text) => setFormData({ ...formData, email: text })}
        />

        <TextInput
          style={styles.input}
          placeholder="Password"
          secureTextEntry
          value={formData.password}
          onChangeText={(text) => setFormData({ ...formData, password: text })}
        />

        <TextInput
          style={styles.input}
          placeholder="Address"
          value={formData.address}
          onChangeText={(text) => setFormData({ ...formData, address: text })}
        />

        <TextInput
          style={styles.input}
          placeholder="Phone Number"
          keyboardType="phone-pad"
          value={formData.phone}
          onChangeText={(text) => setFormData({ ...formData, phone: text })}
        />

        <TouchableOpacity style={styles.button} onPress={handleSignUp}>
          <Text style={styles.buttonText}>Sign Up</Text>
        </TouchableOpacity>

        <TouchableOpacity 
          style={styles.linkButton}
          onPress={() => router.push('/login')}
        >
          <Text style={styles.linkText}>Already have an account? Login</Text>
        </TouchableOpacity>
      </View>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#fff',
  },
  formContainer: {
    padding: 20,
  },
  title: {
    fontSize: 24,
    fontWeight: 'bold',
    color: '#333',
    marginBottom: 30,
    textAlign: 'center',
  },
  input: {
    backgroundColor: '#f5f5f5',
    padding: 15,
    borderRadius: 10,
    marginBottom: 15,
    fontSize: 16,
  },
  button: {
    backgroundColor: '#007AFF',
    padding: 15,
    borderRadius: 10,
    marginTop: 10,
  },
  buttonText: {
    color: '#fff',
    textAlign: 'center',
    fontSize: 16,
    fontWeight: 'bold',
  },
  linkButton: {
    marginTop: 20,
    marginBottom: 20,
  },
  linkText: {
    color: '#007AFF',
    textAlign: 'center',
    fontSize: 14,
  },
}); 