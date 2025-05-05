import React, { useState } from 'react';
import {
  View,
  Text,
  TextInput,
  TouchableOpacity,
  StyleSheet,
  Alert,
} from 'react-native';
import axios from 'axios';
import { router } from 'expo-router';
import AsyncStorage from '@react-native-async-storage/async-storage';
import {API_URL} from '../constants/config';
import { registerDeviceToken } from '../services/notificationService';

export default function LoginScreen() {
  const [formData, setFormData] = useState({
    email: '',
    password: '',
  });

  const handleLogin = async () => {
    try {
      console.log('Tentative de connexion avec:', formData);
      
      const response = await axios.post(`${API_URL}/api/auth/login`, formData, {
        headers: {
          'Content-Type': 'application/json',
        },
      });
      console.log('Réponse du serveur:', response.data);

      if (response.data && response.data.token) {
        // Stocker le token
        await AsyncStorage.setItem('token', response.data.token);
        
        // Stocker les informations de la pharmacie
        if (response.data.pharmacy) {
          await AsyncStorage.setItem('pharmacyData', JSON.stringify(response.data.pharmacy));
          
          // Enregistrer le token de notification
          try {
            await registerDeviceToken(response.data.pharmacy.id, response.data.token);
            console.log('Token de notification enregistré avec succès');
          } catch (error) {
            console.error('Erreur lors de l\'enregistrement du token de notification:', error);
            // On continue même si l'enregistrement du token échoue
          }
        }

        const data = await AsyncStorage.getItem('pharmacyData');
        console.log('veriiiif',data)
        // Rediriger vers le dashboard
        router.replace('/(drawer)');
      } else {
        Alert.alert('Erreur', 'Réponse invalide du serveur');
      }
    } catch (error) {
      console.error('Erreur de connexion:', error);
      
      if (axios.isAxiosError(error)) {
        const errorMessage = error.response?.data?.message || error.message || 'Une erreur est survenue';
        Alert.alert('Erreur', errorMessage);
      } else {
        Alert.alert('Erreur', 'Une erreur inattendue est survenue');
      }
    }
  };

  return (
    <View style={styles.container}>
      <View style={styles.formContainer}>
        <Text style={styles.title}>Welcome Back</Text>

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

        <TouchableOpacity style={styles.button} onPress={handleLogin}>
          <Text style={styles.buttonText}>Login</Text>
        </TouchableOpacity>

        <TouchableOpacity 
          style={styles.linkButton}
          onPress={() => router.push('/signup')}
        >
          <Text style={styles.linkText}>Don't have an account? Sign Up</Text>
        </TouchableOpacity>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#fff',
  },
  formContainer: {
    padding: 20,
    flex: 1,
    justifyContent: 'center',
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
  },
  linkText: {
    color: '#007AFF',
    textAlign: 'center',
    fontSize: 14,
  },
}); 